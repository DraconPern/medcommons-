/*
 * $Id: $
 * Created on Oct 28, 2004
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * A simple tag that converts a MedCommons tracking number
 * to formatted 12 digit form. 
 * 
 * @author ssadedin
 */
public class TrackingNumberTag extends BodyTagSupport {

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    return super.doStartTag();
  }
    
  public int doEndTag() throws JspException {    
    String content = this.getBodyContent().getString();    
    JspWriter previousOut = this.getPreviousOut();
    try {
      if(content.length() < 4) {
          previousOut.print(content);
      }
      else{
          previousOut.print( 
              content.substring(0,4) + 
                  (content.length() > 4 ? (" " + content.substring(4,8) + (content.length() > 8 ? " " + content.substring(8,12) : "")) : "") );
      }
    }
    catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }
}
