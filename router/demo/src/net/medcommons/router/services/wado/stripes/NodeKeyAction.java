/*
 * $Id: NodeKeyAction.java 2468 2008-03-13 06:27:56Z ssadedin $
 * Created on 03/09/2007
 */
package net.medcommons.router.services.wado.stripes;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.client.rest.NodeServiceProxy;
import net.medcommons.modules.services.interfaces.NodeService;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.web.stripes.AllowUnconfigured;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

@AllowUnconfigured
public class NodeKeyAction implements ActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(NodeKeyAction.class);
    
    private ActionBeanContext context = null; 
    
    @Validate(required=true,mask="[A-Fa-f0-9]{40}")
    private String key;
    
    @DefaultHandler
    public Resolution save() throws Exception {
        
        // Attempt to register and get the node id back
        String host = Configuration.getProperty("RemoteAccessAddress");        
        NodeService ns = new NodeServiceProxy(null);
        String nodeId = Configuration.getProperty("NodeID");        
        nodeId = ns.registerNode("gw", nodeId, host, key); 
        
        if(Str.blank(nodeId)) {
            log.info("Failed to validate key " + key + " for host " + host);
            this.context.getValidationErrors().addGlobalError(new SimpleError("Unable to validate key"));
            return this.context.getSourcePageResolution();
        }
        
        // Append to LocalBootParameters.properties
        FileOutputStream f = new FileOutputStream("conf/LocalBootParameters.properties",true);
        PrintWriter out = new PrintWriter(f);
        out.write('\n');
        out.println("# NodeKey appended by user " + (new Date()).toString());
        out.println("NodeKey=" + this.key);
        out.println("NodeID=" + nodeId);
        out.flush();
        f.flush();
        out.close();
        f.close();
        this.context.getServletContext().setAttribute("NodeKey", this.key);
        this.context.getServletContext().setAttribute("NodeID", nodeId);
        Configuration.getAllProperties().setProperty("NodeID", nodeId);
        
        return new RedirectResolution("/savedKey.ftl"); 
    }

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
