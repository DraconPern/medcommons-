/*
 * $Id: HasApplicationTag.java 3489 2009-09-19 00:03:11Z ssadedin $
 * Created on Oct 28, 2004
 */
package net.medcommons.router.web.taglib;

import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.medcommons.modules.services.interfaces.Application;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.NotLoggedInException;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Renders it's body content only if the specified application
 * has access to a specified account
 *  
 * @author ssadedin
 */
public class HasApplicationTag extends BodyTagSupport {
  
  /**
   * Name of application
   */
  private String name = "";
  
  /**
   * Code of application
   */
  private String code = "";
  
  /**
   * Key of application
   */
  private String key = null;
  
  /**
   * Account id to check
   */
  private String accountId = null;
  
  /**
   * Set to true if the action is found to be permitted
   */
  private boolean hasApplication = false;
  

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
      
      this.hasApplication = false; 
      
      if(blank(accountId))
          accountId = ServiceConstants.PUBLIC_MEDCOMMONS_ID;
              
      try {
          UserSession desktop = UserSession.required((HttpServletRequest) pageContext.getRequest());
          
          for(Application app : desktop.getAccountSettings(accountId).getApplications()) {
              if(!blank(name) && name.equals(app.getName())) {
                  hasApplication = true;
                  break;
              }
              if(!blank(code) && code.equals(app.getCode())) {
                  hasApplication = true;
                  break;
              }
              if(!blank(key) && key.equals(app.getKey())) {
                  hasApplication = true;
                  break;
              }
          }
      }
      catch (ServiceException e) {
          throw new JspException("Unable to check if user has app [" + this.toString() + "] on account " + accountId,e);
      }
      catch (NotLoggedInException e) {
          throw new JspException("Unable to check if user has app [" + this.toString() + "] on account " + accountId,e);
      }
      
      return this.hasApplication ? super.doStartTag() : SKIP_BODY;
  }
    
  public int doEndTag() throws JspException {    
      if(this.getBodyContent()==null)
          return SKIP_BODY;
      
      String content = this.getBodyContent().getString();    
      JspWriter previousOut = this.getPreviousOut(); 
      if(hasApplication) {
          try {
              previousOut.write(content);
          }
          catch (IOException e) {
              throw new JspException("Unable write body contents for tag [" + this.toString() + "] on account " + accountId,e);
          }
      }
      return SKIP_BODY;
  }
  
  @Override
  public String toString() {
      return ToStringBuilder.reflectionToString(this);
  }
  
  public String getAccountId() {
      return accountId;
  }

  public void setAccountId(String storageId) {
      this.accountId = storageId;
  }

  @Override
  public void release() {
      this.hasApplication = false;
      this.name = null;
      this.code = null;
      this.accountId = null;
      this.key = null;
      super.release();
  }
  
  public String getName() {
      return name;
  }
  
  public void setName(String name) {
      this.name = name;
  }
  
  public String getCode() {
      return code;
  }
  
  public void setCode(String code) {
      this.code = code;
  }
  
  public String getKey() {
      return key;
  }
  
  public void setKey(String key) {
      this.key = key;
  }
}
