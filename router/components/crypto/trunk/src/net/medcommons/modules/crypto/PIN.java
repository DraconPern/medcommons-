/*
 * $Id$
 * Created on 4/05/2005
 */
package net.medcommons.modules.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * A simple utility class with functions for PINs and their hashes.
 * 
 * @author ssadedin
 */
public class PIN {
    
    public static interface Hasher {
        String hash(String input) throws NoSuchAlgorithmException;
    }
    
    /**
     * The default hasher - simple SHA1 Hasher
     */
    public static Hasher SHA1Hasher = new Hasher() {
        public String hash(String input) throws NoSuchAlgorithmException {
            return PIN.hash(input);
        }        
    };
    
    /**
     * Threadlocal object used to manage hash digest objects
     */
    private static ThreadLocal digests = new ThreadLocal();
    
    private PIN() {
        super();
    }
    
    /**
     * Generates a PIN to access the document.  The PIN has a checksum for simple
     * checking (last digit = sum of other digits).
     * 
     * NOTE: Arguably the PIN should be generated inside the client browser.
     * This method is a stopgap until that is done, or it is decided to do something
     * else.
     * 
     * @return - a randomly generated 5 digit PIN
     */
    public static String generate() {
        // Generate a PIN
        String pin = "";
        int cksum = 0;
        for (int i = 0; i < 4; ++i) {
            int digit = (int) Math.floor(Math.abs(Math.random() * 10));
            pin += digit;
            cksum += digit;
        }
        
        // Last digit is sum of others
        String checkSumString = String.valueOf(cksum);
        pin += checkSumString.charAt(checkSumString.length()-1);
        return pin;
    }


    /**
     * Generates a hash as an SHA-1 digest of the given string, encoded
     * to a url friendly hex format.
     * 
     * @param pin
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String hash(String pin) throws NoSuchAlgorithmException {
        MessageDigest digest = (MessageDigest)digests.get();
        if(digest == null) {
            digest = MessageDigest.getInstance("SHA-1");  
            digests.set(digest);
        }
        byte []  hashBytes = digest.digest(pin.getBytes());
      
        return Utils.hexEncodeBytes(hashBytes, 0, hashBytes.length);
    }    

}
