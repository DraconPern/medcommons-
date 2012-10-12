//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class EncoderStatusAction extends Action {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(EncoderStatusAction.class);

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

    request.setAttribute("activeJobs", EncoderManager.getInstance().getActiveJobs());
    request.setAttribute("waitingJobs", EncoderManager.getInstance().getWaitingJobs());
    
    return mapping.findForward("success");
  }
}