/*
 * $Id: BaseActionBean.java 3658 2010-04-07 20:57:15Z ssadedin $
 * Created on 26/06/2006
 */
package net.medcommons.router.web.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.eq;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.UserSession;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 * Simple default base class for actions, provides convenience methods.
 * 
 * @author ssadedin
 */
public class BaseActionBean implements ActionBean {
    
    public static final String GUID_PATTERN = "[a-z0-9]{40}";
    
    public static final String TOKEN_PATTERN = "[a-z0-9]{40}";

    public static final String MCID_PATTERN = "[0-9]{16}";

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(BaseActionBean.class);
    
    protected ActionBeanContext ctx;
    
    protected UserSession session = null;
    
    /**
     * Whether to send cache headers - if false, no-cache will be sent,
     * otherwise not.
     */
    protected boolean cacheable = false;
    
    protected boolean newSession = false;
    
    public BaseActionBean() {
        super();
    }

    public ActionBeanContext getContext() {
        return ctx;
    }

    /**
     * Sets defaults for every r, including checking if the 
     * r targets a different authenticated user.  If it does,
     * the existing session is terminated and a new one is 
     * created for the the red user.
     */
    public void setContext(net.sourceforge.stripes.action.ActionBeanContext ctx) {
        
        HttpServletRequest r = ctx.getRequest();
            
        String serverPort = (r.getServerPort()==80||r.getServerPort()==443)?"":":"+r.getServerPort(); 
        r.setAttribute("baseUrl",r.getScheme() + "://" +r.getServerName()+serverPort +r.getContextPath() + "/"); 
        
        NDC.clear();
        NDC.push(r.getSession().getId().substring(0,6));
        
        try {
            
            // Set this so that subclasses can modify their behavior depending
            // on whether the session is new or not
            if(!UserSession.has(ctx.getRequest()))
                newSession = true;
            
            this.ctx = (ActionBeanContext)ctx;
            this.ctx.setSourcePageResolution(this.getSourcePageResolution());
            
            if(!UserSession.has(r))
                log.info("New session - no existing destkop");
            
            String accid = r.getParameter("accid");
            String auth = r.getParameter("auth");
            
            if(UserSession.has(r) && (!blank(accid) || !blank(auth))) {
                this.session = UserSession.get(r);
                if((!blank(accid) && !session.isOwner(accid)) || (!eq(session.getAuthenticationToken(), auth) && !eq(session.getContextAuth(),auth))) {
                    log.info("Cleaning desktop due to change of owner:  " + session.getOwnerMedCommonsId() + " => " 
                                    + accid + ", auth " + session.getAuthenticationToken() + " => " + auth);
                    UserSession.clean(r);
                }
            }
            
            this.session = UserSession.get(r);
            
            if(!blank(this.session.getAuthenticationToken()))
                NDC.push(this.session.getAuthenticationToken().substring(0,6));
            
            if(!cacheable) {
	            nocache();
            }
        }
        catch (ServiceException e) {
            throw new RuntimeException("Unable to verify user parameters against existing session",e);
        }
    }

    protected void nocache() {
        ctx.getResponse().setHeader("Cache-Control","no-cache"); // HTTP 1.1
        ctx.getResponse().setHeader("Pragma","no-cache"); // HTTP 1.0
    }
    
    /**
     * Placeholder for child classes to override if they want to override
     * the resolution for validation failures.
     * 
     * @return
     */
    protected Resolution getSourcePageResolution() {
        return null;
    }

    public void setSession(UserSession session) {
        this.session = session;
    }

    public UserSession getSession() {
        return session;
    }

}
