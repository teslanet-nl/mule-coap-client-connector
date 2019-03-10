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
package nl.teslanet.mule.transport.coap.client.test.basic;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.munit.runner.functional.FunctionalMunitSuite;

//TODO bug Code should be String

@RunWith(Parameterized.class)
public class DynamicUriBasicTest extends FunctionalMunitSuite
{
    /**
     * The list of tests with their parameters
     * @return Test parameters.
     */
    @Parameters(name= "path= {4}")
    public static Collection< Object[] > data()
    {
        return Arrays.asList(
            new Object [] []{
                { "do_get", Code.GET, "127.0.0.1", "8976", "/basic/get_me", "2.05", "coap://127.0.0.1:8976/basic/get_me", "GET called on: /basic/get_me".getBytes() },
                { "do_get", Code.GET, "127.0.0.1", "8976", "/basic/do_not_get_me", "4.05", "coap://127.0.0.1:8976/basic/do_not_get_me", "".getBytes() },
                { "do_post", Code.POST, "127.0.0.1", "8976", "/basic/post_me", "2.01", "coap://127.0.0.1:8976/basic/post_me", "POST called on: /basic/post_me".getBytes() },
                { "do_post", Code.POST, "127.0.0.1", "8976", "/basic/do_not_post_me", "4.05", "coap://127.0.0.1:8976/basic/do_not_post_me", "".getBytes() },
                { "do_put", Code.PUT, "127.0.0.1", "8976", "/basic/put_me", "2.04", "coap://127.0.0.1:8976/basic/put_me", "PUT called on: /basic/put_me".getBytes() },
                { "do_put", Code.PUT, "127.0.0.1", "8976", "/basic/do_not_put_me", "4.05", "coap://127.0.0.1:8976/basic/do_not_put_me", "".getBytes() },
                { "do_delete", Code.DELETE, "127.0.0.1", "8976", "/basic/delete_me", "2.02", "coap://127.0.0.1:8976/basic/delete_me", "DELETE called on: /basic/delete_me".getBytes() },
                { "do_delete", Code.DELETE, "127.0.0.1", "8976", "/basic/do_not_delete_me", "4.05", "coap://127.0.0.1:8976/basic/do_not_delete_me", "".getBytes() } } );
    }

    /**
     * The mule flow to call.
     */
    @Parameter(0)
    public String flowName;

    /**
     * The request code that is expected.
     */
    @Parameter(1)
    public Code expectedRequestCode;

    /**
     * The server host to call.
     */
    @Parameter(2)
    public String host;

    /**
     * The server port to call.
     */
    @Parameter(3)
    public String port;

    /**
     * The server path to call.
     */
    @Parameter(4)
    public String path;

    /**
     * The response code that is expected.
     */
    @Parameter(5)
    public String expectedResponseCode;

    /**
     * The request uri that is expected.
     */
    @Parameter(6)
    public String expectedRequestUri;

   /**
     * The payload code that is expected.
     */
    @Parameter(7)
    public byte[] expectedPayload;

    /**
     * Server to test against
     */
    private static CoapServer server= null;

    /* (non-Javadoc)
     * @see org.mule.munit.runner.functional.FunctionalMunitSuite#getConfigResources()
     */
    @Override
    protected String getConfigResources()
    {
        return "mule-config/basic/testclient3.xml";
    };

    /* (non-Javadoc)
     * @see org.mule.munit.runner.functional.FunctionalMunitSuite#haveToDisableInboundEndpoints()
     */
    @Override
    protected boolean haveToDisableInboundEndpoints()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.mule.munit.runner.functional.FunctionalMunitSuite#haveToMockMuleConnectors()
     */
    @Override
    protected boolean haveToMockMuleConnectors()
    {
        return false;
    }

    /**
     * Start the server
     * @throws Exception when server cannot start
     */
    @BeforeClass
    public static void setUpServer() throws Exception
    {
        server= new BasicTestServer( 8976 );
        server.start();
    }

    /**
     * Stop the server
     * @throws Exception when the server cannot stop
     */
    @AfterClass
    public static void tearDownServer() throws Exception
    {
        if ( server != null )
        {
            server.stop();
            server.destroy();
            server= null;
        }
    }

    /**
     * Test CoAP request
     * @throws Exception should not happen in this test
     */
    @Test
    public void testRequest() throws Exception
    {
        MuleEvent event= testEvent( "nothing_important" );
        event.setFlowVariable( "host", host );
        event.setFlowVariable( "port", port );
        event.setFlowVariable( "path", path );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        assertEquals( "wrong request code", expectedRequestCode, response.getInboundProperty( "coap.request.code" ) );
        assertEquals( "wrong request uri", expectedRequestUri, response.getInboundProperty( "coap.request.uri" ) );
        assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
        assertArrayEquals( "wrong response payload", expectedPayload, (byte[]) response.getPayload() );
    }

}