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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.californium.core.CoapServer;
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

import nl.teslanet.mule.transport.coap.client.test.utils.Data;


@RunWith(Parameterized.class)
public class PayloadTest extends FunctionalMunitSuite
{
    /**
     * The list of tests with their parameters
     * @return Test parameters.
     */
    @Parameters(name= "flowName= {0}, resourcePath= {1}, requestPayloadSize= {2}, expectedResponseCode= {3}, expectedResponsePayloadSize= {4}")
    public static Collection< Object[] > data()
    {
        //TODO cf bug GET and DELETE request not blockwise
        return Arrays.asList(
            new Object [] []{
                { "do_get", "/blockwise/rq0", 0, "2.05", 2 },
                { "do_get", "/blockwise/rsp0", 2, "2.05", 0 },
                { "do_post", "/blockwise/rq0", 0, "2.01", 2 },
                { "do_post", "/blockwise/rsp0", 2, "2.01", 0 },
                { "do_put", "/blockwise/rq0", 0, "2.04", 2 },
                { "do_put", "/blockwise/rsp0", 2, "2.04", 0 },
                { "do_delete", "/blockwise/rq0", 0, "2.02", 2 },
                { "do_delete", "/blockwise/rsp0", 2, "2.02", 0 },

                { "do_get", "/blockwise/rq10", 10, "2.05", 2 },
                { "do_get", "/blockwise/rsp10", 2, "2.05", 10 },
                { "do_post", "/blockwise/rq10", 10, "2.01", 2 },
                { "do_post", "/blockwise/rsp10", 2, "2.01", 10 },
                { "do_put", "/blockwise/rq10", 10, "2.04", 2 },
                { "do_put", "/blockwise/rsp10", 2, "2.04", 10 },
                { "do_delete", "/blockwise/rq10", 10, "2.02", 2 },
                { "do_delete", "/blockwise/rsp10", 2, "2.02", 10 },
                
                //{ "do_get", "/blockwise/rq8192", 8192, "2.05", 2 },
                { "do_get", "/blockwise/rsp8192", 2, "2.05", 8192 },
                { "do_post", "/blockwise/rq8192", 8192, "2.01", 2 },
                { "do_post", "/blockwise/rsp8192", 2, "2.01", 8192 },
                { "do_put", "/blockwise/rq8192", 8192, "2.04", 2 },
                { "do_put", "/blockwise/rsp8192", 2, "2.04", 8192 },
                //{ "do_delete", "/blockwise/rq8192", 8192, "2.02", 2 },
                { "do_delete", "/blockwise/rsp8192", 2, "2.02", 8192 },
                
                //{ "do_get", "/blockwise/rq16000", 16000, "2.05", 2 },
                { "do_get", "/blockwise/rsp16000", 2, "2.05", 16000 },
                { "do_post", "/blockwise/rq16000", 16000, "2.01", 2 },
                { "do_post", "/blockwise/rsp16000", 2, "2.01", 16000 },
                { "do_put", "/blockwise/rq16000", 16000, "2.04", 2 },
                { "do_put", "/blockwise/rsp16000", 2, "2.04", 16000 },
                //{ "do_delete", "/blockwise/rq16000", 16000, "2.02", 2 },
                { "do_delete", "/blockwise/rsp16000", 2, "2.02", 16000 },
                
                //{ "do_get", "/blockwise/rq16001", 16001, "2.05", 2 },
                { "do_get", "/blockwise/rsp16001", 2, "2.05", 16001 },
                { "do_post", "/blockwise/rq16001", 16001, "2.01", 2 },
                { "do_post", "/blockwise/rsp16001", 2, "2.01", 16001 },
                { "do_put", "/blockwise/rq16001", 16001, "2.04", 2 },
                { "do_put", "/blockwise/rsp16001", 2, "2.04", 16001 },
                //{ "do_delete", "/blockwise/rq16001", 16001, "2.02", 2 },
                { "do_delete", "/blockwise/rsp16001", 2, "2.02", 16001 } 
        } );
}

    /**
     * The mule flow to call.
     */
    @Parameter(0)
    public String flowName;

    /**
     * The path of the resource to call.
     */
    @Parameter(1)
    public String resourcePath;

    /**
     * The request payload size to test.
     */
    @Parameter(2)
    public Integer requestPayloadSize;

    /**
     * The response code that is expected.
     */
    @Parameter(3)
    public String expectedResponseCode;

    /**
     * The response payload size to test.
     */
    @Parameter(4)
    public Integer expectedResponsePayloadSize;

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
        return "mule-config/blockwise/testclient1.xml";
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
        server= new TestServer();
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
    public void testPayload() throws Exception
    {
        MuleEvent event= testEvent( Data.getContent( requestPayloadSize ) );
        event.setFlowVariable( "path", resourcePath );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
        assertTrue( "wrong response payload", Data.validateContent( (byte[])response.getPayload(), expectedResponsePayloadSize ) );
    }

}