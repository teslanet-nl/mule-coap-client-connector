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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.californium.core.network.config.NetworkConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import nl.teslanet.mule.transport.coap.client.test.config.ConfigAttributes.AttributeName;
import nl.teslanet.mule.transport.coap.client.config.CoAPClientConfig;


@RunWith(Parameterized.class)
public class ClientConfigTest
{
    /**
     * @return List of attributes to test.
     */
    @Parameters(name= "attributeName = {0}")
    public static AttributeName[] propertiesToTest()
    {
        return AttributeName.values();
    }

    /**
     * The attribute to test.
     */
    @Parameter
    public AttributeName attributeName;

    /**
     * Default no exception should be thrown.
     */
    @Rule
    public ExpectedException exception= ExpectedException.none();

    /**
     * Test getter and setter.
     * @throws Exception
     */
    @Test
    public void testGetterAndSetter() throws Exception
    {
        ConfigAttributeDesc prop= getAttributeDesc( attributeName );
        CoAPClientConfig config= new CoAPClientConfig();
        assertNotNull( "constructor deliverd null", config );

        String input;
        String output;

        input= prop.getCustomValue();
        ConfigAttributes.setValue( prop.getAttributeName(), config, input );
        output= ConfigAttributes.getValue( prop.getAttributeName(), config );
        assertEquals( "get/set failed on " + prop.getAttributeName(), input, output );
    }

    /**
     * Test the default value
     * @throws Exception
     */
    @Test
    public void testDefaults() throws Exception
    {
        ConfigAttributeDesc prop= getAttributeDesc( attributeName );
        CoAPClientConfig config= new CoAPClientConfig();
        assertNotNull( "constructor deliverd null", config );

        String defaultValue;
        String output;

        defaultValue= prop.getExpectedDefaultValue();
        output= ConfigAttributes.getValue( prop.getAttributeName(), config );
        assertEquals( "got wrong default value for " + prop.getAttributeName(), defaultValue, output );
    }

    /**
     * Test the networkconfig value when a custom value is set on the attribute
     * @throws Exception
     */
    @Test
    public void testDefaultNetworkValues() throws Exception
    {
        ConfigAttributeDesc prop= getAttributeDesc( attributeName );
        CoAPClientConfig config= new CoAPClientConfig();
        assertNotNull( "constructor deliverd null", config );

        if ( ConfigAttributes.isNetworkConfig( prop.getAttributeName() ) )
        {
            String input;
            String output;

            input= prop.getExpectedDefaultNetworkValue();
            output= ConfigAttributes.getNetworkConfigValue( prop.getAttributeName(), config );
            assertEquals( "got wrong network value for " + prop.getAttributeName(), input, output );
        }
    }

    /**
     * Test a custom value set on the attribute
     * @throws Exception 
     */
    @Test
    public void testCustomNetworkValues() throws Exception
    {
        ConfigAttributeDesc prop= getAttributeDesc( attributeName );
        CoAPClientConfig config= new CoAPClientConfig();
        assertNotNull( "constructor deliverd null", config );

        if ( ConfigAttributes.isNetworkConfig( prop.getAttributeName() ) )
        {
            String input;
            String output;

            input= prop.getCustomValue();
            ConfigAttributes.setValue( prop.getAttributeName(), config, input );
            String expected= prop.getExpectedCustomNetworkValue();
            output= ConfigAttributes.getNetworkConfigValue( prop.getAttributeName(), config );
            assertEquals( "got wrong network value for " + prop.getAttributeName(), expected, output );
        }

    }

    /**
     * Get the test description of the attribute.
     * @param name of the attribute
     * @return description of the attribute test
     * @throws Exception when the name is unknown
     */
    private static ConfigAttributeDesc getAttributeDesc( AttributeName name ) throws Exception
    {
        switch ( name )
        {
            case secure:
                return new ConfigAttributeDesc( AttributeName.secure, null, "false", null, "true", null );
            case logCoapMessages:
                return new ConfigAttributeDesc( AttributeName.logCoapMessages, null, "false", null, "true", null );
            //from EndpointConfig
            case bindToHost:
                return new ConfigAttributeDesc( AttributeName.bindToHost, null, null, "somehost.org", null, null );
            case bindToPort:
                return new ConfigAttributeDesc( AttributeName.bindToPort, NetworkConfig.Keys.COAP_PORT, null, "5683", "9983", "9983" );
            case bindToSecurePort:
                return new ConfigAttributeDesc( AttributeName.bindToSecurePort, NetworkConfig.Keys.COAP_SECURE_PORT, null, "5684", "9984", "9984" );
//            case maxActivePeers:
//                return new ConfigAttributeDesc( AttributeName.maxActivePeers, NetworkConfig.Keys.MAX_ACTIVE_PEERS, null, "150000", "25", "25" );
//            case maxPeerInactivityPeriod:
//                return new ConfigAttributeDesc( AttributeName.maxPeerInactivityPeriod, NetworkConfig.Keys.MAX_PEER_INACTIVITY_PERIOD, null, "600", "333", "333" );
            case keyStoreLocation:
                return new ConfigAttributeDesc( AttributeName.keyStoreLocation, null, null, null, "/tmp/test1", null );
            case keyStorePassword:
                return new ConfigAttributeDesc( AttributeName.keyStorePassword, null, null, null, "secret1", null );
            case trustStoreLocation:
                return new ConfigAttributeDesc( AttributeName.trustStoreLocation, null, null, null, "/tmp/test2", null );
            case trustStorePassword:
                return new ConfigAttributeDesc( AttributeName.trustStorePassword, null, null, null, "secret1", null );
            case privateKeyAlias:
                return new ConfigAttributeDesc( AttributeName.privateKeyAlias, null, null, null, "secretKey", null );
//            case privateKeyPassword:
//                return new ConfigAttributeDesc( AttributeName.privateKeyPassword, null, null, null, "secret_keypassword", null );
            case trustedRootCertificateAlias:
                return new ConfigAttributeDesc( AttributeName.trustedRootCertificateAlias, null, null, null, "certificate2", null );
            case ackTimeout:
                return new ConfigAttributeDesc( AttributeName.ackTimeout, NetworkConfig.Keys.ACK_TIMEOUT, null, "2000", "22000", "22000" );
            case ackRandomFactor:
                return new ConfigAttributeDesc( AttributeName.ackRandomFactor, NetworkConfig.Keys.ACK_RANDOM_FACTOR, null, "1.5", "3.56", "3.56" );
            case ackTimeoutScale:
                return new ConfigAttributeDesc( AttributeName.ackTimeoutScale, NetworkConfig.Keys.ACK_TIMEOUT_SCALE, null, "2.0", "7.364", "7.364" );
            case maxRetransmit:
                return new ConfigAttributeDesc( AttributeName.maxRetransmit, NetworkConfig.Keys.MAX_RETRANSMIT, null, "4", "44", "44" );
            case exchangeLifetime:
                return new ConfigAttributeDesc( AttributeName.exchangeLifetime, NetworkConfig.Keys.EXCHANGE_LIFETIME, null, "247000", "3000000", "3000000" );
            case nonLifetime:
                return new ConfigAttributeDesc( AttributeName.nonLifetime, NetworkConfig.Keys.NON_LIFETIME, null, "145000", "755000", "755000" );
            case maxTransmitWait:
                return new ConfigAttributeDesc( AttributeName.maxTransmitWait, NetworkConfig.Keys.MAX_TRANSMIT_WAIT, null, "93000", "158000", "158000" );
            case nstart:
                return new ConfigAttributeDesc( AttributeName.nstart, NetworkConfig.Keys.NSTART, null, "1", "145", "145" );
            case leisure:
                return new ConfigAttributeDesc( AttributeName.leisure, NetworkConfig.Keys.LEISURE, null, "5000", "9000", "9000" );
            case probingRate:
                return new ConfigAttributeDesc( AttributeName.probingRate, NetworkConfig.Keys.PROBING_RATE, null, "1.0", "3.15", "3.15" );
            case useRandomMidStart:
                return new ConfigAttributeDesc( AttributeName.useRandomMidStart, NetworkConfig.Keys.USE_RANDOM_MID_START, null, "true", "false", "false" );
//            case midTracker:
//                return new ConfigAttributeDesc( AttributeName.midTracker, NetworkConfig.Keys.MID_TRACKER, null, "GROUPED", "MAPBASED", "MAPBASED" );
//            case midTrackerGroups:
//                return new ConfigAttributeDesc( AttributeName.midTrackerGroups, NetworkConfig.Keys.MID_TRACKER_GROUPS, null, "16", "27", "27" );
            case tokenSizeLimit:
                return new ConfigAttributeDesc( AttributeName.tokenSizeLimit, NetworkConfig.Keys.TOKEN_SIZE_LIMIT, null, "8", "15", "15" );
            case preferredBlockSize:
                return new ConfigAttributeDesc( AttributeName.preferredBlockSize, NetworkConfig.Keys.PREFERRED_BLOCK_SIZE, null, "512", "1024", "1024" );
            case maxMessageSize:
                return new ConfigAttributeDesc( AttributeName.maxMessageSize, NetworkConfig.Keys.MAX_MESSAGE_SIZE, null, "1024", "4156", "4156" );
//            case maxResourceBodySize:
//                return new ConfigAttributeDesc( AttributeName.maxResourceBodySize, NetworkConfig.Keys.MAX_MESSAGE_SIZE, null, "8192", "16000", "16000" );
            case blockwiseStatusLifetime:
                return new ConfigAttributeDesc( AttributeName.blockwiseStatusLifetime, NetworkConfig.Keys.BLOCKWISE_STATUS_LIFETIME, null, "600000", "150000", "150000" );
            case notificationCheckIntervalTime:
                return new ConfigAttributeDesc(
                    AttributeName.notificationCheckIntervalTime,
                    NetworkConfig.Keys.NOTIFICATION_CHECK_INTERVAL_TIME,
                    null,
                    "86400000",
                    "91100001",
                    "91100001" );
            case notificationCheckIntervalCount:
                return new ConfigAttributeDesc( AttributeName.notificationCheckIntervalCount, NetworkConfig.Keys.NOTIFICATION_CHECK_INTERVAL_COUNT, null, "100", "95", "95" );
            case notificationReregistrationBackoff:
                return new ConfigAttributeDesc(
                    AttributeName.notificationReregistrationBackoff,
                    NetworkConfig.Keys.NOTIFICATION_REREGISTRATION_BACKOFF,
                    null,
                    "2000",
                    "5002",
                    "5002" );
            case useCongestionControl:
                return new ConfigAttributeDesc( AttributeName.useCongestionControl, NetworkConfig.Keys.USE_CONGESTION_CONTROL, null, "false", "true", "true" );
            case congestionControlAlgorithm:
                return new ConfigAttributeDesc(
                    AttributeName.congestionControlAlgorithm,
                    NetworkConfig.Keys.CONGESTION_CONTROL_ALGORITHM,
                    null,
                    "Cocoa",
                    "PeakhopperRto",
                    "PeakhopperRto" );
            case protocolStageThreadCount:
                return new ConfigAttributeDesc( AttributeName.protocolStageThreadCount, NetworkConfig.Keys.PROTOCOL_STAGE_THREAD_COUNT, null, Integer.toString( Runtime.getRuntime().availableProcessors()), "12", "12" );
            case networkStageReceiverThreadCount:
                return new ConfigAttributeDesc( AttributeName.networkStageReceiverThreadCount, NetworkConfig.Keys.NETWORK_STAGE_RECEIVER_THREAD_COUNT, null, "1", "12", "12" );
            case networkStageSenderThreadCount:
                return new ConfigAttributeDesc( AttributeName.networkStageSenderThreadCount, NetworkConfig.Keys.NETWORK_STAGE_SENDER_THREAD_COUNT, null, "1", "18", "18" );
            case udpConnectorDatagramSize:
                return new ConfigAttributeDesc( AttributeName.udpConnectorDatagramSize, NetworkConfig.Keys.UDP_CONNECTOR_DATAGRAM_SIZE, null, "2048", "4096", "4096" );
            case udpConnectorReceiveBuffer:
                return new ConfigAttributeDesc( AttributeName.udpConnectorReceiveBuffer, NetworkConfig.Keys.UDP_CONNECTOR_RECEIVE_BUFFER, null, "0", "1000", "1000" );
            case udpConnectorSendBuffer:
                return new ConfigAttributeDesc( AttributeName.udpConnectorSendBuffer, NetworkConfig.Keys.UDP_CONNECTOR_SEND_BUFFER, null, "0", "500", "500" );
            case udpConnectorOutCapacity:
                return new ConfigAttributeDesc(
                    AttributeName.udpConnectorOutCapacity,
                    NetworkConfig.Keys.UDP_CONNECTOR_OUT_CAPACITY,
                    null,
                    "2147483647",
                    "1007483647",
                    "1007483647" );
            case deduplicator:
                return new ConfigAttributeDesc(
                    AttributeName.deduplicator,
                    NetworkConfig.Keys.DEDUPLICATOR,
                    null,
                    NetworkConfig.Keys.DEDUPLICATOR_MARK_AND_SWEEP,
                    "DEDUPLICATOR_CROP_ROTATION",
                    NetworkConfig.Keys.DEDUPLICATOR_CROP_ROTATION );
//            case responseMatching:
//                return new ConfigAttributeDesc( AttributeName.responseMatching, NetworkConfig.Keys.RESPONSE_MATCHING, null, "STRICT", "RELAXED", "RELAXED" );
            case markAndSweepInterval:
                return new ConfigAttributeDesc( AttributeName.markAndSweepInterval, NetworkConfig.Keys.MARK_AND_SWEEP_INTERVAL, null, "10000", "22000", "22000" );
            case cropRotationPeriod:
                return new ConfigAttributeDesc( AttributeName.cropRotationPeriod, NetworkConfig.Keys.CROP_ROTATION_PERIOD, null, "2000", "7800", "7800" );
//            case logHealthStatus:
//                return new ConfigAttributeDesc( AttributeName.logHealthStatus, null, "false", null, "true", null );
//            case secureSessionTimeout:
//                return new ConfigAttributeDesc( AttributeName.secureSessionTimeout, NetworkConfig.Keys.SECURE_SESSION_TIMEOUT, null, "86400", "15689", "15689" );
//            case dtlsAutoResumeTimeout:
//                return new ConfigAttributeDesc( AttributeName.dtlsAutoResumeTimeout, NetworkConfig.Keys.DTLS_AUTO_RESUME_TIMEOUT, null, "30000", "15123", "15123" );
            case healthStatusPrintLevel:
                return new ConfigAttributeDesc( AttributeName.healthStatusPrintLevel, NetworkConfig.Keys.HEALTH_STATUS_PRINT_LEVEL, null, "FINEST", "FINE", "FINE" );
            case healthStatusInterval:
                return new ConfigAttributeDesc( AttributeName.healthStatusInterval, NetworkConfig.Keys.HEALTH_STATUS_INTERVAL, null, "60", "100", "100" );
            default:
                throw new Exception( "cannot create AttributeDesc: name unknown" );
        }
    }
}
