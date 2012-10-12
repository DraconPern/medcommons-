/*
 * $Id$
 * Created on 4/10/2005
 */
package net.medcommons.cxp;

import static net.medcommons.cxp.CXPConstants.COMMAND_DELETE;
import static net.medcommons.cxp.CXPConstants.COMMAND_QUERY;
import static net.medcommons.cxp.CXPConstants.COMMAND_TRANSFER;
import static net.medcommons.cxp.CXPConstants.COMMAND_UNDEFINED;
import static net.medcommons.cxp.CXPConstants.CXP_FILECONTENTS;
import static net.medcommons.cxp.CXPConstants.CXP_FILENAME;
import static net.medcommons.cxp.CXPConstants.CXP_FILES;
import static net.medcommons.cxp.CXPConstants.CXP_FILETYPE;
import static net.medcommons.cxp.CXPConstants.CXP_INFORMATION_SYSTEM;
import static net.medcommons.cxp.CXPConstants.CXP_INFORMATION_SYSTEM_NAME;
import static net.medcommons.cxp.CXPConstants.CXP_INFORMATION_SYSTEM_TYPE;
import static net.medcommons.cxp.CXPConstants.CXP_INFORMATION_SYSTEM_VERSION;
import static net.medcommons.cxp.CXPConstants.CXP_OPCODE;
import static net.medcommons.cxp.CXPConstants.CXP_QUERYSTRING;
import static net.medcommons.cxp.CXPConstants.CXP_SHA1;
import static net.medcommons.cxp.CXPConstants.CXP_TXID;
import static net.medcommons.cxp.CXPConstants.CXP_Version;
import static net.medcommons.cxp.CXPConstants.STATUS_INVALID_QUERY_STRING;
import static net.medcommons.cxp.CXPConstants.STATUS_MISSING_PIN;
import static net.medcommons.cxp.CXPConstants.STATUS_MISSING_QUERY_STRING;
import static net.medcommons.cxp.CXPConstants.STATUS_MISSING_TRACKING_NUMBER;
import static net.medcommons.cxp.CXPConstants.STATUS_REPOSITORY_ERROR;
import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import net.medcommons.conversion.Base64Utility;
import net.medcommons.cxp.utils.IdHandling;
import net.medcommons.document.CCRParseException;
import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.cxp.server.RLSHandler;
import net.medcommons.modules.cxp.server.TransactionUtils;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.AccountCreationService;
import net.medcommons.modules.services.interfaces.AccountService;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.AccountType;
import net.medcommons.modules.services.interfaces.DocumentService;
import net.medcommons.modules.services.interfaces.NotifierService;
import net.medcommons.modules.services.interfaces.SecondaryRegistryService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.services.interfaces.TrackingReference;
import net.medcommons.modules.services.interfaces.TrackingService;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.modules.xml.RegistryDocument;
import net.medcommons.modules.xml.XPathUtils;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRBuilder;
import net.medcommons.router.services.ccr.RLSCXPHandler;
import net.medcommons.router.services.dicom.util.RepositoryError;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * Models the information in a basic CXP Request.  
 * 
 * TODO: Some refactoring needed.
 * The constructors were all left in place - but new methods {set/get}MedCommonsId
 * were added so that the registry parameter block could set it for transfer &amp; we
 * could leave the old APIs in place.
 * 
 * TRANSFER should be renamed to PUT. 
 * 
 * 
 * @author ssadedin
 */
public class CXPRequest implements Serializable {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CXPRequest.class);
    
    private String ccrData;
    
    private String xmlData;
    
    private String authToken;
    
    /**
     * Optional PIN Hasher allows custom hashing algorithm, or none as appropriate
     */
    private PIN.Hasher pinHasher = PIN.SHA1Hasher;
    
    private String schemaValidationSetting = CCRConstants.SCHEMA_VALIDATION_STRICT;
    
    private String schemaValiationMessages = null;
    
    /**
     * The opcode for the operation
     */
    private String command = COMMAND_UNDEFINED;
    
    public final static String POPS_MEDCOMMONS_ID = "0000000000000000";
    
    /**
     * Custom notification subject - a MedCommons extension
     */
    private String notificationSubject;
    
    /**
     * MedCommons Id to associate with transfer.  This may be specified in the 
     * registry parameter block of CXP or it may be a session variable.
     */
   // private String medcommonsId;
    
    /**
     * User-specified PIN - currently a MedCommons extension.  This may be left
     * null in which case a random PIN will be generated in this field.
     */
    private String pin;
    
    /**
     * The factory that will be used to access services
     */
    private ServicesFactory serviceFactory;
    
    /**
     * A debug flag, do not set this to false
     */
    private boolean contactCentral = true;
    
    /**
     * The identity provider for the sender.
     */
    private String identityProvider = "";
    
    /**
     * The 'From' email as specifed in the CCR's From Actor.
     */
    private String patientEmail = null;
    
    private RLSHandler rlsHandler = new RLSCXPHandler(); 
  
    
    private String storageId = null;
    
    TransactionUtils transactionUtils = new TransactionUtils();
    
    
    private net.medcommons.modules.cxp.CXPConstants.MergeCCRValues mergeIncomingCCR = net.medcommons.modules.cxp.CXPConstants.MergeCCRValues.NONE; // Bad idea. Should take this as an argument
    /**
     * CXPRequest constructor.
     * 
     * @param ccrData - ccrData for the request
     * @param xmlData - XML Data block containing the OpCode and other data parameters
     * @param authToken - identity of client
     * @param notificationSubject - custom notification subject (non-CXP field)
     */
    public CXPRequest(String storageId, String ccrData, String xmlData, String authToken, String notificationSubject, String pin, String schemaValidationSetting) {
        super();
        this.storageId = storageId;
        
        this.ccrData = ccrData;
        this.xmlData = xmlData;
        this.authToken = authToken;
        this.notificationSubject = notificationSubject;
        this.pin = pin;
        this.serviceFactory = new RESTProxyServicesFactory(authToken);
        this.schemaValidationSetting = schemaValidationSetting;
        
        
    }
    public void setMergeIncomingCCR(net.medcommons.modules.cxp.CXPConstants.MergeCCRValues mergeIncomingCCR){
    	this.mergeIncomingCCR = mergeIncomingCCR;
    }
    public net.medcommons.modules.cxp.CXPConstants.MergeCCRValues getMergeIncomingCCR(){
    	return(this.mergeIncomingCCR);
    }
    /**
     * Executes this CXP Request.
     * 
     * @return - a CXPResponse representing the result of the request.
     * @throws CXPException 
     */
    public CXPResponse execute() throws CXPException {
        log.info("CXP Request execute");
        Metric.getInstance("cxpTransactions").sample();
        try {
            Document xmlDataDoc = null;
            Element files = null;
            String cxpVersion = null;
            Element infoSystem = null;
            String clientType = null;
            String clientVersion = null;
            String clientName = null;
           
	        if ( (storageId == null) || (storageId.equals("")))
	        	storageId = POPS_MEDCOMMONS_ID;
	       
            
            
           
            //log.info("xmlData = " + xmlData);
            if (CXPConstants.COMMAND_PUT.equalsIgnoreCase(command)){
            	;// Leave as is. 
            }
            else if (CXPConstants.COMMAND_GET.equalsIgnoreCase(command)){
            	;// Leave as is;
            }
            else if (xmlData == null) { 
            	// Default command is transfer
                command = COMMAND_TRANSFER;
                // Note - no checking of CXP version # in this branch - this
                // is the 'simple upload via HTML form'
                // Note: this feature is temporarily disabled to force old 
                // clients to update. Many of the pre-0.9.19 versions did not
                // specify an xmlData block and thus the version can't be determined.
                // After a release of this on the public server and after most of the old
                // clients are gone then this logic can be removed since the newer
                // clients always specfy a xmlData block.
                //throw new CXPVersionException("CXP client version out of date");
            }
            else {

                // Attempt to parse the xml data
                xmlDataDoc = new CCRBuilder().build(new StringReader(xmlData));
                command = xmlDataDoc.getRootElement().getChildText(CXP_OPCODE);
                cxpVersion = xmlDataDoc.getRootElement().getChildText(CXP_Version);
                infoSystem = xmlDataDoc.getRootElement().getChild(CXP_INFORMATION_SYSTEM);
                // ss: used to throw null pointer, added this to make it clearer
                if(infoSystem == null) {
                    throw new CXPException("Missing required parameter:  InformationSystem");
                }
                clientType = infoSystem.getChildText(CXP_INFORMATION_SYSTEM_TYPE);
                clientVersion = infoSystem.getChildText(CXP_INFORMATION_SYSTEM_VERSION);
                clientName = infoSystem.getChildText(CXP_INFORMATION_SYSTEM_NAME);
                
                boolean isValid = validVersion(cxpVersion, clientName, clientVersion);
                if (!isValid){
                	throw new CXPVersionException("cxpVersion not supported:" + cxpVersion);
                }
                log.error("CXP message version information:" +
                		"CXP version=" + cxpVersion +
                		", client type=" + clientType +
                		", client version=" + clientVersion +
                		", clientName=" + clientName
                		);
                files = xmlDataDoc.getRootElement().getChild(CXP_FILES);

                if(command == null) {
                    return new CXPResponse(CXPConstants.CXP_STATUS_BAD_REQUEST, "Missing OpCode", COMMAND_UNDEFINED);
                }
               /* if (cxpVersion == null)
                	return new CXPResponse("")
*/
                // Override client id from xml-data if it is found in there
                String xmlClientId = xmlDataDoc.getRootElement().getChildText("ClientId");
                if(xmlClientId != null) {
                    if ((authToken != null) && !authToken.equals(xmlClientId)) {
                        log.warn("Client id specified multiple times.  Using xml-data value:  " + xmlClientId);
                    }
                    authToken = xmlClientId;
                }

            }
           

            if((COMMAND_TRANSFER.equals(command)) || (CXPConstants.COMMAND_PUT.equals(command))) {
                if (ccrData == null) {// Transfers *must* have a CCR
                    Metric.getInstance("cxpErrors").sample();
                    return new CXPResponse(CXPConstants.CXP_STATUS_BAD_REQUEST, "CCR data is missing from request", command);
                }
                return handleTransfer(files);
            }
            else if(CXPConstants.COMMAND_GET.equals(command)) {
                CXPResponse r = handleGET();
                if(r.getStatus()>=CXPConstants.CXP_STATUS_BAD_REQUEST) {
		            Metric.getInstance("cxpErrors").sample();
                }
                return r;
            }
            else if(COMMAND_QUERY.equals(command)) {
                CXPResponse r = handleQuery(xmlDataDoc);
                if(r.getStatus()>=CXPConstants.CXP_STATUS_BAD_REQUEST) {
		            Metric.getInstance("cxpErrors").sample();
                }
                return r;
            }
            else 
            if(COMMAND_DELETE.equals(command)) {
                CXPResponse r = handleDelete(xmlDataDoc);
                if(r.getStatus()>=CXPConstants.CXP_STATUS_BAD_REQUEST) {
		            Metric.getInstance("cxpErrors").sample();
                }
                return r;
            }
            else {
                Metric.getInstance("cxpErrors").sample();
                return new CXPResponse(CXPConstants.CXP_STATUS_BAD_REQUEST, "Command not specified", command);
            }
        }
        catch(CCRParseException e){
        	schemaValiationMessages = e.getMessage();
        	throw new CXPValidationException(e);
        }
        catch (CXPException e){
            Metric.getInstance("cxpErrors").sample();
        	throw e; // Just pass it up.
        }
        catch (Exception e) {
            Metric.getInstance("cxpErrors").sample();
            throw new CXPException(e);
        }
    }
    
   /**
    * Implements a Delete operation.
    * 
    * @param xmlDataDoc
    * @return
 * @throws RepositoryException 
 * @throws ServiceException 
 * @throws NoSuchAlgorithmException 
    */ 
    private CXPResponse handleDelete(Document xmlDataDoc) throws RepositoryException, ServiceException, NoSuchAlgorithmException {
        Metric.getInstance("cxpDeletes").sample();
        
        // TODO - what delete means is very undefined right now.
        // Get the sha-1 hash - for now, grab it from the queryString
        String queryString = xmlDataDoc.getRootElement().getChildTextTrim(CXP_QUERYSTRING);
        String trackingNumber = xmlDataDoc.getRootElement().getChildTextTrim(CXP_TXID);
        String pin = xmlDataDoc.getRootElement().getChildTextTrim("PIN");
        
        log.info("Delete request received for content id = " + queryString);
        if(queryString == null) {
            return new CXPResponse(CXPConstants.CXP_STATUS_BAD_REQUEST, STATUS_MISSING_QUERY_STRING, this.command);
        }
        
        // Verify format
        if(queryString.length() != 40) {
            return new CXPResponse(CXPConstants.CXP_STATUS_BAD_REQUEST, STATUS_INVALID_QUERY_STRING, this.command);
        }
        
        if(pin == null) {
            return new CXPResponse(CXPConstants.CXP_STATUS_BAD_REQUEST, STATUS_MISSING_PIN, this.command);
        }
        
        if(trackingNumber == null) {
            return new CXPResponse(CXPConstants.CXP_STATUS_BAD_REQUEST, STATUS_MISSING_TRACKING_NUMBER, this.command);
        }
        
         // Below is the call to make a phsyical delete.  It was decided not to 
        // use physical delete, but instead to revoke track#/keys
        // RepositoryFactory.getInstance().deleteContent(queryString);
        
        TrackingService trackingService = this.serviceFactory.getTrackingService();
        trackingService.revokeTrackingNumber(trackingNumber, PIN.hash(pin));
        
        // Create the success response
        return new CXPResponse(CXPConstants.CXP_STATUS_SUCCESS, "OK", this.command);
    }

    private CXPResponse handleQuery(Document xmlDataDoc) throws ServiceException, ServletException, IOException {
        
        Metric.getInstance("cxpQueries").sample();
        
        // Get the querystring
        String inputQueryString = xmlDataDoc.getRootElement().getChildTextTrim(CXP_QUERYSTRING);
        
        String queryString = inputQueryString;
        // Completely arbitrary. A kludge. Query type should be specified, not a function of length
        // If normalize is used on a guid then all non-numeric characters are removed - this invalidates
        // the guid.
       if (queryString.length() < 25)
        	queryString = IdHandling.normalizeId(inputQueryString);
        log.debug("Input query string='" + inputQueryString + "'\nQuery string = '" + queryString +"'");
        if (blank(queryString))
            throw new ServiceException("Missing QueryString parameter");
        
        // We require either a track#/pin combo or sha-1 hash - they are both > 17 digits
        if (queryString.length() < 17)
            throw new ServiceException("Unsupported QueryString format:'" + queryString  + "'");
        
        // Hash the pin
        try {
            String guid = null;
            // Handle query by track# pin combination
            if (queryString.length() == 17) {
                // Assume the query string is the original txId sent back
                // Then it is MC Track# + 5 digits for pin        
                String trackingNumber = queryString.substring(0, 12);
                String pin = queryString.substring(12, 17);
                String pinHash = PIN.hash(pin);
                
                
                log.info("CXP Get received for tn=" + trackingNumber + " hpin="
                                + pinHash);
                
                // Contact central to get the guid for this document
                TrackingService ts = serviceFactory.getTrackingService();
                TrackingReference ref = ts.validate(trackingNumber, pinHash, null);
                
                
                if (ref==null)
                    throw new ServiceException ("Tracking number + " + trackingNumber + " does not refer to a known document");
                else if( ref.getDocument().getGuid() == null)
                    throw new ServiceException("Tracking number + " + trackingNumber + " returned no GUID for document");
                else if (ref.getDocument()== null)
                    throw new ServiceException("Tracking number + " + trackingNumber + " returned empty document");
                
                
                guid = ref.getDocument().getGuid();
                storageId = ref.getMcId();
                //storageId = ref.getMcId(); // HACK - not sure if this is correct.
               // log.info("Returned MCID is " + storageId);
            } else if (queryString.length() == 40) { // Handle query by guid
                log.info("CXP Get received for guid=" + queryString);
                guid = queryString;
            } else {
                throw new ServiceException("Unsupported QueryString format:'" + queryString + "'");
            }
            
            // TODO: add check of location to make sure we are the correct gateway
            
            // Create the success response
            RegistryDocument ccr = RepositoryFactory.getLocalRepository().queryDocument(storageId, guid);            
            
            return new CXPQueryResponse(guid, queryString, ccr.getXml());                     
            
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException(e);
        }
    }
    
 private CXPResponse handleGET() throws ServiceException, ServletException, IOException {
        
        Metric.getInstance("cxpGET").sample();
        try{
        if (RequestGuid== null){
        	
        	if (RegistrySecret == null){
        		throw new ServletException("Both RegistrySecret and GUID are unspecifed in URL");
        	}
        	if (ConfirmationCode == null){
        		throw new ServletException("Both ConfirmationCode and GUID are unspecifed in URL");
        	}
        	String pinHash = PIN.hash(RegistrySecret);
        	 TrackingService ts = serviceFactory.getTrackingService();
             TrackingReference ref = ts.validate(ConfirmationCode, pinHash, null);
             log.info("CXP Get received for tn=" + ConfirmationCode + " hpin="+ pinHash);
     
             
            
            if (ref==null)
                throw new ServiceException ("Tracking number + " + ConfirmationCode + " does not refer to a known document");
            else if( ref.getDocument().getGuid() == null)
                throw new ServiceException("Tracking number + " + ConfirmationCode + " returned no GUID for document");
            else if (ref.getDocument()== null)
                throw new ServiceException("Tracking number + " + ConfirmationCode + " returned empty document");
            
                
                RequestGuid = ref.getDocument().getGuid();
               // storageId = ref.getMcId();
                //log.info("Returned MCID is " + storageId);
            }
            
            // TODO: add check of location to make sure we are the correct gateway
            
            // Create the success response
            RegistryDocument ccr = RepositoryFactory.getLocalRepository().queryDocument(storageId, RequestGuid);            
            
            return new CXPQueryResponse(RequestGuid, "", ccr.getXml());                     
            
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException(e);
        }
    }
    /**
     * Handles an inbound transfer of a CCR
     * @throws RepositoryException 
     */
    private CXPResponse handleTransfer(Element files) 
	    throws NoSuchAlgorithmException, IOException, JDOMException, ParseException, ServiceException, RepositoryException, Exception
    {
        Metric.getInstance("cxpTransfers").sample();
        
        String reason = "OK";
        int statusCode = 200;
        LocalFileRepository repository = null;
        
        DocumentService documentService = serviceFactory.getDocumentService();
        try {
            // If the pin was sent in the request then use that, otherwise generate one
            if(this.pin == null)
	            pin = PIN.generate();
                        
            byte[] byteDocument = ccrData.getBytes();
    		
    		SHA1 sha1Eval = new SHA1();
    		sha1Eval.initializeHashStreamCalculation();
    		String ccrGuid = sha1Eval.calculateByteHash(byteDocument);
            String originalCcrGuid = ccrGuid;
           
            log.info("CCR guid is " + originalCcrGuid);
            
            String summaryStatus = null;
            String trackingNumber = null;
            
            repository = (LocalFileRepository)RepositoryFactory.getLocalRepository();
            
            // By default we automatically validate the CCR for the user.
            boolean autoValidate = true;
            TrackingService trackingService = serviceFactory.getTrackingService();
           
            
            trackingNumber = transactionUtils.generateConfirmationCode(
					serviceFactory,
					DocumentTypes.CCR_MIME_TYPE,
					storageId, 
					originalCcrGuid,
					pin, this.pinHasher);
            
            // Store the document
            String ccrGuidStorageSystem = submitMedCommonsRepositoryDocument(repository, storageId, ccrData, CCRConstants.CCR_MIME_TYPE);
            log.info("Document should be registered for GUID " + ccrGuidStorageSystem);
            
            // Register the document on central
		    rlsHandler.newDocumentEvent(serviceFactory, storageId, originalCcrGuid, trackingNumber,mergeIncomingCCR,pin,"CXP","UNKNOWN");
			summaryStatus = "success";
            
            if (files != null) { // Attached files
                List fileList = files.getChildren();
                log.info("Number of attached files:" + fileList.size());
                
                    Iterator iter = fileList.iterator();
                    while (iter.hasNext()) {
                        // Get the file into memory; validate SHA-1 hash; if success then save to repository.
                        Element file = (Element) iter.next();
                        log.info("Element name:" + file.getName());
                        String fileName = file.getChild(CXP_FILENAME).getText();
                        String fileType = file.getChild(CXP_FILETYPE).getText();
                        String fileContents = file.getChildText(CXP_FILECONTENTS);
                        String sha1 = file.getChildText(CXP_SHA1);
                        log.info("FileContents size is " + fileContents.length());
                        
                        byte[] fileUnencodedContents = Base64Utility.decode(fileContents);
                        
                        sha1Eval.initializeHashStreamCalculation();
                        String fileHash = sha1Eval.calculateByteHash(fileUnencodedContents);
                        
                        if (fileHash.equals(sha1)) {
                        	documentService.addDocument(storageId, sha1);
                            String guid = repository.putDocument(storageId, fileUnencodedContents, fileType);
                            if (!guid.equals(fileHash))
                            	// seems backward - shouldn't this test be done before the saving to the repository?
                                throw new RepositoryError(
                                                "Calculated file hash "
                                                + fileHash
                                                + " does not match repository guid "
                                                + guid + " for attached file "
                                                + fileName);
                            
                           
                        } 
                        else {
                            // The following three statements should be commented out and replaced
                            // with the RepositoryError commented out below. 
                            String fileString = new String(
                                            fileUnencodedContents);
                            String guid = repository.putDocument(storageId, fileString,fileType);
                            log.error("Server-calculated file hash "
                                            + fileHash
                                            + " does not match SHA-1 calculated by client "
                                            + sha1 + ", file in repository is named " +
                                            guid);
                            /*throw new RepositoryError("Server-caculated file hash "
                             + fileHash
                             + " does not match SHA-1 calculated by client "
                             + sha1);
                             */
                        }
                        
                    }
                }
            
            // The base CCR is now in WebDAV, but we want to validate it automatically, by
            // adding reference validation attributes to it, and the user should see this
            // one if they log in with the tracking number.
            
            CCRDocument ccr = new CCRDocument(storageId, originalCcrGuid, trackingNumber, ccrData, schemaValidationSetting);
log.info("Incoming CCR via CXP:\n" + Str.toString(ccr.getRoot()));
            // ssadedin:  bug #496 - we can't add validation attributes to CXP
            // instead we add attributes if it is NOT valid.
            /*
            log.info("Auto-validating ccr " + originalCcrGuid);            
            for (MCSeries series : ccr.getSeriesList()) {
                if(series.isValidationRequired()) {
                    ccr.addValidationAttribute(series);
                }
            }
            
            // NOTE: calculate guid does syncFromJDOM().  Must calculate because
            // it has changed since we added attributes above.
            String newGuid = ccr.calculateGuid();
            
            if(!newGuid.equals(ccr.getGuid())) {
                serviceFactory.getDocumentService().addDocument(newGuid);
                
                // Save the new CCR to the repository
                ccr.setGuid(repository.putDocument(ccr.getXml()));                            
            }
            
            log.info("Validated CCR has guid " + ccr.getGuid());
            */
            
            /*
             * SWD: 12/05/2006 - I think this should be commented out because the code
             * above for changing the CCR has been commented out. CXP shouldn't be making
             * changes to the CCR by default anyway.
             * trackingService.reviseTrackedDocument(trackingNumber, pinHash, ccr.getGuid());
             */
            
            if (ccr.getSchemaValidationFailure()){
            	// For this to be executed the schemaValiationMode must be LENIENT.
            	// If there was a problem with STRICT validation an exception would
            	// have been thrown upstream.
            	statusCode = CXPConstants.CXP_STATUS_SUCCESS_VALIDATION_WARNINGS;
            	reason = ccr.getSchemaValidationMessages();
            }
            	
            String from = XPathUtils.getValue(ccr.getJDOMDocument(),"sourceEmail");
            patientEmail = XPathUtils.getValue(ccr.getJDOMDocument(),"patientEmail");
            log.info("From email is " + from);
            
            if ((authToken != null) && (!authToken.equals(""))){
            	from = authToken;
            	log.info("Setting from to be the client id:" + authToken);
            	
            }
            
            // If the ccr has To field, send notification
            int sendCount = notify(storageId, serviceFactory, ccr, this.notificationSubject,from, identityProvider);
            CXPResponse response = null;
            if(sendCount > 1) {
                statusCode = CXPConstants.CXP_STATUS_SUCCESS_MULTIPLEEMAIL;
                reason += "Multiple notifications Sent";
                log.info("Multiple notifications sent.  Returning status " + statusCode);
                
                response= new CXPResponse(statusCode, reason, COMMAND_TRANSFER, trackingNumber, pin,
                                originalCcrGuid, ccr);
                response.setMedcommonsId(storageId);
                return(response);
            }
            log.info("registerTrackDocument summary_status = " + summaryStatus);
            
            // Register the document with central
            if (!"success".equals(summaryStatus)){
                response= new CXPResponse(CXPConstants.CXP_STATUS_BAD_REQUEST, summaryStatus, COMMAND_TRANSFER); // Kludge: need to parse and put in real error message.
                return(response);
            }
            response = new CXPResponse(statusCode, reason, COMMAND_TRANSFER, trackingNumber , pin,originalCcrGuid, ccr);
            response.setMedcommonsId(storageId);
            return response ;            
            }
        catch (RepositoryError e) {
            log.error("RepositoryError in COMMAND_TRANSFER", e);
            return new CXPResponse(CXPConstants.CXP_STATUS_BAD_REQUEST, STATUS_REPOSITORY_ERROR,COMMAND_TRANSFER);
        }
        catch(TransactionException e){
        	 log.error("TransactionException in COMMAND_TRANSFER", e);
             return new CXPResponse(CXPConstants.CXP_STATUS_BAD_REQUEST, STATUS_REPOSITORY_ERROR,COMMAND_TRANSFER);
        }
        finally {
            
    }
    }
    
    /**
     * Causes notifications to be sent to the recipients in the given CCR.
     * 
     * @param destMcId - 
     * @param factory
     * @param trackingNumber
     * @param ccr
     * @param customSubject
     * @throws CCRMultipleElementException
     * @throws ServiceException
     * @throws PHRException 
     */
    protected int notify(String destMcId, ServicesFactory factory, CCRDocument ccr, String customSubject, String fromEmail, String identityProvider)
        throws ServiceException, PHRException {
        
        List<String> toEmails = ccr.getNotificationEmails();
        AccountService  accountService = factory.getAccountService();
        SecondaryRegistryService secondaryRegistryService = factory.getSecondaryRegistryService();
        
        log.info("Found " + toEmails.size() + " notifications to send");
        
        // Send each email
        int sendCount = 0;
        for (String toEmail : toEmails) {
            log.info("toEmail is " + toEmail);
            if(!Str.blank(toEmail)) {
                // MedCommons Specific:  check for custom subject line
                String subject = "MedCommons Notification for " + toEmail;
                if (customSubject != null) {
                    subject = customSubject;
                    log.info("Found custom subject line " + subject);
                }
                NotifierService notifierService = factory.getNotifierService();
                
                if (contactCentral) {
                	// TODO Note that this isn't very transactionally safe. 
                	// The notifier email might be sent out but then the ccrlog entry might fail. 
                    ++sendCount;
                    Date now = new Date();
                    
                    notifierService.sendEmailCXP(destMcId,
                                    toEmail, ccr.getTrackingNumber(), "", subject, null); // no message
                    accountService.addCCRLogEntry(ccr.getGuid(), identityProvider, fromEmail, toEmail, destMcId, 
                    		now, customSubject, "Complete",ccr.getTrackingNumber());
                    
                   
                    
                }
            }
        }
        if ((sendCount == 0) && contactCentral){
        	// There were no notification emails. Make one entry in the CCR log table for this CCR.
        	
        	Date now = new Date();
        	String toEmail = "";
            accountService.addCCRLogEntry(ccr.getGuid(), identityProvider, fromEmail, toEmail, destMcId, 
            		now, customSubject, "Complete",ccr.getTrackingNumber());
        	
        }/*
        if ((sendSecondaryRegistryEvent) && ("TEPR".equalsIgnoreCase(getRegistryEnabled()))){
            Contact contactInfo = ccr.getPatient();
           
            String dob = "";
            Date dobDate = ccr.getPatientDateOfBirth();
            try{
	            if (dobDate != null)
	            	dob = dobDate.toGMTString();
	            }
            catch(Exception e){
            	if (dobDate != null)
            		log.error("Unable to format DOB:" + dobDate.toString(), e);
            	else 
            		log.error("Unable to format null DOB:", e);
            }
            // TODO: Rules here need to be documented.
            // If PatientID/GivenName/FamilyName aren't specified - get them from 
            // the context.
            String patientId = getPatientIdentifier();
            if ((patientId==null) || "".equals(patientId))
            	setPatientIdentifier(this.medcommonsId);
            String pGivenName = getPatientGivenName();
            if ((pGivenName==null) || "".equals(pGivenName))
            	pGivenName = contactInfo.getGivenName();
            
            String pFamilyName = getPatientFamilyName();
            if ((pFamilyName==null) || "".equals(pFamilyName))
            	pFamilyName = contactInfo.getFamilyName();
            
            String comment = getComment();
            if ((comment==null) || "".equals(comment))
            	comment = customSubject;
           
            String purpose = getPurpose();
            if ((purpose==null) || "".equals(purpose.trim()))
            	purpose = ccr.getPurposeText();
            secondaryRegistryService.addCCREvent(
                    pGivenName,
            		pFamilyName,
                    "", // TODO: support for Gender
            		getPatientIdentifier(),
            		"MedCommons",
            		getSenderProviderId(),
            		getReceiverProviderId(),
            		dob,
            		"", // TODO:  support for Age
            		ccr.getTrackingNumber(),
            		pin,
            		ccr.getGuid(), // Purpose
            	    purpose,
            	    getCXPServerURL(),
            	    getCXPServerVendor(),
            	    getViewerUrl(), getComment(), 
                    null // no alternative secondary registry support
                         // SS: TODO:  will probably cause null pointer or similar
            		);
           
        }
        */
        return sendCount;
    }
    
    
    /**
     * Places CCR document in repository with specified content type; returns GUID of document in repository.
     * @param ccrDoc
     * @param contentType
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private String submitMedCommonsRepositoryDocument(LocalFileRepository repository, String storageId, String ccrDoc,
            String contentType) throws IOException, NoSuchAlgorithmException, TransactionException{
        
        String docId = null;

        try {
          
            
            docId = repository.putDocument(storageId,ccrDoc, contentType);
            log.info("Submitted document " + docId
                    + " to MedCommons repository");
        } finally {
        	
        }
        return docId;
    }

    public String getCcrData() {
        return ccrData;
    }

    public void setCcrData(String ccrData) {
        this.ccrData = ccrData;
    }

    public String getXmlData() {
        return xmlData;
    }

    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String clientId) {
        this.authToken = clientId;
    }

    public String getNotificationSubject() {
        return notificationSubject;
    }

    public void setNotificationSubject(String notificationSubject) {
        this.notificationSubject = notificationSubject;
    }

    public String getPin() {
        return pin;
    }


    public String getMedcommonsId() {
        return storageId;
    }


    public void setMedcommonsId(String storageId) {
        this.storageId = IdHandling.normalizeId(storageId);
    }

    public String getIdentityProvider(){
    	return(this.identityProvider);
    }

    public void setIdentityProvider(String identityProvider){
    	this.identityProvider = identityProvider;
    }
    public String getCommand() {
        return command;
    }

    public void setCommand(String command){
    	this.command = command;
    }
    public void setPatientEmail(String patientEmail){
    	this.patientEmail = patientEmail;
    }
    public String getPatientEmail(){
    	return(patientEmail);
    }
    /**
     * Returns a version mismatch message; the text of this message must be understood by a 
     * human.
     * 
     * @param cxpVersion
     * @param clientName
     * @return
     */
    private String generateClientVersionMessage(String cxpVersion, String clientName){
    	StringBuffer buff = new StringBuffer();
    	if ((cxpVersion == null) || "".equals(cxpVersion))
    		buff.append("Your CXP client is out of date or does not specify a CXP version tag. ");
    	else {
    		buff.append("Your CXP client is out of date: CXPVersion ");
    		buff.append(cxpVersion);
    		buff.append(" is not supported by this server");
    		
    	}
    	if ((clientName !=null) && (clientName.indexOf("MedCommons") != -1)){
    		buff.append("Please upgrade your client software");
    		
    	}
    	else{
    		buff.append("Please contact your vendor");
    	}
    	return(buff.toString());
    		
    		
    }
    
    /**
     * Determines if the CXP message is one that can be handled by this server.
     * 
     * The current rule is very crude - simply if the version number is > 0.9.2
     * then the CXP message is valid; otherwise reject. Versions 0.9.x for x > 2
     * will work; all other version numbers will fail.
     * 
     * @param cxpVersion
     * @param clientName
     * @return
     */
    private boolean validVersion(String cxpVersion, String clientName,
			String clientVersion) {
		boolean valid = true;
		if ((cxpVersion == null) || ("".equals(cxpVersion.trim())))
			valid = false;
		else {
			try {
				StringTokenizer tok = new StringTokenizer(cxpVersion, ".");
				String majorVersion = (String) tok.nextElement();
				String minorVersion = (String) tok.nextElement();
				String pointVersion = null;
				if (tok.hasMoreElements())
					pointVersion = tok.nextToken();
				log.error("Version is " + majorVersion + ", " + minorVersion
						+ ", " + pointVersion);
				int major = Integer.parseInt(majorVersion);
				int minor = Integer.parseInt(minorVersion);
				int point = -1;
				if (pointVersion != null)
					point = Integer.parseInt(pointVersion);

				if (major != 0)
					throw new IllegalArgumentException(
							"Major version must be zero");
				if (minor != 9)
					throw new IllegalArgumentException(
							"Minor version must be zero");
				if (point < 2)
					throw new IllegalAccessError(
							"Minor version must be at least 3");

			} catch (Exception e) {
				log.error("CXPVersion is invalid:", e);
				valid = false;
			}
		}
		return (valid);
	}

    public PIN.Hasher getPinHasher() {
        return pinHasher;
    }

    public void setPinHasher(PIN.Hasher pinHasher) {
        this.pinHasher = pinHasher;
    }
    
    private String SenderProviderId = null;
    private String RegistryEnabled = null;
    
    private String PatientFamilyName = null;
    private String PatientGivenName = null;
    private String PatientIdentifier = null;
    private String PatientIdentifierSource = null;
   
    private String ReceiverProviderId = null;
    private String DOB = null;
    private String ConfirmationCode = null;
    private String RegistrySecret = null;
    private String Purpose = null;
    private String CXPServerURL = null;
    private String CXPServerVendor = null;
    private String ViewerUrl = null;
    private String Comment = null;
    private String RequestGuid = null;
   
   
    
    public void setSenderProviderId(String SenderProviderId){
    	this.SenderProviderId = SenderProviderId;
    }
    public String getSenderProviderId(){
    	return(this.SenderProviderId);
    }
    
    public void setRegistryEnabled(String RegistryEnabled){
    	this.RegistryEnabled = RegistryEnabled;
    }
    public String getRegistryEnabled(){
    	return(this.RegistryEnabled);
    }
    
    public void setPatientFamilyName(String PatientFamilyName){
    	this.PatientFamilyName = PatientFamilyName;
    }
    public String getPatientFamilyName(){
    	return(this.PatientFamilyName);
    }
    public void setPatientGivenName(String PatientGivenName){
    	this.PatientGivenName = PatientGivenName;
    }
    public String getPatientGivenName(){
    	return(this.PatientGivenName);
    }
    public void setPatientIdentifier(String PatientIdentifier){
    	this.PatientIdentifier = PatientIdentifier;
    }
    public String getPatientIdentifier(){
    	return(this.PatientIdentifier);
    }
    public void setPatientIdentifierSource(String PatientIdentifierSource){
    	this.PatientIdentifierSource = PatientIdentifierSource;
    }
    public String getPatientIdentifierSource(){
    	return(this.PatientIdentifierSource);
    }
    
    
    public void setReceiverProviderId(String ReceiverProviderId){
    	this.ReceiverProviderId = ReceiverProviderId;
    }
     
    public String getReceiverProviderId(){
    	return(this.ReceiverProviderId);
    }
    
    public void setDOB(String DOB){
    	this.DOB = DOB;
    }
     
    public String getDOB(){
    	return(this.DOB);
    }
    
    public void setConfirmationCode(String ConfirmationCode){
    	this.ConfirmationCode = ConfirmationCode;
    }
     
    public String getConfirmationCode(){
    	return(this.ConfirmationCode);
    }
    
    public void setRegistrySecret(String RegistrySecret){
    	this.RegistrySecret = RegistrySecret;
    }
     
    public String getRegistrySecret(){
    	return(this.RegistrySecret);
    }
    
    public void setPurpose(String Purpose){
    	this.Purpose = Purpose;
    }
     
    public String getPurpose(){
    	return(this.Purpose);
    }
    public void setCXPServerURL(String CXPServerURL){
    	this.CXPServerURL = CXPServerURL;
    }
     
    public String getCXPServerURL(){
    	return(this.CXPServerURL);
    }
    public void setCXPServerVendor(String CXPServerVendor){
    	this.CXPServerVendor = CXPServerVendor;
    }
     
    public String getCXPServerVendor(){
    	return(this.CXPServerVendor);
    }
    public void setViewerUrl(String ViewerUrl){
    	this.ViewerUrl = ViewerUrl;
    }
     
    public String getViewerUrl(){
    	return(this.ViewerUrl);
    }
    
    public void setComment(String Comment){
    	this.Comment = Comment;
    }
     
    public String getComment(){
    	return(this.Comment);
    }
    
    public void setRequestGUID(String RequestGuid){
    	this.RequestGuid = RequestGuid;
    }
    public String getRequestGuid(){
    	return(this.RequestGuid);
    }
    /**
	 * Get the account settings for this storage id.
	 */
	private AccountSettings getAccountSettings(String accountId) throws ServiceException{
		AccountSettings settings = serviceFactory.getAccountService().queryAccountSettings(accountId);
         return(settings);
	}
    private String generateNewPatientAcccount() throws ServiceException{
    
    	serviceFactory = new RESTProxyServicesFactory(authToken);
    	AccountCreationService acctCreationService = serviceFactory.getAccountCreationService();
    	//AccountSettings accountSettings = getAccountSettings("");
    	
    	String patientAcctId = acctCreationService.register(AccountType.USER,"",
                "", 
                null, "", "","", null, null, null, null, null)[0];
    	
        log.info("returning new patient account number '" + patientAcctId + "'");
                
        return(patientAcctId);

    }
   
}
