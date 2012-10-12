package net.medcommons.router.messaging;

import java.util.List;

import org.json.JSONObject;

import net.medcommons.modules.services.interfaces.ServiceException;

/**
 * Simple messaging service that allows messages to be sent between any
 * nodes that are talking to the same MySQL instance.
 */
public interface MessageService {
    
    /**
     * Read a message from the specified connection or return null if there 
     * is no message
     */
    List<JSONObject> read(String connectionId, long timeoutMs) throws ServiceException;
    
    /**
     * Write a message to the specified connection
     */
    void send(String connectionId, JSONObject message) throws ServiceException;
}
