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
import java.util.Collections;
import java.util.List;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.munit.common.mocking.Attribute;
import org.mule.munit.common.mocking.SpyProcess;
import org.mule.munit.runner.functional.FunctionalMunitSuite;
import org.mule.transport.NullPayload;


public class ObserveTest extends FunctionalMunitSuite
{
    static final long PAUZE= 100L;

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
        contents.clear();
        contents.add( "nothing_yet" );
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
        final List< MuleMessage > observations= Collections.synchronizedList( new ArrayList< MuleMessage >() );
        String spiedProcessor= "log-component";
        String spiedProcessorNamespace= "mule";
        Attribute spiedProcessorDocName= Attribute.attribute( "name" ).ofNamespace( "doc" ).withValue( "handler_permanent" );
        SpyProcess spy= new SpyProcess()
            {
                @Override
                public void spy( MuleEvent event ) throws MuleException
                {
                    MuleMessage response= event.getMessage();
                    observations.add( response );
                }
            };
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).before( spy );

        //let asynchronous work happen
        Thread.sleep( PAUZE );

        //verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 1 );

        for ( int i= 1; i < contents.size(); i++ )
        {
            Thread.sleep( PAUZE );
            MuleEvent event= testEvent( contents.get( i ) );
            MuleEvent result= runFlow( "do_put_permanent", event );
            MuleMessage response= result.getMessage();
            assertEquals( "put nr: " + i + " gave wrong response", ResponseCode.CHANGED.toString(), response.getInboundProperty( "coap.response.code" ) );
        }

        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).atLeast( contents.size() - 1 );

        synchronized ( observations )
        {
            assertEquals( "wrong count of observations", contents.size() - 1, observations.size() );

            int obsOffset= 0;
            for ( int i= 0; i < observations.size(); i++ )
            {
                MuleMessage response= observations.get( i );
                if ( i == 0 ) obsOffset= ( (Integer) response.getInboundProperty( "coap.opt.observe" ) ).intValue();
                assertNotEquals( "observation nr: " + i + " is empty", NullPayload.getInstance(), response.getPayload() );
                assertTrue( "observation nr: " + i + " indicates failure", (Boolean) response.getInboundProperty( "coap.response.success" ) );
                assertEquals( "observation nr: " + i + " has wrong content", contents.get( i + 1 ), response.getPayloadAsString() );
                assertEquals( "observation nr: " + i + " has wrong observe option", obsOffset + i, ( (Integer) response.getInboundProperty( "coap.opt.observe" ) ).intValue() );
            }
        }
    }

    /**
     * Test temporary observe 
     * @throws Exception should not happen in this test
     */
    @Test
    public void testTemporaryObserve() throws Exception
    {
        final List< MuleMessage > observations= Collections.synchronizedList( new ArrayList< MuleMessage >() );
        MuleEvent event;
        MuleEvent result;
        MuleMessage response;
        String spiedProcessor= "echo-component";
        String spiedProcessorNamespace= "mule";
        Attribute spiedProcessorDocName= Attribute.attribute( "name" ).ofNamespace( "doc" ).withValue( "handler_temporary" );
        SpyProcess spy= new SpyProcess()
            {
                @Override
                public void spy( MuleEvent event ) throws MuleException
                {
                    MuleMessage response= event.getMessage();
                    observations.add( response );
                }
            };
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).before( spy );

        //let asynchronous work happen
        Thread.sleep( PAUZE );

        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 0 );

        for ( int i= 1; i < contents.size(); i++ )
        {
            Thread.sleep( PAUZE );
            event= testEvent( contents.get( i ) );
            result= runFlow( "do_put_temporary", event );
            response= result.getMessage();
            assertEquals( "1st series put nr: " + i + " gave wrong response", ResponseCode.CHANGED.toString(), response.getInboundProperty( "coap.response.code" ) );
        }

        //let asynchronous work happen
        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 0 );

        event= testEvent( "nothing_important" );
        result= runFlow( "start_observe", event );
        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 1 );

        for ( int i= 1; i < contents.size(); i++ )
        {
            Thread.sleep( PAUZE );
            event= testEvent( contents.get( i ) );
            result= runFlow( "do_put_temporary", event );
            response= result.getMessage();
            assertEquals( "2st series put nr: " + i + " gave wrong response", ResponseCode.CHANGED.toString(), response.getInboundProperty( "coap.response.code" ) );
        }
        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( contents.size() );

        event= testEvent( "nothing_important" );
        result= runFlow( "stop_observe", event );
        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( contents.size() + 1 );

        for ( int i= 1; i < contents.size(); i++ )
        {
            Thread.sleep( PAUZE );
            event= testEvent( contents.get( i ) );
            result= runFlow( "do_put_temporary", event );
            response= result.getMessage();
            assertEquals( "3st series put nr: " + i + " gave wrong response", ResponseCode.CHANGED.toString(), response.getInboundProperty( "coap.response.code" ) );
        }
        Thread.sleep( PAUZE );
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( contents.size() + 1 );

        synchronized ( observations )
        {
            assertEquals( "wrong count of observations", contents.size() + 1, observations.size() );
            int obsOffset= 0;
            for ( int i= 1; i < contents.size(); i++ )
            {
                response= observations.get( i );
                if ( i == 1 ) obsOffset= ( (Integer) response.getInboundProperty( "coap.opt.observe" ) ).intValue() - 1;
                assertNotEquals( "observation nr: " + i + " is empty", NullPayload.getInstance(), response.getPayload() );
                assertTrue( "observation nr: " + i + " indicates failure", (Boolean) response.getInboundProperty( "coap.response.success" ) );
                assertEquals( "observation nr: " + i + " has wrong content", contents.get( i ), response.getPayloadAsString() );
                assertEquals( "observation nr: " + i + " has wrong observe option", obsOffset + i, ( (Integer) response.getInboundProperty( "coap.opt.observe" ) ).intValue() );
            }
        }
    }

    //TODO warn when not existing observe is stopped

    /**
     * Test observe notifications at max_age 
     * @throws Exception should not happen in this test
     */
    @Test
    public void testObserveNotifications() throws Exception
    {
        final List< MuleMessage > observations= Collections.synchronizedList( new ArrayList< MuleMessage >() );
        MuleEvent event;
        @SuppressWarnings("unused")
        MuleEvent result;
        String spiedProcessor= "echo-component";
        String spiedProcessorNamespace= "mule";
        Attribute spiedProcessorDocName= Attribute.attribute( "name" ).ofNamespace( "doc" ).withValue( "handler_maxage1" );
        SpyProcess spy= new SpyProcess()
            {
                @Override
                public void spy( MuleEvent event ) throws MuleException
                {
                    MuleMessage response= event.getMessage();
                    observations.add( response );
                }
            };
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).before( spy );

        //let asynchronous work happen
        Thread.sleep( PAUZE );

        //check clean start
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 0 );

        event= testEvent( "nothing_important" );
        result= runFlow( "start_maxage1", event );
        Thread.sleep( 5500 );
        // GET observe=0 response + notifications= 1+5
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 1 + 5 );

        event= testEvent( "nothing_important" );
        result= runFlow( "stop_maxage1", event );
        Thread.sleep( 5500 );
        //stop result is also handled -> 1+5+1 times
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 1 + 5 + 1 );
    }

    /**
     * Test observe re-registration after max_age 
     * @throws Exception should not happen in this test
     */
    //TODO cf bug, notificationReregistrationBackoff not implemented
    @Ignore
    @Test
    public void testObserveReregistration1() throws Exception
    {
        final List< MuleMessage > observations= Collections.synchronizedList( new ArrayList< MuleMessage >() );
        MuleEvent event;
        @SuppressWarnings("unused")
        MuleEvent result;
        String spiedProcessor= "echo-component";
        String spiedProcessorNamespace= "mule";
        Attribute spiedProcessorDocName= Attribute.attribute( "name" ).ofNamespace( "doc" ).withValue( "handler_maxage1_nonotify" );
        SpyProcess spy= new SpyProcess()
            {
                @Override
                public void spy( MuleEvent event ) throws MuleException
                {
                    MuleMessage response= event.getMessage();
                    observations.add( response );
                }
            };
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).before( spy );

        //let asynchronous work happen
        Thread.sleep( PAUZE );

        //check clean start
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 0 );

        event= testEvent( "nothing_important" );
        result= runFlow( "start_maxage1_nonotify", event );
        //five notifications expected, period= max_age + notificationReregistrationBackoff per notification, plus margin
        Thread.sleep( 5000 + 500 + 100 );
        // GET observe=0 response + notification= 1 + 5
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 1 + 5 );

        event= testEvent( "nothing_important" );
        result= runFlow( "stop_maxage1_nonotify", event );
        //wait some more
        Thread.sleep( 5000 + 500 + 100 );
        //stop result is also handled -> 1+5+1 times
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 1 + 5 + 1 );
    }

    /**
     * Test observe re-registration after max_age 
     * @throws Exception should not happen in this test
     */
    @Test
    public void testObserveReregistration4() throws Exception
    {
        final List< MuleMessage > observations= Collections.synchronizedList( new ArrayList< MuleMessage >() );
        MuleEvent event;
        @SuppressWarnings("unused")
        MuleEvent result;
        String spiedProcessor= "echo-component";
        String spiedProcessorNamespace= "mule";
        Attribute spiedProcessorDocName= Attribute.attribute( "name" ).ofNamespace( "doc" ).withValue( "handler_maxage4_nonotify" );
        SpyProcess spy= new SpyProcess()
            {
                @Override
                public void spy( MuleEvent event ) throws MuleException
                {
                    MuleMessage response= event.getMessage();
                    observations.add( response );
                }
            };
        spyMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).before( spy );

        //let asynchronous work happen
        Thread.sleep( PAUZE );

        //check clean start
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 0 );

        event= testEvent( "nothing_important" );
        result= runFlow( "start_maxage4_nonotify", event );
        //five notifications expected, period= max_age + notificationReregistrationBackoff per notification, plus margin
        Thread.sleep( 5 * ( 4 + 2 ) * 1000 + 500 );
        // GET observe=0 response + notification= 1 + 5
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 1 + 5 );

        event= testEvent( "nothing_important" );
        result= runFlow( "stop_maxage4_nonotify", event );
        //wait some more
        Thread.sleep( 5 * ( 4 + 2 ) * 1000 + 500 );
        //stop result is also handled -> 1+5+1 times
        verifyCallOfMessageProcessor( spiedProcessor ).ofNamespace( spiedProcessorNamespace ).withAttributes( spiedProcessorDocName ).times( 1 + 5 + 1 );
    }

    /**
     * Test list observe relations
     * @throws Exception should not happen in this test
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testListObserve() throws Exception
    {
        MuleEvent event;
        MuleEvent result;
        MuleMessage response;

        //let asynchronous work happen
        Thread.sleep( PAUZE );

        //get observe list
        event= testEvent( "nothing_important" );
        result= runFlow( "observation_list", event );
        response= result.getMessage();
        assertEquals( "wrong number of observations", 0, ( (List< String >) response.getPayload() ).size() );

        //first observe
        event= testEvent( "nothing_important" );
        result= runFlow( "start_observe1", event );

        //get observe list
        Thread.sleep( PAUZE );
        event= testEvent( "nothing_important" );
        result= runFlow( "observation_list", event );
        response= result.getMessage();
        assertEquals( "wrong number of observations", 1, ( (List< String >) response.getPayload() ).size() );
        assertEquals( "wrong observation uri", "coap://127.0.0.1:5683/observe/temporary1", ( (List< String >) response.getPayload() ).get( 0 ) );

        //second observe
        event= testEvent( "nothing_important" );
        result= runFlow( "start_observe2", event );

        //get observe list
        Thread.sleep( PAUZE );
        event= testEvent( "nothing_important" );
        result= runFlow( "observation_list", event );
        response= result.getMessage();
        assertEquals( "wrong number of observations", 2, ( (List< String >) response.getPayload() ).size() );
        assertEquals( "wrong observation uri", "coap://127.0.0.1:5683/observe/temporary1", ( (List< String >) response.getPayload() ).get( 0 ) );
        assertEquals( "wrong observation uri", "coap://127.0.0.1:5683/observe/temporary2", ( (List< String >) response.getPayload() ).get( 1 ) );

        //remove second observe
        event= testEvent( "nothing_important" );
        result= runFlow( "stop_observe2", event );

        //get observe list
        Thread.sleep( PAUZE );
        event= testEvent( "nothing_important" );
        result= runFlow( "observation_list", event );
        response= result.getMessage();
        assertEquals( "wrong number of observations", 1, ( (List< String >) response.getPayload() ).size() );
        assertEquals( "wrong observation uri", "coap://127.0.0.1:5683/observe/temporary1", ( (List< String >) response.getPayload() ).get( 0 ) );

        //remove first observe
        event= testEvent( "nothing_important" );
        result= runFlow( "stop_observe1", event );

        //get observe list
        Thread.sleep( PAUZE );
        event= testEvent( "nothing_important" );
        result= runFlow( "observation_list", event );
        response= result.getMessage();
        assertEquals( "wrong number of observations", 0, ( (List< String >) response.getPayload() ).size() );

    }
}