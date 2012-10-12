/*
 * $Id: AccountImportException.java 2661 2008-06-19 09:08:04Z ssadedin $
 * Created on 17/06/2008
 */
package net.medcommons.router.services.wado;

public class AccountImportException extends Exception {

    public AccountImportException() {
        super();
    }

    public AccountImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountImportException(String message) {
        super(message);
    }

    public AccountImportException(Throwable cause) {
        super(cause);
    }

}
