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
package nl.teslanet.mule.transport.coap.client.test.exceptionhandling;


/**
 * Exception to test exception handling
 *
 */
public class TestException extends Exception
{
    /**
     * class version
     */
    private static final long serialVersionUID= 1L;

    public TestException()
    {
        super();
    }

    public TestException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }

    public TestException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public TestException( String message )
    {
        super( message );
    }

    public TestException( Throwable cause )
    {
        super( cause );
    }

}
