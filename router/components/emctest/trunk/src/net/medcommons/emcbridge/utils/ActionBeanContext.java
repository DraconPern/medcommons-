package net.medcommons.emcbridge.utils;

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
    

}