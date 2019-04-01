/*******************************************************************************
 * Copyright (c) 2019 (teslanet.nl) Rogier Cobben.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License - v 2.0 
 * which accompanies this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *    (teslanet.nl) Rogier Cobben - initial creation
 ******************************************************************************/
package nl.teslanet.mule.transport.coap.client.test.secure;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.logging.Level;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.ScandiumLogger;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;


/**
 * Server used to test client 
 *
 */
public class SecureTestServer extends CoapServer
{
    //TODO add logging options to connector
    static
    {
        CaliforniumLogger.initialize();
        CaliforniumLogger.setLevel( Level.CONFIG );
        ScandiumLogger.initialize();
        ScandiumLogger.setLevel( Level.FINER );
    }

    // allows configuration via Californium.properties
    //public static final int DTLS_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_SECURE_PORT);

    /**
     * Set truststore password
     */
    private static final String TRUST_STORE_PASSWORD= "rootPass";

    /**
     * Set key (store) password
     */
    private final static String KEY_STORE_PASSWORD= "endPass";

    /**
     * Set key store location on classpath
     */
    private static final String KEY_STORE_LOCATION= "certs/keyStore.jks";

    /**
     * Set trust store location  on classpath
     */
    private static final String TRUST_STORE_LOCATION= "certs/trustStore.jks";

    /**
     * Network configuration is set to standards 
     */
    private static NetworkConfig networkConfig= NetworkConfig.createStandardWithoutFile();

    /**
     * Default Constructor for test server.
     * @throws IOException 
     * @throws CertificateException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyStoreException 
     * @throws UnrecoverableKeyException 
     */
    public SecureTestServer() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
    {
        this( CoAP.DEFAULT_COAP_SECURE_PORT );
    }

    /**
     * Constructor for test server listening on non default port.
     * @throws KeyStoreException 
     * @throws IOException 
     * @throws CertificateException 
     * @throws NoSuchAlgorithmException 
     * @throws UnrecoverableKeyException 
     */
    public SecureTestServer( int port ) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException
    {
        super( networkConfig );

        // Pre-shared secrets
        InMemoryPskStore pskStore= new InMemoryPskStore();
        pskStore.setKey( "password", "sesame".getBytes() ); // from ETSI Plugtportest test spec

        // load the trust store
        KeyStore trustStore= KeyStore.getInstance( "JKS" );
        InputStream inTrust= SecureTestServer.class.getClassLoader().getResourceAsStream( TRUST_STORE_LOCATION );
        trustStore.load( inTrust, TRUST_STORE_PASSWORD.toCharArray() );

        // You can load multiple certificates if needed
        Certificate[] trustedCertificates= new Certificate [1];
        trustedCertificates[0]= trustStore.getCertificate( "root" );

        // load the key store
        KeyStore keyStore= KeyStore.getInstance( "JKS" );
        InputStream in= SecureTestServer.class.getClassLoader().getResourceAsStream( KEY_STORE_LOCATION );
        keyStore.load( in, KEY_STORE_PASSWORD.toCharArray() );

        DtlsConnectorConfig.Builder config= new DtlsConnectorConfig.Builder( new InetSocketAddress( port ) );
        config.setSupportedCipherSuites( new CipherSuite []{ CipherSuite.TLS_PSK_WITH_AES_128_CCM_8, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8 } );
        config.setPskStore( pskStore );
        config.setIdentity( (PrivateKey) keyStore.getKey( "server", KEY_STORE_PASSWORD.toCharArray() ), keyStore.getCertificateChain( "server" ), true );
        config.setTrustStore( trustedCertificates );

        DTLSConnector connector= new DTLSConnector( config.build() );
        addEndpoint(new CoapEndpoint(connector, networkConfig ));
        addResources();
    }

    private void addResources()
    {
        // provide an instance of a Hello-World resource
        add( new GetResource( "secure" ) );
        getRoot().getChild( "secure" ).add( new GetResource( "get_me" ) );
        getRoot().getChild( "secure" ).add( new NoneResource( "do_not_get_me" ) );
        getRoot().getChild( "secure" ).add( new PutResource( "put_me" ) );
        getRoot().getChild( "secure" ).add( new NoneResource( "do_not_put_me" ) );
        getRoot().getChild( "secure" ).add( new PostResource( "post_me" ) );
        getRoot().getChild( "secure" ).add( new NoneResource( "do_not_post_me" ) );
        getRoot().getChild( "secure" ).add( new DeleteResource( "delete_me" ) );
        getRoot().getChild( "secure" ).add( new NoneResource( "do_not_delete_me" ) );
    }


    /**
     * Resource without operations
     */
    class NoneResource extends CoapResource
    {

        public NoneResource( String name )
        {

            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );
        }
    }

    /**
     * Resource that allows GET only
     */
    class GetResource extends CoapResource
    {

        public GetResource( String name )
        {

            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );
        }

        @Override
        public void handleGET( CoapExchange exchange )
        {
            // respond to the request
            exchange.respond( ResponseCode.CONTENT, "GET called on: " + this.getURI() );
        }
    }

    /**
     * Resource that allows POST only
     */
    class PostResource extends CoapResource
    {

        public PostResource( String name )
        {

            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );
        }

        @Override
        public void handlePOST( CoapExchange exchange )
        {
            // respond to the request
            exchange.respond( ResponseCode.CREATED, "POST called on: " + this.getURI() );
        }
    }

    /**
     * Resource that allows PUT only
     */
    class PutResource extends CoapResource
    {

        public PutResource( String name )
        {

            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );
        }

        @Override
        public void handlePUT( CoapExchange exchange )
        {
            // respond to the request
            exchange.respond( ResponseCode.CHANGED, "PUT called on: " + this.getURI() );
        }
    }

    /**
     * Resource that allows DELETE only
     */
    class DeleteResource extends CoapResource
    {

        public DeleteResource( String name )
        {

            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );
        }

        @Override
        public void handleDELETE( CoapExchange exchange )
        {
            // respond to the request
            exchange.respond( ResponseCode.DELETED, "DELETE called on: " + this.getURI() );
        }
    }
}