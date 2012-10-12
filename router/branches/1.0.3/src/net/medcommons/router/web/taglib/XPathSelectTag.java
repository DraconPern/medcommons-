/*
 * $Id$
 * Created on 1/04/2005
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.util.struts.JDomForm;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * A tag sort-of-modeling struts HTML tags, but instead mapping to an XPath statement.
 * 
 * @author ssadedin
 */
public class XPathSelectTag extends BodyTagSupport {
    
    private HashMap attributes = new HashMap();
    
    private String bean;
    
    private String values;
    
    private static XPathCache xpath = null;
    
    /**
     * 
     */
    public XPathSelectTag() {
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
            
            out.print("<select ");
            
            // Output all the pass-through attributes
            for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                out.print(entry.getKey()+"=\"" + entry.getValue() + "\" "); 
            }
            out.print(">");

            // Try and get the bean
            Object obj = pageContext.findAttribute(bean);
            
            if(obj == null) {
                throw new JspException("Unable to find attribute " + bean + " in any scope");
            }
            
            // bean should be a JDomForm
            if(!(obj instanceof JDomForm)) {
                throw new JspException("Bean " +  bean + " of type " + obj.getClass().getName() + " does not implement JDomForm interface.");
            }
            
            String selected = null;
            JDomForm form = (JDomForm)obj;
            String expression = (String) attributes.get("name");
            try {
                Element valueElement = 
                        form.getDocument((HttpServletRequest) pageContext.getRequest()).queryProperty(expression);
                
                if(valueElement != null) {
                    // Output the value from XPath statements
                    selected = valueElement.getTextTrim();
                }                
            }
            catch (CCROperationException e1) {
                throw new JspException("Error executing XPath expression " + expression, e1);
            }
            catch (PHRException e) {
                throw new JspException("Error executing XPath expression " + expression, e);
            }
            
            StringTokenizer tok  = new StringTokenizer(values,",");
            while(tok.hasMoreTokens()) {
                String value = tok.nextToken();
                out.print("<option value='" + value + "' ");                
                if(value.equals(selected)) {
                    out.print("selected='true'");
                }
                out.print(">" + value + "</option>");
            }
            out.print("</select>");
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
    public void setId(String value) {
      this.attributes.put("id",value);
    }
    public void setOptions(String value) { 
        this.values = value;
      }
     public void release() {
        this.attributes.clear();
        this.bean = null;
    }
}
