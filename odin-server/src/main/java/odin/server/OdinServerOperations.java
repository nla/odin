package odin.server;

import loki.server.ServerOperations;
import odin.server.stats.StatsService;

/**
* The ServerOptions implementation for the ODIN server.
* Holds a StatsService for retrieving statistics.
*/

public class OdinServerOperations implements ServerOperations
{
    private StatsService statsService;

    public OdinServerOperations() throws Exception
    {
        this.statsService = new StatsService();
    }
    
    /**
    * Returns the service service.
    * 
    * @return   The stats service instance for retrieving statistics.
    */

    public StatsService getStatsService()
    {
        return statsService;
    }
}
