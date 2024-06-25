package odin.server.stats.parser.geo;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;

import loki.server.text.TextFileLineReceiver;
import loki.server.text.TextFileReaderService;
import loki.server.util.ServerUtils;
import odin.server.host.HostInformation;
import odin.server.stats.parser.HostReportStatsParser;
import odin.stats.DomainStats;
import odin.stats.DomainStatsConstants;
import odin.stats.DomainStatsCounts;
import thor.storage.StorableNotFoundException;
import thor.storage.file.FileStorageService;

/**
 * Parses stats for the by_geolocation category by using the list of domains in the hosts report, looking up their IP address, and 
 * finding the estimated Australian state hosting it from the maxmind database. Anything outside Australia is grouped into 'Offshore'.
 */

public class ByGeoLocationHostReportStatsParser implements HostReportStatsParser
{
    private FileStorageService<HostInformation> hostInformationStorageService;
    private int threadPoolCount;
    
    public void initialise() throws Exception
    {
        hostInformationStorageService = new FileStorageService<HostInformation>(
                "storage/host_information/",
                "storage/_temp/host_information/");
        threadPoolCount = Integer.parseInt(ServerUtils.loadFromFile("config/geolocation.properties").get("geolocation_host_reporter_thread_pool", "5"));
    }
    
    public DomainStats parseStats(String reportPath) throws Exception
    {
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolCount);
        
        DomainStats stats = new DomainStats();
        ReentrantLock statsLock = new ReentrantLock();
        
        File database = new File("source_data/geolite/GeoLite2-City.mmdb");
        TextFileReaderService service = new TextFileReaderService();
        
        try(DatabaseReader reader = new DatabaseReader.Builder(database).build())
        {
            service.iterateLines(reportPath, new TextFileLineReceiver(){
                public void onLine(int index, String line) throws Exception
                {
                    if(index==0)
                    {
                        return;
                    }
                    
                    line = line.trim();
                    
                    if(line.isEmpty())
                    {
                        return;
                    }
                    
                    String[] hostEntrySplit = line.split("\\s+");
                    String urlString = hostEntrySplit[2];
                    
                    if(!urlString.contains("://"))
                    {
                        urlString = "http://"+urlString;
                    }
                    
                    URL url;
                    
                    try
                    {
                        url = new URL(urlString);
                    }
                    catch(MalformedURLException e)
                    {
                        //System.out.println("[MALFORMED] "+urlString);
                        return;
                    }
                    
                    final String domain = url.getHost();
                    long urlCount = Long.parseLong(hostEntrySplit[0]);
                    long byteCount = Long.parseLong(hostEntrySplit[1]);
                    
                    if(!domain.endsWith(".au"))
                    {
                        return;
                    }
                    
                    executorService.execute(new Runnable(){
                       public void run()
                       {
                           String storableId = getStorableIdForDomain(domain);
                           
                           try
                           {
                               HostInformation information;
                               
                               try
                               {
                                   statsLock.lock();
                                   
                                   try
                                   {
                                       information = hostInformationStorageService.load(storableId);
                                   }
                                   finally
                                   {
                                       statsLock.unlock();
                                   }
                               }
                               catch(StorableNotFoundException e)
                               {
                                   String ipAddress = "";
                                   String stateName = "Unknown";
                                   String cityName = "Unknown";
                                   
                                   try
                                   {
                                       InetAddress address = InetAddress.getByName(domain);
                                       ipAddress = address.getHostAddress();
                                       //System.out.println("Address is "+address);
                                       CityResponse city = reader.city(address);
                                       //System.out.println("City is "+city.getCity().getName());
                                       
                                       if(city!=null)
                                       {
                                           cityName = city.getCity().getName();
                                           
                                           if(city.getCountry()!=null && city.getCountry().getName()!=null)
                                           {
                                               if(!city.getCountry().getName().equals("Australia"))
                                               {
                                                   stateName = "Offshore";
                                               }
                                               else
                                               {
                                                   if(!city.getSubdivisions().isEmpty())
                                                   {
                                                       stateName = city.getSubdivisions().get(0).getName();
                                                       //System.out.println("Location is "+stateName);
                                                   }
                                               }
                                           }
                                       }
                                   }
                                   catch(UnknownHostException x){}
                                   catch(AddressNotFoundException x){}
                                   
                                   information = new HostInformation(domain, ipAddress, stateName, cityName);
                                   statsLock.lock();
                                   
                                   try
                                   {
                                       hostInformationStorageService.store(storableId, information);
                                   }
                                   catch(Exception x)
                                   {
                                       System.out.println("BAD ID: "+storableId);
                                       x.printStackTrace();
                                   }
                                   finally
                                   {
                                       statsLock.unlock();
                                   }
                               }
                               
                               statsLock.lock();
                               
                               try
                               {
                                   DomainStatsCounts counts = stats.getCategoryToCountsMap().get(information.getState());
                                   
                                   if(counts==null)
                                   {
                                       counts = new DomainStatsCounts();
                                       stats.getCategoryToCountsMap().put(information.getState(), counts);
                                   }
                                   
                                   counts.increment(DomainStatsConstants.STATS_VIEW_COUNT_TYPE__DOMAIN_COUNT, 1);
                                   counts.increment(DomainStatsConstants.STATS_VIEW_COUNT_TYPE__URL_COUNT, urlCount);
                                   counts.increment(DomainStatsConstants.STATS_VIEW_COUNT_TYPE__MB_SIZE, Math.max(1, (byteCount/1024)/1024));
                               }
                               finally
                               {
                                   statsLock.unlock();
                               }
                           }
                           catch(Exception e)
                           {
                               e.printStackTrace();
                           }
                       }
                    });
                }
            });
            
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        
        return stats;
    }
    
    private String getStorableIdForDomain(String domain)
    {
        return domain.replace(".", "_");
    }
}
