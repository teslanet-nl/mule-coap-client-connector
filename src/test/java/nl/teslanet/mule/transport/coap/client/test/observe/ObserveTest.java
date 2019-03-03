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
package nl.teslanet.mule.transport.coap.client.test.observe;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.munit.common.mocking.SpyProcess;
import org.mule.munit.runner.functional.FunctionalMunitSuite;
import org.mule.transport.NullPayload;


public class ObserveTest extends FunctionalMunitSuite
{
    static final long PAUZE= 100L;

    /**
     * The notifications from observed resource
     */
    private CopyOnWriteArrayList< MuleMessage > observations= new CopyOnWriteArrayList< MuleMessage >();

    /**
     * The contents to set on observable resource
     */
    private ArrayList< String > contents= new ArrayList< String >();

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
        return "mule-config/observe/testclient1.xml";
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
        server= new ObserveTestServer();
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
     * Clean start
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception
    {
        observations.clear();
        contents.clear();
        contents.add( "nothing" );
        contents.add( "first" );
        contents.add( "second" );
        contents.add( "third" );
        contents.add( "fourth" );
        contents.add( "fifth" );
    }

    /**
     * Test permanent observe 
     * @throws Exception should not happen in this test
     */
    @Test
    public void testPermanentObserve() throws Exception
    {
        String spiedProcessor= "log-component";
        String spiedProcessorNamespace= "mule";
        SpyProcess spy= new SpyProcess()
            {
                @Override
                public void spy( MuleEvent event ) throws MuleException
                {
                    MuleMessage response= event.getMessage();
                    observations.add( response );
                }
            };
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).before( spy );

        //let asynchronous work happen
        Thread.sleep( PAUZE );

        //verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).times( 1 );

        for ( int i= 1; i < contents.size(); i++ )
        {
            Thread.sleep( PAUZE );
            MuleEvent event= testEvent( contents.get( i ) );
            MuleEvent result= runFlow( "do_put_permanent", event );
            MuleMessage response= result.getMessage();
            assertEquals( "put nr: " + i + " gave wrong response", ResponseCode.CHANGED.toString(), response.getInboundProperty( "coap.response.code" ) );
        }

        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).atLeast( contents.size() - 1 );

        assertEquals( "wrong count of observations", contents.size(), observations.size() );

        for ( int i= 0; i < observations.size(); i++ )
        {
            MuleMessage response= observations.get( i );
            assertNotEquals( "observation nr: " + i + " is empty", NullPayload.getInstance(), response.getPayload() );
            assertTrue( "observation nr: " + i + " indicates failure", (Boolean) response.getInboundProperty( "coap.response.success" ) );
            assertEquals( "observation nr: " + i + " has wrong content", contents.get( i ), response.getPayloadAsString() );
        }
    }

    /**
     * Test permanent observe 
     * @throws Exception should not happen in this test
     */
    @Test
    public void testTemporaryObserve() throws Exception
    {
        MuleEvent event;
        MuleEvent result;
        MuleMessage response;
        String spiedProcessor= "echo-component";
        String spiedProcessorNamespace= "mule";
        SpyProcess spy= new SpyProcess()
            {
                @Override
                public void spy( MuleEvent event ) throws MuleException
                {
                    MuleMessage response= event.getMessage();
                    observations.add( response );
                }
            };
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).before( spy );

        //let asynchronous work happen
        Thread.sleep( PAUZE );

        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).times( 0 );

        for ( int i= 1; i < contents.size(); i++ )
        {
            Thread.sleep( PAUZE );
            event= testEvent( contents.get( i ) );
            result= runFlow( "do_put_temporary", event );
            response= result.getMessage();
            assertEquals( "1st series put nr: " + i + " gave wrong response", ResponseCode.CHANGED.toString(), response.getInboundProperty( "coap.response.code" ) );
        }

        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).times( 0 );

        event= testEvent( "nothing_important" );
        result= runFlow( "start_observe", event );
        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).times( 1 );

        for ( int i= 1; i < contents.size(); i++ )
        {
            Thread.sleep( PAUZE );
            event= testEvent( contents.get( i ) );
            result= runFlow( "do_put_temporary", event );
            response= result.getMessage();
            assertEquals( "2st series put nr: " + i + " gave wrong response", ResponseCode.CHANGED.toString(), response.getInboundProperty( "coap.response.code" ) );
        }
        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).times( contents.size() );

        event= testEvent( "nothing_important" );
        result= runFlow( "stop_observe", event );
        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).times( contents.size() + 1 );

        for ( int i= 1; i < contents.size(); i++ )
        {
            Thread.sleep( PAUZE );
            event= testEvent( contents.get( i ) );
            result= runFlow( "do_put_temporary", event );
            response= result.getMessage();
            assertEquals( "3st series put nr: " + i + " gave wrong response", ResponseCode.CHANGED.toString(), response.getInboundProperty( "coap.response.code" ) );
        }
        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).times( contents.size() + 1 );

        assertEquals( "wrong count of observations", contents.size() + 1, observations.size() );

        for ( int i= 1; i < contents.size(); i++ )
        {
            response= observations.get( i );
            assertNotEquals( "observation nr: " + i + " is empty", NullPayload.getInstance(), response.getPayload() );
            assertTrue( "observation nr: " + i + " indicates failure", (Boolean) response.getInboundProperty( "coap.response.success" ) );
            assertEquals( "observation nr: " + i + " has wrong content", contents.get( i ), response.getPayloadAsString() );
        }
    }

}