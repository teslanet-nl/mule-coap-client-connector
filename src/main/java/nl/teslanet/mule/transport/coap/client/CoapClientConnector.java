/*******************************************************************************
 * Copyright (c) 2017, 2018 (teslanet.nl) Rogier Cobben.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Public License - v 2.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *    (teslanet.nl) Rogier Cobben - initial creation
 ******************************************************************************/

package nl.teslanet.mule.transport.coap.client;


import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;
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
import org.mule.util.IOUtils;

import nl.teslanet.mule.transport.coap.client.config.CoAPClientConfig;
import nl.teslanet.mule.transport.coap.client.error.ErrorHandler;
import nl.teslanet.mule.transport.coap.commons.options.Options;
import nl.teslanet.mule.transport.coap.commons.options.PropertyNames;


@Connector
(
    name= "coap-client", 
    friendlyName= "CoAP Client", 
    schemaVersion= "1.0",
    // namespace= "http://www.mulesoft.org/schema/mule/coap-client",
    schemaLocation= "http://www.teslanet.nl/schema/mule/coap-server/1.0/mule-coap-client.xsd"
)

@OnException(handler= ErrorHandler.class)
public class CoapClientConnector
{
    //TODO make host port optional on connector config and exception handling on missing on connector and on operation

    @Config
    private CoAPClientConfig config;

    @Inject
    private MuleContext muleContext;

    private CoapEndpoint endpoint= null;

    // private Set< WebLink > resources= null;

    private ConcurrentSkipListMap< String, CoapObserveRelation > staticRelations= new ConcurrentSkipListMap< String, CoapObserveRelation >();
    private ConcurrentSkipListMap< String, CoapObserveRelation > dynamicRelations= new ConcurrentSkipListMap< String, CoapObserveRelation >();

    private ConcurrentSkipListMap< String, SourceCallback > handlers= new ConcurrentSkipListMap< String, SourceCallback >();

    // A class with @Connector must contain exactly one method annotated with
    // @Connect
    @TestConnectivity
    public void test() throws ConnectionException, MalformedEndpointException
    {
        CoapClient client= createClient( null, null, "/", null );

        if ( client == null || !client.ping() )
        {
            throw new ConnectionException( ConnectionExceptionCode.CANNOT_REACH, "coap  ping failure", null );
        }
        if ( client != null )
        {
            // resources= client.discover();
            client.shutdown();
        }
    }

    @Start
    public void startConnector() throws ConnectionException
    {
        if ( endpoint != null )
        {
            endpoint.destroy();
        }
        try
        {
            endpoint= createEndpoint( this.config );
            endpoint.start();
        }
        catch ( Exception e )
        {
            throw new ConnectionException( ConnectionExceptionCode.UNKNOWN, "coap endpoint fault", "coap uri endpoint", e );
        }
    }

    // A class with @Connector must contain exactly one method annotated with
    @Stop
    public void stopConnector()
    {
        for ( CoapObserveRelation relation : staticRelations.values() )
        {
            relation.proactiveCancel();
        }
        staticRelations.clear();
        for ( CoapObserveRelation relation : dynamicRelations.values() )
        {
            relation.proactiveCancel();
        }
        dynamicRelations.clear();
        handlers.clear();

        if ( endpoint != null )
        {
            endpoint.destroy();
            endpoint= null;
        }
    }

    private CoapEndpoint createEndpoint( CoAPClientConfig config ) throws Exception
    {
        CoapEndpoint endpoint= null;

        if ( !config.isSecure() )
        {
            endpoint= new CoapEndpoint( config.getLocalAddress(), config.getNetworkConfig() );
        }
        else
        {
            // Pre-shared secrets
            // TODO improve security (-> not in memory )
            InMemoryPskStore pskStore= new InMemoryPskStore();
            // pskStore.setKey("password", "sesame".getBytes()); // from ETSI
            // Plugtest test spec

            // load the key store
            KeyStore keyStore= KeyStore.getInstance( "JKS" );
            InputStream in= IOUtils.getResourceAsStream( config.getKeyStoreLocation(), this.getClass(), true, true );
            keyStore.load( in, config.getKeyStorePassword().toCharArray() );

            // load the trust store
            KeyStore trustStore= KeyStore.getInstance( "JKS" );
            InputStream inTrust= IOUtils.getResourceAsStream( config.getTrustStoreLocation(), this.getClass(), true, true );
            trustStore.load( inTrust, config.getTrustStorePassword().toCharArray() );

            // You can load multiple certificates if needed
            DtlsConnectorConfig.Builder configBuider= new DtlsConnectorConfig.Builder( config.getLocalAddress() );
            configBuider.setPskStore( pskStore );
            try
            {
                configBuider.setTrustStore( trustStore.getCertificateChain( config.getTrustedRootCertificateAlias() ) );
            }
            catch ( Exception e )
            {
                throw new Exception( "coap: certificate chain with alias not found in truststore" );
            }
            try
            {

                configBuider.setIdentity(
                    (PrivateKey) keyStore.getKey( config.getPrivateKeyAlias(), config.getKeyStorePassword().toCharArray() ),
                    keyStore.getCertificateChain( config.getPrivateKeyAlias() ),
                    true );
            }
            catch ( Exception e )
            {
                throw new Exception( "coap: private key with alias not found in keystore" );
            }
            DTLSConnector connector= new DTLSConnector( configBuider.build() );
            // DTLSConnector connector = new DTLSConnector(new
            // InetSocketAddress(DTLS_PORT), trustedCertificates);
            // connector.getConfig().setPrivateKey((PrivateKey)keyStore.getKey("server",
            // KEY_STORE_PASSWORD.toCharArray()),
            // keyStore.getCertificateChain("server"), true);

            endpoint= new CoapEndpoint( connector, config.getNetworkConfig() );
        }
        return endpoint;
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
        CoapClient client= createClient( null, null, path, null );

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
    public Set< WebLink > discover( @Optional List< String > queryParameters ) throws Exception
    {
        CoapClient client= createClient( null, null, "/", null );
        return client.discover( toQueryString( queryParameters ) );
    }

    /**
     * get processor that retrieves a CoAP resource
     * 
     * @return Response of the coap service
     * @throws Exception
     */
    @Processor
    public MuleEvent get(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters ) throws Exception
    {
        return doRequest( event, CoAP.Code.GET, confirmable, host, port, path, queryParameters, null );
    }

    /**
     * get-async processor that retrieves a CoAP resource. The response is
     * handled asynchronously by specified handler.
     * 
     * @return Response of the coap service
     * @throws Exception
     */
    @Processor
    public MuleEvent asyncGet(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters,
        String responseHandler ) throws Exception
    {
        return doRequest( event, CoAP.Code.GET, confirmable, host, port, path, queryParameters, responseHandler );
    }

    /**
     * put processor that delivers a resource the the CoAP-service
     *
     * @param payload
     *            Body of the message to be sent.
     * @return response body of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent put(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters ) throws Exception
    {
        return doRequest( event, CoAP.Code.PUT, confirmable, host, port, path, queryParameters, null );
    }

    /**
     * async put processor that delivers a resource the the CoAP-service. The
     * response is handled asynchronously by specified handler.
     *
     * @param payload
     *            Body of the message to be sent.
     * @return response body of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent asyncPut(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters,
        String responseHandler ) throws Exception
    {
        return doRequest( event, CoAP.Code.PUT, confirmable, host, port, path, queryParameters, responseHandler );
    }

    /**
     * post processor that sends a message to a CoAP-resource
     *
     * @param payload
     *            Body of the message to be sent.
     * @return response body of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent post(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters ) throws Exception
    {
        return doRequest( event, CoAP.Code.POST, confirmable, host, port, path, queryParameters, null );
    }

    /**
     * async post processor that sends a message to a CoAP-resource. The
     * response is handled asynchronously by specified handler.
     *
     * @param payload
     *            Body of the message to be sent.
     * @return response body of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent asyncPost(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters,
        String responseHandler ) throws Exception
    {
        // int type= MediaTypeRegistry.parse( mediatype );
        // if ( type == MediaTypeRegistry.UNDEFINED ) throw new Exception(
        // "coap: unsupported mediatype" );

        return doRequest( event, CoAP.Code.POST, confirmable, host, port, path, queryParameters, responseHandler );
    }

    /**
     * delete processor that sends a delete request to a CoAP-resource
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent delete(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters ) throws Exception
    {

        return doRequest( event, CoAP.Code.DELETE, confirmable, host, port, path, queryParameters, null );
    }

    /**
     * async delete processor that sends a delete request to a CoAP-resource.
     * The response is handled asynchronously by specified handler.
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public MuleEvent asyncDelete(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters,
        String responseHandler ) throws Exception
    {

        return doRequest( event, CoAP.Code.DELETE, confirmable, host, port, path, queryParameters, responseHandler );
    }

    /**
     * startObserve processor that starts observe of a CoAP-resource
     *
     * @return response of the CoAP-service
     * @throws Exception
     */
    @Processor
    public void startObserve( @Optional String host, @Optional Integer port, String path, @Optional List< String > queryParameters, String responseHandler ) throws Exception
    {
        final CoapClient client= createClient( host, port, path, toQueryString( queryParameters ) );

        final SourceCallback callback= handlers.get( responseHandler );
        if ( callback == null ) throw new Exception( "coap unknown handler: " + responseHandler );

        CoapHandler handler= new CoapHandler()
            {
                @Override
                public void onError()
                {
                    String uri= client.getURI();
                    CoapObserveRelation relation= dynamicRelations.get( uri );
                    if ( relation != null )
                    {
                        if ( relation.isCanceled() )
                        {
                            dynamicRelations.remove( uri );
                            relation= client.observe( this );
                            dynamicRelations.put( uri, relation );
                        }
                        else
                        {
                            relation.reregister();
                        } ;
                    }
                    try
                    {
                        callback.process( createMuleMessage( null, client, Code.GET ) );
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
                        callback.process( createMuleMessage( response, client, Code.GET ) );
                    }
                    catch ( Exception e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            };

        CoapObserveRelation relation= dynamicRelations.get( client.getURI() );
        if ( relation != null )
        {
            // only one observe relation allowed per connector & path
            // TODO maybe configurable whether proactive ot not
            relation.proactiveCancel();
            dynamicRelations.remove( client.getURI() );
        }
        relation= client.observe( handler );
        dynamicRelations.put( client.getURI(), relation );

    }

    /**
     * stopObserve processor that stops observe of a CoAP-resource
     *
     * @throws Exception
     */
    @Processor
    public void stopObserve( @Optional String host, @Optional Integer port, String path, @Optional List< String > queryParameters ) throws Exception
    {
        String uri= getUri( host, port, path, toQueryString( queryParameters ) );
        CoapObserveRelation relation= dynamicRelations.get( uri );
        if ( relation != null )
        {
            relation.proactiveCancel();
            dynamicRelations.remove( uri );
        }
        //TODO warn when no relation found
    }

    /**
     * listObservations
     * @return list of active dynamic observations
     */
    @Processor
    public List< String > listObservations()
    {
        CopyOnWriteArrayList< String > list= new CopyOnWriteArrayList< String >();
        list.addAll( dynamicRelations.keySet() );
        return list;
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
    public void observe( final SourceCallback callback, @Optional String host, @Optional Integer port, String path, @Optional List< String > queryParameters ) throws Exception
    {
        final CoapClient client= createClient( host, port, path, toQueryString( queryParameters ) );

        CoapObserveRelation relation= client.observe( new CoapHandler()
            {
                @Override
                public void onError()
                {
                    String uri= client.getURI();
                    CoapObserveRelation relation= staticRelations.get( uri );
                    if ( relation != null )
                    {
                        if ( relation.isCanceled() )
                        {
                            staticRelations.remove( uri );
                            relation= client.observe( this );
                            staticRelations.put( uri, relation );
                        }
                        else
                        {
                            relation.reregister();
                        } ;
                    }
                    try
                    {
                        callback.process( createMuleMessage( null, client, Code.GET ) );
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
                        callback.process( createMuleMessage( response, client, Code.GET ) );
                    }
                    catch ( Exception e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            } );
        staticRelations.put( client.getURI(), relation );
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
        if ( handlers.get( handlerName ) != null ) throw new Exception( "coap ResponseHandler name not unique" );
        handlers.put( handlerName, callback );
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
     * private void throwWhenInvalidResource( String resource ) throws Exception
     * { if ( !verifyResource( resource ) ) { throw new Exception("coap: '" +
     * resource + ", is not a valid resource. Available resources: \n[" +
     * resources.toString() + " ]" ); }; }
     */

    // public boolean verifyResource() throws URISyntaxException
    // {
    // WebLink found= null;
    //
    // if ( resources != null && !resources.isEmpty() )
    // {
    // for ( WebLink link : resources )
    // {
    // if ( link.getURI().equals( "/" ) )
    // {
    // found= link;
    // }
    // }
    // } ;
    // return found != null;
    //
    // }

    public URI getURI( String host, Integer port, String path, String query ) throws URISyntaxException
    {
        String scheme= ( config.isSecure() ? CoAP.COAP_SECURE_URI_SCHEME : CoAP.COAP_URI_SCHEME );
        String uriHost= ( host == null ? config.getHost() : host );
        int uriPort;
        if ( port == null )
        {
            if ( config.isSecure() )
            {
                uriPort= ( config.getPort() != null ? config.getPort() : CoAP.DEFAULT_COAP_SECURE_PORT );
            }
            else
            {
                uriPort= ( config.getPort() != null ? config.getPort() : CoAP.DEFAULT_COAP_PORT );
            }
        }
        else
        {
            uriPort= port;
        }
        return new URI( scheme, null, uriHost, uriPort, path, query, null );
    }

    public String getUri( String host, Integer port, String path, String query ) throws URISyntaxException
    {
        return getURI( host, port, path, query ).toString();
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

    private CoapClient createClient( String host, Integer port, String path, String query ) throws MalformedEndpointException
    {
        CoapClient client;
        try
        {
            client= new CoapClient( getUri( host, port, path, query ) );
        }
        catch ( URISyntaxException e )
        {
            throw new MalformedEndpointException( e );
        }
        client.setEndpoint( endpoint );

        return client;
    }

    // TODO add custom timeout, endpoint, networkconfig?
    private MuleEvent doRequest(
        MuleEvent event,
        final Code requestCode,
        Boolean confirmable,
        String host,
        Integer port,
        String path,
        List< String > queryParameters,
        String handlerName ) throws Exception
    {
        CoapHandler handler= null;

        final CoapClient client= createClient( host, port, path, toQueryString( queryParameters ) );

        // build request
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
        // TODO improve efficiency
        HashMap< String, Object > outboundProps= new HashMap< String, Object >();
        for ( String propName : event.getMessage().getOutboundPropertyNames() )
        {
            outboundProps.put( propName, muleMessage.getOutboundProperty( propName ) );
        }
        request.setOptions( new Options( outboundProps ) );
        // is done via Client
        // if ( queryParameters != null )
        // {
        // //add query parameters
        // request.getOptions().getUriQuery().addAll( queryParameters );
        // }
        String mimeType= muleMessage.getDataType().getMimeType();
        // use mimetype when provided but do not overrule outbound props
        if ( mimeType != null && !request.getOptions().hasContentFormat() )
        {
            request.getOptions().setContentFormat( MediaTypeRegistry.parse( mimeType ) );
        }
        if ( handlerName != null )
        {
            final SourceCallback callback= handlers.get( handlerName );
            // verify handler existence
            if ( callback == null ) throw new Exception( "coap unknown handler: " + handlerName );
            handler= new CoapHandler()
                {
                    @Override
                    public void onError()
                    {
                        try
                        {
                            callback.process( createMuleMessage( null, client, requestCode ) );
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
                            callback.process( createMuleMessage( response, client, requestCode ) );
                        }
                        catch ( Exception e )
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                };
        }
        if ( handler == null )
        {
            // send out request
            CoapResponse response= client.advanced( request );

            // return response to Mule flow
            return createMuleEvent( response, client, requestCode, event );
        }
        else
        {
            // async response
            client.advanced( handler, request );
            // return unchanged event to Mule flow
            // TODO maybe VoidMuleEvent
            return( event );
        }

    }


    private MuleMessage createMuleMessage( CoapResponse response, CoapClient client, Code requestCode )
    {
        DefaultMuleMessage result= null;
        HashMap< String, Object > inboundProps= new HashMap< String, Object >();
        if ( response == null )
        {
            // timeout or request rejected by server

            inboundProps.put( PropertyNames.COAP_REQUEST_CODE, requestCode );
            inboundProps.put( PropertyNames.COAP_REQUEST_URI, client.getURI() );
            inboundProps.put( PropertyNames.COAP_RESPONSE_SUCCESS, new Boolean( false ) );
            result= new DefaultMuleMessage( null, inboundProps, null, null, muleContext );
        }
        else
        {
            inboundProps.put( PropertyNames.COAP_REQUEST_CODE, requestCode );
            inboundProps.put( PropertyNames.COAP_REQUEST_URI, client.getURI() );
            inboundProps.put( PropertyNames.COAP_RESPONSE_SUCCESS, new Boolean( response.isSuccess() ) );
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
                result= new DefaultMuleMessage(
                    response.getPayload(),
                    inboundProps,
                    null,
                    null,
                    muleContext,
                    DataTypeFactory.create( response.getPayload().getClass(), mediaType ) );
            }
        }
        return result;
    }

    private MuleEvent createMuleEvent( CoapResponse response, CoapClient client, Code requestCode, MuleEvent rewriteEvent )
    {
        MuleMessage responseMuleMessage= createMuleMessage( response, client, requestCode );
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