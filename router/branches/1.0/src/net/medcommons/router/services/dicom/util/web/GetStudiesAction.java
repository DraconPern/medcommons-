//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.dicom.util.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Adds  
 */
public class GetStudiesAction extends Action {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(GetStudiesAction.class);

  /**
   * Method execute
   * 
   * @return ActionForward
   * @throws
   */
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    log.info("Retrieving orders using hibernate ... ");
    log.info("DEAD CODE");
    /*
    try {
      Session session = HibernateUtil.currentSession();
      
      List orders = session.find("from MCOrder");
      log.info("Found " + orders.size() + " orders");;
      request.setAttribute("orders", orders);
    }
    finally {
      HibernateUtil.closeSession();
    }
    */
    return mapping.findForward("success");
  }

}