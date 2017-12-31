package nl.teslanet.mule.connectors.coap.client;


import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.Source;
import org.mule.api.annotations.TestConnectivity;
import org.mule.api.annotations.lifecycle.OnException;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.lifecycle.Stop;
import org.mule.api.annotations.param.Default;
import org.mule.api.callback.SourceCallback;
import org.mule.api.endpoint.MalformedEndpointException;
import org.mule.module.http.internal.request.ResponseValidatorException;
import org.mule.transport.NullPayload;

import nl.teslanet.mule.connectors.coap.client.config.CoAPClientConfig;
import nl.teslanet.mule.connectors.coap.client.error.ErrorHandler;
import nl.teslanet.mule.connectors.coap.exceptions.ResponseTimeoutException;
import nl.teslanet.mule.connectors.coap.options.OptionSet;
import nl.teslanet.mule.connectors.coap.options.PropertyNames;


@Connector(name= "coap-client", friendlyName= "CoAP Client", schemaVersion= "1.0"
//namespace= "http://www.teslanet.nl/mule/connectors/coap/client",
//schemaLocation= "http://www.teslanet.nl/mule/connectors/coap/client/1.0/mule-coap-client.xsd"
)
@OnException(handler= ErrorHandler.class)
public class CoapClientConnector
{

    @Config
    private CoAPClientConfig config;

    private CoapClient client= null;

    @Inject
    private MuleContext muleContext;

    //TODO must be list
    private CoapObserveRelation relation= null;

    private Set< WebLink > resources= null;

    HashMap< String, CoapHandler > handlers= new HashMap< String, CoapHandler >();

    // A class with @Connector must contain exactly one method annotated with
    // @Connect
    @TestConnectivity
    public void test() throws ConnectionException
    {
        String scheme= ( config.isSecure() ? CoAP.COAP_SECURE_URI_SCHEME : CoAP.COAP_URI_SCHEME );
        client= new CoapClient( scheme, config.getHost(), config.getPort(), config.getPath() );
        if ( client == null || !client.ping() )
        {
            throw new ConnectionException( ConnectionExceptionCode.CANNOT_REACH, "coap  ping failure", null );
        }
        if ( client != null )
        {
            //resources= client.discover();
            client.shutdown();
        }
    }

    @Start
    public void startClient() throws ConnectionException
    {
        //TODO validaton on path (uri)configuration

        String scheme= ( config.isSecure() ? CoAP.COAP_SECURE_URI_SCHEME : CoAP.COAP_URI_SCHEME );
        try
        {
            client= new CoapClient( config.getUri() );
        }
        catch ( URISyntaxException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        client.setEndpoint( new CoapEndpoint( config.getLocalAddress(), config.getNetworkConfig() ) );
        //TODO make checking connection configurable
        //        if ( client == null || !client.ping() )
        //        {
        //            throw new ConnectionException( ConnectionExceptionCode.CANNOT_REACH, "coap:  ping failure", null );
        //        }
        //        try
        //        {
        //            resources= discover( null );
        //        }
        //        catch ( Exception e )
        //        {
        //            new ConnectionException( ConnectionExceptionCode.CANNOT_REACH, "coap: discovery failure", null, e );
        //        }

    }

    // A class with @Connector must contain exactly one method annotated with
    @Stop
    public void stopClient()
    {
        if ( relation != null ) relation.proactiveCancel();

        if ( client != null )
        {
            client.shutdown();
        }
    }

    /**
     * ping processor, that pings a CoAP resource
     *	
     * @return true if ping was successful, otherwise false
     * @throws Exception
     */
    @Processor
    public Boolean ping() throws Exception
    {
        boolean response= client.ping();
        return new Boolean( response );
    }

    /**
     * discover processor, that retrieves information about CoAP resources
     *	
     * @return true if ping was successful, otherwise false
     * @throws Exception
     */
    @Processor
    public Set< WebLink > discover( String query ) throws Exception
    {
        return client.discover( query );
    }

    /**
     * get processor that retrieves a CoAP resource
     *	
     * @return Response of te coap service
     * @throws Exception
     */
    @Processor
    public MuleEvent get( MuleEvent event, @Default(value= "true") Boolean confirmable ) throws Exception
    {
        //        int type= MediaTypeRegistry.parse( mediatype );
        //        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );

        return doRequest( event, CoAP.Code.GET, confirmable );
    }

    /**
     * put processor that delivers a resource the the CoAP-service
     *
     * @param payload
     *            Body of the message to be sent.
     * @return response body  of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent put( MuleEvent event, @Default(value= "true") Boolean confirmable ) throws Exception
    {
        //        int type= MediaTypeRegistry.parse( mediatype );
        //        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );

        return doRequest( event, CoAP.Code.PUT, confirmable );
    }

    /**
     * post processor that sends a message to a CoAP-resource  
     *
     * @param payload
     *            Body of the message to be sent.
     * @return response body  of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent post( MuleEvent event, @Default(value= "true") Boolean confirmable ) throws Exception
    {
        //        int type= MediaTypeRegistry.parse( mediatype );
        //        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );

        return doRequest( event, CoAP.Code.POST, confirmable );
    }

    /**
     * delete processor that sends a delete request to a CoAP-resource  
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent delete( MuleEvent event, @Default(value= "true") Boolean confirmable ) throws Exception
    {

        return doRequest( event, CoAP.Code.DELETE, confirmable );
    }

    /**
     * startObserve processor that starts observe of a CoAP-resource  
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public void startObserve( String handlerName ) throws Exception
    {
        CoapHandler handler= handlers.get( handlerName );
        //TODO add relation to list under name to identify it for cancel operation
        if ( handler != null )
        {
            relation= client.observe( handler );
        }
        else{
            throw new Exception( "coap handler not found: " + handler );
        }
    }
    
    /**
     * stopObserve processor that stops observe of a CoAP-resource  
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public void stopObserve( String handlerName ) throws Exception
    {
        //TODO add relation to list under name to identify it for cancel operation
        if ( relation != null )
        {
            //TODO cancel option proactive / reactive
            relation.proactiveCancel();
        }
    }
    
    
    /**
     * Custom Message Source
     *
     * @param callback
     *            The sourcecallback used to dispatch message to the flow
     * @throws Exception
     *             error produced while processing the payload
     */
    @Source
    public void observe( final SourceCallback callback ) throws Exception
    {
        //TODO add relation to list under name to identify it for cancel operation
        client.observe( new CoapHandler()
            {
                @Override
                public void onError()
                {
                    try
                    {
                        //TODO preliminary
                        callback.process( new String( "coap: ERROR!" ) );
                    }
                    catch ( Exception e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLoad( CoapResponse response )
                {
                    try
                    {
                        callback.process( createMuleMessage( response ) );
                    }
                    catch ( Exception e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } );

    }

    /**
     * Custom Message Source
     *
     * @param callback
     *            The sourcecallback used to dispatch message to the flow
     * @param name
     *            The sourcecallback used to dispatch message to the flow
     * @throws Exception
     *             error produced while processing the payload
     */
    @Source
    public void handleResponse( final SourceCallback callback, String handlerName ) throws Exception
    {
        if ( handlerName == null || handlerName.isEmpty() ) throw new Exception( "coap Invalid ResponseHandler name" );

        CoapHandler handler= new CoapHandler()
            {

                @Override
                public void onError()
                {
                    try
                    {
                        callback.process( new String( "coap: ERROR!" ) );
                    }
                    catch ( Exception e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

                @Override
                public void onLoad( CoapResponse response )
                {
                    try
                    {
                        callback.process( createMuleMessage( response ) );
                    }
                    catch ( Exception e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            };

        handlers.put( handlerName, handler );
    }

    public CoAPClientConfig getConfig()
    {
        return config;
    }

    public void setConfig( CoAPClientConfig config )
    {
        this.config= config;
    }

    /*
    private void throwWhenInvalidResource( String resource ) throws Exception
    {
        if ( !verifyResource( resource ) )
        {
            throw new Exception("coap: '" + resource + ", is not a valid resource. Available resources: \n[" + resources.toString() + " ]" );
        };
    }
    */

    public boolean verifyResource()
    {
        WebLink found= null;

        if ( resources != null && !resources.isEmpty() )
        {
            for ( WebLink link : resources )
            {
                try
                {
                    if ( link.getURI().equals( config.getUri() ) )
                    {
                        found= link;
                    }
                }
                catch ( URISyntaxException e )
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } ;
        return found != null;

    }

    private MuleEvent doRequest( MuleEvent event, Code requestCode, Boolean confirmable ) throws MuleException, ResponseTimeoutException
    {
        MuleMessage muleMessage= event.getMessage();
        Type type= ( confirmable ? Type.CON : Type.NON );
        Request request= new Request( requestCode, type );
        try
        {
            request.setURI( config.getURI() );
        }
        catch ( URISyntaxException e1 )
        {
            throw new MalformedEndpointException( e1 );
        }
        Object requestPayload= muleMessage.getPayload();
        //TODO reconsider payload type
        if ( requestPayload != null && !requestPayload.equals( NullPayload.getInstance() ) )
        {
            if ( byte[].class.isInstance( requestPayload ) )
            {
                request.setPayload( (byte[]) requestPayload );
            }
            else
            {
                request.setPayload( requestPayload.toString() );
            }
        }
        //TODO improve efficiency
        HashMap< String, Object > outboundProps= new HashMap< String, Object >();
        for ( String propName : event.getMessage().getOutboundPropertyNames() )
        {
            outboundProps.put( propName, muleMessage.getOutboundProperty( propName ) );
        }
        request.setOptions( new OptionSet( outboundProps ) );
        //Map< String, Object > inboundProperties= createInboundProperties( exchange );
        //Map< String, Object > outboundProperties= new HashMap< String, Object >();
        CoapResponse response= client.advanced( request );
        if ( response == null )
        {
            String uri;
            try
            {
                uri= this.config.getUri();
            }
            catch ( URISyntaxException e )
            {
                uri= "?";
            }
            throw new ResponseTimeoutException( uri, type, requestCode );

        }
        else if ( !response.isSuccess() )
        {
            String uri;
            try
            {
                uri= this.config.getUri();
            }
            catch ( URISyntaxException e )
            {
                uri= "?";
            }
            throw new ResponseValidatorException(
                "coap response code ( " + response.getCode().toString() + " ) is mapped to failure." + "\n while doing request: " + type.toString() + "-" + requestCode.toString()
                    + "\n on uri:  " + uri,
                event );
        } ;
        return createMuleEvent( response, event );
    }

    private MuleMessage createMuleMessage( CoapResponse response )
    {
        HashMap< String, Object > inboundProps= new HashMap< String, Object >();
        inboundProps.put( PropertyNames.COAP_RESPONSE_CODE, response.getCode().toString() );
        OptionSet.fillProperties( response.getOptions(), inboundProps );
        //TODO review payloadtype
        DefaultMuleMessage result= new DefaultMuleMessage( response.getResponseText(), inboundProps, null, null, muleContext );

        return result;
    }

    private MuleEvent createMuleEvent( CoapResponse response, MuleEvent rewriteEvent )
    {
        MuleMessage responseMuleMessage= createMuleMessage( response );
        DefaultMuleEvent result= new DefaultMuleEvent( responseMuleMessage, rewriteEvent );

        return result;
    }

    public MuleContext getMuleContext()
    {
        return muleContext;
    }

    public void setMuleContext( MuleContext context )
    {
        this.muleContext= context;
    }

}