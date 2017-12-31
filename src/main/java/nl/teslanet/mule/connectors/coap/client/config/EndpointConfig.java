package nl.teslanet.mule.connectors.coap.client.config;


import org.eclipse.californium.core.network.config.NetworkConfig;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.param.Optional;

public class EndpointConfig
{
    @Configurable
    @Optional
    @Placement(tab= "Endpoint", group="Endpoint")
    private String bindToHost= null;

    @Configurable
    @Optional
    @Placement(tab= "Endpoint", group="Endpoint")
    private String bindToPort= null;

    @Configurable
    @Optional
    @Placement(tab= "Endpoint", group="Endpoint")
    private String bindToSecurePort= null;

    //---------------    
    @Configurable
    @Optional
    @Placement(tab= "Ackowledgement", group= "Ackowledgement")
    private String ackTimeout= null;

    @Configurable
    @Optional
    @Placement(tab= "Ackowledgement", group= "Ackowledgement")
    private String ackRandomFactor= null;

    @Configurable
    @Optional
    @Placement(tab= "Ackowledgement", group= "Ackowledgement")
    private String ackTimeoutScale= null;

    @Configurable
    @Optional
    @Placement(tab= "Ackowledgement", group= "Ackowledgement")
    private String maxRetransmit= null;

    @Configurable
    @Optional
    @Placement(tab= "Ackowledgement", group= "Ackowledgement")
    private String exchangeLifetime= null;

    @Configurable
    @Optional
    @Placement(tab= "Ackowledgement", group= "Ackowledgement")
    private String nonLifetime= null;

    @Configurable
    @Optional
    @Placement(tab= "Ackowledgement", group= "Ackowledgement")
    private String maxTransmitWait= null;

    //-----------------
    @Configurable
    @Optional
    @Placement(tab= "Endpoint", group="Endpoint")
    private String nstart= null;

    //-----------------
    @Configurable
    @Optional
    @Placement(tab= "Endpoint", group="Endpoint")
    private String leisure= null;

    @Configurable
    @Optional
    @Placement(tab= "Endpoint", group="Endpoint")
    private String probingRate= null;

    //-----------------
    @Configurable
    @Optional
    @Placement(tab= "Token", group= "Token")
    private String useRandomMidStart= null;

    @Configurable
    @Optional
    @Placement(tab= "Token", group= "Token")
    private String tokenSizeLimit= null;

    //---------------
    @Configurable
    @Optional
    @Placement(tab= "Blockwise", group= "Blockwise")
    private String preferredBlockSize= null;

    @Configurable
    @Optional
    @Placement(tab= "Blockwise", group= "Blockwise")
    private String maxMessageSize= null;

    @Configurable
    @Optional
    @Placement(tab= "Blockwise", group= "Blockwise")
    private String blockwiseStatusLifetime= null;

    //---------------
    @Configurable
    @Optional
    @Placement(tab= "Notifcation", group= "Notifcation")
    private String notificationCheckIntervalTime= null;

    @Configurable
    @Optional
    @Placement(tab= "Notifcation", group= "Notifcation")
    private String notificationCheckIntervalCount= null;

    @Configurable
    @Optional
    @Placement(tab= "Notifcation", group= "Notifcation")
    private String notificationReregistrationBackoff= null;

    //---------------
    @Configurable
    @Optional
    @Placement(tab= "Congestion", group= "Congestion")
    private String useCongestionControl= null;

    @Configurable
    @Optional
    @Placement(tab= "Congestion", group= "Congestion")
    private String congestionControlAlgorithm= null;

    //-----------------
    @Configurable
    @Optional
    @Placement(tab= "Threads", group= "Threads")
    private String protocolStageThreadCount= null;

    @Configurable
    @Optional
    @Placement(tab= "Threads", group= "Threads")
    private String networkStageReceiverThreadCount= null;

    @Configurable
    @Optional
    @Placement(tab= "Threads", group= "Threads")
    private String networkStageSenderThreadCount= null;

    //---------------
    @Configurable
    @Optional
    @Placement(tab= "UDP", group= "UDP")
    private String udpConnectorDatagramSize= null;

    @Configurable
    @Optional
    @Placement(tab= "UDP", group= "UDP")
    private String udpConnectorReceiveBuffer= null;

    @Configurable
    @Optional
    @Placement(tab= "UDP", group= "UDP")
    private String udpConnectorSendBuffer= null;

    @Configurable
    @Optional
    @Placement(tab= "UDP", group= "UDP")
    private String udpConnectorOutCapacity= null;

    //---------------
    @Configurable
    @Optional
    @Placement(tab= "Deduplicator", group= "Deduplicator")
    private String deduplicator= null;

    @Configurable
    @Optional
    @Placement(tab= "Deduplicator", group= "Deduplicator")
    private String deduplicatorMarkAndSweep= null;

    @Configurable
    @Optional
    @Placement(tab= "Deduplicator", group= "Deduplicator")
    private String markAndSweepInterval= null;

    @Configurable
    @Optional
    @Placement(tab= "Deduplicator", group= "Deduplicator")
    private String deduplicatorCropRotation= null;

    @Configurable
    @Optional
    @Placement(tab= "Deduplicator", group= "Deduplicator")
    private String cropRotationPeriod= null;

    @Configurable
    @Optional
    @Placement(tab= "Deduplicator", group= "Deduplicator")
    private String noDeduplicator= null;

    //---------------
    @Configurable
    @Optional
    @Placement(tab= "HTTP", group= "HTTP")
    private String httpPort= null;

    @Configurable
    @Optional
    @Placement(tab= "HTTP", group= "HTTP")
    private String httpServerSocketTimeout= null;

    @Configurable
    @Optional
    @Placement(tab= "HTTP", group= "HTTP")
    private String httpServerSocketBufferSize= null;

    @Configurable
    @Optional
    @Placement(tab= "HTTP", group= "HTTP")
    private String httpCacheResponseMaxAge= null;

    @Configurable
    @Optional
    @Placement(tab= "HTTP", group= "HTTP")
    private String httpCacheSize= null;

    //---------------
    @Configurable
    @Optional
    @Placement(tab= "Status", group= "Status")
    private String healthStatusPrintLevel= null;

    @Configurable
    @Optional
    @Placement(tab= "Status", group= "Status")
    private String healthStatusInterval= null;

    /**
     * @return the host
     */
    public String getBindToHost()
    {
        return bindToHost;
    }

    /**
     * @param host the host to set
     */
    public void setBindToHost( String host )
    {
        this.bindToHost= host;
    }

    /**
     * @return the coapPort
     */
    public String getBindToPort()
    {
        return this.bindToPort;
    }

    /**
     * @param coapPort the coapPort to set
     */
    public void setBindToPort( String coapPort )
    {
        this.bindToPort= coapPort;
    }

    /**
     * @return the coapSecurePort
     */
    public String getBindToSecurePort()
    {
        return bindToSecurePort;
    }

    /**
     * @param coapSecurePort the coapSecurePort to set
     */
    public void setBindToSecurePort( String coapSecurePort )
    {
        this.bindToSecurePort= coapSecurePort;
    }

    /**
     * @return the ackTimeout
     */
    public String getAckTimeout()
    {
        return ackTimeout;
    }

    /**
     * @param ackTimeout the ackTimeout to set
     */
    public void setAckTimeout( String ackTimeout )
    {
        this.ackTimeout= ackTimeout;
    }

    /**
     * @return the ackRandomFactor
     */
    public String getAckRandomFactor()
    {
        return ackRandomFactor;
    }

    /**
     * @param ackRandomFactor the ackRandomFactor to set
     */
    public void setAckRandomFactor( String ackRandomFactor )
    {
        this.ackRandomFactor= ackRandomFactor;
    }

    /**
     * @return the ackTimeoutScale
     */
    public String getAckTimeoutScale()
    {
        return ackTimeoutScale;
    }

    /**
     * @param ackTimeoutScale the ackTimeoutScale to set
     */
    public void setAckTimeoutScale( String ackTimeoutScale )
    {
        this.ackTimeoutScale= ackTimeoutScale;
    }

    /**
     * @return the maxRetransmit
     */
    public String getMaxRetransmit()
    {
        return maxRetransmit;
    }

    /**
     * @param maxRetransmit the maxRetransmit to set
     */
    public void setMaxRetransmit( String maxRetransmit )
    {
        this.maxRetransmit= maxRetransmit;
    }

    /**
     * @return the exchangeLifetime
     */
    public String getExchangeLifetime()
    {
        return exchangeLifetime;
    }

    /**
     * @param exchangeLifetime the exchangeLifetime to set
     */
    public void setExchangeLifetime( String exchangeLifetime )
    {
        this.exchangeLifetime= exchangeLifetime;
    }

    /**
     * @return the nonLifetime
     */
    public String getNonLifetime()
    {
        return nonLifetime;
    }

    /**
     * @param nonLifetime the nonLifetime to set
     */
    public void setNonLifetime( String nonLifetime )
    {
        this.nonLifetime= nonLifetime;
    }

    /**
     * @return the maxTransmitWait
     */
    public String getMaxTransmitWait()
    {
        return maxTransmitWait;
    }

    /**
     * @param maxTransmitWait the maxTransmitWait to set
     */
    public void setMaxTransmitWait( String maxTransmitWait )
    {
        this.maxTransmitWait= maxTransmitWait;
    }

    /**
     * @return the nstart
     */
    public String getNstart()
    {
        return nstart;
    }

    /**
     * @param nstart the nstart to set
     */
    public void setNstart( String nstart )
    {
        this.nstart= nstart;
    }

    /**
     * @return the leisure
     */
    public String getLeisure()
    {
        return leisure;
    }

    /**
     * @param leisure the leisure to set
     */
    public void setLeisure( String leisure )
    {
        this.leisure= leisure;
    }

    /**
     * @return the probingRate
     */
    public String getProbingRate()
    {
        return probingRate;
    }

    /**
     * @param probingRate the probingRate to set
     */
    public void setProbingRate( String probingRate )
    {
        this.probingRate= probingRate;
    }

    /**
     * @return the useRandomMidStart
     */
    public String getUseRandomMidStart()
    {
        return useRandomMidStart;
    }

    /**
     * @param useRandomMidStart the useRandomMidStart to set
     */
    public void setUseRandomMidStart( String useRandomMidStart )
    {
        this.useRandomMidStart= useRandomMidStart;
    }

    /**
     * @return the tokenSizeLimit
     */
    public String getTokenSizeLimit()
    {
        return tokenSizeLimit;
    }

    /**
     * @param tokenSizeLimit the tokenSizeLimit to set
     */
    public void setTokenSizeLimit( String tokenSizeLimit )
    {
        this.tokenSizeLimit= tokenSizeLimit;
    }

    /**
     * @return the preferredBlockSize
     */
    public String getPreferredBlockSize()
    {
        return preferredBlockSize;
    }

    /**
     * @param preferredBlockSize the preferredBlockSize to set
     */
    public void setPreferredBlockSize( String preferredBlockSize )
    {
        this.preferredBlockSize= preferredBlockSize;
    }

    /**
     * @return the maxMessageSize
     */
    public String getMaxMessageSize()
    {
        return maxMessageSize;
    }

    /**
     * @param maxMessageSize the maxMessageSize to set
     */
    public void setMaxMessageSize( String maxMessageSize )
    {
        this.maxMessageSize= maxMessageSize;
    }

    /**
     * @return the blockwiseStatusLifetime
     */
    public String getBlockwiseStatusLifetime()
    {
        return blockwiseStatusLifetime;
    }

    /**
     * @param blockwiseStatusLifetime the blockwiseStatusLifetime to set
     */
    public void setBlockwiseStatusLifetime( String blockwiseStatusLifetime )
    {
        this.blockwiseStatusLifetime= blockwiseStatusLifetime;
    }

    /**
     * @return the notificationCheckIntervalTime
     */
    public String getNotificationCheckIntervalTime()
    {
        return notificationCheckIntervalTime;
    }

    /**
     * @param notificationCheckIntervalTime the notificationCheckIntervalTime to set
     */
    public void setNotificationCheckIntervalTime( String notificationCheckIntervalTime )
    {
        this.notificationCheckIntervalTime= notificationCheckIntervalTime;
    }

    /**
     * @return the notificationCheckIntervalCount
     */
    public String getNotificationCheckIntervalCount()
    {
        return notificationCheckIntervalCount;
    }

    /**
     * @param notificationCheckIntervalCount the notificationCheckIntervalCount to set
     */
    public void setNotificationCheckIntervalCount( String notificationCheckIntervalCount )
    {
        this.notificationCheckIntervalCount= notificationCheckIntervalCount;
    }

    /**
     * @return the notificationReregistrationBackoff
     */
    public String getNotificationReregistrationBackoff()
    {
        return notificationReregistrationBackoff;
    }

    /**
     * @param notificationReregistrationBackoff the notificationReregistrationBackoff to set
     */
    public void setNotificationReregistrationBackoff( String notificationReregistrationBackoff )
    {
        this.notificationReregistrationBackoff= notificationReregistrationBackoff;
    }

    /**
     * @return the useCongestionControl
     */
    public String getUseCongestionControl()
    {
        return useCongestionControl;
    }

    /**
     * @param useCongestionControl the useCongestionControl to set
     */
    public void setUseCongestionControl( String useCongestionControl )
    {
        this.useCongestionControl= useCongestionControl;
    }

    /**
     * @return the congestionControlAlgorithm
     */
    public String getCongestionControlAlgorithm()
    {
        return congestionControlAlgorithm;
    }

    /**
     * @param congestionControlAlgorithm the congestionControlAlgorithm to set
     */
    public void setCongestionControlAlgorithm( String congestionControlAlgorithm )
    {
        this.congestionControlAlgorithm= congestionControlAlgorithm;
    }

    /**
     * @return the protocolStageThreadCount
     */
    public String getProtocolStageThreadCount()
    {
        return protocolStageThreadCount;
    }

    /**
     * @param protocolStageThreadCount the protocolStageThreadCount to set
     */
    public void setProtocolStageThreadCount( String protocolStageThreadCount )
    {
        this.protocolStageThreadCount= protocolStageThreadCount;
    }

    /**
     * @return the networkStageReceiverThreadCount
     */
    public String getNetworkStageReceiverThreadCount()
    {
        return networkStageReceiverThreadCount;
    }

    /**
     * @param networkStageReceiverThreadCount the networkStageReceiverThreadCount to set
     */
    public void setNetworkStageReceiverThreadCount( String networkStageReceiverThreadCount )
    {
        this.networkStageReceiverThreadCount= networkStageReceiverThreadCount;
    }

    /**
     * @return the networkStageSenderThreadCount
     */
    public String getNetworkStageSenderThreadCount()
    {
        return networkStageSenderThreadCount;
    }

    /**
     * @param networkStageSenderThreadCount the networkStageSenderThreadCount to set
     */
    public void setNetworkStageSenderThreadCount( String networkStageSenderThreadCount )
    {
        this.networkStageSenderThreadCount= networkStageSenderThreadCount;
    }

    /**
     * @return the udpConnectorDatagramSize
     */
    public String getUdpConnectorDatagramSize()
    {
        return udpConnectorDatagramSize;
    }

    /**
     * @param udpConnectorDatagramSize the udpConnectorDatagramSize to set
     */
    public void setUdpConnectorDatagramSize( String udpConnectorDatagramSize )
    {
        this.udpConnectorDatagramSize= udpConnectorDatagramSize;
    }

    /**
     * @return the udpConnectorReceiveBuffer
     */
    public String getUdpConnectorReceiveBuffer()
    {
        return udpConnectorReceiveBuffer;
    }

    /**
     * @param udpConnectorReceiveBuffer the udpConnectorReceiveBuffer to set
     */
    public void setUdpConnectorReceiveBuffer( String udpConnectorReceiveBuffer )
    {
        this.udpConnectorReceiveBuffer= udpConnectorReceiveBuffer;
    }

    /**
     * @return the udpConnectorSendBuffer
     */
    public String getUdpConnectorSendBuffer()
    {
        return udpConnectorSendBuffer;
    }

    /**
     * @param udpConnectorSendBuffer the udpConnectorSendBuffer to set
     */
    public void setUdpConnectorSendBuffer( String udpConnectorSendBuffer )
    {
        this.udpConnectorSendBuffer= udpConnectorSendBuffer;
    }

    /**
     * @return the udpConnectorOutCapacity
     */
    public String getUdpConnectorOutCapacity()
    {
        return udpConnectorOutCapacity;
    }

    /**
     * @param udpConnectorOutCapacity the udpConnectorOutCapacity to set
     */
    public void setUdpConnectorOutCapacity( String udpConnectorOutCapacity )
    {
        this.udpConnectorOutCapacity= udpConnectorOutCapacity;
    }

    /**
     * @return the deduplicator
     */
    public String getDeduplicator()
    {
        return deduplicator;
    }

    /**
     * @param deduplicator the deduplicator to set
     */
    public void setDeduplicator( String deduplicator )
    {
        this.deduplicator= deduplicator;
    }

    /**
     * @return the deduplicatorMarkAndSweep
     */
    public String getDeduplicatorMarkAndSweep()
    {
        return deduplicatorMarkAndSweep;
    }

    /**
     * @param deduplicatorMarkAndSweep the deduplicatorMarkAndSweep to set
     */
    public void setDeduplicatorMarkAndSweep( String deduplicatorMarkAndSweep )
    {
        this.deduplicatorMarkAndSweep= deduplicatorMarkAndSweep;
    }

    /**
     * @return the markAndSweepInterval
     */
    public String getMarkAndSweepInterval()
    {
        return markAndSweepInterval;
    }

    /**
     * @param markAndSweepInterval the markAndSweepInterval to set
     */
    public void setMarkAndSweepInterval( String markAndSweepInterval )
    {
        this.markAndSweepInterval= markAndSweepInterval;
    }

    /**
     * @return the deduplicatorCropRotation
     */
    public String getDeduplicatorCropRotation()
    {
        return deduplicatorCropRotation;
    }

    /**
     * @param deduplicatorCropRotation the deduplicatorCropRotation to set
     */
    public void setDeduplicatorCropRotation( String deduplicatorCropRotation )
    {
        this.deduplicatorCropRotation= deduplicatorCropRotation;
    }

    /**
     * @return the cropRotationPeriod
     */
    public String getCropRotationPeriod()
    {
        return cropRotationPeriod;
    }

    /**
     * @param cropRotationPeriod the cropRotationPeriod to set
     */
    public void setCropRotationPeriod( String cropRotationPeriod )
    {
        this.cropRotationPeriod= cropRotationPeriod;
    }

    /**
     * @return the noDeduplicator
     */
    public String getNoDeduplicator()
    {
        return noDeduplicator;
    }

    /**
     * @param noDeduplicator the noDeduplicator to set
     */
    public void setNoDeduplicator( String noDeduplicator )
    {
        this.noDeduplicator= noDeduplicator;
    }

    /**
     * @return the httpPort
     */
    public String getHttpPort()
    {
        return httpPort;
    }

    /**
     * @param httpPort the httpPort to set
     */
    public void setHttpPort( String httpPort )
    {
        this.httpPort= httpPort;
    }

    /**
     * @return the httpServerSocketTimeout
     */
    public String getHttpServerSocketTimeout()
    {
        return httpServerSocketTimeout;
    }

    /**
     * @param httpServerSocketTimeout the httpServerSocketTimeout to set
     */
    public void setHttpServerSocketTimeout( String httpServerSocketTimeout )
    {
        this.httpServerSocketTimeout= httpServerSocketTimeout;
    }

    /**
     * @return the httpServerSocketBufferSize
     */
    public String getHttpServerSocketBufferSize()
    {
        return httpServerSocketBufferSize;
    }

    /**
     * @param httpServerSocketBufferSize the httpServerSocketBufferSize to set
     */
    public void setHttpServerSocketBufferSize( String httpServerSocketBufferSize )
    {
        this.httpServerSocketBufferSize= httpServerSocketBufferSize;
    }

    /**
     * @return the httpCacheResponseMaxAge
     */
    public String getHttpCacheResponseMaxAge()
    {
        return httpCacheResponseMaxAge;
    }

    /**
     * @param httpCacheResponseMaxAge the httpCacheResponseMaxAge to set
     */
    public void setHttpCacheResponseMaxAge( String httpCacheResponseMaxAge )
    {
        this.httpCacheResponseMaxAge= httpCacheResponseMaxAge;
    }

    /**
     * @return the httpCacheSize
     */
    public String getHttpCacheSize()
    {
        return httpCacheSize;
    }

    /**
     * @param httpCacheSize the httpCacheSize to set
     */
    public void setHttpCacheSize( String httpCacheSize )
    {
        this.httpCacheSize= httpCacheSize;
    }

    /**
     * @return the healthStatusPrintLevel
     */
    public String getHealthStatusPrintLevel()
    {
        return healthStatusPrintLevel;
    }

    /**
     * @param healthStatusPrintLevel the healthStatusPrintLevel to set
     */
    public void setHealthStatusPrintLevel( String healthStatusPrintLevel )
    {
        this.healthStatusPrintLevel= healthStatusPrintLevel;
    }

    /**
     * @return the healthStatusInterval
     */
    public String getHealthStatusInterval()
    {
        return healthStatusInterval;
    }

    /**
     * @param healthStatusInterval the healthStatusInterval to set
     */
    public void setHealthStatusInterval( String healthStatusInterval )
    {
        this.healthStatusInterval= healthStatusInterval;
    }

    public NetworkConfig getNetworkConfig()
    {
        NetworkConfig config= new NetworkConfig();
               
        if ( this.bindToPort != null ) config.setInt(NetworkConfig.Keys.COAP_PORT, Integer.valueOf( this.bindToPort )); // CoAP.DEFAULT_COAP_PORT);
        if ( this.bindToSecurePort != null ) config.setInt(NetworkConfig.Keys.COAP_SECURE_PORT, Integer.valueOf( this.bindToSecurePort )); // CoAP.DEFAULT_COAP_SECURE_PORT);
                
        if ( this.ackTimeout != null ) config.setInt(NetworkConfig.Keys.ACK_TIMEOUT, Integer.valueOf( this.ackTimeout )); // 2000);
        if ( this.ackRandomFactor != null ) config.setFloat(NetworkConfig.Keys.ACK_RANDOM_FACTOR, Float.valueOf( this.ackRandomFactor )); // 1.5f); Float.va
        if ( this.ackTimeoutScale != null ) config.setFloat(NetworkConfig.Keys.ACK_TIMEOUT_SCALE, Float.valueOf( this.ackTimeoutScale )); // 2f);
        if ( this.maxRetransmit != null ) config.setInt(NetworkConfig.Keys.MAX_RETRANSMIT, Integer.valueOf( this.maxRetransmit )); // 4);
        if ( this.exchangeLifetime != null ) config.setLong(NetworkConfig.Keys.EXCHANGE_LIFETIME, Long.valueOf( this.exchangeLifetime )); // 247 * 1000); // ms
        if ( this.nonLifetime != null ) config.setLong(NetworkConfig.Keys.NON_LIFETIME, Long.valueOf( this.nonLifetime )); // 145 * 1000); // ms
        if ( this.maxTransmitWait != null ) config.setLong(NetworkConfig.Keys.MAX_TRANSMIT_WAIT, Long.valueOf( this.maxTransmitWait )); // 93 * 1000);
        if ( this.nstart != null ) config.setInt(NetworkConfig.Keys.NSTART, Integer.valueOf( this.nstart )); // 1);
        if ( this.leisure != null ) config.setInt(NetworkConfig.Keys.LEISURE, Integer.valueOf( this.leisure )); // 5000);
        if ( this.probingRate != null ) config.setFloat(NetworkConfig.Keys.PROBING_RATE, Float.valueOf( this.probingRate )); // 1f);

        if ( this.useRandomMidStart != null ) config.setBoolean(NetworkConfig.Keys.USE_RANDOM_MID_START, Boolean.valueOf( this.useRandomMidStart )); // true);
        if ( this.tokenSizeLimit != null ) config.setInt(NetworkConfig.Keys.TOKEN_SIZE_LIMIT, Integer.valueOf( this.tokenSizeLimit )); // 8);

        if ( this.preferredBlockSize != null ) config.setInt(NetworkConfig.Keys.PREFERRED_BLOCK_SIZE, Integer.valueOf( this.preferredBlockSize )); // 512);
        if ( this.maxMessageSize != null ) config.setInt(NetworkConfig.Keys.MAX_MESSAGE_SIZE, Integer.valueOf( this.maxMessageSize )); // 1024);
        if ( this.blockwiseStatusLifetime != null ) config.setInt(NetworkConfig.Keys.BLOCKWISE_STATUS_LIFETIME, Integer.valueOf( this.blockwiseStatusLifetime )); // 10 * 60 * 1000); // ms

        if ( this.notificationCheckIntervalTime != null ) config.setLong(NetworkConfig.Keys.NOTIFICATION_CHECK_INTERVAL_TIME, Long.valueOf( this.notificationCheckIntervalTime )); // 24 * 60 * 60 * 1000); // ms
        if ( this.notificationCheckIntervalCount != null ) config.setInt(NetworkConfig.Keys.NOTIFICATION_CHECK_INTERVAL_COUNT, Integer.valueOf( this.notificationCheckIntervalCount )); // 100);
        if ( this.notificationReregistrationBackoff != null ) config.setLong(NetworkConfig.Keys.NOTIFICATION_REREGISTRATION_BACKOFF, Long.valueOf( this.notificationReregistrationBackoff )); // 2000); // ms
                
        if ( this.useCongestionControl != null ) config.setBoolean(NetworkConfig.Keys.USE_CONGESTION_CONTROL, Boolean.valueOf( this.useCongestionControl )); // false);
        if ( this.congestionControlAlgorithm != null ) config.setString(NetworkConfig.Keys.CONGESTION_CONTROL_ALGORITHM, String.valueOf( this.congestionControlAlgorithm )); // "Cocoa"); // see org.eclipse.californium.core.network.stack.congestioncontrol
                
        if ( this.protocolStageThreadCount != null ) config.setInt(NetworkConfig.Keys.PROTOCOL_STAGE_THREAD_COUNT, Integer.valueOf( this.protocolStageThreadCount )); // CORES);
        if ( this.networkStageReceiverThreadCount != null ) config.setInt(NetworkConfig.Keys.NETWORK_STAGE_RECEIVER_THREAD_COUNT, Integer.valueOf( this.networkStageReceiverThreadCount )); // WINDOWS ? CORES : 1);
        if ( this.networkStageSenderThreadCount != null ) config.setInt(NetworkConfig.Keys.NETWORK_STAGE_SENDER_THREAD_COUNT, Integer.valueOf( this.networkStageSenderThreadCount )); // WINDOWS ? CORES : 1);
                
        if ( this.udpConnectorDatagramSize != null ) config.setInt(NetworkConfig.Keys.UDP_CONNECTOR_DATAGRAM_SIZE, Integer.valueOf( this.udpConnectorDatagramSize )); // 2048);
        if ( this.udpConnectorReceiveBuffer != null ) config.setInt(NetworkConfig.Keys.UDP_CONNECTOR_RECEIVE_BUFFER, Integer.valueOf( this.udpConnectorReceiveBuffer )); // UDPConnector.UNDEFINED);
        if ( this.udpConnectorSendBuffer != null ) config.setInt(NetworkConfig.Keys.UDP_CONNECTOR_SEND_BUFFER, Integer.valueOf( this.udpConnectorSendBuffer )); // UDPConnector.UNDEFINED);
        if ( this.udpConnectorOutCapacity != null ) config.setInt(NetworkConfig.Keys.UDP_CONNECTOR_OUT_CAPACITY, Integer.valueOf( this.udpConnectorOutCapacity )); // Integer.MAX_VALUE); // unbounded

        if ( this.deduplicator != null ) config.setString(NetworkConfig.Keys.DEDUPLICATOR, String.valueOf( this.deduplicator )); // NetworkConfig.Keys.DEDUPLICATOR_MARK_AND_SWEEP);
        if ( this.markAndSweepInterval != null ) config.setLong(NetworkConfig.Keys.MARK_AND_SWEEP_INTERVAL, Long.valueOf( this.markAndSweepInterval )); // 10 * 1000);
        if ( this.cropRotationPeriod != null ) config.setInt(NetworkConfig.Keys.CROP_ROTATION_PERIOD, Integer.valueOf( this.cropRotationPeriod )); // 2000);

        if ( this.httpPort != null ) config.setInt(NetworkConfig.Keys.HTTP_PORT, Integer.valueOf( this.httpPort )); // 8080);
        if ( this.httpServerSocketTimeout != null ) config.setInt(NetworkConfig.Keys.HTTP_SERVER_SOCKET_TIMEOUT, Integer.valueOf( this.httpServerSocketTimeout )); // 100000);
        if ( this.httpServerSocketBufferSize != null ) config.setInt(NetworkConfig.Keys.HTTP_SERVER_SOCKET_BUFFER_SIZE, Integer.valueOf( this.httpServerSocketBufferSize )); // 8192);
        if ( this.httpCacheResponseMaxAge != null ) config.setInt(NetworkConfig.Keys.HTTP_CACHE_RESPONSE_MAX_AGE, Integer.valueOf( this.httpCacheResponseMaxAge )); // 86400);
        if ( this.httpCacheSize != null ) config.setInt(NetworkConfig.Keys.HTTP_CACHE_SIZE, Integer.valueOf( this.httpCacheSize )); // 32);
                
        if ( this.healthStatusPrintLevel != null ) config.setString(NetworkConfig.Keys.HEALTH_STATUS_PRINT_LEVEL, String.valueOf( this.healthStatusPrintLevel )); // "FINEST");
        if ( this.healthStatusInterval != null ) config.setInt(NetworkConfig.Keys.HEALTH_STATUS_INTERVAL, Integer.valueOf( this.healthStatusInterval )); // 60); // s
        return config;
    }

  

}
