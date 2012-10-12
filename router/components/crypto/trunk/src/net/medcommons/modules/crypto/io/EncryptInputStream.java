package net.medcommons.modules.crypto.io;



import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;


/**
 * EncryptInputStream encrypts the data as it passes using AES.
 * It encrypts the data with AES.
 *
 * @author Sean Doyle
 */
public class EncryptInputStream extends FilterInputStream {
	

    
	Key encryptionKey = null;
	CipherInputStream cipherIs = null;
	Cipher		encrypt = null;
	final static int IV_SIZE = 16;
	SecureRandom iVectorGenerator = null;
	
    /**
     * Constructs a new EncryptInputStream.
     * @param in InputStream to delegate to
     */
    public EncryptInputStream(InputStream in, Key encryptionKey) throws 
    	NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
        super(in);
        this.encryptionKey = encryptionKey;
        
    	iVectorGenerator = new SecureRandom();
		iVectorGenerator.generateSeed(IV_SIZE);
     
        byte [] iv = generateInitializationVector();
		
         encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
         
         encrypt.init(Cipher.ENCRYPT_MODE, encryptionKey, new IvParameterSpec (iv));
         
      
    }

    /**
	 * Returns a random 16 byte initialization vector.
	 * @return
	 */
	private byte[] generateInitializationVector(){
		byte buff[] = new byte[IV_SIZE];
		iVectorGenerator.nextBytes(buff);
		return(buff);
	}
    /**
     * Increases the count by super.read(b)'s return count
     * 
     * @see java.io.InputStream#read(byte[]) 
     */
    public int read(byte[] b) throws IOException {
    	
        int found = super.read(b); // Is 'b' changed by the read?
        if (found > 0)
        	;//
        return found;
    }

    /**
     * Increases the count by super.read(b, off, len)'s return count
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int found = super.read(b, off, len);
        if (found >0)
        	;//
        return found;
    }

    /**
     * Increases the count by 1 if a byte is successfully read. 
     *
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        int found = super.read();
        if (true) throw new IllegalArgumentException("Read without parameters not yet supported");
        //this.count += (found >= 0) ? 1 : 0;
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
        return skip;
    }

   
    

}
