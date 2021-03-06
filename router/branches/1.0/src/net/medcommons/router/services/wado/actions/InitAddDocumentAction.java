//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Initializes the page to allow the user to add a document to a series
 * 
 * @author ssadedin
 */
public class InitAddDocumentAction extends Action {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(InitAddDocumentAction.class);

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

    // Store the id
    request.getSession().setAttribute("addDocumentId", request.getParameter("id"));   
    request.setAttribute("ccrIndex", request.getParameter("updateIndex"));

    UserSession desktop = UserSession.required(request);
    CCRDocument ccr = desktop.getCurrentCcr(request);
    
    return mapping.findForward("success");
  }
}
