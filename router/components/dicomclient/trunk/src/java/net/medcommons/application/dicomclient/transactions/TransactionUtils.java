package net.medcommons.application.dicomclient.transactions;

import static net.medcommons.application.dicomclient.utils.Params.where;
import static net.medcommons.application.utils.Str.blank;
import static net.medcommons.application.utils.Str.bvl;
import static net.medcommons.application.utils.Str.nvl;

import java.util.List;

import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.utils.DicomOutputTransaction;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.Params;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.sourceforge.pbeans.Store;

import org.apache.log4j.Logger;

/**
 * Grab-bag of utilities to manage database transactions.
 * @author mesozoic
 *
 */
public class TransactionUtils {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(TransactionUtils.class);
    
	private static boolean useDB = true;
	
	public static void setUseDB(boolean useDatabase){
		useDB = useDatabase;
	}

	/*
	public static List<CxpTransaction> getCxpTransactionsWithDashboardStatusId(String dashboardStatusId){
		if (!useDB) return(null);
		Store s = DB.get();
		Criteria matching = session.createCriteria(CxpTransaction.class);
	    matching.add(Expression.eq("dashboardStatusId", dashboardStatusId));
	    List<CxpTransaction> transactions = matching.list();
	    return(transactions);
	}
	*/
	
	public static CCRRef getCCRReference(String storageId, String guid){
		if (!useDB) return(null);
		Store db = DB.get();
	    List<CCRRef> transactions = 
	        db.all(CCRRef.class, where("guid",guid).and("storageId",storageId));
	    
	    if (transactions == null) return null;
	    if (transactions.size()== 0)
	    	return(null);
	    else 
	    if (transactions.size()==1) {
	    	return(transactions.get(0));
	    }
	    else {
	    	throw new IllegalStateException("Expected a single match to CCRReference storageId=" + storageId + ", guid=" + guid +
	    			", not " +transactions.size() + " matches");
	    }
	
	}
	
	/**
	 * Set status for transaction  specified by given dashboard id to 
	 * cancelled so that the transfer is terminated.
	 * 
	 * @param transferKey     dashboard transfer key of transfer to terminate
	 */
	public static void cancelTransaction(String transferKey) {
	    
	    log.info("Cancelling transaction " + transferKey);
	    
		if (!useDB) return;
		
		Store db = DB.get();
		try { 
		    
		    String patientName = null;
		    String type = null;
		    
		    List<DicomTransaction> dtxs = db.all(DicomTransaction.class, where("transferKey",transferKey));
		    int count = 0;
		    for(DicomTransaction dtx : dtxs) {
		        dtx.setStatus(CxpTransaction.STATUS_CANCELLED);
		        db.save(dtx);
		        if(blank(patientName))
			        patientName = dtx.getPatientName();
		        ++count;
		    }
		    
		    log.info("Cancelled " + count + " series with transfer key " + transferKey);
		    
		    List<CxpTransaction> txs =  db.all(CxpTransaction.class, where("dashboardStatusId", transferKey));
		    count = 0;
		    for(CxpTransaction tx : txs) {
			    tx.setStatus(CxpTransaction.STATUS_CANCELLED);
			    db.save(tx);
			    if(blank(patientName))
			        patientName = tx.getPatientName();
			    type = tx.getTransactionType();
			    ++count;
		    }
		    log.info("Cancelled " + count + " studies with transfer key " + transferKey);
		    
		    StatusDisplayManager.get().setErrorMessage(
		        "Transfer Cancelled", 
		        nvl(type, "Transfer")  + " for patient " + bvl(patientName,"")  + " was cancelled", transferKey);
	                        
		    log.info("Successfully cancelled transaction " + transferKey);
		}
		finally {
		}
	}
	
	public static ContextState getContextState(Long contextStateId){
		if (!useDB) return(null);
		Store db = DB.get();
		
	    List<ContextState> transactions = db.all(ContextState.class, where("id",contextStateId));
	    if (transactions == null) return null;
	    if (transactions.size()== 0)
	    	return(null);
	    else if (transactions.size()==1){
	    	return(transactions.get(0));
	    }
	    else{
	    	throw new IllegalStateException("Expected a single match to ContextState contextState=" + contextStateId +
	    			", not " +transactions.size() + " matches");
	    }
	
	}
	
	}
