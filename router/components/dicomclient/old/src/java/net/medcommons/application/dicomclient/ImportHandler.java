package net.medcommons.application.dicomclient;

import java.awt.Image;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.StaleStateException;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;


import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.LocalHibernateUtil;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.services.interfaces.DicomMetadata;

/**
 * Handles import process.
 * <ul>
 * <li> Inserts objects into databases.
 * <li> Separate thread mark series as done.
 * </ul>
 *
 * TODO: add some subclasses of exception. Some of these may be caught
 * by the CstoreScp CSTORE event and the state sent back to the calling DICOM CSTORE
 * SCU.
 *
 * Some general DICOM philosophy for the logic in this routine:
 * <ul>
 *   <li> The basic transactional unit in DICOM is the series.
 *   <li> For a non-PACS application - there is no way to detect the total number of objects in
 *        a series. A timeout is used to detect the end of a series. When the timeout has
 *        been reached - the series is marked as closed.
 *   <li> Each time a new DICOM instance arrives the series lastModifiedTime is updated.
 *        This is true whether or not the object is a duplicate.
 *   <li> It's an error to add a new object to a closed series. There is a race condition here
 *        that might be fixable with code restructuring. When the series is closed it is (eventually) handled by the
 *        UploadHandler. At this time the contents are moved to a new directory. Writing
 *        new objects into this directory while the contents are moved may cause all sorts of
 *        inconsistencies - so it's not permitted. Note that the UploadHandler moves the objects
 *        to a new folder which has as its root a timestamp - so the window where new series objects
 *        can't be processed is from the time that the series is marked as closed until the time
 *        that the uploadHandler has moved the data. The UploadHandler deletes the database entries
 *        so the new case is identical with a new series arriving.
 *   <li> Each series is independent from the rest - later routines accumulate series into
 *   	  higher-order objects (like studies).
 * </ul>
 *
 * It's not clear that the timeout/series closed race condition is a practical issue at all. Most DICOM
 * CSTORE SCUs will send a series in a single DICOM association - all of the images should arrive
 * well within the timeout window. If there are other images later sent for a series they will appear
 * in MedCommons as a separate document with its own GUID. The MedCommons viewer might be made smarter
 * to handle this case.
 *
 * The only clinical situation that this might be an issue in would be if the DICOM modality (e.g.,
 * the CT or MR scanner) was generating images very slowly and sending them (say) at 15 second
 * intervals. If this is the case - we can modify the timeout parameteters *or* simply ask them to
 * manually send the study once it is complete.
 *
 * @author mesozoic
 *
 */

public class ImportHandler {

	private static Logger log = Logger.getLogger(ImportHandler.class.getName());



	public ImportHandler() {

		CloseSeriesTransaction closeSeriesTransaction = new CloseSeriesTransaction();

		Thread t = new Thread(closeSeriesTransaction);
		t.start();
	}

	/**
	 * Tests for existing sopInstanceUid. Typically used by the calling
	 * routine to decide if the incoming DICOM object should be saved or not.
	 *
	 * @param sopInstanceUid
	 * @return
	 */
	public boolean sopInstanceExists(String sopInstanceUid){
		boolean exists = false;
		Session session = LocalHibernateUtil.currentSession();
		Criteria critSopInstance = session.createCriteria(DicomMetadata.class);
		critSopInstance.add(Expression.eq("sopInstanceUid", sopInstanceUid));

		List<DicomMetadata> matches = critSopInstance.list();
		if (matches.size() == 0)
			exists = false;
		else{
			session.beginTransaction();
			DicomMetadata dicomMetadata = matches.get(0);
			log.info("Duplicate SOPInstance:" + dicomMetadata);
			String seriesInstanceUid = dicomMetadata.getSeriesInstanceUid();
			exists = true;
			Criteria crit = session.createCriteria(DicomTransaction.class);
			crit.add(Expression.eq("seriesInstanceUid", seriesInstanceUid)).add(Expression.eq("status", DicomTransaction.STATUS_ACTIVE));
			crit.setMaxResults(10);
			List<DicomTransaction> transactions = crit.list();

			processTransaction(session, transactions, dicomMetadata, false);
			session.getTransaction().commit();
			LocalHibernateUtil.closeSession();
		}
		LocalHibernateUtil.closeSession();
		return(exists);

	}
	public void cstoreEvent(DicomMetadata dicomMetadata)
			throws HibernateException {


		if ((dicomMetadata.getSeriesInstanceUid() == null) || (dicomMetadata.getStudyInstanceUid() == null)){
			log.info("Null DICOM UIDs in :" + dicomMetadata.toShortString());
			log.info("Ignoring ill-formed content");
			return;
		}
		String seriesInstanceUid = dicomMetadata.getSeriesInstanceUid();
		Session session = LocalHibernateUtil.currentSession();

		session.beginTransaction();
		session.save(dicomMetadata);
		log.debug("cstore: \n" + dicomMetadata.toShortString());

		// Query for existing DICOMTransaction.
		// If it exists; then use it.
		// Otherwise create it.

		Criteria crit = session.createCriteria(DicomTransaction.class);
		crit.add(Expression.eq("seriesInstanceUid", seriesInstanceUid));
		crit.add(Expression.eq("status", DicomTransaction.STATUS_ACTIVE));
		crit.setMaxResults(10);
		List<DicomTransaction> transactions =  crit.list();
		processTransaction(session, transactions, dicomMetadata, true);

		//
		// TODO:   NEED TO HANDLE STALESTATEEXCEPTION
		//
		session.getTransaction().commit();
		LocalHibernateUtil.closeSession();




	}

	/**
	 * Processes the transaction object(s) for an incoming dicomMetadata.
	 *
	 * Queries for an existing transaction object. If it exists, use it
	 * (update the lastModifiedTime timestamp); otherwise create a new one.
	 *
	 * This method is called for both objects that are being saved and for
	 * duplicates - the series transaction is 'alive' as long as there are
	 * new objects arriving.
	 *
	 * There should be one and only one DicomTransaction object with a given
	 * SeriesInstanceUid. The SeriesInstanceUids are guaranteed to be globally
	 * unique by DICOM.
	 *
	 * If the object is not a duplicate the boolean incrementObjectCount is true;
	 * this updates the transaction's object count and total number of bytes.
	 *
	 * @param session
	 * @param transactions
	 * @param dicomMetadata
	 * @param incrementObjectCount
	 */
	private void processTransaction(Session session, List<DicomTransaction> transactions, DicomMetadata dicomMetadata, boolean incrementObjectCount){
		if (transactions.size() == 0) {


			DicomTransaction newTransaction = new DicomTransaction();
			newTransaction.setStatus(DicomTransaction.STATUS_ACTIVE);
			newTransaction.setCxpJob(DicomTransaction.UNITIALIZED_CXPJOB);
			newTransaction.setTimeStarted(System.currentTimeMillis());
			newTransaction.setLastModifiedTime(System.currentTimeMillis());
			newTransaction.setSeriesInstanceUid(dicomMetadata.getSeriesInstanceUid());
			newTransaction.setStudyInstanceUid(dicomMetadata
					.getStudyInstanceUid());
			newTransaction.setSeriesDescription(dicomMetadata
					.getSeriesDescription());
			newTransaction.setObjectCount(1);
			newTransaction.setPatientName(dicomMetadata.getPatientName());
			newTransaction.setStudyDescription(dicomMetadata.getStudyDescription());
			if (incrementObjectCount)
				newTransaction.setTotalBytes(dicomMetadata.getLength());
			log.info("Created new transaction object:" + newTransaction.toString());
			StatusDisplayManager.getStatusDisplayManager().setMessage("New DICOM series",
	    			dicomMetadata.getPatientName());
			session.save(newTransaction);
		} else if (transactions.size() == 1) {


			DicomTransaction currentTransaction = transactions.get(0);
			//log.info("transaction state:" + currentTransaction.getCompleted());
			boolean transUpdated = updateTransactionTime(currentTransaction);
			if (!transUpdated){
				throw new RuntimeException("Attempt to update transaction failed:\n +" +
						currentTransaction.toString() + "\n" +
						dicomMetadata.toString());
			}
			if (incrementObjectCount){
				currentTransaction.incrementObjectCount();
				currentTransaction.incrementTotalBytes(dicomMetadata.getLength());
				StringBuffer buff = new StringBuffer();
				buff.append("Series for ");
				buff.append(dicomMetadata.getPatientName());
				buff.append("\n Number of images:"); buff.append(currentTransaction.getObjectCount());
				buff.append("\n Number of MB:"); buff.append((int) currentTransaction.getTotalBytes()/(1024 * 1024));
				StatusDisplayManager.getStatusDisplayManager().setToolTip(buff.toString());
			}
			log.debug("Updating transaction time on existing  transaction object:"+ currentTransaction);
			session.save(currentTransaction);
		} else {
			log.error("Error! There is more than one transaction object with the same seriesInstanceUid:"
							+ dicomMetadata.getSeriesInstanceUid());
			for (int i = 0; i < transactions.size(); i++) {
				log.error("Entry " + i + " "
						+ transactions.get(i).getLastModifiedTime());

			}
			throw new RuntimeException("More than one transaction object with same seriesInstanceUid");
		}
	}
	/**
	 * Updates the transaction time for a given transaction.
	 * @param transaction
	 */

	private boolean updateTransactionTime(DicomTransaction transaction){
		boolean success = false;
		log.debug("Updating transaction time on existing  transaction object");


		if (transaction.getStatus().equals(DicomTransaction.STATUS_COMPLETE)) {
			log.fatal("Error: series "
					+ transaction.getSeriesInstanceUid()
					+ " was already marked as done");
			success = false;

		}
		else{
			transaction.setLastModifiedTime(System.currentTimeMillis());
			success = true;
		}
		return(success);
	}


	/**
	 * Closes out a series transaction when there have been no new elements from
	 * the series added in SERIES_COMPLETE_DELTA_SECONDS and the series is not
	 * already closed.
	 */
	private class CloseSeriesTransaction implements Runnable {


		long timeOut = 2 * 1000;

		boolean running = true;

		public void run() {
			while (running) {
			    Session session = LocalHibernateUtil.currentSession();
				try {
					// log.info("In CloseSeriesTransaction.run()");
					Thread.sleep(timeOut);
					// Each iteration - check the dicomTimeout value. Bit of an overkill - but
					// it means that if the user resets the timeout that the behavior of
					// app changes on the fly.
					Configurations configurations = ContextManager.getContextManager().getConfigurations();
					int seriesTimeout = configurations.getDicomTimeout();
					long seriesCompletionDelta = seriesTimeout * 1000; // msec

					session = LocalHibernateUtil.currentSession();

					// Query for existing DICOMTransaction.
					// If it exists; then use it.
					// Otherwise create it.
					long setDoneTime = System.currentTimeMillis()
							- seriesCompletionDelta;

					Criteria crit = session
							.createCriteria(DicomTransaction.class);
					crit.add(Expression.le("lastModifiedTime", setDoneTime));
					crit.add(Expression.eq("status", DicomTransaction.STATUS_ACTIVE));
					List<DicomTransaction> transactions = crit.list();
					if (transactions.size() > 0) {
						log.info("There are " + transactions.size() + " active transactions");
						for (int i = 0; i < transactions.size(); i++) {

							DicomTransaction trans = transactions.get(i);
							log.info("trans is " + trans);
							log.info(trans.getId() + " " + trans.getSeriesDescription() + " total bytes " + trans.getTotalBytes());
							if (trans.getTotalBytes() > 0){
								closeSeries(session, trans);
								log.info("Marking transaction done for series : "
									+ trans.toString());
							}
							else{
								closeEmptySeries(session, trans);
								StatusDisplayManager sdm = StatusDisplayManager.getStatusDisplayManager();
								sdm.setErrorMessage("Duplicate Series ignored",
										trans.getPatientName() + ", series " + trans.getSeriesDescription());
								
							}
						}
					}
					
				    if(session != null && session.isConnected() && session.isOpen())
				        LocalHibernateUtil.closeSession();
				} 
				catch(StaleObjectStateException exStale) {
				    log.info("Stale object " + exStale.getEntityName() + " " + exStale.getIdentifier() + " - will reprocess");
				    if(session != null && session.isConnected() && session.isOpen())
				        LocalHibernateUtil.closeSession();
				}
				catch (Exception e) {
					log.error("Error in CloseSeriesTransaction", e);
					StatusDisplayManager sdm = StatusDisplayManager.getStatusDisplayManager();
					sdm.setErrorMessage("Error in local database", e.getLocalizedMessage());
				}
			}
		}

		/**
		 * Marks the series closed; calculates sha1 for series.
		 *
		 * @param session
		 * @param trans
		 */
		private void closeSeries(Session session, DicomTransaction trans)
				throws NoSuchAlgorithmException {
			session.beginTransaction();
			Criteria crit = session.createCriteria(DicomMetadata.class);
			crit.add(Expression.eq("seriesInstanceUid", trans
					.getSeriesInstanceUid()));
			crit.addOrder(Order.asc("sha1"));
			List<DicomMetadata> dicomMetadata = crit.list();
			if (dicomMetadata.size() < 1) {
				throw new RuntimeException(
						"There are zero DicomMetadata elements for series "
								+ trans.getSeriesInstanceUid());
			}

			String[] sha1Array = new String[dicomMetadata.size()];
			for (int i = 0; i < sha1Array.length; i++) {
				sha1Array[i] = dicomMetadata.get(i).getSha1();
			}

			SHA1 sha1 = new SHA1();
			sha1.initializeHashStreamCalculation();
			//String seriesSha1 = sha1.calculateStringNameHash(sha1Array);
			String seriesSha1 = sha1.calculateStringHash(dicomMetadata.get(0).getSeriesInstanceUid());
			trans.setSeriesSha1(seriesSha1);
			trans.setStatus(DicomTransaction.STATUS_COMPLETE);
			trans.setTimeCompleted(System.currentTimeMillis());
			session.save(trans);
			session.getTransaction().commit();

		}
		private void closeEmptySeries(Session session, DicomTransaction trans)
		throws NoSuchAlgorithmException {
			session.beginTransaction();

			trans.setStatus(DicomTransaction.STATUS_PERMANENT_ERROR);
			trans.setStatusMessage("Series has no elements - probable duplicate SOPInstanceUIDs");
		
			trans.setTimeCompleted(System.currentTimeMillis());
			session.save(trans);
			session.getTransaction().commit();

		}
	}
}
