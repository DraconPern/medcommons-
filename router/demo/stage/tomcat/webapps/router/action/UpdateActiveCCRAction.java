/*
 * $Id$
 */
package net.medcommons.router.services.xds.consumer.web.action;

import static net.medcommons.modules.utils.Algorithm.map;
import static net.medcommons.modules.utils.Str.blank;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Function;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRActorElement;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.account.EmailAddressCache;
import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.services.wado.actions.LoginUtil;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Updates the current CCR in the session with data from the current page
 */
public class UpdateActiveCCRAction extends Action {

    /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(UpdateActiveCCRAction.class);

	/**
	 * Method execute
	 * 
	 * @return ActionForward
	 * @throws
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception { 
      
	    LoginUtil.checkAutoLogin(request);
        String forwardName = request.getParameter("forward");
	    if(!UserSession.has(request)) {
            if("home".equals(forwardName)) {
                String url = Configuration.getProperty("homeUrl");
                if(url != null)
                    return new ActionForward(url,true);
                else
	                return mapping.findForward("home");
            }
            else
                throw new NotLoggedInException();
	    }

        UserSession session = UserSession.get(request);
        List<CCRDocument> ccrs = session.getCcrs();
        
        CCRDocument ccrDoc = session.getCurrentCcr(request);      
        if(ccrDoc != null) {
            // Add account ids to actors that may have been created for emails
            AccountSettings settings = session.getAccountSettings();
            resolveActorAccountIds(session.getServicesFactory(), ccrDoc, settings);
            ccrDoc.syncFromJDom();
        }
        
        
        
        // The desired CCR to display (ccrIndex) may not be the CCR that we just updated.
        // (eg. the user may be transitioning from one tab to another)
        // Hence promote the ccrIndex over the updateIndex by making it
        // a request attribute which gets higher priority than the updateIndex parameter
        request.setAttribute("ccrIndex", request.getParameter("ccrIndex"));
        
        if(ccrs != null) {
            request.getSession().setAttribute("contacts", CCRDocument.createMasterContactList(ccrs));
        }
        
        if(ccrDoc != null && request.getParameter("ccrPurpose") != null) {
            ccrDoc.setSubjectText(request.getParameter("ccrPurpose"));
        }
        
        if(forwardName != null) {
            // Special handling for 'home' forward - send to configured url in config file
            // If no url defined, proceed to use that defined in actions.xml
            if("home".equals(forwardName)) {
                String url = Configuration.getProperty("homeUrl");
                if(url != null)
                    return new ActionForward(url,true);
            }
            return mapping.findForward(forwardName);   
        }
        else            
            return mapping.findForward("success");
	}

	/**
	 * Search for actor emails in the given document and  attempt to convert them
	 * into  MedCommons Account IDs by looking them up.
	 */
    public static void resolveActorAccountIds(ServicesFactory servicesFactory, CCRDocument ccr, AccountSettings settings) throws PHRException, ServiceException {
        
        String patientActorId = ccr.getRoot().getChild("Patient").getChildText("ActorID");
            
        List<CCRElement> actors = ccr.getRoot().getChild("Actors").getChildren();
        ArrayList<CCRActorElement> unresolved = new ArrayList<CCRActorElement>(); 
        for(CCRElement e : actors) {
            CCRActorElement a = (CCRActorElement) e;
            if(a.getAccountId() == null) {
                if(!blank(a.getEmail())) {
	                if(a.getEmail().equals(settings.getEmail()))
	                    a.setAccountId(settings.getAccountId(), a.getChildText("ActorObjectID"));
	                else
	                    unresolved.add(a);
                }
            }
        }
         
        if(!unresolved.isEmpty()) { 
            List<String> accountIds = EmailAddressCache.translateAccounts(servicesFactory,
                            map(unresolved, new Function<String, CCRActorElement>() {
                                public String $(CCRActorElement u) {
                                    return u.getEmail();
                                }
                            }));
            
            for (int i = 0; i < accountIds.size(); i++) {
                if(accountIds.get(i) != null)  {
                    log.info("Resolved account id " + accountIds.get(i) + " for actor " + unresolved.get(i).getEmail());
                    unresolved.get(i).setAccountId(accountIds.get(i), patientActorId);
                }
            }
            
        }
    }
}
    

