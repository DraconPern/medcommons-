/*
 * $Id: OutOfDateException.java 3158 2008-12-24 09:09:57Z ssadedin $
 * Created on 24/12/2008
 */
package net.medcommons.modules.services.interfaces;

public class OutOfDateException extends ServiceException {

    /**
     * 
     */
    public OutOfDateException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public OutOfDateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public OutOfDateException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public OutOfDateException(Throwable cause) {
        super(cause);
    }

}
