/*
 * $Id: EncryptionException.java 2971 2008-10-21 06:47:21Z ssadedin $
 * Created on 21/10/2008
 */
package net.medcommons.modules.crypto;

/**
 * Indicates a problem occurred while encrypting content
 * 
 * @author ssadedin
 */
public class EncryptionException extends Exception {

    public EncryptionException() {
    }

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(Throwable cause) {
        super(cause);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

}
