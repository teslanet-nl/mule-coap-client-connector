/*******************************************************************************
 * Copyright (c) 2017, 2018, 2019 (teslanet.nl) Rogier Cobben.
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
package nl.teslanet.mule.transport.coap.server.test.utils;


import java.io.IOException;
import java.io.InputStream;

import org.mule.util.IOUtils;


public class Data
{
    /**
     * Read resource as string.
     *
     * @param resourcePath the resource path
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String readResourceAsString( String resourcePath ) throws java.io.IOException
    {
        return IOUtils.getResourceAsString( resourcePath, Data.class );
    }

    /**
     * Read resource as inputstream.
     *
     * @param resourcePath the resource path
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static InputStream readResourceAsStream( String resourcePath ) throws java.io.IOException
    {
        return IOUtils.getResourceAsStream( resourcePath, Data.class, true, true );
    }

    /**
     * Create test content of given size 
     * @param size the content size
     * @return the test content
     */
    public static byte[] getContent( int size )
    {
        byte[] content= new byte [size];
        for ( int i= 0; i < content.length; i++ )
        {
            content[i]= (byte) ( i % ( Byte.MAX_VALUE + 1 ) );
        }
        return content;
    }

    /**
     * Validates the test content of standard size
     * @param content to validate
     * @param size the content size
     * @return true when the content is as expected, otherwise false
     */
    public static boolean validateContent( byte[] content, int size )
    {
        if ( content.length != size ) return false;
        for ( int i= 0; i < content.length; i++ )
        {
            if ( content[i] != (byte) ( i % ( Byte.MAX_VALUE + 1 ) ) )
            {
                return false;
            } ;
        }
        return true;
    }
}
