package net.medcommons.application.dicomclient;

import java.util.List;

import org.apache.log4j.Logger;

import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.utils.DicomOutputTransaction;
import net.medcommons.application.dicomclient.utils.Params;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.sourceforge.pbeans.Store;

/**
 * A background thread which runs continuously and  polls for scheduled CXP 
 * download (GET) jobs one at a time from the queue
 * and executes them as it finds them.
 * <p>
 * Jobs are executed by creating instances of {@link DownloadCxpJob}
 * and running them asynchronously.   Thus it is possible for many
 * simultaneous jobs to be launched in parallel.
 * <p>
 * If a {@link DicomOutputTransaction} is attached to the {@link CxpTransaction}
 * being executed then the {@link DicomOutputTransaction} is queued 
 * when the {@link CxpTransaction} completes.   This is how DICOM gets
 * automatically exported to a PACS or file system after a CCR is downloaded.
 */
public class DownloadCCR implements Runnable {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DownloadCCR.class);
    
    long timeOut = 2 * 1000;
    
    boolean running = true;
    
    protected JobHandler jobHandler = JobHandler.JobHandlerFactory();
    
    public void run() {
        
        CxpTransaction trans = null;
        Store s = DB.get();
        
        while(running) {
            try {
                Thread.sleep(timeOut);
                
                // Select a queued job.
                // Should sort by time? Priority?
                trans = s.selectSingle(CxpTransaction.class, new Params() {{
                    put("status", CxpTransaction.STATUS_QUEUED);
                    put("transactionType", "GET");
                }});
                
                if(trans == null)
                    continue; // No jobs
                
                executeJob(trans);
            }
            catch(InterruptedException e) {
                // used by unit tests
            }
            catch(Exception e) {
                log.error("Error downloading", e);
                setErrorState(trans, e);
            }
        }
    }
    
    /**
     * Sets the given transaction to 'permanent error' state and
     * displays a message to the user.
     */
    void setErrorState(CxpTransaction trans, Exception e) {
        Store s = DB.get();
        try {
            s.requestLock(trans, trans.getStudyInstanceUid()); 
            trans.setStatus(CxpTransaction.STATUS_PERMANENT_ERROR);
            trans.setStatusMessage("No matching output transaction for job id " + trans.getId());
            s.save(trans);
	    }
        finally {
            s.relinquishLock();
        }
        StatusDisplayManager.get().setErrorMessage("Error downloading", e.toString(),trans.getDashboardStatusId());
    }
    
    /**
     * Finds the corresponding {@link DicomOutputTransaction} for the specified
     * {@link CxpTransaction} and launches a {@link DownloadCxpJob} to execute the
     * {@link CxpTransaction}, which in turn will queue the {@link DicomOutputTransaction}
     * when it completes. Adds the created job to the registry of active jobs 
     * (see {@link JobHandler}).
     */
    protected void executeJob(CxpTransaction trans) {
        Store s = DB.get();
        
        List<DicomOutputTransaction> dicomTransactions = 
            s.select(DicomOutputTransaction.class, "select * from dicom_output_transaction where CXPJOB = ?", new Object[] { trans.getId() })
            .all();
        
        if(dicomTransactions == null) 
            throw new IllegalStateException("No matching output transaction for job id " + trans.getId());
        
        if(dicomTransactions.size()!=1)
            throw new IllegalStateException("Found " + dicomTransactions.size() + " output transactions instead of 1 for job id " + trans.getId());
        
        DicomOutputTransaction out = dicomTransactions.get(0);
        DownloadCxpJob downloadCxpJob = createCxpJob(trans, out);
        jobHandler.addCxpJob(downloadCxpJob);
        
        try {
            downloadCxpJob.run();
        }
        finally {
            jobHandler.deleteCxpJob(trans.getId());
        }
    }

    /**
     * Create a new CXP job.  Only extracted to method to assist unit tests.
     */
    protected DownloadCxpJob createCxpJob(CxpTransaction trans, DicomOutputTransaction out) {
        return new DownloadCxpJob(trans,out);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}