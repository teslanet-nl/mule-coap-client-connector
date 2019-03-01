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
package nl.teslanet.mule.transport.coap.client.test.observe;


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
 * Server used to test observing client 
 *
 */
public class ObserveTestServer extends CoapServer
{
    /**
     * Network configuration is set to standards 
     */
    private static NetworkConfig networkConfig= NetworkConfig.createStandardWithoutFile();

    /**
     * Default Constructor for test server.
     */
    public ObserveTestServer() throws SocketException
    {
        this( CoAP.DEFAULT_COAP_PORT );
    }

    /**
     * Constructor for test server.
     */
    public ObserveTestServer( int port ) throws SocketException
    {
        super( networkConfig );
        addEndpoints( port );
        addResources();
    }

    private void addResources()
    {
        // provide an instance of an observable resource
        add( new ObservableResource( "observe") );
        Resource parent= getRoot().getChild( "observe" );
        parent.add( new ObservableResource( "temporary" ) );
        parent.add( new ObservableResource( "permanent" ) );
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
    class ObservableResource extends CoapResource
    {
        /**
         * the resource content
         */
        private String content;

        /**
         * @param responseCode the response to return
         */
        public ObservableResource( String name )
        {
            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );

            //set content
            content= "nothing";
            
            //make observable
            setObservable( true );
        }

        @Override
        public void handleGET( CoapExchange exchange )
        {
            exchange.respond( ResponseCode.CONTENT, content );
        }

        @Override
        public void handlePOST( CoapExchange exchange )
        {
            content= exchange.getRequestText();
            exchange.respond( ResponseCode.CHANGED );
            changed();
        }

        @Override
        public void handlePUT( CoapExchange exchange )
        {
            content= exchange.getRequestText();
            exchange.respond( ResponseCode.CHANGED );
            changed();
        }

        @Override
        public void handleDELETE( CoapExchange exchange )
        {
            content= "nothing";
            exchange.respond( ResponseCode.DELETED );
            changed();
        }
    }
}