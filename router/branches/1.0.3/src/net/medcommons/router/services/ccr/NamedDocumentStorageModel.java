/*
 * $Id$
 * Created on 13/04/2007
 */
package net.medcommons.router.services.ccr;

import org.apache.log4j.Logger;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.phr.DocumentNotFoundException;
import net.medcommons.phr.PHRDocument;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.db.PHRDB;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

/**
 * Uses named documents eg. "medcommons.currentccr" that are updated in place
 * for the storage model. 
 * 
 * @author ssadedin
 */
public class NamedDocumentStorageModel implements StorageModel {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(NamedDocumentStorageModel.class);

    public static final String CURRENTCCR_DOCUMENT_NAME = "medcommons.currentccr";
    
    /**
     * {@inheritDoc}
     */
    public String replaceCurrentCCR(String patientId, CCRDocument ccr) throws PHRException {
        
        
        PHRDB db = Configuration.getBean("phrDatabase");
        try {
            db.connect(patientId);
            db.save(CURRENTCCR_DOCUMENT_NAME, ccr.getJDOMDocument()); 
        }
        finally {
            db.close();
        }
                
        return CURRENTCCR_DOCUMENT_NAME;
    }

    public String saveCCRMerge(CCRDocument oldCurrentCCR, CCRDocument mergeTo, AccountDocumentType type) throws PHRException, CCRStoreException {
        PHRDB db = Configuration.getBean("phrDatabase");
        try {
            db.connect(mergeTo.getStorageId());
            db.save(CURRENTCCR_DOCUMENT_NAME, mergeTo.getJDOMDocument());
            return CURRENTCCR_DOCUMENT_NAME;
        }
        finally {
            db.close();
        }
    }

    public CCRDocument resolveByName(String name, String accid) throws PHRException {
        log.info("Attempt to resolve by name " + accid + ", " + name);
        // Get the Account database for the given account
        PHRDB db = Configuration.getBean("phrDatabase");
        
        // Get the PHRDocument out
        db.connect(accid);
        try {
            PHRDocument d = db.open(name);
            return new CCRDocument(accid, d);
        }
        catch(DocumentNotFoundException e) {
            log.info("Document " + name + " was not found for account " + accid);
            return null;
        }
        finally {
            db.close();
        }
    }
}
