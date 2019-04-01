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
package nl.teslanet.mule.transport.coap.client.test.exceptionhandling;


import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.internal.matchers.ThrowableCauseMatcher.hasCause;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.munit.common.mocking.Attribute;
import org.mule.munit.common.mocking.SpyProcess;
import org.mule.munit.runner.functional.FunctionalMunitSuite;

import nl.teslanet.mule.transport.coap.client.error.HandlerException;


@RunWith(Parameterized.class)
public class ExceptionHandlingTest extends FunctionalMunitSuite
{
    /**
     * The list of tests with their parameters
     * @return Test parameters.
     */
    @Parameters(name= "flowName= {0}, host= {1}, port= {2}, path= {3}, expectedResponseCode= {4}, expectedPayload= {5}")
    public static Collection< Object[] > data()
    {
        return Arrays.asList(
            new Object [] []{
                { "do_get", Code.GET, "127.0.0.1", "8976", "/service/get_me", "2.05", "coap://127.0.0.1:8976/service/get_me", "Response is: CONTENT".getBytes() },
                { "do_post", Code.POST, "127.0.0.1", "8976", "/service/post_me", "2.01", "coap://127.0.0.1:8976/service/post_me", "Response is: CREATED".getBytes() },
                { "do_put", Code.PUT, "127.0.0.1", "8976", "/service/put_me", "2.04", "coap://127.0.0.1:8976/service/put_me", "Response is: CHANGED".getBytes() },
                { "do_delete", Code.DELETE, "127.0.0.1", "8976", "/service/delete_me", "2.02", "coap://127.0.0.1:8976/service/delete_me", "Response is: DELETED".getBytes() }});
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
     * Exception rule
     */
    @Rule
    public ExpectedException exception= ExpectedException.none();

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
        return "mule-config/exceptionhandling/testclient1.xml";
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
        server= new ExceptionHandlingTestServer( 8976 );
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
     * Test catching exception in handler
     * @throws Exception should not happen in this test
     */
    @Test
    public void testCatchedException() throws Exception
    {
        String spiedProcessor= "echo-component";
        String spiedProcessorNamespace= "mule";
        Attribute spiedProcessorDocName1= Attribute.attribute( "name" ).ofNamespace( "doc" ).withValue( "spy-me1" );
        Attribute spiedProcessorDocName2= Attribute.attribute( "name" ).ofNamespace( "doc" ).withValue( "spy-me2" );
        final AtomicBoolean spy1IsCalled= new AtomicBoolean( false );
        final AtomicBoolean spy2IsCalled= new AtomicBoolean( false );
        SpyProcess spy1= new SpyProcess()
        {
            @Override
            public void spy( MuleEvent event ) throws MuleException
            {
                MuleMessage response= event.getMessage();
                assertEquals( "wrong request code", expectedRequestCode, response.getInboundProperty( "coap.request.code" ) );
                assertEquals( "wrong request uri", expectedRequestUri, response.getInboundProperty( "coap.request.uri" ) );
                assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
                assertArrayEquals( "wrong response payload", expectedPayload, (byte[]) response.getPayload() );
                spy1IsCalled.set( true );
            }
        };
        SpyProcess spy2= new SpyProcess()
        {
            @Override
            public void spy( MuleEvent event ) throws MuleException
            {
                MuleMessage response= event.getMessage();
                assertEquals( "wrong request code", expectedRequestCode, response.getInboundProperty( "coap.request.code" ) );
                assertEquals( "wrong request uri", expectedRequestUri, response.getInboundProperty( "coap.request.uri" ) );
                assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
                assertArrayEquals( "wrong response payload", expectedPayload, (byte[]) response.getPayload() );
                spy2IsCalled.set( true );
            }
        };
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName1 ).before( spy1 );
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName2 ).before( spy2 );

        MuleEvent event= testEvent( "nothing_important" );
        event.setFlowVariable( "host", host );
        event.setFlowVariable( "port", port );
        event.setFlowVariable( "path", path );
        event.setFlowVariable( "handler", "catching_handler" );
        MuleEvent result= runFlow( flowName, event );

        //let handler do its asynchronous work
        Thread.sleep( 100L );

        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName1 ).times( 1 );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName2 ).times( 1 );
        MuleMessage response= result.getMessage();
        assertTrue( "spy 1 has not been called", spy1IsCalled.get() );
        assertTrue( "spy 2 has not been called", spy2IsCalled.get() );
        assertEquals( "wrong response code", null, response.getInboundProperty( "coap.response.code" ) );
        assertEquals( "wrong response payload", "nothing_important", response.getPayload() );
    }

    /**
     * Test uncatched exception in handler
     * @throws Exception should not happen in this test
     */
    @Test
    public void testUnCatchedException() throws Exception
    {
        String spiedProcessor= "echo-component";
        String spiedProcessorNamespace= "mule";
        Attribute spiedProcessorDocName3= Attribute.attribute( "name" ).ofNamespace( "doc" ).withValue( "spy-me3" );
        Attribute spiedProcessorDocName2= Attribute.attribute( "name" ).ofNamespace( "doc" ).withValue( "spy-me2" );
        final AtomicBoolean spy3IsCalled= new AtomicBoolean( false );
        final AtomicBoolean spy2IsCalled= new AtomicBoolean( false );
        SpyProcess spy3= new SpyProcess()
        {
            @Override
            public void spy( MuleEvent event ) throws MuleException
            {
                MuleMessage response= event.getMessage();
                assertEquals( "wrong request code", expectedRequestCode, response.getInboundProperty( "coap.request.code" ) );
                assertEquals( "wrong request uri", expectedRequestUri, response.getInboundProperty( "coap.request.uri" ) );
                assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
                assertArrayEquals( "wrong response payload", expectedPayload, (byte[]) response.getPayload() );
                spy3IsCalled.set( true );
            }
        };
        SpyProcess spy2= new SpyProcess()
        {
            @Override
            public void spy( MuleEvent event ) throws MuleException
            {
                MuleMessage response= event.getMessage();
                assertEquals( "wrong request code", expectedRequestCode, response.getInboundProperty( "coap.request.code" ) );
                assertEquals( "wrong request uri", expectedRequestUri, response.getInboundProperty( "coap.request.uri" ) );
                assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
                assertArrayEquals( "wrong response payload", expectedPayload, (byte[]) response.getPayload() );
                spy2IsCalled.set( true );
            }
        };
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName3 ).before( spy3 );
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName2 ).before( spy2 );

        MuleEvent event= testEvent( "nothing_important" );
        event.setFlowVariable( "host", host );
        event.setFlowVariable( "port", port );
        event.setFlowVariable( "path", path );
        event.setFlowVariable( "handler", "failing_handler" );
        MuleEvent result= runFlow( flowName, event );

        //let handler do its asynchronous work
        Thread.sleep( 100L );

        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName3 ).times( 1 );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName2 ).times( 0 );
        MuleMessage response= result.getMessage();
        assertTrue( "spy 3 has not been called", spy3IsCalled.get() );
        assertFalse( "spy 2 has wrongfully been called", spy2IsCalled.get() );
        assertEquals( "wrong response code", null, response.getInboundProperty( "coap.response.code" ) );
        assertEquals( "wrong response payload", "nothing_important", response.getPayload() );
    }

    /**
     * Test uncatched exception in handler
     * @throws Exception should not happen in this test
     */
    @Test
    public void testNonExistingHandler() throws Exception
    {
        exception.expect( isA( MessagingException.class ) );
        exception.expect( hasMessage( containsString( "Failed to invoke" ) ) );
        exception.expect( hasCause( isA( HandlerException.class ) ) );
        exception.expect( hasCause( hasMessage( containsString( "referenced handler { nonexisting_handler } not found" ) ) ) );


        MuleEvent event= testEvent( "nothing_important" );
        event.setFlowVariable( "host", host );
        event.setFlowVariable( "port", port );
        event.setFlowVariable( "path", path );
        event.setFlowVariable( "handler", "nonexisting_handler" );
        MuleEvent result= runFlow( flowName, event );

        //let handler do its asynchronous work
        Thread.sleep( 100L );

        MuleMessage response= result.getMessage();
        assertEquals( "wrong response code", null, response.getInboundProperty( "coap.response.code" ) );
        assertEquals( "wrong response payload", "nothing_important", response.getPayload() );
    }

}