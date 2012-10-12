/*
 * $Id$
 * Created on 11/08/2006
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.PHRProfile;
import net.medcommons.modules.services.interfaces.ProfileService;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.wado.InsufficientPrivilegeException;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.medcommons.router.web.stripes.JSON;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

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
    
    
    /**
     * Optional input. Specifies patient account id if there is no existing session.
     * Auth token must be provided in that case to allow access to the patient.
     */
    String accid;
    
    @ValidationMethod
    public void checkContentToBeStored(ValidationErrors e) { 
        if(ccr == null && blank(accid))  {
            log.info("Failed validation");
            e.add("accid", new SimpleError("If called without an existing session must provide accid parameter"));
        }
    }
    
    @DefaultHandler
    @JSON
    public Resolution query() throws Exception {
        
        HashMap<String, Object> results = new HashMap<String, Object>();
        
        // Try and get the patient id to query
        if(this.ccr != null)
            accid = this.ccr.getPatientMedCommonsId();
            
        log.info("Using patient  = " + accid);
        
        results.put("status", "ok");
        if(accid != null)  {
            if(!session.checkPermissions(accid, "R")) 
                throw new InsufficientPrivilegeException("Current session does not have necessary access to patient " + accid);
            results.put("profiles",profileService.getProfiles(accid));
        }
        else {
            results.put("profiles",new ArrayList<PHRProfile>());
            log.info("No patient id: returning blank patient CCR list");
        }

        return new StreamingResolution("text/plain",
                new JSONSerializer().include("ccrs").exclude("class").serialize(results)); 
    }
    
    /**
     * We allow external access as long as the patient account id and explicit authentication token is set
     */
    @Override
    protected void checkSID() {
        HttpServletRequest r = this.ctx.getRequest();
        if(!blank(r.getParameter("accid")) && !blank(r.getParameter("auth"))) 
            return;
        super.checkSID();
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public String getAccid() {
        return accid;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }
}
