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
package nl.teslanet.mule.transport.coap.client.test.basic;


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
public class ResponseTestServer extends CoapServer
{
    /**
     * Network configuration is set to standards 
     */
    private static NetworkConfig networkConfig= NetworkConfig.createStandardWithoutFile();

    /**
     * Default Constructor for test server.
     */
    public ResponseTestServer() throws SocketException
    {
        this( CoAP.DEFAULT_COAP_PORT );
    }

    /**
     * Constructor for test server.
     */
    public ResponseTestServer( int port ) throws SocketException
    {
        super( networkConfig );
        addEndpoints( port );
        addResources();
    }

    private void addResources()
    {
        // provide an instance of a Hello-World resource
        add( new ResponseResource( "response", ResponseCode.CONTENT ) );
        Resource parent= getRoot().getChild( "response" );
        for ( ResponseCode code : ResponseCode.values() )
        {
            parent.add( new ResponseResource( code.name(), code) );
        }
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
    class ResponseResource extends CoapResource
    {
        /**
         * the request payload size to verify
         */
        private ResponseCode responseCode;

        /**
         * @param responseCode the response to return
         */
        public ResponseResource( String name, ResponseCode responseCode )
        {
            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );

            //set response
            this.responseCode= responseCode;
        }

        @Override
        public void handleGET( CoapExchange exchange )
        {
            handleRequest( exchange );
        }

        @Override
        public void handlePOST( CoapExchange exchange )
        {
            handleRequest( exchange );
        }

        @Override
        public void handlePUT( CoapExchange exchange )
        {
            handleRequest( exchange );
        }

        @Override
        public void handleDELETE( CoapExchange exchange )
        {
            handleRequest( exchange );
        }

        /**
         * Generic handler
         * @param exchange
         */
        private void handleRequest( CoapExchange exchange )
        {
            exchange.respond( responseCode, "Response is: " + responseCode.name() );
        }
    }
}