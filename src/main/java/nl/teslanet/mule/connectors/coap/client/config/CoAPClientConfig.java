package nl.teslanet.mule.connectors.coap.client.config;

import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.param.Default;

@Configuration(friendlyName = "CoAP Client Configuration")
public class CoAPClientConfig
{

    /**
     * Reply message
     */
    @Configurable
	public String uri;

 
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}