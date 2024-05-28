package odin.request;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import odin.stats.DomainStatsSeries;

/**
* The remote service used by client implementations to retrieve statistics data from the server.
*/

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService 
{
    /**
    * Retrieves a series of statistics for the category provided and over the time frame specified. 
    * 
    * @param category   The category of statistics to retrieve. The valid options are specified in {@link odin.stats.DomainStatsConstants}.
    * @param earliest   The start of the time period from which to retrieve statistics.
    * @param latest     The end of the time period from which to retrieve statistics.
    * @return           A series of statistics for the category provided and over the time frame specified.
    */
    
	public DomainStatsSeries getDomainStatsSeries(String category, long earliest, long latest) throws Exception;
}