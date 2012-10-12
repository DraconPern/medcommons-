/*
 * $Id$
 * Created on 04/07/2007
 */
package net.medcommons.router.services.wado.stripes;

import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

/**
 * Provides a synonym URL mapping for AccessAction 
 * 
 * @author ssadedin
 */
@UrlBinding("/ccrs/{a}/{g}/{xp}")
public class CCRAccessAction extends AccessAction {
    
    @After(stages=LifecycleStage.EventHandling)
    public void cleanDesktop() {
        if(this.ctx.getRequest().getSession().isNew() && this.getXml() != null || this.getJson() != null) 
            this.ctx.getRequest().getSession().invalidate();
    }
}
