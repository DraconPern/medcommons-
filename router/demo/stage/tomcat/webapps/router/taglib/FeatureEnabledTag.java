/*
 * $Id: HasApplicationTag.java 3489 2009-09-19 00:03:11Z ssadedin $
 * Created on Oct 28, 2004
 */
package net.medcommons.router.web.taglib;

import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.NotLoggedInException;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Renders it's body content only if the specified application
 * has access to a specified account
 *  
 * @author ssadedin
 */
public class FeatureEnabledTag extends BodyTagSupport {
    
  /**
   * Name of feature
   */
  private String name = "";
  
  /**
   * Whether to write out the body (false) or a flag (true)
   */
  private boolean flag = false;
  
  /**
   * Set to true if the feature is found to be enabled
   */
  private boolean isEnabled = false;
  
  private static List<Feature> features = null;
  
  private static ServicesFactory services = Configuration.getBean("systemServicesFactory");
  
  static {
      try {
        features = services.getAccountService().queryFeatures();
    }
    catch (ServiceException e) {
        throw new RuntimeException("Failed to query enabled features",e);
    }
  }
  
  /**
   * Thread local cache of results
   */
  private static ThreadLocal<HashMap<String, Boolean>> results = new ThreadLocal<HashMap<String,Boolean>>();

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
      
      this.isEnabled = true; 
      
      if(results.get() == null)
          results.set(new HashMap<String, Boolean>());
       
      Boolean result = (results.get().get(name));
      if(result == null) {
          result = Boolean.TRUE;
          for(Feature feature : features) {
              if(feature.match(name)) {
                  result = feature.isEnabled();
                  
                  // Exact match can't be overridden by another feature
                  if(feature.getName().length() == name.length())
	                  break;
              }
          }
      }
      isEnabled = result;
         
      return this.isEnabled && !flag ? super.doStartTag() : SKIP_BODY;
  }
    
  public int doEndTag() throws JspException {    
      
      if(flag) {
        try {
            pageContext.getOut().write(isEnabled ? "true" : "false");
        }
        catch (IOException e) {
              throw new JspException("Unable write body contents for tag [" + this.toString() + "]",e);
        }  
        return SKIP_BODY;
      }
      
      if(this.getBodyContent()==null)
          return SKIP_BODY;
      
      JspWriter previousOut = this.getPreviousOut(); 
      String content = this.getBodyContent().getString();    
      if(isEnabled) {
          try {
              previousOut.write(content);
          }
          catch (IOException e) {
              throw new JspException("Unable write body contents for tag [" + this.toString() + "]",e);
          }
      }
      return SKIP_BODY;
  }

  @Override
  public void release() {
      this.name = null;
      this.isEnabled = false;
      this.flag = false;
  }
  
  public String getName() {
      return name;
  }

  @Override
  public String toString() {
      return "FeatureEnabledTag [isEnabled=" + isEnabled + ", name=" + name + "]";
  }

public void setName(String name) {
    this.name = name;
}

public boolean isFlag() {
    return flag;
}

public void setFlag(boolean flag) {
    this.flag = flag;
}
}
