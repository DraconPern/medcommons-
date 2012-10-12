/*
 * $Id$
 * Created on 04/06/2007
 */
package net.medcommons.router.web.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.services.wado.SessionExpiredException;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesRequestWrapper;
import net.sourceforge.stripes.exception.StripesServletException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * A very simple wrapper that unifies struts / stripes exception handling to make
 * life easier.
 * <p>
 * In addition, specific handling is performed to ensure that actions that 
 * are intended to return JSON marshal errors back to the client in JSON 
 * format instead of HTML. JSON error formatting will be used if the processing ActionBean 
 * was a subclass of {@link JSONActionBean}, {@link CCRJSONActionBean}, or if
 * the handler method had a {@link JSON} annotation.
 * <p>
 * This error handler also recognizes {@link SessionExpiredException} and 
 * {@link NotLoggedInException} errors and handles them by directing the user
 * to a login page with appropriate messaging.
 * 
 * @author ssadedin
 */
public class ExceptionHandler implements net.sourceforge.stripes.exception.ExceptionHandler {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ExceptionHandler.class);

    public void handle(Throwable t, HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException 
    {        
        
        String responseType = "html";
        try {
            if(request instanceof StripesRequestWrapper) {
                Object actionBean = request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
                if(actionBean != null) {
                    Class<?> c = actionBean.getClass();
                    if(JSONActionBean.class.isAssignableFrom(c) || CCRJSONActionBean.class.isAssignableFrom(c)) {
                        responseType = "json";
                    }
                    else
                    if(BaseActionBean.class.isAssignableFrom(c)) {
                        BaseActionBean bab = (BaseActionBean) actionBean;
                        String eventName = bab.getContext().getEventName();
                        if(!blank(eventName)) {
                            Method m = c.getMethod(eventName);
                            if(m.isAnnotationPresent(JSON.class)) {
                                responseType = "json";
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            log.warn("Exception while introspecting stack trace of error: " + e.getMessage());
        }
        
        if("json".equals(request.getParameter("fmt")) || "json".equals(request.getAttribute("fmt"))) {
            responseType = "json";
        }
        
        if(t instanceof StripesServletException) {
           StripesServletException sse = (StripesServletException)t;
           if(sse.getRootCause() != null)
	           t = sse.getRootCause(); // unwrap 
        }
        
        log.error("Exception in stripes action", t);
        
        response.setStatus(500);
        
        if("json".equals(responseType)) {
            JSONObject obj = new JSONObject();
            obj.put("status", "failed");
            obj.put("error", t.getMessage());
            obj.put("message", t.getMessage());
            response.getOutputStream().write(obj.toString().getBytes("UTF-8"));
        }
        else
        if(t instanceof NotLoggedInException || t instanceof SessionExpiredException) {
            request.setAttribute("expired", Boolean.TRUE);
            request.getRequestDispatcher("/toplogon.jsp").forward(request, response);
            return;
        }
        else {
	        request.setAttribute("org.apache.struts.action.EXCEPTION", t);
	        request.getRequestDispatcher("/wadoError.jsp").forward(request, response);
        }
    }

    public void init(Configuration arg0) throws Exception {

    }

}
