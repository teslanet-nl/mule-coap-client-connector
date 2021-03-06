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

import java.util.LinkedList;

/**
 * Test outbound location query property, multiple values and leading '?'
 *
 */
public class OptLocationQueryOutbound3mTest extends AbstractOutboundPropertiesTest
{
    /**
     * Test value
     * @return the value to use in test
     */
    private LinkedList< String > getValue()
    {
        LinkedList< String > list= new LinkedList< String >();
        list.add( "first=1" );
        list.add( "second=2" );
        list.add( "third=3" );

        return list;
    }
    
    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getPropertyName()
     */
    @Override
    protected String getPropertyName()
    {
        return "coap.opt.location_query";
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getOutboundPropertyValue()
     */
    @Override
    protected Object getOutboundPropertyValue()
    {
        return new String("?first=1&second=2&third=3");
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractOutboundPropertiesTest#getStrategy()
     */
    @Override
    protected OptionStrategy getStrategy()
    {
        return new OptLocationQueryStrategy( getValue() );
    }
}