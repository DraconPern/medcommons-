/*
 * $Id: JSONTag.java 3039 2008-11-03 10:31:22Z ssadedin $
 * Created on 31/10/2008
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import flexjson.JSONSerializer;

/**
 * Tag for rendering an arbitrary object as JSON
 * 
 * @author ssadedin
 */
public class JSONTag extends BodyTagSupport {
    
    Object src;
    
    String includes;
    
    String excludes;
    
    public int doStartTag() throws JspException {
        return super.doStartTag();
    }
    
    public int doEndTag() throws JspException {    
        
        try {
            JSONSerializer json = new JSONSerializer();
            
            if(includes != null)
                json.include(includes.split(","));
            
            if(excludes != null)
                json.exclude(excludes.split(","));
            
            json.exclude("class");
            
            pageContext.getOut().append(json.serialize(src)); 
        }
        catch (IOException e) {
          throw new JspException(e);
        }
        return SKIP_BODY;
      }

    public Object getSrc() {
        return src;
    }

    public void setSrc(Object src) {
        this.src = src;
    }

    public String getIncludes() {
        return includes;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }
}
