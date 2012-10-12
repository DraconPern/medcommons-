/*
 * $Id$
 * Created on 05/07/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.bvl;

import java.util.HashMap;
import java.util.List;

import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.ccrmerge.MergePolicyViolationException;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;

/**
 * Attempts to merge the active CCR into the patient's current CCR. M
 * 
 * @author ssadedin
 */
public class MergeWithCurrentCCRAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(MergeWithCurrentCCRAction.class);
    
    @DefaultHandler
    public Resolution merge() throws Exception {
        HashMap<String, String> status = new HashMap<String, String>();
        try {
            // Must have a valid medcommons patient id to perform merge
            // We prefer the account id of the patient in the CCR we are merging,
            // However if that CCR has no patient we will merge to the patient 
            // for other CCRs in the session provided there is only ONE such 
            // patient loaded.
            String patientId = this.ccr.getPatientMedCommonsId();
            if(blank(patientId)) {
                patientId = this.session.getPatientAccountId();
            }
            
            if(blank(patientId)) {
                throw new CCROperationException("Patient must have valid MedCommons Account Id to merge with Current CCR");
            }

            // CCR must be saved
            //if(blank(ccr.getGuid()))
            //    throw new CCROperationException("CCR must be saved to merge into Current CCR");
            
            log.info("Merging CCR " + ccr.getGuid() + " into Current CCR for patient " + patientId);
            StoreTransaction tx = null;
            CCRDocument merged = null;
            if(blank(session.getAccountSettings(patientId).getCurrentCcrGuid())) { // No existing current CCR. Save this one AS Current CCR
                ccr.syncFromJDom();
                tx = session.tx(this.ccr.copy());
                tx.registerDocument(null,new String[] { session.getOwnerMedCommonsId(), Rights.ALL});
                tx.storeDocument();
                merged = tx.merge();
            }
            else { // Existing Current CCR - Merge with it
                tx = session.tx(this.ccr);
                merged = tx.merge();
            }
            
            // Update our session
            session.getAccountSettings(patientId).setCurrentCcrGuid(merged.getGuid());
            List<CCRDocument> ccrs = session.getCcrs();
            for(int i=0; i<ccrs.size(); ++i) {
                CCRDocument c = ccrs.get(i);
                if(c.getLogicalType()==AccountDocumentType.CURRENTCCR) {
                    ccrs.set(i, merged);
                }
            }
            status.put("status","ok");
        }
        catch(MergePolicyViolationException e) {
            status.put("status","violation");
            status.put("error",e.getResult().format(session.getMessages()));
        }
        catch(Exception e) {
            log.error("Merge to current ccr for user " + ccr.getPatientMedCommonsId() + " failed",e);
            status.put("status","failed");
            status.put("error",bvl(e.getMessage(), e.toString()));                    
        }
        return new JavaScriptResolution(status);
    }

}
