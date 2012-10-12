package net.medcommons.application.dicomclient.http.action;

import java.util.ArrayList;
import java.util.List;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.http.utils.ResponseWrapper;
import net.medcommons.application.dicomclient.transactions.CCRPatientReference;
import net.medcommons.application.dicomclient.transactions.CCRReference;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.PatientMatch;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DDLTypes;
import net.medcommons.application.dicomclient.utils.DicomOutputTransaction;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.LocalHibernateUtil;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Restrictions;

public class StatusActionBean extends DDLActionBean {
	 private static Logger log = Logger.getLogger(StatusActionBean.class);
	 public ContextManager getContextManager(){
	    	return(super.getContextManager());
	    }
 
	 @DefaultHandler
	public Resolution getCxpUploads(){
		ResponseWrapper response = new ResponseWrapper();

		Session session = LocalHibernateUtil.currentSession();
		//session.beginTransaction();
		Criteria crit = session.createCriteria(CxpTransaction.class);
		crit = session.createCriteria(CxpTransaction.class);
		crit.add(Expression.eq("transactionType", CxpTransaction.TRANSACTION_PUT));
		List<CxpTransaction> transactions = crit.list();
		log.debug("About to return " + transactions.size() + " uploads");
		for (int i=0;i<transactions.size();i++){
			CxpTransaction trans = transactions.get(i);
			log.debug("transaction " + i + ":" + trans.toString());
		}
		LocalHibernateUtil.closeSession();
		response.setContents(transactions);
		response.setMessage("OK");
		response.setStatus(ResponseWrapper.Status.OK);
		return(new JavaScriptResolution(response, ContextState.class));
	}

	public Resolution	getCxpDownloads(){
		ResponseWrapper response = new ResponseWrapper();
		Session session = LocalHibernateUtil.currentSession();
		//session.beginTransaction();
		Criteria crit = session.createCriteria(CxpTransaction.class);
		crit = session.createCriteria(CxpTransaction.class);
		crit.add(Expression.eq("transactionType", CxpTransaction.TRANSACTION_GET));
		crit.add(Expression.ne("status", CxpTransaction.STATUS_ADD_DICOM));
		List<CxpTransaction> transactions = crit.list();
		//log.info("About to return " + transactions.size() + " downloads");
		LocalHibernateUtil.closeSession();
		response.setContents(transactions);
		response.setMessage("OK");
		response.setStatus(ResponseWrapper.Status.OK);
		return(new JavaScriptResolution(response, ContextState.class));

	}

	/**
	 * Ephemeral DICOM input. Disappears as soon as transaction is completed.
	 * @return
	 */
	public Resolution	getDicomSCP(){
		ResponseWrapper response = new ResponseWrapper();
		Session session = LocalHibernateUtil.currentSession();
		//session.beginTransaction();
		Criteria crit = session.createCriteria(DicomTransaction.class);
		Criterion isActive = Expression.eq("status", DicomTransaction.STATUS_ACTIVE);
		Criterion isCompleted = Expression.eq("status", DicomTransaction.STATUS_COMPLETE);
		LogicalExpression orExp = Restrictions.or(isActive, isCompleted);
		crit.add(orExp);
		List<DicomTransaction> transactions = crit.list();
		//log.info("About to return " + transactions.size() + " incoming DICOM transactions");
		LocalHibernateUtil.closeSession();
		response.setContents(transactions);
		response.setMessage("OK");
		response.setStatus(ResponseWrapper.Status.OK);
		return(new JavaScriptResolution(response, ContextState.class));

	}
	/**
	 * Queued output for DICOM being sent to a third party device.
	 * @return
	 */
	public Resolution	getDicomSCU(){
		ResponseWrapper response = new ResponseWrapper();
		Session session = LocalHibernateUtil.currentSession();
		//session.beginTransaction();
		Criteria crit = session.createCriteria(DicomOutputTransaction.class);



		List<DicomTransaction> transactions = crit.list();
		//log.info("About to return " + transactions.size() + " outgoing DICOM transactions");
		LocalHibernateUtil.closeSession();
		response.setContents(transactions);
		response.setMessage("OK");
		response.setStatus(ResponseWrapper.Status.OK);
		return(new JavaScriptResolution(response, ContextState.class));
	}

	public Resolution getCCRReferences(){
		ResponseWrapper response = new ResponseWrapper();

		try{
		
			Session session = LocalHibernateUtil.currentSession();
			//session.beginTransaction();
			Criteria crit = null;
			crit = session.createCriteria(CCRReference.class);
	
			List<CCRReference> transactions = crit.list(); // Get all of them.
			log.debug("About to return " + transactions.size() + " ccr references");
			for (int i=0;i<transactions.size();i++){
				CCRReference trans = transactions.get(i);
				log.debug("ccr reference " + i + ":" + trans.toString());
			}
			LocalHibernateUtil.closeSession();
			List<CCRPatientReference> ccrReferences = new ArrayList<CCRPatientReference>();
			for (int i=0;i<transactions.size();i++){
				CCRReference ref = transactions.get(i);
				// There has to be a more efficent way to do this.
				PixDemographicData pixDemographicData = PatientMatch.getPatient(DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN, ref.getStorageId());
				if (pixDemographicData != null){
					PixIdentifierData pixIdentifierData = PatientMatch.getIdentifier(pixDemographicData.getId(), DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN);
					if (pixIdentifierData != null){
						ccrReferences.add(new CCRPatientReference(ref, pixDemographicData, pixIdentifierData));
					}
				}
			}
	
			response.setContents(ccrReferences);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			
		}
		catch(Exception e){
			log.error("error getting CCR references", e);
			response.setContents(e.getMessage());
			response.setMessage("ERROR");
			response.setStatus(ResponseWrapper.Status.ERROR);
		}
		finally{
			return(new JavaScriptResolution(response, ContextState.class));
		}
	}
	public Resolution getAllCxpTransactions(){
		ResponseWrapper response = new ResponseWrapper();

		Session session = LocalHibernateUtil.currentSession();
		//session.beginTransaction();
		Criteria crit = session.createCriteria(CxpTransaction.class);
		crit = session.createCriteria(CxpTransaction.class);


		List<CxpTransaction> transactions = crit.list();
		log.debug("About to return " + transactions.size() + " transactions");
		for (int i=0;i<transactions.size();i++){
			CxpTransaction trans = transactions.get(i);
			log.debug("transaction " + i + ":" + trans.toString());
		}
		LocalHibernateUtil.closeSession();
		response.setContents(transactions);
		response.setMessage("OK");
		response.setStatus(ResponseWrapper.Status.OK);
		return(new JavaScriptResolution(response, ContextState.class));
	}
	public Resolution getAllDicomMetadata(){

		ResponseWrapper response = new ResponseWrapper();
		try{
			Session session = LocalHibernateUtil.currentSession();
			//session.beginTransaction();
			Criteria crit = session.createCriteria(DicomMetadata.class);


			List<DicomMetadata> metadata = crit.list();
			List<DicomMetadata> savedMetadata = new ArrayList<DicomMetadata>();
			log.info("About to return " + metadata.size() + " DICOM metadata entries");

			for (int i=0;i<metadata.size();i++){
				DicomMetadata m = metadata.get(i);
				savedMetadata.add(m);
				log.info("metadata " + i + ":" + m.toString());
			}

			LocalHibernateUtil.closeSession();
			response.setContents(savedMetadata);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
		}
		catch(Error e){
			response.setStatus(ResponseWrapper.Status.ERROR);
			response.setMessage(e.getLocalizedMessage());
			response.setContents("ERROR");


		}
		return(new JavaScriptResolution(response, ContextState.class));
	}


    public Resolution status() {
		log.info("status()");

        return new ForwardResolution("status.html");
    }


}
