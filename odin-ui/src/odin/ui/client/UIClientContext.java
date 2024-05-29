package odin.ui.client;

import com.google.gwt.core.client.GWT;
import loki.ErrorHandler;
import loki.ui.client.ClientContext;
import loki.ui.client.Navigator;
import odin.request.DataService;
import odin.request.DataServiceAsync;

/**
 * The Client Context of the ODIN application. Creates and holds a remote service instance of the DataServiceAsync interface for retrieving statistics data from the server. 
 * Holds the Navigator to be used by Screen instances to navigate to other screens.
 */

public class UIClientContext extends ClientContext
{
    private static final boolean DEV = false;
    private static final String BACKEND_HOST = DEV ? "http://localhost:2901" : "http://spade.nla.gov.au:2901";

    private final DataServiceAsync dataService = createServiceTarget(GWT.create(DataService.class), BACKEND_HOST + "/data");

    private ErrorHandler errorHandler;
    private Navigator<UIClientContext> navigator;

    /**
     * Initialises a new instance with the specified error handler.
     * 
     * @param errorHandler The error handler responsible for handling errors occurring within screens.
     */

    public UIClientContext(ErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
    }

    /**
     * @return The asynchronous data service for retrieving statistics data from the server.
     */

    public DataServiceAsync getDataService()
    {
        return dataService;
    }

    /**
     * @return The error handler responsible for handling errors occurring within screens.
     */

    public ErrorHandler getErrorHandler()
    {
        return errorHandler;
    }

    /**
     * @return The navigator, which can be used to navigate to other screens and update the page URL.
     */

    public Navigator<UIClientContext> getNavigator()
    {
        return navigator;
    }

    protected void setNavigator(Navigator<UIClientContext> navigator)
    {
        this.navigator = navigator;
    }
}
