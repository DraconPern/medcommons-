package net.medcommons.router.services.wado.stripes;

import net.medcommons.router.web.stripes.JSONActionBean;
import net.medcommons.router.web.stripes.JSONResolution;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

public class FlushSettingsAction extends JSONActionBean {
    
    @DefaultHandler
    public Resolution flush() {
        
       this.session.flushAccountSettings();
       
       return new JSONResolution();   
    }
}
