package net.medcommons.modules.crypto.io;



import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;


import net.medcommons.modules.crypto.SHA1;

/**
 * SHA1InputStream calculates the SHA-1 hash of the stream as a side effect.
 * It calculates the SHA-1 hash of the stream's contents.
 *
 * @author Sean Doyle
 */
public class SHA1InputStream extends FilterInputStream {
	/**
	 * Logger to use with this class
	 */
	private static Logger log =  Logger.getLogger(SHA1InputStream.class);

    long counter = 0;
    SHA1 sha1 = null;
	
    /**
     * Constructs a new SHA1InputStream.
     * @param in InputStream to delegate 
     */
    public SHA1InputStream(InputStream in) throws 
    	NoSuchAlgorithmException{
        super(in);
        sha1 = new SHA1();
        counter = 0;
        sha1.initializeHashStreamCalculation();
    }

    public void setLogger(Logger log){
    	this.log = log;
    }
    /**
     * Increases the count by super.read(b)'s return count
     * 
     * @see java.io.InputStream#read(byte[]) 
     */
    public int read(byte[] b) throws IOException {
    	
        int found = read(b, 0, b.length);
        //log.info("read(byte[]b) returned length " + found);
        return found;
    }

    /**
     * Increases the count by super.read(b, off, len)'s return count
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
    	try{
	        int found = super.read(b, off, len);
	        if (found >0){
	        	sha1.updateHashStreamCalculation(b, off, found);
	        	counter += found;
	        }
	        //log.info("read[b] - length of b is " + b.length + ", offset =" + off + ", len=" + len + ", found = " + found);
	       // log.info("Counter = " + counter);
	        return found;
    	}
    	catch(IOException e){
    		log.error("Error reading stream. Counter = " + counter + ", offset = " + off + ", length = " + len);
    		throw e;
    	}
    }

    /**
     * Increases the count by 1 if a byte is successfully read. 
     *
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
    	byte b[] = new byte[1];
        int found = read(b, 0, 1);
       // log.info("read() returned length " + found);
        return found;
    }
    
    /**
     * 
     * Undefined side-effect on SHA1 calculation.
     * 
     * @see java.io.InputStream#skip(long)
     */
    public long skip(final long length) throws IOException {
        final long skip = super.skip(length);
        // Skip is not defined operation when calculating the 
        // SHA1 hash. For the moment just write out an error message.
        // Perhaps an exception should be thrown.
        log.error("SHA1InputStream: skip not defined " + length);

        return skip;
    }

    /**
     * The number of bytes that have passed through this stream.
     *
     * @return the number of bytes accumulated
     */
    public String getHash() {
        return (sha1.returnHashStreamCalculation());
    }

    public long getLength(){
    	return(counter);
    }

}
