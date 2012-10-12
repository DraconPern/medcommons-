/*
 * $Id$
 * Created on 31/03/2005
 */
package net.medcommons.router.util.struts;

import static net.medcommons.modules.utils.Str.eq;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.wado.CCROperationException;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.RequestProcessor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

/**
 * JDomRequestProcessor adds special handling for JDOMForm.
 * 
 * JDOMForms translate their properties into XPath statements
 * within JDOM instead of bean properties.
 * 
 * @author ssadedin
 */
public class JDomRequestProcessor extends RequestProcessor {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(JDomRequestProcessor.class);
    
    private XPathCache xpath = (XPathCache) Configuration.getBean("ccrXPathCache");

    /**
     * Default constructor
     */
    public JDomRequestProcessor() {
        super();
    }
    
    protected void processPopulate(
                    HttpServletRequest request, 
                    HttpServletResponse response, 
                    ActionForm form, 
                    ActionMapping mapping) throws ServletException
    {
        
        if (form instanceof JDomForm) {
            JDomForm jdomForm = (JDomForm) form;
            Document doc;
            try {                
                doc = jdomForm.getDocument(request);
            }
            catch (CCROperationException e) {
                throw new ServletException("Unable to get JDOM Document for JDOMForm " + form.getClass(), e);
            }
            catch (PHRException e) {
                throw new ServletException("Unable to get JDOM Document for JDOMForm " + form.getClass(), e);
            }
            if(doc != null) {                
                Namespace ns = jdomForm.getNamespace();
                log.debug("Setting properties on JDOM Document " + doc.hashCode());
                String nsPathElement = null;
                if(ns != null)
                    nsPathElement = "/"+ns.getPrefix()+":";
    
                Enumeration names = null;
                names = request.getParameterNames();
    
                // Build a list of relevant request parameters from this request
                HashMap properties = new HashMap();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();                  
                    String xPath;
                    xPath = name.replace('.','/');
                    try {
                        String newValue = request.getParameter(name);
                        
                        Object elementObj = xpath.getXPathResult(doc,xPath,Collections.emptyMap(),false);
                        if (elementObj != null && elementObj instanceof java.util.List && xpath.isMultiValued(xPath)) {
                            log.debug("path " + xPath + " returns list: ignoring default population");
                        }
                        else {
                            // If not defined as multivalued, coerce automatically to set the first element
                            if(elementObj != null && elementObj instanceof java.util.List)
                                elementObj = ((List)elementObj).get(0);
                                
                            Element element = (Element)elementObj;
                            if(element == null) {
                                // Bug #442 - prevent creating "empty" elements - only create if the
                                // user actually specified them
                                if(!Str.blank(newValue)) {
                                    element = jdomForm.createPath(xPath);
                                }
                            }
                            if(element!=null) {
                                if(log.isDebugEnabled()) {
                                    log.debug("Setting xpath " + xPath + " to value " + request.getParameter(name) + " on document " + doc.hashCode());
                                }
                                String oldValue = element.getText();
                                if(!eq(oldValue,newValue)) {
                                    jdomForm.setModified(request);
                                    element.setText(newValue);
                                }
                            }
                        }
                    }
                    catch (JDOMException e) {
                        log.warn("Failed to locate XPath expression " + xPath + ": " + e.getMessage());
                    }
                    catch (IOException e) {
                        log.warn("Failed to locate XPath expression " + xPath + ": " + e.getMessage());
                    }
                    catch (PHRException e) {
                        log.warn("Failed to locate XPath expression " + xPath + ": " + e.getMessage());
                    }
                }
            }
            else
                log.debug("No JDOM Document available - form " + form + " not being populated with xpath");
        }
	
        try {
	        super.processPopulate(request, response, form, mapping);
        }
        catch(Throwable e) {
            
            if (e instanceof ServletException) {
                ServletException se = (ServletException) e;
                if(se.getRootCause()!=null)
                    e = se.getRootCause();
            }
            
            log.error("Error in struts form population",e);
            request.setAttribute(Action.EXCEPTION_KEY, e);
            try {
                request.getRequestDispatcher("/wadoError.jsp").forward(request,response);
            }
            catch (IOException e1) {
                throw new ServletException(e1);
            }
            return;
        }
    }

}
