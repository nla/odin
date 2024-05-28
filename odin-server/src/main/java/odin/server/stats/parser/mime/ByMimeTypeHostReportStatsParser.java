package odin.server.stats.parser.mime;

import java.util.HashMap;

import loki.server.text.TextFileLineReceiver;
import loki.server.text.TextFileReaderService;
import odin.server.stats.parser.HostReportStatsParser;
import odin.stats.DomainStats;
import odin.stats.DomainStatsConstants;
import odin.stats.DomainStatsCounts;

/**
 * Parses stats for the by_mime_types category.
 * Categories include generic groupings of mime types (such as audio, video, etc as specified in documents/mime_type_groups.txt) 
 * from the harvest mime types report associated with the specified hosts report.
 */

public class ByMimeTypeHostReportStatsParser implements HostReportStatsParser
{
    public void initialise() throws Exception
    {
        // Nothing to initialise
    }
    
    public DomainStats parseStats(String reportPath) throws Exception
    {
        reportPath = reportPath.replace("harvest_host_reports", "harvest_mime_reports");
        DomainStats stats = new DomainStats();
        TextFileReaderService service = new TextFileReaderService();
        
        HashMap<String, String> mimeTypeToGroupMap = new HashMap<String, String>();
        
        service.iterateLines("documents/mime_type_groups.txt", new TextFileLineReceiver(){
            public void onLine(int index, String line) throws Exception
            {
                String group = line.trim();
                
                service.iterateLines("documents/mime_types__"+group+".txt", new TextFileLineReceiver(){
                    public void onLine(int index, String line) throws Exception
                    {
                        mimeTypeToGroupMap.put(line.trim(), group);
                    }
                });
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
                
                String[] entrySplit = line.split("\\s+");
                String mime = entrySplit[2].trim();
                
                String group = mimeTypeToGroupMap.get(mime);
                
                if(group==null)
                {
                    group = "miscellaneous";
                }
                
                long urlCount = Long.parseLong(entrySplit[0]);
                long byteCount = Long.parseLong(entrySplit[1]);
                
                DomainStatsCounts counts = stats.getCategoryToCountsMap().get(group);
                
                if(counts==null)
                {
                    counts = new DomainStatsCounts();
                    stats.getCategoryToCountsMap().put(group, counts);
                }
                
                counts.increment(DomainStatsConstants.STATS_VIEW_COUNT_TYPE__DOMAIN_COUNT, 0);
                counts.increment(DomainStatsConstants.STATS_VIEW_COUNT_TYPE__URL_COUNT, urlCount);
                counts.increment(DomainStatsConstants.STATS_VIEW_COUNT_TYPE__MB_SIZE, Math.max(1, (byteCount/1024)/1024));
            }
        });
        
        return stats;
    }
}