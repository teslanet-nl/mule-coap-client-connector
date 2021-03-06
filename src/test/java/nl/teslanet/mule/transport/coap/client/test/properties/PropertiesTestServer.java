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
package nl.teslanet.mule.transport.coap.client.test.properties;


import java.net.InetSocketAddress;
import java.net.SocketException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;


/**
 * Server used to test observing client 
 *
 */
public class PropertiesTestServer extends CoapServer
{
    OptionStrategy strategy;
    /**
     * Network configuration is set to standards 
     */
    private static NetworkConfig networkConfig= NetworkConfig.createStandardWithoutFile();

    /**
     * Default Constructor for test server.
     */
    public PropertiesTestServer(OptionStrategy strategy ) throws SocketException
    {
        this( strategy, CoAP.DEFAULT_COAP_PORT );
    }

    /**
     * Constructor for test server.
     */
    public PropertiesTestServer( OptionStrategy strategy,  int port ) throws SocketException
    {
        super( networkConfig );
        addEndpoints( port );
        addResources();
        this.strategy= strategy;
    }

    private void addResources()
    {
        // provide an instance of an observable resource
        add( new ValidateOptionResource( "property") );
        Resource parent= getRoot().getChild( "property" );
        parent.add( new ValidateOptionResource( "validate" ) );
        parent.add( new SetOptionResource( "setoption" ) );
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
     * Resource that validates inbound options
     */
    class ValidateOptionResource extends CoapResource
    {
        /**
         * Constructor
         * @param name of the resource
         */
        public ValidateOptionResource( String name )
        {
            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );
        }

        @Override
        public void handleGET( CoapExchange exchange )
        {
            if ( strategy.validateOption( exchange.advanced().getRequest() ))
            {
                exchange.respond( ResponseCode.CONTENT );
            }
            else
            {
                exchange.respond( ResponseCode.BAD_OPTION );
            }
        }

        @Override
        public void handlePOST( CoapExchange exchange )
        {
            if ( strategy.validateOption( exchange.advanced().getRequest() ))
            {
                exchange.respond( ResponseCode.CHANGED );
            }
            else
            {
                exchange.respond( ResponseCode.BAD_OPTION );
            }
        }

        @Override
        public void handlePUT( CoapExchange exchange )
        {
            if ( strategy.validateOption( exchange.advanced().getRequest() ))
            {
                exchange.respond( ResponseCode.CHANGED );
            }
            else
            {
                exchange.respond( ResponseCode.BAD_OPTION );
            }
        }

        @Override
        public void handleDELETE( CoapExchange exchange )
        {
            if ( strategy.validateOption( exchange.advanced().getRequest() ))
            {
                exchange.respond( ResponseCode.DELETED );
            }
            else
            {
                exchange.respond( ResponseCode.BAD_OPTION );
            }
        }
    }
    /**
     * Resource that sets outbound options
     */
    class SetOptionResource extends CoapResource
    {
        /**
         * Constructor
         * @param name of the resource
         */
        public SetOptionResource( String name )
        {
            // set resource name
            super( name );

            // set display name
            getAttributes().setTitle( name );
        }

        @Override
        public void handleGET( CoapExchange exchange )
        {
            Response response= new Response( ResponseCode.CONTENT );
            strategy.setOption( response );
            exchange.respond( response );
        }

        @Override
        public void handlePOST( CoapExchange exchange )
        {
            Response response= new Response( ResponseCode.CHANGED );
            strategy.setOption( response );
            exchange.respond( response );
        }

        @Override
        public void handlePUT( CoapExchange exchange )
        {
            Response response= new Response( ResponseCode.CHANGED );
            strategy.setOption( response );
            exchange.respond( response );
        }

        @Override
        public void handleDELETE( CoapExchange exchange )
        {
            Response response= new Response( ResponseCode.DELETED );
            strategy.setOption( response );
            exchange.respond( response );
        }
    }
}