/*
 * $Id$
 * Created on 8/08/2005
 */
package net.medcommons.modules.services.client.rest;

import static net.medcommons.modules.utils.Str.join;
import static net.medcommons.modules.utils.Str.nvl;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.services.interfaces.BillingCharge;
import net.medcommons.modules.services.interfaces.BillingEvent;
import net.medcommons.modules.services.interfaces.BillingEventType;
import net.medcommons.modules.services.interfaces.DocumentLocation;
import net.medcommons.modules.services.interfaces.DocumentReference;
import net.medcommons.modules.services.interfaces.DocumentService;
import net.medcommons.modules.services.interfaces.ExternalShare;
import net.medcommons.modules.services.interfaces.InsufficientRightsException;
import net.medcommons.modules.services.interfaces.NodeKeyProvider;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.TrackingReference;
import net.medcommons.rest.RESTException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Implements a simple REST (aka HTTP POST) client for 
 * the MedCommons Document Service
 * 
 * @author ssadedin
 */
public class DocumentServiceProxy implements DocumentService  {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DocumentServiceProxy.class);
    
    /**
     * Auth token describing identity of accessor
     */
    private String authToken;
    
    /**
     * Auth token describing full security context
     */
    private String contextAuth;
    
    /**
     * Node key
     */
    private String nodeKey;
    
    /**
     * Creates a DocumentServiceProxy
     * @param clientId
     * @throws ServiceException 
     */
    public DocumentServiceProxy(String clientId) throws ServiceException {
        this.authToken = clientId;
        try {
            NodeKeyProvider nodeKeyProvider = Configuration.getBean("nodeKeyProvider");
            if(nodeKeyProvider != null)
                this.nodeKey = nodeKeyProvider.getNodeKey();
        }
        catch(NoSuchBeanDefinitionException ex) {
            // ignore - can still make calls that do not require node key
        }
    }
    
    public DocumentServiceProxy(String authToken, String contextAuth) throws ServiceException {
        this(authToken);
        this.contextAuth = contextAuth;
    }

    /**
     * Registering a document registers the existence of the document.
     */
    public Long registerDocument(String guid, String storageId, String []... additionalRights)  throws ServiceException {
        
        try {
            String []  params = new String[additionalRights.length*2 + 6];
            int i=0;
            params[i++]="mcid";
            params[i++]=storageId;
            params[i++]="guid";
            params[i++]=guid;
            params[i++]="node_key";
            params[i++]=nodeKey;
            
            for (String [] right : additionalRights) {
                params[i++]="right[]"; // Annoying PHP does not support plain multivalued params, arg.
                params[i++]=right[0]+"="+right[1];
            }            
            
            RESTUtil.call(authToken, "DocumentService.registerDocument", params);
            
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to register document for mcId=" + storageId + " guid=" + guid, e);
        }
        
        return null;
    }

    public DocumentReference[] queryUserDocuments(String medcommonsId) throws ServiceException {
        try {
            Document dom = RESTUtil.call(authToken, "DocumentService.queryUserDocuments", "mcid", medcommonsId);
            
          ArrayList<DocumentReference> result = new ArrayList<DocumentReference>();
          List entries = dom.getRootElement().getChildren("entry");
          for (Iterator iter = entries.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();            
            //TODO:  fill in or remove other attributes than  guid
            DocumentReference doc = new DocumentReference(
                            element.getChildTextTrim("storageId"),new Date(), element.getChildText("guid"), "", "");
            result.add(doc);
          }
          
          return result.toArray(new DocumentReference[result.size()]);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to query documents for mcId=" + medcommonsId,e);
        }
    }

    public void addDocument(String storageId, String guid) throws ServiceException {
        try {
            RESTUtil.call(authToken, "DocumentService.addDocument", "node_key", nodeKey, "guid", guid, "storageId", storageId);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to create document for guid=" + guid , e);
        }
   }
    
    public void addDocument(String storageId, String guid, BillingEvent billingEvent) throws ServiceException {
        if(billingEvent == null) { 
            this.addDocument(storageId,guid);
            return;
        }
        
        try {
            RESTUtil.call(authToken, "DocumentService.addDocument", 
                            "node_key", nodeKey, 
                            "guid", guid, 
                            "storageId", storageId, 
                            "chargeType", billingEvent.getType().name(), 
                            "chargeQuantity", String.valueOf(billingEvent.getQuantity()));
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to create document for guid=" + guid , e);
        }
    }
    
    public void addDocumentLocation(String storageId, String guid, String nodeName, String encryptionKey, int intStatus) throws ServiceException {
    	 try {
             RESTUtil.call(authToken, "DocumentService.addDocumentLocation", "node_key", nodeKey, "guid", guid, "node", nodeName, "ekey", encryptionKey,
            		 "intstatus", Integer.toString(intStatus), "storageId", storageId);
         }
         catch (RESTException e) {
        	 Document doc = e.getDocument();
        	 String status = RESTUtil.getSummmaryStatus(doc);
        	 if (status != null){
        		 if (status.indexOf("Duplicate") != -1){
        			 // Then its a duplicate. Assume that the local file is valid; replace reference on central with 
        			 // new value.
        			 log.info("Duplicate document_location:" + status);
        			 log.info("Replacing with new values");
        			 try{
        				 RESTUtil.call(authToken, "DocumentService.updateDocumentLocation", "guid", guid, "node", nodeName, "ekey", encryptionKey,
        						 "intstatus", Integer.toString(intStatus), "storageId",storageId);
        				 return; 
        			 }
        			 catch(Exception e2){
        				 throw new ServiceException("Unable to replace existing document location information for guid " + guid + " node=" + nodeName, e2);
        			 }
        		 }
        	 }
             throw new ServiceException("Unable to create document location for guid=" + guid + " node=" + nodeName, e);
         }
       
    }

    public void deleteDocumentLocation(String storageId, String guid, String nodeName) throws ServiceException{
        try{
            RESTUtil.call(authToken, "DocumentService.deleteDocumentLocation", "node_key", nodeKey, "guid", guid, "node", nodeName, "storageId", storageId);
        }
        catch (RESTException e) {
            throw new ServiceException("Failure calling deleteDocumentLocation for guid=" + guid + " node=" + nodeName, e);
        }
    }
    
    public DocumentLocation[] getDocumentLocation(String guid, String nodeName) throws ServiceException {
        throw new ServiceException(new OperationNotSupportedException());
    }
    
    /**
     * This returns document locations but the node id fields aren't 
     */
    public DocumentKey[] getDocumentDecryptionKey(String storageId, String guid, String nodeName) throws ServiceException {
        try {
          Document dom = RESTUtil.call(authToken, "DocumentService.getDocumentDecryptionKey", "nodeKey", nodeKey, "guid", guid, "node", nodeName,"storageId",storageId);
          Element outputs = dom.getRootElement().getChild("outputs");
          if(outputs == null) {
        	  log.info("No elements in result for decryption key for guid " + guid + " at node " + nodeName);
        	  return new DocumentKey[0];
          } 
          
          ArrayList<DocumentKey> result = new ArrayList<DocumentKey>();
          List entries = outputs.getChildren("entry");
          for(Iterator iter = entries.iterator(); iter.hasNext();) {
              Element element = (Element) iter.next();  
              DocumentKey dk = new DocumentKey(element.getChildText("guid"), element.getChildText("encrypted_key"));
              result.add(dk);
          }
	          
	      return result.toArray(new DocumentKey[result.size()]);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to get document decryption keys for guid=" + guid + " at node " + nodeName,e);
        }
    }

    public DocumentReference[] resolve(String accountId, String guid) throws ServiceException {
        try{
            Document doc = RESTUtil.call(authToken, "DocumentService.resolveDocument", "guid", guid, "accountId", accountId, "ctx", this.contextAuth);
            String status = doc.getRootElement().getChild("outputs").getChildText("status");
            if(!"ok".equals(status)) {
                throw new ServiceException("Document Service returned status " + status);
            }
            
            String result = doc.getRootElement().getChild("outputs").getChildText("result");
            if("unauthorized".equals(result)) {
                throw new InsufficientRightsException("Access by account id " + accountId + " to guid " + guid + " declined");
            }
            else if ("unknown".equals(result)){
                log.warn("result of resolveDocument is unknown; errors downstream may result");
            }
            
            TrackingReference trackingReference = null;
            Element tr = doc.getRootElement().getChild("outputs").getChild("trackingReference");
            if(tr != null) {
                trackingReference = new TrackingReference();
                trackingReference.setTrackingNumber(tr.getChildTextTrim("trackingNumber"));
                trackingReference.setPin(tr.getChildTextTrim("pin"));
            }
            
            ArrayList<DocumentReference> docRefs = new ArrayList<DocumentReference>();
            for(Iterator<Element> iter = doc.getDescendants(new ElementFilter("docRef")); iter.hasNext();) {
                Element element = (Element) iter.next();
                DocumentReference docRef = 
                    new DocumentReference(element.getChildTextTrim("storageId"),
                                          new Date() /* TODO */,
                                          guid, 
                                          element.getChildTextTrim("location"),
                                          element.getChildTextTrim("location_key")
                                          ); 
                docRef.setTrackingReference(trackingReference);
                if(trackingReference != null) {
                    trackingReference.setDocument(docRef);
                }
                
                Element charge = element.getChild("outstandingCharge");
                if(charge != null) {
                    BillingEvent billingEvent = new BillingEvent(BillingEventType.valueOf(charge.getChildText("chargeType")));
                    billingEvent.setQuantity( Integer.parseInt(charge.getChildText("chargeQuantity")));
                    docRef.setOutstandingCharge(billingEvent);
                }
                
                docRefs.add(docRef);
            }
            return docRefs.toArray(new DocumentReference[docRefs.size()]);
        }
        catch (RESTException e) {
            throw new ServiceException("Failed to access guid=" + guid + " for accountId=" + accountId, e);
        }
    }


    public void grantAccountAccess(String accessTo, String accessBy, String rights) throws ServiceException {
     try {
         RESTUtil.callJSON(authToken, "DocumentService.grantAccountAccess", "accessTo", accessTo, "accessBy", accessBy, "rights", rights);
     }
     catch (RESTException e) {
         throw new ServiceException("Unable to grant account access to account " + accessTo + " by account " + accessBy, e);
     }        
    } 
    
    public ExternalShare grantAccountAccess(String accessTo, ExternalShare share, String rights) throws ServiceException {
        try {
            JSONObject obj = RESTUtil.callJSON(authToken, "DocumentService.grantAccountAccess", 
                            "accessTo", accessTo, 
                            "es_identity", share.getIdentity(),
                            "es_identity_type",share.getIdentityType(),
                            "es_auth_token",share.getAuth(),
                            "es_first_name", nvl(share.getFirstName(),""),
                            "es_last_name", nvl(share.getLastName(), ""),
                            "es_tracking_number",nvl(share.getTrackingNumber(),""),
                            "rights", rights);
            
            JSONObject result = obj.getJSONObject("result");
            
            ExternalShare s = new ExternalShare();
            s.setId(result.getLong("es_id"));
            s.setAuth(result.getString("authentication_token"));
            s.setSecret(result.getString("authentication_secret"));
            s.setIdentity(share.getIdentity());
            s.setIdentityType(s.getIdentityType());
            s.setFirstName(share.getFirstName()); 
            s.setLastName(s.getLastName());
            return s;
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to grant account access to account " + accessTo + " by account " + share.getIdentity() + "[" + share.getIdentityType() + "]", e);
        }        
    }
    

    public void inheritRights(String fromAccount, String toAccount, String toAccountType, String storageAccount, String guid) throws ServiceException {
        try {
            RESTUtil.call(authToken, "DocumentService.inheritRights", "fromAccount", fromAccount, "toAccount",
                            toAccount, "toAccountType", toAccountType, "guid", guid, "storageAccount", storageAccount);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to inherit rights from account " + fromAccount + " to account " + toAccount + " for document "  + guid, e);
        }        
    }


    public EnumSet<Rights> getAccountPermissions(String storageId) throws ServiceException {
        try {
            Document doc = RESTUtil.call(authToken, "DocumentService.getPermissions", "toAccount", storageId, "ctx", contextAuth);
            String permissions = doc.getRootElement().getChild("outputs").getChildText("rights");
            log.info("Received " + permissions + " to storage account " + storageId + " with token " + authToken);
            return Rights.toSet(nvl(permissions,"")); 
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to get permissions for account " + storageId + " using auth token " + authToken, e);
        }
   }


    public String createAuthToken(String id, String secret, String type) throws ServiceException {
        try {
            Document doc = RESTUtil.call(authToken, "DocumentService.authorize", "id", id, "secret",secret, "type", type);
            String token = doc.getRootElement().getChild("outputs").getChildText("auth");
            log.info("Received auth token " + token + " to access account " + id);
            return token;
        }
        catch (RESTException e) { 
            throw new ServiceException("Unable to create auth token to access account " + id, e);
        }
    }

    public AccountSpec queryPrincipal() throws ServiceException {
        try {
            JSONObject obj = RESTUtil.callJSON(authToken, "DocumentService.queryAuthenticatedAccount");
            if(obj.isNull("result"))
                return null;
            
            JSONObject ai = obj.getJSONObject("result");
            if(ai.isNull("esId")) { // Plain medcommons account
                return new AccountSpec(ai.getString("accId"), "MedCommons");
            }
            else { // Externally verified account - more work
                AccountSpec result = null;
                if(ai.isNull("esFirstName"))
                    result = new AccountSpec(ai.getString("esIdentity"), ai.getString("esIdentityType"));
                else
                    result = new AccountSpec(ai.getString("esIdentity"), ai.getString("esIdentityType"), ai.getString("esFirstName"), ai.getString("esLastName"));
                
                if(!ai.isNull("accId")) 
                    result.setMcId(ai.getString("accId"));
                
                return result;
            }
            
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to query principal for auth token " + authToken,e);
        }
    }

    public void registerPayment(String storageId, String mcGUID, BillingCharge charge) throws ServiceException {
        try {
            ArrayList<String> params = new ArrayList<String>();
            
            params.add("storageId");
            params.add(storageId);
            params.add("guid");
            params.add(mcGUID);
            params.add("transactionId");
            params.add(charge.getTransactionId());
            for(BillingEventType t : charge.getCounters().keySet()) {
                params.add(t.name());
                params.add(charge.getCounters().get(t).toString());
            }
            
            RESTUtil.callJSON(authToken, "DocumentService.registerPayment", params.toArray(new String[params.size()]));
        }
        catch (RESTException e) {
            throw new ServiceException("Failed to register payment " + charge + " for storage id = " + storageId + " guid = " + mcGUID,e);
        }
    }

    public Map<String, Boolean> verifyPaymentStatus(String storageId, String... guids) throws ServiceException {
        try {
            JSONObject obj = RESTUtil.callJSON(authToken, "DocumentService.verifyPaymentStatus", "storageId", storageId, "guids", Str.join(guids, ","));
            JSONArray statuses = obj.getJSONArray("result");
            
            Map<String, Boolean> result = new HashMap<String, Boolean>();
            for(int i=0; i<statuses.length(); ++i) {
                result.put(statuses.getJSONObject(i).getString("guid"), "PAID".equals(statuses.getJSONObject(i).getString("status")));
            }
            return result;
        }
        catch (RESTException e) {
            throw new ServiceException("Failed to verify payment status for storage id = " + storageId + " guids= " + guids,e);
        }
    }

    @Override
    public void addTrackingRights(String trackingNumber, String pinHash, List<String> guids) throws ServiceException {
        try {
            RESTUtil.callJSON(authToken, "DocumentService.addTrackingRights", 
                    "trackingNumber", trackingNumber, 
                    "hpin", pinHash,
                    "guids", join(guids, ","));
        }
        catch (RESTException e) {
            throw new ServiceException("Failed to add tracking rights to tracking number " + trackingNumber + " for guids = " + join(guids,","),e);
        }
    }

    @Override
    public void registerKey(String storageId, String decryptionKey, String encryptionKey) throws ServiceException {
       try {
            RESTUtil.callJSON(authToken, "DocumentService.registerKey", 
                    "storageId", storageId, 
                    "key", decryptionKey,
                    "enc", encryptionKey);
        }
        catch (RESTException e) {
            throw new ServiceException("Failed to register encryption key for account " + storageId,e);
        } 
        
    }

    @Override
    public void deleteKey(String storageId) throws ServiceException {
       try {
            RESTUtil.callJSON(authToken, "DocumentService.deleteKey", "storageId", storageId);
        }
        catch (RESTException e) {
            throw new ServiceException("Failed to delete encryption keys for account " + storageId,e);
        } 
     }
}
