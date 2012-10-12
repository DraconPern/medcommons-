/*
 * $Id: ImportAttachmentException.java 2550 2008-04-22 23:17:23Z ssadedin $
 * Created on 23/04/2008
 */
package net.medcommons.document.ccr;

public class ImportAttachmentException extends Exception {

    public ImportAttachmentException() {
    }

    public ImportAttachmentException(String message) {
        super(message);
    }

    public ImportAttachmentException(Throwable cause) {
        super(cause);
    }

    public ImportAttachmentException(String message, Throwable cause) {
        super(message, cause);
    }

}
