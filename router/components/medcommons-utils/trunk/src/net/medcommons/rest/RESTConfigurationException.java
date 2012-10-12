/*
 * $Id$
 * Created on 30/01/2007
 */
package net.medcommons.rest;

public class RESTConfigurationException extends RESTException {

    public RESTConfigurationException() {
    }

    public RESTConfigurationException(String message) {
        super(message);
    }

    public RESTConfigurationException(Throwable cause) {
        super(cause);
    }

    public RESTConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
