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
package nl.teslanet.mule.transport.coap.client.test.discovery;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.CoAP;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.munit.runner.functional.FunctionalMunitSuite;


public class DiscoveryTest extends FunctionalMunitSuite
{
    /**
     * Exception to expect
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
        return "mule-config/discovery/testclient1.xml";
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

    private HashMap< String, WebLink > linkMap( Set< WebLink > set )
    {
        HashMap< String, WebLink > map= new HashMap< String, WebLink >();

        for ( WebLink link : set )
        {
            map.put( link.getURI(), link );
        }
        return map;
    }

    /**
     * Test ping
     * @throws Exception should not happen in this test
     */
    @Test
    public void testPing() throws Exception
    {
        String flowName= "ping_ok";
        Boolean expectedPayload= Boolean.TRUE;

        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        //assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
        assertEquals( "wrong response payload", expectedPayload, (Boolean) response.getPayload() );
    }

    /**
     * Test ping on server not listening
     * @throws Exception should not happen in this test
     */
    @Test
    public void testPingNOK() throws Exception
    {
        String flowName= "ping_nok";
        Boolean expectedPayload= Boolean.FALSE;

        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        //assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
        assertEquals( "wrong response payload", expectedPayload, (Boolean) response.getPayload() );
    }

    /**
     * Test ping on server does not exist
     * @throws Exception should not happen in this test
     */
    @Test
    public void testPingNoResolvableHost() throws Exception
    {
        String flowName= "ping_notresolvable";
        Boolean expectedPayload= Boolean.FALSE;
        exception.expect( MessagingException.class );
        exception.expectMessage( "Failed to invoke ping" );
        //exception.expect( hasCause( isA( MalformedUriException.class ) ) );

        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        //assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
        assertEquals( "wrong response payload", expectedPayload, (Boolean) response.getPayload() );
    }

    /**
     * Test Ping on dynamicly set host and port
     * @throws Exception should not happen in this test
     */
    @Test
    public void testDynamicUriPing() throws Exception
    {
        String flowName= "ping_dynamic";
        String host= "127.0.0.1";
        String port= Integer.toString( CoAP.DEFAULT_COAP_PORT );
        Boolean expectedPayload= Boolean.TRUE;

        MuleEvent event= testEvent( "nothing_important" );
        event.setFlowVariable( "host", host );
        event.setFlowVariable( "port", port );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        //assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
        assertEquals( "wrong response payload", expectedPayload, (Boolean) response.getPayload() );
    }

    /**
     * Test Ping on dynamicly set host and port of not listening server
     * @throws Exception should not happen in this test
     */
    @Test
    public void testDynamicUriPingNOK() throws Exception
    {
        String flowName= "ping_dynamic";
        String host= "127.0.0.1";
        String port= Integer.toString( 6767 );
        Boolean expectedPayload= Boolean.FALSE;

        MuleEvent event= testEvent( "nothing_important" );
        event.setFlowVariable( "host", host );
        event.setFlowVariable( "port", port );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        //assertEquals( "wrong response code", expectedResponseCode, response.getInboundProperty( "coap.response.code" ) );
        assertEquals( "wrong response payload", expectedPayload, (Boolean) response.getPayload() );
    }

    /**
     * Test Ping on dynamicly set host and port of not listening server
     * @throws Exception should not happen in this test
     */
    @Test
    public void testDynamicUriPingNoResolvableHost() throws Exception
    {
        String flowName= "ping_dynamic";
        String host= "dit_bestaat_niet.org";
        String port= Integer.toString( CoAP.DEFAULT_COAP_PORT );
        Boolean expectedPayload= Boolean.FALSE;
        exception.expect( MessagingException.class );
        exception.expectMessage( "Failed to invoke ping" );
        //exception.expect( hasCause( isA( MalformedUriException.class ) ) );

        MuleEvent event= testEvent( "nothing_important" );
        event.setFlowVariable( "host", host );
        event.setFlowVariable( "port", port );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        assertEquals( "wrong response payload", expectedPayload, (Boolean) response.getPayload() );
    }

    @Test
    public void testWellKnownCore() throws Exception
    {
        String flowName= "discover";
        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();

        @SuppressWarnings("unchecked")
        HashMap< String, WebLink > links= linkMap( (Set< WebLink >) response.getPayload() );

        assertEquals( "wrong number of weblinks", 8, links.size() );
        WebLink link= links.get( "/.well-known/core" );
        assertNotNull( "/.well-known/core is missing", link );
    }

    @Test
    public void testCt() throws Exception
    {
        String flowName= "discover";
        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();

        @SuppressWarnings("unchecked")
        HashMap< String, WebLink > links= linkMap( (Set< WebLink >) response.getPayload() );
        WebLink link= links.get( "/service/resource_with_ct" );

        assertNotNull( "/service/resource_with_ct is missing", link );
        List< String > ct= link.getAttributes().getContentTypes();
        assertEquals( "wrong number ct", 2, ct.size() );
        assertTrue( "ct does not contain 0", ct.contains( "0" ) );
        assertTrue( "ct does not contain 41", ct.contains( "41" ) );

        //check other attributes are not there
        List< String > ifdesc= link.getAttributes().getInterfaceDescriptions();
        assertEquals( "if unexpected", 0, ifdesc.size() );
        boolean obs= link.getAttributes().hasObservable();
        assertFalse( "obs unexpected", obs );
        List< String > rt= link.getAttributes().getResourceTypes();
        assertEquals( "rt unexpected", 0, rt.size() );
        String sz= link.getAttributes().getMaximumSizeEstimate();
        assertEquals( "sz unexpected", "", sz );
        String title= link.getAttributes().getTitle();
        assertNull( "title unexpected", title );

    }

    @Test
    public void testIf() throws Exception
    {
        String flowName= "discover";
        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();

        @SuppressWarnings("unchecked")
        HashMap< String, WebLink > links= linkMap( (Set< WebLink >) response.getPayload() );

        WebLink link= links.get( "/service/resource_with_if" );
        assertNotNull( "/service/resource_with_if is missing", link );
        List< String > ifdesc= link.getAttributes().getInterfaceDescriptions();
        assertEquals( "wrong number of ifdesc", 2, ifdesc.size() );
        assertTrue( "ifdesc does not contain 0", ifdesc.contains( "if1" ) );
        assertTrue( "ifdesc does not contain 41", ifdesc.contains( "if2" ) );

        //check other attributes are not there
        List< String > ct= link.getAttributes().getContentTypes();
        assertEquals( "ct unexpected", 0, ct.size() );
        boolean obs= link.getAttributes().hasObservable();
        assertFalse( "obs unexpected", obs );
        List< String > rt= link.getAttributes().getResourceTypes();
        assertEquals( "rt unexpected", 0, rt.size() );
        String sz= link.getAttributes().getMaximumSizeEstimate();
        assertEquals( "sz unexpected", "", sz );
        String title= link.getAttributes().getTitle();
        assertNull( "title unexpected", title );
    }

    //TODO cf106 bug?
    @Ignore
    @Test
    public void testObs() throws Exception
    {
        String flowName= "discover";
        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();

        @SuppressWarnings("unchecked")
        HashMap< String, WebLink > links= linkMap( (Set< WebLink >) response.getPayload() );

        WebLink link= links.get( "/service/resource_with_obs" );
        assertNotNull( "/service/resource_with_obs is missing", link );
        boolean obs= link.getAttributes().hasObservable();
        assertTrue( "obs not true", obs );

        //check other attributes are not there
        List< String > ct= link.getAttributes().getContentTypes();
        assertEquals( "ct unexpected", 0, ct.size() );
        List< String > ifdesc= link.getAttributes().getInterfaceDescriptions();
        assertEquals( "if unexpected", 0, ifdesc.size() );
        List< String > rt= link.getAttributes().getResourceTypes();
        assertEquals( "rt unexpected", 0, rt.size() );
        String sz= link.getAttributes().getMaximumSizeEstimate();
        assertEquals( "sz unexpected", "", sz );
        String title= link.getAttributes().getTitle();
        assertNull( "title unexpected", title );
    }

    @Test
    public void testRt() throws Exception
    {
        String flowName= "discover";
        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();

        @SuppressWarnings("unchecked")
        HashMap< String, WebLink > links= linkMap( (Set< WebLink >) response.getPayload() );

        WebLink link= links.get( "/service/resource_with_rt" );
        assertNotNull( "/service/resource_with_rt is missing", link );
        List< String > rt= link.getAttributes().getResourceTypes();
        assertEquals( "wrong number of rt", 2, rt.size() );
        assertTrue( "rt does not contain rt1", rt.contains( "rt1" ) );
        assertTrue( "rt does not contain rt1", rt.contains( "rt2" ) );

        //check other attributes are not there
        List< String > ct= link.getAttributes().getContentTypes();
        assertEquals( "ct unexpected", 0, ct.size() );
        List< String > ifdesc= link.getAttributes().getInterfaceDescriptions();
        assertEquals( "if unexpected", 0, ifdesc.size() );
        boolean obs= link.getAttributes().hasObservable();
        assertFalse( "obs unexpected", obs );
        String sz= link.getAttributes().getMaximumSizeEstimate();
        assertEquals( "sz unexpected", "", sz );
        String title= link.getAttributes().getTitle();
        assertNull( "title unexpected", title );
    }

    @Test
    public void testSz() throws Exception
    {
        String flowName= "discover";
        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();

        @SuppressWarnings("unchecked")
        HashMap< String, WebLink > links= linkMap( (Set< WebLink >) response.getPayload() );

        WebLink link= links.get( "/service/resource_with_sz" );
        assertNotNull( "/service/resource_with_sz is missing", link );
        String sz= link.getAttributes().getMaximumSizeEstimate();
        assertEquals( "sz has wrong value", "123456", sz );

        //check other attributes are not there
        List< String > ct= link.getAttributes().getContentTypes();
        assertEquals( "ct unexpected", 0, ct.size() );
        List< String > ifdesc= link.getAttributes().getInterfaceDescriptions();
        assertEquals( "if unexpected", 0, ifdesc.size() );
        boolean obs= link.getAttributes().hasObservable();
        assertFalse( "obs unexpected", obs );
        List< String > rt= link.getAttributes().getResourceTypes();
        assertEquals( "rt unexpected", 0, rt.size() );
        String title= link.getAttributes().getTitle();
        assertNull( "title unexpected", title );

    }

    @Test
    public void testTitle() throws Exception
    {
        String flowName= "discover";
        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();

        @SuppressWarnings("unchecked")
        HashMap< String, WebLink > links= linkMap( (Set< WebLink >) response.getPayload() );

        WebLink link= links.get( "/service/resource_with_title" );
        assertNotNull( "/service/resource_with_title is missing", link );
        String title= link.getAttributes().getTitle();
        assertEquals( "title has wrong value", "Title is resource_with_title", title );

        //check other attributes are not there
        List< String > ct= link.getAttributes().getContentTypes();
        assertEquals( "ct unexpected", 0, ct.size() );
        List< String > ifdesc= link.getAttributes().getInterfaceDescriptions();
        assertEquals( "if unexpected", 0, ifdesc.size() );
        boolean obs= link.getAttributes().hasObservable();
        assertFalse( "obs unexpected", obs );
        List< String > rt= link.getAttributes().getResourceTypes();
        assertEquals( "rt unexpected", 0, rt.size() );
        String sz= link.getAttributes().getMaximumSizeEstimate();
        assertEquals( "sz unexpected", "", sz );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDynamicResource() throws Exception
    {
        //check resource is not there
        String flowName= "discover";
        MuleEvent event= testEvent( "nothing_important" );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        HashMap< String, WebLink > links= linkMap( (Set< WebLink >) response.getPayload() );
        WebLink link= links.get( "/service/dynamic_resource" );
        assertNull( "/service/dynamic_resource should not be there", link );

        //create resource
        flowName= "post";
        event= testEvent( "dynamic_resource" );
        result= runFlow( flowName, event );
        response= result.getMessage();
        assertEquals( "could not create resource", "2.01", response.getInboundProperty( "coap.response.code" ) );

        //check resource is there
        flowName= "discover";
        event= testEvent( "nothing_important" );
        result= runFlow( flowName, event );
        response= result.getMessage();
        links= linkMap( (Set< WebLink >) response.getPayload() );
        link= links.get( "/service/dynamic_resource" );
        assertNotNull( "/service/dynamic_resource should not be there", link );

        //delete resource
        flowName= "delete";
        event= testEvent( "dynamic_resource" );
        result= runFlow( flowName, event );
        response= result.getMessage();
        assertEquals( "could not delete resource", "2.02", response.getInboundProperty( "coap.response.code" ) );

        //check resource is not there
        flowName= "discover";
        event= testEvent( "nothing_important" );
        result= runFlow( flowName, event );
        response= result.getMessage();
        links= linkMap( (Set< WebLink >) response.getPayload() );
        link= links.get( "/service/dynamic_resource" );
        assertNull( "/service/dynamic_resource should not be there", link );
    }
}