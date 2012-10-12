package net.medcommons.application.dl.stripes;


import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dl.Person;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;



public class DlActionContext extends ActionBeanContext {
	 private static Logger log = Logger.getLogger(DlActionContext.class);
	/**
     * Override for the source page resolution.  Used in some special cases
     * to force validation failures to specific pages without having the
     * annoying _sourcePage parameter in the request.
     */
    private Resolution sourceResolution = null;
    
    public ContextManager getContextManager(){
 
    	return(ContextManager.get());
    	
    }

    public void setContextManager(ContextManager ignored){
    	log.info("===setContextManager() - ignore value");
    	;
    }
    /** Gets the currently logged in user, or null if no-one is logged in. */
    public Person getUser() {
        return (Person) getRequest().getSession().getAttribute("user");
    }

    /** Sets the currently logged in user. */
    public void setUser(Person currentUser) {
        getRequest().getSession().setAttribute("user", currentUser);
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