package odin.server.stats.parser;

import odin.stats.DomainStats;

/**
 * Implementations are responsible for generating statistics for a given date, as specified by the path of the hosts report file for a given harvest.
 */

public interface HostReportStatsParser
{
    /**
     * Called at the server startup.
     * Implementations should override this to perform any initialisation required.
     */
    
    public void initialise() throws Exception;
    
    /**
     * Called when statistics for the given hosts report file/date need to be parsed.
     * Implementations should override this to perform the parsing and return a DomainStats object.
     * 
     * @param reportPath        The path to the hosts reports file for a given harvest. The file name uses a date in the format yyyy-MM.txt
     * @return                  The parsed statistics.
     */
    
    public DomainStats parseStats(String reportPath) throws Exception;
}