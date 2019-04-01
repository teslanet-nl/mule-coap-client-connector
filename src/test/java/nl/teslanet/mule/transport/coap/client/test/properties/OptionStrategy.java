package nl.teslanet.mule.transport.coap.client.test.properties;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

/**
 * Interface of strategies to set and validate options
 *
 */
public interface OptionStrategy
{
    /**
     * Operation to set option on response message 
     * @param response to set option on
     */
    public void setOption( Response response );
    /**
     * Operation to validate option on request
     * @param request containing the option
     * @return true when option value is as expected, false otherwise
     */
    public boolean validateOption( Request request );
}
