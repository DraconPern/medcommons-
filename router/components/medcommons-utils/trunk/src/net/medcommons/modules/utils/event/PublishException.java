/*
 * $Id: PublishException.java 2972 2008-10-21 06:58:03Z ssadedin $
 * Created on 14/10/2008
 */
package net.medcommons.modules.utils.event;

public class PublishException extends Exception {

    public PublishException() {
    }

    public PublishException(String message) {
        super(message);
    }

    public PublishException(Throwable cause) {
        super(cause);
    }

    public PublishException(String message, Throwable cause) {
        super(message, cause);
    }

}
