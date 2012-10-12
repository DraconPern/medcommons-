/*
 * $Id: ImportCancelledException.java 2684 2008-06-26 06:37:38Z ssadedin $
 * Created on 26/06/2008
 */
package net.medcommons.router.services.wado;

public class ImportCancelledException extends AccountImportException {

    public ImportCancelledException() {
    }

    public ImportCancelledException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImportCancelledException(String message) {
        super(message);
    }

    public ImportCancelledException(Throwable cause) {
        super(cause);
    }

}
