package net.medcommons.application.dicomclient.http.action;

import java.util.List;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.http.utils.ResponseWrapper;
import net.medcommons.application.dicomclient.transactions.CCRReference;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.DownloadQueue;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
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

public class DatabaseDumpActionBean extends DDLActionBean {
	 private static Logger log = Logger.getLogger(DatabaseDumpActionBean.class);
	 public ContextManager getContextManager(){
	    	return(super.getContextManager());
	    }

	 @DefaultHandler
	public Resolution getPatientDemographics(){
		ResponseWrapper response = new ResponseWrapper();

		Session session = LocalHibernateUtil.currentSession();
		//session.beginTransaction();
		Criteria crit = session.createCriteria(PixDemographicData.class);
		List<PixDemographicData> rows = crit.list();
		log.debug("About to return " + rows.size() + " rows");

		LocalHibernateUtil.closeSession();
		response.setContents(rows);
		response.setMessage("OK");
		response.setStatus(ResponseWrapper.Status.OK);
		return(new JavaScriptResolution(response));
	}


		public Resolution getPatientIdentifiers(){
			ResponseWrapper response = new ResponseWrapper();

			Session session = LocalHibernateUtil.currentSession();
			//session.beginTransaction();
			Criteria crit = session.createCriteria(PixIdentifierData.class);
			List<PixIdentifierData> rows = crit.list();
			log.debug("About to return " + rows.size() + " rows");
			for (int i=0;i<rows.size();i++){
				rows.get(i).setCreationDate(null);//Throws javascript error
			}
			LocalHibernateUtil.closeSession();
			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
		}
		public Resolution getCCRReferences(){
			ResponseWrapper response = new ResponseWrapper();

			Session session = LocalHibernateUtil.currentSession();
			//session.beginTransaction();
			Criteria crit = session.createCriteria(CCRReference.class);
			List<CCRReference> rows = crit.list();

			log.debug("About to return " + rows.size() + " rows");

			LocalHibernateUtil.closeSession();
			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
		}
		public Resolution getDownloadQueue(){
			ResponseWrapper response = new ResponseWrapper();

			Session session = LocalHibernateUtil.currentSession();
			//session.beginTransaction();
			Criteria crit = session.createCriteria(DownloadQueue.class);
			List<DownloadQueue> rows = crit.list();
			log.debug("About to return " + rows.size() + " rows");

			LocalHibernateUtil.closeSession();
			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
		}
		public Resolution getCxpTransactions(){
			ResponseWrapper response = new ResponseWrapper();

			Session session = LocalHibernateUtil.currentSession();
			//session.beginTransaction();
			Criteria crit = session.createCriteria(CxpTransaction.class);
			List<CxpTransaction> rows = crit.list();
			log.debug("About to return " + rows.size() + " rows");

			LocalHibernateUtil.closeSession();
			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
		}

		public Resolution getDicomMetadata(){

				ResponseWrapper response = new ResponseWrapper();

				Session session = LocalHibernateUtil.currentSession();
				//session.beginTransaction();
				Criteria crit = session.createCriteria(DicomMetadata.class);
				List<DicomMetadata> rows = crit.list();
				
				int n = Math.min(10, rows.size());
				log.debug("getDicomMetadata:About to return " + n + " out of a possible " + 
						rows.size() + " rows");
				for (int i=0;i<n;i++){
					if (log.isDebugEnabled())
						log.debug("i=" + i + ", " + rows.get(i));
					DicomMetadata d = rows.get(i);
					d.setSeriesInstanceUid(null);
					d.setStudyInstanceUid(null);
					d.setFile(null);
				}
				rows = rows.subList(0, n);
				LocalHibernateUtil.closeSession();
				response.setContents(rows);
				response.setMessage("OK");
				response.setStatus(ResponseWrapper.Status.OK);
				return(new JavaScriptResolution(response));
		}
		public Resolution getDicomTransaction(){

			ResponseWrapper response = new ResponseWrapper();

			Session session = LocalHibernateUtil.currentSession();
			//session.beginTransaction();
			Criteria crit = session.createCriteria(DicomTransaction.class);
			List<DicomTransaction> rows = crit.list();
			log.debug("About to return " + rows.size() + " rows");

			LocalHibernateUtil.closeSession();
			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
	}
		public Resolution getContextState(){

			ResponseWrapper response = new ResponseWrapper();

			Session session = LocalHibernateUtil.currentSession();
			//session.beginTransaction();
			Criteria crit = session.createCriteria(ContextState.class);
			List<ContextState> rows = crit.list();
			log.debug("About to return " + rows.size() + " rows");

			LocalHibernateUtil.closeSession();
			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
	}
    public Resolution status() {
		log.info("status()");

        return new ForwardResolution("status.html");
    }
}
