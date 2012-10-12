/*
 * $Id: CCRJSONActionBean.java 3304 2009-03-30 09:30:33Z ssadedin $
 * Created on 05/05/2008
 */
package net.medcommons.router.web.stripes;

import javax.servlet.http.Cookie;

import net.medcommons.modules.utils.Str;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * A utility class that eases writing of JSON services that operate on a user's CCR.
 * <p>
 * This class provides all the features of the {@link JSONActionBean} while
 * adding features to automatically verify the incoming request by an 
 * expected session id parameter (sid).  If the sid parameter is not provided
 * or is not correct then an exception is thrown.
 * 
 * @author ssadedin
 */
public class CCRJSONActionBean extends CCRActionBean implements ValidationErrorHandler {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CCRJSONActionBean.class);
    
    /**
     * Default result object 
     */
    protected JSONObject result = new JSONObject();


    public CCRJSONActionBean() {
        result.put("status","ok");
    }
    /**
     * We override the default implementation to add checking of 
     * a required parameter that should contain the jsessionid.
     * This is to protect against basic XSS attacks that would 
     * try to load a user's data from a 3rd party site.  By 
     * requiring the cookie to be passed in the url we ensure they
     * cannot construct a valid URL without actually having access
     * to the domain (ie. the cookies).
     */
    @Override
    public void setContext(ActionBeanContext ctx) {
        super.setContext(ctx);
        
        ctx.getRequest().setAttribute("fmt", "json");
        
        checkSID();
    }

    protected void checkSID() {
        String sid = ctx.getRequest().getParameter("sid");
        if(Str.blank(sid)) {
            log.error("A JSON request was received without a 'sid' parameter.  The sid parameter is required to protect against XSS attacks, and should contain the value of the JSESSIONID cookie for the user's session.  The caller needs to be modified to include the sid parameter, or better, should be using the default execJSONRequest() utility function that includes it automatically.");
            throw new IllegalArgumentException("Required parameter 'sid' is missing");
        }
        
        Cookie jsid = null;
        for(Cookie c : ctx.getRequest().getCookies()) {
            if("JSESSIONID".equals(c.getName())) {
                jsid = c;
            }
        }
        
        if(jsid == null) 
            throw new RuntimeException("JSON Request received without JSESSIONID cookie.  All JSON requests must have an active session.");
        
        if(!sid.equals(jsid.getValue()))
            throw new RuntimeException("Invalid value for 'sid' parameter.  Active session with this id not found.");
    }

    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("status", "failed");
        obj.put("error", "invalid input for field " + errors.keySet().iterator().next());
        return new StreamingResolution("text/plain", obj.toString());
    }

}
