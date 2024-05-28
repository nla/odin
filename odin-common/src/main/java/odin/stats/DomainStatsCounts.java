package odin.stats;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

import thor.Storable;

/**
* Holds a map of statistic types to statistics values
*/

public class DomainStatsCounts implements Storable, IsSerializable
{
    private static final long serialVersionUID = 1L;
    
    private HashMap<String, Long> countsMap;
    
    public DomainStatsCounts()
    {
        this.countsMap = new HashMap<String, Long>();
    }
    
    /**
    * Get the map of statistic types to statistics values.
    * 
    * @return the map of statistic types to statistics values.
    */

    public HashMap<String, Long> getCountsMap()
    {
        return countsMap;
    }
    
    /**
     * Increments the value of the specified statistics type by the amount specified
     * 
     * @param type      The statistics type to increment.
     * @param amount    The amount it is to be incremented by.
     */
    
    public void increment(String type, long amount)
    {
        Long count = countsMap.get(type);
        
        if(count==null)
        {
            countsMap.put(type, amount);
        }
        else
        {
            countsMap.put(type, count+amount);
        }
    }
}