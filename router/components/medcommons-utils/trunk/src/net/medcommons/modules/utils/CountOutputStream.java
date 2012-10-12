package net.medcommons.modules.utils;

import static java.lang.Math.min;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class CountOutputStream extends FilterOutputStream {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CountOutputStream.class);
    
    /**
     * The maximum write block size.  This is limited to provide better resolution
     * to progress information.  In some cases the HttpClient will dump a large amount of 
     * data into a single write (eg. 1MB).  This causes the socket to block for the whole
     * time that the data is getting written and during that time there is no change to
     * the progress.  To avoid that we break the sends into blocks.
     */
    private static final int MAX_WRITE = 64 * 1024;
	
	boolean inWrite = false;
	long byteCount = 0;
	OutputStream out;
	boolean cancelStream = false;
	private long startTime = System.currentTimeMillis();
	
	private static int throttleRate  = -1;

    private int bytesPerSecond;
	
	static {
	   String throttleValue = System.getProperty("medcommons.cxp.throttleDelay"); 
	   if(throttleValue != null) {
	       throttleRate = Integer.parseInt(throttleValue);
	       log.info("Using throttle delay " + throttleRate + " bytes per second in transfer operations");
	   }
	}
		
	public CountOutputStream(OutputStream out){
		super(out);
		this.out = out;
	}
	
	/**
	 * Throws a CancelledStreamException on the next write().
	 *
	 */
	public void cancelStream(){
		cancelStream = true;
	}
	public void close() throws IOException{
		
		super.close();
		out.close();
		
	}
	public void flush() throws IOException{
		super.flush();
		out.flush();
	}
	public void write(byte[] b) throws IOException{
		//int off = 0;
		//int len = b.length;
		//this.write(b, off, len);
	    /*
		boolean saveWrite = inWrite;
		
		checkCancel();
		try{
			out.write(b);
			if (!saveWrite){
				byteCount+=b.length;
			}
			throttle();
		}
		finally{
			inWrite = saveWrite;
		}
		*/
	    this.write(b,0,b.length);
	}
	public void write(byte[] b, int off, int len) throws IOException{
		boolean saveWrite = inWrite;
		checkCancel();
		
        // if(Math.random() < 0.7)
        //    throw new IOException("Simulated I/O Failure to test retries");

		try {
		    for(int i = 0; i<len; i+=MAX_WRITE) {
				int writeLen = min(MAX_WRITE, len-i);
                out.write(b,off+i,writeLen);
	            if (!saveWrite){
	                byteCount+=writeLen;
	            }
	            checkCancel();
				throttle();
		    }
			
		}
		finally{
			inWrite = saveWrite;
		}
	}

    private void checkCancel() {
        if (cancelStream){
			try{close();} catch(Exception e){;}
			throw new CancelledStreamException("Stream Cancelled");
		}
    }
	public void write(int b) throws IOException{
		boolean saveWrite = inWrite;
		checkCancel();
		try{
			out.write(b);
			
			if (!saveWrite){
				byteCount++;
			}
			throttle();
		}
		finally{
			inWrite = saveWrite;
		}
	}
	public long getByteCount(){
		return(this.byteCount);
	}
	
    private void throttle() {
        bytesPerSecond = (int) (byteCount / (((System.currentTimeMillis()-startTime) / 1000)+1));
        if(log.isDebugEnabled())
	        log.debug("rate = " + bytesPerSecond + " bytes per second");
        
        while(throttleRate > 0 && byteCount > 0 &&  bytesPerSecond > throttleRate ) {
            try { log.info("throttle"); Thread.sleep(200); } catch (InterruptedException e) { }
            bytesPerSecond = (int) (byteCount / (((System.currentTimeMillis()-startTime) / 1000)+1));
        }
    }
}
