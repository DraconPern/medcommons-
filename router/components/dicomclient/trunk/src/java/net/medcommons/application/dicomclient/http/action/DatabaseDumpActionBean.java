package net.medcommons.application.dicomclient.http.action;

import java.util.List;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.http.utils.ResponseWrapper;
import net.medcommons.application.dicomclient.transactions.CCRRef;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.DownloadQueue;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.sourceforge.pbeans.Store;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;

public class DatabaseDumpActionBean extends DDLActionBean {
	 private static Logger log = Logger.getLogger(DatabaseDumpActionBean.class);
	 
	 public ContextManager getContextManager() {
	    	return(super.getContextManager());
	}

	@DefaultHandler
	public Resolution getPatientDemographics() {
		ResponseWrapper response = new ResponseWrapper();

		Store s = DB.get();
		
		List<PixDemographicData> rows = s.select(PixDemographicData.class).all();
		
		log.debug("About to return " + rows.size() + " rows");

		response.setContents(rows);
		response.setMessage("OK");
		response.setStatus(ResponseWrapper.Status.OK);
		return(new JavaScriptResolution(response));
	}


		public Resolution getPatientIdentifiers(){
			ResponseWrapper response = new ResponseWrapper();
			
			Store s = DB.get();
			List<PixIdentifierData> rows = s.select(PixIdentifierData.class).all();
			log.debug("About to return " + rows.size() + " rows");
			for (int i=0;i<rows.size();i++){
				rows.get(i).setCreationDate(null);//Throws javascript error
			}
			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
		}
		
		public Resolution getCCRReferences(){
			ResponseWrapper response = new ResponseWrapper();

			Store s = DB.get();
			//session.beginTransaction();
			List<CCRRef> rows = s.select(CCRRef.class).all();

			log.debug("About to return " + rows.size() + " rows");

			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
		}
		public Resolution getDownloadQueue(){
			ResponseWrapper response = new ResponseWrapper();

			Store s = DB.get();
			
			List<DownloadQueue> rows = s.select(DownloadQueue.class).all();
			log.debug("About to return " + rows.size() + " rows");

			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
		}
		public Resolution getCxpTransactions(){
			ResponseWrapper response = new ResponseWrapper();

			Store s = DB.get();
			
			List<CxpTransaction> rows = s.select(CxpTransaction.class).all();
			log.debug("About to return " + rows.size() + " rows");

			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
		}

		public Resolution getDicomMetadata(){

		    Store s = DB.get();
		    ResponseWrapper response = new ResponseWrapper();
		    
		    List<DicomMetadata> rows = s.select(DicomMetadata.class).all();
		    
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
		    response.setContents(rows);
		    response.setMessage("OK");
		    response.setStatus(ResponseWrapper.Status.OK);
		    return(new JavaScriptResolution(response));
		}
		public Resolution getDicomTransaction(){

			ResponseWrapper response = new ResponseWrapper();

		    Store s = DB.get();
		    
			List<DicomTransaction> rows = s.select(DicomTransaction.class).all();
			
			log.debug("About to return " + rows.size() + " rows");

			response.setContents(rows);
			response.setMessage("OK");
			response.setStatus(ResponseWrapper.Status.OK);
			return(new JavaScriptResolution(response));
	}
		public Resolution getContextState(){

			ResponseWrapper response = new ResponseWrapper();

		    Store s = DB.get();
		    
			//session.beginTransaction();
			List<ContextState> rows = s.select(ContextState.class).all();
			log.debug("About to return " + rows.size() + " rows");

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
