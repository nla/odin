package odin.stats;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

import thor.Storable;

/**
* Holds a map of category keys to statistics values
*/

public class DomainStats implements Storable, IsSerializable
{
    private static final long serialVersionUID = 1L;
    private HashMap<String, DomainStatsCounts> categoryToCountsMap;
    
    public DomainStats()
    {
        this.categoryToCountsMap = new HashMap<String, DomainStatsCounts>();
    }
    
    /**
     * Retrieves the map of category keys to statistics values. 
     * 
     * @return  The map.
     */

    public HashMap<String, DomainStatsCounts> getCategoryToCountsMap()
    {
        return categoryToCountsMap;
    }
}