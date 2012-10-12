/*
 * $Id$
 * Created on 4/08/2005
 */
package net.medcommons.modules.services.interfaces;


/**
 * TrackingService is responsible for verifying user credentials, tracking
 * numbers and PINs and mapping these to medcommons ids.
 * 
 * @author ssadedin
 */
public interface TrackingService {
    
    /**
     * Allocates a new unused tracking number and returns it for use.  The new 
     * tracking number is not associated with any document or PIN. These fields must 
     * be set null.
     * <p>
     * <i>Note: the it is expected that the tracking number will later have a document
     * associated with it via the DocumentService.</i>
     * 
     * @return - the new tracking number
     * @throws ServiceException
     */
    public String allocateTrackingNumber() throws ServiceException;
    
    /**
     * Registers the given document and creates a tracking number for it,
     * accessible via the given pinHash according to the specified rights.
     * <p>
     * Note1: a post condition of this call is that calling validate() 
     * and passing the returned tracking number and pinHash will
     * return a <code>TrackingReference</code> for the document.
     * <p>
     * Note2: in current implementation the MedCommons Id may be passed
     * as all (16) zeros.  This causes a MedCommons Id to be created
     * as equal to the tracking number itself with four zeros appended.
     * 
     * @param storageId        - id under which document is to be stored
     * @param guid
     * @param pinHash
     * @param pin              - unencrypted version of the PIN. Will be stored and 
     *                            returned when track# / PIN is resolved.
     * @param expirySeconds    - optional, number of seconds after which the PIN provided should
     *                           expire. 
     * @param accessConstraint TODO
     * @param additionalRights - an unlimited number of 2d arrays in the form [ id, right ]
     *                           specifying additional accounts to be granted rights to 
     *                           the document.
     * @return
     * @throws ServiceException
     */
    public DocumentRegistration registerTrackDocument(String storageId, String guid, String pinHash, String pin, Long expirySeconds, TrackingAccessConstraint accessConstraint, String []... additionalRights) throws ServiceException; 
    
    /**
     * Validates the given trackingNumber and pin combination. If valid, returns
     * the MedCommons Id for that combination.  If not valid, returns an
     * error.
     * 
     * @param trackingNumber
     * @param pinHash
     * @param externalShareId TODO
     * @return TrackingReference containing information regarding the specified tracking number
     */
    public TrackingReference validate(String trackingNumber, String pinHash, String externalShareId) throws ServiceException;
    
    /**
     * Requests revocation of the given tracking number.
     * <p>
     * After a successful revocation future requests to resolve the 
     * same tracking number MUST refused.
     * <p>
     * Revoked tracking numbers will not be re-used by the system.
     * 
     * @param trackingNumber - the tracking number to be rescinded.  
     * @param pinHash TODO
     */
    public void revokeTrackingNumber(String trackingNumber, String pinHash) throws ServiceException;
    
    /**
     * Updates a specified tracking number with a new guid.
     * 
     * @param trackingNumber - the tracking number to update
     * @param pinHash - the hashed PIN for the specified tracking number
     * @param guid - the new guid to be returned for the tracking number
     * @throws ServiceException 
     */
    public void reviseTrackedDocument(String trackingNumber, String pinHash, String guid) throws ServiceException;
    
    
    /**
     * Updates the specified with a new PIN
     * 
     * @param trackingNumber - tracking number to update
     * @param oldPinHash     - the old PIN  
     * @param newPinHash     - the new PIN
     */
    public void updatePIN(String trackingNumber, String oldPinHash, String newPinHash) throws ServiceException;
    
    /**
     * Resolve GUID from this tracking number
     * 
     * @param trackingNumber
     * @throws ServiceException
     */
    public String queryGuid(String trackingNumber) throws ServiceException;
}
