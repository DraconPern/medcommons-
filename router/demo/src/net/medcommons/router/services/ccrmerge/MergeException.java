/*
 * $Id$
 * Created on 23/08/2006
 */
package net.medcommons.router.services.ccrmerge;

/**
 * Thrown when a merge fails. 
 * 
 * @author ssadedin
 */
public class MergeException extends Exception {

    public MergeException() {
    }

    public MergeException(String message) {
        super(message);
    }

    public MergeException(Throwable cause) {
        super(cause);
    }

    public MergeException(String message, Throwable cause) {
        super(message, cause);
    }
}
