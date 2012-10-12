/*
 * $Id: ConfigValueTag.java 3076 2008-11-13 05:29:24Z ssadedin $
 * Created on Oct 28, 2004
 */
package net.medcommons.router.web.taglib;

import static net.medcommons.modules.utils.Str.nvl;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;

import org.apache.log4j.Logger;

/**
 * A simple tag that converts a MedCommons tracking number to formatted 12 digit
 * form.
 * 
 * @author ssadedin
 */
public class ConfigValueTag extends BodyTagSupport {
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(ConfigValueTag.class);


    private String property;
    
    private String defaultValue;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        return super.doStartTag();
    }

    public int doEndTag() throws JspException {
    	String property = this.property;
    	//log.debug("About to get property:" + property);
        try {
            String value = Configuration.getProperty(property);
            if(value == null)
                value = defaultValue;            
            
            this.pageContext.getOut().print(nvl(value, "")); 
            
            //log.debug("property value is " + value);

        }
        catch (IOException e) {
            throw new JspException(property, e);
        }
        catch (ConfigurationException e) {
            throw new JspException(property,e);
        }
        catch(RuntimeException e){
        	 throw new JspException(property,e);
        }
        return SKIP_BODY;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
    public String getDefault() {
        return defaultValue;
    }
    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}