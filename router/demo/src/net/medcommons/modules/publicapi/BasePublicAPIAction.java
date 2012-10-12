package net.medcommons.modules.publicapi;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.repository.GatewayRepository;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.AccountService;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.MetadataHandler;
import net.medcommons.modules.services.interfaces.PHRProfile;
import net.medcommons.modules.services.interfaces.ProfileService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.util.StringUtil;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.ActionBeanContext;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
/**
 * Base class for public API.
 * 
 * This handles all URL parsing/error checking.
 * 
 * @author mesozoic
 *
 */
public class BasePublicAPIAction implements ActionBean, Constants{
	
	public final static  String healthBookContentType = "application/x-healthbook-url";
	private String storageId = null;
	private String auth = null;
	private String reference = null;
	private String guid = null;
	private String format = null;
	private String senderId = null;
        private String contentType = null;
	private String userAgent = null;
	private String requestedGuid = null;
	private String originalURI = null;
	
	protected String remoteAccessAddress = null;
	private MetadataHandler metadataHandler = null;
	protected  GatewayRepository repository = null;
	protected ActionBeanContext ctx;
	private boolean encryptionEnabled = false;
	private boolean backupEnabled = false;
	private String nodeId = null;
	private String token = null;
	private String healthURL = null;
	
	protected PHRTransaction transaction = null;
	
	protected static File transactionDir = null;
	protected boolean transactionExists = false;
	protected boolean hasPermission = true;
	protected UserSession desktop = null;
	private ServicesFactory factory = null;
//	protected boolean userSchema = false;
	private String homePageServer = null;
	
	private HttpServletResponse httpResponse = null;
	private HttpServletRequest httpRequest  = null;
	
	
	
	private SchemaResponse schemaResponse = null;
	
	private static HashMap<String, SchemaResponse> schemaResponses = new HashMap<String, SchemaResponse>();
	
	private static String defaultSchemaValue = "11";
	
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("BasePublicAPIAction");
	
	public final static String CURRENTCCR = "currentCCR";
	
	static {
		 // We just configure it with a pass-through to the static Configuration class
		schemaResponses.put("10", new SchemaResponse10("10"));
		schemaResponses.put("11", new SchemaResponse11("11"));
		schemaResponses.put("12", new SchemaResponse12("12"));
	}
	
	public BasePublicAPIAction(){
		super();
		
		String path = "conf/config.xml";
		String propertiesPath = "conf/MedCommonsBootParameters.properties";
		try{
			Configuration.load(path, propertiesPath);
			
			nodeId = Configuration.getProperty("NodeID");
			
			String encryptionConfig = Configuration.getProperty("EncryptionEnabled");
			String backupConfig = Configuration.getProperty("Backup_Documents");
			remoteAccessAddress =null; 
			homePageServer =Configuration.getProperty("HomePageServer");
			if ((encryptionConfig != null) && (!"".equals(encryptionConfig))) {
				encryptionEnabled = Boolean.parseBoolean(encryptionConfig);

			}
			if ((backupConfig != null) && (!"".equals(backupConfig))) {
				backupEnabled = Boolean.parseBoolean(backupConfig);

			}
			
			
		}
		catch(Exception e){
			log.error("Unable to load config ", e);
		}
		File data =  new File ("data");
		try{
			if(!data.exists()){
				throw new FileNotFoundException("data directory not found:" +
						data.getAbsoluteFile());
				
			}
			// Create the transaction directory only if it's missing
			// and only the first time this is invoked by the server.
			if(transactionDir == null){
				transactionDir = new File(data, "PHRTransactions");
				if (!transactionDir.exists()){
					boolean success = transactionDir.mkdir();
					if (!success){
						throw new IOException("Unable to create directory " +
								transactionDir.getAbsolutePath());
					}
				}
			}
			else if (!transactionDir.exists()){
				boolean success = transactionDir.mkdir();
				if (!success){
					throw new IOException("Unable to create directory " +
							transactionDir.getAbsolutePath());
				}
			}
		}
		catch(Exception e) {
			log.error("Unable to initial file directory", e);
		}
	}
	protected String getHttpRequestInfo(){
		return(BasePublicAPIAction.getHttpRequestInfo(httpRequest));
	}
	protected HttpServletRequest getHttpRequest(){
	    return(httpRequest);
	}
	static protected String getHttpRequestInfo(HttpServletRequest request){
		StringBuffer buff = new StringBuffer();
		buff.append("\nRequestURI:" + request.getRequestURI());
		buff.append("\nQuery String:" + request.getQueryString());
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()){
			String name = headerNames.nextElement();
			String value = request.getHeader(name);
			buff.append("\n ");
			buff.append(name);
			buff.append(" = ");
			buff.append(value);
		}
		return(buff.toString());
		
	}
	protected static SchemaResponse selectSchemaResponse(String value){
		
		if (value == null)
			value = defaultSchemaValue;
		
		SchemaResponse response = schemaResponses.get(value);
		if (response == null)
			response = schemaResponses.get(defaultSchemaValue); // Current default.
		
		return(response);
	}
	
	
	public ActionBeanContext getContext() {
        return ctx;
    }
	
	protected HttpServletResponse getHttpResponse(){
		return(httpResponse);
	}
	public boolean isAuthorized(){
	    return(hasPermission);
	}
	public void setContext(net.sourceforge.stripes.action.ActionBeanContext ctx) {
		log.info("setContext");
		try{
	        this.ctx = (ActionBeanContext)ctx;
	        this.ctx.setSourcePageResolution(this.getSourcePageResolution());
	        httpResponse = ctx.getResponse();
	        httpRequest = ctx.getRequest();
	        HttpServletRequest r = ctx.getRequest();
	        remoteAccessAddress = getServerRoot() + "/router";
	        if(r != null) {
	        	String schema = null;
	        	
	        	userAgent = r.getHeader("User-Agent");
	        	token = r.getParameter("token");
	        	auth  = (String) ctx.getRequest().getAttribute("auth");
	        	
	        	if(StringUtil.blank(auth))
	                auth = (String) ctx.getRequest().getAttribute("oauth_token");
	        	
	        	if (StringUtil.blank(auth)){
	        		auth=r.getParameter("auth");
	        	}
	        	if (StringUtil.blank(auth)){
	        		throw new NullPointerException("No authorization tokens are defined");
	        	}
	        	schema = r.getParameter("useSchema");
	        	String faceBook_id = (String) ctx.getRequest().getAttribute("oauth_principal"); 
	        	if (!StringUtil.blank(faceBook_id)){
	        		log.info("Facebook id is " + faceBook_id);
	        	}
	        	schemaResponse = selectSchemaResponse(schema);
	        	
	        			
	        	log.info("auth=" + auth);
	        	log.info("token=" + token);
	        	if ("".equals(auth)) auth = null;
	        	factory = new RESTProxyServicesFactory(auth);
	        	format = r.getParameter("format");
	        	if(token != null) { // Existing transaction
	        		transaction = BasePublicAPIAction.readTransactionObject(token);
		        	
		        	transactionExists = true;
					
			        storageId = transaction.getStorageId();
			        senderId = transaction.getSenderId();
			        reference = transaction.getOriginalReference();
			        guid = transaction.getOriginalGuid();
			        requestedGuid = r.getParameter("reference");
			        originalURI = transaction.getOriginalURI();
			        healthURL = transaction.getHealthURL();
			        contentType = transaction.getContentType();
			        log.info("Restored state from token " + token + "\n" +
			        		storageId + "," + reference + ", " + contentType);
			       if(auth != null) {
			    	   transaction.setOriginalAuthToken(auth);
			    	   desktop = new UserSession(storageId, auth, new ArrayList<CCRDocument>());
			    	 
			    	   if (!desktop.checkPermissions(storageId, "W")){
			    		   hasPermission = false;
			    		   log.error("User does not have permission to edit CCR");
			    	   }
			    	   else{
			    		   log.info("User has permission to edit CCR");
			    	   }
			       }
			       
	        	}
	        	else {
	        		storageId = r.getParameter("storageId");
			        reference = r.getParameter("reference");	
			       
			        if(Str.blank(reference) || reference.equalsIgnoreCase(CURRENTCCR)) {
			        	reference = AccountDocumentType.CURRENTCCR.name();
			        }
			        originalURI = getServerRoot() + httpRequest.getRequestURI() + "?" +  httpRequest.getQueryString();
			        // Check permission for this user on this account.
			        if (auth != null) {

		                UserSession desktop = new UserSession(storageId, auth,
		                        new ArrayList<CCRDocument>());
		                if (!desktop.checkPermissions(storageId, "W")){
		                    hasPermission = false;
		                    log.error("User does not have permission to edit CCR");
		                }
		               
		            }
			        
			        
	        	}
	        	if (hasPermission){
    		        repository = new GatewayRepository(auth, nodeId,
    						encryptionEnabled, backupEnabled);
	        	}
		       
	        }
	        else {
	        	log.error("HttpServletRequest is null");
	        	throw new NullPointerException("HttpServletRequest is null");
	        }
	        
	       
	        if (!isMSIE(userAgent)){
	        	// MSIE won't process file downloads with cache-control set.
	        	// See http://support.microsoft.com/kb/812935
	        	// Apparently a bug on their part.
	        	ctx.getResponse().setHeader("Cache-Control","no-cache"); // HTTP 1.1
	        	ctx.getResponse().setHeader("Pragma","no-cache"); // HTTP 1.0
	        }

	       
		}
		catch(Exception e){
			log.error("setContext", e);
		}
    }
	
	protected String getServerRoot(){
		String root = httpRequest.getScheme() + "://" + 
			httpRequest.getServerName();
		return(root); // Assume default ports?
	}
	private boolean isMSIE(String userAgent){
		String s = userAgent.toUpperCase();
		if (s.indexOf("MSIE") == -1)
			return(false);
		else
			return(true);
	}
	public String getReference(){
		return(this.reference);
	}
	public String getStorageId(){
		return(this.storageId);
	}
	public String getAuth(){
		return(this.auth);
	}
	public String getSenderId(){
		return(this.senderId);
	}
	public String getFormat(){
		return(this.format);
	}
	public String getContentType(){
		return(this.contentType);
	}
	public String getToken(){
		return(this.token);
	}

	public String getGuid(){
		return(this.guid);
	}
	public String getUserAgent(){
		return(this.userAgent);
	}
	public SchemaResponse getSchemaResponse(){
		return(this.schemaResponse);
	}
    public String getRequestedGuid(){
        return(this.requestedGuid);
    }
    
    public String getOriginalURI(){
    	return(this.originalURI);
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
    
    public void addResponseHeader(String name, String value){
    	if (httpResponse != null) {
    		httpResponse.addHeader(name, value);
    		log.info("Adding response header name= '" + name + "', value = '" + value + "'");
    	}
    	else {
    		throw new NullPointerException("httpResponse is null");
    	}
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
    
    protected String calculateGuid(String storageId, String reference) throws ServiceException{
    	AccountService accountService = factory.getAccountService();
        
        // Find the patient's current ccr
        AccountSettings accountSettings = accountService.queryAccountSettings(storageId);
    	if (reference.matches("[a-z0-9]{40}")) 
    		return  reference; // The reference is already a SHA1.
    	
    	for(AccountDocumentType t : accountSettings.getAccountDocuments().keySet()) {
    	    if(reference.equalsIgnoreCase(t.name()))
    	        return accountSettings.getAccountDocuments().get(t);
    	}
    	
    	// Unknown reference
    	throw new BadReferenceException(reference);
    }
    
    protected String getAccountInformation(PHRTransaction transaction) throws PHRTransactionException {
    	try {
            AccountService accountService = factory.getAccountService();
            
            // Find the patient's current ccr
            AccountSettings accountSettings = accountService.queryAccountSettings(transaction.getStorageId());

            String firstName = accountSettings.getFirstName();
            String lastName = accountSettings.getLastName();
            transaction.setPhrPersonName(firstName + " " + lastName);
            
            // The original reference is something like
            // "CurrentCCR", "EmergencyCCR", or a guid.
            // Need to make this human readable.
            String originalRef = transaction.getOriginalReference();
            if (originalRef.length() == 40){
            	// Then it's a guid.
            	originalRef = "Selected CCR"; 
            }
            transaction.setPhrAlias(originalRef); 
            String photoURL = accountSettings.getPhotoUrl();
            try {
                if(photoURL != null) {
                    try{
                        byte[] photo = getPNGPhoto(photoURL);
                        transaction.setPhrImage(photo);
                    }
                    catch(Exception e){
                        // Typical errors here include the java awt headless mode
                        // not being set as well as format issues with the image itself.
                        log.error("Error creating user image for account " + transaction.getStorageId(), e);
                    }
                }
            }
            catch(Exception e){
            	log.warn("Error creating image for " + photoURL, e);
            }
            ProfileService profiles = Configuration.getBean("profilesService");
            List<PHRProfile> profileNames = profiles.getProfiles(storageId);
           // PHRProfile[] profileNames = getProfiles(accountSettings);
            
            transaction.setProfiles(profileNames); 
            log.info("Number of profiles:" + transaction.getProfiles().size());
            
            String groupIds[] = new String[1];
            groupIds[0] = accountSettings.getGroupId();
            transaction.setGroupIds(groupIds);
     
            return(guid);
        }
        catch (ServiceException e) {
            throw new PHRTransactionException("Unable to generate account information for PHR transaction for account " + transaction.getStorageId(), e);
        }
    }

   
	
    protected Resolution generateResolution(Document doc) {
		XMLOutputter serializer = new XMLOutputter();
		StringWriter sOut = new StringWriter();
		String sDoc;
		try{
			serializer.output(doc,sOut);
			sDoc = sOut.getBuffer().toString();
			
		}
		// Error trapping of last resort - perhaps there is an internal problem with the JDOM
		// document.
		// Client won't have much information from this - but at least it's a syntactically
		// valid response.
		catch(IOException e){
			log.error("Error generating XML resolution for doc " + doc.toString(),e);
			getSchemaResponse().generateResponse(remoteAccessAddress, null, 500, e.toString(), null, null);
			try{serializer.output(doc,sOut);} catch(IOException e2){log.error("Error during error handling", e2);}
			
			sDoc = sOut.getBuffer().toString();
		}
		
		catch(RuntimeException e){
			log.error("Error generating XML resolution for doc " + doc,e);
			getSchemaResponse().generateResponse(remoteAccessAddress, null, 500, e.toString(), null, null);
			try{serializer.output(doc,sOut);} catch(IOException e2){log.error("Error during error handling", e2);}
			
			sDoc = sOut.getBuffer().toString();
		}
		
		return new StreamingResolution(healthBookContentType, new StringReader(sDoc));
		
	}

    
	protected final static int imageTargetHeight = 100;
	protected final static int imageTargetWidth = 100;
	
	/**
	 * Returns a binary array containing a PNG
	 * image at size {imageTargetWidth, imageTargetHeight}.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 */
	protected byte[] getPNGPhoto(String url) throws IOException,HttpException{
		String imageURL = url;
		if (imageURL.indexOf("http") == -1){
			// It's a local URL.
			imageURL = homePageServer + url;
		}
		byte[] image = null;
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(imageURL);
		int status = client.executeMethod(get);
		log.info("photoURL: status = " + status + ", " + imageURL);
		if (status < 299){
			
			BufferedImage userPhoto = ImageIO.read(get.getResponseBodyAsStream());
			if (userPhoto != null){
				AffineTransform at = new AffineTransform();
				log.info("Input image size : width " +userPhoto.getWidth() + ", height " +  userPhoto.getHeight());
				double widthScale  = (1.0 * imageTargetWidth)/(1.0 * userPhoto.getWidth());
				double heightScale = (1.0 * imageTargetHeight)/(1.0 * userPhoto.getHeight());
				boolean scaleImage = false;
				BufferedImage outputImage;
				if(scaleImage) {
					// Keep aspect ratio.
					if (widthScale > heightScale){
						widthScale = heightScale;
					}
					else{
						heightScale = widthScale;
					}
					log.info("Image scaling to " + widthScale);
					at.setToScale(widthScale, heightScale);
					
					outputImage = new BufferedImage(imageTargetHeight, imageTargetWidth,BufferedImage.TYPE_INT_RGB);
					
					Graphics2D g2 = (Graphics2D) outputImage.getGraphics();
					g2.setComposite(AlphaComposite.Src);
					
					BufferedImageOp biop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
					g2.drawImage(userPhoto,biop,0,0);
					
				}
				else {
					outputImage = userPhoto;
				}
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write(outputImage, "png", out);
				image = out.toByteArray();
				
			}
		}
		else {
			log.error("Unable to retreive photo " + imageURL + ", status = " + status);
		}
		return(image);
	}
	protected static File getTransactionLog(PHRTransaction phrTransaction){
		File f = new File(transactionDir, phrTransaction.getToken() + ".log");
		return(f);
		
	}
	
	
	protected static void writeLog(BufferedWriter buff, String s) {
		if (buff==null){
			log.error("Null buffer in transaction log, writing to standard log instead: " + s);
			return;
		}
		try{
			DateFormat dFormat = new SimpleDateFormat("yyyy/MM.dd HH:mm:ss.S");
			// Put in date format
			Date d = new Date(System.currentTimeMillis());
			buff.write("\n");
			buff.write(dFormat.format(d));
			buff.write(": ");
			buff.write(s);
		}
		catch(IOException e){
			log.error("Error writing log for transaction " + s);
		}
		
	}
	protected static void writeLog(BufferedWriter buff, Throwable t) {
	    if (t == null){
            log.error("writeLog invoked with null Throwable; no error message possible");
            return;
        }
		if (buff==null){
			log.error("Null buffer in transaction log, writing to standard log instead: ", t);
			return;
		}
		
		try{
			DateFormat dFormat = new SimpleDateFormat("yyyy/MM.dd HH:mm:ss.S");
			Date d = new Date(System.currentTimeMillis());
			buff.write("\n");
			buff.write(dFormat.format(d));
			buff.write(": ");
			if (t.getLocalizedMessage() != null){
			    buff.write(t.getLocalizedMessage());
			}
			else{
			    buff.write(t.toString());
			}
			if (t.getStackTrace() != null){
    			StackTraceElement stack[] = t.getStackTrace();
    			for (int i=0;i<stack.length;i++){
    				buff.write("\n\t");
    				buff.write(stack[i].getClassName()); 
    				buff.write(".");
    				buff.write(stack[i].getMethodName());
    				buff.write(" (line ");
    				buff.write(stack[i].getLineNumber());
    				buff.write(" )");
    				
    			}
			}
		}
		catch(IOException e){
			log.error("Error writing to transaction log " + e.getLocalizedMessage(), t);
		}

		
	}
	protected static BufferedWriter getTransactionLogBuffer(PHRTransaction phrTransaction) {
		
		BufferedWriter buff = null;
		try{
			File logFile = getTransactionLog(phrTransaction);
			FileWriter f = new FileWriter(logFile, true);
			
			buff = new BufferedWriter(f);
		}
		catch(IOException e){
			log.error("Can't created bufferedWriter to logFile for " + phrTransaction.getDescription());
		}
		
		return(buff);
		
	}
	protected  static void writeTransationObject(PHRTransaction phrTransaction) throws IOException{
		File f = new File(transactionDir, phrTransaction.getToken());
		FileOutputStream out = new FileOutputStream(f);
		
		ObjectOutputStream oout = new ObjectOutputStream(out);
		oout.writeObject(phrTransaction);
		oout.close();
	}
	
	protected static PHRTransaction readTransactionObject(String token) throws IOException, ClassNotFoundException{
		File f = new File(transactionDir, token);
    	if (!f.exists()){
    		throw new TransactionNotFoundException(token);
    	}
		FileInputStream in = new FileInputStream(f);
		
		ObjectInputStream oin = new ObjectInputStream(in);
		Object obj = oin.readObject();
		
		PHRTransaction transaction = (PHRTransaction) obj;
		return(transaction);
	}
	
	/**
	 * Report an error in the response.
	 * @param description
	 * @param e
	 * @return
	 */
	protected Resolution generateErrorResolution(int status, String description, Exception e){
	    String message = null;
        if (e!= null){
            log.error(description, e);
            message = e.getLocalizedMessage() + description;
        }
        else{
            log.error(description);
            message = description;
        }
        
        Document responseDoc = getSchemaResponse().generateResponse(remoteAccessAddress, null, status, message, null, null);
        HttpServletResponse response = getHttpResponse();
        if (response != null){
        	try{
        		// Preferred way to write message.
        		response.sendError(status, message);
        	}
        	catch(IOException e2){
        		// If fail - just send the status.
        		response.setStatus(status);
        	}
        }
        if (transaction != null){
            BufferedWriter logBuffer = BasePublicAPIAction.getTransactionLogBuffer(transaction);
            BasePublicAPIAction.writeLog(logBuffer, e);
        }
        return generateResolution(responseDoc);
    }
	
	protected Resolution generateAuthenticationFailureResolution(int status, String description){
          
        log.error(description);

       
        HttpServletResponse response = getHttpResponse();
        if (response != null){
            try{
                // Preferred way to write message.
                response.sendError(status, description);
            }
            catch(IOException e2){
                // If fail - just send the status.
                response.setStatus(status);
            }
        }
        return new StreamingResolution("text/plain", description);
        
    }
}
