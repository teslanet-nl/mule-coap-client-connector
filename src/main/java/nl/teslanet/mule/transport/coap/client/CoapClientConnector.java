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


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
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
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;
import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.MessageExchangePattern;
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
import org.mule.api.annotations.param.Optional;
import org.mule.api.callback.SourceCallback;
import org.mule.api.endpoint.MalformedEndpointException;
import org.mule.security.oauth.processor.AbstractListeningMessageProcessor;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.NullPayload;
import org.mule.util.IOUtils;

import nl.teslanet.mule.transport.coap.client.config.CoAPClientConfig;
import nl.teslanet.mule.transport.coap.client.error.EndpointConstructionException;
import nl.teslanet.mule.transport.coap.client.error.ErrorHandler;
import nl.teslanet.mule.transport.coap.client.error.HandlerException;
import nl.teslanet.mule.transport.coap.client.error.MalformedUriException;
import nl.teslanet.mule.transport.coap.commons.options.Options;
import nl.teslanet.mule.transport.coap.commons.options.PropertyNames;
/**
 * Mule CoAP connector - CoapClient. 
 * The CoapClient Connector can be used in Mule applications to implement CoAP clients as defined in {@see http://tools.ietf.org/html/rfc7252}.
 * A CoAP client issues requests on resources that reside on a CoAP server. 
 * On these resources GET, POST, PUT, DELETE and Observer requests can be issued from Mule 
 * applications using this Connector.
 * The client CoAP endpoint has a number of configuration parameters that can be used to tune behavior of the connector. 
 * Apart from host and port, these parameters have sensible defaults and need only to be set for specific needs.     
 */
@Connector
(
    name= "coap-client", 
    friendlyName= "CoAP Client", 
    schemaVersion= "1.0",
    minMuleVersion="3.8.0",
    // namespace= "http://www.mulesoft.org/schema/mule/coap-client",
    schemaLocation= "http://www.teslanet.nl/schema/mule/coap-client/1.0/mule-coap-client.xsd",
    keywords="coap"
    
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
    /**
     * Test connectivity
     * @throws MalformedUriException cannot form valid uri with given parameters
     * @throws ConnectionException server could not be reached
     */
    @TestConnectivity
    public void test() throws MalformedUriException, ConnectionException 
    {
        CoapClient client= createClient( true,  null, null, "/", null );

        if ( client == null || !client.ping() )
        {
            throw new ConnectionException( ConnectionExceptionCode.CANNOT_REACH, "coap ping failure on uri { " + ( client == null ? "null" : client.getURI()) + " }", null );
        }
        if ( client != null )
        {
            // resources= client.discover();
            client.shutdown();
        }
    }

    /**
     *  The startConnector method creates an CoAP endpoint using the configuration parameters.
     *  @throws ConnectionException The CoAP endpoint could not be created.
     */    
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
            if ( config.isLogMessages() )
            {
                // add special interceptor for message traces
                endpoint.addInterceptor( new MessageTracer() );
            }
            endpoint.start();
        }
        catch ( Exception e )
        {
            throw new ConnectionException( ConnectionExceptionCode.UNKNOWN, "coap endpoint fault", "coap uri endpoint", e );
        }
    }

    /**
     *  The stopConnector method terminates the CoAP endpoint. 
     *  Resources are freed.
     *  Observing clients will be notified using a proactive cancel.
     */    
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

    /**
     * Create the CoAP endpoint.
     * @param config The configuration parameters for the CoAP Endpoint.
     * @return The CoAP created Endpoint.
     * @throws EndpointConstructionException endpoint could not be created with given parameters
     */        
    private CoapEndpoint createEndpoint( CoAPClientConfig config ) throws EndpointConstructionException
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
            KeyStore keyStore;
            try
            {
                keyStore= KeyStore.getInstance( "JKS" );
            }
            catch ( KeyStoreException e1 )
            {
                throw new EndpointConstructionException( "cannot create JKS keystore instance", e1 );
            }
            InputStream in;
            try
            {
                in= IOUtils.getResourceAsStream( config.getKeyStoreLocation(), this.getClass(), true, true );
            }
            catch ( IOException e1 )
            {
                throw new EndpointConstructionException( "cannot load keystore from { " + config.getKeyStoreLocation() + " }", e1 );
            }
            try
            {
                keyStore.load( in, config.getKeyStorePassword().toCharArray() );
            }
            catch ( NoSuchAlgorithmException | CertificateException | IOException e1 )
            {
                throw new EndpointConstructionException( "cannot load keystore from { " + config.getKeyStoreLocation() + " } using passwd ***", e1 );
            }

            // load the trust store
            KeyStore trustStore;
            try
            {
                trustStore= KeyStore.getInstance( "JKS" );
            }
            catch ( KeyStoreException e1 )
            {
                throw new EndpointConstructionException( "cannot create JKS truststore instance", e1 );
            }
            InputStream inTrust;
            try
            {
                inTrust= IOUtils.getResourceAsStream( config.getTrustStoreLocation(), this.getClass(), true, true );
            }
            catch ( IOException e1 )
            {
                throw new EndpointConstructionException( "cannot load truststore from { " + config.getTrustStoreLocation() + " }", e1 );
            }
            try
            {
                trustStore.load( inTrust, config.getTrustStorePassword().toCharArray() );
            }
            catch ( NoSuchAlgorithmException | CertificateException | IOException e1 )
            {
                throw new EndpointConstructionException( "cannot load truststore from { " + config.getTrustStoreLocation() + " } using passwd ***", e1 );
            }

            // You can load multiple certificates if needed
            DtlsConnectorConfig.Builder configBuider= new DtlsConnectorConfig.Builder( config.getLocalAddress() );
            configBuider.setPskStore( pskStore );
            try
            {
                configBuider.setTrustStore( trustStore.getCertificateChain( config.getTrustedRootCertificateAlias() ) );
            }
            catch ( Exception e )
            {
                throw new EndpointConstructionException( "certificate chain with alias { " + config.getTrustedRootCertificateAlias()  + " } not found in truststore", e );
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
                throw new EndpointConstructionException( "identity with private key alias { " + config.getPrivateKeyAlias() + " } could not be set" );
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
     * The Ping messageprocessor checks whether a CoAP resource is reachable.
     * @param host The host to ping. Overrides the connector setting.
     * @param port The port the host listens on. Overrides the connector setting.
     * @return true when ping was successful, otherwise false.
     * @throws MalformedUriException cannot form valid uri with given parameters
     */
    @Processor
    public Boolean ping
    (
        @Optional String host,
        @Optional Integer port
    ) 
        throws MalformedUriException
    {
        CoapClient client= createClient( true, host, port, "/", null );

        boolean response= client.ping();
        return new Boolean( response );
    }

    /**
     * Discover message-processor retrieves information about CoAP resources from a server.
     * The host and port are optional parameters that override Connector configuration
     * @param confirmable When true (default), requests are sent confirmable.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param  queryParameters The optional query-parameters for discovery.
     * @return A Set of Weblinks describing the resources on the server. When the retrieval of the set of Weblinks failed, null is returned.
     * @throws MalformedUriException cannot form valid uri with given parameters
     */
    //TODO: Hide Californium API from Mule application

    @Processor
    public Set< WebLink > discover
    ( 
        @Default( value= "true" ) Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
    	@Optional List< String > queryParameters 
	) throws MalformedUriException
    {
        CoapClient client= createClient( confirmable, host, port, "/", null );
        return client.discover( toQueryString( queryParameters ) );
    }

    /**
     * Get messageprocessor retrieves the contents of a CoAP resource from a Server.
     * The resource url can be set, overriding connector configuration.
     * @param confirmable When true the server must confirm the request.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @return On success the contents of the CoAP resource is returned in a byte array( byte[] ) as message payload. 
     * Otherwise the payload will be empty.
     * @throws MalformedUriException cannot form valid uri with given parameters
     * @throws HandlerException response handler with given name not found
     */
    @Processor
    public MuleEvent get(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters ) throws MalformedUriException, HandlerException 
    {
        return doRequest( event, CoAP.Code.GET, confirmable, host, port, path, queryParameters, null );
    }

    /**
     * Async-Get messageprocessor asynchronously retrieves the contents of a CoAP resource from a Server.
     * The resource url can be set, overriding connector configuration.
     * @param confirmable When true the server must confirm the request.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @param responseHandler Name of the handler that will process the returned response.
     * @return The MuleMessage is returned unchanged.
     * @throws MalformedUriException cannot form valid uri with given parameters
     * @throws HandlerException response handler with given name not found
     */
    @Processor
    public MuleEvent asyncGet(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters,
        String responseHandler ) throws MalformedUriException, HandlerException 
    {
        return doRequest( event, CoAP.Code.GET, confirmable, host, port, path, queryParameters, responseHandler );
    }

    /**
     * Put messageprocessor changes the contents of a CoAP resource on a Server.
     * The resource url can be set, overriding connector configuration.
     * @param confirmable When true the server must confirm the request.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @return On success the response payload - if any - is returned in a byte array( byte[] ) as message payload. 
     * Otherwise the payload will be empty.
     * @throws MalformedUriException cannot form valid uri with given parameters
     * @throws HandlerException response handler with given name not found
     */
    @Processor
    public MuleEvent put(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters ) throws MalformedUriException, HandlerException 
    {
        return doRequest( event, CoAP.Code.PUT, confirmable, host, port, path, queryParameters, null );
    }

    /**
     * Async-Put messageprocessor asynchronously changes the contents of a CoAP resource on a Server.
     * The resource url can be set, overriding connector configuration.
     * @param confirmable When true the server must confirm the request.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @param responseHandler Name of the handler that will process the returned response.
     * @return The MuleMessage is returned unchanged.
     * @throws MalformedUriException cannot form valid uri with given parameters
     * @throws HandlerException response handler with given name not found
     */
    @Processor
    public MuleEvent asyncPut(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters,
        String responseHandler ) throws MalformedUriException, HandlerException 
    {
        return doRequest( event, CoAP.Code.PUT, confirmable, host, port, path, queryParameters, responseHandler );
    }

    /**
     * Post messageprocessor delivers contents to a CoAP resource on a Server.
     * The resource url can be set, overriding connector configuration.
     * @param confirmable When true the server must confirm the request.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @return On success the response payload - if any - is returned in a byte array( byte[] ) as message payload. 
     * Otherwise the payload will be empty.
     * @throws MalformedUriException cannot form valid uri with given parameters
     * @throws HandlerException response handler with given name not found
     */
    @Processor
    public MuleEvent post(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters ) throws MalformedUriException, HandlerException 
    {
        return doRequest( event, CoAP.Code.POST, confirmable, host, port, path, queryParameters, null );
    }

    /**
     * Async-Put messageprocessor asynchronously delivers contents to a CoAP resource on a Server.
     * The resource url can be set, overriding connector configuration.
     * @param confirmable When true the server must confirm the request.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @param responseHandler Name of the handler that will process the returned response.
     * @return The MuleMessage is returned unchanged.
     * @throws MalformedUriException cannot form valid uri with given parameters
     * @throws HandlerException response handler with given name not found
     */
    @Processor
    public MuleEvent asyncPost(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters,
        String responseHandler ) throws MalformedUriException, HandlerException 
    {
        return doRequest( event, CoAP.Code.POST, confirmable, host, port, path, queryParameters, responseHandler );
    }

    /**
     * Delete messageprocessor deletes a CoAP resource on a Server.
     * The resource url can be set, overriding connector configuration.
     * @param confirmable When true the server must confirm the request.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @return On success the response payload - if any - is returned in a byte array( byte[] ) as message payload. 
     * Otherwise the payload will be empty.
     * @throws MalformedUriException cannot form valid uri with given parameters
     * @throws HandlerException response handler with given name not found
     */
    @Processor
    public MuleEvent delete(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters ) throws MalformedUriException, HandlerException 
    {

        return doRequest( event, CoAP.Code.DELETE, confirmable, host, port, path, queryParameters, null );
    }

    /**
     * Async-Delete messageprocessor asynchronously deletes a CoAP resource on a Server.
     * The resource url can be set, overriding connector configuration.
     * @param confirmable When true the server must confirm the request.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @param responseHandler Name of the handler that will process the returned response.
     * @return The MuleMessage is returned unchanged.
     * @throws MalformedUriException cannot form valid uri with given parameters
     * @throws HandlerException response handler with given name not found
     */
    @Processor
    public MuleEvent asyncDelete(
        MuleEvent event,
        @Default(value= "true") Boolean confirmable,
        @Optional String host,
        @Optional Integer port,
        String path,
        @Optional List< String > queryParameters,
        String responseHandler ) throws MalformedUriException, HandlerException 
    {

        return doRequest( event, CoAP.Code.DELETE, confirmable, host, port, path, queryParameters, responseHandler );
    }

    /**
     * Start-observe messageprocessor dynamically initiates observation of a CoAP resource on a Server.
     * The resource url can be set, overriding connector configuration.
     * @param confirmable When true (default), requests are sent confirmable.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @param responseHandler Name of the handler that will process the resource updates received from server.
     * @throws MalformedUriException cannot form valid uri with given parameters
     * @throws HandlerException response handler with given name not found
     */
    //TODO: return mule event?
    @Processor
    public void startObserve
    ( 
        @Default( value= "true" ) Boolean confirmable,
        @Optional String host, 
        @Optional Integer port, 
        String path, 
        @Optional List< String > queryParameters, 
        String responseHandler
    ) throws MalformedUriException, HandlerException 
    {
        final CoapClient client= createClient( confirmable, host, port, path, toQueryString( queryParameters ) );

        final SourceCallback callback= handlers.get( responseHandler );
        if ( callback == null ) throw new HandlerException( "response handler { " + responseHandler + " }");

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
                    processMuleFlow( null, client, callback, Code.GET );
                }

                @Override
                public void onLoad( CoapResponse response )
                {
                    processMuleFlow( response, client, callback, Code.GET );
                }

            };

        CoapObserveRelation relation= dynamicRelations.get( client.getURI() );
        if ( relation != null )
        {
            // only one observe relation allowed per connector & path
            // TODO maybe configurable whether proactive or not
            relation.proactiveCancel();
            dynamicRelations.remove( client.getURI() );
        }
        relation= client.observe( handler );
        dynamicRelations.put( client.getURI(), relation );

    }

    /**
     * Stop-observe messageprocessor ends a dynamically set observation of a CoAP resource on a Server.
     * The resource url can be set, overriding connector configuration.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @throws MalformedUriException cannot form valid uri with given parameters
     */
    //TODO: return mule event?
    @Processor
    public void stopObserve( @Optional String host, @Optional Integer port, String path, @Optional List< String > queryParameters ) throws MalformedUriException 
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
     * List-Observations messageprocessor retrieves the list of active dynamic observations on a CoAP resource.
     * The list contains the CoAP resource url that is being observed dynamically.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @return The List of urls of active dynamic observations.
     * @throws Exception Is thrown when an unexpected error occurs
     */
    @Processor
    public List< String > listObservations()
    {
        CopyOnWriteArrayList< String > list= new CopyOnWriteArrayList< String >();
        list.addAll( dynamicRelations.keySet() );
        return list;
    }

    /**
     * Observe messagesource observes a CoAP resource on a Server.
     * The observation is static - meaning the observation will be active as long as the Mule-flow is running.
     * The resource url can be set, overriding connector configuration.
     * @param confirmable When true (default), requests are sent confirmable.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @return Updates on the resource the server sends are returned as a MuleMessage. 
     * CoAP message contents - if any - are delivered as a byte array ( byte[] ) message payload. 
     * When there are no CoAP contents the payload will be empty.
     * @throws MalformedUriException no valid uri could be formed with given parameters
     * @throws Exception Is thrown when an unexpected error occurs
     */
    @Source
    public void observe
    ( 
        final SourceCallback callback,
        @Default( value= "true" ) Boolean confirmable,
        @Optional String host, 
        @Optional Integer port, 
        String path, 
        @Optional List< String > queryParameters
    ) throws MalformedUriException 
    {
        final CoapClient client= createClient( confirmable, host, port, path, toQueryString( queryParameters ) );

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
                    processMuleFlow( null, client, callback, Code.GET );
                }

                @Override
                public void onLoad( CoapResponse response )
                {
                    processMuleFlow( response, client, callback, Code.GET );
                }

            } );
        staticRelations.put( client.getURI(), relation );
    }

    /**
     * Handle-Response messagesource handles responses on asynchronous requests that reference this handler.
     * The Handler can process the responses of multiple requests and it the type of requests can differ. 
     * So one Handler could be used to process responses to asynchronous Get, Put, Post, Delete or Observe requests.
     * @param handlerName The name of the hander. Used by asynchronous requests to reference the handler.
     * @return Responses on asynchronous requests are returned as a MuleMessage. 
     * CoAP response contents - if any - are delivered as a byte array ( byte[] ) message payload. 
     * When there are no CoAP contents the payload will be empty.
     * @throws HandlerException 
     * @throws Exception Is thrown when an unexpected 
     */
    @Source
    public void handleResponse( final SourceCallback callback, String handlerName ) throws HandlerException
    {
        if ( handlerName == null || handlerName.isEmpty() ) throw new HandlerException( "empty responsehandler name not allowed" );
        if ( handlers.get( handlerName ) != null ) throw new HandlerException( "responsehandler name { " + handlerName + " } not unique" );
        handlers.put( handlerName, callback );
    }



    /**
     * Get an URI object describing the given CoAP resource.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param query String containing query parameters.
     * @return The URI object. 
     * @throws MalformedUriException cannot form valid uri with given parameters
     */ 
    public URI getURI( String host, Integer port, String path, String query ) throws MalformedUriException 
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
        try
        {
            return new URI( scheme, null, uriHost, uriPort, path, query, null );
        }
        catch ( URISyntaxException e )
        {
            throw new MalformedUriException( "cannot form valid uri using: { " + host + ", " +  port + ", " + path + ", " + query + " }");
        }
    }

    /**
     * Get a String object containing the uri of the given CoAP resource.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param query String containing query parameters.
     * @return The String containing the uri. 
     * @throws MalformedUriException cannot form valid uri with given parameters
     */     
    public String getUri( String host, Integer port, String path, String query ) throws MalformedUriException 
    {
        return getURI( host, port, path, query ).toString();
    }

    /**
     * Get a querystring containing containing query parameters that can be use as part of a an Uri-string.
     * @param queryParameters List of query parameters.
     * @return The querystring. 
     */     
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

    /**
     * Create a client object that can be used to issue requests.
     * @param confirmable 
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param query String containing query parameters.
     * @return The client object.
     * @exception MalformedEndpointException The client uri is invalid.
     * @throws MalformedUriException 
     */     
    private CoapClient createClient( Boolean confirmable, String host, Integer port, String path, String query ) throws MalformedUriException
    {
        CoapClient client= new CoapClient( getUri( host, port, path, query ) );
        client.setEndpoint( endpoint );
        if ( confirmable)
        {
            client.useCONs();
        }
        else
        {
            client.useNONs();
        }
        return client;
    }

    /**
     * Send request to the CoAP server.
     * @param event The input event.
     * @param requestCode The request type.
     * @param confirmable When true the request must be confirmed by the server.
     * @param host The host address of the server.
     * @param port The port the server is listening on.
     * @param path The path of the resource.
     * @param queryParameters List of query parameters.
     * @param handlerName Optional name of the handler. Use when the response should be handled asynchronously 
     * @return The response Mule event containing the response message.
     * @throws MalformedUriException no valid coap uri could be built with given parameters
     * @throws HandlerException handler not found
     */    
    // TODO add custom timeout, endpoint, networkconfig?
    private MuleEvent doRequest(
        MuleEvent event,
        final Code requestCode,
        Boolean confirmable,
        String host,
        Integer port,
        String path,
        List< String > queryParameters,
        String handlerName ) throws MalformedUriException, HandlerException
    {
        CoapHandler handler= null;

        final CoapClient client= createClient( confirmable, host, port, path, toQueryString( queryParameters ) );
 
        // build request
        MuleMessage muleMessage= event.getMessage();
        
        Request request= new Request( requestCode );

        Object requestPayload= muleMessage.getPayload();

        if ( requestPayload != null && ! NullPayload.getInstance().equals( requestPayload ) )
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
            if ( callback == null ) throw new HandlerException( "referenced handler { " + handlerName + " } not found");
            handler= createHandler( client, callback, requestCode );
        }
        if ( handler == null )
        {
            // send out synchronous request
            CoapResponse response= client.advanced( request );

            // return response to Mule flow
            return createProcessorMuleEvent( response, client, requestCode, event );
        }
        else
        {
            // asynchronous request
            client.advanced( handler, request );
            // return unchanged event to Mule flow
            // TODO maybe VoidMuleEvent
            return( event );
        }

    }

    /**
     * Create a Handler of CoAP responses.
     * @param client  The Coap client that produced the response
     * @param callback The Listening Messageprocessor that nedds to be called
     * @param requestCode The coap request code from the request context
     * @return
     */
    private CoapHandler createHandler( final CoapClient client, final SourceCallback callback, final Code requestCode )
    {
        return new CoapHandler(  )
        {
            @Override
            public void onError()
            {
                processMuleFlow( null, client, callback, requestCode );   
            }
    
            @Override
            public void onLoad( CoapResponse response )
            {
                processMuleFlow( response, client, callback, requestCode );
           }
    
        };
    }
    

    
    /**
     * Create response MuleMessage. The payload will be set to the CoAP payload. 
     * CoAP metadata including CoAP options will be added as inbound properties.  
     * @param response the CoAP response that needs to be delivered into the Mule flow
     * @param client The client object tht issued the request.
     * @param requestCode The type of request that was issued.
     * @return MuleMessage created.
     */
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
            //a response received from server
        	
        	inboundProps.put( PropertyNames.COAP_REQUEST_CODE, requestCode );
            inboundProps.put( PropertyNames.COAP_REQUEST_URI, client.getURI() );
            inboundProps.put( PropertyNames.COAP_RESPONSE_SUCCESS, new Boolean( response.isSuccess() ) );
            //TODO: response code toString gives number format (9.99), this is not in line with server connector that uses text format for the property
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

    /**
     * Create response MuleEvent in the context of a Message processor. The payload will be set to the CoAP payload. 
     * CoAP metadata including CoAP options will be added as inbound properties.  
     * @param response the CoAP response that needs to be delivered into the Mule flow
     * @param client The client object tht issued the request.
     * @param requestCode The type of request that was issued.
     * @param rewriteEvent The input event.
     * @return MuleMessage created.
     */
    private MuleEvent createProcessorMuleEvent( CoapResponse response, CoapClient client, Code requestCode, MuleEvent rewriteEvent )
    {
        MuleMessage responseMuleMessage= createMuleMessage( response, client, requestCode );
        DefaultMuleEvent result= new DefaultMuleEvent( responseMuleMessage, rewriteEvent );

        return result;
    }
    
    /**
     * Gets the message processed through the Mule flow 
     * @param response The Coap response to handled by the mule flow
     * @param client  The Coap client that produced the response
     * @param callback The Listening Messageprocessor that nedds to be called
     * @param requestCode The coap request code from the request context
     */
    private void processMuleFlow( CoapResponse response, final CoapClient client, final SourceCallback callback, final Code requestCode )   
    {
        @SuppressWarnings("unused")
        MuleEvent responseEvent= null;
        
        AbstractListeningMessageProcessor processor= (AbstractListeningMessageProcessor) callback;

        //TODO make safe:
        MuleMessage muleMessage = createMuleMessage( response, client, requestCode );
        MuleEvent muleEvent= new DefaultMuleEvent( muleMessage, MessageExchangePattern.ONE_WAY, processor.getFlowConstruct() );
        try
        {
            responseEvent= processor.processEvent( muleEvent );
        }
        catch ( MuleException ex )
        {
            //handle over to Flow's exception handling
            responseEvent= processor.getFlowConstruct().getExceptionListener().handleException( ex, muleEvent );
        }
    };
    
    /**
     * Gets the Mule context.
     * @return The Mule context.
     */ 
    public MuleContext getMuleContext()
    {
        return muleContext;
    }

     /**
      * Sets the The Mule context. The context will be set by Mule on application start.
      * @param context The Mule context.
      */  
    public void setMuleContext( MuleContext context )
    {
        this.muleContext= context;
    }
    
    /**
     * Gets the Connector configuration.
     * @return The Connector configuration.
     */    
    public CoAPClientConfig getConfig()
    {
        return config;
    }

    /**
     * Sets the Connector configuration. The configuration object will be set by Mule on application start.
     * @param config The Connector configuration.
     */  
    public void setConfig( CoAPClientConfig config )
    {
        this.config= config;
    }

}