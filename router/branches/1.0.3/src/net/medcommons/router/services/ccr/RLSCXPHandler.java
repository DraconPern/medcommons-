package net.medcommons.router.services.ccr;

import java.util.ArrayList;
import java.util.Date;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.cxp.server.RLSHandler;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
/**
 * This is the handler for documents entering the system
 * via CXP. The design intent (not yet realized) is to factor out the 
 * transactional elements of document import from the protocol handling.
 * 
 * @author sean
 *
 */
public class RLSCXPHandler implements RLSHandler {

    private final static String DEFAULT_PIN = "12345";
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(RLSCXPHandler.class);

	/**
	 * The factory that will be used to access services
	 */
	//private ServicesFactory serviceFactory = null;
	
	/**
	 * Get the account settings for this storage id.
	 */
	private AccountSettings getAccountSettings(ServicesFactory serviceFactory, String accountId) throws ServiceException{
		AccountSettings settings = serviceFactory.getAccountService().queryAccountSettings(accountId);
         return(settings);
	}


	private ArrayList<String[]> generateDefaultRights(String storageId){
		ArrayList<String[]> creationRights = new ArrayList<String[]>();
		creationRights.add(new String[] { storageId, Rights.ALL});
		return(creationRights);
	} 
	
	public void newDocumentEvent(ServicesFactory serviceFactory, String storageId, String guid, String trackingNumber, CXPConstants.MergeCCRValues mergeIncomingCCR, String pin, String notificationSubject, String fromEmail) throws RepositoryException,ServiceException,Exception{
	    
		log.info("newdocumentevent for " + storageId + " " + guid);
		if (storageId.equals("-1")){
			throw new RepositoryException("storageId of -1 not valid in this context");
		}
		
		// Load document from the data store as a CCRDocument
		CCRDocument ccr = (CCRDocument) RepositoryFactory.getLocalRepository().queryDocument(storageId, guid);
		
		AccountSettings accountSettings = getAccountSettings(serviceFactory, storageId);
		
		if(mergeIncomingCCR!=CXPConstants.MergeCCRValues.NONE) {
		   ccr.setLogicalType(AccountDocumentType.CURRENTCCR);
		}
		else
        if(!ServiceConstants.PUBLIC_MEDCOMMONS_ID.equals(storageId)) { // Don't store profiles for POPS users
		    ProfileService profiles = Configuration.getBean("profilesService");

		    PHRProfile profile = new PHRProfile(guid);
		    Date createDate = ccr.getCreateTime();
		    if (createDate == null){
		        createDate = new Date(); 
		    }
		    profile.setDate(createDate); // The tab time creation time is set to be the CCR creation time.
	        profiles.createProfile(storageId,profile);
	        return;
		}
		    
		ccr.setTrackingNumber(trackingNumber);
	
		// TODO: Should be derived from the CCR.
		// Hmm. Shouldn't it be derived from the transaction context (in CXP)?
		if (fromEmail == null){
		    fromEmail = "UNKNOWN"; 
		}
		
		ArrayList<String[]> defaultRights = generateDefaultRights(storageId);
		
        //ssadedin: set the storage id so that CCR is stored under correct id
        // note: no longer set at all in StoreTransaction
		ccr.setStorageId(storageId);
		StoreTransaction storeTx = new StoreTransaction(serviceFactory,  accountSettings,  ccr);
		ccr.setStorageMode(StorageMode.FIXED);
		
	
		 if (accountSettings != null) { // Does this assume that the account exists?
	        storeTx.setIdp("idp"); //??
	        storeTx.notifyRegistry();
	        
	       if (pin == null){
	           pin = DEFAULT_PIN;
	       }
	       storeTx.registerDocument(pin, defaultRights.get(0));
	       storeTx.storeDocument();
	   
    	   log.info("Merging incoming CCR for " + storageId + ", " + guid);
    	   CCRDocument merged = storeTx.merge();
    	   if (merged != null){
    		   // It's null if there is no patient id.
    		   accountSettings.setCurrentCcrGuid(merged.getGuid());
    	   }
		 }
		 else{
		     log.error("CCR arrived for storageId " + storageId + " which has no accountSettings");
		 }
	}
}

