package odin.server;

import loki.ErrorHandler;
import loki.server.Server;
import loki.server.util.ServerUtils;

/**
* The main class for starting the ODIN Server.
*/

public class OdinServer extends Server<OdinServerOperations>
{
    public static void main(String[] args) throws Exception
    {
        ServerUtils.log("Starting ODIN Server...");
        long start = System.currentTimeMillis();
        
        ErrorHandler errorListener = new ErrorHandler(){
            public void handleError(Throwable e)
            {
                ServerUtils.log(e);
            }
        };
        
        try
        {
            OdinServer server = new OdinServer();
            server.start(errorListener);
        }
        catch(Exception e)
        {
            ServerUtils.log("Error starting server:");
            ServerUtils.log(e);
            throw e;
        }
    }
    
    protected OdinServerOperations createServerOperations() throws Exception
    {
        return new OdinServerOperations();
    }
}
