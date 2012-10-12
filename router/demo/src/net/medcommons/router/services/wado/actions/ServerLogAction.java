//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.actions;

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Reads the tomcat log file and sends it back as the response.
 * 
 * @author ssadedin
 */
public class ServerLogAction extends Action {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(ServerLogAction.class);

  /**
   * Method execute
   * 
   * @return ActionForward
   * @throws
   * @throws ConfigurationException -
   *           if configuration cannot be accessed
   * @throws SelectionException -
   *           if a problem scanning the selections occurs
   */
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {
      
      if(!Configuration.getProperty("EnableRemoteLog",false)) {
          throw new SecurityException("Remote log not enabled on this server.");
      }
          
      response.setContentType("text/plain");
      File logFile = null;
      try{
    	  //Enumeration<Appender> appenders = log.getRootLogger().getAllAppenders();
    	 
    	 
    	  FileAppender fileAppender = (FileAppender) log.getRootLogger().getAppender("FILE");
    	 
    	  if (fileAppender != null){
    		  String filename = fileAppender.getFile();
    		  logFile = new File(filename);
	    	  if (logFile.exists()){
			      // Open the log file
			      byte [] buffer = new byte[4096];
			      FileInputStream in = new FileInputStream(logFile);
			      int len = -1;
			      while((len = in.read(buffer)) > 0) {
			          response.getOutputStream().write(buffer,0,len);
			      }
    		  }
	    	  else{
	    		  response.getOutputStream().println("Expected log file:" + logFile.getAbsolutePath() + " does not exist");
	    	  }
    	  }
    	  else
    	  {
    		  response.getOutputStream().println("No 'FILE' log services available on this machine");
    	  }
      }
      catch(Exception e){
    	  e.printStackTrace(response.getWriter());
      }
      return null;
  }
}