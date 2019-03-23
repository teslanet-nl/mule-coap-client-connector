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
import nl.teslanet.mule.transport.coap.commons.options.InvalidETagException;
import nl.teslanet.mule.transport.coap.client.test.properties.Stringable;


/**
 * Test outbound Etag list property, multiple values
 *
 */
public class OptEtagListOutbound4mTest extends AbstractOutboundPropertiesTest
{
    /**
     * Test value
     * @return the value to use in test
     * @throws InvalidETagException 
     */
    private LinkedList< ETag > getValue() throws InvalidETagException
    {
        LinkedList< ETag > list= new LinkedList< ETag >();
        list.add( new ETag( "68656C6C6F" ) );
        list.add( new ETag( "6F6C6C61" ) );
        list.add( new ETag( "686F69" ) );

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
    protected Object getOutboundPropertyValue() throws InvalidETagException
    {
        LinkedList< Object > propertyValues= new LinkedList< Object >();
        for ( ETag value : getValue() )
        {
            propertyValues.add( new Stringable( new String( value.asBytes() ) ) );
        }
        return propertyValues;
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractOutboundPropertiesTest#getStrategy()
     */
    @Override
    protected OptionStrategy getStrategy() throws InvalidETagException
    {
        return new OptEtagListStrategy( getValue() );
    }
}