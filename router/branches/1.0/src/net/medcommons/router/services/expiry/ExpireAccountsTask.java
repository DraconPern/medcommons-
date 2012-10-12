package net.medcommons.router.services.expiry;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.TimerTask;

import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;

import org.apache.log4j.Logger;

/**
 * Checks for any accounts that have been expired and removes their PHI data.
 * 
 * @author ssadedin
 */
public class ExpireAccountsTask extends TimerTask {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ExpireAccountsTask.class);
    
    ServicesFactory services;
    
    DocumentIndexService indexService;
    
    ProfileService profiles;
    
    ActivityLogService activityLog;
    
    LocalFileRepository repo = (LocalFileRepository) RepositoryFactory.getLocalRepository();
    
    
    /*
    static {
        Metric.register("ExpiryTask.AverageBackupDelayMs", 
                        new TimeSampledMetric(Metric.getInstance("SmallFileBackupService.BackupDelayMs"), 2000, 20));
        Metric.register("ExpiryTask.AverageBackupJobTimeMs", 
                        new TimeSampledMetric(Metric.getInstance("SmallFileBackupService.BackupJobTimeMs"), 2000, 20));
    }
    */
  
    public ExpireAccountsTask(ServicesFactory services, DocumentIndexService indexService, ProfileService profiles) {
        super();
        this.services = services;
        this.indexService = indexService;
        this.profiles = profiles;
        this.activityLog = services.getActivityLogService();
    }

    public ExpireAccountsTask(ServicesFactory services, BackupService backupService) {
        super();
        this.services = services;
    }

    @Override
    public void run() {
        
        try {
            // Get the expired accounts
            AccountCreationService accountCreationService = services.getAccountCreationService();
            String [] accids = accountCreationService.queryExpiredAccounts();  
            log.info("Found " + accids.length + " accounts requiring expiration"); 
            for(String accid : accids) {
                
                try {
                    log.info("Expiring account " + accid);
                    this.expire(accid);
                    accountCreationService.deleteExpiredAccount(accid,null);
                    Metric.addSample("ExpiryTask.expiredAccounts");
                }
                catch(Exception e) {
                    Metric.addSample("ExpiryTask.expirationFailures");
                    accountCreationService.deleteExpiredAccount(accid,AccountType.DELETE_FAIL.name());
                    log.error("Failed to expire account " + accid,e);
                }
            }
        }
        catch(Exception e) {
            log.error("Unable to query or process expired accounts", e);
        }
    }

    private void expire(String accid) throws ServiceException {
        
        // Get all documents for user
        try {
            
            // It may look odd to log an activity event immediately before deleting the log, but it will
            // be captured by the auditing layer
            ActivityEvent clearEvent =
                new ActivityEvent(ActivityEventType.PHR_UPDATE, "Activity Log Cleared",new AccountSpec("MedCommons", "ExpiryTask"),
                    accid, "","");
            
            this.activityLog.log(clearEvent);
            
            // Delete the activity log
            this.activityLog.clear(accid); // it actually does a delete
            
            // Delete the profiles
            this.profiles.deleteProfile(accid);
            
            List<DocumentDescriptor> docs = indexService.getDocuments(accid, null, null, null);
            for(DocumentDescriptor doc : docs) {
                
                if(DocumentTypes.DICOM_STUDY_MIME_TYPE.equals(doc.getContentType())) {
                    // Don't delete entries for studies as they have no physical 
                    // manifestation
                    continue;
                }
                
                try {
	                if(doc instanceof CompoundDocumentDescriptor) {
	                    log.info("Expiring compound document " + doc.toShortString());
	                    
	                    doc.setGuid(doc.getSha1());
	                    
	                    // In the index all the compound documents belonging to a single entity
	                    // are indexed using a single entry. Here we don't just want to delete one 
	                    // entry, actually we want to delete all of them.
	                    repo.deleteAllDocuments((CompoundDocumentDescriptor)doc);
	                    
	                }
	                else {
	                    log.info("Expiring document " + doc.toShortString());
	                    
	                    // Convention in index is that guid is null if document has no 
	                    // parent, but convention in repository is that guid equal to 
	                    // SHA-1 for all simple documents.
	                    doc.setGuid(doc.getSha1());
	                    repo.deleteDocument(doc);
	                }
                }
                catch(RepositoryException ex) {
                    if(ex.getCause() != null && ex.getCause().getClass().isAssignableFrom(FileNotFoundException.class)) {
                        log.warn("Unable to delete expired document " + doc.toShortString() + " from repository: repository file not found");
                    }
                    else
                        throw ex;
                }
            }
        }
        catch(Exception e) {
            throw new ServiceException("Failed to expire account " + accid, e);
        }
    }
}
