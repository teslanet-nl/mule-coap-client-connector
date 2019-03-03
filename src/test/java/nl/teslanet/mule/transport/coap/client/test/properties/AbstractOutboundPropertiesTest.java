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


import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

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


@RunWith(Parameterized.class)
public abstract class AbstractOutboundPropertiesTest extends FunctionalMunitSuite
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
                { "do_get", "/property/validate", ResponseCode.CONTENT },
                { "do_post", "/property/validate", ResponseCode.CHANGED },
                { "do_put", "/property/validate", ResponseCode.CHANGED },
                { "do_delete", "/property/validate", ResponseCode.DELETED } } );
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
     * The property value to set on outbound test
     * @return the value to set
     */
    protected Object getOutboundPropertyValue()
    {
        return new String( getPropertyName() + "_test_value" );
    }

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
        return "";
    }

    /**
     * Override this method when a specific coap resource has to be used. 
     * @return the path extension, that will be added to the base path
     */
    protected String getPathExtension()
    {
        return "";
    }

    /**
     * Start the server
     * @throws Exception when server cannot start
     */
    /**
     * @throws Exception
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
     * Test outbound property
     * @throws Exception should not happen in this test
     */
    @Test
    public void testOutboundProperty() throws Exception
    {
        MuleEvent event= testEvent( "nothing_important" );
        event.setFlowVariable( "path", path + getPathExtension() );
        event.getMessage().setOutboundProperty( getPropertyName(), getOutboundPropertyValue() );
        MuleEvent result= runFlow( flowName + getFlowNameExtension(), event );
        MuleMessage response= result.getMessage();
        assertEquals( "wrong response code", expectedResponseCode.toString(), response.getInboundProperty( "coap.response.code" ) );
    }
}