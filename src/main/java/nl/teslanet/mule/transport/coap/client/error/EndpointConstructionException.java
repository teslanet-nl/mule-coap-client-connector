/*******************************************************************************
 * Copyright (c) 2017, 2018 (teslanet.nl) Rogier Cobben.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Public License - v 2.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *    (teslanet.nl) Rogier Cobben - initial creation
 ******************************************************************************/
package nl.teslanet.mule.transport.coap.client.error;


/**
 * The EndpointConstructionException is thrown when the CoAP endpoint could not be constructed.
 * 
 */
public class EndpointConstructionException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID= 1L;

    public EndpointConstructionException()
    {
        super();
    }

    public EndpointConstructionException( String message )
    {
        super( message );
    }

    public EndpointConstructionException( Throwable cause )
    {
        super( cause );
    }

    public EndpointConstructionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public EndpointConstructionException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
