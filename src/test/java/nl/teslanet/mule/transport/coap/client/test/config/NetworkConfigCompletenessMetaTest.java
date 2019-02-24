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


import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import nl.teslanet.mule.transport.coap.client.test.config.ConfigAttributes.AttributeName;


@RunWith(Parameterized.class)
public class NetworkConfigCompletenessMetaTest
{
    /**
     * @return List of attributes to test.
     */
    @Parameters(name= "key = {0}")
    public static Iterable< String > attributeKeysToTest()
    {
        //Alas the keys is not an enum, so...
        //copy from NetworkConfig.Keys
        // regex: (public static final String \w+ = )("\w+");
        // replace: /* \1 */ \2,
        return Arrays.asList(

             "COAP_PORT",
             "COAP_SECURE_PORT",
             "ACK_TIMEOUT",
             "ACK_RANDOM_FACTOR",
             "ACK_TIMEOUT_SCALE",
             "MAX_RETRANSMIT",
             "EXCHANGE_LIFETIME",
             "NON_LIFETIME",
             "MAX_TRANSMIT_WAIT",
             "NSTART",
             "LEISURE",
             "PROBING_RATE",

             "USE_RANDOM_MID_START",
             "TOKEN_SIZE_LIMIT",

             "PREFERRED_BLOCK_SIZE",
             "MAX_MESSAGE_SIZE",
             "BLOCKWISE_STATUS_LIFETIME",

             "NOTIFICATION_CHECK_INTERVAL",
             "NOTIFICATION_CHECK_INTERVAL_COUNT",
             "NOTIFICATION_REREGISTRATION_BACKOFF",

             "USE_CONGESTION_CONTROL",
             "CONGESTION_CONTROL_ALGORITHM",

             "PROTOCOL_STAGE_THREAD_COUNT",
             "NETWORK_STAGE_RECEIVER_THREAD_COUNT",
             "NETWORK_STAGE_SENDER_THREAD_COUNT",

             "UDP_CONNECTOR_DATAGRAM_SIZE",
             "UDP_CONNECTOR_RECEIVE_BUFFER",
             "UDP_CONNECTOR_SEND_BUFFER",
             "UDP_CONNECTOR_OUT_CAPACITY",

             "DEDUPLICATOR",
//             "DEDUPLICATOR_MARK_AND_SWEEP",
             "MARK_AND_SWEEP_INTERVAL",
//             "DEDUPLICATOR_CROP_ROTATION",
             "CROP_ROTATION_PERIOD",
//             "NO_DEDUPLICATOR",

//             "HTTP_PORT",
//             "HTTP_SERVER_SOCKET_TIMEOUT",
//             "HTTP_SERVER_SOCKET_BUFFER_SIZE",
//             "HTTP_CACHE_RESPONSE_MAX_AGE",
//             "HTTP_CACHE_SIZE",

             "HEALTH_STATUS_PRINT_LEVEL",
             "HEALTH_STATUS_INTERVAL"

        );
    }

    /**
     * The attribute to test.
     */
    @Parameter
    public String attributeKey;

    /**
     * Default no exception should be thrown.
     */
    @Rule
    public ExpectedException exception= ExpectedException.none();

    /**
     * Test whether the attribute key is covered in tests.
     * @throws Exception
     */
    @Test
    public void testKeyIsCovered() throws Exception
    {
        AttributeName attributeName= ConfigAttributes.getName( attributeKey );
        assertNotNull( "could not get AttributeName for key: " + attributeKey, attributeName );
    }
}