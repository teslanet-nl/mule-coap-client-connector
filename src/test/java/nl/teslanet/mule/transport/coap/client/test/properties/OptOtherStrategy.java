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


import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;


/**
 * The strategy to use by the testserver
 */
public class OptOtherStrategy implements OptionStrategy
{
    /**
     * option test value
     */
    private Option value;

    /**
     * Constructor 
     * @param value the value to test
     */
    public OptOtherStrategy( Option value )
    {
        this.value= value;
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.OptionStrategy#setOption(org.eclipse.californium.core.coap.Response)
     */
    @Override
    public void setOption( Response response )
    {
        response.getOptions().addOption( value );
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.OptionStrategy#validateOption(org.eclipse.californium.core.coap.Request)
     */
    @Override
    public boolean validateOption( Request request )
    {
        Option found= null;
        for ( Option option : request.getOptions().getOthers() )
        {
            if ( option.compareTo( value ) == 0 )
            {
                if ( found == null )
                {
                    //option is found
                    found= option;
                }
                else
                {
                    //option must occur only once
                    return false;
                }
            }
        }
        if ( found != null )
        {
            //option must have equal value
            return found.equals( value );
        }
        else
        {
            //option is missing
            return false;
        }
    }
}
