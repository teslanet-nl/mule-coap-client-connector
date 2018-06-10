package nl.teslanet.mule.transport.coap.client.error;

/**
 * MalformedUriException is thrown when a CoAP uri is invalid or cannot be constructed
 * from schema, host, port. parth and query parameters to form a valid uri.
 *
 */
public class MalformedUriException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID= 1L;

    public MalformedUriException()
    {
        super();
    }

    public MalformedUriException( String message )
    {
        super( message );
    }

    public MalformedUriException( Throwable cause )
    {
        super( cause );
    }

    public MalformedUriException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public MalformedUriException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }

}
