/*
 * $Id$
 * Created on 29/03/2007
 */
package net.medcommons.phr.ccr;

public class InvalidCCRException extends Exception {

    public InvalidCCRException() {
    }

    public InvalidCCRException(String message) {
        super(message);
    }

    public InvalidCCRException(Throwable cause) {
        super(cause);
    }

    public InvalidCCRException(String message, Throwable cause) {
        super(message, cause);
    }

}
