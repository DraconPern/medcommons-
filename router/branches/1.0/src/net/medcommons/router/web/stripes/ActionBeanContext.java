/*
 * $Id$
 * Created on 26/07/2006
 */
package net.medcommons.router.web.stripes;

import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.sourceforge.stripes.action.Resolution;

public class ActionBeanContext extends net.sourceforge.stripes.action.ActionBeanContext {

    /**
     * Override for the source page resolution.  Used in some special cases
     * to force validation failures to specific pages without having the
     * annoying _sourcePage parameter in the request.
     */
    private Resolution sourceResolution = null;
    
    
    public ActionBeanContext() {
        super();
    }
    
    public void setSourcePageResolution(Resolution r) {
        this.sourceResolution = r;
    }
    
    @Override
    public Resolution getSourcePageResolution() {
        if(sourceResolution==null)            
            return super.getSourcePageResolution();
        else
            return this.sourceResolution;
    }
    
    public void setAttribute(String name, Object value) {
        getRequest().setAttribute(name, value);
    }
    
    /**
     * Convenience method to get the currently active CCR for the
     * current request.
     * 
     * @return  the CCR being acted upon by this request
     */
    public CCRDocument getActiveCCR() throws ServiceException {
        UserSession session = UserSession.get(getRequest());
        if(session == null)
            return null;
        return session.getCurrentCcr(getRequest());
    }

}
