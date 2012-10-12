/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr;

public class ValidationFailureException extends Exception {
    public ValidationFailureException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public ValidationFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ValidationFailureException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ValidationFailureException(Throwable cause) {
        super(cause);
    }
}
