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
package nl.teslanet.mule.transport.coap.client.test.config;


import nl.teslanet.mule.transport.coap.client.test.config.ConfigAttributes.AttributeName;


public class ConfigAttributeDesc
{

    /**
     * Name of the attribute
     */
    private AttributeName attributeName;

    /**
     * Californium NetworkConfig name of the attribute
     */
    private String networkConfigName;

    /**
     * Expected default value of the attribute 
     */
    private String expectedDefaultValue;

    /**
     * Expected default Californium NetworkConfig value
     */
    private String expectedDefaultNetworkValue;

    /**
     * Custom value to test
     */
    private String customValue;

    /**
     *  Expected Custom Californium NetworkConfig value
     */
    private String expectedCustomNetworkValue;

    /**
     * Constructor
     * @param attributeName
     * @param defaultValue
     * @param customValue
     */
    public ConfigAttributeDesc(
        ConfigAttributes.AttributeName attributeName,
        String networkConfigName,
        String expectedDefaultValue,
        String expectedDefaultNetworkValue,
        String customValue,
        String expectedCustomNetworkValue )
    {
        this.attributeName= attributeName;
        this.networkConfigName= networkConfigName;
        this.expectedDefaultValue= expectedDefaultValue;
        this.expectedDefaultNetworkValue= expectedDefaultNetworkValue;
        this.customValue= customValue;
        this.expectedCustomNetworkValue= expectedCustomNetworkValue;
    }

    /**
     * @return the attributeName
     */
    public AttributeName getAttributeName()
    {
        return attributeName;
    }

    /**
     * @param attributeName the attributeName to set
     */
    public void setAttributeName( AttributeName attributeName )
    {
        this.attributeName= attributeName;
    }

    /**
     * @return the networkConfigName
     */
    public String getNetworkConfigName()
    {
        return networkConfigName;
    }

    /**
     * @param networkConfigName the networkConfigName to set
     */
    public void setNetworkConfigName( String propertyNetworkName )
    {
        this.networkConfigName= propertyNetworkName;
    }

    /**
     * @return the expectedDefaultValue
     */
    public String getExpectedDefaultValue()
    {
        return expectedDefaultValue;
    }

    /**
     * @param expectedDefaultValue the expectedDefaultValue to set
     */
    public void setExpectedDefaultValue( String expectedDefaultValue )
    {
        this.expectedDefaultValue= expectedDefaultValue;
    }

    /**
     * @return the expectedDefaultNetworkValue
     */
    public String getExpectedDefaultNetworkValue()
    {
        return expectedDefaultNetworkValue;
    }

    /**
     * @return the expectedCustomNetworkValue
     */
    public String getExpectedCustomNetworkValue()
    {
        return expectedCustomNetworkValue;
    }

    /**
     * @param expectedDefaultNetworkValue the expectedDefaultNetworkValue to set
     */
    public void setExpectedDefaultNetworkValue( String expectedDefaultNetworkValue )
    {
        this.expectedDefaultNetworkValue= expectedDefaultNetworkValue;
    }

    /**
     * @return the customValue
     */
    public String getCustomValue()
    {
        return customValue;
    }

    /**
     * @param customValue the customValue to set
     */
    public void setCustomValue( String customValue )
    {
        this.customValue= customValue;
    }

    /**
     * @param expectedCustomNetworkValue the expectedCustomNetworkValue to set
     */
    public void setExpectedCustomNetworkValue( String expectedCustomNetworkValue )
    {
        this.expectedCustomNetworkValue= expectedCustomNetworkValue;
    }
}
