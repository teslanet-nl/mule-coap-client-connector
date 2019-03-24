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
package nl.teslanet.mule.transport.coap.client.test.exceptionhandling;


import java.net.InetSocketAddress;
import java.net.SocketException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;


/**
 * Server used to test client 
 *
 */
public class ExceptionHandlingTestServer extends CoapServer
{
    /**
     * Network configuration is set to standards 
     */
    private static NetworkConfig networkConfig= NetworkConfig.createStandardWithoutFile();

    /**
     * Default Constructor for test server.
     */
    public ExceptionHandlingTestServer() throws SocketException
    {
        this( CoAP.DEFAULT_COAP_PORT );
    }

    /**
     * Constructor for test server.
     */
    public ExceptionHandlingTestServer( int port ) throws SocketException
    {
        super( networkConfig );
        addEndpoints( port );
        addResources();
    }

    private void addResources()
    {
        // provide an instance of a Hello-World resource
        add( new TestResource( "service" ) );
        Resource parent= getRoot().getChild( "service" );
        parent.add( new TestResource( "get_me" ));
        parent.add( new TestResource( "put_me" ));
        parent.add( new TestResource( "post_me" ));
        parent.add( new TestResource( "delete_me" ));
    }

    /**
     * Add test endpoints listening on default CoAP port.
     */
    private void addEndpoints( int port )
    {
        InetSocketAddress bindToAddress= new InetSocketAddress( port );
        addEndpoint( new CoapEndpoint( bindToAddress ) );
    }

    /**
     * Resource that to test payloads
     */
    class TestResource extends CoapResource
    {
        /**
         * @param responseCode the response to return
         */
        public TestResource( String name )
        {
            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );
        }

        @Override
        public void handleGET( CoapExchange exchange )
        {
            ResponseCode responseCode= ResponseCode.CONTENT;
            exchange.respond( responseCode, "Response is: " + responseCode.name() );
        }

        @Override
        public void handlePOST( CoapExchange exchange )
        {
            ResponseCode responseCode= ResponseCode.CREATED;
            exchange.respond( responseCode, "Response is: " + responseCode.name() );
        }

        @Override
        public void handlePUT( CoapExchange exchange )
        {
            ResponseCode responseCode= ResponseCode.CHANGED;
            exchange.respond( responseCode, "Response is: " + responseCode.name() );
        }

        @Override
        public void handleDELETE( CoapExchange exchange )
        {
            ResponseCode responseCode= ResponseCode.DELETED;
            exchange.respond( responseCode, "Response is: " + responseCode.name() );
        }
    }
}