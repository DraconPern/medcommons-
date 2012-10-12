package net.medcommons.application.dicomclient;

import static net.medcommons.application.dicomclient.utils.Params.where;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.Logger;

import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.Params;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.sourceforge.pbeans.Store;

/**
 * Closes out a series transaction when there have been no new elements from
 * the series added in SERIES_COMPLETE_DELTA_SECONDS and the series is not
 * already closed.
 */
class CloseSeriesTransaction implements Runnable {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CloseSeriesTransaction.class);

	long timeOut = 2 * 1000;

	boolean running = true;

	public void run() {
		while (running) {
		    
		    Store db = DB.get();
			try {
				// log.info("In CloseSeriesTransaction.run()");
			    
				Thread.sleep(timeOut);
				
				// Each iteration - check the dicomTimeout value. Bit of an overkill - but
				// it means that if the user resets the timeout that the behavior of
				// app changes on the fly.
				Configurations configurations = ContextManager.get().getConfigurations();
				int seriesTimeout = configurations.getDicomTimeout();
				long seriesCompletionDelta = seriesTimeout * 1000; // msec
				long setDoneTime = System.currentTimeMillis() - seriesCompletionDelta;

				List<DicomTransaction> transactions = 
				    db.select(DicomTransaction.class,
				            "select * from dicom_transaction where lastmodifiedtime < ? and status = ?",
				            new Object[] { setDoneTime, DicomTransaction.STATUS_ACTIVE }).all();
				
				if(transactions.isEmpty())
				    continue;
				
				log.info("There are " + transactions.size() + " active transactions");
				for (int i = 0; i < transactions.size(); i++) {
				    
				    DicomTransaction trans = transactions.get(i);
				    db.requestLock(trans,trans.getSeriesInstanceUid());
				    boolean rollback = true;
				    try {
				        log.info("trans is " + trans);
				        log.info(trans.getId() + " " + trans.getSeriesDescription() + " total bytes " + trans.getTotalBytes());
				        
				        if (trans.getTotalBytes() > 0) {
				            closeSeries(trans);
				            log.info("Marking transaction done for series : " + trans.toString());
				        }
				        else {
				            closeEmptySeries(trans);
				            StatusDisplayManager sdm = StatusDisplayManager.get();
				            sdm.setErrorMessage("Duplicate Series ignored",
				                    trans.getPatientName() + ", series " + trans.getSeriesDescription());
				        }
				        rollback = false;
				    }
				    finally {
				        db.relinquishLock(rollback);
				    }
				    
				}
			} 
			catch (Exception e) {
				log.error("Error in CloseSeriesTransaction", e);
				StatusDisplayManager sdm = StatusDisplayManager.get();
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
	private void closeSeries(DicomTransaction trans)
			throws NoSuchAlgorithmException {
	    
	    Store db = DB.get();
	    List<DicomMetadata> dicomMetadata = 
	        db.select(DicomMetadata.class,
	                  "select * from dicom_meta_data where seriesinstanceuid = ? order by sha1 asc",
	                  new Object[]{trans.getSeriesInstanceUid()}).all();
	    
	    if (dicomMetadata.size() < 1) 
	        throw new RuntimeException("There are zero DicomMetadata elements for series "+trans.getSeriesInstanceUid());
	    
	    String[] sha1Array = new String[dicomMetadata.size()];
	    for (int i = 0; i < sha1Array.length; i++) {
	        sha1Array[i] = dicomMetadata.get(i).getSha1();
	    }
	    
	    SHA1 sha1 = new SHA1();
	    sha1.initializeHashStreamCalculation();
	    String seriesSha1 = sha1.calculateStringHash(dicomMetadata.get(0).getSeriesInstanceUid());
	    trans.setSeriesSha1(seriesSha1);
	    trans.setStatus(DicomTransaction.STATUS_COMPLETE);
	    trans.setTimeCompleted(System.currentTimeMillis());
	    db.save(trans);
	}
	
	/**
	 * Set the given series to error status and complete it's timestamp.
	 * Note: should be executed within lock on trans object
	 * @param trans
	 * @throws NoSuchAlgorithmException
	 */
	private void closeEmptySeries(DicomTransaction trans) throws NoSuchAlgorithmException {
	    Store db = DB.get();
		trans.setStatus(DicomTransaction.STATUS_PERMANENT_ERROR);
		trans.setStatusMessage("Series has no elements - probable duplicate SOPInstanceUIDs");
		trans.setTimeCompleted(System.currentTimeMillis());
		db.save(trans);
	}
}