/*
 * $Id$
 * Created on 13/04/2007
 */
package net.medcommons.router.services.ccr;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.DocumentRegistration;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;


/**
 * Implements the fixed content storage model.  Under this model, account documents are stored
 * in a database that is opened and closed, are identified by names rather than guids and
 * are updated "in place" on the gateway rather than stored as fixed content with each updated. 
 */
public class FixedContentStorageModel implements StorageModel {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(FixedContentStorageModel.class);
    
    private ServicesFactory factory = Configuration.getBean("systemServicesFactory");
    
    /**
     * Repository that will be used to store data.
     */
    private LocalFileRepository localFileRepository;
    
    /**
     * {@inheritDoc}
     */
    public String replaceCurrentCCR(String patientId, CCRDocument ccr) throws PHRException, ServiceException {
        
        factory.getAccountService().addAccountDocument(
                        patientId, 
                        ccr.getGuid(), 
                        AccountDocumentType.CURRENTCCR, 
                        "Saved new Current CCR", 
                        true, 
                        CCRConstants.CCR_CHANGE_NOTIFICATION_STATUS_NOTIFIED);
        return ccr.getGuid();
    }

    public String saveCCRMerge(CCRDocument mergeFrom, CCRDocument mergeTo, AccountDocumentType type) throws PHRException, CCRStoreException {
        
        // If the source and the destination are the same, no need to save!
        if(mergeFrom == mergeTo)
            return mergeFrom.getGuid();
        
        mergeTo.syncFromJDom();
        mergeTo.setGuid(mergeTo.calculateGuid());
        
        try {
            long expirySeconds = Configuration.getProperty("TrackingNumberPinExpirySeconds", 0);
            DocumentRegistration docReg = 
                factory.getTrackingService().registerTrackDocument(mergeTo.getStorageId(), mergeTo.getGuid(), "", "", expirySeconds, null);
            
            String guid = this.getRepository().putDocument(mergeTo.getStorageId(), mergeTo.getXml(), CCRConstants.CCR_MIME_TYPE);
            mergeTo.setGuid(guid);
            mergeTo.setTrackingNumber(docReg.getTrackingNumber());
            factory.getAccountService().addCCRLogEntry(
                            mergeTo.getGuid(),
                            null, 
                            mergeTo.getValue("sourceEmail"),
                            mergeTo.getValue("toEmail"), 
                            mergeTo.getStorageId(), new Date(),  
                            "Updated Current CCR", "Complete", 
                            docReg.getTrackingNumber());
            return guid;
        }
        catch (NoSuchAlgorithmException e) {
            throw new CCRStoreException("Unable to save result of merge " + mergeTo.getGuid() + " under storage id " + mergeTo.getStorageId(), e);
        }
        catch (IOException e) {
            throw new CCRStoreException("Unable to save result of merge " + mergeTo.getGuid() + " under storage id " + mergeTo.getStorageId(), e);
        }
        catch (ServiceException e) {
            throw new CCRStoreException("Unable to save result of merge " + mergeTo.getGuid() + " under storage id " + mergeTo.getStorageId(), e);
        }
    }
    
    private LocalFileRepository getRepository() {
        if (localFileRepository == null)
            localFileRepository = (LocalFileRepository) RepositoryFactory.getLocalRepository();
        return localFileRepository;        
    }

    public CCRDocument resolveByName(String name, String accid) {
        log.info("Attempt to resolve by name a fixed content document, returning null " + accid + ", " + name);
        return null;
    }
}
