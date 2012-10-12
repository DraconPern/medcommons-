/*
 * $Id$
 * Created on 06/07/2007
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * This is a 'dummy' action that does not do anything itself
 * other than allow other actions to route through it in order
 * to update the user's existing CCR on the way out to another URL 
 * 
 * @author ssadedin
 */
public class UpdateCCRAction extends CCRActionBean {
    
    /**
     * Action to which we will forward
     */
    private String forward;
    
    @DefaultHandler
    public Resolution update() {
        return new ForwardResolution("/"+forward);
    }

    public String getForward() {
        return forward;
    }

    public void setForward(String forward) {
        this.forward = forward;
    }
}
