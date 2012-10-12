/*
 * $Id$
 * Created on 01/09/2006
 */
package net.medcommons.router.services.ccr;

/**
 * Indicates a problem storing a CCR
 * 
 * @author ssadedin
 */
public class CCRStoreException extends Exception {

    public CCRStoreException() {
    }

    public CCRStoreException(String message) {
        super(message);
    }

    public CCRStoreException(Throwable cause) {
        super(cause);
    }

    public CCRStoreException(String message, Throwable cause) {
        super(message, cause);
    }

}
