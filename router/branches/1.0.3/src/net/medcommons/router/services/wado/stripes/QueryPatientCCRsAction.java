/*
 * $Id$
 * Created on 11/08/2006
 */
package net.medcommons.router.services.wado.stripes;

import java.util.ArrayList;
import java.util.HashMap;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.PHRProfile;
import net.medcommons.modules.services.interfaces.ProfileService;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.medcommons.router.web.stripes.JSON;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.log4j.Logger;

import flexjson.JSONSerializer;

/**
 * Queries all CCRs for the patient for the active CCR 
 * and returns them as a JSON string.
 * 
 * @author ssadedin
 */
public class QueryPatientCCRsAction extends CCRJSONActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(QueryPatientCCRsAction.class);
    
    private ProfileService profileService = Configuration.getBean("profilesService");

    HashMap<String, Object> results = new HashMap<String, Object>();
    
    @DefaultHandler
    @JSON
    public Resolution query() throws Exception {
        
        // Try and get the patient id to query
        String  patientId = this.ccr.getPatientMedCommonsId();
        log.info("Using patient  = " + patientId);
        
        results.put("status", "ok");
        if(patientId != null)  {
        
            results.put("profiles",profileService.getProfiles(patientId));
            
         /*
            out = session.getServicesFactory().getAccountService().queryCCRLog(patientId);
            
            // Add the current ccr, if available, at the start
            // Huge hack:  when using fixed content storage model, the current ccr is recorded in the ccr log
            // just like all the others.  But in the non-fixed-content model it is not returned.  To make the
            // results look uniform, add it back in
            if(Configuration.getBean("systemStorageModel") instanceof NamedDocumentStorageModel) {
                JSONObject obj = new JSONObject(out);
                JSONArray ccrs = obj.getJSONArray("ccrs");
                JSONObject currentCcr = new JSONObject();
                currentCcr.put("guid", NamedDocumentStorageModel.CURRENTCCR_DOCUMENT_NAME);
                currentCcr.put("tracking", "");
                currentCcr.put("date", CCRElement.EXACT_DATE_TIME_FORMATTER.format(System.currentTimeMillis()));
                ccrs.put(ccrs.length(), currentCcr);            
                out = obj.toString();
            }
            */
        }
        else {
            results.put("profiles",new ArrayList<PHRProfile>());
            log.info("No patient id: returning blank patient CCR list");
        }

        return new StreamingResolution("text/javascript",
                new JSONSerializer().include("ccrs").exclude("class").serialize(results)); 
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }
}
