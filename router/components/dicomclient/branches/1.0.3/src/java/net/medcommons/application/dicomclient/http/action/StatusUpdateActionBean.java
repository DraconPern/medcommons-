package net.medcommons.application.dicomclient.http.action;



import static net.medcommons.application.dicomclient.utils.ClearUtils.clearAll;
import static net.medcommons.application.dicomclient.utils.ClearUtils.clearCompleted;
import static net.medcommons.application.dicomclient.utils.Params.where;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.Job;
import net.medcommons.application.dicomclient.JobHandler;
import net.medcommons.application.dicomclient.http.utils.ResponseWrapper;
import net.medcommons.application.dicomclient.http.utils.Voucher;
import net.medcommons.application.dicomclient.transactions.CCRRef;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.DownloadQueue;
import net.medcommons.application.dicomclient.transactions.PatientMatch;
import net.medcommons.application.dicomclient.utils.*;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.services.interfaces.PatientDemographics;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.modules.utils.dicom.DicomNameParser;
import net.sourceforge.pbeans.Store;
import net.sourceforge.pbeans.data.ResultsIterator;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;

/**
 * Handles status updates from a web client.
 *
 * Updates are of several sorts:
 * <ul>
 * <li> Database updates. "ClearAllCompleted" jobs removes all completed jobs from
 * the database; ClearAll removes all jobs (and cancels jobs that are in progress).
 * <li> Retry &lt;jobId&gt; restarts a job whose state is ERROR or CANCELED.
 * <li> Cancel &ltjobId&gt; cancels an existing job.
 * <li> Delete &lt;jobId&gt; deletes a job. A job can only de deleted if it is not active.
 * </ul>
 * Each action generates a new StatusMessage in the context manager.
 * <P>
 * Need to use finally{} to make sure that session is closed.
 * @author mesozoic
 * 
 */
public class StatusUpdateActionBean extends DDLActionBean {

    public final static String COMMAND_CLEAR_COMPLETED = "CLEAR_COMPLETED";
    public final static String COMMAND_CLEAR_ALL = "CLEAR_ALL";
    public final static String COMMAND_RETRYJOB = "RETRY_JOB";
    public final static String COMMAND_CANCELJOB = "CANCEL_JOB";
    public final static String COMMAND_MERGE_TO_PENDING = "MERGE_TO_PENDING_QUEUE";
    public final static String COMMAND_DELETEJOB = "DELETE_JOB";
    public final static String COMMAND_DELETE_PENDING_JOB = "DELETE_PENDING_JOB";
    public final static String COMMAND_DEMOGRAPHICS = "CLEAR_DEMOGRAPHICS";
    public final static String COMMAND_CREATE_VOUCHER = "CREATE_VOUCHER_ACCOUNT";


    private static Logger log = Logger.getLogger(StatusUpdateActionBean.class);

    JobHandler jobHandler;
    
    public StatusUpdateActionBean() {
        jobHandler = JobHandler.JobHandlerFactory();
    }

    public ContextManager getContextManager(){
        return(super.getContextManager());
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @DefaultHandler
    public Resolution update() throws IOException{
        ResponseWrapper response = null;
        try{

         Enumeration<String> paramNames = getContext().getRequest().getParameterNames();

         CommandParameters parameters = new CommandParameters();

         while(paramNames.hasMoreElements()){
             String name = paramNames.nextElement();
             String values[] = getContext().getRequest().getParameterValues(name);
             String value = values[0]; // Just grab the first
             log.info("update:" + name + ":" + value);
             if (name.equalsIgnoreCase("command")){
                 parameters.command = value;
             }
             else if (name.equalsIgnoreCase("jobType")){
                 parameters.jobType = value;
             }
             else if (name.equalsIgnoreCase("jobId")){
                 parameters.jobId = new Long(value.trim());
             }
             else if (name.equalsIgnoreCase("dojo.preventCache")){
            	 ;//no-op
             }
             else if (name.equalsIgnoreCase("update")){
            	 ;//no-op
             }
             else{
                 log.info("Ignored parameter '" + name + "'=" + value);
             }

         }
         response = runCommand(parameters);
         StatusMessage message = new StatusMessage(StatusMessage.INFO, "Status update:" + parameters.toString() + ":" + response.getStatus());
         getContextManager().addMessage(message);

        }
        catch(Exception e){
        	log.error("Exception running update()", e);
        	response.setStatus(ResponseWrapper.Status.ERROR);
        	response.setMessage(e.getLocalizedMessage());
        	response.setContents(e.getLocalizedMessage());
        }
        //return new ForwardResolution("status.html");
         return new JavaScriptResolution(response, ContextState.class);

    }

    private boolean blank(String s){
        if (s==null) return true;
        if ("".equals(s)) return true;
        return(false);
    }
    private ResponseWrapper runCommand(CommandParameters parameters){
        log.info("Running " + parameters.toString());
        ResponseWrapper response = new ResponseWrapper();
        response.setStatus(ResponseWrapper.Status.ERROR);
        response.setMessage("Uninitialized");

        String command = parameters.command;
        String jobType = parameters.jobType;
        Long jobId = parameters.jobId;
        Class clazz = null;
        if (jobType != null){
            if (jobType.equalsIgnoreCase("CXP")){
                clazz = CxpTransaction.class;
            }
            else if (jobType.equalsIgnoreCase("DICOM")){
                clazz = DicomOutputTransaction.class;
            }
        }


        try{
            if (blank(command)){
                throw new NullPointerException("Null command");
            }
            else if (command.equalsIgnoreCase(COMMAND_CANCELJOB)){
                cancelJob(clazz,jobId);
                response.setStatus(ResponseWrapper.Status.OK);
                response.setMessage("OK");
                response.setContents("Action completed");
            }
            else if (command.equalsIgnoreCase(COMMAND_RETRYJOB)){
                retryJob(clazz,jobId);
                response.setStatus(ResponseWrapper.Status.OK);
                response.setMessage("OK");
                response.setContents("Action completed");
            }
            else if (command.equalsIgnoreCase(COMMAND_DELETEJOB)){
                deleteJob(clazz,jobId);
                response.setStatus(ResponseWrapper.Status.OK);
                response.setMessage("OK");
                response.setContents("Action completed");
            }
            else if (command.equalsIgnoreCase(COMMAND_MERGE_TO_PENDING)){
            	Status status = mergeToPendingQueueJob(clazz,jobId);
            	 response.setStatus(status.status);
                 response.setMessage(status.message);
                 response.setContents(status.contents);
            }
            else if (command.equalsIgnoreCase(COMMAND_CREATE_VOUCHER)){
            	Status status = createVoucherQueueJob(clazz,jobId);
            	 response.setStatus(status.status);
                 response.setMessage(status.message);
                 response.setContents(status.contents);
            }
            
            else if (command.equalsIgnoreCase(COMMAND_DELETE_PENDING_JOB)){
                deletePendingJob(clazz,jobId);
                response.setStatus(ResponseWrapper.Status.OK);
                response.setMessage("OK");
                response.setContents("Action completed");
            }
            else if (command.equalsIgnoreCase(COMMAND_CLEAR_COMPLETED)){

                log.info("Clearing all completed jobs ");

                clearCompleted(DicomTransaction.class);
                clearCompleted(DicomOutputTransaction.class);
                clearCompleted(CxpTransaction.class);
                response.setStatus(ResponseWrapper.Status.OK);
                response.setMessage("OK");
                response.setContents("Action completed");


                //getContextManager().clearMessages();
            }
            else if (command.equalsIgnoreCase(COMMAND_CLEAR_ALL)){

                log.info("Clearing all  jobs ");

                clearAll();
                //getContextManager().clearMessages();
                response.setStatus(ResponseWrapper.Status.OK);
                response.setMessage("OK");
                response.setContents("Action completed");
            }
            else if (command.equalsIgnoreCase(COMMAND_DEMOGRAPHICS)){
            	log.info("Clearing demographics ");
            	clearAll(PixIdentifierData.class);
                clearAll(PixDemographicData.class);
                PatientMatch.flushCache();
                response.setStatus(ResponseWrapper.Status.OK);
                response.setMessage("OK");
                response.setContents("Action completed");
            }


        }
        catch(Exception e){
        	log.error("Failed", e);
            response.setStatus(ResponseWrapper.Status.ERROR);
            response.setMessage("Failed:" + e.toString());
            response.setContents(e.toString());
            StatusDisplayManager sdm = StatusDisplayManager.get();
            sdm.setErrorMessage("Failed", e.toString());
            
        }

        return(response);
    }

    // For all the below - need to test status.
    // Can't delete an active job.
    // Can't cancel a job that isn't active.
    // Can't retry a job that is active.. &etc.
    public void cancel(DicomOutputTransaction transaction){
        log.info("About to cancel DICOM output transaction " + transaction );
        Store db = DB.get();
        Job currentTrans = jobHandler.getDicomJob(transaction.getId());
        if (currentTrans!=null){
        	currentTrans.cancelJob();
        }
        else{
        	 log.error("Attempt to cancel DICOM output " + transaction + ", but no active job has id " + transaction.getId());
        }
        transaction.setStatus(CxpTransaction.STATUS_CANCELLED);
        db.save(transaction);
        
        jobHandler.deleteDicomJob(transaction.getId());

    }
    public void cancel(CxpTransaction transaction){
        log.info("About to cancel CXP  transaction " + transaction );
        Job cxpJob = jobHandler.getCxpJob(transaction.getId());
        if (cxpJob != null){
            cxpJob.cancelJob();
        }
        else
            log.error("Attempt to cancel CXP session " + transaction + ", but no active job has id " + transaction.getId());
        
        Store db = DB.get();
        transaction.setStatus(CxpTransaction.STATUS_CANCELLED);
        db.save(transaction);
        log.info("CXP transaction with id " + transaction.getId() + " now has state " + transaction.getStatus());
        jobHandler.deleteCxpJob(transaction.getId());

    }
    private void delete(DicomOutputTransaction transaction){
        log.info("About to delete DICOM output transaction " + transaction );
        String status = transaction.getStatus();
        Store db = DB.get();
        if (!status.equals(CxpTransaction.STATUS_ACTIVE)){
            db.delete(transaction);
        }
        else{
            throw new IllegalStateException("Unable to delete job with status " + status);
        }
    }
    
    private void delete(CxpTransaction transaction){
        log.info("About to delete CXP  transaction " + transaction );
        String status = transaction.getStatus();
        Store db = DB.get();
        if(!status.equals(CxpTransaction.STATUS_ACTIVE)) {
            db.delete(transaction);
            
            ResultsIterator<DicomMetadata> metaIter = 
                db.select(DicomMetadata.class, where("cxpJob", transaction.getId()));
            
            try {
                while(metaIter.hasNext()) {
                    DicomMetadata m = metaIter.next();
                    db.delete(m);
                }
            }
            finally {
                metaIter.close();
            }
            String folderPath = transaction.getTransactionFolder();
            
            File transactionFolder = new File(folderPath);
            FileUtils.deleteDir(transactionFolder);
        }
        else{
            throw new IllegalStateException("Unable to delete job with status " + status);
        }
    }

    private void retry(DicomOutputTransaction transaction){
        log.info("About to retry DICOM output transaction " + transaction );
        transaction.setStatus(DicomOutputTransaction.STATUS_QUEUED);
        transaction.setStatusMessage("Restarted by user");
        DB.get().save(transaction);
    }
    
    private void retry(CxpTransaction transaction){
        log.info("About to retry CXP  transaction " + transaction );
        transaction.setStatus(CxpTransaction.STATUS_QUEUED);
        transaction.setStatusMessage("Restarted by user");
        DB.get().save(transaction);
    }
    
    private Object getJob(Class clazz, Long jobId) {
        
        if (clazz == null) throw new NullPointerException("jobType not specified: must be DICOM or CXP");
        Store db = DB.get();
        List<Object> objs = db.all(clazz, where("id",jobId));
        if (objs.size() != 0){
	        Object obj = objs.get(0);
	        return obj;
        }
    	throw new RuntimeException("Job id "+jobId+" for class "+clazz.getCanonicalName()+" does not exist");
    }
    
    public void cancelJob(Class clazz, Long jobId){
        Object obj = getJob(clazz, jobId);

        if (obj== null)
            throw new NullPointerException("No job with id " + jobId + " can be found for class " + clazz.getCanonicalName());
        if (obj instanceof DicomOutputTransaction) {
            DicomOutputTransaction job = (DicomOutputTransaction) obj;
            cancel(job);
        }
        else if (obj instanceof CxpTransaction) {
            CxpTransaction job = (CxpTransaction) obj;
            cancel(job);
        }
        else{
            throw new IllegalStateException("Unknown database type:" + obj);
        }

    }
    private void deleteJob(Class clazz, Long jobId){
        Object obj = getJob(clazz, jobId);
        if (obj== null)
            throw new NullPointerException("No job with id " + jobId + " can be found for class " + clazz.getCanonicalName());
        if (obj instanceof DicomOutputTransaction) {
            DicomOutputTransaction job = (DicomOutputTransaction) obj;
            delete(job);
        }
        else if (obj instanceof CxpTransaction) {
            CxpTransaction job = (CxpTransaction) obj;
            delete(job);
        }
        else{
            throw new IllegalStateException("Unknown database type:" + obj);
        }

    }
    private void retryJob(Class clazz, Long jobId){
        Object obj = getJob(clazz, jobId);
        if (obj== null)
            throw new NullPointerException("No job with id " + jobId + " can be found for class " + clazz.getCanonicalName());
        if (obj instanceof DicomOutputTransaction) {
            DicomOutputTransaction job = (DicomOutputTransaction) obj;
            retry(job);
        }
        else if (obj instanceof CxpTransaction) {
            CxpTransaction job = (CxpTransaction) obj;
            retry(job);
        }
        else{
            throw new IllegalStateException("Unknown database type:" + obj);
        }

    }
    
    private Status createVoucherQueueJob(Class clazz, Long jobId) throws Exception{
    	Status status = new Status();
    	status.status = ResponseWrapper.Status.OK;
    	status.message = "OK";
    	status.contents = "Action completed";
    	StatusDisplayManager sdm = StatusDisplayManager.get();

    	Object obj = getJob(clazz, jobId);
    	if (obj instanceof CxpTransaction){
    		CxpTransaction transaction = (CxpTransaction) obj;
    		

    		log.info("Request to generate voucher get medcommons id of created account :" + transaction.getPatientName());

    		ContextState contextState = ContextManager.get().getCurrentContextState();
    		DicomNameParser parser = new DicomNameParser();
    		String service = "DICOM+Upload";
    
    		String patientGivenName = parser.givenName(transaction.getPatientName());
    		String patientFamilyName = parser.familyName(transaction.getPatientName());
    		
    		Store db = DB.get();
    		DicomMetadata metaData = db.selectSingle(DicomMetadata.class, "cxpJob", transaction.getId());
    		
    		String patientSex = (metaData == null ? null : metaData.getPatientSex());
    		
    		// Create voucher if there is enough context to create one
    		if (Voucher.contextComplete(contextState)){
	    		Voucher voucher = new Voucher(contextState, patientGivenName, patientFamilyName,patientSex,service);
	    		voucher.createVoucher();
	    		log.info("Voucher returned new medcommons id " + voucher.getPatientMedCommonsId());
    		
    			ContextState uploadContextState = voucher.createDocumentUploadContextState(contextState);
    			log.info("Document upload context state is " + contextState);
    			
    			db.requestLock(transaction);
    			try {
    			    Long contextStateId = uploadContextState.getId();
    			    transaction.setContextStateId(contextStateId);
    			    transaction.setStatus(CxpTransaction.STATUS_QUEUED);
    			    transaction.setStatusMessage("Uploading to voucher-created account");
    			    db.save(transaction);
    			}
    			finally {
    			    db.relinquishLock();
    			}
	    		
	    		if(sdm != null) {
    				sdm.setMessage("Voucher created", "Voucher created for " + transaction.getPatientName());
    			}

    		}
    		else{
    		
    			if (sdm != null){
    				sdm.setErrorMessage("Voucher could not be created", "User must authenticate");
    			}
    		}
    		

    	}
    	else{
    		throw new IllegalArgumentException("Unexpected class " + clazz  +", " + obj);
    	}
    	return(status);

    }

    /**
     * Sets the status to be queued and the storage id to be "-1". An account will
     * be created.
     * @param clazz
     * @param jobId
     */
    private Status mergeToPendingQueueJob(Class clazz, Long jobId){
        
    	Status status = new Status();
    	status.status = ResponseWrapper.Status.OK;
    	status.message = "OK";
    	status.contents = "Action completed";
    	StatusDisplayManager sdm = StatusDisplayManager.get();
    	Store db = DB.get();

    	Object obj = getJob(clazz, jobId);
    	if (obj instanceof CxpTransaction) {
    		CxpTransaction transaction = (CxpTransaction) obj;
    		
    		db.requestLock(transaction);
    		try {
	    		Configurations configurations = ContextManager.get().getConfigurations();
	    		
	
	    		log.info("Need to get account information to merge in DICOM from :" + transaction.getPatientName());
	  /*  		if ((transaction.getSenderId() == null) || (transaction.getSenderId().equals(""))){
	
	                  if ((senderAccountId== null) || ("".equals(senderAccountId))){
	                	  status.status = ResponseWrapper.Status.ERROR;
	                	  status.message = "Not logged in";
	                	  status.contents = "Must refresh dashboard or log into account";
	                  }
	                  else{
	                	  transaction.setSenderId(senderAccountId);
	                	  transaction.setCxpEndpoint(cxpEndpoint);
	                  }
	    		}
	    		*/ 
	    		
	    		if (status.status == ResponseWrapper.Status.OK){
	    			List<CCRRef> references = db.select(CCRRef.class).all();
	    			if ((references == null) || (references.size() == 0)){
	    				status.status = ResponseWrapper.Status.ERROR;
	              	  	status.message = "No pending references to merge into";
	              	  	status.contents = "Must select 'Add DICOM' from existing account and then reselect this row";
	    			}
	
	    			else if (references.size()>1){
	    				status.status = ResponseWrapper.Status.ERROR;
	              	  	status.message = "Too many pending references";
	              	  	status.contents = "There should be a single matching reference, not " + references.size() +
	              	  	"\n Clear transactions and start again";
	    			}
	    			else{
	    				CCRRef ref = references.get(0);
	    				PixDemographicData pixDemographicData = PatientMatch.getPatient(DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN, ref.getStorageId());
	    				String originalPatientName = transaction.getPatientName();
	    				if ((pixDemographicData.getFamilyName() == null) && (pixDemographicData.getGivenName() == null)){
	    					transaction.setPatientName(originalPatientName + "\n(CCR patient name was blank; replacing with DICOM name");
	    				}
	    				else{
		    				String patientName = pixDemographicData.getFamilyName() + ", " + pixDemographicData.getGivenName();
		    				
		    				transaction.setPatientName(patientName + " \n(was " + originalPatientName + ")");
	    				}
	    				
	    				Long contextStateId = ref.getContextStateId();
	    				ContextState contextState = db.selectSingle(ContextState.class, "id", contextStateId);
	    	    		if (contextState == null){
	    	    			throw new NullPointerException("Non context state linked to by pending CCR:" + ref);
	    	    		}
	    	    		transaction.setContextStateId(contextStateId);
	    	            
	    	            String gatewayRoot = contextState.getGatewayRoot();
	    	          
	    	            if (gatewayRoot == null){
	    	            	throw new NullPointerException("Null gateway root from " + contextState);
	    	            }
	    				contextState.setStorageId(ref.getStorageId());
	    	    		transaction.setStatus(CxpTransaction.STATUS_QUEUED);
	    	    		transaction.setStatusMessage("Merging to existing account");
	    	    		db.save(transaction);
	    			}
	
	    		}
	    		if (status.status.equals(ResponseWrapper.Status.ERROR)){
	    			if (sdm != null){
	    				sdm.setErrorMessage(status.message, status.contents);
	    			}
	    		}
	    		else{
	    			if (sdm != null){
	    				sdm.setMessage("Merge request arrived", "Merge request for " + transaction.getPatientName());
	    			}
	    		}
    		}
    		finally {
    		    db.relinquishLock();
    		}
	
    	}
    	else 
    		throw new IllegalArgumentException("Unexpected class " + clazz  +", " + obj);
    	return(status);

    }

    private void deletePendingJob(Class clazz, Long jobId){
    	  Object obj = getJob(clazz, jobId);
          if (obj== null)
              throw new NullPointerException("No job with id " + jobId + " can be found for class " + clazz.getCanonicalName());
          if (obj instanceof CxpTransaction) {
              CxpTransaction job = (CxpTransaction) obj;
              String jobStatus = job.getStatus();
              if (!CxpTransaction.STATUS_WAIT_PENDING_MATCH.equals(jobStatus)){

              }
              delete(job);
          }
          else{
              throw new IllegalStateException("Unknown database type:" + obj);
          }

    }
    /**

        Criteria crit = session.createCriteria(DicomOutputTransaction.class);
        //crit.add(Expression.eq("completed", false));
        List<DicomTransaction> transactions = crit.list();
        //log.info("About to return " + transactions.size() + " outgoing DICOM transactions");
        LocalHibernateUtil.closeSession();
        response.setContents(transactions);
     */

    private class CommandParameters{
        String command = null;
        String jobType = null;
        Long jobId = new Long(Long.MIN_VALUE);

        public String toString(){
            if (jobType == null)
                return("Command=" + command);
            else if (jobId.longValue()== Long.MIN_VALUE)
                return("Command=" + command + ", jobType=" + jobType);
            else
                return("Command=" + command + ", jobType=" + jobType + ", jobId=" + jobId);

        }
    }
    private class Status{
    	ResponseWrapper.Status status;
    	String message;
    	String contents;
    }
}
