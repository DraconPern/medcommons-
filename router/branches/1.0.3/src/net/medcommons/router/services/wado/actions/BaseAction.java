/*
 * $Id: BaseAction.java 2687 2008-06-27 03:54:05Z ssadedin $
 * Created on 19/01/2006
 */
package net.medcommons.router.services.wado.actions;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.struts.action.Action;

/**
 * Not used widely yet but eventually will make all actions extend this to allow
 * for interception, security, utilities etc. to live here.
 * 
 * @author ssadedin
 */
public class BaseAction extends Action {
    
    protected void setOutputCCR(HttpServletRequest request, CCRDocument ccr) throws ServiceException {
        UserSession desktop = UserSession.get(request);
        int ccrIndex = desktop.getCcrs().indexOf(ccr);
        request.setAttribute("ccrIndex", String.valueOf(ccrIndex));
        request.setAttribute("ccr", ccr);
    }

}
