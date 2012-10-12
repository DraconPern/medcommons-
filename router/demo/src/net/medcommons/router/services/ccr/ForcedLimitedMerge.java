/*
 * $Id: ForcedLimitedMerge.java 3501 2009-10-08 21:39:26Z ssadedin $
 * Created on 16/05/2008
 */
package net.medcommons.router.services.ccr;

import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.AccountService;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.ccrmerge.Change;
import net.medcommons.router.services.ccrmerge.MergeException;
import net.medcommons.router.services.ccrmerge.MergePolicy;
import net.medcommons.router.services.ccrmerge.PolicyResult;
import net.medcommons.router.services.ccrmerge.ReferenceIDsMergerFactory;
import net.medcommons.router.services.ccrmerge.preprocess.MarkIncomingCCR;
import net.medcommons.router.services.ccrmerge.preprocess.MarkIncomingHealthFrameCCR;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * ForceLimitedMerge is the merge invoked by HealthFrame. This differs from a normal merge
 * that is a section-by-section merge of the CCR data object. This method takes the 
 * <Body> element of the CCR from HealthFrame verbatim; other sections have their own 
 * special rules.
 * 
 * The ReferenceIDsMergerFactory is used to enforce these rules; ReferenceIDsContinuityOfCareRecordMerger
 * contains most of the specialized HealthFrame logic.
 * 
 * Refactored out of StoreTransaction.
 * 
 * @author ssadedin
 */
public class ForcedLimitedMerge extends ForcedMerge {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ForcedLimitedMerge.class);

    public ForcedLimitedMerge(ServicesFactory factory, String patientId) {
        super(factory, patientId);
    }
    
    public CCRDocument merge(StoreTransaction tx) throws MergeException, PHRException {
        
        CCRDocument ccr = tx.getCcr();
 
        log.info("Limited merge for patient " + patientId);
        String currentCcrGuid = null;
        try {
            AccountService accountService = factory.getAccountService();
            if(blank(patientId)) {
                log.info("No patient id in ccr " + ccr.getGuid() + " - no merge required");
                return null;
            }
            
            CCRDocument mergeTo = getMergeTarget(ccr, patientId);
            
            // The resulting merged CCR
            CCRDocument mergedCcr = null; 
            
            ActivityEvent activityEvent = 
                   new ActivityEvent(ActivityEventType.PHR_UPDATE, "PHR Updated", tx.getPrincipal(), patientId, null, null); 
            
            // does the user have a current ccr yet?
            if(mergeTo == null) { // No current CCR - set this one as their Current CCR
                log.info("User " + patientId + " has no Current CCR.  Replacing with merge target");
                mergedCcr = ccr;
                
                // Save to the user's account database
                activityEvent.setDescription("Current CCR Created");
            } 
            else { // existing current CCR, merge the updates from this one into it 
                
                log.info("Merging updates from new CCR " + ccr.getGuid() + " to Current CCR " + currentCcrGuid + " for patient " + patientId);
                
                MergePolicy policy = Configuration.getBean("mergePolicy");
                PolicyResult result = policy.canMerge(ccr, mergeTo);
                if(!result.allowed) {
                    log.info("Merge of ccr " + ccr.getGuid() + " into ccr " + mergeTo.getGuid() + " disallowed by policy with reason: " + result.reason);
                    return ccr; // return original
                }
                
                Change c = ReferenceIDsMergerFactory.merge(ccr, mergeTo);
                
                log.info("Limited Merge complete");
                
                // Ensure the timestamp is updated
                mergeTo.setCreateTimeMs(System.currentTimeMillis());
                
                // Always store under patient id
                mergeTo.setStorageId(patientId);
                
                updateChangeHistory(tx, mergeTo, c);
                MarkIncomingCCR markIncoming = new MarkIncomingHealthFrameCCR();
                
                mergedCcr = markIncoming.clearMarkedAttributes(mergeTo);;
            }
            
            saveMerge(ccr, mergedCcr); 

            activityEvent.setTrackingNumber(mergedCcr.getTrackingNumber());
            factory.getActivityLogService().log(activityEvent);
            
            factory.getAccountService().addAccountDocument(
                            mergedCcr.getPatientMedCommonsId(), mergedCcr.getGuid(), mergedCcr.getLogicalType(), "Updated from " + ccr.getGuid(), true, "Pending");
                        
            log.info("Finished limited merging - mergedCcr is " + mergedCcr.getGuid());
            return mergedCcr;
        }
        catch(MergeException e) {
            throw e;
        }
        catch (IOException e) {
            throw new MergeException("Unable to merge ccr " + ccr.getGuid() + " to patient Current CCR",e);
        }
        catch (ServiceException e) {
            throw new MergeException("Unable to merge ccr " + ccr.getGuid() + " to patient Current CCR",e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new MergeException("Unable to merge ccr " + ccr.getGuid() + " to patient Current CCR",e);
        }
        catch (CCRStoreException e) {
            throw new MergeException("Unable to merge ccr " + ccr.getGuid() + " to patient Current CCR",e);
        }
        catch(JDOMException e){
            throw new MergeException("Unable to merge ccr " + ccr.getGuid() + " to patient Current CCR",e);
        }
    }

}
