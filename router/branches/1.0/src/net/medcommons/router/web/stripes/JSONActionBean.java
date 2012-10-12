/*
 * $Id: JSONActionBean.java 3242 2009-02-19 01:46:47Z ssadedin $
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
 * Utility class that makes writing actions that return JSON output easier.
 * <p>
 * A validation handler is implemented so that child classes can freely use
 * Stripes validation on parameters and know that errors will be marshaled
 * back as JSON rather than HTML.
 * <p>
 * Child classes are also recognized by the {@link ExceptionHandler} when 
 * exceptions are thrown so that
 * errors will be reported in JSON format as well.
 * 
 * @see CCRJSONActionBean
 * @author ssadedin
 */
public class JSONActionBean extends BaseActionBean implements ValidationErrorHandler {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(JSONActionBean.class);

    /**
     * Default result object 
     */
    protected JSONObject result = new JSONObject();
    
    public JSONActionBean() {
        result.put("status","ok");
    }

    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        result.put("status", "failed");
        result.put("error", "invalid input for field " + errors.keySet().iterator().next());
        return new StreamingResolution("text/plain", result.toString());
    }

    @Override
    public void setContext(ActionBeanContext ctx) {
        ctx.getRequest().setAttribute("fmt", "json");
        
        super.setContext(ctx);
        
        
        checkSID();
    }

    /**
     * Verify that a parameter has been passed identifying the session id.
     * <p>
     * Requiring this parameter helps to protect against XSS attacks since it
     * contains information normally not available to a 3rd party.
     */
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
}
