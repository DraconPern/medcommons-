/*
 * $Id$
 * Created on 8/08/2005
 */
package net.medcommons.modules.services.client.rest;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.DocumentReference;
import net.medcommons.modules.services.interfaces.DocumentRegistration;
import net.medcommons.modules.services.interfaces.NodeKeyProvider;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.TrackingAccessConstraint;
import net.medcommons.modules.services.interfaces.TrackingReference;
import net.medcommons.modules.services.interfaces.TrackingService;
import net.medcommons.rest.ParamString;
import net.medcommons.rest.RESTException;
import net.medcommons.rest.RESTUtil;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class TrackingServiceProxy implements TrackingService {
    
    /**
     * Client for which this proxy is currently being used
     */
    private String authToken;

    
    /**
     * Node key
     */
    private String nodeKey;
  
    /**
     * @param id
     * @throws ServiceException 
     */
    public TrackingServiceProxy(String id) throws ServiceException {
        super();
        authToken = id;
        try {
            NodeKeyProvider nodeKeyProvider = Configuration.getBean("nodeKeyProvider");
            if(nodeKeyProvider != null)
                this.nodeKey = nodeKeyProvider.getNodeKey();
        }
        catch(NoSuchBeanDefinitionException ex) {
            // ignore - can still make calls that do not require node key
        }
    }
 
    public DocumentRegistration registerTrackDocument(
            String storageId, String guid, String pinHash, String pin, Long expirySeconds, TrackingAccessConstraint constraint, 
            String []... additionalRights) 
    throws ServiceException 
    {
            
        if(constraint == null) 
            constraint = TrackingAccessConstraint.UNLIMITED;
        
    	Document doc = null;
        try {
            ParamString params = new ParamString();
            params.add("mcid", storageId)
		          .add("guid", guid)
		          .add("pinHash", pinHash)
		          .add("pin", pin)
		          .add("expirySeconds",  (expirySeconds == null ? "" : expirySeconds.toString()))
		          .add( "node_key", this.nodeKey)
		          .add("constraint", constraint.name());
		
            if(additionalRights!=null) {
                int i=10;
                for (String [] right : additionalRights) {
                    if(right == null)
                        continue;
                    params.add("right[]", // Annoying PHP does not support plain multivalued params, arg.
                               right[0]+"="+right[1]);
                }            
            }
            
            assert storageId != null : "Storage id must be provided";
            
            doc = RESTUtil.call(authToken, "TrackingService.registerTrackDocument",params.flatten());
            String trackingNumber =  doc.getRootElement().getChild("outputs").getChildTextTrim("trackingNumber");
            storageId =  doc.getRootElement().getChild("outputs").getChildTextTrim("mcid");
            String summaryStatus = doc.getRootElement().getChildTextTrim("summary_status");
            return new DocumentRegistration(summaryStatus, trackingNumber, storageId);            
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to register/track document for mcId=" + storageId + " guid=" + guid + ": " + e.getMessage(), e);
        }
    }
    
    public TrackingReference validate(String trackingNumber, String pinHash, String externalShareId) throws ServiceException {
        Document doc = null;
        try {
            doc = RESTUtil.call(authToken, "TrackingService.validateDocument", 
                            "node_key", this.nodeKey, 
                            "trackingNumber", trackingNumber, 
                            "pinHash",  pinHash, 
                            "esId", externalShareId);
            
            TrackingReference result = new TrackingReference();
            result.setTrackingNumber(trackingNumber);
            
            Element outputs = doc.getRootElement().getChild("outputs");
            result.setConstraint(TrackingAccessConstraint.valueOf(outputs.getChildTextTrim("access_constraint")));
            result.setMcId(outputs.getChildTextTrim("mcid"));
            result.setPin(outputs.getChildTextTrim("pin"));
            DocumentReference docRef = new DocumentReference();
            docRef.setGuid(outputs.getChildTextTrim("guid"));
            docRef.setStorageAccountId(outputs.getChildTextTrim("storageId"));
            docRef.setLocation(outputs.getChildTextTrim("node"));
            docRef.setLocationKey(outputs.getChildTextTrim("node_key"));
            result.setDocument(docRef);
            result.setAuth(outputs.getChildText("auth"));
            return result;
        }
        catch (RESTException e) {
            if("not found".equals(e.getStatus())) {
                return null;
            }
            else
            if("invalid email address".equals(e.getStatus())) { 
                String email = "unknown";
                if(e.getDocument() != null) {
                    email = e.getDocument().getRootElement().getChildText("email");
                }
                throw new IncorrectEmailAddressException(trackingNumber, email);
            }
            else
                throw new ServiceException("Unable to validate tracking number " + trackingNumber + " pinHash = " + pinHash+ ": " + e.getMessage(), e);
        }
    }

    public void revokeTrackingNumber(String trackingNumber, String pinHash) throws ServiceException {        
        try {
            RESTUtil.call(authToken, "TrackingService.revokeTrackingNumber", "tn", trackingNumber, "hashedPin", pinHash);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to revoke tracking number " + trackingNumber + ": " + e.getMessage(), e);
        }
    }

    public String allocateTrackingNumber() throws ServiceException {
        try {
            Document doc = RESTUtil.call(authToken, "TrackingService.allocateTrackingNumber");
            return doc.getRootElement().getChild("outputs").getChildText("trackingNumber");
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to allocate new tracking number: " + e.getMessage(), e);
        }
    }

    public void reviseTrackedDocument(String trackingNumber, String pinHash, String guid) throws ServiceException {
        try {
            Document doc = RESTUtil.call(authToken, "TrackingService.reviseTrackedDocument",
                            "trackingNumber", trackingNumber,
                            "pinHash", pinHash,
                            "guid",guid);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to revise tracking number guid " + guid + ": " + e.getMessage(), e);
        }
    }

    public void updatePIN(String trackingNumber, String oldPinHash, String newPinHash) throws ServiceException {
        
        try {
            Document doc = RESTUtil.call(authToken, "TrackingService.updatePIN",
                            "trackingNumber", trackingNumber,
                            "oldPINHash", oldPinHash,
                            "newPINHash",newPinHash);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to update PIN for tracking number " + trackingNumber + ": " + e.getMessage(), e);
        } 
    }

    public String queryGuid(String trackingNumber) throws ServiceException {
        try {
            Document doc = RESTUtil.call(authToken, "TrackingService.queryGuid","trackingNumber", trackingNumber);
            return doc.getRootElement().getChild("outputs").getChildText("guid");
        } 
        catch (RESTException e) {
            throw new ServiceException("Unable to query guid for tracking number " + trackingNumber + ": " + e.getMessage(), e);
        }
    }
}
