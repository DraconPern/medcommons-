/*
 * $Id$
 * Created on 22/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import org.apache.log4j.Logger;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

public class RegistryAction implements ActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(RegistryAction.class);
    
    private ActionBeanContext ctx = null;

    public RegistryAction() {
        super();
    }
    
    @DefaultHandler
    public Resolution display() throws ConfigurationException, NotLoggedInException {
        UserSession desktop = (UserSession) ctx.getRequest().getSession().getAttribute("desktop");
        if(desktop == null) {
            throw new NotLoggedInException();
        }
        
        String registry = (String)ctx.getRequest().getSession().getAttribute("registry");
        if(registry == null) {
            throw new IllegalArgumentException("No registry in user context");
        }
        
        String url = Configuration.getProperty("SecondaryRegistry."+registry+".ui.url");
        if(url == null) {
            throw new ConfigurationException("No registry URL configured");
        }
        
        log.info("Displaying registry UI at " + url);
        return new RedirectResolution(url,false);
    }
    
    public void setContext(ActionBeanContext ctx) {
        this.ctx = ctx;
    }
    public ActionBeanContext getContext() {
        return ctx;
    }

}
