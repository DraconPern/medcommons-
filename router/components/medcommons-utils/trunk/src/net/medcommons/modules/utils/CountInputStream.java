package net.medcommons.modules.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * Simple filter stream for counting bytes being read.
 * Assumes a single thread is doing the read (see flag inRead below).
 * 
 * @author mesozoic
 *
 */
public class CountInputStream extends FilterInputStream{
	private static Logger log = Logger.getLogger(CountInputStream.class);
	InputStream inputStream = null;
	boolean inRead = false;
	private long byteCount = 0;
	
	
	private long startTime = System.currentTimeMillis();
	
	private boolean cancelStream = false;
	
	private static int throttleRate  = -1;
	static {
	   String throttleValue = System.getProperty("medcommons.cxp.throttleDelay"); 
	   if(throttleValue != null) {
	       throttleRate = Integer.parseInt(throttleValue);
	       log.info("Using throttle delay " + throttleRate + " in transfer operations");
	   }
	}
	
	public long getByteCount(){
		return(this.byteCount);
	}
	public CountInputStream(InputStream inputStream){
		super(inputStream);
		this.inputStream = inputStream;
	}
	/**
	 * Throws a CancelledStreamException on the next read().
	 *
	 */
	public void cancelStream(){
		cancelStream = true;
	}
	public int read() throws IOException{
		boolean saveRead = inRead;
		if (cancelStream){
			try{close();} catch(Exception e){;}
			throw new CancelledStreamException("Stream Cancelled");
		}
		inRead = true;
		int found = -1;
		try{
			found=in.read();
			if(byteCount % 1000 == 0) {
			    log.info("counted " + byteCount + " bytes");
			}
			
			throttle();
			if (!saveRead){
				byteCount+=found;
				
			}
		}
		finally{
			inRead = saveRead;
		}
		//log.info("read() " + found+ " byteCount = " + byteCount + ", found=" + found + ":" + this);
		return(found);
	}
    private void throttle() {
        if(throttleRate > 0 && byteCount > 0 && byteCount / (((System.currentTimeMillis()-startTime) / 1000)+1) > throttleRate ) {
            try { Thread.sleep(100); } catch (InterruptedException e) { }
        }
    }
	public int read(byte[] b) throws IOException{
		int found = -1;
		boolean saveRead = inRead;
		if (cancelStream){
			try{close();} catch(Exception e){;}
			throw new CancelledStreamException("Stream Cancelled");
		}
		inRead = true;
		try{
			found = in.read(b);
			if (!saveRead){
				byteCount+=found;
				//log.info("read(byte[] b) " + found+ " transferbytes = " + transferByteCount);
				
			}
			throttle();
		}
		finally{
			inRead = saveRead;
		}
		//log.info("read(byte[] b) " + found+ " byteCount = " + byteCount + ", found="  + ":" + this);
        return(found);
	}
	public int read(byte[] b, int off, int len) throws IOException {
		int found = -1;
		boolean saveRead = inRead;
		if (cancelStream){
			try{close();} catch(Exception e){;}
			throw new CancelledStreamException("Stream Cancelled");
		}
		inRead = true;
		
		try{
			found = in.read(b, off, len);
	        if (!saveRead){
	        	byteCount+=found;
	        	
	        }
			throttle();
		}
		finally{
			inRead = saveRead;
		}
		//log.info("read(byte[] b, int off, int len) " + found + " byteCount = " + byteCount + ", delta = " + found  + ":" + this);
        return(found);
	}
}
