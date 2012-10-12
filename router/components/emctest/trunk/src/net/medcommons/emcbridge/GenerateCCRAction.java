package net.medcommons.emcbridge;

/**
 * Stripes framework class which 
 * <ol>
 * <li>processes HTTP request from user to display information from Documentum into MedCommons</li>
 * <li>queries Documentum for content based on document id and user credentials </li>
 * <li>generates CCR and organizes file attachments (DICOM, PDF) for upload to MedCommmons.</li>
 * <li>uploads files to MedCommons to a newly created account via CXP</li>
 * <li>issues a redirect to the user's browser to the uploaded CCR.
 * </ol>
 
 Examples of input URLs
 http://localhost:8090/emcbridge/net/medcommons/emcbridge/GenerateCCR.action?userName=Administrator&passWord=healthcare&documentId=090004d28000198b&docbase=EMCOSA
 http://stego.myhealthespace.com/emcbridge/net/medcommons/emcbridge/GenerateCCR.action?userName=Administrator&passWord=healthcare&documentId=090004d28000198b&docbase=EMCOSA
 
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import net.medcommons.emcbridge.utils.ActionBeanContext;
import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.cxp.client.CXPClient;
import net.medcommons.modules.transfer.UploadFileAgent;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;
import org.cxp2.Document;
import org.cxp2.PutResponse;
import org.cxp2.RegistryParameters;

import com.emc.solution.osa.client.dao.MasterClientData;

///@UrlBinding("/generateCCR")
public class GenerateCCRAction implements ActionBean{
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("GenerateCCRAction");
	protected String userAgent = null;
	protected String userName = null;
	protected String passWord = null;
	protected String documentId = null;
	protected String docBase = null;
	protected String rootURL = null;
	
	protected String senderId = null;
	protected String auth = null;
	
	
	
	protected ActionBeanContext  ctx;
	public ActionBeanContext getContext() {
        return ctx;
    }
	public void setPassWord(String passWord){
		this.passWord = passWord;
	}
	public String getPassWord(){
		return(this.passWord);
	}
	public void setDocumentId(String documentId){
		this.documentId = documentId;
	}
	public String getDocumentId(){
		return(this.documentId);
	}
	public void setDocBase(String docBase){
		this.docBase = docBase;
	}
	public String getDocBase(){
		return(this.docBase);
	}
	public void setUserName(String userName){
		log.info("setUserName=" + userName);
		this.userName = userName;
	}
	public String getUserName(){
		return(this.userName);
	}
	
	/**
     * Placeholder for child classes to override if they want to override
     * the resolution for validation failures.
     * 
     * @return
     */
    public Resolution getSourcePageResolution() {
        return null;
    }
	
	public void setContext(net.sourceforge.stripes.action.ActionBeanContext ctx) {
		log.info("setContext");
		Properties emcbridgeProperties = new Properties();
		try{
			File propertiesFile = new File("conf/emcbridge.properties");
			FileInputStream in = new FileInputStream(propertiesFile);
			
			emcbridgeProperties.load(in);
			in.close();
		}
		catch(IOException e){
			log.error("Error reading properties file", e);
			
		}
		try{
	        this.ctx = (ActionBeanContext)ctx;
	        this.ctx.setSourcePageResolution(this.getSourcePageResolution());
	        
	        HttpServletRequest r = ctx.getRequest();
	        HttpServletResponse httpResponse = null;
	        
	        if(r != null) {
	        	
	        	
	        	userAgent = r.getHeader("User-Agent");
	     
	        	rootURL = r.getScheme() + "://" + r.getServerName() + "/";
	        	log.info("queryString = " + r.getQueryString());
        	
	           	userName = r.getParameter("userName");
	        	passWord  = r.getParameter("passWord");
	        	documentId = r.getParameter("documentId");
	        	docBase = r.getParameter("docbase");
	        	
		       
	        }
	        else {
	        	log.error("HttpServletRequest is null");
	        	throw new NullPointerException("HttpServletRequest is null");
	        }
	        
	        httpResponse = ctx.getResponse();
	        /*
	        if (!isMSIE(userAgent)){
	        	// MSIE won't process file downloads with cache-control set.
	        	// See http://support.microsoft.com/kb/812935
	        	// Apparently a bug on their part.
	        	ctx.getResponse().setHeader("Cache-Control","no-cache"); // HTTP 1.1
	        	ctx.getResponse().setHeader("Pragma","no-cache"); // HTTP 1.0
	        }
	        */
	        
	        // Retrieve properties for a MedCommons authorization token. This is for 
	        // the demo only - in real life the user would be authenticated in
	        // Documentum and MedCommons would honor that authentication.
	        auth = emcbridgeProperties.getProperty("auth");
	        
	        // Retrieve properties for a MedCommons account for the user of the system
	        // (as distinct from the patient's account). The authorization token above must
	        // be linked to this account.
	        senderId = emcbridgeProperties.getProperty("senderId");
	        if ((auth==null) || ("".equals(auth))){
	        	throw new RuntimeException("No auth parameter defined in properties file");
	        }
	        if ((senderId==null) || ("".equals(senderId))){
	        	throw new RuntimeException("No senderId parameter defined in properties file");
	        }
	        
	        String configRootURL = emcbridgeProperties.getProperty("rootURL");
	        if ((configRootURL != null) && (!"".equals(configRootURL)) && (!"default".equalsIgnoreCase(configRootURL))){
	        	rootURL = configRootURL;
	        }

	        log.info("== Sender is " + senderId);
	        log.info("== Auth is " + auth);
	       
		}
		catch(Exception e){
			log.error("setContext", e);
		}
    }
	
		
		/**
		 * Main entry point for stripes invokation
	    */
	    @DefaultHandler 
	    public Resolution generateCCR() throws Throwable{
	    	Resolution res = null;
	    	
	    	res = generateCCRFromDfcClient();;
	    	
	    	return(res);
	    }
	    	
	    public Resolution generateCCRFromDfcClient() throws Throwable{
	    	//File[] files = null;
	    	Resolution res = null;
	    	File transactionDirectory= null;
	    	GenerateCCRFromDFCClient generateCCR = null;
	    	long startTotalTime = System.currentTimeMillis();
	    	log.info("== Start calculating [timing]  === ");
	    	try{
	    		
	    		
    			generateCCR = new GenerateCCRFromDFCClient(docBase, userName, passWord, documentId);
	    			
    			generateCCR.startSession();
    			
    			// Get all metadata from Documentum
    			MasterClientData clientData = generateCCR.getMetadata(documentId);
    			
    			long endReadMetadata = System.currentTimeMillis();
    			long readMetadataTime = (endReadMetadata - startTotalTime);
    			log.info("[timing]  Time to read metadata from documentum: " + readMetadataTime + "msec");
    			generateCCR.retrieveFilesFromDocumentum(clientData);
    			
				log.info("clientData fetched");
				MasterClientUtil.dumpMasterClient(clientData);
			
    			

	    		transactionDirectory = generateCCR.getTransactionDirectory1();

	    		long startUploadTime = System.currentTimeMillis();
	    		// Upload file attachments to MedCommons.
	    		// The "-1" for the patient account means that a 
	    		// new patient account will be generated.
	    		UploadFileAgent uploadAgent = new UploadFileAgent(
	    				rootURL + "gateway/services/CXP2", 
	    				auth, 
	    				"-1",
	    				senderId,
	    				transactionDirectory); 
	    		
	    		
	    		PutResponse response = uploadAgent.upload();
	    		List<RegistryParameters> registryParameters = response
				.getRegistryParameters();
	    		CXPClient.displayResponseInfo(response);
	    		log.info("CXP response = " + response.getStatus()); // TODO: need to test for failed upload.
	    		List<Document> documents = response.getDocinfo();
	    		for (int i=0;i<documents.size();i++){
	    			Document doc = documents.get(i);
	    			log.info("CXP document[" + i + "] = contentType=" + doc.getContentType() 
	    					+ ", Description =" + doc.getDescription() 
	    					+ ", document name=" + doc.getDocumentName() + ", guid=" + doc.getGuid() 
	    					+ ", sha1=" + doc.getSha1()
	    					+ ", parentName=" + doc.getParentName());
	    		}
	    		// The returnedStorageId is the MedCommons account
	    		// which has been created. This value is inserted into the 
	    		// CCR below before the CCR is uploaded.
	    		String returnedStorageId = CXPClient.getMedCommonsParameter(
						registryParameters, CXPConstants.STORAGE_ID);
	    		
	    		File ccrTransactionDirectory = generateCCR.generateCCRUpload(returnedStorageId);
	    		log.info("CCR is in transaction directory " + ccrTransactionDirectory.getAbsolutePath() + 
	    				" and returned storageid is " + returnedStorageId);
	    		
	    		// Upload the CCR
	    		uploadAgent = new UploadFileAgent(
	    				rootURL + "gateway/services/CXP2", 
	    				auth,
	    				returnedStorageId, 
	    				senderId,
	    				ccrTransactionDirectory);
	    		response = uploadAgent.upload();
	    		CXPClient.displayResponseInfo(response);
	    		long endUploadTime = System.currentTimeMillis();
	    		long uploadTime = (endUploadTime - startUploadTime );
	    		log.info("Should be new account created with storageid " + returnedStorageId);
	    		
	    		List<Document> docs = response.getDocinfo();
	    		Document doc = docs.get(0);
	    		String guid = doc.getGuid();
	    		String newURL = makeURL(returnedStorageId, senderId,guid, auth);
	    		res = new RedirectResolution(newURL, false);
	    		long endTotalTime = System.currentTimeMillis();
	    		long totalTime = (endTotalTime - startTotalTime);
	    		log.info("[timing] Upload time = " + uploadTime + "msec");
	    		log.info("[timing] Total time = " + totalTime + "msec");
	    		
	    		
	    	}
	    	finally{
	    		try{
		    		if (generateCCR != null){
		    			generateCCR.endSession();
		    		}
	    		}
	    		catch(Exception e){
	    			log.error("Error ending session:" , e);
	    		}
	    	}
	    	
	    	return(res);
	    }
	    
	    /**
	     * Creates a URL for viewing the newly uploaded content.
	    */
	    String makeURL(String storageId, String senderId, String guid, String auth){
	    	String newURL = rootURL + "router/currentccr?a=";
	    	newURL+= storageId;
	    	newURL+="&aa=";
	    	newURL+=senderId;
	    	newURL+="&g=";
	    	newURL+=guid;
	    	newURL+="&auth=";
	    	newURL+=auth;
	    	newURL+="&at=";
	    	newURL+=auth;
	    	log.info("Generated URL " + newURL);
	    	
	    	return(newURL);
	    }
	
	
}
