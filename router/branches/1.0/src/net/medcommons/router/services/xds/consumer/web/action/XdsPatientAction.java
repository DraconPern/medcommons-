/*
 * Created on Dec 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.services.xds.consumer.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * @author sean
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XdsPatientAction extends Action  {


  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(XdsPatientAction.class);

  /**
   * Method execute
   * 
   * @return ActionForward
   * @throws
   */
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    log.info("Should select patient ");
    
   
    
    return mapping.findForward("success");
  }
}
