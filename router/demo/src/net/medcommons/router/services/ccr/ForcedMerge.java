/*
 * $Id: ForcedMerge.java 3501 2009-10-08 21:39:26Z ssadedin $
 * Created on 16/05/2008
 */
package net.medcommons.router.services.ccr;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.AccountService;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.ccrmerge.Change;
import net.medcommons.router.services.ccrmerge.MergeException;
import net.medcommons.router.services.ccrmerge.MergePolicy;
import net.medcommons.router.services.ccrmerge.MergerFactory;
import net.medcommons.router.services.ccrmerge.PolicyResult;
import net.medcommons.router.services.ccrmerge.preprocess.MarkIncomingCCR;
import net.medcommons.router.services.ccrmerge.preprocess.MarkIncomingHealthFrameCCR;
import net.medcommons.router.services.repository.DocumentResolver;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * Attempts to merge the contents of the CCR for this transaction 
 * with that of the patient's Current CCR.
 * 
 * The Current CCR is determined by querying the account service based
 * on the patient id stored in the CCR.
 * 
 * Merges even if the account information is missing.
 * 
 * ****  This method may be obsolete.
 * 
 * @return - the merged document
 * @throws PHRException 
 */
public class ForcedMerge extends CCRMergeLogic {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ForcedMerge.class);
    
    /**
     * Patient id that the merge will be forced to use.  
     */
    protected String patientId = null;

    public ForcedMerge(ServicesFactory factory, String patientId) {
        super(factory);
        this.patientId = patientId;
    }
    
    public CCRDocument merge(StoreTransaction tx) throws MergeException, PHRException {
        
        CCRDocument ccr = tx.getCcr();

        String currentCcrGuid = null;
        try {
            if(Str.blank(patientId)) {
                log.info("No patient id in ccr " + ccr.getGuid() + " - no merge required");
                return null;
            }
            
            AccountService accountService = factory.getAccountService();
            
            // Find the patient's current ccr
            AccountSettings accountSettings = accountService.queryAccountSettings(patientId);
            currentCcrGuid = accountSettings.getCurrentCcrGuid();
            
            DocumentResolver resolver = Configuration.getBean("documentResolver");
            CCRDocument mergeTo = null;
            if(!Str.blank(currentCcrGuid)) {
                // SS: TODO: IMPORTANT - why is this being accessed via the patient account id?
                // Our rule for merging should be that if the account responsible for the merge is not
                // able to access the user's current CCR then they can not merge into it.
                // Using the patientId here allows ANYBODY to merge to the current ccr of the patient                
                try {
                    mergeTo = (CCRDocument) resolver.resolveCCR(patientId, currentCcrGuid);
                }
                catch(RepositoryException e) {
                    // HACK: The only reason we are swallowing this exception is because of demo data
                    // If the central demo data is present but the gw content is not then we get a Current CCR guid
                    // but then when we try to resolve it we can't and the whole operation
                    // aborts, preventing demo data from loading properly
                    log.warn("Expected current CCR guid " + currentCcrGuid + " could not be resolved.  Incoming CCR will overwrite Current CCR");
                }
            }
            
            // The resulting merged CCR
            CCRDocument mergedCcr = null; 
            
            ActivityEvent activityEvent = 
                new ActivityEvent(ActivityEventType.PHR_UPDATE, "PHR Updated2", tx.getPrincipal(), patientId, null, null); 
            
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
                
                Change c = MergerFactory.merge(ccr, mergeTo);
                
            log.info("Merge complete");
                // Ensure the timestamp is updated
                mergeTo.setCreateTimeMs(System.currentTimeMillis());

                
                // Always store under patient id
                mergeTo.setStorageId(patientId);
                
                updateChangeHistory(tx, mergeTo, c);
                MarkIncomingCCR markIncoming = new MarkIncomingHealthFrameCCR();
                
                mergedCcr = markIncoming.clearMarkedAttributes(mergeTo);;
            }
            
            mergedCcr.setLogicalType(AccountDocumentType.CURRENTCCR);
            mergedCcr.setStorageMode(StorageMode.LOGICAL);
            
            StorageModel storageModel = Configuration.getBean("systemStorageModel");
            storageModel.saveCCRMerge(ccr, mergedCcr, AccountDocumentType.CURRENTCCR);

            activityEvent.setTrackingNumber(mergedCcr.getTrackingNumber());
            factory.getActivityLogService().log(activityEvent);
            accountService.addAccountDocument(
                            patientId, mergedCcr.getGuid(), AccountDocumentType.CURRENTCCR, "Updated from " + ccr.getGuid(), true, "Pending");
                        
        log.info("Finished merging - mergedCcr is " + mergedCcr.getGuid());
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
        catch (ConfigurationException e) {
            throw new MergeException("Unable to merge ccr " + ccr.getGuid() + " to patient Current CCR",e);
        }
        catch(JDOMException e){
            throw new MergeException("Unable to merge ccr " + ccr.getGuid() + " to patient Current CCR",e);
        }
    }    
}
