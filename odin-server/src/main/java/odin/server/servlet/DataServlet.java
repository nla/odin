package odin.server.servlet;

import loki.server.request.RequestServlet;
import loki.server.util.ServerUtils;
import odin.request.DataService;
import odin.server.OdinServerOperations;
import odin.stats.DomainStatsSeries;

/**
* RequestServlet implementation of the DataService remote service.
* This servlet responds to web client method calls via DataService/DataServiceAsync.
*/

public class DataServlet extends RequestServlet<OdinServerOperations> implements DataService
{
    private static final long serialVersionUID = 1L;
    
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
        ServerUtils.log("Stats requested: "+category);
        return super.getOperations().getStatsService().getDomainStatsSeries(category, earliest, latest);
    }
}