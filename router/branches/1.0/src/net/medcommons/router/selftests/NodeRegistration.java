/*
 * $Id: NodeRegistration.java 3396 2009-06-10 10:46:34Z ssadedin $
 * Created on 19/07/2007
 */
package net.medcommons.router.selftests;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.client.rest.NodeServiceProxy;
import net.medcommons.modules.services.interfaces.NodeService;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.configuration.tomcat.RegisterNodeTask;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestFailure;
import net.medcommons.router.selftest.SelfTestResult;

public class NodeRegistration implements SelfTest { 

    public SelfTestResult execute(ServicesFactory services) throws Exception {
        NodeService nodeService = new NodeServiceProxy(null);
        String host = Configuration.getProperty("RemoteAccessAddress");        
        String nodeId = Configuration.getProperty("NodeID");        
        String key = new RegisterNodeTask().getNodeKey();
        // String key = Configuration.getProperty("NodeKey");        
        
        String assignedNodeId = nodeService.registerNode("gw",nodeId, host, key);
        
        if(Str.blank(assignedNodeId)) {
            return new SelfTestFailure("Node registration returned blank Node ID.  This gateway is unregistered or incorrectly registered.");
        }
        
        String configuredNodeID = Configuration.getProperty("NodeID");
        
        // Note we no longer check the returned node id.  It does not need
        // to be the same as any preconfigured node id we are using
        
        return null;
    }
}
