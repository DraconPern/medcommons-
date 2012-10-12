/*
 *  Copyright 2005 MedCommons Inc.   All Rights Reserved.
 * 
 * Created on Jun 1, 2005
 *
 * 
 */
package net.medcommons.modules.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;



/**
 * Utility class which calculates the SHA-1 hash of a set of bytes or a file.
 * Files are processed BUFFERSIZE (typically 8K) chunks at a time.
 * 
 * @author sean
 *
 */
public class SHA1 {
	// Arbitrary: might want to test different values to test speed. Value makes no difference
	// to computed values.
	private final static int BUFFERSIZE = 64*1024;
	
	private final static Logger log = Logger.getLogger(SHA1.class);
	private MessageDigest sha = null;
	public SHA1(){
	}
	
	/**
	 * Reads file in chunks; calculates SHA-1 hash of bytes in file.
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public byte[]calculateHash(File input) throws IOException {
		if (!input.exists())
			throw new FileNotFoundException(input.getAbsolutePath());
		
		FileInputStream in = new FileInputStream(input);
		try {
		    byte[] result = calculateStreamHash(in);
		    return result;
		}
		finally {
		    if(in != null) in.close();
		}
	}
	
    public byte[] calculateStreamHash(InputStream in) throws IOException {
        if (sha == null)
			throw new NullPointerException("Not initialized; must call initializeHashStreamCalculation() before this routine");
		byte buff [] = new byte[BUFFERSIZE];
		int nBytes = 0;
		while ((nBytes = in.read(buff)) != -1){
				sha.update(buff, 0, nBytes);
		}
	    byte[] hash = sha.digest();
	    return(hash);
    }
    
    public String calculateHashString(InputStream in) throws IOException {
        byte[] hash = calculateStreamHash(in);
        String hashStr = Utils.encodeBytes( hash, 0, hash.length);
        return hashStr;
    }
	
	/**
	 * Calculates SHA-1 hash of an array of bytes.
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public  byte[]calculateHash(byte [] buff) {
		
		if (sha == null)
			throw new NullPointerException("Not initialized; must call initializeHashStreamCalculation() before this routine");
		sha.update(buff, 0, buff.length);
		
	    byte[] hash = sha.digest();
	    
	    return(hash);
	}
	/**
	 * Calculates the SHA-1 hash of an array of Strings. The hash is calculated from
	 * the bytes of the string elements in the order that they are in the array.
	 * @param a
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public  byte[]calculateHash(String a[]) {
		if (sha == null)
			throw new NullPointerException("Not initialized; must call initializeHashStreamCalculation() before this routine");
		for (int i=0;i<a.length; i++){
			byte b[] = a[i].getBytes();
			sha.update(b, 0, b.length);			
		}
		
	    byte[] hash = sha.digest();
	    return(hash);
	}
	
	/**
	 * Returns the SHA-1 hash of the bytes within the specified file. Throws an exception if the file
	 * does not exist or if the file is empty.
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public  String calculateFileHash(File input) throws NoSuchAlgorithmException, IOException{
		if (sha == null)
			throw new NullPointerException("Not initialized; must call initializeHashStreamCalculation() before this routine");
		if (!input.exists())
			throw new IOException("File does not exist:" + input.getAbsolutePath());
		if (input.length() <1){
			throw new IOException("File has no contents:" + input.getAbsolutePath());
		}
		byte hash[] = calculateHash(input);
		String hashStr = Utils.encodeBytes(hash, 0, hash.length);
		return(hashStr);
	}
	
	public  String calculateByteHash(byte[] input) {
		byte hash[] = calculateHash(input);
		return(Utils.encodeBytes(hash, 0, hash.length));
	}
	
	/**
	 * Calculates the SHA-1 hash of the bytes of each String in an array.
	 * @param a
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public  String calculateStringNameHash(String a[]) {
		byte hash[] = calculateHash(a);
		return(Utils.encodeBytes(hash, 0, hash.length));
	}
	
	public String calculateStringHash(String a) {
	    return calculateStringNameHash( new String[] { a } );
	}
	
	public static String sha1(String a) {
	    SHA1 s = new SHA1();
	    s.initializeHashStreamCalculation();
	    return s.calculateStringHash(a);
	}
	
	/**
	 * Initializes the SHA1 algorithm. Must be called before any other 
	 * method in the class.
	 * 
	 * @return     this - for convenience, fluid method chaining
	 */
	public SHA1 initializeHashStreamCalculation()  {
	    try {
            sha = MessageDigest.getInstance("SHA-1");
            return this;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize SHA1 algorithm");
        }
	}
	public  void updateHashStreamCalculation(byte[]input, int start, int count){
		if (sha == null)
			throw new NullPointerException("Not initialized; must call initializeHashStreamCalculation() before this routine");
		sha.update(input,start, count);
	}
	
	public  String returnHashStreamCalculation(){
		if (sha == null)
			throw new NullPointerException("Not initialized; must call initializeHashStreamCalculation() before this routine");
		byte[] hash = sha.digest();
		
		String hashStr = Utils.encodeBytes(hash, 0, hash.length);
		return(hashStr);
		
	}
	public void reset(){
		if (sha == null)
			throw new NullPointerException("Not initialized; must call initializeHashStreamCalculation() before this routine");
		sha.reset();
	}
	}
