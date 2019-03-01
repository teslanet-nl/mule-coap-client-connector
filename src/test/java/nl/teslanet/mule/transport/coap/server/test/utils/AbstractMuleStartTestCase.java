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


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.context.MuleContextBuilder;
import org.mule.api.context.MuleContextFactory;
import org.mule.config.DefaultMuleConfiguration;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextBuilder;
import org.mule.context.DefaultMuleContextFactory;


public abstract class AbstractMuleStartTestCase
{
    /**
     * The Mule context
     */
    protected static MuleContext muleContext= null;

    /**
     * Override to use the name of application tested
     * @return the name of the application
     */
    protected String getApplicationName()
    {
        return "";
    }

    /**
     * Overide this method to set the mule config to test
     * @return coma separated paths to mule configs
     */
    protected String getConfigResources()
    {
        return "mule-config.xml";
    }

    /**
     * Override to set expected exception
     */
    protected void expectException()
    {
        //NONE
    }

    /**
     * Exception rule
     */
    @Rule
    public ExpectedException exception= ExpectedException.none();

    /**
     * no setup actions needed
     */
    @Before
    public final void setUp()
    {
        System.getProperties().put( "mule.testingMode", "true" );
        //mvn clean test -Dmule.testingMode=true 
    }

    /**
     * mule cleanup
     */
    @After
    public final void tearDown()
    {
        if ( muleContext != null && !muleContext.isDisposed() )
        {
            try
            {
                muleContext.stop();
            }
            catch ( MuleException e )
            {
                e.printStackTrace();
            }
            muleContext.dispose();
        }
    }

    /**
     * Test whether the mule configuration can be started
     * @throws MuleException thrown when mule could not start with given configuration
     */
    @Test
    public void startMuleTest() throws MuleException
    {
        expectException();

        SpringXmlConfigurationBuilder configBuilder;
        configBuilder= new SpringXmlConfigurationBuilder( getConfigResources() );
        DefaultMuleConfiguration muleConfig= new DefaultMuleConfiguration();
        muleConfig.setId( "testapplication" );
        MuleContextBuilder contextBuilder= new DefaultMuleContextBuilder();
        contextBuilder.setMuleConfiguration( muleConfig );
        MuleContextFactory contextFactory= new DefaultMuleContextFactory();
        MuleContext muleContext;

        muleContext= contextFactory.createMuleContext( configBuilder, contextBuilder );
        muleContext.start();
    }

}
