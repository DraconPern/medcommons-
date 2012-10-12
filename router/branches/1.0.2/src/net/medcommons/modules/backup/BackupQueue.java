package net.medcommons.modules.backup;

import static net.medcommons.modules.utils.HibernateUtil.closeSession;
import static net.medcommons.modules.utils.HibernateUtil.currentSession;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.HibernateUtil;
import net.medcommons.router.services.repository.RepositoryEvent;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryListener;

import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * Interface to database backup queue.
 * 
 * Document entries are placed into a database table as they arrive.
 * A separate thread polls the table and initiates backup to
 * a remote backup service of anything that is in un-backed-up 
 * state.
 * 
 * @author mesozoic, ssadedin
 */
public class BackupQueue implements RepositoryListener {
    
	public static final String FAILED = "FAILED";
    public static final String QUEUED = "QUEUED";
	public static final String BACKED_UP = "BACKED_UP";
    public static final String RETRY = "RETRY";
	
	private static Logger log = Logger.getLogger(BackupQueue.class.getName());
	
	private BackupService backupService;
	
	public BackupQueue(BackupService backupService) {
	    this.backupService = backupService;
	    createIndexes();
	}
	
    private static boolean initialized = false;
    
    /**
     * Ugly - hibernate has a bug in the schema auto-update that prevents indexes
     * getting created.  So we have to make them ourselves.
     */
    private void createIndexes() {
        if(initialized)
            return;
        
        initialized = true;
        
        log.info("Initializing indexes");
        
        Session s = currentSession();        
        Statement stmt = null;
        try {
            stmt = s.connection().createStatement();
            execSql(stmt, "create index bq_queuetime_idx on backup_queue (queuetime)");
            execSql(stmt, "create index bq_accountid_idx on backup_queue (account_id)");
            execSql(stmt, "create index bq_status_idx on backup_queue (status)");
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to create statement for initializing backup_queue table");
        }
        finally {
            if(stmt != null) try { stmt.close(); } catch (SQLException e) { }
            closeSession();
        }
    }

    /**
     * Exceute sql and ignore any errors / warnings
     */
    private void execSql(Statement stmt, String sql) {
        try {
            stmt.executeUpdate(sql);
            log.info("Created index using: " + sql);
        }
        catch(Exception e) {
            log.info("Failed to initialize document_index table");
        }
    }
	
	/**
	 * Inserts backup entry into a database table. Returns the modified object
	 * if the insert is successful.
	 * 
	 * The only diffence between the returned BackupQueueEntry object and
	 * the input one is the getId() value - the database driver sets 
	 * this value which is useful for deletion.
	 * 
	 * @param backupQueueEntry
	 * @return
	 */
	public BackupQueueEntry insert(BackupQueueEntry backupQueueEntry) {
	    log.debug("Inserting entry in backup queue for "
	            + backupQueueEntry.getAccountId() + " "
	            + backupQueueEntry.getGuid());
	    Session session = HibernateUtil.currentSession();
	    try {
	        session.beginTransaction(); 
	        
	        int rows = session.createQuery("delete from BackupQueueEntry q where q.accountId = :accid and q.guid = :guid and q.status = :queued")
				              .setLong("accid", backupQueueEntry.getAccountId())
				              .setString("guid", backupQueueEntry.getGuid())
				              .setString("queued", BackupQueue.QUEUED)
				              .executeUpdate();
	        if(rows > 0) 
	            log.info(rows + " backup entries superceded by new entry for " + backupQueueEntry);
	               
	        session.save(backupQueueEntry);
	        session.getTransaction().commit();
	    }
	    finally {
	        HibernateUtil.closeSession();
	    }
	    
	    return (backupQueueEntry);
	}
	
	/**
	 * Deletes backup queue entry with a given getId().
	 * 
	 * This routine is not invoked by the standard code; it's only used by the BackupQueueTest tests 
	 * which clean up after themselves (to avoid confusing the background process which reads this table).
	 * 
	 * @param backupQueueEntry
	 */
	public void delete(BackupQueueEntry backupQueueEntry){
		
		log.info("Inserting backupQueue entry for " + backupQueueEntry.getAccountId() + " " + backupQueueEntry.getGuid());
		Session session = HibernateUtil.currentSession();
		try {
			session.beginTransaction();
			session.delete(backupQueueEntry);
	
			session.getTransaction().commit();
		}
		finally {
			HibernateUtil.closeSession();
		}
		log.info("backupQueueId is " + backupQueueEntry.getId());
	}

    @Override
    public void onBeginStoreDocument(RepositoryEvent evt) throws RepositoryException {
        // Ignore
    }

    @Override
    public void onEndStoreDocument(RepositoryEvent evt) throws RepositoryException {
        
        DocumentDescriptor document = evt.desc;
        Long accountId = new Long(document.getStorageId());
        if(accountId <= 0) {
            log.debug("Ignoring backup for POPS document " + document.getGuid());
            return;
        }
        
        BackupQueueEntry backupQueueEntry = new BackupQueueEntry();
        backupQueueEntry.setAccountId(accountId);
        backupQueueEntry.setGuid(document.getGuid());
        Long documentSize = new Long(document.getLength());
        backupQueueEntry.setSize(documentSize);
        backupQueueEntry.setStatus(BackupQueue.QUEUED);
        
        // If it is a compound document, schedule it for a delayed backup
        // in case more pieces of the document are coming in
        if(document instanceof CompoundDocumentDescriptor) {
            backupQueueEntry.setQueuetime(new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000));
        }
        
        insert(backupQueueEntry);
    }

    @Override
    public void onInput(RepositoryEvent evt) throws RepositoryException {
        // Ignore
    }

    @Override
    public void onOutput(RepositoryEvent evt) throws RepositoryException {
        // Ignore
    }

    @Override
    public void onFileUnavailable(RepositoryEvent evt, File f) throws RepositoryException {
        try {
            if(evt.desc != null) 
	            this.restoreDocument(evt.desc, f);
            else
                log.info("Cannot restore file " + f + " because document " + evt + " is not known to the system");
        }
        catch (BackupException e) {
            throw new RepositoryException("Restore of file " + f.getAbsolutePath() + " for document " + evt.desc.toShortString() + " failed", e);
        }
    }
    
    public void restoreDocument(DocumentDescriptor doc, File f) throws BackupException {
        
        try {
            if(doc instanceof SimpleDocumentDescriptor) {
                restoreDocument((SimpleDocumentDescriptor)doc,f);
            }
            else
            if(doc instanceof CompoundDocumentDescriptor) {
                restoreDocument((CompoundDocumentDescriptor)doc,f);
            }
            else
                throw new IllegalArgumentException("Request to restore unknown document type " + doc + "("+doc.toShortString()+")");
        }
        catch (Exception e) {
            throw new BackupException("Unable to restore document " + doc, e);
        }
    }

    private void restoreDocument(SimpleDocumentDescriptor doc, File f) throws Exception {
        
        File parentFolder = f.getParentFile();
        if(!parentFolder.exists())
            if(!parentFolder.mkdirs())
                throw new IOException("Unable to create directory for file restore: " + parentFolder.getAbsolutePath());
                
        backupService.restore(doc.getStorageId(), doc.getGuid(), f);
        
        backupService.restore(doc.getStorageId(), doc.getGuid()+".properties", new File(f.getAbsolutePath()+".properties"));
    }
    
    private void restoreDocument(CompoundDocumentDescriptor doc, File f) throws Exception {
        
        log.info("Restoring compound document " + doc.toShortString());
        
        ZipInputStream in = null;
        try {
            InputStream backupStream = backupService.openStream(doc.getStorageId(), doc.getGuid());
            if(backupStream == null) {
                log.info("Document " + doc.toShortString() + " not found in backup");
                return;
            }
            
            log.info("Opened backup resource as stream for restore");
            
            in = new ZipInputStream(backupStream);
            log.info("Opened ZIP stream on backup stream");
            
            ZipEntry ze = null;
            while((ze = in.getNextEntry()) != null) {
                
                log.debug("Found backed up entry: " + ze.getName());
                
                File zeFile = new File("data/Repository", ze.getName());
                if(ze.isDirectory() && !zeFile.exists()) {
                    if(!zeFile.mkdirs())
                        throw new IOException("Unable to create directory: " + zeFile.getAbsolutePath());
                    continue;
                }
                
                if(!zeFile.getParentFile().exists())
                    if(!zeFile.getParentFile().mkdirs())
                        throw new IOException("Unable to create parent directories for file: " + zeFile.getAbsolutePath());
                    
                // Not a directory
                OutputStream out = new FileOutputStream(zeFile);
                log.info("Restoring file " + zeFile.getAbsolutePath());
                try {
                    copy(in, out);
                }
                finally {
                    closeQuietly(out);
                }
                
                in.closeEntry();
            }
        }
        catch(Exception e) {
            f.delete();
            throw e;
        }
        finally {
            closeQuietly(in);
        }
    }

    @Override
    public void onDelete(RepositoryEvent evt) throws RepositoryException {
        try {
            log.info("Deleting backups for document " + evt.desc);
            String guid = evt.desc.getGuid();
            backupService.delete(evt.desc.getStorageId(), guid);
            if(evt.desc instanceof SimpleDocumentDescriptor) {
                backupService.delete(evt.desc.getStorageId(), guid + ".properties");
            }
        }
        catch (BackupException e) {
            throw new RepositoryException("Failed to delete resource " + evt.desc + " from backup",e);
        }
    }
}