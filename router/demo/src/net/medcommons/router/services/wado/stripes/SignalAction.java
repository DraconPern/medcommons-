/*
 * $Id$
 * Created on 17/01/2007
 */
package net.medcommons.router.services.wado.stripes;

import javax.servlet.http.Cookie;

import net.medcommons.router.web.stripes.ActionBeanContext;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

/**
 * Acts as a relay to allow other domains to send events to this gateway. 
 * 
 * @author ssadedin
 */
public class SignalAction implements ActionBean {

    /**
     * The event to process
     */
    private String e;
    
    /**
     * Stripes context
     */
    private ActionBeanContext ctx;
     
    /**
     * Signals an event 'e' 
     */
    @DefaultHandler
    public Resolution signal() {        
        Cookie c = new Cookie("ce", e);
        c.setPath("/");
        ctx.getResponse().addCookie(c);
        return new StreamingResolution("text/javascript", "var registeredAt="+ (System.currentTimeMillis()/1000));
    }
    
    public void setContext(net.sourceforge.stripes.action.ActionBeanContext ctx) {
        this.ctx = (ActionBeanContext)ctx;
        ctx.getResponse().setHeader("Cache-Control","no-cache"); // HTTP 1.1
        ctx.getResponse().setHeader("Pragma","no-cache"); // HTTP 1.0
    }    
    
    public ActionBeanContext getContext() {
        return ctx;
    }
    
    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

}
