/*
 * $Id$
 * Created on 1/04/2005
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import net.medcommons.modules.utils.Str;

/**
 * A tag sort-of-modeling struts HTML tags, but instead mapping to an XPath
 * statement.
 * 
 * @author ssadedin
 */
public class XPathNodeTag extends XPathValueTag {

    /**
     * Name of node
     */
    private String name;
    
    /**
     *  
     */
    public XPathNodeTag() {
        super();
    }
    
    /**
     * Writes the value found.
     * 
     * @param obj
     * @throws IOException
     * @throws JspException
     */
    protected void writeTag(Object obj) throws IOException, JspException {
        
        if(Str.blank(name)) {
            throw new JspException("Attribute name must be non-null and non-blank");
        }
        
        // Output the value from XPath statements
        pageContext.getRequest().setAttribute(this.name, obj);
    }
 

    public void release() {
        super.release();
        this.name = null;
   }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setForceList(boolean value) {
        this.alwaysList = true;
    }
}
