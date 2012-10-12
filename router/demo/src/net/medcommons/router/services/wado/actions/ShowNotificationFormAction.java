/*
 * $Id: ShowNotificationFormAction.java 3132 2008-12-09 06:42:45Z ssadedin $
 * Created on Nov 1, 2004
 */package net.medcommons.router.services.wado.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.NotLoggedInException;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Initializes and shows NotificationForm
 * 
 * @author ssadedin
 */
public class ShowNotificationFormAction extends Action {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(ShowNotificationFormAction.class);


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
      UserSession desktop = null;
      if((desktop = (UserSession)request.getSession().getAttribute("desktop"))==null) {
          throw new NotLoggedInException();
      }
      
      NotificationForm.get(request);
      
      return mapping.findForward("success");
  }
}
