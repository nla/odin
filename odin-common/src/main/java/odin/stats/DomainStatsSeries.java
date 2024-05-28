package odin.stats;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

import thor.Storable;

/**
* Holds a map of time values to domain statistics
*/

public class DomainStatsSeries implements Storable, IsSerializable
{
    private static final long serialVersionUID = 1L;
    private HashMap<Long, DomainStats> statsMap;
    
    public DomainStatsSeries()
    {
        this.statsMap = new HashMap<Long, DomainStats>();
    }
    
    /**
    * Gets the map of values to domain statistics.
    * 
    * @return the map of values to domain statistics.
    */

    public HashMap<Long, DomainStats> getStatsMap()
    {
        return statsMap;
    }
}
