package net.medcommons.application.dicomclient;

import static net.medcommons.application.dicomclient.utils.Params.where;

import java.util.List;

import net.medcommons.application.dicomclient.transactions.CCRRef;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.PatientMatch;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.Params;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.utils.dicom.DicomNameParser;
import net.sourceforge.pbeans.Store;

import org.apache.log4j.Logger;

/**
 * A service which monitors active transactions and notices when they finish.
 * <p>
 * Closes out a series transaction when there have been no new elements from
 * the series added in SERIES_COMPLETE_DELTA_SECONDS and the series is not
 * already closed.
 * 
 * @author sdoyle@medcommons.net, ssadedin@medcommons.net
 */
class StudyCompletionMonitor implements Runnable {
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(StudyCompletionMonitor.class);
	
    /**
	 * Listener to notify when studies are complete
	 */
	private final StudyCompletionListener listener;
	
	/**
	 * @param uploadHandler
	 */
	public StudyCompletionMonitor(StudyCompletionListener listener) {
		this.listener = listener;
	}

	long timeOut = 2 * 1000;

    boolean running = true;

    /**
     * Main body of thread.  Loops indefinitely, polling database to 
     * see if all series are completed for a study; if they are then the study is marked
     * completed on one of two ways: either it's queued for upload (if the 
     * storage id of the account can be uploaded) or put into the pending merge queue.
     */
    @SuppressWarnings("unchecked")
	public void run() {
       
        while (running) {
            try {

                Thread.sleep(timeOut);
                
                // Now check for brand new studies
                checkForNewStudies();
                
                // Look for any studies that have become complete
                // since we last checked
                checkForCompleteStudies();
                
            } 
            catch (Exception e) {
                log.error("Error in CloseSeriesTransaction", e);
            }
        }
    }

	@SuppressWarnings("unchecked")
	private void checkForCompleteStudies() {
		
		Store db= DB.get();
		
		// First select series that are done.
		// Done means:
		// They are in state 'completed'
		// All of the series for this studyinstanceuid are also
		// 'completed'
		List<DicomTransaction> completedTransactions = 
		    db.all(DicomTransaction.class, where("status", DicomTransaction.STATUS_COMPLETE));

		if(completedTransactions.isEmpty())
			return;
			
		log.info("There are " + completedTransactions.size() + " series that are completed");
		for(DicomTransaction trans : completedTransactions) {
			
			log.info("completed transaction :" + trans.toString());
			List<DicomTransaction> incompleteTransactions = getIncompleteTransactionsForStudy(trans);
			if(incompleteTransactions.size() > 0) {
				log.info("There are "
						+ incompleteTransactions.size()
						+ " series not yet completed for studyInstanceUID "
						+ trans.getStudyInstanceUid());
			}
			else {
				// There are no incomplete transactions for study.
				// Queue upload.
				List<DicomTransaction> completedSeries = 
				    db.all(DicomTransaction.class, 
				            where("status", DicomTransaction.STATUS_COMPLETE)
				             .and("studyInstanceUid", trans.getStudyInstanceUid()));
				
				// log.info("Search by studyinstance uid and
				// completed = true is gives " + seriesToUpload.size());
				if(completedSeries.size() == 0) {
					throw new RuntimeException(
							"Inconsistent database - no studies found with studyInstanceUid="
							+ trans.getStudyInstanceUid());
				}
				
				ContextState contextState = trans.resolveContextState();
				listener.studyComplete(contextState,completedSeries);
			}
			break; // Only handle one at a time - if we found one, break out and return
		}
	}

	@SuppressWarnings("unchecked")
    private void checkForNewStudies() {
		
		Store db = DB.get();
		
		List<DicomTransaction> txs =
		    db.all(DicomTransaction.class, where("status", DicomTransaction.STATUS_ACTIVE).and("cxpJob", DicomTransaction.UNITIALIZED_CXPJOB));
		
		if(txs.isEmpty()) {
			// log.info("No active transactions");
			return;
		}
		
		for(DicomTransaction tx : txs) {
		    
		    // The transaction is not new if it has already been assigned
		    // a patient context
		    if(tx.getContextStateId() != null)
		        continue;
		    
			log.info("Found new active transaction " + tx);
		    this.listener.newTransaction(tx);
		}
	}

	private List<DicomTransaction> getIncompleteTransactionsForStudy(DicomTransaction trans) {
		Store db = DB.get();
		List<DicomTransaction> incompleteTransactions =  
		    db.all(DicomTransaction.class, where("status", DicomTransaction.STATUS_ACTIVE)
		                                    .and("studyInstanceUid", trans.getStudyInstanceUid()));
		    
		return incompleteTransactions;
	}
	

	
}