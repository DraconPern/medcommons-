/*
 * $Id$
 * Created on 01/09/2006
 */
package net.medcommons.router.services.ccr;

import static java.util.Collections.singleton;
import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.eq;
import static net.medcommons.router.services.wado.utils.AccountUtil.isRealAccountId;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.GuidGenerator;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.xml.XPathUtils;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ValidationResult;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.ccrmerge.MergeException;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.wado.utils.AccountUtil;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;

/**
 * Provides high level support for storing CCRs and performing the associated operations
 * that make a complete storage transaction.  These include (approximately in this order):
 * 
 * <li>Registering the document with central services, including allocating a tracking number
 * <li>Physical storage of the CCR
 * <li>Optionally creating a patient account for the CCR if none already exists
 * <li>Granting rights to the storing user and the patient
 * <li>Merging changes from the stored CCR into the patient's Current CCR 
 * <li>Sending notifications to secondary registry
 * <li>Logging the transaction the CCR Log (making it potential visible as a tab in the UI)
 * <li>Logging the activity in the Activity Log
 * 
 * <p>Note 1: that currently the StoreTransaction is <i>not</i> a transactional
 * operation. A failure in later portions does not roll back earlier portions,
 * nor is there any locking to ensure that concurrent updates that may happen
 * in separate threads do not interfere with the outcome.  However the intention
 * is that eventually some of these transactional characteristics may be applied here.
 * 
 * @author ssadedin
 */
public class StoreTransaction {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(StoreTransaction.class);
    
    /**
     * Factory for creating services for the transaction
     */
    private ServicesFactory factory = null;
    
    /**
     * CCR to be stored
     */
    private CCRDocument ccr;
    
    /**
     * Guid of stored document.  Note that this is not initialized
     * until the document is registered (since it is not fixed until the document is finalized).
     */
    private String documentGuid;
    
    /**
     * Contains the patient account (if any) of the patient that is the subject of the transaction
     * (and referenced as such in the CCR).
     */
    private String patientAcctId;
    
    private String patientGivenName;
    
    private String patientFamilyName;
    
    private AccountSettings accountSettings;
    
    /**
     * Account settings for patient, if different to transaction user
     */
    private AccountSettings patientAccountSettings;
    
    private String idp;

	private LocalFileRepository localFileRepository = null;
    
    private XPathCache xpath = (XPathCache) Configuration.getBean("ccrXPathCache");
    
    /**
     * The identity of the party responsible for this transaction - will be written to 
     * activity log
     */
    private AccountSpec principal;
    
    /**
     * Hasher to use when hashing PINs
     */
    private PIN.Hasher pinHasher = PIN.SHA1Hasher;
    
    /**
     * PIN associated with CCR when it was saved, if any
     */
    private String savedPin;
    
    /**
     * Algorithm to use for merge
     */
    private CCRMergeLogic mergeLogic;

    /**
     * Creates a new StoreTransaction
     * 
     * @param factory       factory to use for accessing services
     * @param settings      account settings of user saving CCR (Not necessarily the patient)
     * @param ccr           the CCR to be stored by the transaction
     */
    public StoreTransaction(ServicesFactory factory, AccountSettings settings, CCRDocument ccr) throws CCRStoreException {
        super();
        this.factory = factory;
        this.mergeLogic = new CCRMergeLogic(factory);
        this.ccr = ccr; 
        this.accountSettings = settings;
        assert ccr.getStorageId() != null && !LocalFileRepository.HACK_MEDCOMMONS_ID.equals(ccr.getStorageId()) : "ccr must have storage id set" ;
        
        try {
            this.patientGivenName = ccr.getPatientGivenName();
            this.patientFamilyName = ccr.getPatientFamilyName();
            this.patientAcctId = ccr.getPatientMedCommonsId();
        }
        catch (PHRException e) {
            throw new CCRStoreException(e);
        }
    }
    
    /**
     * Creates a MedCommons patient account representing the patient actor in 
     * the CCR.  No check is performed to see if an account already exists.
     * 
     * @throws CCRStoreException 
     * @throws InsufficientCreditException - if no available account has credit to pay for the transaction
     */
    public void createPatient() throws CCRStoreException, InsufficientCreditException {
        try {
            
            List<BillingCharge> charges = resolveNewAccountPayer();
            // List<BillingCharge> charges = new ArrayList<BillingCharge>(); // billing for new accounts is disabled for now
                
            AccountCreationService acctCreationService = factory.getAccountCreationService();
            Set<String> accounts = new HashSet<String>();
            accounts.addAll(accountSettings.getCreationRights());
            patientAcctId = acctCreationService.register(AccountType.SPONSORED,
                            XPathUtils.getValue(ccr.getJDOMDocument(),"patientEmail"), 
                            null,  // no password
                            patientGivenName, patientFamilyName, "",null, this.getAccountSettings().getRegistry(), null, null, null,null)[0];
            
            // Add the ID to the Patient
            ccr.addPatientId(patientAcctId, CCRConstants.MEDCOMMONS_PATIENT_ID_TYPE);
            
            // Req. 2.1 Authorization to Account Creator
            if(accountSettings.getAccountId()!=null) {  
                accounts.add(accountSettings.getAccountId());
            }
            
            if(!accounts.isEmpty()) {
                factory.getDocumentService().grantAccountAccess(patientAcctId, Str.join(accounts.toArray(), ","), Rights.ALL);
            }
            
            // Everything worked: bill the account
            if(!charges.isEmpty())
                factory.getBillingService().charge(charges.get(0));
        }
        catch(InsufficientCreditException e) {
            throw e;
        }
        catch (ServiceException e) {
            throw new CCRStoreException(e);
        }
        catch (PHRException e) {
            throw new CCRStoreException(e);
        }
    }

    /**
     * Determines an account to charge for the cost of creating a new account.
     * 
     * @return list of billing charges
     * @throws ServiceException
     * @throws InsufficientCreditException
     */
    private List<BillingCharge> resolveNewAccountPayer() throws ServiceException, InsufficientCreditException {
        // TODO:  Should move the billing into the identity server 
        // however, to do that we need to add config support to the id server
        // so that it can resolve the billing service.  I'd prefer to delay that
        // until we have sorted out exactly where the billing service is going
        // to be deployed in production machines.  If it's always available
        // on localhost then life is easy.
        AccountSpec spec = factory.getDocumentService().queryPrincipal();
        
        List<BillingCharge> charges = new ArrayList<BillingCharge>();
        if(Configuration.getProperty("EnableBilling",false)) {
            // Resolve payers
            List<String> billableAccounts = new ArrayList<String>();
            if(!blank(spec.getMcId())) {
                // Bill the user's group in preference to them personally, if they have one
                AccountSettings authedUserSettings = factory.getAccountService().queryAccountSettings(spec.getMcId());
                if(!blank(authedUserSettings.getGroupId())) {
                    billableAccounts.add(accountSettings.getGroupId());
                }
                billableAccounts.add(spec.getMcId());
            }
            
            log.info("Resolved " + billableAccounts.size() + " billable accounts to pay for new sponsored account");
            
            charges = factory.getBillingService().resolvePayer(billableAccounts, singleton(new BillingEvent(BillingEventType.NEW_ACCOUNT)));
            if(charges.isEmpty()) {
                throw new InsufficientCreditException("Accounts " + Str.join(billableAccounts, ",") + " have insufficient credit to pay for new account");
            }
        }
        return charges;
    }
    
    

    /**
     * Create a fixed tab for the CCR saved in this transaction
     * <p>
     * A fixed tab is identified by a guid and appears as a 
     * tab identified by its date in the viewer interface.
     * 
     * @throws ServiceException
     */
    public void createFixedTab() throws ServiceException {
        // Add tab
        ProfileService profiles = Configuration.getBean("profilesService");
        String profileAcctId = blank(this.patientAcctId) ? this.ccr.getStorageId() : this.patientAcctId;
        if(isRealAccountId(profileAcctId)) {
	        log.info("Creating fixed tab for CCR " + this.ccr.getGuid() + " in profiles for account " + profileAcctId);
	        profiles.createProfile(profileAcctId,new PHRProfile(this.getDocumentGuid())); 
        }
    }
    
    /**
     * Checks if a registry exists for the user performing the transaction and if so,
     * notifies the secondary registry about this transaction.
     *  
     * @throws CCRStoreException
     */
    public void notifyRegistry() throws CCRStoreException {
        try {
            if(blank(this.accountSettings.getRegistry()) && blank(this.getPatientAccountSettings().getRegistry())) {
                log.info("No registry configured for user " + accountSettings.getAccountId());
                return;
            }
            
            String patientIdValue = "";
            String patientIdSource = ""; 

            // NOTE: We used to have some logic that would set the first non-medcommons
            // patient id as the registry patient id if there was no medcommons id for
            // the patient.  This is now removed - we'd rather send null than 
            // send an id we don't know how to handle on the other end.
            if(!blank(this.patientAcctId)) {
                patientIdValue = this.patientAcctId;
                patientIdSource = CCRConstants.MEDCOMMONS_PATIENT_ID_TYPE;
            }
            
            log.info("about to notifyRegistry of CCR with purpose " + ccr.getDocumentPurpose());
            SecondaryRegistryService srs = factory.getSecondaryRegistryService();
            Date patientDateOfBirth = ccr.getPatientDateOfBirth();
            
            // Notify each registry, but don't notify the same registry twice
            // if there are duplicates
            Set<String> registries = new HashSet<String>();
            registries.add(this.accountSettings.getRegistry());
            registries.add(this.getPatientAccountSettings().getRegistry());
                 
            for(String registry : registries) {
                if(blank(registry))
                    continue;
                
                srs.addCCREvent(
                        patientGivenName, 
                        patientFamilyName,
                        ccr.getPatientGender(),
                        patientIdValue,
                        patientIdSource, 
                        idp,
                        idp, 
                        patientDateOfBirth != null ? patientDateOfBirth.toGMTString() : null,
                        ccr.getPatientAge(),
                        ccr.getTrackingNumber(),
                        "", // PIN no longer sent to registry!
                        ccr.getGuid(),
                        ccr.getDocumentPurpose(), 
                        "", 
                        "MedCommons",
                        Configuration.getProperty("RemoteAccessAddress")+"/access?g="+ccr.getGuid(), // viewer url
                        ccr.getPurposeText(), 
                        registry
                );
            }
        }
        catch (PHRException e) {
            throw new CCRStoreException(e);
        }
        catch (ServiceException e) {
            throw new CCRStoreException(e);
        }
        catch (ConfigurationException e) {
            throw new CCRStoreException(e);
        }
    }          

    /**
     * Sends an email to the recipient email address specified in the CCR. 
     * 
     * @param destAcctId - the account id of the recipient, if any.
     */
    public void sendEmail(String destAcctId) throws PHRException, ServiceException {
        this.sendEmail(destAcctId, null);
    }
    
    public void sendEmail(String destAcctId, String messageTemplate) throws PHRException, ServiceException {
        sendEmail(destAcctId, messageTemplate, null, null);
    }
    
    /**
     * Sends an email to the recipient email address specified in the CCR. 
     * 
     * @param messageTemplate   template to use for email (if any) 
     * @param destAcctId        the account id of the recipient, if any.
     */
    public void sendEmail(String destAcctId, String messageTemplate, String subject, String comments) throws PHRException, ServiceException {
        if(this.documentGuid == null) 
            throw new IllegalStateException("Cannot send email until document is registered.  Please store and register document first.");
        
        NotifierService notifierService = factory.getNotifierService();
        if(blank(subject))
            subject = this.ccr.getPurposeText();
        String toEmail = ccr.getJDOMDocument().getValue("toEmail");            
        notifierService.sendEmailCXP(destAcctId, toEmail, this.ccr.getTrackingNumber(), messageTemplate, subject, comments);
    }
    

    /**
     * Registers the guid of this document with central services, also allocating
     * a tracking number and registering the associated PIN (if any).
     * 
     * @param pin - unhashed pin. may be null, in which case a tracking number 
     *              will be created with null pin (still valid, but not usable for
     *              accessing the CCR via pin).
     *              
     * @param additionalRights - optional list of 2 element string arrays containing 
     *                           account/rights pairs to grant for accessing the document.
     *                           For example, if you had one additional account that
     *                           should have ALL rights, pass { { accountId, Rights.ALL }}
     * @throws CCRStoreException 
     */
     public void registerDocument(String pin, String[]... additionalRights) throws CCRStoreException {
        this.registerDocument(pin, TrackingAccessConstraint.UNLIMITED, additionalRights);
    }
    
    /**
     * Registers the guid of this document with central services, also allocating
     * a tracking number and registering the associated PIN (if any).
     * 
     * @param pin - unhashed pin. may be null, in which case a tracking number 
     *              will be created with null pin (still valid, but not usable for
     *              accessing the CCR via pin).
     *              
     * @param constraint         constraint to apply to tracking number, if any 
     * @param additionalRights   optional list of 2 element string arrays containing 
     *                           account/rights pairs to grant for accessing the document.
     *                           For example, if you had one additional account that
     *                           should have ALL rights, pass { { accountId, Rights.ALL }}
     * @throws CCRStoreException 
     */
    public void registerDocument(String pin, TrackingAccessConstraint constraint, String[]... additionalRights) throws CCRStoreException {
        try {
            String pinHash = Str.blank(pin) ? null : this.pinHasher.hash(pin);
            
            // Generate a unique document id   
            String docId = 
                new GuidGenerator().generateGuid(String.valueOf(System.currentTimeMillis()).getBytes());         
            
            ccr.getJDOMDocument().setValue("CCRDocumentObjectID", docId);    
            
            this.documentGuid = ccr.calculateGuid();
            
            log.info("About to submit CCRDocumentObjectID " + docId + " (guid="+documentGuid+") to repository");         
            
            // If the hasher hashed the pin then supply the raw pin to the registration call
            String rawPin = (!Str.equals(pinHash, pin)) ? pin : null;
            
            if(!blank(pin) && !blank(rawPin))
                this.savedPin = rawPin;

            DocumentRegistration docReg = null;
            int expirySeconds = Configuration.getProperty("TrackingNumberPinExpirySeconds", 0);
            
            docReg = factory.getTrackingService().registerTrackDocument(
                    ccr.getStorageId(), 
                    documentGuid,  
                    pinHash, 
                    rawPin, 
                    new Long(expirySeconds),
                    constraint, 
                    additionalRights);

            ccr.setGuid(this.documentGuid); 
            ccr.setTrackingNumber(docReg.getTrackingNumber());
            
            List<String> guids = new ArrayList<String>();
            if(ServiceConstants.PUBLIC_MEDCOMMONS_ID.equals(ccr.getStorageId())) {
                for(MCSeries series : ccr.getSeriesList()) {
                    if(series.isInSession())
                        guids.add(series.getMcGUID());
                }
            }
            if(!guids.isEmpty())
	            factory.getDocumentService().addTrackingRights(docReg.getTrackingNumber(), pinHash, guids);
        }
        catch (NoSuchAlgorithmException e) {
            throw new CCRStoreException(e);
        }
        catch (IOException e) {
            throw new CCRStoreException(e);
        }
        catch (ServiceException e) {
            throw new CCRStoreException(e);
        }
        catch (PHRException e) {
            throw new CCRStoreException(e);
        }
    }
    
    /**
     * Sends the CCR of the transaction to physical storage. Note that it is essential
     * that the document be registered prior to storage;  unregistered documents
     * will be stored but encryption keys will be denied at central and thus
     * the stored document will be innaccessible even if later registered.      * 
     * @throws CCRStoreException
     */
    public void storeDocument() throws CCRStoreException {
        this.storeDocument(this.ccr);
         
        if (!this.ccr.getGuid().equals(documentGuid))
            throw new CCRStoreException("Document guid " + documentGuid + " does not match repository guid " + ccr.getGuid());
    }
    
    /**
     * Sends the the document to physical storage, and for logical documents also
     * updates the logical document in the patient's account. 
     * 
     * Note that it is essential that the document be registered prior to storage;  
     * unregistered documents will be stored but encryption keys will be denied 
     * at central and thus the stored document will be innaccessible even if 
     * later registered. 
     * 
     * This function may also re-store the attachments on a CCR if the
     * storage id of the CCR was changed after the attachments were loaded.
     * 
     * @throws CCRStoreException
     */
    private void storeDocument(CCRDocument toStore) throws CCRStoreException {
        try {
            if(this.documentGuid == null) {
                throw new IllegalStateException("Cannot store document until it is registered.  Please register document first.");
            }
            
            // Validate
            ValidationResult result = toStore.getJDOMDocument().validate();
            if(!result.isPassed()){
            	log.error("CCR failed validation\n" + toStore.getXml());
                throw new CCRStoreException("CCR Failed Validation: " + (result.errors.isEmpty() ? result.fatal.get(0) : result.errors.get(0)));
            }
            // The JDOM document is always the authoritative source - hence we synch from JDOM to ensure
            // that there are no inconsistencies.
            toStore.syncFromJDom();     
            String doc = toStore.getXml();
            String storageId = toStore.getStorageId();
            
            String guid = this.getRepository().putDocument(storageId, doc, CCRConstants.CCR_MIME_TYPE);
            
            // Update the guid on the CCR.  This is important, because it is now becoming fixed content.
            toStore.setGuid(guid);
            
            // Store each attachment, if necessary
            boolean first = true;
            for(MCSeries s : toStore.getSeriesList()) {
                // Already storing the CCR, don't store that again
                if(first) {
                    first = false;
                    continue;
                }
                
                // References without guids don't need to be stored
                if(Str.blank(s.getMcGUID()))  
                    continue;
                
                boolean inRepository  = false;
                try{
                  
                    inRepository = this.getRepository().inRepository(storageId, s.getMcGUID());
                    log.info("Testing to see if reference exists for " + storageId + ", " + s.getMcGUID() + " = " + inRepository);
                }
                catch(RepositoryException e){
                    log.error("Error testing to see if document " + storageId + "," + s.getMcGUID() + " is in repository",e);
                }
                if (!inRepository){
                    //  TODO:  Better logic here would just be to test if the storage account has the content or not
                    // and if not just store it.  Then can remove the 'originalStorageId' field which is going to be
                    // dodgy and a source of bugs later on.
                    if(!Str.equals(s.getStorageId(), s.getOriginalStorageId())) {
                        log.info("Reference " + s.getMcGUID() + " has changed storage id from " + s.getOriginalStorageId() + " to " + s.getStorageId() + ": attempting to re-store");
                        factory.getDocumentService().addDocument(storageId,s.getMcGUID());
                        this.getRepository().putDocument(storageId, this.getRepository().getDocument(s.getOriginalStorageId(), s.getMcGUID()), s.getMimeType());
                        s.resetOriginalStorageId();
                    }
                }
            }
            
            log.info("Submitted document " + guid + " to MedCommons repository");
            
            if(!blank(this.patientAcctId) && ccr.getStorageMode() == StorageMode.LOGICAL) { 
                
                log.info("Updating logical document type " + ccr.getLogicalType() + " for patient " + this.patientAcctId);
                
                // Record the new guid for the document type
                factory.getAccountService().addAccountDocument( 
                                this.patientAcctId, this.documentGuid, ccr.getLogicalType(), "Updated by " + accountSettings.getAccountId(), true, "Pending");
            }
            else{
                log.info("Not updating logical document type " + ccr.getLogicalType() + " for patient '" + this.patientAcctId + "'");
            }
        }
        catch (TransactionException e) {
            throw new CCRStoreException(e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new CCRStoreException(e);
        }
        catch (IOException e) {
            throw new CCRStoreException(e);
        }
        catch (PHRException e) {
            throw new CCRStoreException(e);
        }
        catch (ConfigurationException e) {
            throw new CCRStoreException(e);
        }
        catch (ServiceException e) {
            throw new CCRStoreException(e);
        }
    }
    
    /**
     * Writes an activity log entry for the patient of the stored CCR (if the CCR patient has a MedCommons Account).
     * 
     * @param type - type of activity 
     * @param description - human readable description of activity
     */
    public void writeActivity(ActivityEventType type, String description) throws ServiceException {
        this.writeActivity(type,description, null);
    }
    
    /**
     * Writes an activity log entry for the patient of the stored CCR (if the CCR patient has a MedCommons Account).
     * 
     * @param type - type of activity 
     * @param description - human readable description of activity
     */
    public void writeActivity(ActivityEventType type, String description, BillingCharge charge) throws ServiceException {
        if(!blank(this.patientAcctId)) {
            // Write to activity log
            try {
                ActivityEvent evt = new ActivityEvent(type, description, getPrincipal(), patientAcctId, ccr.getTrackingNumber(), this.savedPin);
                evt.setCharge(charge);
                factory.getActivityLogService().log(evt);
            }
            catch (IOException e) {
                throw new ServiceException("Unable to write activity log for patient " + patientAcctId, e);
            }            
        }
    }
    
    public LocalFileRepository getRepository() {
        if (localFileRepository == null)
            localFileRepository = (LocalFileRepository) RepositoryFactory.getLocalRepository();
        return localFileRepository;        
    }

    /**
     * Attempts to merge the contents of the CCR for this transaction 
     * with that of the patient's Current CCR.
     * <p>
     * The Current CCR is determined by querying the account service based
     * on the patient id stored in the CCR.
     * <p>
     * The merged CCR is saved as the Current CCR for the patient user.
     * 
     * @return - the merged document
     * @throws PHRException 
     */
    
    public CCRDocument merge() throws MergeException, PHRException {
        return mergeLogic.merge(this);
    }


    public String getDocumentGuid() {
        return documentGuid;
    }

    public void setDocumentGuid(String documentGuid) {
        this.documentGuid = documentGuid;
    }

    public String getIdp() {
        return idp;
    }


    public void setIdp(String idp) {
        this.idp = idp;
    }
    
    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void setAccountSettings(AccountSettings accountSettings) {
        this.accountSettings = accountSettings;
    }

    public CCRDocument getCcr() {
        return ccr;
    }

    public void setCcr(CCRDocument ccr) {
        this.ccr = ccr;
    }

    public String getPatientAcctId() {
        return patientAcctId;
    }

    public void setPatientAcctId(String patientAcctId) {
        this.patientAcctId = patientAcctId;
    }

    public String getPatientFamilyName() {
        return patientFamilyName;
    }

    public void setPatientFamilyName(String patientFamilyName) {
        this.patientFamilyName = patientFamilyName;
    }

    public String getPatientGivenName() {
        return patientGivenName;
    }

    public void setPatientGivenName(String patientGivenName) {
        this.patientGivenName = patientGivenName;
    }


    public AccountSpec getPrincipal() throws ServiceException {
        if(principal == null) {
            principal = this.factory.getDocumentService().queryPrincipal();
            if(principal == null)
                principal = new AccountSpec(this.accountSettings.getAccountId());
        }

        return principal;
    }



    public PIN.Hasher getPinHasher() {
        return pinHasher;
    }



    public void setPinHasher(PIN.Hasher pinHasher) {
        this.pinHasher = pinHasher;
    }



    public CCRMergeLogic getMergeLogic() {
        return mergeLogic;
    }



    public void setMergeLogic(CCRMergeLogic mergeLogic) {
        this.mergeLogic = mergeLogic;
    }



    public static Logger getLog() {
        return log;
    }

    public ServicesFactory getFactory() {
        return factory;
    }

    public LocalFileRepository getLocalFileRepository() {
        return localFileRepository;
    }

    public XPathCache getXpath() {
        return xpath;
    }

    public String getSavedPin() {
        return savedPin;
    }
    
    public AccountSettings getPatientAccountSettings() throws ServiceException {
        if(patientAccountSettings != null) 
            return patientAccountSettings;
        if(eq(accountSettings.getAccountId(), patientAcctId))
            return patientAccountSettings = accountSettings;
        return 
           patientAccountSettings = factory.getAccountService().queryAccountSettings(patientAcctId);
    }

    public void setPatientAccountSettings(AccountSettings patientAccountSettings) {
        this.patientAccountSettings = patientAccountSettings;
    }
}
