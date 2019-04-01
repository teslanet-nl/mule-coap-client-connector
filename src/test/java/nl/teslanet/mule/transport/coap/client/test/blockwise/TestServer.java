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
package nl.teslanet.mule.transport.coap.client.test.blockwise;


import java.net.InetSocketAddress;
import java.net.SocketException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;

import nl.teslanet.mule.transport.coap.client.test.utils.Data;


/**
 * Server used to test client 
 *
 */
public class TestServer extends CoapServer
{
    /**
     * Network configuration is set to standards 
     */
    private static NetworkConfig networkConfig= NetworkConfig.createStandardWithoutFile();

    /**
     * Default Constructor for test server.
     */
    public TestServer() throws SocketException
    {
        this( CoAP.DEFAULT_COAP_PORT );
    }

    /**
     * Constructor for test server.
     */
    public TestServer( int port ) throws SocketException
    {
        super( networkConfig );
        addEndpoints( port );
        addResources();
    }

    private void addResources()
    {
        // provide an instance of a Hello-World resource
        add( new PayloadResource( "blockwise", 0, 0 ) );
        getRoot().getChild( "blockwise" ).add( new PayloadResource( "rq0", 0, 2 ) );
        getRoot().getChild( "blockwise" ).add( new PayloadResource( "rsp0", 2, 0 ) );
        getRoot().getChild( "blockwise" ).add( new PayloadResource( "rq10" , 10, 2 ) );
        getRoot().getChild( "blockwise" ).add( new PayloadResource( "rsp10", 2, 10 ) );
        getRoot().getChild( "blockwise" ).add( new PayloadResource( "rq8192" , 8192, 2 ) );
        getRoot().getChild( "blockwise" ).add( new PayloadResource( "rsp8192", 2, 8192 ) );
        getRoot().getChild( "blockwise" ).add( new PayloadResource( "rq16000" , 16000, 2 ) );
        getRoot().getChild( "blockwise" ).add( new PayloadResource( "rsp16000", 2, 16000 ) );
        getRoot().getChild( "blockwise" ).add( new PayloadResource( "rq16001" , 16001, 2 ) );
        getRoot().getChild( "blockwise" ).add( new PayloadResource( "rsp16001", 2, 16001 ) );
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
    class PayloadResource extends CoapResource
    {
        /**
         * the request payload size to verify
         */
        private int requestPayloadSize;

        /**
         * the response payload to return
         */
        private int responsePayloadSize;

        /**
         * @param name identifies the resource
         * @param requestPayloadSize the request payload size to verify
         * @param responsePayloadSize the response payload to return
         */
        public PayloadResource( String name, int requestPayloadSize, int responsePayloadSize )
        {
            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );
            
            //set payload sizes
            this.requestPayloadSize= requestPayloadSize;
            this.responsePayloadSize= responsePayloadSize;
        }

        @Override
        public void handleGET( CoapExchange exchange )
        {
            handleRequest( exchange, ResponseCode.CONTENT, ResponseCode.BAD_REQUEST );
        }

        @Override
        public void handlePOST( CoapExchange exchange )
        {
            handleRequest( exchange, ResponseCode.CREATED, ResponseCode.BAD_REQUEST );
        }

        @Override
        public void handlePUT( CoapExchange exchange )
        {
            handleRequest( exchange, ResponseCode.CHANGED, ResponseCode.BAD_REQUEST );
        }

        @Override
        public void handleDELETE( CoapExchange exchange )
        {
            handleRequest( exchange, ResponseCode.DELETED, ResponseCode.BAD_REQUEST );
        }
        
        /**
         * Generic handler
         * @param exchange
         * @param okResponse response code when valid request
         * @param nokResponse response code when request not valid
         */
        private void handleRequest( CoapExchange exchange, ResponseCode okResponse, ResponseCode nokResponse )
        {
            if ( Data.validateContent( exchange.getRequestPayload(), requestPayloadSize ))
            {
                exchange.respond( okResponse, Data.getContent( responsePayloadSize ) );
            }
            else
            {
                exchange.respond( nokResponse );               
            }
        }
    }
}