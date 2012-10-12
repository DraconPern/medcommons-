/*
 * $Id$
 * Created on 1/04/2005
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.util.struts.JDomForm;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * A tag sort-of-modeling struts HTML tags, but instead mapping to an XPath statement.
 * 
 * @author ssadedin
 */
public class XPathInputTag extends BodyTagSupport {
    
    private HashMap attributes = new HashMap();
    
    private String bean;
    
    /**
     * True if characters should be converted to their HTML entity
     * equivalents in the output.
     */
    private boolean escape = true;
    
    private static XPathCache xpath = null;
    
    /**
     * 
     */
    public XPathInputTag() {
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
            
            if(Str.blank(bean)) {
                throw new JspException("Attribute bean is not allowed to be blank.");
            }
            
            
            JspWriter out = this.pageContext.getOut();
            
            out.print("<input ");
            
            // Output all the pass-through attributes
            for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();                
                out.print(entry.getKey()+"=\"" + entry.getValue() + "\" "); 
            }

            // Try and get the bean
            Object obj = pageContext.findAttribute(bean);
            
            if(obj == null) {
                throw new JspException("Unable to find attribute " + bean + " in any scope");
            }
            
            String expression = (String) attributes.get("name"); 
            try {
                XMLPHRDocument document = null; 
                
                if(obj instanceof JDomForm) {
                    JDomForm form = (JDomForm)obj;
                    document = form.getDocument((HttpServletRequest) pageContext.getRequest());
                }
                else
                if(obj instanceof CCRDocument) {
                    CCRDocument ccr = (CCRDocument) obj;
                    document = ccr.getJDOMDocument();
                }
                else
                if(obj instanceof XMLPHRDocument) { 
                    document = (XMLPHRDocument) obj;
                }
                
                // Object should be one of the known types - if not, throw a meaningful error
                if(document == null) {
                    throw new JspException("Object " +  bean + " of type " + obj.getClass().getName() + " does not implement JDomForm, CCRDocument or XMLPHRDocument interface .");
                }
                
                String value = "";
                Object join = this.attributes.get("join");
                if(join != null) {
                    StringBuilder b = new StringBuilder();
                    List<String> values = document.queryTextValues(expression);
                    for(String v : values) {
                        if(b.length() > 0) {
                            b.append(join.toString());
                        }
                        b.append(v);
                    }
                    value = b.toString();
                }
                else {
                    Element valueElement = document.queryProperty(expression);
                    if(valueElement != null) {
                        // Output the value from XPath statements
                        value = escape ? Str.escapeHTMLEntities(valueElement.getTextTrim()) : valueElement.getTextTrim();
                    }                
                }
                 
                pageContext.getOut().print("value=\""+value+"\" ");                    
            }
            catch (PHRException e) {
                throw new JspException("Error executing XPath expression " + expression, e);
            }
            catch (CCROperationException e) {
                throw new JspException("Error executing XPath expression " + expression, e);
            }
            out.print("/>");
        }
        catch (IOException e) {
            throw new JspException(e);
        }
       return SKIP_BODY;
    }

    public void setAlt(String value) {
      this.attributes.put("alt",value);
    }
    public void setDisabled(String value) {
      this.attributes.put("disabled",value);
    }
    public void setIndexed(String value) {
      this.attributes.put("indexed",value);
    }
    public void setMaxlength(String value) {
      this.attributes.put("maxlength",value);
    }
    public void setName(String value) {
      this.attributes.put("name",value);
    }
    public void setOnblur(String value) {
      this.attributes.put("onblur",value);
    }
    public void setOnchange(String value) {
      this.attributes.put("onchange",value);
    }
    public void setOnclick(String value) {
      this.attributes.put("onclick",value);
    }
    public void setOndblclick(String value) {
      this.attributes.put("ondblclick",value);
    }
    public void setOnfocus(String value) {
      this.attributes.put("onfocus",value);
    }
    public void setOnkeydown(String value) {
      this.attributes.put("onkeydown",value);
    }
    public void setOnkeypress(String value) {
      this.attributes.put("onkeypress",value);
    }
    public void setOnkeyup(String value) {
      this.attributes.put("onkeyup",value);
    }
    public void setOnmousedown(String value) {
      this.attributes.put("onmousedown",value);
    }
    public void setOnmousemove(String value) {
      this.attributes.put("onmousemove",value);
    }
    public void setOnmouseout(String value) {
      this.attributes.put("onmouseout",value);
    }
    public void setOnmouseover(String value) {
      this.attributes.put("onmouseover",value);
    }
    public void setOnmouseup(String value) {
      this.attributes.put("onmouseup",value);
    }
    public void setBean(String value) {
        this.bean=value;
    }
    public void setReadonly(String value) {
      this.attributes.put("readonly",value);
    }
    public void setSize(String value) {
      this.attributes.put("size",value);
    }
    public void setStyle(String value) {
      this.attributes.put("style",value);
    }
    public void setStyleClass(String value) {
      this.attributes.put("class",value);
    }
    public void setStyleId(String value) {
      this.attributes.put("id",value);
    }
    public void setTabindex(String value) {
      this.attributes.put("tabindex",value);
    }
    public void setTitle(String value) {
      this.attributes.put("title",value);
    }
    public void setPath(String value) {
      this.attributes.put("name",value);
    }
     public void setId(String value) {
      this.attributes.put("id",value);
    }
    public void setJoin(String value) {
        this.attributes.put("join",value);
    }
    
    public void release() {
       this.attributes.clear();
       this.bean = null;
    }

    public boolean isEscape() {
        return escape;
    }

    public void setEscape(boolean escape) {
        this.escape = escape;
    }
}
