/*
 * $Id: $
 * Created on Oct 28, 2004
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.NotLoggedInException;

/**
 * Renders it's body content only if the specified rights are satisifed on the
 * given storage id.
 *  
 * @author ssadedin
 */
public class PermissionsTag extends BodyTagSupport {
  
  /**
   * Rights that are checked to exist before body content is rendered
   */
  private String rights = "";
  
  /**
   * Storage id for which rights are to be checked  
   */
  private String accountId = null;
  
  /**
   * Set to true if the action is found to be permitted
   */
  private boolean permitted = false;

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
      
      try {
          UserSession desktop = UserSession.required((HttpServletRequest) pageContext.getRequest());
          String accountToQuery = Str.bvl(this.accountId, ServiceConstants.PUBLIC_MEDCOMMONS_ID).replaceAll(" ", "");       
          this.permitted = desktop.checkPermissions(accountToQuery, this.rights);
      }
      catch (ServiceException e) {
          throw new JspException("Unable to verify permissions for rights [" + this.rights + "] on account " + accountId,e);
      }
      catch (NotLoggedInException e) {
          throw new JspException("Unable to verify permissions for rights [" + this.rights + "] on account " + accountId,e);
      }
      
      return this.permitted ? super.doStartTag() : SKIP_BODY;
  }
    
  public int doEndTag() throws JspException {    
      if(this.getBodyContent()==null)
          return SKIP_BODY;
      
      String content = this.getBodyContent().getString();    
      JspWriter previousOut = this.getPreviousOut(); 
      if(permitted) {
          try {
              previousOut.write(content);
          }
          catch (IOException e) {
              throw new JspException("Unable to verify permissions for rights [" + this.rights + "] on account " + accountId,e);
          }
      }
      return SKIP_BODY;
  }

  public String getRights() {
      return rights;
  }

  public void setRights(String rights) {
      this.rights = rights;
  }

  public String getAccountId() {
      return accountId;
  }

  public void setAccountId(String storageId) {
      this.accountId = storageId;
  }

  @Override
  public void release() {
      this.permitted = false;
      super.release();
  }
}
