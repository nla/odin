package odin.server.host;

import thor.Storable;

/**
* A cached object representing static (for the purposes of this application) information about a host.
* Current sources of this information include: maxmind database for geolocation, and InetAddress.getByName() for the IP address.
*/

public class HostInformation implements Storable
{
    private static final long serialVersionUID = 1L;
    
    private String host;
    private String ipAddress;
    private String state;
    private String city;
    
    /**
    * Creates a new instance with the specified information.
    * 
    * @param host       The full host name to which this data relates.
    * @param ipAddress  The IP address of the host.
    * @param state      The estimated state in which the address is hosted, or Unknown.
    * @param city       The estimated city in which the address is hosted, or Unknown.
    */
    
    public HostInformation(String host, String ipAddress, String state, String city)
    {
        this.host = host;
        this.ipAddress = ipAddress;
        this.state = state;
        this.city = city;
    }
    
    /**
     * @return      the full host name.
     */

    public String getHost()
    {
        return host;
    }
    
    /**
     * @return      the IP address.
     */

    public String getIpAddress()
    {
        return ipAddress;
    }
    
    /**
     * @return      the estimated state in which this IP address is hosted. Could return Unknown.
     */

    public String getState()
    {
        return state;
    }
    
    /**
     * @return      the estimated city in which this IP address is hosted. Could return Unknown.
     */

    public String getCity()
    {
        return city;
    }
}