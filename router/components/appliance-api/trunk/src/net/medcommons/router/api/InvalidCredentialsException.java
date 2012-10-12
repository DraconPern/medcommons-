/*
 * $Id: InvalidCredentialsException.java 3322 2009-04-22 05:56:41Z ssadedin $
 * Created on 30/06/2008
 */
package net.medcommons.router.api;

import net.medcommons.router.oauth.api.APIException;

public class InvalidCredentialsException extends APIException {

    public InvalidCredentialsException() {
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCredentialsException(Throwable cause) {
        super(cause);
    }

}
