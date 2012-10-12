package net.medcommons.modules.backup;

import static net.medcommons.modules.utils.HibernateUtil.getSessionFactory;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.*;
import java.sql.Timestamp;
import java.util.Date;

import net.medcommons.modules.filestore.SimpleRepository;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.HibernateUtil;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * The {@link BackupThread} runs in the background and polls the backup queue,
 * periodically processing any new documents that need to be backed up.
 * <p>
 * This is very crude right now - it's currently single threaded.  This 
 * makes outbound data to S3 limited to a single pipe.   One solution is 
 * to run multiple backup threads in parallel - as long as they are 
 * thread safe, that would work and not require any different code in 
 * this class.
 * 
 * @author ssadedin
 */
public class BackupThread implements Runnable {

    private static final int MAX_RETRY_TIME_MS = 2 * 3600 * 1000;

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(BackupThread.class);
    
    /**
     * Index used to retrieve document details
     */
    DocumentIndexService indexService; 

    /**
     * The backup implementation that will be used to store documents
     */
    BackupService backupService;
    
    /**
     * Create a backup thread.  The thread is not started.
     * 
     * @param indexService
     */
    public BackupThread(DocumentIndexService indexService, BackupService backupService) {
        this.indexService = indexService;
        this.backupService = backupService;
    }

    @Override
    public void run() {
        
        log.info("Starting backup poller");
        
        while(true) {
            if(!pollForJobs()) {
                // If no jobs found, sleep for a few seconds so we are not spinning needlessly
                try { Thread.sleep(3000); } catch (InterruptedException e) { }
            }
        }
    }
    
    /**
     * Polls for a single job and if found, executes it. 
     * 
     * @return
     */
    private boolean pollForJobs() {
        Session db = getSessionFactory().openSession();
        db.beginTransaction();
        BackupQueueEntry entry = null;
        try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Query query = db.createQuery("from BackupQueueEntry q where q.status = :queued and q.queuetime < :time order by q.queuetime desc")
				            .setString("queued", BackupQueue.QUEUED)
				            .setTimestamp("time", now)
				            .setMaxResults(1);
            
            entry = (BackupQueueEntry)query.uniqueResult();
            if(entry == null) {
                log.debug("No pending files found for backup");
                entry = (BackupQueueEntry)query.setString("queued", BackupQueue.RETRY)
									           .uniqueResult();
            }
            
            if(entry == null) {
                log.debug("No pending files found for backup");
                return false;
            }
            
            log.info("Found file scheduled for backup: " + entry.getGuid());
            
            try {
	            backupDocument(entry);
	            entry.setStatus(BackupQueue.BACKED_UP); 
            }
            catch(PermanentBackupError ex) {
	            entry.setStatus(BackupQueue.FAILED); 
            }
            db.update(entry);
            db.getTransaction().commit();
            return true;
        }
        catch(Throwable t) {
            log.error("Backup failed for entry " + entry,t);
            try {
	            if(db.getTransaction() != null && db.getTransaction().isActive())
	                db.getTransaction().rollback();
                
	            if(entry != null)
		            markForRetry(entry.getId());
            }
            catch(Exception e) {
                log.error("Failed to mark backup queue entry for retry: " + entry, t);
            }
            return true;
        }
        finally {
            db.close();
        }
    }
    
    /**
     * Attempt to mark the specified backup entry as requiring 
     * a RETRY at a later time period.
     * 
     * @param id
     * @throws BackupException
     */
    private void markForRetry(long id) throws BackupException {
        
        // Note we use a different hibernate session here.
        Session db = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = db.beginTransaction();
		        
	        BackupQueueEntry entry = (BackupQueueEntry) db.get(BackupQueueEntry.class, id);
	        if(entry == null) 
	            throw new IllegalStateException("Attempt to mark unknown backup entry " + id + " for retry");
	        
	        if(BackupQueue.BACKED_UP.equals(entry.getStatus()))
	            throw new IllegalStateException("Attempt to mark already backed up entry " + id + " for retry");
	        
	        if(BackupQueue.RETRY.equals(entry.getStatus()) && entry.getQueuetime().before(new Date(System.currentTimeMillis() - MAX_RETRY_TIME_MS))) {
	            entry.setStatus(BackupQueue.FAILED);
	        }
	        else {
	            log.info("Marking backup entry " + entry + " for retry");
	            entry.setStatus(BackupQueue.RETRY);
	            
	            // Requeue for 5 minutes from now
	            entry.setQueuetime(new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000));
	        }
	        
	        db.update(entry);
	        tx.commit();
        }
        finally {
            HibernateUtil.closeSession();
        }
    }

    private void backupDocument(BackupQueueEntry entry) throws ServiceException, RepositoryException, IOException {
        
        final DocumentDescriptor doc = indexService.getDocument(String.valueOf(entry.getAccountId()), entry.getGuid());
        
        if(doc == null) 
            throw new PermanentBackupError("Unable to locate document in meta data index for backup entry: " + entry.toString());
        
        LocalFileRepository repo = (LocalFileRepository)RepositoryFactory.getLocalRepository();
        
        if(doc instanceof CompoundDocumentDescriptor) {
            
            File sourceFile = new File(SimpleRepository.StorageDir(doc.getStorageId()), doc.getGuid());
            
            log.info("Backing up compound document " + doc.toShortString());
            
            // Here what we must have is a DICOM series
            // The backup generator actually works by accepting a single image
            // of the series as input and it then generates a backup of the *whole* series
            // So we need to rig the document descriptor to look like an actual
            // compound document member rather than the parent document (the series)
            doc.setGuid(doc.getSha1());
            
            // A compound document - we will zip and send to backup storage
            ZipArchiveFormat archiver = new ZipArchiveFormat(doc.getStorageId(), null, doc);
            File scratchFile = new File(sourceFile.getPath()+".zip");
            try {
                if(scratchFile.exists())
                    throw new IllegalStateException("Backup zip file " + scratchFile.getPath() + " already exists:  concurrent backup in progress?"); 
                
                OutputStream out = archiver.generateBackup(new FileOutputStream(scratchFile));
                closeQuietly(out);
                
                log.info("Created temporary zip file " + scratchFile + " for transfer to backup storage");
                
                backupService.backup(doc.getStorageId(), doc.getGuid(), scratchFile);
            }
            finally {
                if(scratchFile.exists())
                    if(!scratchFile.delete())
                        log.warn("Unable to delete scratch file " + scratchFile);
            }
            
        }
        else {
            log.info("Backing up simple document " + doc.toShortString());
            
            File sourceFile = repo.getDocumentFile(doc.getStorageId(), doc.getGuid(), doc.getGuid());
            log.info("Sending file " + sourceFile.getAbsolutePath() + " to backup storage");
            backupService.backup(doc.getStorageId(), sourceFile.getName(), sourceFile);
            
            log.info("Sending properties file " + sourceFile.getAbsolutePath() + ".properties to backup storage");
            File propFile = SimpleRepository.propertyFile(doc.getStorageId(), doc.getGuid()); 
            backupService.backup(doc.getStorageId(), propFile.getName(), propFile);
        }        
    }
}
