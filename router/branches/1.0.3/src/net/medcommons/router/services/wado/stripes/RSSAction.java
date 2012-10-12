/*
 * $Id$
 * Created on 20/02/2007
 */
package net.medcommons.router.services.wado.stripes;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityLogService;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.InsufficientPrivilegeException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;

/**
 * Renders an RSS feed of items in the patient's activity log 
 * 
 * @author ssadedin
 */
@UrlBinding("/rss")
public class RSSAction extends AccessAction {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(RSSAction.class);
    
    /**
     * Account number for authorization
     */
    @Validate(required=true,mask="[0-9]{4} *[0-9]{4} *[0-9]{4} *[0-9]{4} *")
    private String a = null;
    
    /**
     * Authentication token to prove access rights
     */
    @Validate(required=false,mask="[0-9a-f]{40}")
    private String auth = null;
    
    /**
     * Number of entries to return
     */
    private int limit = 20;
    
    /**
     * Account settings for account in question
     */
    private AccountSettings accountSettings;
    
    /**
     * Renders an RSS version of the given user's activity log
     */
    @DefaultHandler
    public Resolution rss() throws Exception { 
        
        
        if(this.auth == null)
            this.auth = this.getAt();
        
        this.session = new UserSession(null, auth, new ArrayList<CCRDocument>());
        
        if(!this.session.checkPermissions(this.a, Rights.READ.toString())) {
            throw new InsufficientPrivilegeException("Authorization token " + session.getAuthenticationToken() + " not authorized for access to account " + this.a);
        }
        
        this.accountSettings = session.getAccountSettings(this.a); 
            
        Collection<ActivityEvent> events = 
            session.getServicesFactory().getActivityLogService().load(this.a, 0, ActivityLogService.READ_ALL);
        this.ctx.getRequest().setAttribute("events", events);
        return new ForwardResolution("/activityRSS.jsp");
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void setAccountSettings(AccountSettings accountSettings) {
        this.accountSettings = accountSettings;
    }
 
}
