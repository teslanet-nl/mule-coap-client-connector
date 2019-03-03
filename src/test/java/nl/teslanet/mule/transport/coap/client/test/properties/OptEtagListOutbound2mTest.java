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

import nl.teslanet.mule.transport.coap.commons.options.ETag;

/**
 * Test outbound Etag list property, multiple values
 *
 */
public class OptEtagListOutbound2mTest extends AbstractOutboundPropertiesTest
{
    /**
     * Test value
     * @return the value to use in test
     */
    private LinkedList< ETag > getValue()
    {
        LinkedList< ETag  > list= new LinkedList< ETag >();
        list.add( new ETag( "A0" ) );
        list.add( new ETag( "0011FF" ) );
        list.add( new ETag( "0011223344556677" ) );

        return list;
    }
    
    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getPropertyName()
     */
    @Override
    protected String getPropertyName()
    {
        return "coap.opt.etag.list";
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getOutboundPropertyValue()
     */
    @Override
    protected Object getOutboundPropertyValue()
    {
        LinkedList<byte[]> propertyValue= new LinkedList<byte[]>();
        for ( ETag value : getValue() )
        {
            propertyValue.add( value.asBytes() );
        }
        return propertyValue;
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractOutboundPropertiesTest#getStrategy()
     */
    @Override
    protected OptionStrategy getStrategy()
    {
        return new OptEtagListStrategy( getValue() );
    }
}