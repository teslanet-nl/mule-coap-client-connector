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

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

/**
 * The strategy to use by the testserver
 */
public class OptContentFormatStrategy implements OptionStrategy
{
    private int value;
    
    public OptContentFormatStrategy( int value )
    {
        this.value= value;
    }
    
    @Override
    public void setOption( Response response )
    {
        response.getOptions().setContentFormat( value );        
    }

    @Override
    public boolean validateOption( Request request )
    {
        int option= request.getOptions().getContentFormat();
        return option == value;
    }
}
