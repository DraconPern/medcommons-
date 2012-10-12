package net.medcommons.modules.cxp.server;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.bvl;
import static net.medcommons.modules.utils.Str.join;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.crypto.spec.SecretKeySpec;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.filestore.RepositoryFileProperties;
import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.repository.GatewayRepository;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.BillingCharge;
import net.medcommons.modules.services.interfaces.BillingEvent;
import net.medcommons.modules.services.interfaces.BillingEventType;
import net.medcommons.modules.services.interfaces.BillingService;
import net.medcommons.modules.services.interfaces.CompoundDocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.InsufficientCreditException;
import net.medcommons.modules.services.interfaces.MetadataHandler;
import net.medcommons.modules.services.interfaces.SecondaryRegistryService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.rest.RESTConfiguration;
import net.medcommons.rest.RESTConfigurationException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.PerformanceMeasurement;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.router.globalcontext.Transactions;
import net.medcommons.router.services.ccr.CCRStoreException;
import net.medcommons.router.services.ccr.RLSCXPHandler;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.codehaus.xfire.attachments.AttachmentDataSource;
import org.cxp2.DeleteResponse;
import org.cxp2.Document;
import org.cxp2.GetResponse;
import org.cxp2.Parameter;
import org.cxp2.PutResponse;
import org.cxp2.RegistryParameters;
import org.cxp2.soap.CXPService;
/**
 * Old CXP service. Needs to be deprecated.
 * 
 * @author sean
 *
 */
@WebService(wsdlLocation = "file:/Users/mesozoic/Documents/MedCommons/svn.medcommons.net/services/trunk/java/cxpserver/etc/wsdl/CXP2.wsdl", endpointInterface = "org.cxp2.soap.CXPService")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class CXP2Impl implements CXPService, CXPConstants {

	//private GatewayRepository repository = null;

	//String authToken = "Gateway";

	String nodeId = null;

	boolean encryptionEnabled = true;

	boolean backupEnabled = false;

	

	private ServicesFactory serviceFactory=null;

	TransactionUtils transactionUtils = new TransactionUtils();

	public CXP2Impl() {
		log.info("Instantiating CXP2 SOAP service");

		try {
			String path = "conf/config.xml";
			String propertiesPath = "conf/MedCommonsBootParameters.properties";
			Configuration.load(path, propertiesPath);
			String nodeId = Configuration.getProperty("NodeID");
			String encryptionConfig = Configuration.getProperty("EncryptionEnabled");
			String backupConfig = Configuration.getProperty("Backup_Documents");

			if ((encryptionConfig != null) && (!"".equals(encryptionConfig))) {
				encryptionEnabled = Boolean.parseBoolean(encryptionConfig);

			}
			if ((backupConfig != null) && (!"".equals(backupConfig))) {
				backupEnabled = Boolean.parseBoolean(backupConfig);

			}

            // We just configure it with a pass-through to the static Configuration class
            //log.info("Initializing REST configuration ...");
            //System.err.println("Initializing REST configuration ...");
            RESTUtil.init( new RESTConfiguration() {
                public String getProperty(String name) throws RESTConfigurationException {
                    try {
                        return Configuration.getProperty(name);
                    }
                    catch (ConfigurationException e) {
                        throw new RESTConfigurationException("Failed retrieving configuration value " + name, e);
                    }
                }
                public String getProperty(String name, String defaultValue) {
                    return Configuration.getProperty(name,defaultValue);
                }

                public int getProperty(String name, int defaultValue) {
                    return Configuration.getProperty(name, defaultValue);
                }

                public boolean getProperty(String name, boolean defaultValue) {
                    return Configuration.getProperty(name,defaultValue);
                }
            });
		} catch (Exception e) {
			log.error("Exception initializing repository", e);
			throw new RuntimeException("Exception initializing repository", e);
		}
	}

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("CXP2Impl");

	public GetResponse get(org.cxp2.GetRequest GetRequest) {
	    newRequest();
		long startTime = System.currentTimeMillis();
		long totalBytes = 0;
		log.info("In get: storageid " + GetRequest.getStorageId());
		String storageId = GetRequest.getStorageId();
		if ((storageId == null) || ("".equals(storageId.trim()))){
			storageId =  POPS_MEDCOMMONS_ID;
		}
		
		GetResponse resp = new GetResponse();
		resp.setStatus(200);
		resp.setReason("OK");
		resp.setCxpVersion("2.0");
		DocumentDescriptor docDescriptor = null;
		InputStream inputStream = null;
		GetRequest.setStorageId(storageId);

		List<Document> documents = GetRequest.getDocinfo();
		Iterator<Document> iter = documents.iterator();
		String registrySecret = null;
		String confirmationCode = null;
		boolean retrieveData = true;
		boolean notFound = false;
		try {
			if ((storageId == null) || (storageId.trim().equals("")))
				throw new RuntimeException("Missing storageId from CXP");
			// TODO:
			// Registry secret, confirmation code currently not used in code
			// below.
			List<RegistryParameters> requestParameters = GetRequest
					.getRegistryParameters();
			registrySecret = getMedCommonsParameter(requestParameters,
					REGISTRY_SECRET);
			String sRetrieveData = getMedCommonsParameter(requestParameters,
					RETRIEVE_DATA);
			if (sRetrieveData != null) {
				if (TRUE.equals(sRetrieveData))
					retrieveData = true;
				else{
					retrieveData = false;
					log.info("GET request: retrieveData = false");
				}
			}
			if (registrySecret != null) {
				log.info("Incoming registry secret set to " + registrySecret);
			}
			confirmationCode = getMedCommonsParameter(requestParameters,
					CONFIRMATION_CODE);
			if (confirmationCode != null) {
				log.info("Incoming confirmation code set to "
						+ confirmationCode);
			}
			String authToken = getMedCommonsParameter(requestParameters, AUTHORIZATION_TOKEN);
			ServicesFactory servicesFactory= getServiceFactory(authToken);
			GatewayRepository repository =  new GatewayRepository(authToken, nodeId, encryptionEnabled, backupEnabled);
			
			// First pass - calculate the size of the data to return.
			totalBytes = calculateRequestSize(repository, GetRequest);
			RegistryParameters registryParameters = generateResponseLength(totalBytes);
			resp.getRegistryParameters().add(registryParameters);
			// Second pass - return the data.


			while (iter.hasNext()) {
				Document requestedDoc = iter.next();
				log.info("Get for guid " + requestedDoc.getGuid());
				if (requestedDoc.getGuid() == null)
					throw new NullPointerException("Null GUID in document:" + requestedDoc);
				docDescriptor = createDocumentDescriptor(GetRequest
						.getStorageId(), requestedDoc);

				/**
				 * String filename = requestedDoc.getGuid(); log.info("About to
				 * get document " + filename); File f = new File(filename); if
				 * (!f.exists()) { throw new
				 * FileNotFoundException(f.getAbsolutePath()); } FileDataSource
				 * fileSource = new FileDataSource(f); DataHandler dh = new
				 * DataHandler(fileSource); Conceptual problem: If return
				 * inputstream -assuming that it's a single document. If
				 * document is a compound doc - then we really want to iterate
				 * here over the contents.
				 */
				// This is where a FileNotFoundException is thrown if
				// guid does not exist.
				Properties props = repository.getMetadata(docDescriptor);
				// TODO: For existence-only get - return here with a status of
				// 200.
				if (retrieveData) {
					String nDocuments = props
							.getProperty(RepositoryFileProperties.N_DOCUMENTS);
					String parent = props.getProperty(RepositoryFileProperties.PARENT_NAME);
					boolean compound = false;
					if ((parent!= null) && (!"".equals(parent)))
						compound = true;

					// A document may be a simple document (n==1) or a compound
					// one (n>1) for a single GUID.
					int n = Integer.parseInt(nDocuments);
					if (!compound) { // Send one document back in response to
									// this requested

						Document responseDoc = new Document();
						responseDoc.setGuid(requestedDoc.getGuid());
						String mimeType;
						// Set the response type to be what's in the file metadata.
						responseDoc.setContentType(props.getProperty(RepositoryFileProperties.CONTENT_TYPE));
						responseDoc.setDescription("");
						responseDoc.setSha1(props.getProperty(RepositoryFileProperties.SHA_1_HASH));
						docDescriptor.setContentType(responseDoc.getContentType());
						docDescriptor.setSha1(responseDoc.getSha1());

						log.info("CXP GET returning a non-compound document: SHA-1=" +  responseDoc.getSha1()+ ", content type=" + responseDoc.getContentType());
						inputStream = repository.get(docDescriptor);
						if (inputStream == null)
							throw new IOException(
									"Null inputstream attempting to get "
											+ documentDescriptorDisplay(docDescriptor));

						String contentType = "multipart/related; type=\"" + docDescriptor.getContentType() + "\"";
						String name = docDescriptor.getGuid() + "_" + docDescriptor.getSha1();
						DataSource ds = new RepositoryDataSource(contentType,name,inputStream);
						DataHandler dh = new DataHandler(ds);


						responseDoc.setData(dh);
						resp.getDocinfo().add(responseDoc);
						if (DocumentTypes.CCR_MIME_TYPE.equals(docDescriptor.getContentType())){
							CCRDocument ccr = parseCCR(repository, docDescriptor);
							log.info("GET on a CCR " + ccr.getXml());
							String senderGroupId = getMedCommonsParameter(requestParameters,
								SenderProviderId);
							//AccountSettings accountSettings = serviceFactory.getAccountService().queryAccountSettings(senderGroupId);

							// This is very ugly.
							// Basically - if there is dicom, mark it available for download.
							// Otherwise - skip.
							ccr.parseReferences();
							String guid = docDescriptor.getGuid();
							List series = (List) ccr.getSeriesList();
							AccountSettings senderAccountSettings = getAccountSettings(servicesFactory, senderGroupId);
							String accountGroupId = senderAccountSettings.getGroupId();
							if (!Str.blank(accountGroupId)){
								if ((series != null) && (series.size() > 0) ){
									String workflowType = "Download Status";
									String workflowStatus = "Downloaded";
									
									setWorklistDownloadState(
									        servicesFactory,
											accountGroupId,
											storageId,
											guid,
											workflowType,
											workflowStatus);
								}
								else{
									log.info("No dicom available for ccr " + guid + ", download state not set");
								}
							}
							else{
								log.info("No group defined for user " + senderGroupId + ", download state not set");
							}
						}
					} else { // Send back all documents in this compound
								// document.


						docDescriptor = createDocumentDescriptor(GetRequest
								.getStorageId(), requestedDoc);
						log.info("Returning compound document with " + n
								+ " enclosed documents");
						CompoundDocumentDescriptor docs[] = repository
								.getCompoundDocumentDescriptors(storageId,
										requestedDoc.getGuid());
						// ArrayList<DocumentInfo> retrievedDocuments = new
						// ArrayList<DocumentInfo>();

						for (int i = 0; i < docs.length; i++) {
							CompoundDocumentDescriptor aCompoundDoc = docs[i];

							docs[i].setStorageId(storageId);

							Document responseDoc = new Document();
							responseDoc.setGuid(aCompoundDoc.getGuid());
							responseDoc.setContentType(aCompoundDoc
									.getContentType());
							responseDoc.setParentName(aCompoundDoc
									.getParentName());
							responseDoc.setDocumentName(aCompoundDoc
									.getDocumentName());
							responseDoc.setSha1(aCompoundDoc.getSha1());

							log.debug("About to get:" + docs[i]);
							inputStream =repository.get(docs[i]);
							//text/plain; charset=UTF-8
							// "multipart/related; type=\"application/xop+xml\"
							String contentType = "multipart/related; type=\"" + docDescriptor.getContentType() + "\"";
							String name = aCompoundDoc.getGuid() + "_" + aCompoundDoc.getSha1();
							DataSource ds = new RepositoryDataSource(contentType,name,inputStream);
							DataHandler dh = new DataHandler(ds);

							//DataHandler dh = new DataHandler(fileSource);

							responseDoc.setData(dh);
							resp.getDocinfo().add(responseDoc);

							// Note: assume that inputStream is closed by DataHandler.

						}

					}

				}
			}
		} 
		catch (FileNotFoundException e) {
		    notFound = true;
		}
		catch (Exception e) {
            log.error("Failed to get document:"
                    + documentDescriptorDisplay(docDescriptor), e);
		    
		    if(e.getCause() instanceof FileNotFoundException)
		        notFound = true;
		    else {
		        resp.setStatus(500);
		        resp.setReason("ServerFailure:" + e.getLocalizedMessage());
		    }
		}
		finally{
			long endTime = System.currentTimeMillis();
			log.info(PerformanceMeasurement.throughputString("CXP GET", (endTime - startTime), totalBytes));
		}
		
		if(notFound) {
            resp.setStatus(404);
            if (retrieveData){
                resp.setReason("ServerFailure: File Not Found");
            }
            else{
                resp.setReason("ServerFailure: Document does not exist");
            }
		}

		return (resp);

	}

	private boolean isCompoundDocument(Document document) {
		log.debug("isCompoundDocument - parent name is '"
				+ document.getParentName() + "'");
		if ((null == document.getParentName())
				|| ("".equals(document.getParentName())))
			return (false);
		else
			return (true);
	}

	/**
	 * Sets the guid in the response documents. The response documents are
	 * created as the stream arrives, the guid is calculated at the end of each
	 * 'compound document'.
	 *
	 * @param documentDescriptor
	 * @param documents
	 */
	private void setResponseGuids(DocumentDescriptor documentDescriptor,
			List<Document> documents) {
		String guid = documentDescriptor.getGuid();

		for (int i = 0; i < documents.size(); i++) {
			documents.get(i).setGuid(guid);
		}
	}

	/**
	 * Manages state of response documents during CXP transaction.
	 * <P>
	 * Multiple compound documents may be sent during the same CXP transaction;
	 * some calculations are done on the set of current documents, others on the
	 * entire set of documents transferred.
	 *
	 * @author mesozoic
	 *
	 */
	private class CXPDocumentState {
		// The response documents are what is returned in the put response.

		ArrayList<Document> responseDocuments = new ArrayList<Document>();

		// The current documents are 'current' for the current compound
		// document.
		ArrayList<Document> currentCompoundDocuments = new ArrayList<Document>();

		void add(Document document, boolean compoundDocument) {
			responseDocuments.add(document);
			if (compoundDocument)
				currentCompoundDocuments.add(document);
		}

		/**
		 * Reset the list of compound documents so that CXPDocumentState is
		 * ready for the start of the next compound document.
		 */
		void clearCurrentCompoundDocuments() {
			currentCompoundDocuments.clear();
		}

		/**
		 * Set guids on the current documents only.
		 *
		 * @param documentDescriptor
		 */
		void setResponseGuids(DocumentDescriptor documentDescriptor) {
			String guid = documentDescriptor.getGuid();

			for (int i = 0; i < currentCompoundDocuments.size(); i++) {
				currentCompoundDocuments.get(i).setGuid(guid);
			}
			clearCurrentCompoundDocuments();
		}

	}

	/*
	 * The criterion of the 'parentName' attribute is used to determine where
	 * one compound document ends and the next one begins.
	 */
	private boolean isNewDocument(String parent1, String parent2) {
		if ((parent1 == null) || (parent2 == null))
			return (true);
		else
			return (!parent1.equals(parent2));
	}

	public PutResponse put(org.cxp2.PutRequest putRequest) {
	    newRequest();
		long startTime = System.currentTimeMillis();
		long totalBytes = 0;
		
		
		boolean generatedReturnParameters = false;
		String senderGroupId = null;
		String accountGroupId = null;
		String paymentBypassToken = null;
		String authToken = null;
		CXPConstants.MergeCCRValues mergeCCR = CXPConstants.MergeCCRValues.ALL; // Default is merge in all

		DocumentDescriptor docDescriptor = null;
		CXPDocumentState documentState = new CXPDocumentState();
		boolean bypassPayment = false;

		PutResponse resp = new PutResponse();
		resp.setCxpVersion("2.0");
		resp.setStatus(500);
		resp.setReason("Server Error");
		List<Document> documents = putRequest.getDocinfo();
		DocumentDescriptor transactionDescr = null;


		String registrySecret = null;
		// String confirmationCode = null;
		String guid = null;
		String storageId = bvl(putRequest.getStorageId(),POPS_MEDCOMMONS_ID) ;

		//log.info("In put for storageId " + storageId);
		RepositoryMetadataHandler metadataHandler = new RepositoryMetadataHandler();
		
		// Need to t
		DICOMThumbnailGenerator t = new DICOMThumbnailGenerator();
		metadataHandler.setThumbnailGenerator(t);
		Key key = null;

		SecretKeySpec decryptionKey = null;

		try {
			List<RegistryParameters> requestParameters = putRequest.getRegistryParameters();
			
			String providedToken = getMedCommonsParameter(requestParameters, AUTHORIZATION_TOKEN);
			if(!blank(providedToken)) {
			    log.info("Using provided auth token " + authToken);
			    authToken = providedToken;
			}
			ServicesFactory servicesFactory= getServiceFactory(authToken);
			
			displayRegistryParameters(requestParameters);
			registrySecret = getMedCommonsParameter(requestParameters,
					REGISTRY_SECRET);
			if (registrySecret != null) {
				log.info("Incoming registry secret set to " + registrySecret);
			}

			senderGroupId = getMedCommonsParameter(requestParameters,
					SenderProviderId);
			log.debug("senderGroupId:" + senderGroupId);
			log.info("Storage id = " + storageId + ", senderGroupId=" + senderGroupId + ", SenderProviderId=" + SenderProviderId);
			paymentBypassToken = getMedCommonsParameter(requestParameters, PaymentBypassToken);
			
			log.info(PaymentBypassToken + " value is " + paymentBypassToken + " auth is " + authToken);
			
			String mergeVal = getMedCommonsParameter(requestParameters, MergeCCR);
			if (!Str.blank(mergeVal)){
			    mergeCCR = CXPConstants.MergeCCRValues.valueOf(mergeVal);
			}
			// Logic here needs to get improved in the future.
			// If there is a payment bypass token test to see if it is equal to the 
			if ((paymentBypassToken != null) && (authToken != null) && (authToken.equals(paymentBypassToken))){
			    bypassPayment = true;
			    log.info("CXP PUT Payment bypass is set to true");
			}
			else{
			    log.info("Bill payment is active");
			}
			
			
			if (storageId.equals("-1"))
			    throw new RuntimeException("StorageID is set to '-1' for account creation. This option is no longer supported");
			
			accountGroupId = resolveGroupAccountID(servicesFactory, senderGroupId, storageId);
			
			log.info("Number of documents to put:" + documents.size());
			if (documents.size() < 1){
				throw new IllegalStateException("Number of documents uploaded via CXP must be at least one; the number present was " +
						documents.size());
			}

			// ssadedin: we will store the documents below and accumulate charges as we go
			// (which may be none, if there is no billable content).
			// At the end we will execute the charge - this makes sure as best we can that 
			// we don't charge people if for some reason their transaction fails.
			BillingCharge billingCharge = null;
		
			/*
			 * Need to change logic. Read in all documents. there are two
			 * states: - in a compound document - not in a compound document.
			 *
			 * if you're in a compound document and the parent is the same as
			 * the last parent -then it's the same document. Otherwise - no.
			 *
			 * Need routines: Start compound/endcompound. Start
			 * simple/endsimple.
			 */
			boolean newDocument = true;
			boolean compoundDocument = false;
			String lastParentname = null;

			if(Str.blank(storageId))
				throw new RuntimeException("Missing storageId from CXP");

			GatewayRepository repository =  new GatewayRepository(authToken, nodeId, encryptionEnabled, backupEnabled);
            
			Set<String> trackingNumbers = new HashSet<String>();
			List<String> documentsImported = new ArrayList<String>();
            for(Document doc : documents) {
				DataHandler source = doc.getData();
				try {
					InputStream is = new BufferedInputStream(source.getInputStream());

					log.debug("PUT: input: description=" + doc.getDescription()
							+ ", name = " + doc.getDocumentName()
							+ "\n parent=" + doc.getParentName()
							+ " content type " + doc.getContentType());

					// Thread.sleep(10);
					
					compoundDocument = isCompoundDocument(doc);
					newDocument = isNewDocument(lastParentname, doc.getParentName());

					if (newDocument && (transactionDescr != null)) {
						DocumentDescriptor compoundDocumentDescriptor = 
						    repository.finalizeCompoundDocument(transactionDescr.getTransactionHandle());

						documentState.setResponseGuids(compoundDocumentDescriptor);
						transactionDescr = null;
					}
					
					docDescriptor = createDocumentDescriptor(storageId, doc, metadataHandler);
					if (newDocument && compoundDocument) {
						// key isn't calculated until the first file in compound
						// document is PUT.
						transactionDescr = repository.initializeCompoundDocument(docDescriptor);
					} 
					else 
					if(compoundDocument) {
						docDescriptor.setTransactionHandle(transactionDescr.getTransactionHandle());
						docDescriptor.setKey(key);
						docDescriptor.setDecryptionKey(decryptionKey);
					}
					
                    if(billingCharge == null && "application/dicom".equals(doc.getContentType()) && !bypassPayment) {
                        billingCharge = generateBillingCharge(servicesFactory,docDescriptor);
                    }
					
                    // Store bytes to disk. The document
                    // has not been validated in any way.
                    repository.putInputStream(docDescriptor, is);
					if (newDocument && compoundDocument) {
						newDocument = false;

						// Store these for use in subsequent
						// files in the same compound document.
						key = docDescriptor.getKey();
						decryptionKey = docDescriptor.getDecryptionKey();

					}
					log.debug("CXP PUT complete for file: "
							+ documentDescriptorDisplay(docDescriptor));

					is.close();
					totalBytes+=docDescriptor.getLength();

					Document responseDoc = new Document();

					responseDoc
							.setDocumentName(docDescriptor.getDocumentName());
					responseDoc.setContentType(docDescriptor.getContentType());

					responseDoc.setGuid(docDescriptor.getGuid());
					responseDoc.setSha1(docDescriptor.getSha1());
					responseDoc.setParentName(doc.getParentName());
					documentState.add(responseDoc, compoundDocument);
					String mimeType = responseDoc.getContentType();
					String trackingNumber = null;
					if (DocumentTypes.CCR_MIME_TYPE.equals(responseDoc.getContentType())) {
					    
						trackingNumber = transactionUtils.generateConfirmationCode(
						        servicesFactory,
								responseDoc.getContentType(),
								storageId,
								responseDoc.getGuid(),
								registrySecret, PIN.SHA1Hasher);

					    RegistryParameters params =
                            trackingNumberParameters( storageId, trackingNumber, registrySecret);

						newDocumentEvent(servicesFactory,storageId, responseDoc, trackingNumber, mergeCCR);
						
						documentsImported.add(responseDoc.getGuid());

						if (params != null) {
							log.info(registryParametersToString(params));
							resp.getRegistryParameters().add(params);
							generatedReturnParameters  = true;
						}

						if ((senderGroupId != null) &&(accountGroupId != null)){
							CCRDocument ccr = parseCCR(repository, docDescriptor);
							AccountSettings accountSettings = servicesFactory.getAccountService().queryAccountSettings(senderGroupId);
							if ((accountSettings.getRegistry() != null) && (!"".equals(accountSettings.getRegistry()))) // Only send if registry is configured for this user
							    notifyRegistry(servicesFactory,storageId, responseDoc.getGuid(), trackingNumber, accountSettings, ccr);

							// This is ugly.
							// Basically - if there is DICOM, mark it available for downloading.
							// Otherwise - skip.
							ccr.parseReferences();
							if (!Str.blank(accountGroupId)){
							    List series = (List) ccr.getSeriesList();
								if((series != null) && (series.size() > 0)) {
									String workflowType = "Download Status";
									String workflowStatus = "Available";
									setWorklistDownloadState(
									        servicesFactory,
											accountGroupId,
											storageId,
											responseDoc.getGuid(),
											workflowType,
											workflowStatus);
								}
								else {
									log.info("No dicom available for ccr " + guid + ", download state not set");
								}
							}
							else{
								log.info("No group set for senderId " + senderGroupId + ", download state not set");
							}
						}
						else{
							log.info("senderGroupId is null; no worklist entry created for " + responseDoc.getGuid());
						}
					}
					else {
					    // non-CCR documents still need to be registered
					    servicesFactory.getDocumentService().addDocument(storageId, docDescriptor.getGuid());
					}

					lastParentname = doc.getParentName();
					
	                
				} finally {
					AttachmentDataSource attSource = (AttachmentDataSource) source
							.getDataSource(); // Delete the file if there is
												// one
					File attFile = attSource.getFile();
					if (attFile != null){
						//log.info("Deleting temporary xfire file:" + attFile.getAbsolutePath());
						attFile.delete();
					}
				}

			} // End of processing documents.

            this.notifyTransactionListeners(requestParameters, documentsImported);
            
			if (transactionDescr != null) {
				DocumentDescriptor compoundDocumentDescriptor = repository
						.finalizeCompoundDocument(transactionDescr
								.getTransactionHandle());

				documentState.setResponseGuids(compoundDocumentDescriptor);
				transactionDescr = null;
			}
			resp.getDocinfo().addAll(documentState.responseDocuments);
			
            // Now bill for the transaction, if necessary
            if(billingCharge != null) {
                servicesFactory.getBillingService().charge(billingCharge);
            }

            // Add to activity log for storage id
			// Show first tn - may need better logic later
            String activityTrackingNumber = trackingNumbers.isEmpty()?"":trackingNumbers.iterator().next();
            ActivityEvent evt =
                new ActivityEvent(ActivityEventType.CXP_IMPORT, "Uploaded " + documents.size() + " documents via CXP", 
                        new AccountSpec(storageId), storageId, activityTrackingNumber, registrySecret);
            evt.setCharge(billingCharge);
            servicesFactory.getActivityLogService().log(evt);
            
			if (!generatedReturnParameters) {
				RegistryParameters parameters = generateReturnParameters(storageId);
				resp.getRegistryParameters().add(parameters);
			}

			resp.setStatus(200);
			resp.setReason("OK");

		} catch (ServiceException e) {
			log.error("Failed saving document "
					+ documentDescriptorDisplay(docDescriptor), e);
			resp.setStatus(500);
			resp.setReason(e.getLocalizedMessage());
		} catch (TransactionException e) {
			log.error("Failed saving document "
					+ documentDescriptorDisplay(docDescriptor), e);
			resp.setStatus(500);
			resp.setReason(e.getLocalizedMessage());
		}
		/*
		 * catch(ServiceException e){ log.error("Failed saving document"+
		 * documentDescriptorDisplay(docDescriptor), e); resp.setStatus(500);
		 * resp.setReason(e.getLocalizedMessage()); }
		 */
		catch (IOException e) {
			log.error("Failed saving document "
					+ documentDescriptorDisplay(docDescriptor), e);
			resp.setStatus(500);
			resp.setReason(e.getLocalizedMessage());
		} catch (Exception e) {
			log.error("Failed saving document "
					+ documentDescriptorDisplay(docDescriptor), e);
			resp.setStatus(500);
			resp.setReason(e.getLocalizedMessage());
		} catch(Error e){
		    // Hm. Catching an error is pretty extreme - but there have been a few
		    // (such as ClassNotFoundError) which lead to incomprehensible error 
		    // messages on the other side. Here - if we include the documentDescriptorDisplay
		    // then someone sending us a client-side trace will get a much more
		    // focussed response from us.
		    log.error("Failed saving document "
                    + documentDescriptorDisplay(docDescriptor), e);
		    Throwable cause = e.getCause();
		    if (cause != null){
		        log.error("Caused by ", cause);
		    }
            resp.setStatus(500);
            resp.setReason("Internal server error:" + e.getLocalizedMessage());
		}
		finally {

			long endTime = System.currentTimeMillis();
			log.info(PerformanceMeasurement.throughputString("CXP PUT", (endTime - startTime), totalBytes));

		}

		return (resp);
	}

    private String resolveGroupAccountID(ServicesFactory servicesFactory, String senderGroupId,
            String storageId) throws ServiceException {
        String accountGroupId;
        // if senderGroupId is null -what should it be set to?
        if (senderGroupId != null){
            AccountSettings senderAccountSettings = getAccountSettings(servicesFactory, senderGroupId);
            if (senderAccountSettings == null){
                // Should throw a typed exception here - I'm sure we want to catch
                // this and handle in a clean way.
                throw new NullPointerException("AccountSettings for " + senderGroupId + " are null");
            }
            accountGroupId = senderAccountSettings.getGroupId();
        }
        else {
            log.info("getting account settings for storage id " + storageId);
            AccountSettings senderAccountSettings = getAccountSettings(servicesFactory, storageId);
            
            if (senderAccountSettings == null){
                // Should throw a typed exception here - I'm sure we want to catch
                // this and handle in a clean way.
                throw new NullPointerException("AccountSettings for " + senderGroupId + " are null");
            }
            accountGroupId = senderAccountSettings.getGroupId();
        }
        return accountGroupId;
    }

	/**
	 * Set the guid of the response document as a response to the referrer guid.
	 * <p>
	 * This is a hack that allows the concurrently running /router web context
	 * to know that a this CCR has arrived and to update the user interface
	 * with data from the transaction.
	 * 
	 * @param requestParameters
	 * @param responseDoc
	 */
	@SuppressWarnings("unchecked")
    private void notifyTransactionListeners(List<RegistryParameters> requestParameters, List<String> guids) {
	    
        Hashtable transactions = Transactions.transactions;
        String referrerGuid = getMedCommonsParameter(requestParameters, CXPConstants.REFERRER_GUID);
        if(referrerGuid!=null) {
            List<String> transactionGuids = Transactions.transactions.get(referrerGuid);
            if(transactionGuids == null)
                transactions.put(referrerGuid, guids);
            else {
                synchronized(transactionGuids) {
                    transactionGuids.addAll(guids);
                }
            }
        }
    }

    /**
	 * Attempt to generate a charge for the given document descriptor 
	 * @throws ServiceException 
	 */
	private BillingCharge generateBillingCharge(ServicesFactory servicesFactory, DocumentDescriptor docDescriptor) throws ServiceException {
	    
	    if(!Configuration.getProperty("EnableBilling", false))
	        return null;
	    
	    // Resolve the candidate list of accounts
	    AccountSpec principal = servicesFactory.getDocumentService().queryPrincipal();
	    List<String> accounts = new ArrayList<String>();
	    if(principal != null && !blank(principal.getMcId())) {
	        AccountSettings settings = getAccountSettings(servicesFactory,principal.getMcId());
	        
	        // If it exists we prioritize the group's account id 1st
	        if(!blank(settings.getGroupId())) {
	            accounts.add(settings.getGroupId());
	        }
	        
	        // Then the individual's account
	        accounts.add(principal.getMcId());
	    }
	    
	    // Finally the patient's account
	    accounts.add(docDescriptor.getStorageId()); 
	    
	    BillingService billingService = servicesFactory.getBillingService();
	    List<BillingCharge> charges = billingService.resolvePayer(accounts, Collections.singleton(new BillingEvent(BillingEventType.INBOUND_DICOM)));
	    
	    if(charges.isEmpty())
	        throw new InsufficientCreditException("None of accounts [" + join(accounts,",") + "] have sufficient credit to accept inbound DICOM");
	    
	    log.info("Resolved billing charge " + charges.get(0));
	    
	    // Always use the first charge
        return charges.get(0);
    }

    public DeleteResponse delete(org.cxp2.DeleteRequest DeleteRequest) {
		log.info("In delete" + DeleteRequest.getStorageId());
		
		newRequest();

		DeleteResponse resp = new DeleteResponse();
		resp.setCxpVersion("2.0");
		resp.setStatus(500);
		resp.setReason("ServerFailure");
		DocumentDescriptor docDescriptor = null;
		 
		try {
		    List<RegistryParameters> requestParameters = DeleteRequest
            .getRegistryParameters();
		    String authToken = getMedCommonsParameter(requestParameters, AUTHORIZATION_TOKEN);
		    GatewayRepository repository =  new GatewayRepository(authToken, nodeId, encryptionEnabled, backupEnabled);
		       
			List<Document> documents = DeleteRequest.getDocinfo();
			log.info("Number elements in list:" + documents.size());
			Iterator<Document> docs = documents.iterator();
			while (docs.hasNext()) {
				Document doc = docs.next();
				docDescriptor = createDocumentDescriptor(DeleteRequest
						.getStorageId(), doc);

				repository.delete(docDescriptor);
				log.info("CXP DELETE complete: "
						+ documentDescriptorDisplay(docDescriptor));

			}
			resp.setStatus(200);
			resp.setReason("OK");

		} catch (Exception e) {
			resp.setStatus(500);
			resp.setReason("ServerFailure:" + e.getLocalizedMessage());
			log.error("Failed to delete document:"
					+ documentDescriptorDisplay(docDescriptor), e);
		}

		return (resp);
	}




	private String documentDescriptorDisplay(DocumentDescriptor docDescriptor) {
		StringBuffer buff = new StringBuffer();
		if (docDescriptor == null)
			buff.append("DocumentDescriptor is null");
		else {
			buff.append("DocumentDescriptor[storageId=");
			buff.append(docDescriptor.getStorageId());
			buff.append(",guid=");
			buff.append(docDescriptor.getGuid());
			buff.append(",sha1=");
			buff.append(docDescriptor.getSha1());
			buff.append(",name=");
			buff.append(docDescriptor.getDocumentName());
			buff.append("]");

		}
		return (buff.toString());
	}

	private RegistryParameters generateResponseLength(long byteCount){
		RegistryParameters params = new RegistryParameters();
		params.setRegistryName(MEDCOMMMONS_REGISTRY);
		params.setRegistryId(MEDCOMMMONS_REGISTRY_ID);
		Parameter param = new Parameter();
		param.setName(LENGTH);
		param.setValue(Long.toString(byteCount));
		params.getParameters().add(param);

		return(params);
	}


	private void newDocumentEvent(ServicesFactory servicesFactory, String storageId, Document responseDoc, String confirmationCode, CXPConstants.MergeCCRValues mergeIncomingCCR)
			throws Exception {
	        RLSHandler rlsHandler = new RLSCXPHandler();
		
			rlsHandler.newDocumentEvent(servicesFactory, storageId, responseDoc.getGuid(),confirmationCode, mergeIncomingCCR,"11111", "Incoming CXP", "UNKNOWN");
	}
	
	protected long calculateRequestSize(GatewayRepository repository, org.cxp2.GetRequest getRequest) throws IOException{
		long totalBytes = 0;

		List<Document> documents = getRequest.getDocinfo();
		Iterator<Document> iter = documents.iterator();
		DocumentDescriptor docDescriptor = null;

		while (iter.hasNext()) {
			Document requestedDoc = iter.next();
			if (requestedDoc.getGuid() == null)
				throw new NullPointerException("Null GUID in document:"
						+ requestedDoc);
			docDescriptor = createDocumentDescriptor(getRequest
					.getStorageId(), requestedDoc);

			/**
			 * String filename = requestedDoc.getGuid(); log.info("About to
			 * get document " + filename); File f = new File(filename); if
			 * (!f.exists()) { throw new
			 * FileNotFoundException(f.getAbsolutePath()); } FileDataSource
			 * fileSource = new FileDataSource(f); DataHandler dh = new
			 * DataHandler(fileSource); Conceptual problem: If return
			 * inputstream -assuming that it's a single document. If
			 * document is a compound doc - then we really want to iterate
			 * here over the contents.
			 */
			// This is where a FileNotFoundException is thrown if
			// guid does not exist.
			Properties props = repository.getMetadata(docDescriptor);
			String sBytes = props.getProperty(RepositoryFileProperties.LENGTH);
			if (sBytes != null){
				long nBytes = Long.parseLong(sBytes.trim());
				totalBytes+=nBytes;
			}
			else{
				throw new RuntimeException("Missing byte count from property file for " + docDescriptor);
			}
		}

		return(totalBytes);
	}

	public  RegistryParameters trackingNumberParameters( String storageId,
			String confirmationCode, String registrySecret) throws ServiceException {

		String rSecret = registrySecret;

		RegistryParameters params = new RegistryParameters();

		params.setRegistryName(MEDCOMMMONS_REGISTRY);
		params.setRegistryId(MEDCOMMMONS_REGISTRY_ID);

		if (rSecret != null) {
			Parameter param = new Parameter();
			param.setName(REGISTRY_SECRET);
			param.setValue(rSecret);
			params.getParameters().add(param);
		}

		if (confirmationCode != null) {
			Parameter param = new Parameter();
			param.setName(CONFIRMATION_CODE);
			param.setValue(confirmationCode);
			params.getParameters().add(param);
		}

		Parameter versionParam = new Parameter();
		versionParam.setName(VERSION);
		versionParam.setValue(CXP_VERSION);
		params.getParameters().add(versionParam);

		Parameter storageIdParameter = new Parameter();
		storageIdParameter.setName(STORAGE_ID);
		storageIdParameter.setValue(storageId);
		params.getParameters().add(storageIdParameter);

		return (params);
	}
	/**
	 * Generates a simple Registry Parameter block for returns.
	 * Always includes the storage id.
	 * @param storageId
	 * @return
	 * @throws ServiceException
	 */
	public  RegistryParameters generateReturnParameters( String storageId
			) throws ServiceException {



		RegistryParameters params = new RegistryParameters();

		params.setRegistryName(MEDCOMMMONS_REGISTRY);
		params.setRegistryId(MEDCOMMMONS_REGISTRY_ID);




		Parameter versionParam = new Parameter();
		versionParam.setName(VERSION);
		versionParam.setValue(CXP_VERSION);
		params.getParameters().add(versionParam);

		Parameter storageIdParameter = new Parameter();
		storageIdParameter.setName(STORAGE_ID);
		storageIdParameter.setValue(storageId);
		params.getParameters().add(storageIdParameter);

		return (params);
	}
	protected String registryParametersToString(
			RegistryParameters registryParameters) {
		StringBuffer buff = new StringBuffer("RegistryParameters[");
		buff.append("id=");
		buff.append(registryParameters.getRegistryId());
		buff.append(",name=");
		buff.append(registryParameters.getRegistryName());
		List<Parameter> parameters = registryParameters.getParameters();
		if (parameters.size() > 0) {
			buff.append("\nParameters[");
			for (int i = 0; i < parameters.size(); i++) {
				Parameter p = parameters.get(i);
				buff.append("\n Name=");
				buff.append(p.getName());
				buff.append(", Value=");
				buff.append(p.getValue());

			}
			buff.append("  ],\n");
		} else {
			buff.append("\n Parameters=null,");
		}
		buff.append("]");
		return (buff.toString());
	}
	/**
	 * Metadata handlers only used (currently) with PUT; it's a no-op in other
	 * contexts
	 *
	 * @param storageId
	 * @param doc
	 * @return
	 */
	private DocumentDescriptor createDocumentDescriptor(String storageId,
			Document doc) {
		return (createDocumentDescriptor(storageId, doc, null));
	}
	/**
	 * Creates a DocumentDescriptor (a repository metadata object) from a CXP
	 * Document metadata object. These two objects are nearly identical but
	 * serve different purposes:
	 * <UL>
	 * <LI> The document classes are in the org.cxp2 package; there may be
	 * non-CXP-aware classes invoking the repository</LI>
	 * <LI> The document class contains a data object whose stream may or may
	 * not be appropriate for the respository. </LI>
	 * </UL>
	 *
	 * @param storageId
	 * @param doc
	 * @return
	 */
	private DocumentDescriptor createDocumentDescriptor(String storageId,
			Document doc, MetadataHandler metadataHandler) {
		String parentName = doc.getParentName();
		DocumentDescriptor docDescriptor = null;
		if (parentName != null) {
			docDescriptor = new CompoundDocumentDescriptor();
			((CompoundDocumentDescriptor) docDescriptor)
					.setParentName(parentName);

		} else
			docDescriptor = new SimpleDocumentDescriptor();
		// if (doc.getGuid()==null){
		// throw new NullPointerException("Null GUID specified in document");
		// }
		docDescriptor.setMetadataHandler(metadataHandler);
		docDescriptor.setContentType(doc.getContentType());
		docDescriptor.setDocumentName(doc.getDocumentName());
		docDescriptor.setGuid(doc.getGuid());
		docDescriptor.setStorageId(storageId);
		docDescriptor.setSha1(doc.getSha1());
		docDescriptor.setMetadataHandler(metadataHandler);
		return (docDescriptor);

	}


	/**
	 * Returns a value from the incoming MedCommons registry parameter block.
	 * Typical values:
	 * <ul>
	 * <li> REGISTRY_SECRET
	 * <li> CONFIRMATION_CODE
	 *
	 * </ul>
	 *
	 * @param registryParameters
	 * @return
	 */
	protected String getMedCommonsParameter(
			List<RegistryParameters> registryParameters, String name) {
		String value = null;
		if (registryParameters == null)
			return null;
		for (int i = 0; i < registryParameters.size(); i++) {
			RegistryParameters r = registryParameters.get(i);
			if (r.getRegistryId().equals(MEDCOMMMONS_REGISTRY_ID)) {
				List<Parameter> params = r.getParameters();
				for (int j = 0; j < params.size(); j++) {
					Parameter p = params.get(j);
					if (p.getName().equals(name)) {
						value = p.getValue();
						break;
					}

				}
			}
		}
		return (value);
	}
	public void displayRegistryParameters(List<RegistryParameters> registryParameters){

		if (registryParameters == null){
			log.info("Null RegistryParameters");
		}
		if (registryParameters.size() == 0){
			log.info("There are zero registry parameters specied in List");
		}
		log.info("Registry Parameters: Size =" + registryParameters.size());
		for (int i=0;i<registryParameters.size(); i++){
			RegistryParameters r = (RegistryParameters) registryParameters.get(i);
			log.info("Registry Parameters:" + r.getRegistryId() + "," + r.getRegistryName());
			List<Parameter> params = r.getParameters();
			log.info("Parameter name/value: Size=" + params.size());
			for (int k=0;k<params.size();k++){
				Parameter p = params.get(k);
				log.info("  Parameter name=" + p.getName() + ", value=" + p.getValue());
			}

		}
	}
	 /**
	 * Get the account settings for this account id.
	 */
	private AccountSettings getAccountSettings(ServicesFactory servicesFactory, String accountId) throws ServiceException{
	    AccountSettings settings = servicesFactory.getAccountService().queryAccountSettings(accountId);
         return(settings);
	}

    /**
     * Creates a MedCommons storage id account.  No check is performed to see if an account already exists.
     *
     * @throws CCRStoreException
     */
	/*
    public String createStorageIdAccount(AccountSettings accountSettings) throws CCRStoreException {
    	String storageId = null;
        try {
        	String patientGivenName = "";
        	String patientFamilyName = "";
        	String patientEmail = null;

            AccountCreationService acctCreationService = getServiceFactory().getAccountCreationService();
            Set<String> accounts = new HashSet<String>();
            accounts.addAll(accountSettings.getCreationRights());
            storageId = acctCreationService.register(AccountType.PROVISIONAL,
                            patientEmail, /* Note:  after some discussion it was decided passwords
                                     for auto created accounts are best created blank
                                     and code will be added to prevent such accounts
                                     from logging in with a support message * /
                            null, patientGivenName, patientFamilyName, "", null, accountSettings.getRegistry(), null, null, null)[0];

            // Req. 2.1 Authorization to Account Creator
            accounts.add(accountSettings.getAccountId());
            if(accountSettings.getAccountId()!=null) {
                getServiceFactory().getDocumentService().grantAccountAccess(storageId, Str.join(accounts.toArray(), ","), Rights.ALL);
            }
        }
        catch (ServiceException e) {
            throw new CCRStoreException(e);
        }
      return(storageId);
    }
    */

    public void notifyRegistry(ServicesFactory servicesFactory, String storageId, String guid, String trackingNumber, AccountSettings accountSettings, CCRDocument ccr) throws CCRStoreException {
        try {
            String patientIdValue = storageId;
            String patientIdSource = CCRConstants.MEDCOMMONS_PATIENT_ID_TYPE;
            String idp = "idp";


            log.info("about to notifyRegistry of CCR with purpose " +
            		ccr.getDocumentPurpose());
            SecondaryRegistryService srs = servicesFactory.getSecondaryRegistryService();
            Date patientDateOfBirth = ccr.getPatientDateOfBirth();
            //Document doc = ccr.getJDOMDocument();
            srs.addCCREvent(
                            ccr.getPatientGivenName(),
                            ccr.getPatientFamilyName(),
                            ccr.getPatientGender(),
                            patientIdValue,
                            patientIdSource,
                            idp,
                            idp,
                            patientDateOfBirth != null ? patientDateOfBirth.toGMTString() : null,
                            ccr.getPatientAge(),
                            ccr.getTrackingNumber(),
                            "", // PIN no longer sent to registry!
                            guid,
                            ccr.getDocumentPurpose(),
                            "",
                            "MedCommons",
                            Configuration.getProperty("RemoteAccessAddress")+"/access?g="+guid, // viewer url
                            ccr.getPurposeText(),
                            accountSettings.getRegistry()
            );
        }
        catch(PHRException e){
        	throw new CCRStoreException(e);
        }

        catch (ServiceException e) {
        	throw new CCRStoreException(e);
        }
        catch (ConfigurationException e) {
            throw new CCRStoreException(e);
        }

    }
    private CCRDocument parseCCR(GatewayRepository repository, DocumentDescriptor descriptor) throws PHRException, ParseException, TransactionException, IOException, ServiceException, RepositoryException{
    	String storageId = descriptor.getStorageId();
    	String guid = descriptor.getGuid();
    	String ccrXml = getCCRDocument(repository, descriptor);
    	return new CCRDocument(storageId, guid, "CCR", "session://currentCcr", null,
				ccrXml, new ArrayList(), CCRConstants.SCHEMA_VALIDATION_OFF);
    }
	/**
	 * Returns a string containing the CCR.
	 *
	 * @param guid
	 * @return
	 * @throws IOException
	 * @throws RepositoryException
	 */
	private String getCCRDocument(GatewayRepository repository, DocumentDescriptor descriptor) throws IOException,
			RepositoryException, ServiceException, TransactionException {

		StringBuffer buff = new StringBuffer();
		InputStream in = repository.get(descriptor);//getInputstream(guid, guid);

		byte[] buffer = new byte[8 * 1024];
		int n = -1;
		while ((n = in.read(buffer)) >= 0) {
			String s = new String(buffer, 0, n);
			buff.append(s);
		}
		try {
			in.close();
		} catch (Exception e) {
			;
		}

		return (buff.toString());
	}

	/**
	 * Catch all errors locally - setting this state shouldn't kill the
	 * rest of the transaction.
	 * TODO: need to document this.
	 * @param srcAccountId
	 * @param storageId
	 * @param guid
	 * @param workflowType
	 * @param workflowStatus
	 */
	private void setWorklistDownloadState(
	        ServicesFactory servicesFactory,
			String srcAccountId,
			String storageId,
			String guid,
			String workflowType,
			String workflowStatus
			){
		try{
			log.info("setWorklistDownloadState: srcAccountId = " + srcAccountId + ", storageId =" + storageId
					+ ", guid= " + guid + ", workflowType = " + workflowType + ", "
					+ ", workflowStatus = " + workflowStatus);
			if (srcAccountId == null){
				throw new NullPointerException("srcAccountID is null");
			}
			if (guid==null){
				throw new NullPointerException("guid is null");
			}
			if (storageId == null){
				throw new NullPointerException("storageId is null");
			}
			if (workflowType == null){
				throw new NullPointerException("workflowType is null");
			}
			if (workflowStatus == null){
				throw new NullPointerException("workflowStatus is null");
			}
		//	DocumentTypes.CCR_MIME_TYPE.equals(document.getContentType());
			String workflowKey = guid;
			//String workflowType = "Download Status";
			//String workflowStatus = "Available";

			servicesFactory.getAccountService().updateWorkflow(workflowKey, srcAccountId, storageId, workflowType, workflowStatus);

		}
		catch(Exception e){
			log.error("Error setting worklist state ", e);

		}
	}
	
	private void newRequest() {
	   // repository = null;
	    serviceFactory = null;
	}
/*
    GatewayRepository getRepository() throws ServiceException {
        if(repository == null){
            log.info("Initializing repository with auth token" + authToken);
            repository =  new GatewayRepository(authToken, nodeId, encryptionEnabled, backupEnabled);
        }
        return repository;
    }
*/
    ServicesFactory getServiceFactory(String authToken) {
        return(new RESTProxyServicesFactory(authToken));
        
    }
}
