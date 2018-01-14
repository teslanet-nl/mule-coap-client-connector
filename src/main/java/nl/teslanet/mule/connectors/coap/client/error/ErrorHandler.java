package nl.teslanet.mule.connectors.coap.client.error;

import org.mule.api.annotations.Handle;
import org.mule.api.annotations.components.Handler;

@Handler
public class ErrorHandler 
{

    @Handle
    public void handle(Exception ex) throws Exception {
    	//TODO Process the exception
        System.err.print( "COAP CONNECTOR ERROR HANDLER");
    }

}