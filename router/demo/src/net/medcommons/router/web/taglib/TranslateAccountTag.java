/*
 * $Id: TranslateAccountTag.java 3126 2008-12-08 10:06:04Z ssadedin $
 * Created on Oct 28, 2004
 */
package net.medcommons.router.web.taglib;

import static java.lang.String.format;
import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.formatPhoneNumber;
import static net.medcommons.modules.utils.Str.nvl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.account.EmailAddressCache;

import org.apache.log4j.Logger;

/**
 * Displays the email address of the given account, if it is known.  
 * IF unable to translate account to email address, displays account id instead.
 * 
 * @author ssadedin
 */
public class TranslateAccountTag extends BodyTagSupport {
    
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(TranslateAccountTag.class);
    
  /**
   * Account ID
   */
  private String account = "";
  
  /**
   * Type of account id provided / IDP
   */
  private String type = "";
  
  /**
   * AccountSpec to display
   */
  private AccountSpec spec;

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    JspWriter out = this.pageContext.getOut();
    
    EmailAddressCache cache = Configuration.getBean("emailAddressCache");
    
    
    if(blank(account) && spec != null)
        account = spec.getId();
    
    if(blank(type) && spec != null)
        type = spec.getIdType();
    
    
    String mcid = null;
    if("MedCommons".equals(type)) {
        mcid = account;
    }
    else
    if(spec != null && !blank(spec.getMcId())) {
        mcid = spec.getMcId();
    }
        
    
    try {
        UserSession desktop = UserSession.get((HttpServletRequest) this.pageContext.getRequest());
        
        if("Phone".equals(type)) {  // Phone 
            out.print(format("%s %s - Ph: %s", nvl(spec.getFirstName(),""), 
                                               nvl(spec.getLastName(),""),
                                               formatPhoneNumber(account)));
        }
        else
        if("PIN".equals(type) || ServiceConstants.PUBLIC_MEDCOMMONS_ID.equals(mcid)) {
            out.print("Anonymous User");
        }
        else
        if(blank(type) || mcid != null) { // no type or valid mcid => display mcid
            String [] email = cache.translate(desktop.getServicesFactory(), new String[] { mcid });
            if(email[0] != null) {
                out.print(email[0]);
            }
            else  
                MedCommonsIdTag.writeMcId(mcid, out);
            
            // Not medcommons type?  Show the IDP
            if("FaceBook".equals(type)) {
                out.print(String.format(" ( <a href='http://www.facebook.com/profile.php?id=%s' target='fb'>Facebook</a> )",account));
            }
            else
            if(!"MedCommons".equals(type)) {
                out.print(" ( "+type+" )");
            }
        }
        else
        if("openid".equals(type)) {  // OpenID - just show whole URL
            out.print(account);
        }
        else
        if("FaceBook".equals(type)) {  // Facebook 
            out.print(String.format("%s Account <a href='http://www.facebook.com/profile.php?id=%s' target='fb'>%s</a> - %s %s", type, account, account, nvl(spec.getFirstName(),""), nvl(spec.getLastName(),"")));
        }
        else { // other type
            if(spec != null && (!blank(spec.getFirstName() ) || !blank(spec.getLastName()))) {
                out.print(String.format("%s Account %s - %s %s", type, account, nvl(spec.getFirstName(),""), nvl(spec.getLastName(),"")));
            }
            else
                out.print(account + " (" + type + ")");
        }
    }
    catch (ServiceException e) {
        log.error("Unable to translate account " + this.account + " / spec " + spec.toString(), e);
        throw new JspException(e);
    }
    catch (IOException e) {
        log.error("Unable to translate account " + this.account + " / spec " + spec.toString(), e);
        throw new JspException(e);
    } 
    
    return super.doStartTag();
  }
    
  public int doEndTag() throws JspException {
      this.release();
      return SKIP_BODY; 
  }

  public String getAccount() {
      return account;
  }

  public void setAccount(String account) {
      this.account = account;
  }

public String getType() {
    return type;
}

public void setType(String type) {
    this.type = type;
}

public AccountSpec getSpec() {
    return spec;
}

public void setSpec(AccountSpec spec) {
    this.spec = spec;
}

@Override
public void release() {
    
    this.spec = null;
    this.account = null;
    this.type = null;
}




}
