/*
 * $Id: NewCCRAction.java 3094 2008-11-21 04:08:39Z ssadedin $
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;

/**
 * Initializes the session with an optional template CCR and 
 * takes the user to it, ready to edit it. 
 * 
 * If the user is already logged in or viewing an existing POPS tracking number 
 * then the new CCR will be added to others on their session.
 * 
 * If the user is not logged in then they will be autologged in to their new
 * CCR. *  
 * @param template       a guid of a CCR to use as a template
 * @param initialFrom    email for actor from which CCR is to be sent 
 * 
 * @author ssadedin */
public class NewCCRAction extends BaseActionBean {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(NewCCRAction.class);

    
    /**
     * Guid of template to load when creating new CCR
     */
    private String template = null;
    
    /**
     * Initial from to set for new CCR, if any
     */
    private String initialFrom = null;

    @DefaultHandler
    public Resolution newCCR() throws Exception {
        
        log.info("Initializing new CCR");
        
       // The CCR for the user to edit - we will initialize it below.
        CCRDocument newCCR = null;
        
        if(template != null) {            
            // Load the template guid, if possible            
            log.debug("Loading new ccr from template " + template);
            newCCR = (CCRDocument) RepositoryFactory.getLocalRepository().queryDocument(session.getOwnerMedCommonsId(), template);
        }
        else {
            newCCR = CCRDocument.createFromTemplate(session.getOwnerMedCommonsId());
        }
        newCCR.setCreateTimeMs(System.currentTimeMillis());
        
        
        boolean newSession = false;
        
        XMLPHRDocument ccrDOM = newCCR.getJDOMDocument();
        AccountSettings settings = session.getAccountSettings();
        if(blank(initialFrom) && session.hasAccount()) {
            initialFrom = settings.getEmail();
            ccrDOM.setValue("sourceFamilyName", settings.getLastName());
            ccrDOM.setValue("sourceGivenName", settings.getFirstName());
        }
        
        if(!blank(initialFrom)) {
            newCCR.createPath("sourceEmail").setText(initialFrom);
        }
        
        if(newSession || Str.empty(session.getReplyPin())) {
            if(!session.hasAccount())
                session.setReplyPin(PIN.generate());
        }
        
        // Note: must do this before below since it resets to SCRATCH mode
        newCCR.setNewCcr(true);
        
        // If access mode is 'p' for patient, put the account id of the user as the patient id
        if(session.isPatientMode() && session.hasAccount()) { 
            newCCR.addPatientId(session.getOwnerMedCommonsId(), CCRConstants.MEDCOMMONS_PATIENT_ID_TYPE);
            newCCR.setDisplayMode("patient");
            
            // If patient does not yet have a current ccr, make this their current ccr automatically
            if(blank(settings.getCurrentCcrGuid())) {
                newCCR.setStorageMode(StorageMode.LOGICAL);
                newCCR.setLogicalType(AccountDocumentType.CURRENTCCR);
            } 
            
            // If we have them, set the first name and last name on the CCR
            ccrDOM.setValue("patientGivenName", settings.getFirstName());
            ccrDOM.setValue("patientFamilyName", settings.getLastName());
            ccrDOM.setValue("patientEmail", settings.getEmail());
        }
        newCCR.syncFromJDom();
        newCCR.getValidatedJDCOMDocument();
        
        log.info("New CCR validated");
        
        session.getCcrs().add(newCCR);
        
        int ccrIndex = session.getCcrs().indexOf(newCCR);
        ctx.getRequest().setAttribute("ccrIndex", String.valueOf(ccrIndex));
        return new RedirectResolution("/view#"+ccrIndex+"e");
    }

    @Override
    public void setContext(ActionBeanContext ctx) {
        this.initialFrom = (String) ctx.getRequest().getSession().getAttribute("initialCCRFrom");
        super.setContext(ctx);
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getInitialFrom() {
        return initialFrom;
    }

    public void setInitialFrom(String initialFrom) {
        this.initialFrom = initialFrom;
    }
}