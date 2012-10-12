/*
 * $Id: ListenForAttachmentsAction.java 3026 2008-10-29 03:39:38Z ssadedin $
 * Created on 27/10/2008
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.eq;

import java.util.Hashtable;
import java.util.List;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.PHRProfile;
import net.medcommons.modules.services.interfaces.ProfileService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.phr.PHRException;
import net.medcommons.router.globalcontext.Transactions;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

/**
 * Causes the user's session to be subscribed to listens for attachments 
 * uploaded for the patient of the specified CCR and when one is found
 * to update the CCR with the new attachments.
 * 
 * @author ssadedin
 */
public class ListenForAttachmentsAction extends CCRJSONActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ListenForAttachmentsAction.class);
    
    private ProfileService profileService = Configuration.getBean("profilesService");
    
    @Validate(required=true) 
    String guid;
    
    @SuppressWarnings("unchecked")
    @DefaultHandler
    public Resolution listen() throws Exception {
        long maxListenTime = 30000;
        long totalListenTime = 0;
        while(true) {
            Hashtable transactions = Transactions.transactions;
            if(transactions.containsKey(guid)) {
                List<String> docs = (List<String>) transactions.get(guid);
                if(docs == null) 
                    throw new CCROperationException("A transaction occurred but no DICOM was found in the uploaded data.");
                log.info("Received " + docs.size() + " documents in notification for CXP Upload");
                
                // Find the tab associated with the guids and remove it
                List<PHRProfile> profiles = profileService.getProfiles(ccr.getPatientMedCommonsId());
                synchronized(docs) {
	                for(PHRProfile p : profiles) {
	                    if(!blank(p.getGuid())) {
	                        for(String g : docs) {
	                            log.info("comparing profile guid " + p.getGuid() + " to transaction guid " + g);
	                            if(eq(g, p.getGuid())) {
	                                profileService.hideProfile(ccr.getPatientMedCommonsId(), p.getProfileId());
	                            }
	                        }
	                    }
	                }
                }
                
                result.put("result","found");
                break;
            }
            
            Thread.sleep(1000);
            totalListenTime += 1000;
            if(totalListenTime > maxListenTime) {
                result.put("result", "timeout");
                break;
            } 
        }
        return new StreamingResolution("plain/text",this.result.toString());
    }

    private void mergeAttachments(List<String> docs) throws ServiceException, PHRException {
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
