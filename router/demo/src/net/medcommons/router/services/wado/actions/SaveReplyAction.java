/*
 * $Id: SaveReplyAction.java 3404 2009-06-26 01:59:52Z ssadedin $
 */
package net.medcommons.router.services.wado.actions;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.BillingCounters;
import net.medcommons.modules.services.interfaces.InsufficientCreditException;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.CCRStoreException;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.ccrmerge.MergeException;
import net.medcommons.router.services.wado.InvalidPINException;
import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.JDOMException;

/** * Saves the active CCR without sending it
 *
 * @author ssadedin */
public class SaveReplyAction extends Action {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SaveReplyAction.class);

    
    public SaveReplyAction() {
    }

    /**
     * Method execute
     * 
     * @return ActionForward
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                    HttpServletResponse response) throws Exception {
        
        NotificationForm notificationForm = null;
        CCRDocument ccr = null;
        UserSession desktop = UserSession.required(request);
        try {    
            notificationForm = (NotificationForm) form;
            ccr = notificationForm.getCcr();
            
            String[][] rights = getSaveRights(desktop);
            
            saveCcr(notificationForm, request, true, rights);
            
            String destMcId = ccr.getStorageId();
            
            request.setAttribute("ccr", desktop.getCurrentCcr(request));
        }
        catch(InsufficientCreditException e) {
            log.error("Failed saving CCR due to insufficient credit", e);
            queryAndSetCounters(request, desktop);
        }
        catch(Throwable e) {
            if(notificationForm!=null) {
                log.error("Failed saving CCR " + ((ccr!=null) ? ccr.getTrackingNumber() : " null "), e);
            }
            else {
                log.error("Failed saving CCR - null notification form");
            }
           request.setAttribute("sendStatus", "FAILED");
           request.setAttribute("sendError", e.getMessage());
        }

       return mapping.findForward("success");
    }

    protected void queryAndSetCounters(HttpServletRequest request, UserSession desktop) throws ServiceException {
        request.setAttribute("sendStatus", "INSUFFICIENTCREDIT");
        BillingCounters counters = desktop.getServicesFactory().getBillingService().queryAvailableCredits(desktop.getOwnerMedCommonsId());
        request.setAttribute("counters", counters.toJSON());
    }

    public String[][] getSaveRights(UserSession desktop) {
        if(desktop.hasAccount()) {  
            return new String[][] { new String[]{ desktop.getOwnerMedCommonsId(), Rights.ALL } };
        }
        else
            return new String[][]{};
    }

    public String saveCcr(HttpServletRequest request, String[]... additionalRights) 
      throws NoSuchAlgorithmException, 
              IOException, 
              JDOMException, 
              InvalidPINException, 
              ServiceException, 
              NotLoggedInException, 
              ConfigurationException, 
              MergeException, 
              CCRStoreException, 
              PHRException 
    {
        return saveCcr(NotificationForm.get(request), request, true, additionalRights);
    }
    
    /**
     * Saves the CCR
     * <p/>
     * The resultant saved CCR is placed in the request attribute 'savedCcr'
     * @param assignPin TODO
     * @param additionalRights - optional accounts to grant rights to 
     * 
     * @throws CCRStoreException 
     * @throws PHRException 
     */
    public String saveCcr(NotificationForm form, HttpServletRequest request, boolean assignPin, String[]... additionalRights) 
        throws 
            IOException, 
            JDOMException, 
            InvalidPINException, 
            NoSuchAlgorithmException, 
            ServiceException, 
            NotLoggedInException, 
            ConfigurationException,
            MergeException, CCRStoreException, PHRException
    {
       
        HttpSession session = request.getSession();
        UserSession desktop = UserSession.required(request);
        
        // Get the current CCR
        CCRDocument ccr = form.getCcr();        
        
        ServicesFactory services = desktop.getServicesFactory();
        StoreTransaction storeTx = new StoreTransaction(services, desktop.getAccountSettings(), ccr);
        String registry = desktop.getAccountSettings().getRegistry();
        storeTx.setIdp("idp");
        if((registry != null) && "true".equals(request.getParameter("newAcct"))) {
            log.info("Creating new patient account for CCR");
            storeTx.createPatient();
            request.setAttribute("accountCreatedId", storeTx.getPatientAcctId());
            
            // Requirement:  when creating an account via Save, the first saved CCR should become
            // their Current CCR.  Note this will cause the CCR to get treated below
            // as logical instead of fixed and saved as the user's current ccr
            ccr.setStorageMode(StorageMode.LOGICAL);
            ccr.setLogicalType(AccountDocumentType.CURRENTCCR);
        }
        else{
        	log.info("Saving into existing account");
        }
        desktop.updateStorageId(ccr); 
        
        String storageId = ccr.getStorageId();
       
        ccr.syncFromJDom();        
        log.info("Saving CCR " + ccr.getGuid());
        
        // Update the reply PIN, according to this request, or set it if necessary
        setReplyPIN(desktop, form, request);  
        
        form.setNewMedcommonsPatient(Str.empty(ccr.getPatientMedCommonsId()));
        
        // Update the create time to "now"
        ccr.setCreateTimeMs(System.currentTimeMillis());
        
        // If CCR came from provisional account, now that we are saving it, confirm the account
        if(ccr.isProvisionalCcr()) {
            log.info("Confirming provisional account " + ccr.getStorageId());
            services.getAccountCreationService().confirmAccount(ccr.getStorageId());
            ccr.setProvisionalCcr(false);
        }
        
        if(assignPin)
            storeTx.registerDocument(form.getPin(), additionalRights);
        else
            storeTx.registerDocument(null, additionalRights);

        form.setTrackingNumber(ccr.getTrackingNumber());
        
        desktop.getAccountTrackingNumbers().add(ccr.getTrackingNumber());

        // Put in repository 
        storeTx.storeDocument();
        
        
        ccr.setNewCcr(false);
        
        if(ccr.getStorageMode() == StorageMode.LOGICAL) { 
            // Write to activity log
            storeTx.writeActivity(ActivityEventType.PHR_UPDATE, "PHR Updated");
            
            desktop.getAccountSettings().getAccountDocuments().put(ccr.getLogicalType(), ccr.getGuid());
        } 
        else
            storeTx.createFixedTab();
        
        storeTx.notifyRegistry();
        
        // Send "save" event
        try {
            ccr.getEvents().publish("save", ccr);
        }
        catch (Exception e) {
            throw new CCRStoreException("Unable to publish save event",e);
        }
        
        // Sometimes saving is the main target of an action, but sometimes it is a side effect.
        // In those cases it's necessary sometimes also to know about the saved ccr.
        request.setAttribute("savedCcr", ccr);            
        
        return ccr.getStorageId();
     }

    /**
     * Check if a PIN needs to be set and if so, set it, or create a default random one. 
     * 
     * @throws InvalidPINException
     */
    private void setReplyPIN(UserSession desktop, NotificationForm form, HttpServletRequest request) throws InvalidPINException {
        // In case the user changed it
        String replyPin = request.getParameter("assignedPin");
        if(replyPin != null) {
            desktop.setReplyPin(request.getParameter("assignedPin"));
        }
        
        // Requirement is to make reply CCRs go back with the same PIN they came in with
        // so check if there was a PIN used to access the CCR and if so, use it.
        replyPin = desktop.getReplyPin();
        if(!Str.blank(replyPin)) {
            if(replyPin.matches("[0-9]{5}")) {
                form.setPin(replyPin);        
            }
            else
                throw new InvalidPINException("Supplied PIN must be 5 digits exactly");
        }
        else {
            // HACK: when sending to an account, or if the current user has an account, then
            // the PIN is optional - really need to factor this out better
            if(Str.blank(request.getParameter("destAcctId")) && !desktop.hasAccount()) {
                form.setPin(PIN.generate()); // no pin - reply with new PIN
            }
            else {
                form.setPin("");
            }
        }
    }
}
