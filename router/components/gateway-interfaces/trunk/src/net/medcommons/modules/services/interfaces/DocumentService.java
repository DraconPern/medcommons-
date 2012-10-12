/*
 * $Id$
 * Created on 4/08/2005
 */
package net.medcommons.modules.services.interfaces;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;



/**
 * DocumentService manages MedCommons documents.
 *
 * @author ssadedin
 */
public interface DocumentService {


    /**
     * Adds a document and adds requested rights for a document.
     *
     * The identity passed as "storageId" will be registered as owner for
     * the document.  Additional users can be granted rights by passing the
     * given map which should contain mappings from MedCommons IDs to rights
     * to be granted to those ids for the document.
     * <p/>
     * Note1: a registered document must have locations stored in the
     * document_locations table in central.   Currently this is performed
     * separately to this call by the storage layer itself.  As the document must
     * be registered <b>prior</b> to any locations being stored it is important
     * to call this service before physically storing the document itself.
     * <p/>
     * Note2: in a POPS scenario we may wish to accept documents from
     * a user who does not have a MedCommons Id.  Current plan (7/24/06) is that
     * all such documents will be assigned medcommons ids, but this must be done
     * prior to this call.
     *
     * @param storageId - medcommons id to become principal owner of the document
     * @param guid - guid of document
     * @param additionalRights - an unlimited number of 2d arrays in the form [ id, right ]
     *                           specifying additional accounts to be granted rights to
     *                           the document.
     * @return - the document id
     */
    public Long registerDocument(String guid, String storageId, String []... additionalRights) throws ServiceException;

    /**
     * Queries for all documents from the repository for the given user.
     *
     * @param medcommonsId
     * @return
     */
    public DocumentReference [] queryUserDocuments(String medcommonsId) throws ServiceException;

    /**
     * Registers the given document as being stored at the caller's node.
     * <p>
     * The values of integrityStatus will be set to VALID (zero) and the integrity check time will be set
     * to the current server time.
     *
     * @param guid
     */
    public void addDocument(String storageId, String guid) throws ServiceException;
    
    /**
     * Registers the given document as being stored at the caller's node, including
     * a pending payment required to access the content.
     *
     * @see #addDocument(String,String)
     * @param billingEvent    Charge to be recorded as pending for this document
     */
    public void addDocument(String storageId, String guid, BillingEvent billingEvent) throws ServiceException;

    /**
     * Creates a DocumentLocation object for a document to be at a specified node with an encryption key.
     * The values of integrityStatus will be set to VALID (zero) and the integrity check time will be set
     * to the current server time.
     *
     * @param guid
     * @param nodeName
     * @param encryptionKey
     */
    public void addDocumentLocation(String storageId, String guid, String nodeName, String encryptionKey, int intStatus) throws ServiceException;


    /**
     * Deletes an existing DocumentLocation object for a given guid and nodename.
     *
     * <P>
     * This action should be performed by the storage layer when the item is deleted from disk.
     * <P>
     * @param guid
     * @param nodeName
     */
    public void deleteDocumentLocation(String storageId, String guid, String nodeName) throws ServiceException;

    /**
     * Returns document guid/decryption key pairs for the specified document at the specified node.
     * If no key exists for the specified account / document then a zero length array 
     * is returned.
     * <p>
     * If the content for the user is unencrypted then a special pre-defined key, "NONE" is returned.
     *
     * @param guid
     * @param nodeName
     * @throws ServiceException
     */
    public DocumentKey[] getDocumentDecryptionKey(String storageId, String guid, String nodeName) throws ServiceException;

    /**
     * Returns an array of DocumentLocation objects for the specified guid. If the nodeName is null then all locations are
     * returned; otherwise only the ones matching the specified nodeName are returned.
     *
     * @param guid
     * @param nodeName
     * @return
     */
    public DocumentLocation[] getDocumentLocation(String guid, String nodeName) throws ServiceException;

    /**
     * Attempts to resolve the given guid in the context of the given account and returns
     * reference information describing the location and status of the document.
     * <p/>
     * The requested account MUST have rights to the document requested for this call to
     * succeed.  If not the service MUST return an error code and no document information.
     *
     * @param accountId
     * @param guid
     * @return
     *
     * @throws InsufficientRightsException - if the requested account is declined access to the specified guid
     * @throws ServiceException - if an error occurs
     */
    public DocumentReference[] resolve(String accountId, String guid) throws ServiceException;

    /**
     * Adds an access right to all documents of a particular account by another account.
     *
     * <p><i>Note: the caller's auth token must have at least the rights that are
     * to be granted to the consent recipient.</i></p>
     * 
     * @param accessTo - account id of account documents to grant access To
     * @param accessBy - account id of account that should be granted access
     * @param rights - consent to grant to the entity gaining access.
     */
    public void grantAccountAccess(String accessTo,  String accessBy, String rights) throws ServiceException;


    /**
     * Add an access right to all documents of a particular account by a given external 
     * entity identified by the supplied ExternalShare
     * 
     * <p><i>Note: the caller's auth token must have at least the rights that are
     * to be granted to the consent recipient.</i></p>
     * 
     * @param patientId - account to grant access rights to
     * @param share - entity to gain access
     * @param rights - consent to grant to the entity gaining access.
     * @return ExternalShare - an object indicating the external share created,
     *                         it must have the authentication token set.
     * 
     * @throws ServiceException
     */
    public ExternalShare grantAccountAccess(String patientId, ExternalShare share, String rights) throws ServiceException;
    
    /**
     * Duplicates all rights available to fromAccount for accessing document specified by 'guid'
     * to account toAccount.  No rights are removed from fromAccount.
     * <p/>
     * Note: if fromAccount has global access rights to the storage id associated with 'guid' then
     * this right will also be duplicated, thus granting toAccount the same global access
     * to all documents stored under this storage id.
     * <p/>
     * Note2: as an optimization, if the toAccount and fromAccount are equal, rights are not
     * duplicated.  However it is better to avoid calling this function in the first place for that
     * situation.
     *
     * @param fromAccount - account currently having rights
     * @param toAccount   - account which should gain rights
     * @param toAccountType - type of account or null if MedCommons account type.  Use "openid" for OpenID.
     * @param storageAccount - account under which document is stored
     * @param guid        - document
     */
    public void inheritRights(String fromAccount, String toAccount, String toAccountType, String storageAccount, String guid) throws ServiceException;

    /**
     * Return the set of rights available for the current authorized user to perform operations
     * on the given storageId account.
     */
    public EnumSet<Rights> getAccountPermissions(String storageId) throws ServiceException;
    
    
    /**
     * Create an authentication token for the user based on the given credentials
     * 
     * @param id - identifier (eg. openid url)
     * @param secret - secret (eg. handle returned by openid provider) or null
     * @param type - must be "openid", the only supported type
     * @return
     * @throws ServiceException 
     */
    public String createAuthToken(String id, String secret, String type ) throws ServiceException;
    
    /**
     * Return the principal that is authorized for this session by the secure service
     */
    public AccountSpec queryPrincipal() throws ServiceException;

    /**
     * Register the given payment for the given document
     */
    public void registerPayment(String storageId, String mcGUID, BillingCharge charge) throws ServiceException;
    
    /**
     * Return true if payment for specified documents has been made.
     * @throws ServiceException 
     */
    public Map<String, Boolean> verifyPaymentStatus(String storageId, String...guids) throws ServiceException;

    /**
     * Add rights to access a new document to the specified tracking number.  Rights 
     * to access any previous document will still be maintained.
     * 
     * @param trackingNumber
     * @param pinHash
     * @param mcGUID
     */
    void addTrackingRights(String trackingNumber, String pinHash, List<String> guids) throws ServiceException;
    
    /**
     * Register an encryption key for specified user.
     * <p>
     * If the user already has a key registered then this call MUST fail and not
     * change the existing key.
     * 
     * @param storageId
     * @param key
     * @throws ServiceException 
     */
    void registerKey(String storageId, String decryptionKey, String encryptionKey) throws ServiceException;
    
    /**
     * Delete the encryption key for the specified user from the key store
     * 
     * @param storageId
     * @throws ServiceException 
     */
    void deleteKey(String storageId) throws ServiceException;
}
