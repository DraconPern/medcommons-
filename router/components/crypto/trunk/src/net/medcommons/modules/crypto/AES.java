/*
 *  Copyright 2005 MedCommons Inc.   All Rights Reserved.
 * 
 * Created on Jun 21, 2005
 *
 * Originally based on Java Forum's posting by edsonw on March 8, 2005.
 * Much changed since then.
 *
 */
package net.medcommons.modules.crypto;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
/**
 * Contains routines for handling AES encryption and decryption. Can generate/save/restore keys.
 * 
 * TODO need overview
 * 
 * File format of encrypted files follows the RFC 3602 ESP scheme:
 * <pre>
 * The ESP payload is made up of the IV followed by raw cipher-text.
 * Thus the payload field, as defined in [ESP], is broken down according
 * to the following diagram:
 *
 * +---------------+---------------+---------------+---------------+
 * |                                                               |
 * +               Initialization Vector (16 octets)               +
 * |                                                               |
 * +---------------+---------------+---------------+---------------+
 * |                                                               |
 * ~ Encrypted Payload (variable length, a multiple of 16 octets)  ~
 * |                                                               |
 * +---------------------------------------------------------------+
 *
 * The IV field MUST be the same size as the block size of the cipher
 * algorithm being used.  The IV MUST be chosen at random, and MUST be
 * unpredictable.
 *
 * Including the IV in each datagram ensures that decryption of each
 * received datagram can be performed, even when some datagrams are
 * dropped, or datagrams are re-ordered in transit.
 *
 * To avoid CBC encryption of very similar plaintext blocks in different
 * packets, implementations MUST NOT use a counter or other low-Hamming
 * distance source for IVs.
 * </pre>
 * 
 * Note: the format really is:
 * IV (16 octets)
 * length (32 bytes)
 * [SWD notes: looks like length is not written; need to change doc if true]
 * Encrypted payload (variable length, a multiple of 16 octets)
 * @author sean
 *
 */
public class AES {
	
	final static int BUFFER_SIZE = 32*1024; // Arbitrary - need to see if there is an optimum buffer size.
	
	/**
	 * Each file encrypted requires a separate initialization vector.
	 */
	private static SecureRandom iVectorGenerator = null;
	
	/**
	 * 
	 */
	 private static KeyGenerator	keyGen = null;
	
	public final static int IV_SIZE = 16;
	/**
    * Logger to use with this class
    */
    private static Logger log = Logger.getLogger(AES.class);
    
    /**
     * Serialized dummy key (used as placeholder when there is no encryption)
     */
    public static String SERIALIZED_DUMMY_KEY = "NONE";
	
	
	
	public AES(){
	}
	
	/**
	 * Returns a random 16 byte initialization vector.
	 * @return
	 */
	public byte[] generateInitializationVector(){
        if(iVectorGenerator == null) {
            log.debug("new securerandom");
            iVectorGenerator = new SecureRandom();
            log.debug("generating seed");
            iVectorGenerator.generateSeed(IV_SIZE);
        }
		byte buff[] = new byte[IV_SIZE];
		iVectorGenerator.nextBytes(buff);
		return(buff);
	}
	
    /**
     * Generates and returns a new random 128-bit AES key
     * @return
     */
    public Key generateKey() throws NoSuchAlgorithmException{
    	Key key = null;
    	if(keyGen == null){
    		keyGen = KeyGenerator.getInstance("AES");
    		keyGen.init(new SecureRandom());
    	}
        key = keyGen.generateKey();
        return(key);
    }
    
    /**
     * Returns a key that is a constant. Used as a placeholder for the case
     * where encryption is turned off.
     * @return
     */
    public Key generateDummyKey(){
    	return new DummyKey();
    }
    /**
     * Saves the key to the specified file as raw bytes.
     * @param key
     * @param file
     * @throws IOException
     */
    
    public void saveKeyToFile(Key key, File file) throws IOException{
    	byte[] keyBytes = key.getEncoded();
        FileOutputStream fosKey = new FileOutputStream (file);
        fosKey.write (keyBytes);
        fosKey.flush();
        fosKey.close();
    }
   
    /**
     * Returns a hexidecimal string represenation of binary key. The binary 
     * values are mapped to a large integer (BigInteger); the string value is the hex
     * value of this integer.
     * @param key
     * @return
     */
    public String keyToString(Key key){
    	if (isDummyKey(key))
    		return(SERIALIZED_DUMMY_KEY);
    	byte [] keyBytes = key.getEncoded();
    	byte [] encodedBytes = Base64.encodeBase64(keyBytes);

		String encodedString = new String(encodedBytes);
    	
    	
    	//log.info("format:Key" + key.getFormat() + ", base64 is " + encodedString);
    	return(encodedString);
    }
    
    public String keyToString(SecretKeySpec key){
    	
    	if (isDummyKey(key))
    		return(SERIALIZED_DUMMY_KEY);
    	byte [] keyBytes = key.getEncoded();
    	byte [] encodedBytes = Base64.encodeBase64(keyBytes);
    	
    	String encodedString = new String(encodedBytes);
    	
    	//log.info("format:SecretKeySpec" + key.getFormat() + ", base64 is " + encodedString);
	    return(encodedString);
    	
    }
    public String keyToString(DummyKeySpec key){
    	//log.info("key to string for DummyKeySpec");
    	return(SERIALIZED_DUMMY_KEY);
    }
    public String keyToString(DummyKey key){
    	//log.info("key to string for DummyKey");
    	return(SERIALIZED_DUMMY_KEY);
    }
    
  
   
   /**
    * Returns a key given an encoded string.
    */
    public SecretKeySpec getKeyFromString(String encoded){
    	if(SERIALIZED_DUMMY_KEY.equals(encoded)){
    		return new DummyKeySpec();
    	}
    	else{
    		//byte [] bKey = Base64Coder.decode(s).getBytes();
    		byte [] bKey = Base64.decodeBase64(encoded.getBytes());
	    	
	    	SecretKeySpec sks = new SecretKeySpec(bKey, "AES");
	    	return(sks);
    	}
    }
    
    public SecretKeySpec getKeyFromKey(DummyKey k){
    	//log.info("getKeyFromKey:DummyKey");
    	return(new DummyKeySpec());
    }
    public SecretKeySpec getKeyFromKey(Key k){
    	//log.info("getKeyFromKey:Key");
    	SecretKeySpec sks = new SecretKeySpec(k.getEncoded(), k.getAlgorithm());
    	return(sks);
    }
    

    /**
     * Returns decryption parameters (the Initialization Vector iv and the length of the stream)
     * @param dis
     * @return
     * @throws IOException
     */
    private DecryptionParameters getParametersFromStream(DataInputStream dis)throws IOException{
        DecryptionParameters val = new DecryptionParameters();
        
        //log.info("getParametersFromStream:dis="  + dis);
        //log.info(" avail = " + dis.available());
        byte []iv = new byte[16];
        try{
            
            dis.read(iv);
            //long streamLength = dis.readLong();
            val.setIV(iv);
            //val.setStreamLength(streamLength);
            //log.info("get parameters - length = " + streamLength);
        }
        catch(IOException e){
            log.error("Error getting decryption information from input stream.");
            throw e;
        }
        
        return(val);
    }

    public InputStream createInputStream(InputStream is,  Key k) throws EncryptionException {
        try {
            return createInputStream(is, new SecretKeySpec(k.getEncoded(), k.getAlgorithm()));
        }
        catch (InvalidKeyException e) {
            throw new EncryptionException("Unable to create decryptor data using key " + k,e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("Unable to create decryptor data using key " + k,e);
        }
        catch (NoSuchPaddingException e) {
            throw new EncryptionException("Unable to create decryptor data using key " + k,e);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Unable to create decryptor data using key " + k,e);
        }
        catch (IOException e) {
            throw new EncryptionException("Unable to create decryptor data using key " + k,e);
        }
	}
    
    /**
     * Returns decryption parameters (the Initialization Vector iv and the length of the stream)
     * 
     * @param sha1Value - identifier of the individual file
     * @param uidValue- identical to the sha1Value for a simple document; for the series this is 
     *        the name of the series.
     * @return
     * @throws IOException
     */
 



    public InputStream createInputStream(InputStream is,  SecretKeySpec key)
			throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException,
			InvalidKeyException {
        
    	if (key == null){
    		throw new NullPointerException("Null key");
    	}
    
    	byte[] encodedKey = key.getEncoded();
    	if (encodedKey == null)
    		throw new NullPointerException("Null key; can not decrypt input stream");
    	
    	
    	if (isDummyKey(key)){
			log.debug("File not encrypted, returning raw input stream");
    		return(is);
		} else {
    		 log.debug("File encrypted");
    		 
			DataInputStream d = new DataInputStream(is);
			
			DecryptionParameters params = getParametersFromStream(d);
			//log.info("Decrypting input stream - length  = " + params.getStreamLength());
	    	Cipher decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    	
			decrypt.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(params
					.getIV()));
	        
	        CipherInputStream cis = new CipherInputStream (is, decrypt);
	       
	        return(cis);
    	}
    	
    }		
    
    public OutputStream createOutputStream(OutputStream out, Key key) throws EncryptionException {
        try {
            Cipher encrypt = null;
            AES aes = new AES();
            if (key == null) 
                throw new NullPointerException("Null key provided for encrypting output stream");
            
            byte [] iv = aes.generateInitializationVector();
            
            // If a dummy key is supplied then just return the plain input stream
            if(aes.isDummyKey(key)) 
                return out;
                
            encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encrypt.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            
            out.write(iv, 0, AES.IV_SIZE); // Write out the initialization vector as the first 16 bytes.]
            return new CipherOutputStream(out, encrypt);
        }
        catch (InvalidKeyException e) {
            throw new EncryptionException("Unable to encrypt data using key " + key,e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("Unable to encrypt data using key " + key,e);
        }
        catch (NoSuchPaddingException e) {
            throw new EncryptionException("Unable to encrypt data using key " + key,e);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Unable to encrypt data using key " + key,e);
        }
        catch (IOException e) {
            throw new EncryptionException("Unable to encrypt data using key " + key,e);
        }
    }
    
    public InputStream createInputStream(InputStream is,  DummyKeySpec key)
    throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException
    {
    	log.debug("returning unencrypted content...");
    	//DataInputStream d = new DataInputStream(is);
    	//DecryptionParameters params = getParametersFromStream(d);
    	
        
        return(is);
    	
    }	
    
    public boolean isDummyKey(Key key){
    	byte [] encoded = key.getEncoded();
    	
    	return(Arrays.equals(encoded, DummyKeySpec.NONE));
    }
    public boolean isDummyKey(SecretKeySpec key){
    	
    	byte [] encoded = key.getEncoded();
    	return(Arrays.equals(encoded, DummyKeySpec.NONE));
    	
    }
    
    
}

