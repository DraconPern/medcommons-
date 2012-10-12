/*
 * $Id$
 * Created on 13/04/2007
 */
package net.medcommons.router.services.ccr;

import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

/**
 * Allows aspects of the storage model to be customized.
 * 
 * @author ssadedin
 */
public interface StorageModel {
    
    /**
     * Replaces the current CCR for the given patient
     * 
     * @return - the guid of new current ccr of patient
     * @throws ServiceException 
     */
    String replaceCurrentCCR(String patientId, CCRDocument ccr) throws PHRException, ServiceException;
    
    String saveCCRMerge(CCRDocument mergeFrom, CCRDocument mergeTo, AccountDocumentType type) throws PHRException, CCRStoreException;
    
    /**
     * Attempt to resolve the requested document by name
     * @param accid 
     * @throws PHRException 
     */
    CCRDocument resolveByName(String name, String accid) throws PHRException;
}
