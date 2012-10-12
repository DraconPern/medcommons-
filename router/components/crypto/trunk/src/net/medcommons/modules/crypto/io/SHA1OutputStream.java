package net.medcommons.modules.crypto.io;




import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import net.medcommons.modules.crypto.SHA1;

import org.apache.log4j.Logger;

/**
 * SHA1OutputStream calculates the SHA-1 hash of the stream as a side effect.
 * It calculates the SHA-1 hash of the stream's contents.
 * TODO: This class is broken; don't use it unless you can verify the results.
 * The problem:
 * Some OutputStreams repeatedly invoke write(int) from the write(byte[], int offset, int len).
 * So -the hash values are corrupt if write(int) or write(byte[]) are invoked. If you can control calls
 * to this class and always invoke write(byte[], offset, length) then the values are correct. 
 * If the hash logic is applied to write(int) then it gets invoked more than once.
 *
 * @author Sean Doyle
 */
public class SHA1OutputStream extends FilterOutputStream {
	/**
	 * Logger to use with this class
	 */
	private static Logger log =  Logger.getLogger(SHA1OutputStream.class);

    long counter = 0;
    SHA1 sha1 = null;
	
    /**
     * Constructs a new SHA1OutputStream.
     * @param out OutputStream to delegate 
     */
    public SHA1OutputStream(OutputStream out) throws 
    	NoSuchAlgorithmException{
        super(out);
        sha1 = new SHA1();
        counter = 0;
        sha1.initializeHashStreamCalculation();
    }

    public void setLogger(Logger log){
    	this.log = log;
    }
    /*
    public void write(int b)
    throws IOException{
    	super.write(b);
    	byte bytes[] = intToByteArray(b);
    	sha1.updateHashStreamCalculation(bytes, 0, 1);
    	counter ++;
    	log.info("write(int b): counter = " + counter);
    	
    }
    */
    /**
     * Increases the count by super.read(b)'s return count
     * 
     * @see java.io.OutputStream#write(byte[]) 
     */
    /*
    public void write(byte[] b) throws IOException {
    	
    	super.write(b);
    	log.info("write(byte[]b): counter = " + counter);
       
      
    }
*/
    /**
     * Increases the count by super.write(b, off, len)'s return count
     *
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        if (len >0){
        	sha1.updateHashStreamCalculation(b, off, len);
        	counter += len;
        }
       // log.info("write(byte[]b, " + off + ", " + len + "): counter = " + counter);
      
    }


    public void flush() throws IOException{
    	super.flush();
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
    private static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

}
