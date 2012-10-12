/*
 * $Id: InsufficientCreditException.java 2622 2008-05-27 05:49:47Z ssadedin $
 * Created on 22/01/2007
 */
package net.medcommons.modules.services.interfaces;

/**
 * Thrown if an operation is attempted which requires payment but
 * for which no credit is available.
 * 
 * @author ssadedin
 */
public class InsufficientCreditException extends ServiceException {

    public InsufficientCreditException() {
    }

    public InsufficientCreditException(String message) {
        super(message);
    }

    public InsufficientCreditException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientCreditException(Throwable cause) {
        super(cause);
    }

}
