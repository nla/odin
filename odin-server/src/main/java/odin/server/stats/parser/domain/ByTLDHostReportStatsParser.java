package odin.server.stats.parser.domain;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import loki.server.text.TextFileLineReceiver;
import loki.server.text.TextFileReaderService;
import odin.server.stats.parser.HostReportStatsParser;
import odin.stats.DomainStats;
import odin.stats.DomainStatsConstants;
import odin.stats.DomainStatsCounts;

/**
 * Parses stats for the by_tld category.
 * Categories include all the high level domains of interest, as a combination of those in the documents/tld_au.txt file 
 * and /documents/states_au.txt file. Only includes domains that end with '.au'.
 */

public class ByTLDHostReportStatsParser implements HostReportStatsParser
{
    public void initialise() throws Exception
    {
        // Nothing to initialise
    }
    
    public DomainStats parseStats(String reportPath) throws Exception
    {
        DomainStats stats = new DomainStats();
        TextFileReaderService service = new TextFileReaderService();
        
        List<String> stateList = service.readAllLines("documents/states_au.txt");
        List<String> tldList = service.readAllLines("documents/tld_au.txt");
        Collections.sort(tldList, new Comparator<String>(){
            public int compare(String a, String b)
            {
                return -Integer.compare(a.length(), b.length());
            }
        });
        
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
                
                String domain = url.getHost();
                long urlCount = Long.parseLong(hostEntrySplit[0]);
                long byteCount = Long.parseLong(hostEntrySplit[1]);
                
                if(!domain.endsWith(".au"))
                {
                    return;
                }
                
                boolean found = false;
                                
                for(String tld: tldList)
                {
                    if(tld.startsWith(".gov") || tld.startsWith(".edu") || tld.equals(".au"))
                    {
                        for(String state: stateList)
                        {
                            String d = "."+state+tld;
                            
                            if(domain.endsWith(d))
                            {
                                domain = d;
                                found = true;
                                break;
                            }
                        }
                        if(found)
                        {
                            break;
                        }
                    }
                    if(domain.endsWith(tld))
                    {
                        domain = tld;
                        break;
                    }
                }
                
                DomainStatsCounts counts = stats.getCategoryToCountsMap().get(domain);
                
                if(counts==null)
                {
                    counts = new DomainStatsCounts();
                    stats.getCategoryToCountsMap().put(domain, counts);
                }
                
                counts.increment(DomainStatsConstants.STATS_VIEW_COUNT_TYPE__DOMAIN_COUNT, 1);
                counts.increment(DomainStatsConstants.STATS_VIEW_COUNT_TYPE__URL_COUNT, urlCount);
                counts.increment(DomainStatsConstants.STATS_VIEW_COUNT_TYPE__MB_SIZE, Math.max(1, (byteCount/1024)/1024));
            }
        });
        
        return stats;
    }
}
