/*
 * $Id$
 * Created on 1/04/2005
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.util.struts.JDomForm;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.apache.struts.util.MessageResources;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * Outputs a value for the given XPath statement evaluated against the
 * supplied bean found in the standard scope hierarchy.
 * 
 * Note:  if the XPath expression evaluates to null or blank then
 * this tag evaluates its content;  this allows you to enter alternative
 * content in the body that appears if the content is missing.
 * 
 * @author ssadedin
 */
public class XPathValueTag extends BodyTagSupport {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(XPathValueTag.class);
    
    /**
     * XPath cache
     */
    private static XPathCache xpath = null;
    
    /**
     * The name of the bean that will be used to retrieve the XML
     */
    private String bean = null;

    /**
     * The name of the XPath to resolve to
     */
    private String path = null;

    /**
     * The property (optional) that will be used to resolve the XML
     */
    private String property = null;

    /**
     * Map of variables that are found in the expression and their values
     */
    private HashMap variables = new HashMap();

    /**
     * Parameters that should be explicitly passed through from page context,
     * if they exist.
     */
    private String params = "";
    
    /**
     * Set to true if you want to always return a list when multiple results are 
     * returned.  If false, a List with a single result will be converted to
     * that single result.
     */
    protected boolean alwaysList = false;
    
    /**
     * Whether or not to escape the value that is written for HTML entities.
     */
    protected boolean escape = true;
    
    
    /**
     * Whether or not to escape the value that is written  as a javascript string Javascript (ala json);
     */
     protected boolean jsEscape = true;
    
    /**
     * Set this flag to determine if the body should be skipped or not.
     * By default, if something is rendered skipBody is set to false,
     * otherwise it is set to true.  
     */
    protected boolean skipBody = true;
    
    
    /**
     * Regex for finding variables
     */
    private static final Pattern variablePattern = Pattern.compile("\\$([\\w]*)");

    /**
     *  
     */
    public XPathValueTag() {
        super();
        if(xpath == null) 
            xpath = (XPathCache) Configuration.getBean("ccrXPathCache");
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        return super.doStartTag();
    }

    public int doEndTag() throws JspException {
        try {

            if (Str.blank(bean)) {
                throw new JspException("Attribute bean is not allowed to be blank.");
            }

            JspWriter out = this.pageContext.getOut();

            Matcher matcher = variablePattern.matcher(this.path);
            while (matcher.find()) {
                String var = matcher.group(1);
                // Try to resolve the variable
                Object value = pageContext.findAttribute(var);
                if (value != null) {
                    this.variables.put(var, value);
                }
            }
            
            if(!Str.blank(this.params)) {
                for (String param : this.params.split(",")) {
                    param = param.trim();
                    Object value = pageContext.findAttribute(param);
                    if (value != null) {
                        this.variables.put(param, value);
                    }                                        
                }
            }

            // Try and get the bean
            Object obj = pageContext.findAttribute(bean);
            if (obj == null) {
                throw new JspException("Unable to find attribute " + bean + " in any scope");
            }

            if (property != null) {
                obj = PropertyUtils.getProperty(obj, this.property);
            }

            Object contextObj = null;

            // If the object is a JDomForm, use that
            if (obj instanceof JDomForm) {
                JDomForm form = (JDomForm) obj;
                contextObj = form.getDocument((HttpServletRequest) pageContext.getRequest());
                if (contextObj == null) {
                    throw new JspException("Document is null for JDOMForm " + bean + (property == null ? "" : property)
                                    + " of type " + obj.getClass().getName());
                }
            }
            else if (obj instanceof Document) {// If it is a JDOM Document use that
                contextObj = obj;
            }
            else if (obj instanceof Content) {// If it is a JDOM Document use that
                contextObj = obj;
            }

            if (contextObj == null) {
                throw new JspException("Bean " + bean + (property == null ? "" : property) + " of type "
                                + obj.getClass().getName() + " does not implement a JDOM interface.");
            }

            try {
            	if (log.isDebugEnabled())
            		log.debug("Getting value " + this.path + " from JDOM document/node " + contextObj.hashCode() + "(" + contextObj.toString() + ")");
                Object valueObj = xpath.getXPathResult(contextObj, this.path, this.variables, this.alwaysList);

                if (valueObj != null) {
                    // Output the value from XPath statements
                    this.writeTag(valueObj);
                }
                else 
                    skipBody = false;
            }
            catch (JDOMException e1) {
                throw new JspException("Error executing XPath expression " + this.path, e1);
            }
        }
        catch (IOException e) {
            throw new JspException(e);
        }
        catch (IllegalAccessException e) {
            throw new JspException(e);
        }
        catch (InvocationTargetException e) {
            throw new JspException(e);
        }
        catch (NoSuchMethodException e) {
            throw new JspException(e);
        }
        catch (CCROperationException e) {
            throw new JspException(e);
        }
        catch (PHRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
        return this.skipBody ? SKIP_BODY : EVAL_BODY_BUFFERED;
    }

    /**
     * Writes the value found.
     * 
     * @param obj
     * @throws IOException
     * @throws JspException
     */
    protected void writeTag(Object obj) throws IOException, JspException {
        if (obj instanceof Element) {
            obj = ((Element) obj).getTextTrim().toString();
        }
        else if (obj instanceof Attribute) {
            obj = ((Attribute) obj).getValue();
        }
        // Output the value from XPath statements
        String stringVal = obj.toString();
        
        if(escape)
            stringVal = Str.escapeHTMLEntities(stringVal);
        
        if(jsEscape) {
            // Replace new lines with \n
            // prefix single and double quotes with backslashes
            stringVal = Str.escapeForJavaScript(stringVal);
        }
            
        
        pageContext.getOut().print(stringVal);        
        if(Str.blank(stringVal)) {
            skipBody = false;
        }
    }

    public void release() {
        this.bean = null;
        this.path = null;
        this.property = null;
        this.skipBody = true;
        this.variables.clear();
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public boolean isEscape() {
        return escape;
    }

    public void setEscape(boolean escape) {
        this.escape = escape;
    }

    public boolean isJsEscape() {
        return jsEscape;
    }

    public void setJsEscape(boolean jsEscape) {
        this.jsEscape = jsEscape;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String parameters) {
        this.params = parameters;
    }
}
