/*
 * $Id: ShareOpenIDAction.java 2664 2008-06-23 04:34:12Z ssadedin $
 * Created on 14/09/2007
 */
package net.medcommons.router.services.wado.stripes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryInformation;

/**
 * Accepts a list of OpenIDs and shares the active CCR with each one
 * 
 * @author ssadedin
 */
public class ShareOpenIDAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ShareOpenIDAction.class);
    
    List<String> shareIds = new ArrayList<String>();
    
    @DefaultHandler
    public Resolution share() throws Exception {
        
        
        HashMap<String, String> result = new HashMap<String, String>(); 
        try {
            
            String patientId = ccr.getPatientMedCommonsId();
            
            if(Str.blank(patientId)) 
                throw new CCROperationException("No valid patient account id set");
            
            
            // Perform discovery to resolve the OpenID
            ConsumerManager manager = new ConsumerManager();
            this.ctx.getRequest().getSession().setAttribute("openid-manager", manager);
            
            for (String id : shareIds) { 
                
                id = id.trim(); 
                
                log.info("id = " + id);
                
                if(!id.contains("*")) { 
                    // perform discovery on the user-supplied identifier
                    try {
                        List<DiscoveryInformation> discoveries = manager.discover(id);
                        log.info("Added open-id share for resolved id = " + discoveries.get(0).getIdpEndpoint());
                    }
                    catch(Exception ex) {
                        throw new CCROperationException("A problem occurred validating the OpenID that you provided.  Please check it and try again");
                    }
                }
                this.session.getServicesFactory().getDocumentService().grantAccountAccess(patientId, id, Rights.ALL);
                
            }
            result.put("status","ok");
        }
        catch (Exception e) {
            log.error("Failed to share account with open ids " + this.shareIds, e);
            result.put("status","failed");
            result.put("error",e.getMessage());
        }
        return new JavaScriptResolution(result);
    }

    public List<String> getShareIds() {
        return shareIds;
    }

    public void setShareIds(List<String> shareIds) {
        this.shareIds = shareIds;
    }

}
