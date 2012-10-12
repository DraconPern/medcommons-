package net.medcommons.modules.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMAC {
	
	private SecretKeySpec hmac = null;
	private Mac mac = null;
	
	/**
	 * Returns a HMAC for a given key. This object may be used for repeated
	 * HMAC calculations. 
	 * TODO: how many bytes in the key? 32?
	 * @param key
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public HMAC(byte [] key)throws NoSuchAlgorithmException, InvalidKeyException{
		
		hmac = new SecretKeySpec(key, "HmacSHA1" );
		
		mac  = Mac.getInstance(hmac.getAlgorithm());
		mac.init(hmac);
	}
	/**
	 * Calculates the digest of a particular message.
	 * @param message
	 * @return
	 */
	public synchronized byte [] calculateDigest(String message){
		mac.reset();
		byte[] digest = mac.doFinal(message.getBytes());
		return(digest);
	}
	
	
}
