/*
 * $Id: OAuthException.java 2349 2008-01-18 05:04:31Z ssadedin $
 * Created on 17/01/2008
 */
package net.medcommons.security;

/**
 * Indicates an unexpected system failure during OAuth verification.
 * 
 * @author ssadedin
 */
public class OAuthException extends Exception {

    /**
     * 
     */
    public OAuthException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public OAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public OAuthException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public OAuthException(Throwable cause) {
        super(cause);
    }

}
