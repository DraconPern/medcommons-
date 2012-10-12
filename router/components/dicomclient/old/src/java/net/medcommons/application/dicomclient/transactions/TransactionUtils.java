package net.medcommons.application.dicomclient.transactions;

import java.util.List;

import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DicomOutputTransaction;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.LocalHibernateUtil;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.modules.services.interfaces.DicomMetadata;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

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

public static DicomOutputTransaction saveTransaction(DicomOutputTransaction transaction){
		if (!useDB) return(transaction);
		DicomOutputTransaction saved = null;
		Session session = LocalHibernateUtil.currentSession();

		session.beginTransaction();
		session.saveOrUpdate(transaction);
		saved = transaction;

		session.getTransaction().commit();
		LocalHibernateUtil.closeSession();

		return(saved);
	}
public static List<CxpTransaction> getCxpTransactionsWithStatus(String status){
	if (!useDB) return(null);
	Session session = LocalHibernateUtil.currentSession();
	Criteria matching = session.createCriteria(CxpTransaction.class);
    matching.add(Expression.eq("status", status));
    List<CxpTransaction> transactions = matching.list();
    return(transactions);
}

public static List<CxpTransaction> getCxpTransactionsWithDashboardStatusId(String dashboardStatusId){
	if (!useDB) return(null);
	Session session = LocalHibernateUtil.currentSession();
	Criteria matching = session.createCriteria(CxpTransaction.class);
    matching.add(Expression.eq("dashboardStatusId", dashboardStatusId));
    List<CxpTransaction> transactions = matching.list();
    return(transactions);
}

public static CxpTransaction getCxpTransactionWithId(Long id){
	if (!useDB) return(null);
	Session session = LocalHibernateUtil.currentSession();
	Criteria matching = session.createCriteria(CxpTransaction.class);
    matching.add(Expression.eq("id", id));
    List<CxpTransaction> transactions = matching.list();
    if (transactions == null) return null;
    if (transactions.size()== 0)
    	return(null);
    else if (transactions.size()==1){
    	return(transactions.get(0));
    }
    else{
    	throw new IllegalStateException("Expected a single match to CxpTransaction id=" + id  +
    			", not " +transactions.size() + " matches");
    }

}
public static CCRReference getCCRReference(String storageId, String guid){
	if (!useDB) return(null);
	Session session = LocalHibernateUtil.currentSession();
	Criteria matching = session.createCriteria(CCRReference.class);
    matching.add(Expression.eq("guid", guid));
    matching.add(Expression.eq("storageId", storageId));
    List<CCRReference> transactions = matching.list();
    if (transactions == null) return null;
    if (transactions.size()== 0)
    	return(null);
    else if (transactions.size()==1){
    	return(transactions.get(0));
    }
    else{
    	throw new IllegalStateException("Expected a single match to CCRReference storageId=" + storageId + ", guid=" + guid +
    			", not " +transactions.size() + " matches");
    }

}
public static List<CCRReference> getCCRReferences(){
	if (!useDB) return(null);
	Session session = LocalHibernateUtil.currentSession();
	Criteria matching = session.createCriteria(CCRReference.class);

    List<CCRReference> transactions = matching.list();
    return(transactions);

}

/**
 * Set status for transaction  specified by given dashboard id to 
 * cancelled so that the transfer is terminated.
 * 
 * @param dashboardStatusId     dashboard transfer key of transfer to terminate
 */
public static void cancelTransaction(String dashboardStatusId) {
    
    log.info("Cancelling transaction " + dashboardStatusId);
    
	if (!useDB) return;
	
	Session session = LocalHibernateUtil.currentSession();
	try { 
	    CxpTransaction tx =  (CxpTransaction) 
		    session.createQuery("from CxpTransaction tx where tx.dashboardStatusId = :id")
	               .setString("id", dashboardStatusId)
	               .uniqueResult();
	    if(tx == null)
	        throw new IllegalArgumentException("Invalid transfer key / dashboard status id" + dashboardStatusId 
						                      + " specified for cancel.  No transaction has this key.");
	    
	    tx.setStatus(CxpTransaction.STATUS_CANCELLED);
	    session.update(tx);
	    
	    StatusDisplayManager.getStatusDisplayManager().setErrorMessage(
	        "Transfer Cancelled", 
	        tx.getTransactionType()  + " for patient " + tx.getPatientName()  + " was cancelled" ,tx.getDashboardStatusId());
                        
	    log.info("Successfully cancelled transaction " + dashboardStatusId + " (cxp transaction " + tx.getId());
	}
	finally {
	    session.close();
	}
}

public static ContextState getContextState(Long contextStateId){
	if (!useDB) return(null);
	Session session = LocalHibernateUtil.currentSession();
	Criteria matching = session.createCriteria(ContextState.class);
    matching.add(Expression.eq("id", contextStateId));
   
    List<ContextState> transactions = matching.list();
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

/**
 * Returns an existing ContextState with a given auth and account id.
 * @param auth
 * @param accountId
 * @return
 */
public static ContextState getContextState(String storageId, String guid, String auth, String accountId, String groupAccountId ){
	if (!useDB) return(null);
	Session session = LocalHibernateUtil.currentSession();
	Criteria matching = session.createCriteria(ContextState.class);
    matching.add(Expression.eq("accountId", accountId));
    matching.add(Expression.eq("auth", auth));
    matching.add(Expression.eq("groupAccountId", groupAccountId));
    matching.add(Expression.eq("storageId", storageId));
    matching.add(Expression.eq("guid", guid));
    
   
    List<ContextState> transactions = matching.list();
    if (transactions == null) return null;
    if (transactions.size()== 0)
    	return(null);
    else if (transactions.size()==1){
    	return(transactions.get(0));
    }
    else{
    	throw new IllegalStateException("Expected a single match to ContextState auth=" + auth +
    			",accountId=" + accountId +
    			", not " +transactions.size() + " matches");
    }

}

public static DicomMetadata saveTransaction(DicomMetadata transaction){
	if (!useDB) return(transaction);
	DicomMetadata saved = null;
	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	session.saveOrUpdate(transaction);
	saved = transaction;

	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return(saved);
}

public static DicomTransaction saveTransaction(DicomTransaction transaction){
	if (!useDB) return(transaction);
	DicomTransaction saved = null;
	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	session.saveOrUpdate(transaction);
	saved = transaction;

	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return(saved);
}
public static CxpTransaction saveTransaction(CxpTransaction transaction){
	if (!useDB) return(transaction);
	CxpTransaction saved = null;
	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	session.saveOrUpdate(transaction);
	saved = transaction;

	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return(saved);
}

public static CCRReference saveTransaction(CCRReference transaction){
	if (!useDB) return(transaction);
	CCRReference saved = null;
	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	session.saveOrUpdate(transaction);
	saved = transaction;

	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return(saved);
}
public static PixIdentifierData saveTransaction(PixIdentifierData transaction){
	if (!useDB) return(transaction);
	PixIdentifierData saved = null;
	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	session.saveOrUpdate(transaction);
	saved = transaction;

	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return(saved);
}

public static PixDemographicData saveTransaction(PixDemographicData transaction){
	if (!useDB) return(transaction);
	PixDemographicData saved = null;
	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	session.saveOrUpdate(transaction);
	saved = transaction;

	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return(saved);
}
public static DownloadQueue saveTransaction(DownloadQueue transaction){
	if (!useDB) return(transaction);
	DownloadQueue saved = null;
	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	session.saveOrUpdate(transaction);
	saved = transaction;

	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return(saved);
}

public static ContextState saveTransaction(ContextState transaction){
    if (!useDB) return(transaction);
    ContextState saved = null;
    Session session = LocalHibernateUtil.currentSession();

    session.beginTransaction();
    session.saveOrUpdate(transaction);
    saved = transaction;

    session.getTransaction().commit();
    LocalHibernateUtil.closeSession();

    return(saved);
}

public static void delete(DicomOutputTransaction transaction){
	if (!useDB) return;

	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	session.saveOrUpdate(transaction);

	session.delete(transaction);
	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return;
}

public static void delete(CCRReference transaction){

	if (!useDB) return;
	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	session.saveOrUpdate(transaction);

	session.delete(transaction);
	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return;
}
public static void delete(DownloadQueue transaction){

	if (!useDB) return;
	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	session.saveOrUpdate(transaction);

	session.delete(transaction);
	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return;
}
public static void delete(CxpTransaction transaction){

	if (!useDB) return;
	Session session = LocalHibernateUtil.currentSession();

	session.beginTransaction();
	//session.saveOrUpdate(transaction);

	session.delete(transaction);
	session.getTransaction().commit();
	LocalHibernateUtil.closeSession();

	return;
}

public static void delete(ContextState transaction){

    if (!useDB) return;
    Session session = LocalHibernateUtil.currentSession();

    session.beginTransaction();
    //session.saveOrUpdate(transaction);

    session.delete(transaction);
    session.getTransaction().commit();
    LocalHibernateUtil.closeSession();

    return;
}


}
