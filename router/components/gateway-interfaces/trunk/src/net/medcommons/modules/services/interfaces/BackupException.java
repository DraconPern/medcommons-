/*
 * $Id: BackupException.java 3501 2009-10-08 21:39:26Z ssadedin $
 * Created on 21/10/2008
 */
package net.medcommons.modules.services.interfaces;

public class BackupException extends ServiceException {

    public BackupException() {
    }

    public BackupException(String message) {
        super(message);
    }

    public BackupException(Throwable cause) {
        super(cause);
    }

    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }

}
