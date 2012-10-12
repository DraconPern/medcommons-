package net.medcommons.modules.cxp.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import org.apache.log4j.Logger;

import net.medcommons.modules.utils.CountInputStream;
import net.medcommons.modules.utils.CountOutputStream;
/**
 * A socket where the input and output stream byte count is
 * calculated. Otherwise - a vanilla socket.
 * 
 * @author mesozoic
 *
 */
public class MeteredSocket extends Socket implements MeteredIO{

	private static Logger log = Logger.getLogger(MeteredSocket.class);
	private CountInputStream inputStream = null;
	private CountOutputStream outputStream = null;
	boolean closed = false;
	
	
	public MeteredSocket(){
		super();
	}
	
	public MeteredSocket(String host,
	        int port,
	        InetAddress localAddress,
	        int localPort) throws IOException{
		super(host, port, localAddress, localPort);
		
		
	}
	public MeteredSocket(String host, int port) throws IOException{
		super(host, port);
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
			InputStream in = super.getInputStream();
			inputStream = new CountInputStream(in);
		}
		//log.info(this + " has input stream " + inputStream);
		return(inputStream);
	}
	
	public OutputStream getOutputStream() throws IOException{
		if (outputStream == null){
			OutputStream out = super.getOutputStream();
			outputStream = new CountOutputStream(out);
		}
		//log.info(this + " has output stream " + outputStream);
		return(outputStream);
	}
	
	public long getInputBytes(){
		if (inputStream == null) return 0;
		else{
		    if(!closed)
				log.debug("getInputBytes:" + inputStream.getByteCount());
			return(inputStream.getByteCount());
		}
	}
	public long getOutputBytes(){
		if (outputStream == null) return 0;
		else{
		    if(!closed)
				log.debug("getOutputBytes:" + outputStream.getByteCount());
			return(outputStream.getByteCount());
		}
	}
	public void close() throws IOException{
		super.close();
		closed = true;
		if (outputStream != null){
			outputStream.close();
		}
		if (inputStream != null){
			inputStream.close();
		}
	}
}
