/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr;

public class PHRException extends Exception {
    public PHRException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public PHRException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public PHRException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public PHRException(Throwable cause) {
        super(cause);
    }
}
