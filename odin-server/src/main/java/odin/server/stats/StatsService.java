package odin.server.stats;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import loki.Attributes;
import loki.StringPair;
import loki.server.util.ServerUtils;
import odin.server.stats.parser.HostReportStatsParser;
import odin.stats.DomainStats;
import odin.stats.DomainStatsConstants;
import odin.stats.DomainStatsSeries;
import thor.storage.StorableNotFoundException;
import thor.storage.file.FileStorageService;
import thor.util.LockService;
import thor.util.LockedTask;

/**
 * A service responsible for generating, storing, and retrieving statistics for the ODIN application
 */

public class StatsService
{
    private LockService lockService;
    private HashMap<String, FileStorageService<DomainStats>> hostsStatsStorageServices;
    private HashMap<String, HostReportStatsParser> hostsStatsParsers;
    
    public StatsService() throws Exception
    {
        lockService = new LockService();
        hostsStatsParsers = new HashMap<String, HostReportStatsParser>();
        hostsStatsStorageServices = new HashMap<String, FileStorageService<DomainStats>>();
        
        Attributes<String> parserProperties = ServerUtils.loadFromFile("config/host_stats_parsers.properties");
        
        for(StringPair category: DomainStatsConstants.STATS_VIEW_CATEGORIES)
        {
            hostsStatsParsers.put(category.getKey(), (HostReportStatsParser)Class.forName(parserProperties.get(category.getKey())).getDeclaredConstructor().newInstance());
            hostsStatsParsers.get(category.getKey()).initialise();
            hostsStatsStorageServices.put(category.getKey(), new FileStorageService<DomainStats>(
                    "storage/stats/hosts/"+category.getKey()+"/",
                    "storage/_temp/stats/hosts/"+category.getKey()+"/"));
        }
    }
    
    /**
     * Retrieves a series of statistics for the category provided and over the time frame specified. 
     * 
     * @param category   The category of statistics to retrieve. The valid options are specified in {@link odin.stats.DomainStatsConstants}.
     * @param earliest   The start of the time period from which to retrieve statistics.
     * @param latest     The end of the time period from which to retrieve statistics.
     * @return           A series of statistics for the category provided and over the time frame specified.
     */
    
    public DomainStatsSeries getDomainStatsSeries(String category, long earliest, long latest) throws Exception
    {
        try
        {
            FileStorageService<DomainStats> storageService = hostsStatsStorageServices.get(category);
            SimpleDateFormat reportFilenameFormat = new SimpleDateFormat("yyyy_MM");
            DomainStatsSeries series = new DomainStatsSeries();
            
            for(File file: new File("source_data/harvest_host_reports/").listFiles())
            {
                String id = file.getName().substring(0, file.getName().indexOf("."));
                long time = reportFilenameFormat.parse(id).getTime();
                
                if(time>=earliest && time<=latest)
                {
                    DomainStats stats = lockService.runLockedTask(category+"__"+id, new LockedTask<DomainStats>(){
                        public DomainStats execute() throws Exception
                        {
                            try
                            {
                                return storageService.load(id);
                            }
                            catch(StorableNotFoundException e)
                            {
                                try
                                {
                                    DomainStats stats = hostsStatsParsers.get(category).parseStats(file.getAbsolutePath());
                                    storageService.store(id, stats);
                                    return stats; 
                                }
                                catch(Exception x)
                                {
                                    ServerUtils.log(x);
                                    throw x;
                                }
                            }
                        }
                    });
                    
                    series.getStatsMap().put(time, stats);
                }
            }
            
            return series;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }
}