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
package nl.teslanet.mule.transport.coap.client.test.properties;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.munit.runner.functional.FunctionalMunitSuite;

import nl.teslanet.mule.transport.coap.commons.options.ETag;


/**
 * Abstract class for testing inbound properties
 *
 */
@RunWith(Parameterized.class)
public abstract class AbstractInboundPropertiesTest extends FunctionalMunitSuite
{
    /**
     * The list of tests with their parameters
     * @return Test parameters.
     */
    @Parameters(name= "flowName= {0}, resourcePath= {1}, responseCode= {2}")
    public static Collection< Object[] > data()
    {
        return Arrays.asList(
            new Object [] []{
                { "do_get", "/property/setoption", ResponseCode.CONTENT },
                { "do_post", "/property/setoption", ResponseCode.CHANGED },
                { "do_put", "/property/setoption", ResponseCode.CHANGED },
                { "do_delete", "/property/setoption", ResponseCode.DELETED } } );
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
    public String path;

    /**
     * The expected response code.
     */
    @Parameter(2)
    public ResponseCode expectedResponseCode;

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
        return "mule-config/properties/testclient1.xml";
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
     * Implement this method to specify the property to test.
     * @return the property name
     */
    protected abstract String getPropertyName();

    /**
     * The property value that is expected to receive in inbound test
     * @return the value to expect
     */
    protected Object getExpectedInboundPropertyValue()
    {
        return new String( getPropertyName() + "_test_value" );
    }

    /**
     * Implement this method to specify the strategy the coap test server has to use
     * in the test.
     * @return the Options strategy to use
     */
    protected abstract OptionStrategy getStrategy();

    /**
     * Override this method when a specific flow has to be used. 
     * @return the flow name extension, that will be added to the base flow name
     */
    protected String getFlowNameExtension()
    {
        //default is no extension
        return "";
    }

    /**
     * Override this method when a specific coap resource has to be used. 
     * @return the path extension, that will be added to the base path
     */
    protected String getPathExtension()
    {
        //default is no extension
        return "";
    }

    /**
     * The assertion needs to know what to expect
     * @return the type of the property expected
     */
    protected PropertyType getPropertyType()
    {
        // default type is object
        return PropertyType.Object;
    }

    /**
     * Start the server, when not started already
     * @throws Exception when server cannot start
     */
    @Before
    public void setUpServer() throws Exception
    {
        if ( server == null )
        {
            server= new PropertiesTestServer( getStrategy() );
            server.start();
        }
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
     * Test inbound property
     * @throws Exception should not happen in this test
     */
    @Test
    public void testInboundProperty() throws Exception
    {
        MuleEvent event= testEvent( "nothing_important" );
        event.setFlowVariable( "path", path + getPathExtension() );
        MuleEvent result= runFlow( flowName, event );
        MuleMessage response= result.getMessage();
        assertEquals( "wrong response code", expectedResponseCode.toString(), response.getInboundProperty( "coap.response.code" ) );

        switch ( getPropertyType() )
        {
            case CollectionOfByteArray:
            {
                @SuppressWarnings("unchecked")
                Collection< byte[] > property= (Collection< byte[] >) response.getInboundProperty( getPropertyName() );
                assertNotNull("property is not found in inbound scope", property );

                @SuppressWarnings("unchecked")
                Collection< byte[] > expected= (Collection< byte[] >) getExpectedInboundPropertyValue();
                assertEquals( "option value list length differ", expected.size(), property.size() );

                Iterator< byte[] > propertyIt= property.iterator();
                Iterator< byte[] > expectedIt= expected.iterator();
                while ( propertyIt.hasNext() && expectedIt.hasNext() )
                {
                    byte[] optionValue= propertyIt.next();
                    byte[] expectedValue= expectedIt.next();
                    assertArrayEquals( "value in collection not equal", expectedValue, optionValue );
                } ;
            }
                break;

            case CollectionOfObject:
            {
                @SuppressWarnings("unchecked")
                Collection< Object > property= (Collection< Object >) response.getInboundProperty( getPropertyName() );
                assertNotNull("property is not found in inbound scope", property );

                @SuppressWarnings("unchecked")
                Collection< Object > expected= (Collection< Object >) getExpectedInboundPropertyValue();
                assertEquals( "option value list length differ", expected.size(), property.size() );

                Iterator< Object > propertyIt= property.iterator();
                Iterator< Object > expectedIt= expected.iterator();
                while ( propertyIt.hasNext() && expectedIt.hasNext() )
                {
                    Object optionValue= propertyIt.next();
                    Object expectedValue= expectedIt.next();
                    assertEquals( "value in collection not equal", expectedValue, optionValue );
                } ;
            }
                break;

            case CollectionOfETag:
            {
                @SuppressWarnings("unchecked")
                Collection< ETag > property= (Collection< ETag >) response.getInboundProperty( getPropertyName() );
                assertNotNull("property is not found in inbound scope", property );

                @SuppressWarnings("unchecked")
                Collection< ETag > expected= (Collection< ETag >) getExpectedInboundPropertyValue();
                assertEquals( "option value list length differ", expected.size(), property.size() );

                Iterator< ETag > propertyIt= property.iterator();
                Iterator< ETag > expectedIt= expected.iterator();
                while ( propertyIt.hasNext() && expectedIt.hasNext() )
                {
                    ETag optionValue= propertyIt.next();
                    ETag expectedValue= expectedIt.next();
                    assertTrue( "value in collection not equal", expectedValue.equals( optionValue ) );
                } ;
            }
                break;

            case ByteArray:
                assertArrayEquals( "wrong inbound property value", (byte[]) getExpectedInboundPropertyValue(), (byte[]) response.getInboundProperty( getPropertyName() ) );
                break;

            case ETag:
                assertTrue( "wrong inbound property value", ( (ETag) getExpectedInboundPropertyValue() ).equals( (ETag) response.getInboundProperty( getPropertyName() ) ) );
                break;

            default:
                assertEquals( "wrong inbound property value", getExpectedInboundPropertyValue(), response.getInboundProperty( getPropertyName() ) );
                break;
        }
    }
}