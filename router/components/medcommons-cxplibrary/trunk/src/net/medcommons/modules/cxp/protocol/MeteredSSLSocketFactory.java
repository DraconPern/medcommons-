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
 * Extension of {@link MeteredSocketFactory} that
 * creates SSL Sockets.
 * 
 * @author mesozoic
 * @author ssadedin
 */
public class MeteredSSLSocketFactory extends MeteredSocketFactory {
    
	private static Logger log = Logger.getLogger(MeteredSocketFactory.class.getName());

    static final MeteredSSLSocketFactory sslFactory = new MeteredSSLSocketFactory();
	
    @Override
    protected Socket createMeteredSocket(String host, int port, InetAddress localAddress, int localPort)
            throws IOException {
        return new MeteredSSLSocket(host, port, localAddress, localPort);
    }

    @Override
    protected Socket createMeteredSocket(String host, int port) throws IOException {
        return new  MeteredSSLSocket(host, port);
    }

    @Override
    public void register(MeteredSocketListener listener) {
        MeteredSSLSocketFactory metered = new MeteredSSLSocketFactory();
        metered.setListener(listener);
        Protocol protocol = new Protocol("https", metered, 443);
        Protocol.registerProtocol("https", protocol);
    }
}
