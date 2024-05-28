package odin.request;

import com.google.gwt.user.client.rpc.AsyncCallback;
import odin.stats.DomainStatsSeries;

/**
* The asynchronous clone of {@link DataService}.
* 
* Used by client implementations to retrieve statistics data from the server.
*/

public interface DataServiceAsync
{
    /**
     * Retrieves a series of statistics for the category provided and over the time frame specified. 
     * 
     * @param category   The category of statistics to retrieve. The valid options are specified in {@link odin.stats.DomainStatsConstants}.
     * @param earliest   The start of the time period from which to retrieve statistics.
     * @param latest     The end of the time period from which to retrieve statistics.
     * @param callback   The callback to retrieve the statistics.
     */
    
	public void getDomainStatsSeries(String category, long earliest, long latest, AsyncCallback<DomainStatsSeries> callback);
}
