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
 * Test outbound uri port property, as String
 *
 */
public class OptUriPortOutbound2Test extends AbstractOutboundPropertiesTest
{
    private int value= 5899;
    
    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getPropertyName()
     */
    @Override
    protected String getPropertyName()
    {
        return "coap.opt.uri_port";
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getOutboundPropertyValue()
     */
    @Override
    protected Object getOutboundPropertyValue()
    {
        return Integer.toString( value );
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractOutboundPropertiesTest#getStrategy()
     */
    @Override
    protected OptionStrategy getStrategy()
    {
        return new OptUriPortStrategy( value );
    }
}