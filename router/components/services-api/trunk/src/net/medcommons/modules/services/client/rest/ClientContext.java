/*
 * $Id$
 * Created on 22/09/2005 
 */
package net.medcommons.modules.services.client.rest;

/**
 * A context object containing information about the current client.
 * 
 * Specifically, this is managed as a thread-local global variable available
 * throughout the system.  It prevents passing the clientId around.
 * 
 * <i>This variable must be managed very carefully to avoid incorrect
 * clientIds getting used.  Handle with care and use finally() to reset
 * the variable</i>
 * 
 * @author ssadedin
 */
public class ClientContext {
    
    private static ThreadLocal<String> clientId = new ThreadLocal();

    public static String getClientId() {
    	return(clientId.get());
    }

    public static void setClientId(String id) {
        clientId.set(id);
    }
    
    public void reset() {
        clientId.set(null);
    }
}
