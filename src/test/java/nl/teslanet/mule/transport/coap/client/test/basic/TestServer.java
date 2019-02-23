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
     * Constructor for test server.
     */
    public TestServer() throws SocketException
    {
        super( networkConfig );
        addEndpoints();
        // provide an instance of a Hello-World resource
        add( new GetResource( "basic" ) );
        getRoot().getChild( "basic" ).add( new GetResource( "get_me" ) );
        getRoot().getChild( "basic" ).add( new NoneResource( "do_not_get_me" ) );
        getRoot().getChild( "basic" ).add( new PutResource( "put_me" ) );
        getRoot().getChild( "basic" ).add( new NoneResource( "do_not_put_me" ) );
        getRoot().getChild( "basic" ).add( new PostResource( "post_me" ) );
        getRoot().getChild( "basic" ).add( new NoneResource( "do_not_post_me" ) );
        getRoot().getChild( "basic" ).add( new DeleteResource( "delete_me" ) );
        getRoot().getChild( "basic" ).add( new NoneResource( "do_not_delete_me" ) );
    }

    /**
     * Add test endpoints listening on default CoAP port.
     */
    private void addEndpoints()
    {
        InetSocketAddress bindToAddress= new InetSocketAddress( CoAP.DEFAULT_COAP_PORT );
        addEndpoint( new CoapEndpoint( bindToAddress ) );
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