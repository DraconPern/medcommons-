/*
 * $Id: PHRTransactionException.java 2592 2008-05-12 07:16:15Z ssadedin $
 * Created on 08/05/2008
 */
package net.medcommons.modules.publicapi;

public class PHRTransactionException extends Exception {

    public PHRTransactionException() {
    }

    public PHRTransactionException(String message) {
        super(message);
    }

    public PHRTransactionException(Throwable cause) {
        super(cause);
    }

    public PHRTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

}
