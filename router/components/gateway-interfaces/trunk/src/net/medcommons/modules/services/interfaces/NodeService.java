/*
 * $Id$
 * Created on 10/08/2005
 */
package net.medcommons.modules.services.interfaces;

/**
 * Provides facilities to manage, lookup, register and authenticate Nodes
 * within the MedCommons network.
 * 
 * @author ssadedin
 */
public interface NodeService {

    /**
     * Registers this node with a MedCommons system.
     * 
     * @param type - one of "repo", "gw"
     * @param expected nodeId - if this matches an existing node Id for this gateway then it will be echoed back in the output.
     * @param key - key assigned in central for this node
     * @return the node id assigned for this node, or blank if either node id is not provided OR key is not provided / mismatches
     */
    String registerNode(String type, String nodeId, String url, String key) throws ServiceException;
    
}
