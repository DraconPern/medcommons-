/*
 * $Id: $
 * Created on Oct 28, 2004
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * A simple tag that converts a MedCommons tracking number
 * to formatted 12 digit form. 
 * 
 * @author ssadedin
 */
public class MedCommonsIdTag extends BodyTagSupport {

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
      writeMcId(content, previousOut);
    }
    catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }

  public static void writeMcId(String content, Writer out) throws IOException {
      
      final int length = content.length();
      for(int i=0; i<length; ++i) {
          if(i > 0 && i % 4 == 0)
              out.append(' ');
              
          out.append(content.charAt(i));
      }
    }
}
