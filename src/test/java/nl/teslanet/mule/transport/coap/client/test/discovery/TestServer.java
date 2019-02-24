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
package nl.teslanet.mule.transport.coap.client.test.discovery;


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
        // provide an instance of a basic resource
        add( new GetResource( "basic" ) );
        getRoot().getChild( "basic" ).add( new GetResource( "get_me" ) );
        getRoot().getChild( "basic" ).add( new NoneResource( "do_not_get_me" ) );
        getRoot().getChild( "basic" ).add( new PutResource( "put_me" ) );
        getRoot().getChild( "basic" ).add( new NoneResource( "do_not_put_me" ) );
        getRoot().getChild( "basic" ).add( new PostResource( "post_me" ) );
        getRoot().getChild( "basic" ).add( new NoneResource( "do_not_post_me" ) );
        getRoot().getChild( "basic" ).add( new DeleteResource( "delete_me" ) );
        getRoot().getChild( "basic" ).add( new NoneResource( "do_not_delete_me" ) );
        // provide an instance of a service resource
        add( new GetResource( "service" ) );
        getRoot().getChild( "service" ).add( new CtResource( "resource_with_ct" ) );
        getRoot().getChild( "service" ).add( new IfResource( "resource_with_if" ) );
        getRoot().getChild( "service" ).add( new ObsResource( "resource_with_obs" ) );
        getRoot().getChild( "service" ).add( new RtResource( "resource_with_rt" ) );
        getRoot().getChild( "service" ).add( new SzResource( "resource_with_sz" ) );
        getRoot().getChild( "service" ).add( new TitleResource( "resource_with_title" ) );       
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
    
    /**
     * Resource with content types
     */
    class CtResource extends CoapResource
    {
        public CtResource( String name )
        {
            // set resource name
            super( name );
            // set display name
            getAttributes().setTitle( name );
            //set ContentType
            getAttributes().addContentType( 0 );
            getAttributes().addContentType( 41 );
        }
    }
    
    /**
     * Resource with interface descriptions
     */
    class IfResource extends CoapResource
    {
        public IfResource( String name )
        {
            // set resource name
            super( name );
            // set display name
            getAttributes().setTitle( name );
            //set interface descriptions
            getAttributes().addInterfaceDescription( "if1" );
            getAttributes().addInterfaceDescription( "if2" );
        }
    }

    //TODO add observe type option
    /**
     * Resource that is observable
     */
    class ObsResource extends CoapResource
    {
        public ObsResource( String name )
        {
            // set resource name
            super( name );
           // set display name
            getAttributes().setTitle( name );
            //set observable
            setObservable( true );
        }
    }

    /**
     * Resource with resource type
     */
    class RtResource extends CoapResource
    {
        public RtResource( String name )
        {
            // set resource name
            super( name );
            // set display name
            getAttributes().setTitle( name );
            //set interface descriptions
            getAttributes().addResourceType( "rt1" );
            getAttributes().addResourceType( "rt2" );
        }
    }

    /**
     * Resource with size
     */
    class SzResource extends CoapResource
    {
        public SzResource( String name )
        {
            // set resource name
            super( name );
            // set display name
            getAttributes().setTitle( name );
            //set interface descriptions
            getAttributes().setMaximumSizeEstimate( 123456 );
        }
    }
    /**
     * Resource with title
     */
    class TitleResource extends CoapResource
    {
        public TitleResource( String name )
        {
            // set resource name
            super( name );
            // set display name
            getAttributes().setTitle( "Title is "+ name );
            //set interface descriptions
            getAttributes().setMaximumSizeEstimate( 123456 );
        }
    }


}