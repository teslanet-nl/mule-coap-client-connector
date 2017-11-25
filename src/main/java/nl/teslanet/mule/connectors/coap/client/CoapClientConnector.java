package nl.teslanet.mule.connectors.coap.client;


import java.util.Set;

import javax.inject.Inject;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.MuleContext;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.Source;
import org.mule.api.annotations.SourceStrategy;
import org.mule.api.annotations.TestConnectivity;
import org.mule.api.annotations.lifecycle.OnException;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.lifecycle.Stop;
import org.mule.api.callback.SourceCallback;
import org.mule.api.context.MuleContextAware;

import nl.teslanet.mule.connectors.coap.client.config.CoAPClientConfig;
import nl.teslanet.mule.connectors.coap.client.error.ErrorHandler;


@Connector
(   
    name= "coap-client",
    friendlyName= "CoAP Client Connector",
    schemaVersion= "1.0"
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
    private MuleContext context;

    private CoapObserveRelation relation= null;

    private Set< WebLink > resources= null;
    

    // A class with @Connector must contain exactly one method annotated with
    // @Connect
    @TestConnectivity
    public void test() throws ConnectionException
    {
        client= new CoapClient( config.uri  );
        if ( client == null || !client.ping() )
        {
            throw new ConnectionException( ConnectionExceptionCode.CANNOT_REACH, "coap  ping failure", null );
        }
        if ( client != null )
        {
            client.shutdown();
        }
    }

    @Start
    public void startClient() throws ConnectionException
    {
        client= new CoapClient( config.uri );
        if ( client == null || !client.ping() )
        {
            throw new ConnectionException( ConnectionExceptionCode.CANNOT_REACH, "coap:  ping failure", null );
        }
        try
        {
            resources= discover( null );
        }
        catch ( Exception e )
        {
            new ConnectionException( ConnectionExceptionCode.CANNOT_REACH, "coap: discovery failure", null, e );
        }
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
        Set< WebLink > response= null;
        if ( query == null || query.length() <= 0 )
        {
            response= client.discover();
        }
        else
        {
            response= client.discover( query );
        }
        return response;

    }

    /**
     * get processor that retrieves a CoAP resource
     *	
     * @return Response of te coap service
     * @throws Exception
     */
    @Processor
    public String get( ) throws Exception
    {
        
        CoapResponse response= client.get();
        if ( response != null )
        {

            return response.getResponseText();
            // DefaultMuleMessage msg= DefaultMuleMessage(Object message,
            // Map<String, Object> inboundProperties,
            // Map<String, Object> outboundProperties, Map<String, DataHandler>
            // attachments,
            // MuleContext muleContext)

        }
        else
        {

            throw new Exception( "coap  leeg response" );

        }
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
    public String put( String payload, String mediatype ) throws Exception
    {
        int type= MediaTypeRegistry.parse( mediatype );
        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );

        CoapResponse response= client.put( payload.getBytes(), type );
        if ( response != null )
        {

            return response.getResponseText();
            // DefaultMuleMessage msg= DefaultMuleMessage(Object message,
            // Map<String, Object> inboundProperties,
            // Map<String, Object> outboundProperties, Map<String, DataHandler>
            // attachments,
            // MuleContext muleContext)

        }
        else
        {

            throw new Exception( "coap:  empty response" );

        }
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
    public String post( String payload, String mediatype ) throws Exception
    {
        int type= MediaTypeRegistry.parse( mediatype );
        if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception( "coap: unsupported mediatype" );

        CoapResponse response= client.put( payload.getBytes(), type );
        if ( response != null )
        {

            return response.getResponseText();
            // DefaultMuleMessage msg= DefaultMuleMessage(Object message,
            // Map<String, Object> inboundProperties,
            // Map<String, Object> outboundProperties, Map<String, DataHandler>
            // attachments,
            // MuleContext muleContext)

        }
        else
        {

            throw new Exception( "coap:  empty response" );

        }
    }

    /**
     * delete processor that sends a delete request to a CoAP-resource  
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public String delete() throws Exception
    {

        CoapResponse response= client.delete();
        if ( response != null )
        {

            return response.getResponseText();
            // DefaultMuleMessage msg= DefaultMuleMessage(Object message,
            // Map<String, Object> inboundProperties,
            // Map<String, Object> outboundProperties, Map<String, DataHandler>
            // attachments,
            // MuleContext muleContext)

        }
        else
        {

            throw new Exception( "coap:  no response" );

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
    @Source(sourceStrategy= SourceStrategy.POLLING, pollingPeriod= 5000)
    public void getNewMessages( final SourceCallback callback ) throws Exception
    {
        /*
         * Every 5 the flow using this processor will be called and the payload
         * will be the one defined here.
         * 
         * The PAYLOAD can be anything. In this example a String is used.
         */
        callback.process( "Start working" );
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
        relation= client.observe( new CoapHandler()
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
                public void onLoad( CoapResponse arg0 )
                {
                    try
                    {
                        callback.process( arg0.getResponseText() );
                    }
                    catch ( Exception e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            } );

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
    
    public boolean verifyResource( )
    {
        WebLink found= null;

        if ( resources != null && !resources.isEmpty() )
        {
            for ( WebLink link : resources )
            {
                if ( link.getURI().equals( config.getUri()) )
                {
                    found= link;
                }
            }
        };
        return found != null;

    }

    public MuleContext getContext()
    {
        return context;
    }

    public void setContext( MuleContext context )
    {
        this.context = context;
    }

}