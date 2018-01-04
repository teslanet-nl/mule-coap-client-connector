package nl.teslanet.mule.connectors.coap.client;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.inject.Inject;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
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
import org.mule.api.annotations.param.Optional;
import org.mule.api.callback.SourceCallback;
import org.mule.api.endpoint.MalformedEndpointException;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.NullPayload;

import nl.teslanet.mule.connectors.coap.client.config.CoAPClientConfig;
import nl.teslanet.mule.connectors.coap.client.error.ErrorHandler;
import nl.teslanet.mule.connectors.coap.exceptions.ResponseTimeoutException;
import nl.teslanet.mule.connectors.coap.options.Options;
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

    @Inject
    private MuleContext muleContext;

    private CoapEndpoint endpoint= null;

    //private Set< WebLink > resources= null;

    private ConcurrentSkipListMap< String, CoapObserveRelation > relations= new ConcurrentSkipListMap< String, CoapObserveRelation >();

    private ConcurrentSkipListMap< String, CoapHandler > handlers= new ConcurrentSkipListMap< String, CoapHandler >();

    // A class with @Connector must contain exactly one method annotated with
    // @Connect
    @TestConnectivity
    public void test() throws ConnectionException, MalformedEndpointException
    {
        CoapClient client= createClient( "/", null );

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
    public void startConnector() throws ConnectionException
    {
        NetworkConfig networkConfig= config.getNetworkConfig();
        //prevent creating property file
        NetworkConfig.setStandard( networkConfig );

        if ( endpoint != null )
        {
            endpoint.destroy();
        }
        endpoint= new CoapEndpoint( config.getLocalAddress(), networkConfig );
        try
        {
            endpoint.start();
        }
        catch ( IOException e )
        {
            throw new ConnectionException( ConnectionExceptionCode.UNKNOWN, "coap endpoint fault", "coap uri endpoint", e );
        }
    }

    // A class with @Connector must contain exactly one method annotated with
    @Stop
    public void stopConnector()
    {
        for ( CoapObserveRelation relation : relations.values() )
            relation.proactiveCancel();
        relations.clear();
        handlers.clear();

        if ( endpoint != null )
        {
            endpoint.destroy();
            endpoint= null;
        }
    }

    /**
     * ping processor, that pings a CoAP resource
     *	
     * @return true if ping was successful, otherwise false
     * @throws Exception
     */
    @Processor
    public Boolean ping( String path ) throws Exception
    {
        CoapClient client= createClient( path, null );

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
    public Set< WebLink > discover( @Optional List< String > queryParameters) throws Exception
    {
        CoapClient client= createClient( "/", null );
        return client.discover( toQueryString( queryParameters ) );
    }

    /**
     * get processor that retrieves a CoAP resource
     *	
     * @return Response of te coap service
     * @throws Exception
     */
    @Processor
    public MuleEvent get( MuleEvent event, String path, @Default(value= "true") Boolean confirmable, @Optional List< String > queryParameters ) throws Exception
    {
        //        int type= MediaTypeRegistry.parse( mediatype );
        //        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );
        return doRequest( event, CoAP.Code.GET, path, confirmable, null, queryParameters );
    }

    /**
     * get-async processor that retrieves a CoAP resource. The response is handled asynchronously by specified handler.
     *  
     * @return Response of te coap service
     * @throws Exception
     */
    @Processor
    public MuleEvent asyncGet( MuleEvent event, String path, @Default(value= "true") Boolean confirmable, String responseHandler, @Optional List< String > queryParameters )
        throws Exception
    {
        //        int type= MediaTypeRegistry.parse( mediatype );
        //        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );
        return doRequest( event, CoAP.Code.GET, path, confirmable, responseHandler, queryParameters );
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
    public MuleEvent put( MuleEvent event, String path, @Default(value= "true") Boolean confirmable ) throws Exception
    {
        //        int type= MediaTypeRegistry.parse( mediatype );
        //        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );

        return doRequest( event, CoAP.Code.PUT, path, confirmable, null, null );
    }

    /**
     * async put processor that delivers a resource the the CoAP-service. The response is handled asynchronously by specified handler.
     *
     * @param payload
     *            Body of the message to be sent.
     * @return response body  of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent asyncPut( MuleEvent event, String path, @Default(value= "true") Boolean confirmable, String responseHandler, @Optional List< String > queryParameters )
        throws Exception
    {
        //        int type= MediaTypeRegistry.parse( mediatype );
        //        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );

        return doRequest( event, CoAP.Code.PUT, path, confirmable, responseHandler, queryParameters );
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
    public MuleEvent post( MuleEvent event, String path, @Default(value= "true") Boolean confirmable ) throws Exception
    {
        //        int type= MediaTypeRegistry.parse( mediatype );
        //        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );

        return doRequest( event, CoAP.Code.POST, path, confirmable, null, null );
    }

    /**
     * async post processor that sends a message to a CoAP-resource. The response is handled asynchronously by specified handler.
     *
     * @param payload
     *            Body of the message to be sent.
     * @return response body  of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent asyncPost( MuleEvent event, String path, @Default(value= "true") Boolean confirmable, String responseHandler, @Optional List< String > queryParameters )
        throws Exception
    {
        //        int type= MediaTypeRegistry.parse( mediatype );
        //        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );

        return doRequest( event, CoAP.Code.POST, path, confirmable, responseHandler, queryParameters );
    }

    /**
     * delete processor that sends a delete request to a CoAP-resource  
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent delete( MuleEvent event, String path, @Default(value= "true") Boolean confirmable ) throws Exception
    {

        return doRequest( event, CoAP.Code.DELETE, path, confirmable, null, null );
    }

    /**
     * async delete processor that sends a delete request to a CoAP-resource. The response is handled asynchronously by specified handler.
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent asyncDelete( MuleEvent event, String path, @Default(value= "true") Boolean confirmable, String responseHandler, @Optional List< String > queryParameters )
        throws Exception
    {

        return doRequest( event, CoAP.Code.DELETE, path, confirmable, responseHandler, queryParameters );
    }

    /**
     * startObserve processor that starts observe of a CoAP-resource  
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public void startObserve( String path, String responseHandler, @Optional List< String > queryParameters  ) throws Exception
    {
        CoapClient client= createClient( path, toQueryString( queryParameters ));

        CoapHandler handler= handlers.get( responseHandler );
        if ( handler != null )
        {
            CoapObserveRelation relation= relations.get( path );
            if ( relation != null )
            {
                //only one observe relation allowed per connector & path 
                relation.proactiveCancel();
                relations.remove( path );
            }
            relation= client.observe( handler );
            relations.put( path, relation );
        }
        else
        {
            throw new Exception( "coap handler not found: " + responseHandler );
        }
    }

    /**
     * stopObserve processor that stops observe of a CoAP-resource  
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public void stopObserve( String path ) throws Exception
    {
        CoapObserveRelation relation= relations.get( path );
        if ( relation != null )
        {
            relation.proactiveCancel();
            relations.remove( path );
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
    public void observe( String path, final SourceCallback callback, @Optional List< String > queryParameters  ) throws Exception
    {
        CoapClient client= createClient( path, toQueryString( queryParameters )  );
        //TODO retry mechanism
        CoapObserveRelation relation= client.observe( new CoapHandler()
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
        //TODO interacts with dynamic created observe mechanism: review
        relations.put( path, relation );

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

    //    public boolean verifyResource() throws URISyntaxException
    //    {
    //        WebLink found= null;
    //
    //        if ( resources != null && !resources.isEmpty() )
    //        {
    //            for ( WebLink link : resources )
    //            {
    //                if ( link.getURI().equals( "/" ) )
    //                {
    //                    found= link;
    //                }
    //            }
    //        } ;
    //        return found != null;
    //
    //    }

    public URI getURI( String path, String query ) throws URISyntaxException
    {
        String scheme= ( config.isSecure() ? CoAP.COAP_SECURE_URI_SCHEME : CoAP.COAP_URI_SCHEME );
        int port;

        if ( config.isSecure() )
        {
            port= ( config.getPort() != null ? config.getPort() : CoAP.DEFAULT_COAP_SECURE_PORT );
        }
        else
        {
            port= ( config.getPort() != null ? config.getPort() : CoAP.DEFAULT_COAP_PORT );
        }
        return new URI( scheme, null, config.getHost(), port, path, query, null );
    }

    public String getUri( String path, String query ) throws URISyntaxException
    {
        return getURI( path, query ).toString();
    }

    private String toQueryString( List< String > queryParameters )
    {
        if ( queryParameters == null ) return null;

        StringBuilder builder= new StringBuilder();
        boolean first;
        Iterator< String > it;
        for ( first= true, it= queryParameters.iterator(); it.hasNext(); first= false )
        {
            if ( !first ) builder.append( "&" );
            builder.append( it.next() );
        }
        return builder.toString();
    }

    private CoapClient createClient( String path, String query ) throws MalformedEndpointException
    {
        CoapClient client;
        try
        {
            client= new CoapClient( getUri( path, query ) );
        }
        catch ( URISyntaxException e )
        {
            throw new MalformedEndpointException( e );
        }
        client.setEndpoint( endpoint );

        return client;
    }

    //TODO add custom timeout
    private MuleEvent doRequest( MuleEvent event, Code requestCode, String path, Boolean confirmable, String handlerName, List< String > queryParameters ) throws Exception
    {
        //when handler needed, verify handler existence 
        CoapHandler handler= null;
        if ( handlerName != null )
        {
            handler= handlers.get( handlerName );
            if ( handler == null ) throw new Exception( "coap unknown handler: " + handlerName );
        }

        CoapClient client= createClient( path, toQueryString( queryParameters ) );

        //build request
        MuleMessage muleMessage= event.getMessage();
        Request request= new Request( requestCode, ( confirmable ? Type.CON : Type.NON ) );

        Object requestPayload= muleMessage.getPayload();

        if ( requestPayload != null && !requestPayload.equals( NullPayload.getInstance() ) )
        {
            if ( byte[].class.isAssignableFrom( requestPayload.getClass() ) )
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
        request.setOptions( new Options( outboundProps ) );
        //is done via Client
//        if ( queryParameters != null )
//        {
//            //add query parameters
//            request.getOptions().getUriQuery().addAll( queryParameters );
//        }
        String mimeType= muleMessage.getDataType().getMimeType();
        //use mimetype when provided but do not overrule outbound props
        if ( mimeType != null && !request.getOptions().hasContentFormat() )
        {
            request.getOptions().setContentFormat( MediaTypeRegistry.parse( mimeType ) );
        }
        if ( handler == null )
        {
            //send out request
            CoapResponse response= client.advanced( request );

            //throw exception when no success
            checkSucces( response, request );

            //return response to mule flow
            return createMuleEvent( response, event );
        }
        else
        {
            //async response
            client.advanced( handler, request );
            //return unchanged message to mule flow
            return( event );
        }

    }

    private void checkSucces( CoapResponse response, Request request ) throws Exception
    {
        if ( response == null )
        {
            throw new ResponseTimeoutException( request.getURI(), request.getType(), request.getCode() );

        }
        else if ( !response.isSuccess() )
        {
            throw new Exception(
                "coap response code ( " + response.getCode().toString() + " ) is mapped to failure." + "\n while doing request: " + request.getType().toString() + "-"
                    + request.getCode().toString() + "\n on uri:  " + request.getURI() );
        } ;
    }

    private MuleMessage createMuleMessage( CoapResponse response )
    {
        DefaultMuleMessage result= null;
        HashMap< String, Object > inboundProps= new HashMap< String, Object >();
        inboundProps.put( PropertyNames.COAP_RESPONSE_CODE, response.getCode().toString() );
        Options.fillProperties( response.getOptions(), inboundProps );

        int contentFormat= response.getOptions().getContentFormat();
        if ( contentFormat == MediaTypeRegistry.UNDEFINED )
        {
            result= new DefaultMuleMessage( response.getPayload(), inboundProps, null, null, muleContext );
        }
        else
        {
            String mediaType= MediaTypeRegistry.toString( response.getOptions().getContentFormat() );
            result= new DefaultMuleMessage( response.getPayload(), inboundProps, null, null, muleContext, DataTypeFactory.create( response.getPayload().getClass(), mediaType ) );
        }
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