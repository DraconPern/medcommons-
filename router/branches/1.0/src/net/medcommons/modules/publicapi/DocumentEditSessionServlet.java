package net.medcommons.modules.publicapi;

import static net.medcommons.modules.utils.Str.blank;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.cxp.server.MergeClientContext;
import net.medcommons.modules.cxp.server.TransactionUtils;
import net.medcommons.modules.filestore.RepositoryFileProperties;
import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.repository.GatewayRepository;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentRegistration;
import net.medcommons.modules.services.interfaces.MetadataHandler;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.CCRMergeLogic;
import net.medcommons.router.services.ccr.CCRStoreException;
import net.medcommons.router.services.ccr.ForcedLimitedMerge;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.ccrmerge.MergeException;
import net.medcommons.router.services.ccrmerge.preprocess.MarkIncomingCCR;
import net.medcommons.router.services.ccrmerge.preprocess.MarkIncomingHealthFrameCCR;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.sun.xml.bind.StringInputStream;

public class DocumentEditSessionServlet extends HttpServlet implements Constants {
    
    /**
     * Default XML preamble
     */
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("DocumentEditSessionServlet");

	private File transactionDir = null;
	private String nodeId;
	private String remoteAccessAddress;
	private boolean backupEnabled;
	private boolean encryptionEnabled;
	
	public void init() {

		String path = "conf/config.xml";
		String propertiesPath = "conf/MedCommonsBootParameters.properties";
		try {
			Configuration.load(path, propertiesPath);

			nodeId = Configuration.getProperty("NodeID");

			String encryptionConfig = Configuration
					.getProperty("EncryptionEnabled");
			String backupConfig = Configuration.getProperty("Backup_Documents");
			remoteAccessAddress = Configuration
					.getProperty("RemoteAccessAddress");
			if ((encryptionConfig != null) && (!"".equals(encryptionConfig))) {
				encryptionEnabled = Boolean.parseBoolean(encryptionConfig);

			}
			if ((backupConfig != null) && (!"".equals(backupConfig))) {
				backupEnabled = Boolean.parseBoolean(backupConfig);

			}
			
			// framework
		} catch (Exception e) {
			log.error("Unable to load config ", e);
		}
		File data = new File("data");
		try {
			if (!data.exists()) {
				throw new FileNotFoundException("data directory not found:"
						+ data.getAbsoluteFile());

			}
			transactionDir = new File(data, "PHRTransactions");
			if (!transactionDir.exists()) {
				boolean success = transactionDir.mkdir();
				if (!success) {
					throw new IOException("Unable to create directory "
							+ transactionDir.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			log.error("Unable to initial file directory", e);
		}

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		    
		
		String useSchema = useSchema(request);
		SchemaResponse schemaResponse = BasePublicAPIAction.selectSchemaResponse(useSchema);
		try {
		    
			String userAgent = getUserAgent(request);
			log.info("doGet: " + userAgent);
			PHRTransaction transaction = parseParameters(request);
			
			
			GatewayRepository repository = 
				repository = new GatewayRepository(transaction.getOriginalAuthToken(), nodeId,
						encryptionEnabled, backupEnabled);
			//log.info("Getting doc with original guid " + transaction.getOriginalGuid());
			DocumentDescriptor doc = createDocumentDescriptor(
					transaction.getStorageId(), 
					transaction.getOriginalGuid(),
					transaction.getContentType());
			log.info("Got document descriptor for " + transaction.getStorageId() + ", " + 
					transaction.getOriginalGuid());
			Properties props = repository.getMetadata(doc);
			InputStream in = repository.get(doc);
			
			String contentType = props.getProperty(RepositoryFileProperties.CONTENT_TYPE);
			log.info("Repository content type= " + contentType);
			if (in != null){
		       byte buff [] = new byte[4096];
		       int i = 0;
		       response.setContentType(contentType);
		       OutputStream out = response.getOutputStream();
		       while((i = in.read(buff,0, buff.length))!= -1){
		    	  
		    	   out.write(buff, 0, i);
		       }
		       log.info("doGet returned CCR " + doc.getLength() + " bytes");
			}
			else{
				throw new IOException("Null inputstream returned from repository");
			}
			transaction.setTransactionState(TransactionState.DOWNLOADED);
			transaction.setTimeLastActivity(new Timestamp(System.currentTimeMillis()));
			BasePublicAPIAction.writeTransationObject(transaction);
		} 
		catch (TransactionNotFoundException e){
			log.error("Error in GET:" + e.getLocalizedMessage(), e);
		        response.getOutputStream().write(XML_HEADER.getBytes());
			Document doc = generateResponse(500, "Transaction not found", null,
					null);
			generateXMLResponse(response, doc,schemaResponse);
		}
		catch (Exception e) {
			log.error("Error in GET:" + e.getLocalizedMessage(), e);
		        response.getOutputStream().write(XML_HEADER.getBytes());
			Document doc = generateResponse(500, e.getLocalizedMessage(), null,
					null);
			generateXMLResponse(response, doc,schemaResponse);
		}
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response) 
	    throws ServletException, IOException {
	    
		String userAgent = getUserAgent(request);
		log.info("doPut: userAgent="+ userAgent + ", schema= " + useSchema(request));
		
		InputStream is = request.getInputStream();
		BufferedInputStream buff_is = new BufferedInputStream(is);
		
		doUpload(request, response, buff_is);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String xml = request.getParameter("document");
	    StringInputStream s = new StringInputStream(xml);
	    
		String userAgent = getUserAgent(request);
		log.info("doPost: userAgent="+ userAgent + ", schema= " + useSchema(request));
		
		doUpload(request,response, s);
	}

    private void doUpload(HttpServletRequest request, HttpServletResponse response, InputStream is) throws IOException{
        String redirectURL = null;
		String newAuth= null;
		Document doc = null;
		PHRTransaction transaction = null;
		String useSchema = useSchema(request);
		SchemaResponse schemaResponse = BasePublicAPIAction.selectSchemaResponse(useSchema);
		try {
			response.getOutputStream().write(XML_HEADER.getBytes());
			TransactionUtils transactionUtils = new TransactionUtils();
			transaction = parseParameters(request);
			
			DocumentDescriptor docDescriptor = createDocumentDescriptor(
					transaction.getStorageId(), 
					transaction.getOriginalGuid(),
					transaction.getContentType());
			log.info("content type is " + transaction.getContentType());
			log.info(BasePublicAPIAction.getHttpRequestInfo(request));
			GatewayRepository repository = 
				new GatewayRepository(transaction.getOriginalAuthToken(), nodeId, encryptionEnabled, backupEnabled);
			
			repository.putInputStream(docDescriptor, is);
            
			//log.info("doPut transaction.getContentType() " + transaction.getContentType() );
			if (DocumentTypes.CCR_MIME_TYPE.equals(transaction.getContentType())) {
			    UserSession d = new UserSession(transaction.getStorageId(), transaction.getOriginalAuthToken()); 
			    ServicesFactory svc = d.getServicesFactory();
                DocumentRegistration reg = 
			        svc.getTrackingService().registerTrackDocument(transaction.getStorageId(), docDescriptor.getGuid(), null, null, 0L, null);

				String trackingNumber = reg.getTrackingNumber();				
				
				log.info("doPut: incoming CCR trackingNumber: " + trackingNumber + ", guid " +  docDescriptor.getGuid());
				
                MergeClientContext mergeClientContext = new MergeClientContext();
                mergeClientContext.setUserAgent("HealthFrame"); // Hack
                mergeClientContext.setAuth(transaction.getOriginalAuthToken());
                mergeClientContext.setMergeLogic(new ForcedLimitedMerge(svc, docDescriptor.getStorageId()));
                
				if(!transaction.getOriginalReference().matches("[a-z0-9]{40}")) {
				    
				    // The first upload is always a reference export that has no semantic changes
				    // from the downloaded CCR.  This state (DOWNLOADED) is used to trigger
				    // a backup so that the previous version is saved for the user.
				    if(transaction.getTransactionState().equals(TransactionState.DOWNLOADED)) {
				        backupLogicalCCR(d,transaction.getOriginalReference());
				    }
				    
                    CCRDocument ccr = d.resolve(docDescriptor.getGuid());
                    if (mergeClientContext.getUserAgent().equals("HealthFrame")) {
                        MarkIncomingCCR markIncoming = new MarkIncomingHealthFrameCCR();
                        ccr = markIncoming.markIncomingCCR(ccr);
                    }
                    ccr.setStorageId(transaction.getStorageId());
                    
				    mergeLogicalCCR(d, docDescriptor, transaction, ccr);
				}
				else { // Updating a fixed content CCR
				    EnumSet<TransactionState> ignoreStates = EnumSet.of(TransactionState.NEW, TransactionState.UNINITALIZED, TransactionState.DOWNLOADED);
				    if(!ignoreStates.contains(transaction.getTransactionState())) {
				        createFixedTab(d, transaction, docDescriptor, trackingNumber);
				    }
				}
				
				doc = schemaResponse.generateResponse(remoteAccessAddress, transaction,200, "OK", redirectURL, newAuth);
				transaction.setTransactionState(TransactionState.UPDATED);
				transaction.setTimeLastActivity(new Timestamp(System.currentTimeMillis()));
				BasePublicAPIAction.writeTransationObject(transaction);
				
			}
			else{
				doc = schemaResponse.generateResponse(remoteAccessAddress, transaction, 415, "Unsupported content type " + transaction.getContentType(), redirectURL, newAuth);
			}
		}
		catch(Exception e){
			log.error("Exception in PUT " + e.getLocalizedMessage(), e);
			doc = schemaResponse.generateResponse(remoteAccessAddress,transaction,500, e.getLocalizedMessage(), null,null);
			try{
				response.sendError(510, e.toString());
			}
			catch(IOException e2){
				response.setStatus(510);
			}
			return;
		}
		generateXMLResponse(response, doc,schemaResponse);
    }

    private void mergeLogicalCCR(UserSession d, DocumentDescriptor docDescriptor, PHRTransaction editTx, CCRDocument mergeFrom) throws PHRTransactionException {
        
        try {
            // Get the target CCR to merge with
            AccountDocumentType type = AccountDocumentType.valueOf(editTx.getOriginalReference());
            
            log.info("mergeLogicalCCR: merge target type = " + type);
           
            
            CCRDocument mergeTo = d.resolve(d.getAccountSettings().getAccountDocuments().get(type));
            // Set to current ccr 
            StoreTransaction storeTx = d.tx(mergeFrom);
            
            // Default merge logic will not merge if there is no patient id,
            // so we set our own that will allow HealthFrame CCRs that have no patient id
            ForcedLimitedMerge mergeLogic = new ForcedLimitedMerge(d.getServicesFactory(), docDescriptor.getStorageId());
            mergeLogic.setMergeTarget(type);
            storeTx.setMergeLogic(mergeLogic);
            
            // Normally the guid is set by registering the CCR - but the CCR is already stored
            // so skip that and just set the guid manually
            storeTx.setDocumentGuid(mergeFrom.getGuid());
            storeTx.notifyRegistry();
            
            // The DOWNLOADED state indicates that HealthFrame has just downloaded the CCR and
            // no changes have been made yet.  Therefore, do not write activity
            if(editTx.getTransactionState() != TransactionState.DOWNLOADED) {
                storeTx.writeActivity(ActivityEventType.PHR_UPDATE, "External CCR Edit");
            }
            
            log.info("Attempting to forceMerge inbound CCR");
            CCRDocument merged = storeTx.merge();
            if (merged != null) {
                log.info("merged ccr into existing account " + docDescriptor.getStorageId() + ", new currentCCR= " + merged.getGuid());
            }
            else{
                log.info("merged failed");
            }
        }
        catch (ServiceException e) {
            throw new PHRTransactionException("Failed to merge logica CCR of type " + editTx.getOriginalReference() + " for storage id " + docDescriptor.getStorageId());
        }
        catch (ConfigurationException e) {
            throw new PHRTransactionException("Failed to merge logica CCR of type " + editTx.getOriginalReference() + " for storage id " + docDescriptor.getStorageId());
        }
        catch (RepositoryException e) {
            throw new PHRTransactionException("Failed to merge logica CCR of type " + editTx.getOriginalReference() + " for storage id " + docDescriptor.getStorageId());
        }
        catch (PHRException e) {
            throw new PHRTransactionException("Failed to merge logica CCR of type " + editTx.getOriginalReference() + " for storage id " + docDescriptor.getStorageId());
        }
        catch (CCRStoreException e) {
            throw new PHRTransactionException("Failed to merge logica CCR of type " + editTx.getOriginalReference() + " for storage id " + docDescriptor.getStorageId());
        }
        catch (MergeException e) {
            throw new PHRTransactionException("Failed to merge logica CCR of type " + editTx.getOriginalReference() + " for storage id " + docDescriptor.getStorageId());
        }
    }
    
    private void backupLogicalCCR(UserSession d, String originalReference) throws PHRTransactionException {
       try {
           // Load the original
           AccountDocumentType type = AccountDocumentType.valueOf(originalReference);
           CCRDocument ccr = d.resolve(d.getAccountSettings().getAccountDocuments().get(type));
           if (ccr == null){
               log.error("Apparently an error? Null CCR for account");
               return;
           }
           // Make fixed
           ccr.setStorageMode(StorageMode.FIXED);
            
           // Store it
           StoreTransaction tx = d.tx(ccr);
           tx.registerDocument(null);
           tx.storeDocument();
           tx.createFixedTab();
        }
        catch (ServiceException e) {
            throw new PHRTransactionException("Failed to back up old CCR with reference " + originalReference);
        }
        catch (ConfigurationException e) {
            throw new PHRTransactionException("Failed to back up old CCR with reference " + originalReference);
        }
        catch (RepositoryException e) {
            throw new PHRTransactionException("Failed to back up old CCR with reference " + originalReference);
        }
        catch (PHRException e) {
            throw new PHRTransactionException("Failed to back up old CCR with reference " + originalReference);
        }
        catch (CCRStoreException e) {
            throw new PHRTransactionException("Failed to back up old CCR with reference " + originalReference);
        }
    }

    /**
     * For Fixed content CCRs we create a New CCR tab.  
     * 
     * If there is already an existing New CCR then we replace it, saving the old
     * one as a fixed content tab (unlike normal behavior when saving a logical CCR).
     * @throws PHRTransactionException 
     */
    private void createFixedTab(UserSession d, PHRTransaction transaction, DocumentDescriptor desc, String trackingNumber)
    throws PHRTransactionException {
        
        
        try {
            ServicesFactory svc = d.getServicesFactory();
            
            final CCRDocument originalCCR = d.resolve(transaction.getOriginalGuid());
            
            // Add back the Patient ID to the CCR 
            CCRDocument ccr = d.resolve(desc.getGuid());
            ccr.setStorageId(desc.getStorageId());
            
            AccountSettings settings = d.getAccountSettings();
            String oldNewCCRGuid = settings.getAccountDocuments().get(AccountDocumentType.NEWCCR);
            
            StoreTransaction tx = d.tx(ccr);
            
            // Normal merge logic always merges to Current CCR - we want it to 
            // merge to the previous fixed CCR that the user started editing
            CCRMergeLogic mergeLogic = new ForcedLimitedMerge(svc, desc.getStorageId()) {
                @Override
                protected CCRDocument getMergeTarget(CCRDocument ccr, String patientId) throws MergeException {
                    return originalCCR;
                }
            };
            
            mergeLogic.setMergeTarget(AccountDocumentType.NEWCCR);
            CCRDocument merged = mergeLogic.merge(tx);
                 
            if(!blank(oldNewCCRGuid)) {
                svc.getAccountService().addCCRLogEntry(oldNewCCRGuid, 
                            null, "", "", desc.getStorageId(), new Date(),
                            "Replaced New CCR", "Complete", trackingNumber);
            }
        }
        catch (ServiceException e) {
            throw new PHRTransactionException("Unable to save fixed CCR edit " + desc.getGuid() + " with track# " + trackingNumber);
        }
        catch (ConfigurationException e) {
            throw new PHRTransactionException("Unable to save fixed CCR edit " + desc.getGuid() + " with track# " + trackingNumber);
        }
        catch (RepositoryException e) {
            throw new PHRTransactionException("Unable to save fixed CCR edit " + desc.getGuid() + " with track# " + trackingNumber);
        }
        catch (PHRException e) {
            throw new PHRTransactionException("Unable to save fixed CCR edit " + desc.getGuid() + " with track# " + trackingNumber);
        }
        catch (CCRStoreException e) {
            throw new PHRTransactionException("Unable to save fixed CCR edit " + desc.getGuid() + " with track# " + trackingNumber);
        }
        catch (MergeException e) {
            throw new PHRTransactionException("Unable to save fixed CCR edit " + desc.getGuid() + " with track# " + trackingNumber);
        }
    }

	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
	    throws ServletException, IOException {
	    
		String userAgent = getUserAgent(request);
		log.info("doDelete: " + userAgent);
		String useSchema = useSchema(request);
		SchemaResponse schemaResponse = BasePublicAPIAction.selectSchemaResponse(useSchema);
		PHRTransaction transaction = null;
	        response.getOutputStream().write(XML_HEADER.getBytes());
		Document doc = null;
		try{
			transaction = parseParameters(request);
			transaction.setTransactionState(TransactionState.COMPLETE);
			log.info("Should update activity log - session " +
					transaction.getToken() + " should be closed ");
			// No deletion until transactions clearer.
			//deleteTransaction(transaction);
			doc = schemaResponse.generateResponse(remoteAccessAddress,transaction, 200, "OK", null,
						null);
			transaction.setTimeLastActivity(new Timestamp(System.currentTimeMillis()));
			BasePublicAPIAction.writeTransationObject(transaction);
		}
		catch(Exception e){
			log.error("Exception in Delete " + e.getLocalizedMessage(), e);
			doc = schemaResponse.generateResponse(remoteAccessAddress,transaction, 500, e.getLocalizedMessage(), null,
						null);
			response.sendError(511, e.getLocalizedMessage());
			return;
		}
		generateXMLResponse(response, doc, schemaResponse);
	}

	protected ServicesFactory getServicesFactory(String auth) {
		return (new RESTProxyServicesFactory(auth));
	}

	/**
	 * Deletes the transaction.
	 * 
	 * @param transaction
	 */
	protected void deleteTransaction(PHRTransaction transaction){
		if (transaction.getToken() != null) {
			String token = transaction.getToken();
			File f = new File(transactionDir, token);
			if (!f.exists()) {
				throw new TransactionNotFoundException(token);
			}
			else{
				boolean success = f.delete();
				if (!success){ 
					throw new RuntimeException("Transaction can not be deleted for token " + 
							token);
				}
			}
		}
	}
	
	protected String useSchema(HttpServletRequest r){
		String s = r.getParameter("useSchema");
		return(s);
	}
	
	/**
	 * Inspects the given request for a transaction token and attempts to 
	 * load that transaction token from the persistent store.
	 * <p>
	 * Also validates that the loaded transaction has permission to store
	 * into the specified account.
	 */
	protected PHRTransaction parseParameters(HttpServletRequest r) throws IOException, ClassNotFoundException {
		String token = r.getParameter("token");
		if (Str.blank(token)){
			throw new IllegalArgumentException("No token specified in HTTP request");
		}
		PHRTransaction transaction = BasePublicAPIAction.readTransactionObject(token);
		
		if (!token.equals(transaction.getToken())){	// Small reality test.
			log.error("Token mismatch: HTTP request specifies '" + token + "' but stored value is '"
					+ transaction.getToken() + "'");
			// Don't report the tokens back to the user - this might aid an attack
			// on the system.
			throw new RuntimeException("Token specified in HTTP request does not match internal value" );
			
		}
	
		try {
			if (transaction.getOriginalAuthToken() != null) {
				String auth = transaction.getOriginalAuthToken();
				String storageId = transaction.getStorageId();
				UserSession desktop = new UserSession(storageId, auth,
						new ArrayList<CCRDocument>());
				if (!desktop.checkPermissions(storageId, "W"))
					throw new PermissionRefusedException(auth);
			}
		} 
		catch (ServiceException e) {
			String message = "Error checking permissions for storage id "
					+ transaction.getStorageId() + " with authorization "
					+ transaction.getOriginalAuthToken();
			log.error(message, e);
			throw new RuntimeException(message, e);
		}

		return (transaction);
	}

	protected Document generateResponse(int status, String reason,
			String redirectURL, String newAuth) {

		log.info("updatePHR response status=" + status + ", reason = " + reason
				+ ", redirectURL = " + redirectURL);
		Element root = new Element("healthbook");
		Document doc = new Document(root);

		Element statusElement = new Element("status");
		statusElement.setText(status + "");
		root.addContent(statusElement);

		Element reasonElement = new Element("reason");
		reasonElement.setText(reason);
		root.addContent(reasonElement);

		if (redirectURL != null) {
			Element redirectElement = new Element("redirect");
			redirectElement.setText(redirectURL);
			root.addContent(redirectElement);
		}
		if (newAuth != null) {
			Element newAuthElement = new Element("authorizationToken");
			newAuthElement.setText(newAuth);
			root.addContent(newAuthElement);
		}
		return (doc);
	}


	protected void generateXMLResponse(HttpServletResponse response,Document doc, SchemaResponse schemaResponse) throws IOException {
		XMLOutputter serializer = new XMLOutputter();
		StringWriter sOut = new StringWriter();
		String sDoc;
		SAXBuilder builder = null;
		boolean error = true;
			
		
		try {
			builder = schemaResponse.borrowObject();
			serializer.output(doc, sOut);
			sDoc = sOut.getBuffer().toString();
			StringReader reader = new StringReader(sDoc);
			builder.build(reader);

			sOut = new StringWriter();
			serializer.output(doc.getRootElement(),sOut);
			sDoc = sOut.getBuffer().toString();
			byte[] bDoc = sDoc.getBytes();
			response.setContentType(MIME_TYPE_XML);
			response.getOutputStream().write(bDoc);
			log.info("response:" + sDoc);
			error = false;

		}
		// Error trapping of last resort - perhaps there is an internal problem with
		// the JDOM
		// document.
		// TODO: Fix syntax here to be new schema
		catch (IOException e) {
			log.error(
					"Error generating XML response for doc " + doc.toString(),
					e);
			
			response.sendError(500, e.getLocalizedMessage());
			
		}

		catch (RuntimeException e) {
			log.error("Error generating XML response for doc " + doc, e);
			
			response.sendError(501, e.getLocalizedMessage());
		}
		catch(Exception e){
			log.error("Error generating XML response for doc " + doc, e);
		
			response.sendError(502, e.getLocalizedMessage());
		}
		finally {
			try{
				if (!error)
					schemaResponse.returnObject(builder);
			}
			catch(Exception e){
				log.error("Error generating XML response for doc " + doc, e);
				response.sendError(503, e.getLocalizedMessage());
			}
			
		}
	}
	private String getUserAgent(HttpServletRequest request){
		return(request.getHeader("User-Agent"));
	}
    
    /**
     * At the moment - only handle simple documents, not compound ones
     * like DICOM series. 
     * @param storageId
     * @param guid
     * @return
     */
    public DocumentDescriptor createDocumentDescriptor(String storageId,
			String guid, String contentType) {
    	MetadataHandler metadataHandler = null; // SWD: TODO: Not sure this is right.
    	log.info("createDocumentDescriptor " + storageId + " " + guid + " " + contentType);
		DocumentDescriptor docDescriptor = null;
		docDescriptor = new SimpleDocumentDescriptor();
		
		docDescriptor.setMetadataHandler(metadataHandler);
		docDescriptor.setContentType(contentType);
		docDescriptor.setStorageId(storageId);
		//docDescriptor.setSha1(guid);
		docDescriptor.setGuid(guid);
		return (docDescriptor);

	}
    protected Document generateSchemaDoc(){
		Namespace xsiNS = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema-instance");
		
		Namespace defaultNamespace = Namespace.getNamespace("http://www.medcommons.net/editsession01");
		
		Element root = new Element("EditSession",defaultNamespace);
	
		root.setAttribute("schemaLocation",
				defaultNamespace.getURI() + " " + 
				remoteAccessAddress + "/EditSession10.xsd",
				xsiNS);
		Document doc = new Document(root);
		return(doc);
	}
}
