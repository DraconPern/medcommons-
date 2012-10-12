/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import java.util.HashMap;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.InsufficientRightsException;
import net.medcommons.modules.services.interfaces.ProfileService;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

/**
 * Changes status of requested CCR in the CCR Log
 * so that it becomes no longer displayed in CCR Form. 
 *  
 * @author ssadedin
 */
public class HideCCRAction extends CCRJSONActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(HideCCRAction.class);
    
    ProfileService profileService = Configuration.getBean("profilesService");
    
    @Validate(required=true)
    String profileId;

    public HideCCRAction() {
        super();
    }
     
    @DefaultHandler
    public Resolution hide() throws Exception {                
       
        HashMap<String, String> status = new HashMap<String, String>();
        String patientMedCommonsId = this.ccr.getPatientMedCommonsId();
        if(Str.blank(patientMedCommonsId)) {
            throw new CCROperationException("Cannot hide CCR with no Patient Account Id");
        }
        
        if(!session.checkPermissions(patientMedCommonsId, "W")) {
            throw new InsufficientRightsException("Hiding a CCR requires Write Privileges on the Patient Account");
        }
        
        if(this.ccr.getGuid()==null) 
            throw new CCROperationException("CCR is unsaved.  Cannot hide unsaved CCR");
        
        // Remove the tab
        profileService.hideProfile(patientMedCommonsId, profileId);
        
        // Add note to activity log
        ActivityEvent evt = new ActivityEvent(ActivityEventType.PHR_UPDATE, "CCR Hidden", session.getOwnerPrincipal(), patientMedCommonsId, ccr.getTrackingNumber(),null);
        session.getServicesFactory().getActivityLogService().log(evt);
        
        status.put("status","ok");
       
        return new JavaScriptResolution(status);
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
     

}
