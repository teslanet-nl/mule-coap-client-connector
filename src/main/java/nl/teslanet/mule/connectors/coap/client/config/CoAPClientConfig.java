package nl.teslanet.mule.connectors.coap.client.config;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.coap.CoAP;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;

import nl.teslanet.mule.connectors.coap.client.config.EndpointConfig;

@Configuration(friendlyName = "Configuration")
public class CoAPClientConfig extends EndpointConfig
{
    @Configurable
    @Placement(tab= "General")
    private String host= null;

    @Configurable
    @Optional
    @Placement(tab= "General")
    private Integer port= null;

    @Configurable
    @Default( value= "false")
    @Placement(tab= "General")
    //@FriendlyName(value = false)
    private boolean secure= false;

    public InetSocketAddress getLocalAddress()
    {
        int port = 0;

        if ( secure )
        {
            if ( getBindToPort() != null ) port=  Integer.parseInt( getBindToPort());
        }
        else if ( getBindToSecurePort() != null )
        {
            port= Integer.parseInt( getBindToSecurePort());
        };
        if ( getBindToHost() != null )
        {
            return new InetSocketAddress( getBindToHost(), port );            
        }
        else
        {
            return new InetSocketAddress( port );            
        }
	}


/**
     * @return the host
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost( String host )
    {
        this.host= host;
    }

    /**
     * @return the port
     */
    public Integer getPort()
    {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort( Integer port )
    {
        this.port= port;
    }


    /**
     * @return the secure
     */
    public boolean isSecure()
    {
        return secure;
	}

    /**
     * @param secure the secure to set
     */
    public void setSecure( boolean secure )
    {
        this.secure= secure;
    }
}