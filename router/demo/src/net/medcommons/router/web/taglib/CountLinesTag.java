/*
 * $Id: $
 * Created on Oct 28, 2004
 */
package net.medcommons.router.web.taglib;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.medcommons.modules.utils.Str;

/**
 * Renders an estimate of the number of lines that a given value 
 * will be expected to take based on a particular width and 
 * new lines that are in the value.  
 *  
 * @author ssadedin
 */
public class CountLinesTag extends BodyTagSupport {

    public String value;

    public int cols;

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
      return SKIP_BODY;
  }
  
  public int doEndTag() throws JspException {
      int rows = Str.countRows(value, cols);
      try {
        this.pageContext.getOut().write(String.valueOf(rows));
      } 
      catch (IOException e) {
          throw new JspException("Unable to calculate lines in value " + value,e);
      }
      return SKIP_BODY;
  }
  
  public int count() {
     return Str.countRows(value, cols); 
  }

    
  public int getCols() {
      return cols;
  }

  public void setCols(int cols) {
      this.cols = cols;
  }

  public String getValue() {
      return value;
  }

  public void setValue(String value) {
      this.value = value;
  }
}
