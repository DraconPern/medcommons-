package net.medcommons.application.dicomclient.utils;

import java.util.List;

import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.utils.MonitorTransfer;
import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;



/**
 * Shuts down DDL.
 * In the future - all threads should be interrupted in a way appropriate for their
 * task or at least things should be able to restart at a known state.
 *
 * @author mesozoic
 *
 */
public class Shutdown extends Thread{
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
		Session session = LocalHibernateUtil.currentSession();
		
		try {
		    String sql = "SHUTDOWN";
		    SQLQuery query = session.createSQLQuery(sql);
		    query.executeUpdate();
		    LocalHibernateUtil.closeSession();
		    LocalHibernateUtil.getSessionFactory().close();
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


		// Run HSQL shutdown comment to free lock file.
		Session session = LocalHibernateUtil.currentSession();

		//TODO - need to clean up files too.
		
		
		Criteria activeCxp = session.createCriteria(CxpTransaction.class);
		activeCxp.add(Expression.eq("status", CxpTransaction.STATUS_ACTIVE));
		List<CxpTransaction> cxpTransactions = activeCxp.list();
		LocalHibernateUtil.closeSession();
		
		for (int i=0;i<cxpTransactions.size(); i++){
			CxpTransaction transaction = cxpTransactions.get(i);
			String status = transaction.getStatus();
			// Only reset job status of jobs which are currently uploading.
			// Leave errors and queued state in place.
			if (!Str.blank(status)){
				if (CxpTransaction.STATUS_ACTIVE.equals(status)){
					transaction.setStatus(CxpTransaction.STATUS_CANCELLED);
					transaction.setBytesTransferred(0L);
					transaction.setTotalBytes(0);
					transaction.setStatusMessage("Cancelled by DDL shutdown");
					transaction = TransactionUtils.saveTransaction(transaction);
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

		session = LocalHibernateUtil.currentSession();
		Criteria activeDicomOutput = session.createCriteria(DicomOutputTransaction.class);
		activeDicomOutput.add(Expression.eq("status", DicomOutputTransaction.STATUS_ACTIVE));
		List<DicomOutputTransaction> dicomOutTransactions = activeDicomOutput.list();
		LocalHibernateUtil.closeSession();

		for (int i=0;i<dicomOutTransactions.size(); i++){
			DicomOutputTransaction transaction = dicomOutTransactions.get(i);
			transaction.setStatus(DicomOutputTransaction.STATUS_CANCELLED);
			transaction.setBytesTransferred(0L);
			transaction.setStatusMessage("Cancelled by DDL shutdown");
			transaction = TransactionUtils.saveTransaction(transaction);
			log.info("Status set to " + transaction.getStatus() + " for id = " + transaction.getId() + ":" + transaction.getClass().getCanonicalName());
		}

		session = LocalHibernateUtil.currentSession();
		Criteria activeDicomInput = session.createCriteria(DicomTransaction.class);
		activeDicomOutput.add(Expression.eq("status", DicomTransaction.STATUS_ACTIVE));
		List<DicomTransaction> dicomInTransactions = activeDicomInput.list();
		LocalHibernateUtil.closeSession();

		for (int i=0;i<dicomInTransactions.size(); i++){
			DicomTransaction transaction = dicomInTransactions.get(i);
			transaction.setStatus(DicomTransaction.STATUS_CANCELLED);
			transaction.setBytesTransferred(0L);
			transaction.setStatusMessage("Cancelled by DDL shutdown");
			transaction = TransactionUtils.saveTransaction(transaction);
			log.info("Status set to " + transaction.getStatus() + " for id = " + transaction.getId() + ":" + transaction.getClass().getCanonicalName());
		}


	}
	

}
