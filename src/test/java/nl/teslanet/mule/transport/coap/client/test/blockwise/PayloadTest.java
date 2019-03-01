/*******************************************************************************
 * Copyright (c) 2017, 2018, 2019 (teslanet.nl) Rogier Cobben.
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.munit.common.mocking.MessageProcessorMocker;
import org.mule.munit.common.mocking.SpyProcess;
import org.mule.munit.runner.functional.FunctionalMunitSuite;

import nl.teslanet.mule.transport.coap.server.test.utils.Data;


@RunWith(Parameterized.class)
public class PayloadTest extends FunctionalMunitSuite
{
    @Parameters(name= "Request= {0}, port= {1}, path= {2}, contentSize= {3}")
    public static Collection< Object[] > data()
    {
        return Arrays.asList(
            new Object [] []{
                //default maxResourceBodySize on server
                { Code.GET, 5683, "/service/get_me", 10, true, false },
                { Code.PUT, 5683, "/service/put_me", 10, false, false },
                { Code.POST, 5683, "/service/post_me", 10, false, false },
                { Code.DELETE, 5683, "/service/delete_me", 10, true, false },
//                { Code.GET, 5683, "/service/get_me", 8192, true, false },
                { Code.PUT, 5683, "/service/put_me", 8192, false, false },
                { Code.POST, 5683, "/service/post_me", 8192, false, false },
//                { Code.DELETE, 5683, "/service/delete_me", 8192, true, false },
//                { Code.GET, 5683, "/service/get_me", 16000, true, true },
                { Code.PUT, 5683, "/service/put_me", 16000, false, false },
                { Code.POST, 5683, "/service/post_me", 16000, false, false },
//                { Code.DELETE, 5683, "/service/delete_me", 16000, true, true },
                //16000 maxResourceBodySize on server
                { Code.GET, 5685, "/service/get_me", 10, true, false },
                { Code.PUT, 5685, "/service/put_me", 10, false, false },
                { Code.POST, 5685, "/service/post_me", 10, false, false },
                { Code.DELETE, 5685, "/service/delete_me", 10, true, false },
//                { Code.GET, 5685, "/service/get_me", 8192, true, false },
                { Code.PUT, 5685, "/service/put_me", 8192, false, false },
                { Code.POST, 5685, "/service/post_me", 8192, false, false },
//                { Code.DELETE, 5685, "/service/delete_me", 8192, true, false },
//                { Code.GET, 5685, "/service/get_me", 16000, true, false },
                { Code.PUT, 5685, "/service/put_me", 16000, false, false },
                { Code.POST, 5685, "/service/post_me", 16000, false, false },
//                { Code.DELETE, 5685, "/service/delete_me", 16000, true, false },
//                { Code.GET, 5685, "/service/get_me", 16001, true, true },
                { Code.PUT, 5685, "/service/put_me", 16001, false, false },
                { Code.POST, 5685, "/service/post_me", 16001, false, false },
//                { Code.DELETE, 5685, "/service/delete_me", 16001, true, true }, 
            } );
    }

    /**
     * Request code to test
     */
    @Parameter(0)
    public Code requestCode;

    /**
     * Test server port
     */
    @Parameter(1)
    public int port;

    /**
     * Test resource to call
     */
    @Parameter(2)
    public String resourcePath;

    /**
     * Test message content size
     */
    @Parameter(3)
    public int contentSize;

    /**
     * True when request is not supposed to have a payload, but does
     */
    @Parameter(4)
    public boolean unintendedPayload;

    /**
     * True when response should be 4.13 Request Entity Too Large
     */
    @Parameter(5)
    public boolean expectTooLarge;

    /**
     * MAX_RESOURCE_BODY_SIZE of the testclient.
     */
    public int maxResourceBodySize= 16001;

    /**
     * Test client. 
     */
    private CoapClient client= null;

    /**
     * Flag indicating the message processor that is spied on was activated 
     */
    private AtomicBoolean spyActivated= new AtomicBoolean();

    @Override
    protected String getConfigResources()
    {
        return "mule-config/blockwise/testserver1.xml";
    };

    @Override
    protected boolean haveToDisableInboundEndpoints()
    {
        return false;
    }

    @Override
    protected boolean haveToMockMuleConnectors()
    {
        return false;
    }

    @Before
    public void setUpClient() throws Exception
    {
        client= new CoapClient( new URI( "coap", null, "127.0.0.1", port, resourcePath, null, null ) );
        client.setTimeout( 2000L );
        //NetworkConfig config= NetworkConfig.createStandardWithoutFile();
        //config.setInt( NetworkConfig.Keys.MAX_RESOURCE_BODY_SIZE, maxResourceBodySize );
        //CoapEndpoint.Builder endpointBuilder= new CoapEndpoint.Builder();
        //endpointBuilder.setNetworkConfig( config );
        //client.setEndpoint( endpointBuilder.build() );
    }

    @After
    public void tearDownClient() throws Exception
    {
        if ( client != null ) client.shutdown();
    }

    protected void validateInboundResponse( Code call, CoapResponse response )
    {
        boolean activated= spyActivated.get();

        if ( !expectTooLarge )
        {
            assertNotNull( "no response on: " + call, response );
            assertTrue( "response indicates failure on : " + call + " response: " + response.getCode() + " msg: " + response.getResponseText(), response.isSuccess() );
            assertTrue( "spy was not activated on: " + call, activated );
        }
        else
        {
            assertNotNull( "no response on: " + call, response );
            assertEquals(
                "response is not REQUEST_ENTITY_TOO_LARGE : " + call + " response: " + response.getCode() + " msg: " + response.getResponseText(),
                ResponseCode.REQUEST_ENTITY_TOO_LARGE,
                response.getCode() );
        }
    }

    protected void validateOutboundResponse( CoapResponse response )
    {
        assertNotNull( "no response on: " + requestCode, response );
        assertTrue( "response indicates failure on : " + requestCode + " response: " + response.getCode() + " msg: " + response.getResponseText(), response.isSuccess() );
        assertTrue( "wrong payload in response on: " + requestCode, Data.validateContent( response.getPayload(), contentSize ) );
    }

    private void mockResponseMessage()
    {
        MuleMessage messageToBeReturned= muleMessageWithPayload( Data.getContent( contentSize ) );
        MessageProcessorMocker mocker= whenMessageProcessor( "set-payload" ).ofNamespace( "mule" );
        mocker.thenReturn( messageToBeReturned );
    }

    private void spyRequestMessage()
    {
        SpyProcess beforeSpy= new SpyProcess()
            {
                @Override
                public void spy( MuleEvent event ) throws MuleException
                {
                    Object payload= event.getMessage().getPayload();
                    assertEquals( "payload has wrong class", byte[].class, payload.getClass() );
                    assertTrue( "content invalid", Data.validateContent( (byte[]) payload, contentSize ) );
                    spyActivated.set( true );
                }
            };

        spyMessageProcessor( "set-payload" ).ofNamespace( "mule" ).before( beforeSpy );
    }

    @Test(timeout= 20000L)
    public void testInboundRequest() throws URISyntaxException
    {
        spyRequestMessage();

        spyActivated.set( false );
        client.useLateNegotiation();
        Request request= new Request( requestCode );
        //if ( unintendedPayload ) request.setUnintendedPayload();
        request.setPayload( Data.getContent( contentSize ) );

        CoapResponse response= client.advanced( request );

        validateInboundResponse( requestCode, response );
    }

    @Test(timeout= 20000L)
    public void testInboundRequestEarlyNegotiation() throws URISyntaxException
    {
        spyRequestMessage();

        spyActivated.set( false );
        client.useEarlyNegotiation( 32 );
        Request request= new Request( requestCode );
        //if ( unintendedPayload ) request.setUnintendedPayload();
        request.setPayload( Data.getContent( contentSize ) );

        CoapResponse response= client.advanced( request );

        validateInboundResponse( requestCode, response );
    }

    @Test(timeout= 20000L)
    public void testOutboundRequest() throws URISyntaxException
    {
        mockResponseMessage();

        Request request= new Request( requestCode );
        //if ( unintendedPayload ) request.setUnintendedPayload();
        request.setPayload( "nothing important" );

        CoapResponse response= client.advanced( request );

        validateOutboundResponse( response );
    }

    @Test(timeout= 20000L)
    public void testOutboundRequestEarlyNegotiation() throws URISyntaxException
    {
        mockResponseMessage();

        client.useEarlyNegotiation( 32 );

        Request request= new Request( requestCode );
        //if ( unintendedPayload ) request.setUnintendedPayload();
        request.setPayload( "nothing important" );

        CoapResponse response= client.advanced( request );

        validateOutboundResponse( response );
    }

}