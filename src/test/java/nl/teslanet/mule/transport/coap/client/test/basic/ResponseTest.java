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


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
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


@RunWith(Parameterized.class)
public class ResponseTest extends FunctionalMunitSuite
{
    /**
     * The list of tests with their parameters
     * @return Test parameters.
     */
    @Parameters(name= "flowName= {0}, resourcePath= {1}, expectedResponseCode= {2}, expectedPayload= {3}")
    public static Collection< Object[] > getTests()
    {
        ArrayList< Object[] > tests= new ArrayList< Object[] >();

        for ( ResponseCode code : ResponseCode.values() )
        {
            tests.add( new Object []{ "do_get", "/response/" + code.name(), code, "Response is: " + code.name() } );
            tests.add( new Object []{ "do_post", "/response/" + code.name(), code, "Response is: " + code.name() } );
            tests.add( new Object []{ "do_put", "/response/" + code.name(), code, "Response is: " + code.name() } );
            tests.add( new Object []{ "do_delete", "/response/" + code.name(), code, "Response is: " + code.name() } );
        }
        return tests;
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
     * The response code that is expected.
     */
    @Parameter(2)
    public ResponseCode expectedResponseCode;

    /**
     * The response payload that is expected.
     */
    @Parameter(3)
    public String expectedResponsePayload;

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
        server= new ResponseTestServer();
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
    public void testResponse() throws Exception
    {
        MuleEvent event= testEvent( "nothing_important" );
        event.setFlowVariable( "host", "127.0.0.1" );
        event.setFlowVariable( "port", "5683" );
        event.setFlowVariable( "path", resourcePath );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        assertEquals( "wrong response code", expectedResponseCode.toString(), response.getInboundProperty( "coap.response.code" ) );
        assertEquals( "wrong response payload", expectedResponsePayload, response.getPayloadAsString() );
        assertEquals( "wrong success flag", ResponseCode.isSuccess( expectedResponseCode ), response.getInboundProperty( "coap.response.success" ) );
        //TODO test for property clienterror, servererror
    }

}