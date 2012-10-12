package net.medcommons.application.dicomclient.utils;

import static net.medcommons.application.dicomclient.utils.Params.where;
import static net.medcommons.modules.utils.Str.blank;

import java.util.List;
import java.util.concurrent.Future;

import net.medcommons.application.utils.MonitorTransfer;
import net.sourceforge.pbeans.Store;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Shuts down DDL.
 * <p>
 * Active transactions are reset to Cancelled state.
 * <p>
 * In the future - all threads should be interrupted in a way appropriate for their
 * task or at least things should be able to restart at a known state.
 *
 * @author mesozoic
 */
public class Shutdown extends Thread implements Command {
	 private static Logger log = Logger.getLogger(Shutdown.class.getName());
	public Shutdown(){
		super();
	}
	public void run(){
		Shutdown.shutdownThreads();
		Shutdown.cleanup();
		Shutdown.shutdownDB();
	}
	
	private static void shutdownThreads(){
		// Prevent the monitor thread from sending out 
		// updates.
		MonitorTransfer.setShutdown();

	}
	public static void shutdownDB(){
		try {
		    DB.shutdown();
		}
		catch(Throwable t) {
		    log.warn("Error while shutting down hiberate",t);
		}

		try{
			Thread.sleep(250); // 1/4 second - just to give Hiberate threads time to terminate.
		}
		catch(InterruptedException e){
			;
		}
	}
	/**
	 * Note that this method may be called more than once upon exit.
	 *
	 */
	public static void cleanup(){

		Store db = DB.get();
		List<CxpTransaction> cxpTransactions = 
		    db.all(CxpTransaction.class, where("status", CxpTransaction.STATUS_ACTIVE));
		
		for (int i=0;i<cxpTransactions.size(); i++){
			CxpTransaction transaction = cxpTransactions.get(i);
			String status = transaction.getStatus();
			
			// Only reset job status of jobs which are currently uploading.
			// Leave errors and queued state in place.
			if (!blank(status)){
				if (CxpTransaction.STATUS_ACTIVE.equals(status)){
					transaction.setStatus(CxpTransaction.STATUS_CANCELLED);
					transaction.setBytesTransferred(0L);
					transaction.setTotalBytes(0);
					transaction.setStatusMessage("Cancelled by DDL shutdown");
					db.save(transaction);
					log.info("Status set to " + transaction.getStatus() + " " +
							"for id = " + transaction.getId() + ":" + transaction.getClass().getCanonicalName());
					String dashboardStatusId = transaction.getDashboardStatusId();
					if (dashboardStatusId != null){
						try{
							MonitorTransfer.sendStatusMessage(transaction, "ERROR:DDL Shutdown", 0.0d);
						}
						catch(Exception e){
							log.error("Error sending shutdown message to server for CXP transaction " + transaction);
						}
					}
					}
				
				}
			
		}

		List<DicomOutputTransaction> dicomOutTransactions =
		    db.all(DicomOutputTransaction.class, where("status", DicomOutputTransaction.STATUS_ACTIVE));

		for (int i=0;i<dicomOutTransactions.size(); i++){
			DicomOutputTransaction transaction = dicomOutTransactions.get(i);
			transaction.setStatus(DicomOutputTransaction.STATUS_CANCELLED);
			transaction.setBytesTransferred(0L);
			transaction.setStatusMessage("Cancelled by DDL shutdown");
			db.save(transaction);
			log.info("Status set to " + transaction.getStatus() + " for id = " + transaction.getId() + ":" + transaction.getClass().getCanonicalName());
		}

		List<DicomTransaction> dicomInTransactions = 
		    db.all(DicomTransaction.class, where("status", DicomTransaction.STATUS_ACTIVE));

		for (int i=0;i<dicomInTransactions.size(); i++){
			DicomTransaction transaction = dicomInTransactions.get(i);
		    db.requestLock(transaction, transaction.getSeriesInstanceUid());
		    try {
				transaction.setStatus(DicomTransaction.STATUS_CANCELLED);
				transaction.setBytesTransferred(0L);
				transaction.setStatusMessage("Cancelled by DDL shutdown");
				db.save(transaction);
		    }
		    finally {
		        db.relinquishLock();
		    }
			log.info("Status set to " + transaction.getStatus() + " for id = " + transaction.getId() + ":" + transaction.getClass().getCanonicalName());
		}
	}
	
    @Override
    public Future<JSONObject> execute(CommandBlock params) {
        new Thread() {
            @Override
            public void run() {
                try{
                    Shutdown.cleanup();
                }
                finally {
                    System.exit(0);
                }
            }
        }.start();
        return null;
    }
}
