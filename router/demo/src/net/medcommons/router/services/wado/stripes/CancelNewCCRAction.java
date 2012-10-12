/*
 * $Id$
 * Created on 07/08/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.eq;

import java.util.HashMap;
import java.util.List;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

/**
 * Cancels the existing New CCR so that the currently active patient
 * has no New CCR. 
 * 
 * @author ssadedin
 */
public class CancelNewCCRAction extends CCRActionBean {
    
    ProfileService profileService = Configuration.getBean("profilesService");

    @DefaultHandler
    public Resolution cancel() {

        HashMap<String, String> result = new HashMap<String, String>();

        try {
            String patientId = this.ccr.getPatientMedCommonsId();
            if(blank(patientId))
                throw new CCROperationException("Cannot cancel New CCR for CCR without patient account");
            
            session.getServicesFactory().getAccountService().removeAccountDocument(patientId, AccountDocumentType.NEWCCR);
            
            hideNewCCRProfile(patientId);
            
            result.put("status","success");
        }
        catch (Exception e) {
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }

        return new JavaScriptResolution(result);
    }

    public void hideNewCCRProfile(String patientId) throws ServiceException {
        // If the user has a NEWCCR profile, then remove it
        List<PHRProfile> profiles = profileService.getProfiles(patientId); 
        for(PHRProfile p : profiles) {
            if(eq(p.getName(), AccountDocumentType.NEWCCR.name())) {
                profileService.hideProfile(patientId, p.getProfileId());
            }
        }
    }

}
