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


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

import nl.teslanet.mule.transport.coap.commons.options.ETag;


/**
 * The strategy to use by the testserver
 */
public class OptIfMatchListStrategy implements OptionStrategy
{
    private List< ETag > values;

    /**
     * Constructor using single etag
     * @param value the test value
     */
    public OptIfMatchListStrategy( ETag value )
    {
        values= new LinkedList<ETag>();
        values.add( value );
    }

    /**
     * Constructor using list
     * @param values of expected etags
     */
    public OptIfMatchListStrategy( List< ETag > values )
    {
        this.values= values;
    }


    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.OptionStrategy#setOption(org.eclipse.californium.core.coap.Response)
     */
    @Override
    public void setOption( Response response )
    {
        for ( ETag etag : values )
        {
            response.getOptions().addIfMatch( etag.asBytes() );
        }
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.OptionStrategy#validateOption(org.eclipse.californium.core.coap.Request)
     */
    @Override
    public boolean validateOption( Request request )
    {
        List< byte[] > etags= request.getOptions().getIfMatch();
        if ( etags.size() != values.size() ) return false;
        for ( int i= 0; i < etags.size(); i++ )
        {
            if ( !Arrays.equals( etags.get( i ), values.get( i ).asBytes() ) )
            {
                return false;
            }
        }
        return true;
    }
}
