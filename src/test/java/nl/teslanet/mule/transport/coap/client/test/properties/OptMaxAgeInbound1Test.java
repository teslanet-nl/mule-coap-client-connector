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
 * Test inbound max age property, long value
 *
 */
public class OptMaxAgeInbound1Test extends AbstractInboundPropertiesTest
{

    /**
     * Test value
     * @return the value to use in test
     */
    private Long getValue()
    {
        return new Long( 120 );
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getPropertyName()
     */
    @Override
    protected String getPropertyName()
    {
        return "coap.opt.max_age";
    }
    
    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractInboundPropertiesTest#getPropertyType()
     */
    @Override
    protected PropertyType getPropertyType()
    {
        return PropertyType.Object;
    }


    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getExpectedInboundPropertyValue()
     */
    @Override
    protected Object getExpectedInboundPropertyValue()
    {
        return getValue();
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractInboundPropertiesTest#getStrategy()
     */
    @Override
    protected OptionStrategy getStrategy()
    {
        return new OptMaxAgeStrategy( getValue() );
    }
}