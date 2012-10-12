/*
 * $Id$
 * Created on 10/08/2005
 */
package net.medcommons.modules.services.client.rest;

import org.jdom.Document;

import net.medcommons.rest.RESTException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.modules.services.interfaces.NodeService;
import net.medcommons.modules.services.interfaces.ServiceException;

/**
 * @author ssadedin
 */
public class NodeServiceProxy implements NodeService {
    /**
     * Client for which this proxy is currently being used
     */
    private String clientId;       

    /**
     * @param id
     */
    public NodeServiceProxy(String id) {
        super();
        clientId = id;
    }

    public String registerNode(String type, String nodeId, String host, String key) throws ServiceException {
        try {
            Document doc = RESTUtil.call(clientId, "NodeService.registerNode", "type", type, "host", host, "key",key,"nodeId",nodeId);
            nodeId = doc.getRootElement().getChild("outputs").getChildText("nodeid");
            return nodeId;
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to register node with type=" + type + " host=" + host, e);
        }        
    }

}
