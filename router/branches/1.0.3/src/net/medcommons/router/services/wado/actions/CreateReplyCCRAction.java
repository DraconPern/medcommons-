/*
 * $Id: CreateReplyCCRAction.java 2664 2008-06-23 04:34:12Z ssadedin $
 */
package net.medcommons.router.services.wado.actions;

import static net.medcommons.modules.utils.Str.blank;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Creates a reply CCR and sets it as the viewable CCR so that
 * display will show it.  If the CCR has a patient account associated
 * with it then will also configure the reply CCR as a logical CCR
 * with the type REPLYCCR.
 * 
 * @author ssadedin */
public class CreateReplyCCRAction extends Action {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CreateReplyCCRAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                    HttpServletResponse response) throws Exception {
        
        UserSession desktop = UserSession.required(request);
        
        CCRDocument ccr = desktop.getReplyCcr(desktop.getCurrentCcr(request));
        if(!desktop.getCcrs().contains(ccr)) {
            desktop.getCcrs().add(ccr);
        }
        
        String replyPin = request.getParameter("replyPin");
        if(!blank(replyPin)) {
            ccr.setAccessPin(replyPin);
        }
        
        // If there is a patient account, set it as the REPLY CCR for that account.
        if(!blank(ccr.getPatientMedCommonsId())) {
            ccr.setStorageMode(StorageMode.LOGICAL);
            ccr.setLogicalType(AccountDocumentType.REPLYCCR);
        }
        else { // no patient account - set as scratch type
            ccr.setStorageMode(StorageMode.SCRATCH);
            ccr.setLogicalType(null);
        }
        
        int ccrIndex = desktop.getCcrs().indexOf(ccr);
        log.info("Created Reply CCR at ccr index " + ccrIndex);
        desktop.setActiveCCR(request, ccr);
        request.setAttribute("ccr",ccr);
        
        return mapping.findForward("success");
    }
}