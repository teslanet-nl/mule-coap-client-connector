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


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;


/**
 * The strategy to use by the testserver
 */
public class OptUriQueryStrategy implements OptionStrategy
{
    private LinkedList< String > value;

    /**
     * Constructor 
     * @param value the test value
     */
    public OptUriQueryStrategy( LinkedList< String > value )
    {
        this.value= value;
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.OptionStrategy#setOption(org.eclipse.californium.core.coap.Response)
     */
    @Override
    public void setOption( Response response )
    {
        for ( String segment : value )
        {
            response.getOptions().addUriQuery( segment );
        }
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.OptionStrategy#validateOption(org.eclipse.californium.core.coap.Request)
     */
    @Override
    public boolean validateOption( Request request )
    {
        List< String > uriQuery= request.getOptions().getUriQuery();
        if ( uriQuery.size() != value.size() )
        {
            return false;
        }
        Iterator< String > it1= value.iterator();
        Iterator< String > it2= uriQuery.iterator();
        while ( it1.hasNext() )
        {
            if ( !it1.next().equals( it2.next() ) ) return false;
        }
        return true;
    }
}
