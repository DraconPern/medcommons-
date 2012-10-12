/*
 * $Id: BaseTag.java 3603 2010-03-02 20:09:09Z ssadedin $
 * Created on Oct 28, 2004
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * A very simple tag that just renders a <base> tag
 * pointing at the context root of the application
 * 
 * @author ssadedin
 */
public class BaseTag extends BodyTagSupport {

  public int doStartTag() throws JspException {
    return super.doStartTag();
  }
    
  public int doEndTag() throws JspException {    
      
      HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
      
      String serverPort = (request.getServerPort()==80||request.getServerPort()==443)?"":":"+request.getServerPort(); 
      String baseUrl = request.getScheme() + "://" +request.getServerName()+serverPort +request.getContextPath() + "/";
      
      JspWriter out = this.pageContext.getOut();
      try {
        out.print("<base href='" + baseUrl + "'/>");
      }
      catch (IOException e) {
          throw new JspException(e);
      }
      return SKIP_BODY;
  }
}
