/*
 * $Id: $
 * Created on Oct 28, 2004
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Converts a time in milliseconds into flexible description of the age
 * such as "1 day ago" or "32 minutes ago"
 * 
 * @author ssadedin
 */
public class AgeTag extends BodyTagSupport {

  private static final int ONE_HOUR = 3600000;
  private static final int ONE_DAY = 86400000; 
  
  /**
   * TODO
   */
  private String maxUnit = null;

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
        long timeMs = Long.parseLong(content);
        long ageMs = System.currentTimeMillis() - timeMs;
        
        boolean future = ageMs < 0;
        String suffix = " ago";
        String prefix = "";
        if(future) {
            ageMs = -ageMs; 
            suffix = "";
            prefix = "in ";
        }
        
        if(ageMs > ONE_DAY * 7) {
            previousOut.print((future ? "on " : "") + new SimpleDateFormat("MM/dd/yyyy").format(new Date(timeMs)));
        }
        else
        if(ageMs > ONE_DAY) {
            previousOut.print( prefix + (ageMs / ONE_DAY) + " days" + suffix);
        }
        else
        if(ageMs >= ONE_HOUR) {
            previousOut.print( prefix + ((ageMs % ONE_DAY) /  ONE_HOUR) + " hrs " +  ((ageMs % 3600000) / 60000) + " mins" + suffix );
        }
        else { // less than 1 hour, show minutes only
            previousOut.print( prefix + ((ageMs % 3600000) / 60000) + " mins" + suffix );
        }
    }
    catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }

  public String getMaxUnit() {
      return maxUnit;
  }
  
  public void setMaxUnit(String maxUnit) {
      this.maxUnit = maxUnit;
  }
}
