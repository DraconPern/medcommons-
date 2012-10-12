package net.medcommons.router.services.repository;

import static net.medcommons.modules.utils.Str.blank;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.documentum.DocumentRetrievalService;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.configuration.ConfigurationService;
import net.medcommons.modules.cxp.server.RepositoryMetadataHandler;
import net.medcommons.modules.filestore.RepositoryFileProperties;
import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.repository.GatewayRepository;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.util.xml.XMLStreamReader;
import net.medcommons.s3.S3Client;
import net.medcommons.s3.S3Restore;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * Wrapper around {@link GatewayRepository} which adapts it to {@link DocumentRepository}
 * interface.
 * 
 * <p>
 * There are some additional methods that are not in DocumentRepository for local disk access. 
 * <p> 
 * The following illustrates the hierarchy of classes that manage the repository inside an appliance:
 * <pre>
 *    +-------------+
 *    | Repository  |
 *    | Interface   |
 *    +-------------+
 *           ^                    +---------------------+
 *           |                    | DocumentRepository  |
 *           |                    |    Interface        |
 *  -+-------+--+-------+         +---------------------+
 *  | SimpleRepository  |                     ^
 *  +----------+--------+                     |
 *          ^                                 |
 *          |                     +-----------+---------+
 * +--------+---------+           |                     |
 * |GatewayRepository |----------O| LocalFileRepository |
 * +------------------+           |                     |
 *                                +---------------------+  
 * </pre>
 * @author sean, ssadedin@medcommons.net
 */
public class LocalFileRepository implements DocumentRepository {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(LocalFileRepository.class);
	
	
	public final static String HACK_MEDCOMMONS_ID = "123456654321";
    
	public final static String POPS_MEDCOMMONS_ID = ServiceConstants.PUBLIC_MEDCOMMONS_ID;
	
	protected static String clientId = null;
	protected static String nodeId = null;
	
	protected static ConfigurationService configService = new ConfigurationService();
	
	protected static boolean configurationFailure = true;
	protected static GatewayRepository fileRepository = null;
	
	static {
	    
	    // Some unit tests do not need any initialization and just exercise pure
	    // java code.   They don't need init() to be called and it invokes a lot of
	    // setup code so ignore it.
	    if(System.getProperty("medcommons.unittest") == null)
	        init();
	}
	
	/**
	 * Global initialization for file repository
	 * <p>
	 * Probes configuration to set up encryption, backup and other
	 * optional features.
	 */
	public static void init() {
	    
        try {
            
            nodeId = Configuration.getProperty("NodeID");

            if ("UNKNOWN".equals(nodeId))
                throw new RuntimeException("NodeID not specifed in configuration file.");

            clientId = "LocalFileRepository_" + nodeId;

            configurationFailure = false;

            boolean encryptionEnabled = Configuration.getProperty("EncryptionEnabled", true);

            boolean backupEnabled = Configuration.getProperty("Backup_Documents", false);

            fileRepository = new GatewayRepository(clientId, nodeId, encryptionEnabled, backupEnabled);
        }
        catch (Exception e) {
            throw new RuntimeException("Config path = " + configService.getConfigPath(), e); // Wimpy - but logger not defined yet to display error.
        }
	}


	/**
	 * Loads a specific document via its guid and storage id.
	 * 
	 * @param medcommonsId
	 * @return
	 * @throws RepositoryException
	 */
	public CCRDocument queryDocument(String storageId, String guid) throws RepositoryException {
		
		try {
			DocumentDescriptor descriptor = new DocumentDescriptor();
			descriptor.setGuid(guid);
			descriptor.setStorageId(storageId);
			log.info("queryDocument " + storageId + " " + guid);
			String ccrXml = getCCRDocument(descriptor);
			// TODO: Need somehow to link track# to ccrs - could query central
			// at this point however
			// that could be very inefficient if the caller is not expecting it.
			// For now it is passed null, which causes blank display in Viewer.
			return new CCRDocument(storageId, guid, "CCR", "session://currentCcr", null,
					ccrXml, new ArrayList(), CCRConstants.SCHEMA_VALIDATION_OFF);
		} catch (IOException e) {
			log.error("Error retrieving document for storageid= " + storageId + " guid= " + guid, e);
			throw new RepositoryException("Error retrieving document for storageid= " + storageId + " guid " + guid,e);
		} catch (ParseException e) {
			log.error("Error retrieving document for storageid= " + storageId + " guid= " + guid, e);
			throw new RepositoryException("Error retrieving document for storageid= " + storageId + " guid " + guid,e);
		}
		catch (ServiceException e){
			log.error("Error retrieving document for guid " + guid, e);
			throw new RepositoryException("Error retrieving document for guid " + guid,e);
		}
        catch (PHRException e) {
			log.error("Error retrieving document for guid " + guid, e);
			throw new RepositoryException("Error retrieving document for guid " + guid,e);
        }
	}



	/**
	 * Physically deletes the given content, including all references.
	 * 
	 * <i>Warning: deleting content can have serious consequences for the
	 * integrity of references in the system. <b>Do not use this method without
	 * full consideration of how you are handling dependencies.</b></i>
	 * 
	 * @param queryString
	 * @throws RepositoryException
	 */
	public void deleteContent(String storageId,String queryString) throws RepositoryException {
		throw new RepositoryException(
				"Delete not supported from this interface because metadata must be deleted as well");
	}


	
	/**
	 * This returns a stream to a XML document which contains metadata useful 
	 * by the viewer. The other metadata routines in this class access the 
	 * property files (which contain sha1 values, creation dates, content types...); 
	 * this method returns metadata useful for displaying the data (DICOM series and
	 * instance data right now - but this method will eventually be more generic
	 * than that).
	 * @param storageId
	 * @param guid
	 * @return
	 * @throws RepositoryException 
	 */
	public InputStream getCompoundDocumentMetadata(String storageId, String guid) throws RepositoryException {
		CompoundDocumentDescriptor descriptor = new CompoundDocumentDescriptor();
		descriptor.setStorageId(storageId);
		descriptor.setGuid(guid);
		InputStream in = fileRepository.getCompoundDocumentManifest(descriptor);
		return(in);
		
	}
	/**
	 * Returns a string containing the CCR.
	 * 
	 * @param guid
	 * @return
	 * @throws IOException
	 * @throws RepositoryException
	 */
	private String getCCRDocument(DocumentDescriptor descriptor) throws IOException,
			RepositoryException, ServiceException, TransactionException {

		StringBuilder buff = new StringBuilder();
		InputStream in = fileRepository.get(descriptor);
        Reader r = new XMLStreamReader(in, "UTF-8");
		
		char[] buffer = new char[8 * 1024];
		int n = -1;
		while ((n = r.read(buffer)) >= 0) {
			buff.append(buffer,0,n); 
		}
		r.close();
		in.close();
		
		return buff.toString();
	}
	
	/**
	 * Returns file (may be encrypted)
	 * @param storageId
	 * @param uidValue
	 * @param documentName
	 * @return
	 * @throws IOException
	 * @throws RepositoryException
	 * @throws ServiceException
	 */
	public File getDocumentFile(String storageId, String uidValue, String documentName) 
	throws IOException,RepositoryException, ServiceException {
		DocumentDescriptor doc = null;
		File docFile = null;
		if (uidValue.equals(documentName)){
			doc= new SimpleDocumentDescriptor();
		}
		else{
			doc = new CompoundDocumentDescriptor();
			doc.setDocumentName(documentName);	
		}
		doc.setStorageId(storageId);
		doc.setGuid(uidValue);
		try{
			docFile = fileRepository.getFile(doc);
		}
		catch(TransactionException e){
			throw new ServiceException("Error getting file for " + doc, e);
		}
		return(docFile);
		
	}
	/**
	 * Returns a decrypted input stream.
	 * 
	 * @param uidValue - the document identifier (in the case of the simple document) or series identifier.
	 * @param sha1Value - the document identifier (in the case of a simple document) or the object identifier within a series.
	 * @return
	 * @throws IOException
	 * @throws RepositoryException
	 */
	public InputStream getDocument(String storageId, String uidValue, String documentName) throws IOException,
			RepositoryException, ServiceException {
		
		DocumentDescriptor doc = null;
		InputStream in = null;
		if (uidValue.equals(documentName)){
			doc= new SimpleDocumentDescriptor();
		}
		else{
			doc = new CompoundDocumentDescriptor();
			doc.setDocumentName(documentName);	
		}
		doc.setStorageId(storageId);
		doc.setGuid(uidValue);
		try{
			in = fileRepository.get(doc);
		}
		catch(TransactionException e){
			throw new ServiceException("Error getting input stream for " + doc, e);
		}
		return(in);
		
	}
	

	public String putDocument(String storageId, String strDocument, String contentType) throws NoSuchAlgorithmException, IOException, TransactionException{
		InputStream is = null;
		String guid = null;
		SimpleDocumentDescriptor descriptor = new SimpleDocumentDescriptor();
		descriptor.setStorageId(storageId);
		descriptor.setContentType(contentType);
		descriptor.setCreationDate(new Date());
		try{
		    byte[] bDocument = strDocument.getBytes("UTF-8");
		    
		    is = new ByteArrayInputStream(bDocument);
		    fileRepository.putInputStream(descriptor, is);
		    guid = descriptor.getGuid();
		    log.info("putDocument: byte sha1 is now " + guid);
		}
		finally{
		    if (is != null)
		        is.close();
		}
			
		return(guid);
		
	}
    
    /**
     * Deltes the specified file from the repository.
     * 
     * @param guid
     * @throws HttpException
     * @throws IOException
     */
    public void deleteDocument(String storageId, String guid) throws RepositoryException{
    	DocumentDescriptor descriptor = new DocumentDescriptor();
		descriptor.setStorageId(storageId);
		descriptor.setGuid(guid);
		try {
			fileRepository.delete(descriptor);
		}
		catch(Exception e){
			log.error("Error deleting document:" + descriptor.toString(), e);
			throw new RepositoryException("Error deleting document " + descriptor.toString(), e);
		}
    }
    
    public void deleteDocument(DocumentDescriptor descriptor) throws RepositoryException {
        fileRepository.delete(descriptor);
    }
    
    public String getContentType(String storageId, String guid) throws RepositoryException {
    	SimpleDocumentDescriptor descriptor = new SimpleDocumentDescriptor();
		descriptor.setStorageId(storageId);
		descriptor.setGuid(guid);
		String contentType = null;
		try{
		Properties props = fileRepository.getMetadata(descriptor);
		contentType = props.getProperty(RepositoryFileProperties.CONTENT_TYPE);
		}
		catch(Exception e){
			throw new RepositoryException("Error retrieving content type for document " + descriptor.toString(), e);
		}
		return(contentType);
    	
    	
    }
    
    /**
     * Returns a set of known properties of the given guid as a Properties object.
     * 
     * @see RepositoryFileProperties
     * 
     * @param storageId
     * @param guid
     * @return
     * @throws RepositoryException
     */
    public Properties getProperties(String storageId, String guid) throws RepositoryException {
    	SimpleDocumentDescriptor descriptor = new SimpleDocumentDescriptor();
		descriptor.setStorageId(storageId);
		descriptor.setGuid(guid);
		
		try {
		    return fileRepository.getMetadata(descriptor);
		}
		catch(Exception e){
			throw new RepositoryException("Error retrieving content type for document " + descriptor.toString(), e);
		}
    }
    /**
     * Returns true if document exists in repository; false otherwise.
     */
    public boolean inRepository(String storageId, String guid) throws RepositoryException {
        boolean foundDocument = false;
        SimpleDocumentDescriptor descriptor = new SimpleDocumentDescriptor();
        descriptor.setStorageId(storageId);
        descriptor.setGuid(guid);
        
       
        try {
          
            foundDocument = fileRepository.exists(descriptor);
        }
        catch(Exception e){
            foundDocument = false;
            log.error("Document " + descriptor.toString()+ " is not found", e);
        }
        return(foundDocument);
    }
    	
    	
  
    public long getContentLength(String storageId, String guid) throws RepositoryException{
    	SimpleDocumentDescriptor descriptor = new SimpleDocumentDescriptor();
		descriptor.setStorageId(storageId);
		descriptor.setGuid(guid);
		long l = -1;
		String length = null;
		
		try{
			Properties props = fileRepository.getMetadata(descriptor);
			length = props.getProperty(RepositoryFileProperties.LENGTH);

			l = Long.parseLong(length.trim());	
			
		}
		catch(Exception e){
			throw new RepositoryException("Error retrieving file length of '" + length + "'" + " for document " + descriptor.toString(), e);
			
		}
		return(l);
    }
    
    /**
     * Adds the given bytes to the repository as a binary document.
     * 
     * @param bytes
     * @param contentType     
     * @return guid of document 
     */
    public String putDocument(String storageId, byte [] bytes, String contentType) throws IOException, TransactionException, NoSuchAlgorithmException {
        return putDocument(storageId,bytes,contentType, null, false);
    }
    
    /**
     * Adds the given bytes to the repository as a binary document.
     * 
     * @param bytes
     * @param contentType     
     * @return guid of document 
     */
    public String putDocument(String storageId, byte [] bytes, String fileName, String contentType) throws IOException, TransactionException, NoSuchAlgorithmException {
        return putDocument(storageId,bytes,fileName, contentType, false);
    }
    
     /**
     * Adds the given bytes to the repository as a binary document.
     * 
     * @param bytes
     * @param contentType     
     * @param paymentRequired   pass true if the document should be flagged as having outstanding
     *                           payment required
     * @return guid of document 
     */
    public String putDocument(String storageId, byte [] bytes, String fileName, String contentType, boolean paymentRequired) throws IOException, TransactionException, NoSuchAlgorithmException {
    	ByteArrayInputStream is = null;
		String guid = null;
		SimpleDocumentDescriptor descriptor = new SimpleDocumentDescriptor();
		descriptor.setStorageId(storageId);
		descriptor.setContentType(contentType);
		descriptor.setPaymentRequired(paymentRequired);
		descriptor.setDocumentName(fileName);
		try {
		    is = new ByteArrayInputStream(bytes);
		    fileRepository.putInputStream(descriptor, is);
		    guid = descriptor.getGuid();
		}
		finally{
			if (is != null)
				is.close();
		}
    
    	return(guid);
    }
    
	public String putDocument(String storageId, File file, String contentType) 
		throws NoSuchAlgorithmException, IOException, ConfigurationException, ServiceException, TransactionException
	{
	    return putDocument(storageId,file,file.getName(),contentType);
	}
	
    public String putDocument(String storageId, File document, String fileName, String contentType) 
    throws IOException, TransactionException, ServiceException, ConfigurationException, NoSuchAlgorithmException{
		String guid = null;
		FileInputStream in = null;
		try{
			in = new FileInputStream(document);
    	 guid = putDocument(storageId, in, fileName, contentType);
		}
		finally{
			if (in != null) in.close();
		}
    	return(guid);
    }
    
    /**
     * Stores the given stream as a document in the repository. This method is designed
     * to allow streaming in of arbitrary sized documents.
     * <p>
     * <b>Important:</b> The stream will be first buffered to a temporary file
     * on the local file system and then the File will stored.  Don't use
     * this method if you already have the contents in File or byte[] form. 
     * <p>
     * <b>Important2:</b> This method will register the document with central.  You do
     * <b>not</b> need to call the DocumentService to add the document.
     * 
     * @return guid of the created document
     * @throws ConfigurationException 
     * @throws ServiceException 
     */
	
	public String putDocument(String storageId, InputStream is, String contentType)  
		throws NoSuchAlgorithmException, IOException, ConfigurationException, ServiceException, TransactionException
	{
	    return putDocument(storageId, is, null, contentType);
	}
	
	public String putDocument(String storageId, InputStream is, String fileName, String contentType) 
		throws NoSuchAlgorithmException, IOException, ConfigurationException, ServiceException, TransactionException	 
	{
		SimpleDocumentDescriptor descriptor = new SimpleDocumentDescriptor();
		descriptor.setStorageId(storageId);
		descriptor.setContentType(contentType);
		descriptor.setDocumentName(fileName);
		descriptor.setCreationDate(new Date());
		if (is == null)
			throw new NullPointerException("Null InputStream passed to putDocument for account " + storageId);
		
		fileRepository.putInputStream(descriptor, is);
		return(descriptor.getGuid());
    }
  
    
	public InputStream getDocument(String storageId, String guid)throws IOException, TransactionException{
		DocumentDescriptor descriptor = new DocumentDescriptor();
		descriptor.setStorageId(storageId);
		descriptor.setGuid(guid);
		
		return(fileRepository.get(descriptor));
		
	}
    
	public String getDocumentAsString(String storageId, String guid) throws IOException, TransactionException, RepositoryException {
		InputStream in = getDocument(storageId, guid);
		try {
		    return IOUtils.toString(in, "UTF-8");
		}
		finally {
		    IOUtils.closeQuietly(in);
		}
	}

	public void putManifest(String storageId, String guid, InputStream in){
		CompoundDocumentDescriptor descriptor = new CompoundDocumentDescriptor();
		descriptor.setGuid(guid);
		descriptor.setStorageId(storageId);
		fileRepository.putCompoundDocumentManifest(descriptor, in);
	}
	
	public InputStream getManifest(String storageId, String guid) throws RepositoryException {
		CompoundDocumentDescriptor descriptor = new CompoundDocumentDescriptor();
		descriptor.setGuid(guid);
		descriptor.setStorageId(storageId);
		
		InputStream in = fileRepository.getCompoundDocumentManifest(descriptor);
		return(in);
	}
	
	/**
	 * Places the specified files into named collection.
	 * 
	 * @param collectionName
	 * @param documents
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * 
	 * Note: Doesn't test to see if the collection already exists. 
	 * There are two cases here:
	 * <ul>
	 * <li> The entire collection already exists - everything is a duplicate.
	 * <li> A partial collection exists due a previous failed upload.
	 * </ul>
	 * Possible behavior: test to see if collection exists. If it does, then 
	 * check to see if individual elements exist (e.g., do a wdr.list() to get the 
	 * contents, then check for missing documents.
	 */
	/*
	public void putCollection(String collectionName, Collection c, String contentType)
			throws NoSuchAlgorithmException, IOException {
		
		//c.putNamedCollection(wdr, collectionName, contentType);

	}
	*/
	public File getScratchDirectory(){
		return(fileRepository.getScratchDirectory());
	}
    
    /**
     * Returns folder under which documents are stored for a given account.
     */
    public File getAccountDirectory(String accountId) {
        return new File(fileRepository.getRootDirectory(), "/" + accountId);
    }
    
    public void setDocumentRetrievalService(DocumentRetrievalService documentRetrievalService){
        fileRepository.setDocumentRetrievalService(documentRetrievalService);
    }
    
    /**
     * Returns the first metadata object for the given document.
     * <p>
     * This is mainly useful for getting series meta data for DICOM
     * series which are stored in metadata.xml
     * 
     * @param storageId
     * @param guid
     * @return
     */
    public DicomMetadata loadMetadata(String storageId, String guid) throws IOException, JDOMException {
        return loadMetadatas(storageId, guid).get(0);
    }

    /**
     * Returns the list of DICOM Meta data objects for the given series guid
     * 
     * @param storageId
     * @param guid
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    public List<DicomMetadata> loadMetadatas(String storageId, String guid) throws IOException, JDOMException {
        InputStream manifest = getManifest(storageId, guid);
        try {
            return RepositoryMetadataHandler.parseMetadata(manifest);
        }
        finally {
            if(manifest != null)
                manifest.close();
        }
    }

    /**
     * Deletes compound document belonging to the same entity as 
     * the specified document
     * 
     * @param doc
     * @throws RepositoryException 
     */
    public void deleteAllDocuments(CompoundDocumentDescriptor doc) throws RepositoryException {
        try {
            List<DicomMetadata> metas = loadMetadatas(doc.getStorageId(), doc.getGuid());
            
            // Set up a default template descriptor that we'll use for all the
            // documents we are going to delete
            CompoundDocumentDescriptor del = new CompoundDocumentDescriptor();
            del.setStorageId(doc.getStorageId());
            del.setGuid(doc.getSha1());
            
            for(DicomMetadata meta : metas) {
                // Delete the individual document
                del.setDocumentName(meta.getDocumentName());
                fileRepository.delete(del);
            }
            
            fileRepository.deleteCompoundDocumentManifest(doc);
        }
        catch (Exception e) {
            if(e.getCause() != null && e.getCause().getClass().isAssignableFrom(FileNotFoundException.class))  
                  log.warn("Unable to locate compound document manifest (metadata.xml) for document " + doc.toShortString());
            else
	            throw new RepositoryException("Failed to delete compound document: " + doc.toShortString(), e);
        }
    }
}
