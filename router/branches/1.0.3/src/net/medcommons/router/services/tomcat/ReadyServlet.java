/*
 * $Id$
 * Created on Dec 15, 2004
 */
package net.medcommons.router.services.tomcat;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.modules.utils.metrics.TimeSampledMetric;

import org.apache.log4j.Logger;

/**
 * The whole purpose in life of this servlet is to log a message to say that startup
 * has been completed.  It should be run as the last servlet to start up.
 * NB.  This class also forces the static initialers in the WADOImageJob to
 * run by referencing the class.
 * 
 * @author ssadedin
 */
public class ReadyServlet implements Servlet {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(ReadyServlet.class);
  
  /**
   * HACK - we want the static initializers in the WADOImageJob to be guaranteed
   * to run on startup - so we just refernece the class here.
   */
  static {
    Metric.register("imageBytesPerSecond", new TimeSampledMetric(Metric.getInstance("bytesEncoded"), 500, 10));    
  }

  /**
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig arg0) throws ServletException {
    log.info("Startup Completed.");
  }

  /**
   * Dummy
   */
  public ServletConfig getServletConfig() {
    return null;
  }

  /**
   * Dummy
   */
  public void service(ServletRequest arg0, ServletResponse arg1)
      throws ServletException, IOException 
  {
    log.error("Request sent to ReadyServlet:  should be used only for initialization");    
  }

  /**
   * Dummy
   * @see javax.servlet.Servlet#getServletInfo()
   */
  public String getServletInfo() {
    return null;
  }

  /**
   * Dummy
   */
  public void destroy() {
  }
}
