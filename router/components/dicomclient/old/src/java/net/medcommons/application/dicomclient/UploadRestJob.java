package net.medcommons.application.dicomclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import net.medcommons.application.dicomclient.transactions.CCRMatch;
import net.medcommons.application.dicomclient.transactions.CCRReference;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.PatientMatch;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DDLTypes;
import net.medcommons.application.dicomclient.utils.DicomNameParser;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.DirectoryUtils;
import net.medcommons.application.dicomclient.utils.LocalHibernateUtil;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.upload.StatusUpdate;
import net.medcommons.application.utils.JSONSimpleGET;
import net.medcommons.application.utils.MonitorTransfer;
import net.medcommons.client.utils.CCRDocumentUtils;
import net.medcommons.client.utils.CCRGenerator;
import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.cxp.client.CXPClient;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.transfer.UploadFileAgent;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.modules.utils.SupportedDocuments;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;
import org.cxp2.Document;
import org.cxp2.Parameter;
import org.cxp2.PutRequest;
import org.cxp2.PutResponse;
import org.cxp2.RegistryParameters;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;



import astmOrgCCR.ContinuityOfCareRecordDocument;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.Actors;

/**
 * TODO: What needs to be saved here? Do we save ContextState or is that read-only? 
 * @author mesozoic
 *
 */
public class UploadRestJob implements Runnable, Job {
	CxpTransaction cxpTransaction = null;

	Long id = null;

	UploadFileAgent uploadFileAgent = null;
	//String auth = null;

	File cacheFolder;
	ContextManager contextManager = ContextManager.getContextManager();
	private static Logger log = Logger.getLogger(UploadRestJob.class.getName());

	public UploadRestJob(File cacheFolder, CxpTransaction cxpTransaction) {
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

		final ContextManager contextManager = ContextManager.getContextManager();
		Configurations configurations = contextManager.getConfigurations();
		String transactionFolder = cxpTransaction.getTransactionFolder();
		StatusUpdate statusUpdate = contextManager.getStatusUpdate();
		
		

		CxpTransferMonitor monitor = null;
		MonitorTransfer monitorTransfer  = null;
		CCRReference anOrder = null;
		DicomMetadata dicomMetadata = null;
		CCRDocumentUtils ccrUtils = new CCRDocumentUtils();
		String storageId = null;
		StatusDisplayManager sdm = StatusDisplayManager
				.getStatusDisplayManager();
		boolean unknownMedCommonsAccount = true;
		PixDemographicData pixDemographicData = null;
		Long contextStateId = cxpTransaction.getContextStateId();
		ContextState contextState = TransactionUtils.getContextState(contextStateId);
		if (contextState == null){
		    throw new NullPointerException("Missing contextState in " + cxpTransaction);
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
			Session session = LocalHibernateUtil.currentSession();

			Criteria transactionsWithCxpId = session
					.createCriteria(DicomTransaction.class);
			transactionsWithCxpId.add(Expression.eq("cxpJob", cxpTransaction
					.getId()));

			List<DicomTransaction> transactions = transactionsWithCxpId.list();

			Criteria metadataWithStudyInstanceUID = session
					.createCriteria(DicomMetadata.class);
			metadataWithStudyInstanceUID.add(Expression.eq("studyInstanceUid",
					cxpTransaction.getStudyInstanceUid()));
			metadataWithStudyInstanceUID.add(Expression.eq("transactionStatus",
					DicomMetadata.STATUS_READY_TO_DELETE));
			List<DicomMetadata> metadata = metadataWithStudyInstanceUID.list();
			if (metadata.size() > 0) {
				dicomMetadata = metadata.get(0);
			} else {
				throw new IllegalStateException(
						"No DICOM metadata in database to match StudyInstanceUID"
								+ cxpTransaction.getStudyInstanceUid()
								+ " with transaction status "
								+ DicomMetadata.STATUS_READY_TO_DELETE);
			}
			LocalHibernateUtil.closeSession();

			String institution = dicomMetadata.getInstitutionName();
			String patientId = dicomMetadata.getPatientId();
			PixIdentifierData pixIdentifier = null;
			if ((institution != null) && (patientId != null)) {
				pixIdentifier = PatientMatch.getIdentifier(dicomMetadata
						.getPatientId(), dicomMetadata.getInstitutionName());

				if (pixIdentifier == null) {
					log.info("Creating new PixIdentifierData for "
            				+ dicomMetadata.getInstitutionName() + "," + dicomMetadata.getPatientId());
					pixIdentifier = new PixIdentifierData();
					pixIdentifier.setAffinityDomain(dicomMetadata
							.getInstitutionName().toUpperCase());

					pixIdentifier.setAffinityIdentifier(dicomMetadata
							.getPatientId().toUpperCase());
					pixIdentifier.setCreationDate(new Date());
					DicomNameParser dicomNameParser = new DicomNameParser();
					String givenName = dicomNameParser.givenName(dicomMetadata
							.getPatientName());
					String familyName = dicomNameParser
							.familyName(dicomMetadata.getPatientName());
					String middleName = null;
					List<PixDemographicData> matchingPatients = PatientMatch
							.getPatient(givenName, middleName, familyName);
					if (matchingPatients != null) {
						if (matchingPatients.size() == 1) {
							// Add this DICOM identifier for future matches
							pixIdentifier
									.setPixDemographicDataId(matchingPatients
											.get(0).getId());
							//pixIdentifier = TransactionUtils
							//		.saveTransaction(pixIdentifier);
						} else {
							String message = "Ambiguity - there are multiple ("
									+ matchingPatients.size()
									+ ") with name "
									+ familyName
									+ ","
									+ givenName
									+ " and DICOM identifier "
									+ dicomMetadata.getPatientId()
									+ " does not exist in table"
									+ "\n Will continue and make new MedCommons account";
							log.error(message);
							StatusDisplayManager.getStatusDisplayManager()
									.setErrorMessage("Matching ambiguity",
											message);
						}
					} else {
						pixDemographicData = new PixDemographicData();
						pixDemographicData.setFamilyName(familyName);
						pixDemographicData.setGivenName(givenName);
						String sex = dicomMetadata.getPatientSex();
						if ((sex != null) && (!sex.equals(""))) {
							String gender = null;
							if (sex.equalsIgnoreCase("F")) {
								gender = "Female";
							} else if (sex.equalsIgnoreCase("M")) {
								gender = "Male";
							}
							if (gender != null) {
								pixDemographicData.setGender(gender);
							}
						}
						pixDemographicData.setDob(dicomMetadata
								.getPatientDateOfBirth()); 
						
						pixDemographicData = TransactionUtils
								.saveTransaction(pixDemographicData);
						pixIdentifier
								.setPixDemographicDataId(pixDemographicData
										.getId());
						pixIdentifier.setContextStateId(contextState.getId());
						pixIdentifier = TransactionUtils
								.saveTransaction(pixIdentifier);
						log.info("Saved new PixIdentifier" + pixIdentifier.getAffinityDomain() +"," + pixIdentifier.getAffinityIdentifier());
					}

				} else {
					pixDemographicData = PatientMatch.getPatient(pixIdentifier
							.getAffinityDomain(), pixIdentifier
							.getAffinityIdentifier());

					if (pixDemographicData == null) {
						String message = "Internal inconsistency: database has reference to id with affinityDomain="
								+ pixIdentifier.getAffinityDomain()
								+ ", affinityIdentifier="
								+ pixIdentifier.getAffinityIdentifier()
								+ "\nWill upload into new account";
						log.error(message);
						StatusDisplayManager.getStatusDisplayManager()
								.setErrorMessage("Internal inconsistency",
										message);
					} else {
						PixIdentifierData medcommonsIdentifierData = PatientMatch
								.getIdentifier(pixDemographicData.getId(),
										DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN);
						if (medcommonsIdentifierData != null) {
							storageId = medcommonsIdentifierData
									.getAffinityIdentifier();
							log.info("Obtained storageId from PixIdentiferData:" + storageId);
						} else {
							String message = "Internal inconsistency: patient has no medcommons id="
									+ pixDemographicData.getFamilyName()
									+ ", "
									+ pixDemographicData.getGender()
									+ ", internal database id ="
									+ pixDemographicData.getId()
									+ "\nWill upload into new account";
							log.error(message);
							StatusDisplayManager.getStatusDisplayManager()
									.setErrorMessage("Internal inconsistency",
											message);
						}
					}
				}
			}
			if (DDLTypes.UNKNOWN_MEDCOMMONS_ACCOUNT.equals(contextState
					.getStorageId())) {
				// None of the matching above found a valid MedCommmons ID - a
				// new one will be generated.
				unknownMedCommonsAccount = true;


			}
			// Late binding to cxp endpoint - if the DICOM arrived before
			// there was a gateway set, now set it to the current active gateway.
			// TODO: Very problematic
			if (CxpTransaction.UNKNOWN.equals(contextState.getGatewayRoot())){
				String gatewayRoot = configurations.getGatewayRoot();
				if (gatewayRoot == null){
					throw new NullPointerException("Gateway root is null; CXP upload fails");
				}
			
				contextState.setGatewayRoot(gatewayRoot); 

            }
			
			log.info("About to upload with sender AccountId: " + contextState.getAccountId());
			cxpTransaction.setStatus(CxpTransaction.STATUS_ACTIVE);
			cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
			List<Part> partList = new ArrayList<Part>();
			partList.add(new StringPart("storageid", contextState.getStorageId()));
			partList.add(new StringPart("auth", contextState.getAuth()));
			partList.add(new StringPart("accountid", contextState.getAccountId()));
			
				

			
			 for (int i=0;i<metadata.size(); i++){
				 DicomMetadata m = metadata.get(i);
				 log.info("dicom:" + m.toString());
				 FilePart filePart  = new FilePart(m.getSopInstanceUid(), m.getFile());
				 filePart.setContentType(SupportedDocuments.DICOM.getContentType());
				 
				 partList.add(filePart);
				
			 }
			 Part[] parts = new Part[partList.size()];
			 parts = partList.toArray(parts);
			 
			 String protocol = contextState.getCxpProtocol();
				if ("https".equals(protocol)){
					protocol = JSONSimpleGET.HTTPS; 
				}
				
				
				protocol = "http";
				int port = 80;
				String targetURL = protocol + "://" + contextState.getCxpHost() + ":" +
					port + "/router/AccountDocument";
				log.info("About to upload to targeturl " + targetURL);
				PostMethod filePost = new PostMethod(targetURL);
				
				filePost.setRequestEntity(
						new MultipartRequestEntity(parts, filePost.getParams())
                 );
			
             HttpClient client = new HttpClient();
             client.getHttpConnectionManager().
                 getParams().setConnectionTimeout(5000);
             
           
             int status = client.executeMethod(filePost);
             filePost.releaseConnection();
             
             if (status == HttpStatus.SC_OK) {
                log.info("Upload successful");
             } else {
            	 cxpTransaction.setStatus(CxpTransaction.STATUS_TEMPORARY_ERROR);
 				cxpTransaction.setStatusMessage(HttpStatus.getStatusText(status));
 				cxpTransaction.setBytesTransferred(0);
 				cxpTransaction
 						.setRetryCount(cxpTransaction.getRetryCount() + 1);
 				cxpTransaction = TransactionUtils
 						.saveTransaction(cxpTransaction);

 				throw new RuntimeException("Image upload failed:"
 						+ HttpStatus.getStatusText(status));
             }


		
			
			
            String returnedStorageId = contextState.getStorageId();

			

		
			File dicomTimeDir = transactionDirectory.getParentFile();

			File timeDir = new File(cacheFolder, System.currentTimeMillis()
					+ "");
			DirectoryUtils.makeDirectory(timeDir);
			transactionDirectory = new File(timeDir, contextState.getStorageId());
			DirectoryUtils.makeDirectory(transactionDirectory);

			
			cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);


			log.info("New transaction directory for CCR:"
					+ transactionDirectory.getAbsolutePath());
			File ccrDir = new File(transactionDirectory, "CCR");
			DirectoryUtils.makeDirectory(ccrDir);

			List<CCRReference> orderCCRReferences = CCRMatch
					.getCCRReferences(contextState.getStorageId());

			ContinuityOfCareRecordDocument ccrDocument = null;
			if (orderCCRReferences != null) {
				anOrder = orderCCRReferences.get(0); // In future - do a
														// better match.
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
							+ contextState.getStorageId();

					log.error(message);
					StatusDisplayManager.getStatusDisplayManager()
							.setErrorMessage("Missing File", message);
				} else {
					log.info("Read local CCR from disk (Add DICOM case):"
							+ anOrder.getStorageId() + "," + anOrder.getGuid()
							+ "," + anOrder.getFileLocation());
					ccrDocument = ccrUtils.parseFile(localCCRFile);
					CCRGenerator ccrGenerator = new CCRGenerator(ccrDocument);
					ccrGenerator.initializeCrossReferences(returnedStorageId, metadata, transactions, null);
					
					for (int i=0;i<transactions.size();i++){
						DicomTransaction transaction = transactions.get(i);
						ccrDocument =addDicomReferences(ccrGenerator, transaction, metadata);
								
					}
					ContinuityOfCareRecord aCCR = ccrDocument.getContinuityOfCareRecord();
					Actors actors = aCCR.getActors();
					ccrGenerator.filterPatientActor(actors, returnedStorageId, metadata.get(0));
					if (!ccrGenerator.isCCRValid()){
						throw new RuntimeException("Invalid CCR created");
					}
					

				}
			}

			if (ccrDocument == null) {
				// If there is no CCR order, create a new one.
				CCRGenerator ccrGenerator = new CCRGenerator();
				log.info("passing storageid " + returnedStorageId);
				ccrGenerator.createDemographicsFromDICOM(returnedStorageId, metadata, transactions, null);
				for (int i=0;i<transactions.size();i++){
					DicomTransaction transaction = transactions.get(i);
					ccrDocument =addDicomReferences(ccrGenerator, transaction, metadata);
							
				}
				if (!ccrGenerator.isCCRValid()){
					log.error("\n" + ccrDocument.toString() + "\n");
					throw new RuntimeException("Invalid CCR created");
				}
				
				
			}

			log.info("About to upload CCR\n" + ccrDocument.toString());
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
			String cxpEndpoint = contextState.getGatewayRoot() +  CxpTransaction.CXP_ENDPOINT;
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
			
			PutResponse response = uploadFileAgent.upload();

			CXPClient.displayResponseInfo(response);
			if (!uploadFileAgent.statusOK(response.getStatus())) {
				cxpTransaction.setStatus(CxpTransaction.STATUS_TEMPORARY_ERROR);
				cxpTransaction.setStatusMessage(response.getReason());
				cxpTransaction = TransactionUtils
						.saveTransaction(cxpTransaction);
				throw new RuntimeException("CCR upload failed:"
						+ response.getReason());
			}
			String ccrGuid = getCCRGuid(response);

			//cxpTransaction.setGuid(ccrGuid);
			//monitorTransfer.setSuccess(true);
			cxpTransaction.setStatus(CxpTransaction.STATUS_COMPLETE);
			cxpTransaction.setElapsedTime(System.currentTimeMillis()
					- cxpTransaction.getTimeStarted());
			String url = cxpTransaction.makeViewUrl(contextState.getGatewayRoot(),
			        contextState.getStorageId(), contextState.getAccountId(), ccrGuid, contextState.getAuth());
			cxpTransaction.setViewUrl(url);
			cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);

			log.info("Deleting the dicomTimeDir:"
					+ dicomTimeDir.getAbsolutePath());
			FileUtils.deleteDir(dicomTimeDir); // Delete the DICOM
			log.info("Deleting the timeDir:" + timeDir.getAbsolutePath());
			FileUtils.deleteDir(timeDir); // Delete the CCR directory
			StatusDisplayManager.getStatusDisplayManager().setMessage(
					"Upload Complete",
					"DICOM study for " + cxpTransaction.getPatientName());
			monitor.stopMonitor(false);
			StatusDisplayManager.setIdleIcon();
 
			URL documentUrl = new URL(url);
			boolean displaySuccess = true;
			if(!contextState.isFlagSet(ContextState.Flag.NOBROWSER)) {
				if (contextManager.getDisplayUploadedCCR())
					displaySuccess = StatusDisplayManager.showDocument(documentUrl);
			}
			session = LocalHibernateUtil.currentSession();
			session.beginTransaction();
			Iterator<DicomMetadata> metadataIter = metadata.iterator();
			while (metadataIter.hasNext()) {
				session.delete(metadataIter.next());
			}
			Iterator<DicomTransaction> transactionIter = transactions
					.iterator();
			while (transactionIter.hasNext()) {
				session.delete(transactionIter.next());
			}
			session.getTransaction().commit();
			LocalHibernateUtil.closeSession();

			if (!displaySuccess) {
				log.error("Unable to display document:" + url);
			}
		}

		catch (Exception e) {
			boolean cancelled = FileUtils.isStreamCancelled(e);

			if (cancelled) {
				String message = "Upload of " + cxpTransaction.getPatientName()
						+ ":" + cxpTransaction.getDisplayName()
						+ " was cancelled by user";

				log.info(message);

				sdm.setErrorMessage("Upload cancelled", message);
				if (cxpTransaction != null) {
					cxpTransaction = TransactionUtils.getCxpTransactionWithId(cxpTransaction.getId());
					cxpTransaction.setStatus(CxpTransaction.STATUS_CANCELLED);
					cxpTransaction.setStatusMessage("Upload cancelled by user");
					cxpTransaction = TransactionUtils
							.saveTransaction(cxpTransaction);
				}
			} else {
				log.error("Exception uploading data", e);

				sdm.setErrorMessage("Error uploading", e.getLocalizedMessage());
				if (cxpTransaction != null) {
					cxpTransaction = TransactionUtils.getCxpTransactionWithId(cxpTransaction.getId());
					cxpTransaction
							.setStatus(CxpTransaction.STATUS_TEMPORARY_ERROR);
					cxpTransaction.setStatusMessage(e.getLocalizedMessage());
					cxpTransaction = TransactionUtils
							.saveTransaction(cxpTransaction);
				}
			}

		} finally {
			if (monitor != null) {
				monitor.stopMonitor(false);
			}
			if (monitorTransfer != null){
				monitorTransfer.exit();
			}
			if (anOrder != null) {
				TransactionUtils.delete(anOrder);
				log.info("Deleted order  " + anOrder.getStorageId() + ","
						+ anOrder.getGuid());
			}
		}

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
