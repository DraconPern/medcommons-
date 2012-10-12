/*
 * $Id: CCRMergeLogic.java 3527 2009-10-23 04:50:53Z ssadedin $
 * Created on 16/05/2008
 */
package net.medcommons.router.services.ccr;

import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.xml.XPathUtils;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.ccr.CCRMangler;
import net.medcommons.router.services.ccrmerge.Change;
import net.medcommons.router.services.ccrmerge.MergeException;
import net.medcommons.router.services.ccrmerge.MergePolicy;
import net.medcommons.router.services.ccrmerge.MergePolicyViolationException;
import net.medcommons.router.services.ccrmerge.MergerFactory;
import net.medcommons.router.services.ccrmerge.PolicyResult;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.DocumentResolver;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Default merge algorithm that locates the user's Current CCR
 * and if found, merges with it, otherwise replaces it
 * with the existing CCR in place.
 * 
 * @author ssadedin
 */
public class CCRMergeLogic {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CCRMergeLogic.class);
    
    /**
     * The target document type (if any) to which the merge should be performed
     */
    private AccountDocumentType mergeTarget = AccountDocumentType.CURRENTCCR;
    
    protected ServicesFactory factory;
    
    public CCRMergeLogic(ServicesFactory factory) {
        this.factory = factory;
    }

    public CCRDocument merge(StoreTransaction tx) throws MergeException, PHRException {
        
        CCRDocument ccr = tx.getCcr();
        
        try {
            String patientId = ccr.getPatientMedCommonsId(); 
            CCRDocument mergeTo = this.getMergeTarget(tx); 
            
            // The resulting merged CCR
            CCRDocument mergedCcr = null; 
            
            // TODO:  Should use cached version from desktop
            ActivityEvent activityEvent = 
                new ActivityEvent(ActivityEventType.PHR_UPDATE, "PHR Updated", tx.getPrincipal(), patientId, null,null); 
            
            // does the user have a current ccr yet?
            if(mergeTo == null) { // No current CCR - set this one as their Current CCR
                log.info("User " + patientId + " has no Current CCR.  Replacing with merge target");
                mergedCcr = ccr;
                
                // Save to the user's account database
                activityEvent.setDescription("Current CCR Created");
            } 
            else { // existing current CCR, merge the updates from this one into it 
                
                log.info("Merging updates from new CCR " + ccr.getGuid() + " to target CCR " + mergeTo.getGuid() + " for patient " + patientId);
                
                MergePolicy policy = Configuration.getBean("mergePolicy");
                PolicyResult result = policy.canMerge(ccr, mergeTo);
                if(!result.allowed) {
                    log.info("Merge of ccr " + ccr.getGuid() + " into ccr " + mergeTo.getGuid() + " disallowed by policy with reason: " + result.reason);
                    throw new MergePolicyViolationException(result);
                }
                CCRMangler.mangleCCR(ccr.getRoot());
                //log.info(Str.toString(markedDoc.getRoot()));
                CCRMangler.mangleCCR(mergeTo.getRoot());
                
                ccr.syncFromJDom();
                mergeTo.syncFromJDom();
                Change c = MergerFactory.merge(ccr, mergeTo);
                CCRMangler.unMangleCCR(mergeTo.getRoot());
                mergeTo.syncFromJDom();
                CCRMangler.unMangleCCR(ccr.getRoot());
                ccr.syncFromJDom();
                
                // Ensure the timestamp is updated
                mergeTo.setCreateTimeMs(System.currentTimeMillis());
                
                // Always store under patient id
                mergeTo.setStorageId(patientId);
                
                updateChangeHistory(tx, mergeTo, c);
                
                mergedCcr = mergeTo;
            }
            
            saveMerge(ccr,mergedCcr);
            
            activityEvent.setTrackingNumber(mergedCcr.getTrackingNumber());
            factory.getActivityLogService().log(activityEvent);
            
            if(!blank(patientId) && !ServiceConstants.PUBLIC_MEDCOMMONS_ID.equals(patientId)) {
	            factory.getAccountService().addAccountDocument(
	                            mergedCcr.getPatientMedCommonsId(), 
	                            mergedCcr.getGuid(), 
	                            mergedCcr.getLogicalType(), 
	                            "Updated from " + ccr.getGuid(), 
	                            true, "Pending");
            }
            
            return mergedCcr;
        }
        catch(MergeException e) {
            throw e;
        }
        catch (IOException e) {
            throw new MergeException("Unable to merge ccr " + ccr.getGuid() + " to patient Current CCR",e);
        }
        catch(JDOMException e){
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
    } 
    
    
    protected CCRDocument getMergeTarget(StoreTransaction tx) throws MergeException {
        
        try {
            CCRDocument ccr = tx.getCcr();
            
            String patientId = XPathUtils.getValue(ccr.getJDOMDocument(),"patientMedCommonsId");
            return getMergeTarget(ccr, patientId);
        }
        catch (PHRException e) {
            throw new MergeException("Unable to resolve merge target for input CCR " + tx.getCcr().getGuid());
        }
    }

    protected CCRDocument getMergeTarget(CCRDocument ccr, String patientId) throws MergeException {
                    
        try {
            if(Str.blank(patientId)) {
                log.info("No patient id in ccr " + ccr.getGuid() + " - no merge required");
                return null;
            }
            
            // Find the patient's current ccr
            AccountSettings accountSettings = factory.getAccountService().queryAccountSettings(patientId);
            log.info("accountSettings for patient " + patientId + 
                    " currentCCRGuid =" + accountSettings.getCurrentCcrGuid() +
                    ", lastName =" + accountSettings.getLastName());
            Set<AccountDocumentType> docKeys = accountSettings.getAccountDocuments().keySet();
            Iterator<AccountDocumentType> iter = docKeys.iterator();
            while(iter.hasNext()){
                AccountDocumentType docKey = iter.next();
                String aGuid = accountSettings.getAccountDocuments().get(docKey);
                log.info("Document for key " + docKey.name() + " = " + aGuid);
            }
            String targetGuid = accountSettings.getAccountDocuments().get(mergeTarget);
            log.info("Target guid is " + targetGuid + 
                    ", current ccr guid is " + accountSettings.getCurrentCcrGuid() );
            
            DocumentResolver resolver = new DocumentResolver(factory);
            CCRDocument mergeTo = null;
            if(!Str.blank(targetGuid)) {
                // SS: TODO: IMPORTANT - why is this being accessed via the patient account id?
                // Our rule for merging should be that if the account responsible for the merge is not
                // able to access the user's current CCR then they can not merge into it.
                // Using the patientId here allows ANYBODY to merge to the current ccr of the patient                
                try {
                    mergeTo = (CCRDocument) resolver.resolveCCR(patientId, targetGuid);
                    if (mergeTo!= null)
                        log.info("getMergeTarget mergeto is " + mergeTo.getGuid());
                    else
                        log.info("resolver.resolveCCR for " + patientId + ", " + targetGuid + " returns null");
                }
                catch(RepositoryException e) {
                    // HACK: The only reason we are swallowing this exception is because of demo data
                    // If the central demo data is present but the gw content is not then we get a Current CCR guid
                    // but then when we try to resolve it we can't and the whole operation
                    // aborts, preventing demo data from loading properly
                    log.warn("Expected current CCR guid " + targetGuid + " could not be resolved.  Incoming CCR will overwrite Current CCR");
                }
            }
 
            return mergeTo;
        }
        catch (PHRException e) {
            throw new MergeException("Unable to resolve merge target for input CCR " + ccr.getGuid());
        }
        catch (ConfigurationException e) {
            throw new MergeException("Unable to resolve merge target for input CCR " + ccr.getGuid());
        }
        catch (ServiceException e) {
            throw new MergeException("Unable to resolve merge target for input CCR " + ccr.getGuid());
        }
    }

    /**
     * Attempts to update the change history of the given document consistent with the
     * given list of Changes.  If no changes are given, saves the change history
     * itself as is.
     * 
     * @param mergeTo - document to update
     * @param changes - changes, if any to add to the change history
     * @throws PHRException 
     */
    public void updateChangeHistory(StoreTransaction tx, CCRDocument mergeTo, Change... changes) throws IOException, NoSuchAlgorithmException, TransactionException, PHRException {
        
        CCRDocument ccr = tx.getCcr();
        Document changeDoc = mergeTo.getChangeHistory();
       
        if(changeDoc == null) { // No existing change history
            changeDoc = new Document(new Element("ChangeHistory"));
        }
        else {
            mergeTo.removeReference(mergeTo.getChangeHistoryGuid());
        }
        
        for(Change c : changes) {
            Element changeSet = new Element("ChangeSet");
            changeSet.addContent(new Element("Source").setText(ccr.getGuid()));
            changeSet.addContent(new Element("DateTime").setText(CCRElement.getCurrentTime()));
            changeSet.addContent(new Element("NotificationStatus").setText(CCRConstants.CCR_CHANGE_NOTIFICATION_STATUS_PENDING));
            

            Element changesElement = new Element("Changes");
            c.toXml("", changesElement);
            changeSet.addContent(changesElement);
            changeDoc.getRootElement().addContent(changeSet);
        }
        
        Format outputFormat = Format.getPrettyFormat().setEncoding("UTF-8");
        StringWriter xml = new StringWriter();  
        new XMLOutputter(outputFormat).output(changeDoc, xml);
        
        // Store the changes 
        String changesGuid = tx.getRepository().putDocument(
                ccr.getStorageId(), xml.toString(), CCRConstants.CCR_CHANGE_HISTORY_MIME_TYPE );
        
        // Remove old series
        MCSeries changeHistorySeries = 
            mergeTo.createReferenceSeries(ccr.getStorageId(),"ChangeHistory.xml", changesGuid, CCRConstants.CCR_CHANGE_HISTORY_MIME_TYPE);
        
        changeHistorySeries.setMcGUID(changesGuid);
        
        changeHistorySeries.setValidationRequired(false);
        
        mergeTo.addReferenceXML(changeHistorySeries); 
    }
    
    
    protected void saveMerge(CCRDocument ccr, CCRDocument mergedCcr) throws PHRException, CCRStoreException, ServiceException {
        mergedCcr.setLogicalType(mergeTarget);
        mergedCcr.setStorageMode(StorageMode.LOGICAL);

        StorageModel storageModel = Configuration.getBean("systemStorageModel");
        storageModel.saveCCRMerge(ccr, mergedCcr, mergeTarget);
        
    }

    public AccountDocumentType getMergeTarget() {
        return mergeTarget;
    }

    public void setMergeTarget(AccountDocumentType mergeTarget) {
        this.mergeTarget = mergeTarget;
    }

    public static Logger getLog() {
        return log;
    }

    public ServicesFactory getFactory() {
        return factory;
    } 
}
