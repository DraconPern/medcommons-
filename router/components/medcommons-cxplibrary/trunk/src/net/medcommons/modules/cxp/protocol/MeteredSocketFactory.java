package net.medcommons.modules.cxp.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import net.medcommons.modules.cxp.client.CXPClient;
import net.medcommons.modules.cxp.client.MeteredSocketListener;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.*;
import org.apache.log4j.Logger;

/**
 * Creates sockets that can be metered for I/O.
 * Note very fragile - creates regular sockets if timeout parameters are 
 * set.
 * @author mesozoic
 *
 */
public class MeteredSocketFactory extends DefaultProtocolSocketFactory{
    
	private static Logger log = Logger.getLogger(MeteredSocketFactory.class.getName());
	
	/**
	 * We use a thread local here because the socket factory once registered
	 * handles all http / https requests that route through the 
	 * apache framework.  If we used a single global client then 
	 * we would mix up overlapping requests from cxp clients or in fact
	 * anything that uses apache http support.
	 */
	private ThreadLocal<MeteredSocketListener> meteredSocketListener = new ThreadLocal<MeteredSocketListener>();
	
	 /**
     * The factory singleton.
     */
    private static final MeteredSocketFactory factory = new MeteredSocketFactory();
    
    /**
     * Gets an singleton instance of the DefaultProtocolSocketFactory.
     * @return a DefaultProtocolSocketFactory
     */
    static MeteredSocketFactory getSocketFactory() {
        return factory;
    }
    
    public void register(MeteredSocketListener listener) {
        MeteredSocketFactory metered = new MeteredSocketFactory();
        metered.setListener(listener);
        Protocol protocol = new Protocol("http", metered, 80);
        Protocol.registerProtocol("http", protocol);
    }
    
    public static void register(String protocol, MeteredSocketListener listener) {
        if("https".equals(protocol)) {
            MeteredSSLSocketFactory.sslFactory.register(listener);
        }
        else
        if("http".equals(protocol)) {
            factory.register(listener);
        }
        else {
			log.fatal("Unsupported protocol:" + protocol);
			throw new RuntimeException("Unsupported protocol " + protocol);
        }
    }
    
    /**
     * Constructor for DefaultProtocolSocketFactory.
     */
    protected MeteredSocketFactory() {
        super();
       
    }

    public void setListener(MeteredSocketListener MeteredSocketListener){
    	 this.meteredSocketListener.set(MeteredSocketListener);
    	 log.info(this + " has MeteredSocketListener " + MeteredSocketListener);
    }
    
    /**
     * @see #createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(
        String host,
        int port,
        InetAddress localAddress,
        int localPort
    ) throws IOException, UnknownHostException {
        
    	log.info("Creating socket 1" + host + " " + port + " " + localAddress + " " + localPort);
    	Socket socket = createMeteredSocket(host, port, localAddress, localPort);
    	MeteredSocketListener client = meteredSocketListener.get();
        if (client != null){
    		client.setMeteredSocket((MeteredIO) socket);
    	}
    	else{
    		log.debug("MeteredSocketListener is null; not initializing callback to MeteredSocketListener");
    	}
        return socket; 
        
    }

    protected Socket createMeteredSocket(String host, int port, InetAddress localAddress, int localPort)
            throws IOException {
        return new MeteredSocket(host, port, localAddress, localPort);
    }
    
    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p>
     * This method employs several techniques to circumvent the limitations of older JREs that 
     * do not support connect timeout. When running in JRE 1.4 or above reflection is used to 
     * call Socket#connect(SocketAddress endpoint, int timeout) method. When executing in older 
     * JREs a controller thread is executed. The controller thread attempts to create a new socket
     * within the given limit of time. If socket constructor does not return until the timeout 
     * expires, the controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *  
     * @param host the host name/IP
     * @param port the port on the host
     * @param localAddress the local host name/IP to bind the socket to
     * @param localPort the port on the local machine
     * @param params {@link HttpConnectionParams Http connection parameters}
     * 
     * @return Socket a new socket
     * 
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     * @throws ConnectTimeoutException if socket cannot be connected within the
     *  given time limit
     * 
     * @since 3.0
     */
    public Socket createSocket(
        final String host,
        final int port,
        final InetAddress localAddress,
        final int localPort,
        final HttpConnectionParams params
    ) throws IOException, UnknownHostException, ConnectTimeoutException {
        
    	log.debug("Creating socket 2" + host + " " + port + " " + localAddress + " " + localPort);
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            return createSocket(host, port, localAddress, localPort);
        } else {
        	log.info("Not creating a metered socket - timeout specified is " + timeout);
        	// TBD.
            // To be eventually deprecated when migrated to Java 1.4 or above
            Socket socket = ReflectionSocketFactory.createSocket(
                "javax.net.SocketFactory", host, port, localAddress, localPort, timeout);
            if (socket == null) {
                socket = ControllerThreadSocketFactory.createSocket(
                    this, host, port, localAddress, localPort, timeout);
            }
            return socket;
        }
    }

    /**
     * @see ProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException {
    	log.info("Creating socket 3" + host + " " + port);
    	Socket socket = createMeteredSocket(host, port);
    	MeteredSocketListener client = meteredSocketListener.get();
        if (client != null){
    		client.setMeteredSocket((MeteredIO) socket);
    	}
    	else{
    		log.debug("MeteredSocketListener is null; not initializing callback to MeteredSocketListener");
    	}
        return socket;
    }
    
    protected Socket createMeteredSocket(String host, int port) throws IOException {
        return new  MeteredSocket(host, port);
    }

    /**
     * All instances of DefaultProtocolSocketFactory are the same.
     */
    public boolean equals(Object obj) {
        return ((obj != null) && obj.getClass().equals(this.getClass()));
    }

    /**
     * All instances of DefaultProtocolSocketFactory have the same hash code.
     */
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
