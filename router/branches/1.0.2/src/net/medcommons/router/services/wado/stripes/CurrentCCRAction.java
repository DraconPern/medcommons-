/*
 * $Id$
 * Created on 31/08/2006
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.DocumentNotFoundException;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

/**
 * Displays a user's Current CCR or information pertaining to it.
 * 
 * @author ssadedin
 */
public class CurrentCCRAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CurrentCCRAction.class);
    
    @Validate(required=true, mask=MCID_PATTERN)
    private String a;
    
    /**
     * Authorization account - the account context under which this account should be accessed
     * if not provided, defaults to same account as being accessed.
     */
    private String aa;
    
    /**
     * Authorization token, if any 
     */
    private String auth;
    
    /**
     * Mode to display CCR in (edit, view)
     */
    private String m;
    
    /**
     * Context
     */
    private String c;
    
    @DefaultHandler
    public Resolution show() throws Exception {        
        if(Str.blank(this.aa)) {
            this.aa = this.a;
        }
        ServicesFactory factory = new RESTProxyServicesFactory("token:"+auth);
        AccountSettings settings = factory.getAccountService().queryAccountSettings(a);
        if(Str.blank(settings.getCurrentCcrGuid())) {
            throw new DocumentNotFoundException("User " + a + " does not have a current ccr set");            
        }         
        return access("",settings);
    }

    public Resolution widget() throws Exception {        
        if(Str.blank(this.aa)) {
            this.aa = this.a;
        }
        ServicesFactory factory = new RESTProxyServicesFactory("token:"+auth);
        AccountSettings settings = factory.getAccountService().queryAccountSettings(a);
        if(Str.blank(settings.getCurrentCcrGuid())) {
            log.info("User " + a + " has no current ccr");
            return new ForwardResolution(CurrentCCRWidgetAction.class);
        }         
        return access("widget",settings);
     }
    
    private Resolution access(String event, AccountSettings settings) throws Exception {
        AccessAction accessAction = new AccessAction();
        accessAction.setContext(ctx);
        accessAction.setA(this.aa);
        accessAction.setAt(this.auth);
        accessAction.setG(settings.getCurrentCcrGuid());
        accessAction.setM(this.m);
        accessAction.setC(this.c);
        
        // Used to do redirect, but now to improve performance forward directly
        // return new RedirectResolution("/access?"+event+"a="+this.aa+"&at="+auth+"&g="+settings.getCurrentCcrGuid()+"&c=currentccr");
        return "widget".equals(event) ? accessAction.widget() : accessAction.access();
    }        
    
    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getAa() {
        return aa;
    }

    public void setAa(String aa) {
        this.aa = aa;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public synchronized String getM() {
        return m;
    }

    public synchronized void setM(String m) {
        this.m = m;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

}
