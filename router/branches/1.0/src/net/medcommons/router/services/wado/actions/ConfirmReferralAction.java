/*
 * $Id: ConfirmReferralAction.java 3537 2009-11-02 10:15:38Z ssadedin $
 */
package net.medcommons.router.services.wado.actions;

import static net.medcommons.modules.utils.Str.blank;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.InsufficientCreditException;
import net.medcommons.modules.services.interfaces.PHRProfile;
import net.medcommons.modules.services.interfaces.ProfileService;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.wado.stripes.CancelNewCCRAction;
import net.medcommons.router.services.wado.utils.AccountUtil;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/** * Sends the active CCR as a Referral, including sending
 * a notification, logging activity, and saving updates to the logical CCR
 * being edited (if any).
 * <p>
 * In the current implementation, a send actually translates to two
 * saves.  The first save updates the existing logical CCR being edited
 * (if any) in the patient's account and the second one
 * is the actual CCR sent, which becomes the "Fixed" version that
 * appears as a tab. These CCRs are identical except for the CCRDocumentObjectId.
 * <p>
 * 
 * 
 * @author ssadedin */
public class ConfirmReferralAction extends SaveReplyAction {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ConfirmReferralAction.class);
    
    ProfileService profiles = Configuration.getBean("profilesService");

    public ConfirmReferralAction() {
    }

    /**
     * Method execute
     * 
     * @return ActionForward
     * @throws
     * @throws ConfigurationException -
     *             if configuration cannot be accessed
     * @throws SelectionException -
     *             if a problem scanning the selections occurs
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                    HttpServletResponse response) throws Exception {

        log.info("Confirming Referral");
        NotificationForm notificationForm = (NotificationForm) form;

        UserSession desktop = UserSession.required(request); 
        CCRDocument ccr = notificationForm.getCcr();
        
        
        try {    
        	if (ccr == null){
        		throw new IllegalArgumentException("CCR for referral is not defined");
        	}
        	ccr.setNewCcr(false);
            ccr.setCreateTimeMs(System.currentTimeMillis());
            
            // First save the CCR itself - this saves the existing logical CCR
            String storageAccountId =  saveCcr(notificationForm, request, false, getSaveRights(desktop));
  
            // The CCR has been saved, but we still need to create a fixed version for the send
            // We need to put this CCR aside and make a new one with a new 
            // tracking number
            CCRDocument fixedCCR = ccr.copy();
            
            fixedCCR.setStorageMode(StorageMode.FIXED);
            fixedCCR.setLogicalType(null); 
            desktop.getCcrs().add(fixedCCR);         
            
            fixedCCR.setDocumentObjectId(fixedCCR.getRoot().generateObjectID());
            fixedCCR.syncFromJDom();

            StoreTransaction storeTx = new StoreTransaction(desktop.getServicesFactory(), desktop.getAccountSettings(), fixedCCR);
            String destAcctId = request.getParameter("destAcctId");
            String toEmail = ccr.getJDOMDocument().getValue("toEmail");            
            String desc = "Send to " + Str.bvl(toEmail, destAcctId);
            
            String[][] creationRights = resolveCreationRights(desktop, destAcctId);
            
            ServicesFactory services = desktop.getServicesFactory();
            storeTx.registerDocument(notificationForm.getPin(), creationRights);
            notificationForm.setTrackingNumber(fixedCCR.getTrackingNumber());
            
            // Put in repository 
            storeTx.storeDocument();
            
            // Create a profile ("Tab") for the sent CCR
            if(AccountUtil.isRealAccountId(storeTx.getPatientAcctId()))
	            profiles.createProfile(storeTx.getPatientAcctId(), new PHRProfile(storeTx.getDocumentGuid()));
            
             // Make sure we log with the right tracking number, account id (if any)
            AccountSpec ownerPrincipal = desktop.getOwnerPrincipal();
            String accessId = ownerPrincipal.getId();
            if((blank(accessId) || ServiceConstants.PUBLIC_MEDCOMMONS_ID.equals(accessId)) && desktop.getAccessTrackingReference() != null) 
                ownerPrincipal = new AccountSpec(desktop.getAccessTrackingReference().getTrackingNumber(),"Tracking Number");
            
            ActivityEvent activityEvent = 
                new ActivityEvent(ActivityEventType.PHR_SEND, desc, ownerPrincipal, ccr.getPatientMedCommonsId(), null, notificationForm.getPin());
            
            activityEvent.setTrackingNumber(fixedCCR.getTrackingNumber());
            
            // If there is a patient account affected by this transaction, log it to the patient's 
            // activity log
            if(!blank(ccr.getPatientMedCommonsId())) {
                activityEvent.setAffectedAccountId(ccr.getPatientMedCommonsId());
                services.getActivityLogService().log(activityEvent);
            }
            
            // Requirement:  11.1.11 - Remove New CCR when sending.
            if(ccr.getLogicalType() == AccountDocumentType.NEWCCR) { 
                services.getAccountService().removeAccountDocument(ccr.getStorageId(), AccountDocumentType.NEWCCR);
                new CancelNewCCRAction().hideNewCCRProfile(ccr.getStorageId());
            }
            
            if (desktop.hasAccount() && !blank(destAcctId)) { // is this an Account => Account send?
                // Note: the account id might be that of a group or an individual: it doesn't matter.
                log.info("Inheriting rights from account " + desktop.getOwnerMedCommonsId() + " to "+ destAcctId);
                services.getDocumentService().inheritRights(desktop.getOwnerMedCommonsId(), destAcctId, null, storageAccountId, fixedCCR.getGuid());
            }
            
            // Notify recipient
            storeTx.sendEmail(destAcctId);
            
            request.setAttribute("ccr", fixedCCR);            
            request.setAttribute("ccrIndex", desktop.getCcrs().indexOf(fixedCCR));            
            request.setAttribute("sendStatus", "SUCCESS");            
        }
        catch(InsufficientCreditException e) {
            log.error("Failed saving CCR due to insufficient credit", e);
            queryAndSetCounters(request, desktop);
        }
        catch(Exception e) {
           log.error("Failed sending CCR " + ((ccr != null) ? ccr.getTrackingNumber() : "null"), e);
           request.setAttribute("sendStatus", "FAILED");
           request.setAttribute("sendError", Str.bvl(e.getMessage(), e.toString()));
        }

       return mapping.findForward("success");
    }

    /**
     * Determine rights to grant for send operation for the given destination account
     * (which might be empty) for the current user.
     * 
     * @param desktop
     * @param destAcctId
     * @return - a 2d array of account id, rights string pairs.
     */
    private String[][] resolveCreationRights(UserSession desktop, String destAcctId) {
        ArrayList<String[]> creationRights = new ArrayList<String[]>();
        if(!desktop.hasAccount() && !Str.blank(destAcctId)) { // POPS Send to Account - add rights to the account directly
            creationRights.add(new String[] { destAcctId, Rights.ALL});
        }
        
        if(desktop.hasAccount()) {
            creationRights.add(new String[] { desktop.getOwnerMedCommonsId(), Rights.ALL});
        }
        return creationRights.toArray(new String[creationRights.size()][2]);
    }
}