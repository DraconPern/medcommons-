package net.medcommons.application.dicomclient;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.CxpDocument;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DicomOutputTransaction;
import net.medcommons.application.dicomclient.utils.ManagedTransaction;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.upload.StatusUpdate;
import net.medcommons.application.utils.MonitorTransfer;
import net.medcommons.client.utils.CCRDocumentUtils;
import net.medcommons.modules.transfer.DownloadFileAgent;
import net.medcommons.modules.utils.FileUtils;

import org.apache.log4j.Logger;
import org.cxp2.Document;
import org.cxp2.GetResponse;

import astmOrgCCR.ContinuityOfCareRecordDocument;

/**
 * Reads the CCR from the disk; then downloads references 
 * and passes them onto the next process (output to DICOM or File).
 * Creates a {@link MonitorTransfer} object that monitors the
 * transfer.
 * 
 * @author mesozoic
 *
 */
public class DownloadCxpJob implements Runnable,Job {
	CxpTransaction cxpTransaction = null;
	DicomOutputTransaction outputTransaction = null;
	DownloadFileAgent downloadFileAgent = null;
	 private static Logger log = Logger.getLogger(DownloadCxpJob.class
	            .getName());

	// create DicomJob class to handle DICOM transactions.
	// Create fileJob class ot handle FIle export.
	// Dicom/File jobs  don't need to be synchronized?
	File transactionFolder;
	JobHandler jobHandler = null;
	Long id = null;
 

	public DownloadCxpJob(CxpTransaction cxpTransaction, DicomOutputTransaction outputTransaction){
		this.cxpTransaction = cxpTransaction;
		if (cxpTransaction == null){
			throw new NullPointerException("Null cxpTransaction");
		}
		if (cxpTransaction.getTransactionFolder() == null){
			throw new NullPointerException("Null transaction folder in " + cxpTransaction);
		}
		if (outputTransaction == null){
			throw new NullPointerException("outputTransaction is null" + cxpTransaction);
		}


		this.transactionFolder = new File(cxpTransaction.getTransactionFolder());
		this.outputTransaction = outputTransaction;
		this.id = cxpTransaction.getId();
		
		log.info("Created DownloadCxpJob for CxpTransaction with id =" + this.id);
		log.info("CCR file for this CxpTransaction is " + cxpTransaction.getCcrFilename());

	}
	public Long getId(){
		return(this.id);
	}
	public void run(){

		jobHandler = JobHandler.JobHandlerFactory();
		cxpTransaction.setStatus(CxpTransaction.STATUS_ACTIVE);
		cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
		Long contextStateId = cxpTransaction.getContextStateId();
		final ContextManager contextManager = ContextManager.getContextManager();
		ContextState contextState = TransactionUtils.getContextState(contextStateId);
		StatusUpdate statusUpdate = contextManager.getStatusUpdate();
		MonitorTransfer monitorTransfer = null;
		if (contextState == null){
			throw new NullPointerException("ContextState is null in " + cxpTransaction);
		}
		String guids[] =null;

		List<String> referenceGuids = new ArrayList<String>();
		CCRDocumentUtils ccrUtils = new CCRDocumentUtils();
		StatusDisplayManager sdm = StatusDisplayManager.getStatusDisplayManager();
		ContinuityOfCareRecordDocument currentCCR = null;
		List<Document> documents;
		List<File> dicomFiles = new ArrayList<File>();
		GetResponse resp;
		// Get the CCR (parse)
		//
		String ccrFilename = cxpTransaction.getCcrFilename();
		if (ccrFilename == null){
			throw new NullPointerException("ccrFilename for cxpTransaction " + cxpTransaction.getId() + " is null");
		}

		File f = new File(cxpTransaction.getCcrFilename());
		if (!f.exists()){
			String message = "CCR file not found:"  + f.getAbsolutePath();
			log.error(message);
			cxpTransaction.setStatus(CxpTransaction.STATUS_PERMANENT_ERROR);
			cxpTransaction.setStatusMessage(message);
			cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
			sdm.setErrorMessage("Error starting download", message, cxpTransaction.getDashboardStatusId());
			return;
		}
		try{
			currentCCR = CCRDocumentUtils.parseAndCheckSchemaValidation(f);
		}
		catch(Exception e){
			String message = "Error parsing CCR:"  + f.getAbsolutePath();
			log.error(message, e);
			cxpTransaction.setStatus(CxpTransaction.STATUS_PERMANENT_ERROR);
			cxpTransaction.setStatusMessage(message);
			cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
			sdm.setErrorMessage("Error parsing CCR", message);
			return;

		}
		log.info("Downloaded CCR\n" + currentCCR.xmlText());
        List<CxpDocument> referencedDocuments = ccrUtils.getReferencedDocuments(currentCCR);
        log.info("About to download references for CCR - there are " + referencedDocuments.size() + " referenced documents in ccr");
        for (int i = 0; i < referencedDocuments.size(); i++) {
        	CxpDocument aDocument = referencedDocuments.get(i);
        	//if ("application/dicom".equals(aDocument.getContentType())){
        		// Currently - only download DICOM documents.
	            cxpTransaction.setTotalBytes(referencedDocuments.get(i).getSize() + cxpTransaction.getTotalBytes());
	            log.info("set cxpTransaction total bytes to " + cxpTransaction.getTotalBytes() + " doctype " + aDocument.getContentType());
	            referenceGuids.add(referencedDocuments.get(i)
	                    .getGuid());
        	//}
        }
        cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
        log.info("Number of documents to be downloaded:" + referenceGuids.size());
        if (referenceGuids.size() > 0) {
            guids = new String[referenceGuids.size()];
            for (int i = 0; i < guids.length; i++) {
                guids[i] = referenceGuids.get(i);

            }

            try{
            	log.info("Download agent - set to " + contextState.getCXPEndpoint() + " for senderid " + contextState.getAccountId());
	            downloadFileAgent = new DownloadFileAgent(contextState.getCXPEndpoint(),
	            		contextState.getStorageId(), contextState.getAccountId(), guids, transactionFolder);
            }
            catch(Exception e){

            	String message = generateCxpDocumentString("Error initializing downloads for account " +  
            			contextState.getStorageId(),
            			guids);
    			log.error(message, e);
    			cxpTransaction.setStatus(CxpTransaction.STATUS_PERMANENT_ERROR);
    			cxpTransaction.setStatusMessage(message);
    			cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
    			sdm.setErrorMessage("Error downloading documents", message, cxpTransaction.getDashboardStatusId());
    			return;
            }
            //xpTransaction.setTotalBytes(referenceGuids.size());
            log.info("All referenced documents - total bytes to download = " + cxpTransaction.getTotalBytes() );
            CxpTransferMonitor monitor = new CxpTransferMonitor(downloadFileAgent,
                    "Download ",  cxpTransaction);
            cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
            Thread t = new Thread(monitor);
            t.start();
            
            if(statusUpdate != null){
            	downloadFileAgent.setTotalBytes(cxpTransaction.getTotalBytes());
				monitorTransfer = new MonitorTransfer(this, downloadFileAgent, statusUpdate, contextState);
				cxpTransaction.setDashboardStatusId(monitorTransfer.getTransferKey());
				cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
				new Thread(monitorTransfer).start();
			}
            downloadFileAgent.startTransactionTimer();
            try{
            	resp = downloadFileAgent.download();
            	int status = resp.getStatus();
            	if (status < 299){
            		if (monitorTransfer != null)
            			monitorTransfer.setSuccess(true);
            	}
            }
            catch(Exception e){
            	boolean cancelled = FileUtils.isStreamCancelled(e);

            	 if (cancelled){
            		 String message = "Download of " + cxpTransaction.getPatientName() + ":" + cxpTransaction.getDisplayName() +
                	 " was cancelled by user";
            		 log.info(message);
            		 cxpTransaction.setStatus(CxpTransaction.STATUS_CANCELLED);
            		 cxpTransaction.setStatusMessage("Download cancelled by user");
            		 cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
 	    			  sdm.setErrorMessage("Download cancelled", message, cxpTransaction.getDashboardStatusId());
            		 return;
            	 }
            	 else{
	            	String message = generateCxpDocumentString("Error downloading documents for account " + contextState.getStorageId(),
	            			guids);
	    			log.error(message, e);
	    			cxpTransaction.setStatus(CxpTransaction.STATUS_PERMANENT_ERROR);
	    			cxpTransaction.setStatusMessage(message);
	    			cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
	    			sdm.setErrorMessage("Error downloading documents", message,cxpTransaction.getDashboardStatusId());
	    			return;
            	 }
            }
            finally{
            	if (monitorTransfer != null){
            		monitorTransfer.exit();
            	}
            }
            int status = resp.getStatus();
            String reason = resp.getReason();
            log.info("Retrieval of referenced documents status="
                    + status + ", reason=" + reason);
            if (status < 299) {
         
                documents = resp.getDocinfo();
                StatusDisplayManager.getStatusDisplayManager()
                        .setMessage(
                                "Image Download Complete",
                                "Received " + documents.size()
                                        + " images ");
                cxpTransaction.setStatus(CxpTransaction.STATUS_COMPLETE);
                cxpTransaction.setElapsedTime(System.currentTimeMillis() - cxpTransaction.getTimeStarted());
                cxpTransaction.setTotalImages(documents.size());
                cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
              
            } else {
            	cxpTransaction.setStatus(CxpTransaction.STATUS_PERMANENT_ERROR);
    			cxpTransaction.setStatusMessage(reason);
    			cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
    			cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
                StatusDisplayManager
                        .getStatusDisplayManager()
                        .setErrorMessage("Download Failure", reason, cxpTransaction.getDashboardStatusId());
                return;
            }

            outputTransaction.setStatus(ManagedTransaction.STATUS_QUEUED);
            outputTransaction.setRetryCount(0);
            outputTransaction.setPatientName(cxpTransaction.getPatientName());
            outputTransaction.setStudyDescription(cxpTransaction.getDisplayName());
            String defaultPatientId[] = ccrUtils.getDefaultPatientIdType(currentCCR);
            outputTransaction.setPatientId(defaultPatientId[0]);
            outputTransaction.setPatientIdType(defaultPatientId[1]);
            outputTransaction.setDashboardStatusId(cxpTransaction.getDashboardStatusId());





            Iterator<Document> iter = documents.iterator();
            while (iter.hasNext()) {
                Document doc = iter.next();
                printResponseDocument(doc);
                if (doc.getContentType()
                        .equals("application/dicom")) {
                    File imageFile = new File(doc.getDocumentName());


                    if (imageFile.exists()) {


                        dicomFiles.add(imageFile);
                        //log.info("Adding image file:" + imageFile.getAbsolutePath());
                        outputTransaction.incrementTotalBytes(imageFile.length());
                        outputTransaction.incrementObjectCount();



                    } else {
                        log.error("DICOM file "
                                + doc.getDocumentName()
                                + " does not exist");
                    }

                }

            }

        }
        else{
        	String message = "No downloadable references in this CCR";
        	sdm.setErrorMessage("Error starting download", message);
        	log.error(message);
        }
        outputTransaction.setExportFolder(cxpTransaction.getExportFolder());
        outputTransaction.setTransactionFolder(transactionFolder.getAbsolutePath());


        outputTransaction = TransactionUtils.saveTransaction(outputTransaction);


	}
	// Get CCR into memory
	// Start download.
	// Note that at this point - the transaction can be cancelled/retried later.


	    private String generateCxpDocumentString(String caption, String guids[]){
	    	StringBuffer buff = new StringBuffer(caption);
        	buff.append(":\n");
        	buff.append("{");
        	for (int i=0;i<guids.length;i++){
        		if (i > 0) buff.append(",");
        		buff.append(guids[i]);
        	}
        	buff.append("}");
        	String message = buff.toString();
        	return(message);
	    }

	    public void cancelJob(){
	    	if (downloadFileAgent != null){
	    		if (cxpTransaction.getStatus().equals(CxpTransaction.STATUS_ACTIVE)){
	    			downloadFileAgent.cancelStream();
	    		}
	    	}
	    }
	    
	    private void printResponseDocument(Document doc){
	    	log.info("Document: contentType =" + doc.getContentType() + ", Description=" + doc.getDescription() + 
	    			", DocumentName=" + doc.getDocumentName() + ", guid=" +
	    			doc.getGuid() + ", ParentName" + doc.getParentName() + ", sha1" + doc.getSha1());
	    }
}

