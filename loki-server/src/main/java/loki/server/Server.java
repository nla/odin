package loki.server;

import java.util.ArrayList;
import java.util.List;
import loki.Attributes;
import loki.ErrorHandler;
import loki.server.adapter.Adapter;
import loki.server.util.ServerUtils;

public abstract class Server<Ops extends ServerOperations>
{
    private Ops ops;
    private List<Adapter<Ops>> adapters;
    private Thread mainThread;
    
	public Server()
	{
		this.adapters = new ArrayList<Adapter<Ops>>();
	}
	
	protected abstract Ops createServerOperations() throws Exception;
	
	protected void start(ErrorHandler errorListener) throws Exception
	{
	    mainThread = Thread.currentThread();
	    
	    ops = createServerOperations();
		Attributes<String> adapterProperties = ServerUtils.loadFromFile("config/adapters.properties");
	
		for(String key: adapterProperties.keys())
		{
		    @SuppressWarnings("unchecked")
            Adapter<Ops> adapter = (Adapter<Ops>)Class.forName(key).getDeclaredConstructor().newInstance();
		    adapter.initialise(ServerUtils.loadFromFile(adapterProperties.get(key)), errorListener, ops);
		    adapters.add(adapter);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
		    public void run()
		    {
		        stop();
		    }
		}));
		
		try
		{
		    ServerUtils.log("Server initialised.");
		    Thread.sleep(Long.MAX_VALUE);
		}
		catch(InterruptedException e){}
	}
	
	private void stop()
	{
	    ServerUtils.log("Shutdown requested.");
	    ServerUtils.log("Terminating adapters...");
	    
	    for(Adapter<Ops> adapter: adapters)
	    {
	        adapter.terminate();
	    }
	    
	    mainThread.interrupt();
	    ServerUtils.log("Shutdown complete.");
	}
}