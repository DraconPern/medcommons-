//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.router.services.wado.EncodeJob;
import net.medcommons.router.services.wado.EncoderManager;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Returns the status of the EncoderManager in the request context
 * 
 * @author ssadedin
 */
public class KillEncoderJob extends Action {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(KillEncoderJob.class);

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

    // Get the index from the parameter
    int index = Integer.parseInt(request.getParameter("jobIndex"));
    
    // Find the job
    try {
      EncodeJob job = (EncodeJob) EncoderManager.getInstance().getActiveJobs().get(index);
      
      if(job != null) {
        EncoderManager.getInstance().terminate(job);
        
        request.setAttribute("message", "Terminate message sent to job.  The job may not exit immediately.");
      }
      else {
        request.setAttribute("message", "Unable to locate selected job.");
      }
    }
    catch(IndexOutOfBoundsException e) {
      request.setAttribute("message", "Unable to locate selected job.");
    }
    
    return mapping.findForward("success");
  }
}