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
 * Test Accept option
 *
 */
public class OptAcceptOutboundTest extends AbstractOutboundPropertiesTest
{
    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getPropertyName()
     */
    @Override
    protected String getPropertyName()
    {
        return "coap.opt.accept";
    }

    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getOutboundPropertyValue()
     */
    @Override
    protected Object getOutboundPropertyValue()
    {
        return new Integer( 41 );
    }

     
    /* (non-Javadoc)
     * @see nl.teslanet.mule.transport.coap.client.test.properties.AbstractPropertiesTest#getStrategy()
     */
    @Override
    protected OptionStrategy getStrategy()
    {
        return new Strategy();
    }
    
    /**
     * The strategy to use by the testserver
     */
    public class Strategy implements OptionStrategy
    {

        @Override
        public void setOption( Response response )
        {
            response.getOptions().setAccept( 41 );        
        }

        @Override
        public boolean validateOption( Request request )
        {
            int accept= request.getOptions().getAccept();
            return accept == 41;
        }
    }
}