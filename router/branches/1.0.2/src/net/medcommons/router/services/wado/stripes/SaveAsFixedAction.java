/*
 * $Id$
 * Created on 05/07/2007
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.PHRProfile;
import net.medcommons.modules.services.interfaces.ProfileService;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.medcommons.router.web.stripes.JSON;
import net.medcommons.router.web.stripes.JSONResolution;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.apache.log4j.Logger;

/**
 * Copies the existing CCR and opens it in Edit mode as a new
 * CCR that will not get merged back to the Current CCR. 
 * 
 * @author ssadedin
 */
public class SaveAsFixedAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SaveAsFixedAction.class);
    
    private String replyPin;
    
    private String assignedPin;
    
    private String ccrPurpose;

    public ProfileService profileSvc = Configuration.getBean("profilesService");

    /**
     * Output
     */
    private PHRProfile profile; 
    
    /**
     * Creates a new fixed CCR and returns a JSON result
     */
    @JSON
    public Resolution saveTab() throws Exception {
        
       CCRDocument newCCR = createTab();
       
       log.info("Saved CCR as new profile");
       
       return new JSONResolution(profile.toJSON());
    }
    
    /**
     * Creates a new CCR and returns an HTML result
     * @return
     * @throws Exception
     */
    @DefaultHandler
    public Resolution saveFixed() throws Exception {
       
       CCRDocument newCCR = createTab();
       
       // Set it as the "current" ccr
       session.setActiveCCR(ctx.getRequest(), newCCR);
       
       return new ForwardResolution("/viewEditCCR.do"); 
    }

    /**
     * Create a new fixed CCR based on the parameters specified
     */
    protected CCRDocument createTab() throws Exception {
        
        // Copy the existing CCR
        CCRDocument newCCR = this.ccr.copy();
        
        // Use assigned pin if provided
        if(!Str.blank(assignedPin)) {
            replyPin = assignedPin;
        }
        
        // Use FIXED mode to indicate it's been saved permanently
        newCCR.setStorageMode(StorageMode.FIXED);
        newCCR.setLogicalType(null);
        newCCR.setCreateTimeMs(System.currentTimeMillis());
        newCCR.setAccessPin(replyPin); 
        session.getCcrs().add(newCCR);
        
        // Fix the content
        newCCR.syncFromJDom();
        newCCR.calculateGuid();
        
        // Now store 
        log.info("Saving CCR " + newCCR.getGuid() + " as new FIXED CCR");
        StoreTransaction storeTx = new StoreTransaction(session.getServicesFactory(), session.getAccountSettings(), newCCR);
        storeTx.registerDocument(replyPin,new String[] { session.getOwnerMedCommonsId(), Rights.ALL});
        storeTx.storeDocument();
        
        profile = new PHRProfile(storeTx.getDocumentGuid());
        profileSvc.createProfile(storeTx.getPatientAcctId(), profile);
        
        storeTx.notifyRegistry();
        newCCR.setGuid(storeTx.getDocumentGuid());
        return newCCR;
    }
    
    public String getReplyPin() {
        return replyPin;
    }

    public void setReplyPin(String assignedPin) {
        this.replyPin = assignedPin;
    }

    public String getAssignedPin() {
        return assignedPin;
    }

    public void setAssignedPin(String assignedPin) {
        this.assignedPin = assignedPin;
    }

}
