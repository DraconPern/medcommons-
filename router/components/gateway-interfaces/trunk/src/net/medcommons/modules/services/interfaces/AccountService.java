package net.medcommons.modules.services.interfaces;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AccountService manages information associated with an account. It does 
 * not itself manage accounts (or at least not yet).
 * 
 * @author sean
 *
 */
public interface AccountService {
	/**
	 * Adds a CCR Log entry for a particular guid
	 * <p>
	 * The CCR log entries are the primary source for what is known as "tabs" in the user
	 * interface of the viewer and elsewhere.   In general, to add a "tab", simply write a new 
	 * entry to the CCR log.
	 * <p>
	 * Note that not all entries in the CCR  are displayed to the user, as they are filtered
	 * on a field called 'merge_status' (misleadingly named).   The merge_status is set to null
	 * by this call, which is the default state and causes the tab to be displayed.   However
	 * several other factors can cause the merge_status to be set to different states, some of
	 * which are filtered from view.   (two hidden states at this time are 'Hidden' and 'Replaced').
	 * <p>
	 * So-called "logical" documents actually consist of many entries in the CCR log. Each time
	 * a logical document is updated the old entries for that logical document are set to 'Replaced'
	 * in the CCR Log, causing only the most recent to show as a tab in the display of the user.
	 * <p>For this reason, if you want to "fix" a logical CCR to appear as a tab, you must first
	 * save a copy of it with a different GUID, because the old guid will be marked as 'Replaced'
	 * 
	 * @param guid - guid of document to be replaced
	 * @param idp - the identity provider
	 * @param from
	 * @param to
	 * @param accid - the medcommons account 
	 * @param date
	 * @param subject
	 * @param status
	 * @deprecated   THE CCR LOG IS NO LONGER IN USE. DO NOT WRITE ENTRIES TO IT
	 * @throws ServiceException
	 */
	public void addCCRLogEntry(String guid, String idp, String from, String to, String accid, Date date, String subject, String status, String trackingNumber) throws ServiceException;
    
    /**
     * Sets the given guid to be the chosen 'red' (emergency) CCR for the given account.
     * <p/>
     * TODO: currently this call requires no credentials except knowledge of a guid
     * and account id, hence can be trivially hacked to set any one's Red CCR to any guid.
     * 
     * @param accId
     * @param guid
     * @throws ServiceException 
     */
    public void setEmergencyCCR(String accId, String guid, String einfo) throws ServiceException;
    
    
    /**
     * Returns information about a given cover sheet
     * 
     * @param coverId
     * @return
     */
    public CoverInfo queryCoverInfo(Long coverId)  throws ServiceException;
    
    /**
     * Adds a given document to an account's special documents
     * @param guid
     * @param documentType
     * @param comment
     * @param unique - set to true if old entries of the same type should be deleted
     * @param notificationStatus - optional, set to indicate if account holder needs to be/has been notified.
     */
    public void addAccountDocument(String accountId, String guid, AccountDocumentType documentType, String comment, boolean unique, String notificationStatus)  throws ServiceException;
    
    
    /**
     * Removes all instances of the given document type from the given account.
     * 
     * @param accountId
     * @param documentType
     */
    public void removeAccountDocument(String accountId, AccountDocumentType documentType) throws ServiceException;

    /**
     * The account to query for settings.
     * 
     * @param accountId
     * @return
     */
    public AccountSettings queryAccountSettings(String accountId) throws ServiceException;
    
    /**
     * Returns JSON representing CCRs that appear in the CCR Log for the given account.
     * 
     * @param accountId
     * @throws ServiceException
     */
    public String queryCCRLog(String accountId) throws ServiceException;
    
    /**
     * Registers the given billing event with the user's account. 
     * 
     * @param accountId - account to bill
     * @param type - the type of event that occurred
     * @param reference - a reference number (eg. tracking number) to track the event
     * @param count - an optional count indicating the size or amount of the event.  
     *                Interpretation depends on the type.
     * @param description - human readable description
     * @throws ServiceException 
     */
    public void billingEvent(String accountId, BillingEventType type, int count, String reference, String description) throws ServiceException;
    
    /**
     * Returns a list of rights indicating ability of other accounts to read or update documents
     * in the given account.
     */
    public ArrayList<AccountShare> querySharingRights(String accountId) throws ServiceException;
    
    /**
     * Updates the given accounts with requested rights for the given storage id.
     * <p>
     * The authentication token associated with the caller *must* have 
     * consent / authorization to update consents for the #storageId account.
     * 
     * @param update    accounts to update
     */
    public void updateSharingRights(String storageId, List<AccountHolderRight> update) throws ServiceException;
       
    /**
     * Create or update a workflow item with the given key
     * 
     * @param key - optional unique key for this (workflow item, source account, patient account) - eg. guid of document
     * @param srcAccountId - account id of account owning this workflow item (eg. doctor group account id)
     * @param patientAccountId - account id of patient or subject of workflow item
     * @param workflowType - type of workflow:  'Download Status'
     * @param status -  current status:  ('Available','Downloaded')
     * @throws ServiceException
     */
    public void updateWorkflow(String key, String srcAccountId, String patientAccountId, String workflowType, String status) throws ServiceException;
    
    /**
     * Queries for information about the given appliction registered with the appliance
     * on which this service is running.
     */
    public Application queryApplicationInfo(String applicationToken) throws ServiceException;
    
    
    /**
     * Shares the given voucher with the specified account so that it appears on the
     * voucher patient list for the account.
     */
    public void shareVoucher(String accid, Voucher voucher) throws ServiceException;
    
    /**
     * Create an access code to authenticate the specified phone user.
     * 
     * @param phoneNumber
     * @param carrier 
     * @throws ServiceException
     * @return access code created
     */
    public String createPhoneAccessCode(String phoneNumber, String carrier, String accessTo) throws ServiceException;
    
    /**
     * Returns a list of features and whether they are enabled or disabled 
     * on the current appliance.
     * 
     * @throws ServiceException
     */
    public List<Feature> queryFeatures() throws ServiceException;
}
