package net.medcommons.application.dicomclient;

import static net.medcommons.application.dicomclient.utils.Params.where;
import static net.medcommons.modules.utils.Str.blank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import net.medcommons.application.dicomclient.transactions.CCRMatch;
import net.medcommons.application.dicomclient.transactions.CCRRef;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.PatientMatch;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.utils.DDLTypes;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.DirectoryUtils;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.upload.StatusUpdate;
import net.medcommons.application.utils.MonitorTransfer;
import net.medcommons.client.utils.CCRDocumentUtils;
import net.medcommons.client.utils.CCRGenerator;
import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.cxp.client.CXPClient;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.transfer.UploadFileAgent;
import net.medcommons.modules.utils.FileUtils;
import net.sourceforge.pbeans.Store;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.cxp2.Document;
import org.cxp2.Parameter;
import org.cxp2.PutRequest;
import org.cxp2.PutResponse;
import org.cxp2.RegistryParameters;

import astmOrgCCR.ContinuityOfCareRecordDocument;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.Actors;

/**
 * TODO: What needs to be saved here? Do we save ContextState or is that read-only? 
 * @author mesozoic
 *
 */
public class UploadCxpJob implements Runnable, Job {
    
	CxpTransaction cxpTransaction = null;

	Long id = null;

	UploadFileAgent uploadFileAgent = null;
	//String auth = null;

	File cacheFolder;
	ContextManager contextManager = ContextManager.get();

    protected PixDataExtractor pixDataExtractor = new PixDataExtractor();
    
	private static Logger log = Logger.getLogger(UploadCxpJob.class.getName());

	public UploadCxpJob(File cacheFolder, CxpTransaction cxpTransaction) {
		this.cxpTransaction = cxpTransaction;
		this.id = cxpTransaction.getId();
		this.cacheFolder = cacheFolder;
		
	}

	public Long getId() {
		return (this.id);
	}

	public void cancelJob() {
		if (uploadFileAgent != null) {
			if (cxpTransaction.getStatus().equals(CxpTransaction.STATUS_ACTIVE)) {
				uploadFileAgent.cancelStream();
			}
		}
	}

	public void run() {

		final ContextManager contextManager = ContextManager.get();
		Configurations configurations = contextManager.getConfigurations();
		String transactionFolder = cxpTransaction.getTransactionFolder();
		StatusUpdate statusUpdate = contextManager.getStatusUpdate();

		CxpTransferMonitor monitor = null;
		MonitorTransfer monitorTransfer  = null;
		String storageId = null;
		StatusDisplayManager sdm = StatusDisplayManager.get();
		boolean unknownMedCommonsAccount = true;
		Long contextStateId = cxpTransaction.getContextStateId();
		ContextState contextState = TransactionUtils.getContextState(contextStateId);
		if (contextState == null) {
		    // Should not really get here, but if we do, make sure this 
		    // transaction doesn't keep on getting re-scheduled - it needs
		    // to be matched to a patient.
            cxpTransaction.setStatus(CxpTransaction.STATUS_WAIT_PENDING_MATCH);
            DB.get().save(cxpTransaction);
		    throw new IllegalStateException("Upload job " + cxpTransaction.getId() +
		            " has not been resolved to a patient account.  Please resolve this patient manually");
		}
		
		if (contextState.getGatewayRoot() == null){
		    throw new NullPointerException("Missing gateway root");
		}
		
		try {
			if (transactionFolder == null) {
				throw new NullPointerException(
						"Null transaction folder in queued job "
								+ cxpTransaction);
			}
			File transactionDirectory = new File(cxpTransaction
					.getTransactionFolder());
			if (!transactionDirectory.exists())
				throw new FileNotFoundException(transactionDirectory
						.getAbsolutePath());

			// Get metadata (not used until CCR is needed below - but if there
			// is an error
			// here because of missing data because information was deleted
			// while this job was
			// in the queue - let's error out before we do any heavy lifting).
			Store db = DB.get();

			List<DicomTransaction> transactions = 
			    db.all(DicomTransaction.class, where("cxpJob", cxpTransaction.getId()));

			List<DicomMetadata> metadata = 
			        db.all(DicomMetadata.class, 
		               where("studyInstanceUid", cxpTransaction.getStudyInstanceUid())
		                .and("transactionStatus", DicomMetadata.STATUS_READY_TO_DELETE));
		              
			DicomMetadata dicomMetadata = null;
			if(metadata.size() > 0) {
				dicomMetadata = metadata.get(0);
			} 
			else {
				throw new IllegalStateException(
						"No DICOM metadata in database to match StudyInstanceUID"
								+ cxpTransaction.getStudyInstanceUid()
								+ " with transaction status "
								+ DicomMetadata.STATUS_READY_TO_DELETE);
			}

			PixDemographicData pixDemographicData  = pixDataExtractor.extractPixData(cxpTransaction, dicomMetadata, contextState);
			
			if (DDLTypes.UNKNOWN_MEDCOMMONS_ACCOUNT.equals(contextState
					.getStorageId())) {
				// None of the matching above found a valid MedCommmons ID - a
				// new one will be generated.
				unknownMedCommonsAccount = true;
			}
			// Late binding to cxp endpoint - if the DICOM arrived before
			// there was a gateway set, now set it to the current active gateway.
			// TODO: Very problematic
			if(CxpTransaction.UNKNOWN.equals(contextState.getGatewayRoot())){
				String gatewayRoot = configurations.getGatewayRoot();
				if (gatewayRoot == null){
					throw new NullPointerException("Gateway root is null; CXP upload fails");
				}
				contextState.setGatewayRoot(gatewayRoot); 
            }
			
			log.info("About to upload with sender AccountId: " + contextState.getAccountId());
			String cxpEndpoint = contextState.getCXPEndpoint();
			uploadFileAgent = new UploadFileAgent( cxpEndpoint, 
			        contextState.getAuth(), 
					contextState.getStorageId(),
					contextState.getAccountId(), 
					transactionDirectory);
		
			cxpTransaction.setStatus(CxpTransaction.STATUS_ACTIVE);
			db.save(cxpTransaction);
			monitor = new CxpTransferMonitor(uploadFileAgent, "Upload "
					+ cxpTransaction.getPatientName(), cxpTransaction);
			Thread t = new Thread(monitor);
			t.start();
			
			if(statusUpdate != null) {
			    
				monitorTransfer = MonitorTransfer.getTransferMonitor(this, uploadFileAgent, statusUpdate, contextState, cxpTransaction.getDashboardStatusId());
				
				boolean start = false;
				if(monitorTransfer == null) {
				    monitorTransfer = new MonitorTransfer(this, uploadFileAgent,statusUpdate,contextState,cxpTransaction.getDashboardStatusId());
				    start = true;
				}

				// If the transfer key was blank in the cxp transaction 
				// then it may have been set by the transfer, so set it
				// back on the transaction (hack)
				cxpTransaction.setDashboardStatusId(monitorTransfer.getTransferKey());
				
				db.save(cxpTransaction);
				
				if(start)
					new Thread(monitorTransfer).start();
			}
			
			uploadFileAgent.startTransactionTimer();
			PutResponse response = uploadFileAgent.upload();
			
			List<RegistryParameters> registryParameters = response
					.getRegistryParameters();
			CXPClient.displayResponseInfo(response);
			

			if (!uploadFileAgent.statusOK(response.getStatus())) {
				cxpTransaction.setStatus(CxpTransaction.STATUS_TEMPORARY_ERROR);
				cxpTransaction.setStatusMessage(response.getReason());
				cxpTransaction.setBytesTransferred(0);
				cxpTransaction
						.setRetryCount(cxpTransaction.getRetryCount() + 1);
				db.save(cxpTransaction);

				throw new RuntimeException("Image upload failed:"
						+ response.getReason());
			}

			String returnedStorageId = CXPClient.getMedCommonsParameter(
					registryParameters, CXPConstants.STORAGE_ID);
			if (unknownMedCommonsAccount) {
				log.info("New storageId returned:" + returnedStorageId);
				// Then this is a new id; record it in the database for future
				// use.
				if (pixDemographicData != null) {

					PixIdentifierData medcommonsPixIdentifierData = PatientMatch.getIdentifier(DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN,
							returnedStorageId);
					if (medcommonsPixIdentifierData == null){
						medcommonsPixIdentifierData = new PixIdentifierData();
						medcommonsPixIdentifierData
								.setAffinityDomain(DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN);
						medcommonsPixIdentifierData
								.setAffinityIdentifier(returnedStorageId);
						medcommonsPixIdentifierData.setCreationDate(new Date());
						medcommonsPixIdentifierData
								.setPixDemographicDataId(pixDemographicData.getId());
						db.save(medcommonsPixIdentifierData);
					}
				}
			}
			else{
				log.info("Using existing storage id " + returnedStorageId);
			}
			File dicomTimeDir = transactionDirectory.getParentFile();

			File timeDir = new File(cacheFolder, System.currentTimeMillis()
					+ "");
			DirectoryUtils.makeDirectory(timeDir);
			transactionDirectory = new File(timeDir, contextState.getStorageId());
			DirectoryUtils.makeDirectory(transactionDirectory);

			contextState.setStorageId(returnedStorageId);
			db.save(cxpTransaction);


			log.info("New transaction directory for CCR:"
					+ transactionDirectory.getAbsolutePath());
			File ccrDir = new File(transactionDirectory, "CCR");
			DirectoryUtils.makeDirectory(ccrDir);

			ContinuityOfCareRecordDocument ccrDocument = 
			    resolveOrderCCR(contextState.getStorageId(), transactions, metadata);

			if (ccrDocument == null) {
				ccrDocument = createCCRFromDICOM(transactions, metadata, returnedStorageId);
			}

			if(log.isDebugEnabled())
				log.debug("About to upload CCR\n" + ccrDocument.toString().substring(0, 200) + " .... (truncated)");
			
			if (!ccrDocument.validate()){
				File tempJunk = new File("CCRValidationError_" + System.currentTimeMillis() + ".xml");
				StringBuffer buff = new StringBuffer();
				
				CCRDocumentUtils.saveCCR(ccrDocument, tempJunk);
				log.error("Wrote invalid CCR document to " + tempJunk.getAbsolutePath());
				ContinuityOfCareRecordDocument aDoc = CCRDocumentUtils.parseAndCheckSchemaValidation(tempJunk);
				
			}
			/*
			 * if (ccrDocument == null) // Create a new CCR
			 *
			 * else { DownloadHandler.Factory().setCurrentCCR(null); // once a //
			 * CCR has // DICOM // added to // it - it // should be // removed.
			 * ccrDocument = ccrGenerator.addDicom(ccrDocument,
			 * cxpTransaction.getStorageId(), dicomMetadata, transactions);
			 *  }
			 */

			File ccrFile = new File(ccrDir, System.currentTimeMillis() + ".xml");
			CCRDocumentUtils.saveCCR(ccrDocument, ccrFile);

			File receiptFile = new File(transactionDirectory, "Receipt.txt");
			FileOutputStream out = new FileOutputStream(receiptFile);
			Properties p = new Properties();
			p.setProperty("Version", "1.0.0.5");// Version.getVersionString());

			p.setProperty("Revision", configurations.getVersion());
			p.store(out, "DDL Receipt");
			cxpEndpoint = contextState.getGatewayRoot() +  CxpTransaction.CXP_ENDPOINT;
			final String docGuid = contextState.getGuid();
			uploadFileAgent = new UploadFileAgent(cxpEndpoint, contextState.getAuth(), returnedStorageId, contextState
					.getAccountId(), transactionDirectory) {
			    
                protected void setRequestParameters(PutRequest request) {
                    log.info("Setting referrer guid = " + docGuid);
                    super.setRequestParameters(request);
                    Parameter param = new Parameter();
                    param.setName(CXPConstants.REFERRER_GUID);
                    param.setValue(docGuid);
                    request.getRegistryParameters().get(0).getParameters().add(param);
                }
			};
			
			response = uploadFileAgent.upload();

			CXPClient.displayResponseInfo(response);
			if (!uploadFileAgent.statusOK(response.getStatus())) {
				cxpTransaction.setStatus(CxpTransaction.STATUS_TEMPORARY_ERROR);
				cxpTransaction.setStatusMessage(response.getReason());
				db.save(cxpTransaction);
				throw new RuntimeException("CCR upload failed:"
						+ response.getReason());
			}
			String ccrGuid = getCCRGuid(response);

			//cxpTransaction.setGuid(ccrGuid);
			monitorTransfer.setSuccess(true);
			cxpTransaction.setStatus(CxpTransaction.STATUS_COMPLETE);
			cxpTransaction.setElapsedTime(System.currentTimeMillis() - cxpTransaction.getTimeStarted());
			String url = cxpTransaction.makeViewUrl(contextState.getGatewayRoot(),
			        contextState.getStorageId(), contextState.getAccountId(), ccrGuid, contextState.getAuth());
			cxpTransaction.setViewUrl(url);
			db.save(cxpTransaction);

			log.info("Deleting the dicomTimeDir:"
					+ dicomTimeDir.getAbsolutePath());
			FileUtils.deleteDir(dicomTimeDir); // Delete the DICOM
			log.info("Deleting the timeDir:" + timeDir.getAbsolutePath());
			FileUtils.deleteDir(timeDir); // Delete the CCR directory
			StatusDisplayManager.get().setMessage(
					"Upload Complete",
					"DICOM study for " + cxpTransaction.getPatientName(),
					cxpTransaction.getDashboardStatusId());
			monitor.stopMonitor(false);
			StatusDisplayManager.setIdleIcon();

			URL documentUrl = new URL(url);
			boolean displaySuccess = true;
			if(!contextState.isFlagSet(ContextState.Flag.NOBROWSER)) {
				if (contextManager.getDisplayUploadedCCR())
					displaySuccess = StatusDisplayManager.showDocument(documentUrl);
			}
			
			Iterator<DicomMetadata> metadataIter = metadata.iterator();
			while (metadataIter.hasNext()) {
				db.delete(metadataIter.next());
			}
			Iterator<DicomTransaction> txIter = transactions.iterator();
			while (txIter.hasNext()) {
				db.delete(txIter.next());
			}

			if (!displaySuccess) {
				log.error("Unable to display document:" + url);
			}
		}
		catch (Exception e) {
			boolean cancelled = FileUtils.isStreamCancelled(e);
			Store db = DB.get();

			cxpTransaction = db.selectSingle(CxpTransaction.class, "id", id);
			
			if (cancelled) {
				if(cxpTransaction != null) {
					String message = "Upload of " + cxpTransaction.getPatientName()
							+ ":" + cxpTransaction.getDisplayName()
							+ " was cancelled by user";
	
					log.info(message);
				    sdm.setErrorMessage("Upload cancelled", message, cxpTransaction.getDashboardStatusId());
					cxpTransaction.setStatus(CxpTransaction.STATUS_CANCELLED);
					cxpTransaction.setStatusMessage("Upload cancelled by user");
					db.save(cxpTransaction);
				}
				else {
				    log.info("Transaction cancelled but no longer present in database - assumed deleted.");
				}
			}
			else {
				log.error("Exception uploading data", e);
				if(cxpTransaction != null) {
					cxpTransaction.setStatus(CxpTransaction.STATUS_TEMPORARY_ERROR);
					cxpTransaction.setStatusMessage(e.getLocalizedMessage());
					db.save(cxpTransaction);
					sdm.setErrorMessage("Error uploading", e.getLocalizedMessage(), cxpTransaction.getDashboardStatusId());
				}
				else {
					sdm.setErrorMessage("Error uploading", e.getLocalizedMessage());
				}
				
				if(monitorTransfer != null)
					monitorTransfer.setState(MonitorTransfer.TxState.ERROR);
			}

		} finally {
			if (monitor != null) {
				monitor.stopMonitor(false);
			}
			if (monitorTransfer != null){
				monitorTransfer.exit();
			}
		}

	}

	/**
	 * Create a CCR based on the specified DICOM
	 */
    private ContinuityOfCareRecordDocument createCCRFromDICOM(List<DicomTransaction> transactions,
            List<DicomMetadata> metadata, String returnedStorageId)
            throws IOException 
    {
        ContinuityOfCareRecordDocument ccrDocument = null;
        
        // If there is no CCR order, create a new one.
        CCRGenerator ccrGenerator = new CCRGenerator();
        log.info("passing storageid " + returnedStorageId);
        ccrGenerator.createDemographicsFromDICOM(returnedStorageId, metadata, transactions, null);
        for(DicomTransaction tx : transactions) {
        	ccrDocument = addDicomReferences(ccrGenerator, tx, metadata);
        }
        
        if(!ccrGenerator.isCCRValid()) {
        	log.error("\n" + ccrDocument.toString() + "\n");
        	throw new RuntimeException("Invalid CCR created");
        }
        return ccrDocument;
    }

	
	/**
	 * Search for an order for the specified patient.
	 * <p>
	 * If order is found, return CCR based on specified order
	 * and delete order found.
	 * <p>
	 * Otherwise, return null.
	 */
    private ContinuityOfCareRecordDocument resolveOrderCCR(String storageId,
            List<DicomTransaction> transactions, List<DicomMetadata> metadata) throws XmlException, IOException 
    {
        List<CCRRef> orderCCRReferences = CCRMatch.getCCRReferences(storageId);
        
        if (orderCCRReferences == null) 
            return null;
        
        DicomMetadata dicomMetadata = metadata.get(0);
        ContinuityOfCareRecordDocument ccrDocument = null;
        CCRRef anOrder = orderCCRReferences.get(0); // In future - do a
											        // better match.
        
        try {
	        File localCCRFile = new File(anOrder.getFileLocation()); 
	        if (!localCCRFile.exists()) {
	            // Inconsistency - the database entry exists but the CCR is
	            // not on disk.
	            // Display an error- then continue.
	            
	            String message = "Internal inconsistency: CCR order for account="
	                + anOrder.getStorageId()
	                + ", guid="
	                + anOrder.getGuid()
	                + " references a file that does not exist:\n"
	                + anOrder.getFileLocation()
	                + ". Order will be removed from the database; DICOM data will be placed in account "
	                + storageId;
	            
	            log.error(message);
	            StatusDisplayManager.get()
	            .setErrorMessage("Missing File", message,cxpTransaction.getDashboardStatusId());
	        }
	        else {
	            log.info("Read local CCR from disk (Add DICOM case):"
	                    + anOrder.getStorageId() + "," + anOrder.getGuid()
	                    + "," + anOrder.getFileLocation());
	            
	            CCRDocumentUtils ccrUtils = new CCRDocumentUtils();
	            ccrDocument = ccrUtils.parseFile(localCCRFile);
	            CCRGenerator ccrGenerator = new CCRGenerator(ccrDocument);
	            ccrGenerator.initializeCrossReferences(storageId, dicomMetadata);
	            
	            for (int i=0;i<transactions.size();i++) {
	                DicomTransaction transaction = transactions.get(i);
	                if(!blank(transaction.getSeriesSha1())) { // A bit of a hack, but errors earlier in the import process can 
											                  // lead to getting to here with no sha1
	                    ccrDocument =addDicomReferences(ccrGenerator, transaction, metadata);
	                }
	                else
	                    log.warn("Ignoring dicom transaction " + transaction.getId() + " due to null sha1");
	            }
	            
	            ContinuityOfCareRecord aCCR = ccrDocument.getContinuityOfCareRecord();
	            Actors actors = aCCR.getActors();
	            ccrGenerator.filterPatientActor(actors, storageId, dicomMetadata);
	            if (!ccrGenerator.isCCRValid()){
	                throw new RuntimeException("Invalid CCR created");
	            }
	        }
        }
        finally {
			if (anOrder != null) {
				DB.get().delete(anOrder);
				log.info("Deleted order  " + anOrder.getStorageId() + "," + anOrder.getGuid());
			}
        }
        return ccrDocument;
    }

	private ContinuityOfCareRecordDocument  addDicomReferences(
			CCRGenerator ccrGenerator, 
			DicomTransaction transaction, 
			List<DicomMetadata> metadata) throws IOException{
		
	
		DicomMetadata seriesMetadata = null;
		for (int j=0;j<metadata.size();j++){
			DicomMetadata candidate = metadata.get(j);
			if (candidate.getSeriesInstanceUid().equals(transaction.getSeriesInstanceUid())){
				seriesMetadata=candidate;
				break;
			}

		}
		if (seriesMetadata == null){
			throw new NullPointerException("No series with SeriesInstanceUID " +
					transaction.getSeriesInstanceUid());
		}
		
		ContinuityOfCareRecordDocument ccrDocument = ccrGenerator.addDicomReference(transaction, seriesMetadata, null);
		return(ccrDocument);
		
	}
	private String getCCRGuid(PutResponse resp) {
		String guid = null;
		List<Document> responseDocs = resp.getDocinfo();

		Iterator<Document> iter = responseDocs.iterator();

		while (iter.hasNext()) {
			Document doc = iter.next();

			String contentType = doc.getContentType();
			if ("application/x-ccr+xml".equals(contentType)) {
				guid = doc.getGuid();
				log.info("Guid is " + guid);
			}

		}
		return (guid);
	}

}
