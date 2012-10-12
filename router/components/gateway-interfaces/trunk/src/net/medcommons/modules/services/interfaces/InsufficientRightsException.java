/*
 * $Id$
 * Created on 22/01/2007
 */
package net.medcommons.modules.services.interfaces;

/**
 * Thrown by DocumentService if request is made to access a document
 * that is revoked by end user.
 * @author ssadedin
 */
public class InsufficientRightsException extends ServiceException {

    public InsufficientRightsException() {
    }

    public InsufficientRightsException(String message) {
        super(message);
    }

    public InsufficientRightsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientRightsException(Throwable cause) {
        super(cause);
    }

}
