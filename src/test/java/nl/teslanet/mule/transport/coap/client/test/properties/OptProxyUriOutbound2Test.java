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


/**
 * Test outbound proxy scheme property, stringable value
 *
 */
public class OptProxyUriOutbound2Test extends AbstractOutboundPropertiesTest
{
    /**
     * Test other option
     * @return the option to use in test
     */
    private String getValue()
    {
        return new String( "coap://some.server.org/test" );
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getPropertyName()
     */
    @Override
    protected String getPropertyName()
    {
        return "coap.opt.proxy_uri";
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getOutboundPropertyValue()
     */
    @Override
    protected Object getOutboundPropertyValue()
    {
        return new Stringable( getValue() );
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractOutboundPropertiesTest#getStrategy()
     */
    @Override
    protected OptionStrategy getStrategy()
    {
        return new OptProxyUriStrategy( getValue() );
    }
}