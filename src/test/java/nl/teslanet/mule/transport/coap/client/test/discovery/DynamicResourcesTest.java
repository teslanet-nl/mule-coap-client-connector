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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.WebLink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.munit.runner.functional.FunctionalMunitSuite;


public class DynamicResourcesTest extends FunctionalMunitSuite
{
    URI uri= null;

    HashMap< String, WebLink > links= new HashMap< String, WebLink >();

    @Override
    protected String getConfigResources()
    {
        return "mule-config/discovery/testserver1.xml";
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
    public void setUp() throws Exception
    {
        uri= new URI( "coap", "127.0.0.1", null, null );
        String path= "/service/add_resources";

        CoapClient client= getClient( path );
        client.post( "nothing important", 0 );
        Set< WebLink > response= client.discover();

        links.clear();
        for ( WebLink link : response )
        {
            links.put( link.getURI(), link );
        }
    }

    @After
    public void tearDown() throws Exception
    {
    }

    private CoapClient getClient( String path )
    {
        CoapClient client= new CoapClient( uri.resolve( path ) );
        client.setTimeout( 1000L );
        return client;
    }

    @Test
    public void testWellKnownCore()
    {
        assertEquals( "wrong number of weblinks", 15, links.size() );
        WebLink link= links.get( "/.well-known/core" );
        assertNotNull( "/.well-known/core is missing", link );
    }

    @Test
    public void testCt()
    {
        WebLink link= links.get( "/service/resource2_with_ct" );
        assertNotNull( "/service/resource2_with_ct is missing", link );
        List< String > ct= link.getAttributes().getContentTypes();
        assertEquals( "wrong number ct", 2, ct.size() );
        assertTrue( "ct does not contain 0", ct.contains( "0" ) );
        assertTrue( "ct does not contain 41", ct.contains( "41" ) );
    }

    @Test
    public void testIf()
    {
        WebLink link= links.get( "/service/resource2_with_if" );
        assertNotNull( "/service/resource2_with_if is missing", link );
        List< String > ifdesc= link.getAttributes().getInterfaceDescriptions();
        assertEquals( "wrong number of ifdesc", 2, ifdesc.size() );
        assertTrue( "ifdesc does not contain 0", ifdesc.contains( "if1" ) );
        assertTrue( "ifdesc does not contain 41", ifdesc.contains( "if2" ) );
    }

    @Test
    public void testObs()
    {
        WebLink link= links.get( "/service/resource2_with_obs" );
        assertNotNull( "/service/resource2_with_obs is missing", link );
        boolean obs= link.getAttributes().hasObservable();
        assertTrue( "obs not true", obs );
    }

    @Test
    public void testRt()
    {
        WebLink link= links.get( "/service/resource2_with_rt" );
        assertNotNull( "/service/resource2_with_rt is missing", link );
        List< String > rt= link.getAttributes().getResourceTypes();
        assertEquals( "wrong number of rt", 2, rt.size() );
        assertTrue( "rt does not contain rt1", rt.contains( "rt1" ) );
        assertTrue( "rt does not contain rt1", rt.contains( "rt2" ) );
    }

    @Test
    public void testSz()
    {
        WebLink link= links.get( "/service/resource2_with_sz" );
        assertNotNull( "/service/resource2_with_sz is missing", link );
        String sz= link.getAttributes().getMaximumSizeEstimate();
        assertEquals( "sz has wrong value", "123456", sz );
    }

    @Test
    public void testTitle()
    {
        WebLink link= links.get( "/service/resource2_with_title" );
        assertNotNull( "/service/resource2_with_title is missing", link );
        String title= link.getAttributes().getTitle();
        assertEquals( "title has wrong value", "another resource with a title", title );
    }
}