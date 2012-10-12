package net.medcommons.modules.cxp.protocol;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;

import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.HttpSecureProtocol;
import org.apache.commons.ssl.SSLClient;
import org.apache.commons.ssl.TrustMaterial;
import org.apache.log4j.Logger;
import org.codehaus.xfire.transport.http.EasySSLProtocolSocketFactory;

import net.medcommons.modules.utils.CountInputStream;
import net.medcommons.modules.utils.CountOutputStream;

/**
 * Metered wrapper around SSL socket created by EasySSLProtocolSocketFactory.
 * 
 * @author sdoyle
 *
 */
public class MeteredSSLSocket extends Socket implements MeteredIO{

	private static Logger log = Logger.getLogger(MeteredSSLSocket.class);
	
	private CountInputStream inputStream = null;
	private CountOutputStream outputStream = null;
	private ProtocolSocketFactory easy = new EasySSLProtocolSocketFactory();
	private ThreadLocal<SSLClient> factory = new ThreadLocal<SSLClient>();
	
	private boolean closed = false;
	
	Socket socket = null;
	
	public MeteredSSLSocket(String host,
	        int port,
	        InetAddress localAddress,
	        int localPort) throws IOException {
	    
	    createSocketFactory();
        
		socket = factory.get().createSocket(host, port, localAddress, localPort);
		log.info("Created SSL socket");
		
	}
	public MeteredSSLSocket(String host, int port) throws IOException{
	    
	    createSocketFactory();
		socket = factory.get().createSocket(host, port);
	}
	
	private static String [] certs = new String[] {
	    "startcom/ca.pem",
	    "startcom/sub.class2.server.ca.pem"
	};
    private void createSocketFactory() throws IOException {
        if(factory.get() == null) {
	        try {
	            
	            SSLClient sslClient = new SSLClient();
	            // Hack workaround - trust all certs
	            // sslClient.addTrustMaterial(TrustMaterial.TRUST_ALL);
	            for(String cert : certs) {
		            InputStream certStream = getClass().getClassLoader().getResourceAsStream(cert);
		            if(certStream != null) {
		                log.info("Adding trusted certificate: " + cert);
		                sslClient.addTrustMaterial(new TrustMaterial(certStream));
		            }
	            }
	            sslClient.setCheckCRL(false);
	            sslClient.setCheckHostname(false);
	            factory.set(sslClient);
	        }
	        catch (GeneralSecurityException e) {
	            throw new IOException(e);
	        }
	    }
    }
	public void cancelStream(){
		
		if (inputStream != null){
			inputStream.cancelStream();
		}
		if (outputStream != null){
			outputStream.cancelStream();
		}
	}
	public InputStream getInputStream() throws IOException{
		if (inputStream == null){
			InputStream in = socket.getInputStream();
			inputStream = new CountInputStream(in);
		}
		//log.info(this + " has input stream " + inputStream);
		return(inputStream);
	}
	
	public OutputStream getOutputStream() throws IOException{
		if (outputStream == null){
			OutputStream out = socket.getOutputStream();
			outputStream = new CountOutputStream(out);
		}
		//log.info(this + " has output stream " + outputStream);
		return(outputStream);
	}
	
	public long getInputBytes(){
		if (inputStream == null) return 0;
		else{
			if (log.isDebugEnabled() && !closed)
					log.debug("getInputBytes:" + inputStream.getByteCount());
			return(inputStream.getByteCount());
		}
	}
	public long getOutputBytes(){
		if (outputStream == null) return 0;
		else{
			if (log.isDebugEnabled() && !closed)
					log.debug("getOutputBytes:" + outputStream.getByteCount());
			return(outputStream.getByteCount());
		}
	}
	public void close() throws IOException{
		super.close();
		socket.close();
		closed = true;
		if (outputStream != null){
			outputStream.close();
		}
		if (inputStream != null){
			inputStream.close();
		}
	}
}
