/*
 * $Id$
 * Created on 26/09/2006
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.ccr.StorageModel;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;

/**
 * Sets the currently active CCR as the Current CCR
 * for the Patient Actor in that CCR.
 * 
 * @author ssadedin
 */
public class SetCurrentCCRAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SetCurrentCCRAction.class);

    private StorageModel storageModel = Configuration.getBean("systemStorageModel");
    
    /**
     * Sets the currently active CCR (as indicated by ccrIndex parameter) 
     * as the active 
     * 
     * @return
     */
    @DefaultHandler
    public Resolution set() {
        String patientId = null;
        try {
            patientId = this.ccr.getValue("patientMedCommonsId");
            if(Str.empty(patientId)) {
                throw new IllegalArgumentException("CCR must have valid Patient MedCommons Id to save as Current CCR");
            }
            
            String currentCcrGuid = storageModel.replaceCurrentCCR(patientId, ccr);
            
            // If the patient is cached in this session then ensure their settings are updated
            AccountSettings patientSettings = session.getAccountSettings(patientId);
            if(patientSettings != null) {
                patientSettings.setCurrentCcrGuid(currentCcrGuid);
            }
            
            // Write to activity log
            session.getServicesFactory().getActivityLogService().log(
                new ActivityEvent(ActivityEventType.PHR_UPDATE, "Current CCR Set", session.getOwnerPrincipal(), patientId, ccr.getTrackingNumber(), null));
            
             this.ctx.getRequest().setAttribute("ccr", this.ccr);
             
            ctx.getRequest().setAttribute("sendStatus", "SUCCESS");
        }
        catch(Exception e) {
            log.error("Failed to set CCR " + this.ccr.getGuid() + " as current CCR for patient id "+patientId, e);
            ctx.getRequest().setAttribute("sendStatus", "FAILED");
            ctx.getRequest().setAttribute("sendError", e.getMessage());
        }
        
        return new ForwardResolution("/sendResult.jsp");
    }
}
