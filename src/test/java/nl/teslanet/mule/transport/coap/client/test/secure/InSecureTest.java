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
package nl.teslanet.mule.transport.coap.client.test.secure;


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
import org.mule.transport.NullPayload;

//TODO bug Code should be string

@RunWith(Parameterized.class)
public class InSecureTest extends FunctionalMunitSuite
{
    /**
     * The list of tests with their parameters
     * @return Test parameters.
     */
    @Parameters(name= "flowName= {0}" )
    public static Collection< Object[] > data()
    {
        return Arrays.asList(
            new Object [] []{
                { "get_me", Code.GET, "coap://127.0.0.1:5684/secure/get_me", null, NullPayload.getInstance() },
                { "do_not_get_me", Code.GET, "coap://127.0.0.1:5684/secure/do_not_get_me", null, NullPayload.getInstance()  },
                { "post_me", Code.POST, "coap://127.0.0.1:5684/secure/post_me", null, NullPayload.getInstance()  },
                { "do_not_post_me", Code.POST, "coap://127.0.0.1:5684/secure/do_not_post_me", null,  NullPayload.getInstance() },
                { "put_me", Code.PUT, "coap://127.0.0.1:5684/secure/put_me", null, NullPayload.getInstance() },
                { "do_not_put_me", Code.PUT, "coap://127.0.0.1:5684/secure/do_not_put_me", null,  NullPayload.getInstance()  },
                { "delete_me", Code.DELETE, "coap://127.0.0.1:5684/secure/delete_me", null, NullPayload.getInstance() },
                { "do_not_delete_me", Code.DELETE, "coap://127.0.0.1:5684/secure/do_not_delete_me", null,  NullPayload.getInstance()  } } );
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
     * The request uri that is expected.
     */
    @Parameter(2)
    public String expectedRequestUri;

    /**
     * The response code that is expected.
     */
    @Parameter(3)
    public String expectedResponseCode;

    /**
     * The payload code that is expected.
     */
    @Parameter(4)
    public Object expectedPayload;

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
        return "mule-config/secure/testclient2.xml";
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
        server= new SecureTestServer();
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
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        assertEquals( "wrong request code", expectedRequestCode, response.getInboundProperty( "coap.request.code" ) );
        assertEquals( "wrong request uri", expectedRequestUri, response.getInboundProperty( "coap.request.uri" ) );
        assertEquals( "wrong response success flag", false, response.getInboundProperty( "coap.response.success" ) );
        assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
        assertEquals( "wrong response payload", expectedPayload, response.getPayload() );
    }

}