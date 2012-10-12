package net.medcommons.modules.personalbackup;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.medcommons.Version;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.filestore.SimpleRepository;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.publicapi.PHRTransactionException;
import net.medcommons.modules.repository.GatewayRepository;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.utils.SupportedDocuments;
import net.medcommons.router.services.repository.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Generates a stream of the files to be backed up from a user's account.
 * Also includes
 * <ul>
 * <li> Receipt.txt - Currently contains only verison information from the build of the router
 *      which generated the backup. This is primarily useful when files are to be re-imported - there
 *      may be differences between versions of backup.
 * <li> CCRViewer - a simple HTML/JavaScript/XSL/CSS utility to view CCRs from the local disk.
 * </ul>
 * 
 * TODO: Add error document to output if there are errors. 
 * TODO: Perhaps add access logs.
 * 
 * @author mesozoic
 *
 */
public class BackupGenerator {
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("BackupGenerator");
	
	/**
     * The factory that will be used to access services
     */
	private ServicesFactory serviceFactory = null;
	
	/**
	 * The services for adding/deleting document locations.
	 */
	private DocumentService documentService = null;
	private String storageId;
	
	
	private static final int BUFFER = 2048;
	
	protected  SimpleRepository repository = null;
	
	private static boolean encryptionEnabled = true;
	private static boolean backupEnabled = false;
	private static String nodeId;
	
	private final static String DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss" ;
	
	private Hashtable<String, String> docNames = new Hashtable<String, String>();
	
	/**
	 * Set to true to include properties files and metadata.xml in backups
	 */
	protected boolean includeMetaFiles = false;
	
	
	/**
	 * Read standard configuration info
	 */
	static{
		String path = "conf/config.xml";
		String propertiesPath = "conf/MedCommonsBootParameters.properties";
		
		try{
			Configuration.load(path, propertiesPath);
			nodeId = Configuration.getProperty("NodeID");
			
			String encryptionConfig = Configuration.getProperty("EncryptionEnabled");

			if ((encryptionConfig != null) && (!"".equals(encryptionConfig))) {
				encryptionEnabled = Boolean.parseBoolean(encryptionConfig);

			}
			
			String backupConfig = Configuration.getProperty("Backup_Documents");
			
			if ((backupConfig != null)&&(!"".equals(backupConfig))){
				backupEnabled = Boolean.parseBoolean(backupConfig);
			}
			
			log.info("Initialized BackupGenerator");
		}
		catch(Exception e){
			log.error("Error reading configuration", e);
		}
	}
	
	
	public BackupGenerator(String storageId, String auth) throws ServiceException {
	    this(storageId, auth, new GatewayRepository(auth, nodeId, encryptionEnabled, backupEnabled));
	}
	
	/**
	 * Create a {@link BackupGenerator} with a specified repository
	 * 
	 * @param storageId
	 * @param auth
	 * @param repository
	 * @throws ServiceException
	 */
	public BackupGenerator(String storageId, String auth, SimpleRepository repository) throws ServiceException {
		this.storageId = storageId;
		serviceFactory = new RESTProxyServicesFactory(auth);
		documentService = serviceFactory.getDocumentService();
		this.repository = repository;
	}
	
	/**
	 * Returns subset of images by content type. Returned list may be empty. 
	 * <P>
	 * As a side effect - the elements returned in the ArrayList here are removed
	 * from the input allDocs. This means that if all known contentTypes 
	 * are removed from allDocs that there should be zero elements left
	 * in the set. Any remaining elements would signal a bug or a new
	 * (and unsupported) document type which could not be backed up.
	 * @param docs2
	 * @param contentType
	 * @return
	 */
	private ArrayList<DocumentRefHolder> extractContentTypeDocs(List<DocumentRefHolder> docs2, String contentType){
		ArrayList<DocumentRefHolder> docs = new ArrayList<DocumentRefHolder>();
		
		ListIterator<DocumentRefHolder> listIterator = docs2.listIterator();
		while (listIterator.hasNext()) {
			DocumentRefHolder doc = listIterator.next();
			if (doc.contentType.equals(contentType)){
				docs.add(doc);
				listIterator.remove();
			}
			
		}
		return(docs);	
	}
	
	private void zipCompoundDocument(ZipOutputStream zipOut, SupportedDocuments docType, DocumentRefHolder doc) throws IOException, TransactionException{
	    
		String directoryName = getBackupLocation(docType);
		
		CompoundDocumentDescriptor compoundDocs[] = 
		    repository.getCompoundDocumentDescriptors(storageId, doc.descriptor.getGuid());
		
		long totalDocumentLength = 0;
		for(int j=0;j<compoundDocs.length;j++) {
		    totalDocumentLength+= compoundDocs[j].getLength();
		}
		
		log.info("About to process compound document " + doc.guid + " # elements = " +
				doc.descriptor.getNDocuments() + ", total bytes = " + totalDocumentLength);
		
		long totalBytesSent = 0;
		for (CompoundDocumentDescriptor compoundDoc : compoundDocs) {
            String documentName = directoryName + "/" +  doc.guid + "/"
                                + defaultFilename(compoundDoc.getDocumentName(),doc.contentType);
            
            ZipEntry entry = new ZipEntry(documentName);
            entry.setMethod(docType.getCompressionMethod());
            if(entry.getMethod() == ZipOutputStream.STORED) {
                InputStream in = getDocumentInputStream(compoundDoc);
                entry.setCrc(calculateCRC(in));
                entry.setSize(getDocumentSize(compoundDoc));
                if(log.isDebugEnabled())
                    log.debug("Setting " + compoundDoc.getGuid() + " " 
	                            + compoundDoc.getDocumentName() + " compression to STORED"
	                            + " with length " + entry.getSize());
                IOUtils.closeQuietly(in);
            }
            
            entry.setTime(doc.creationDate.getTime());
            InputStream in = getDocumentInputStream(compoundDoc);
            zipOut.putNextEntry(entry);
            totalBytesSent += copy(in, zipOut);
            closeQuietly(in);
            zipOut.closeEntry();
            
            if(includeMetaFiles) {
	            entry = new ZipEntry(documentName+".properties");
	            entry.setTime(doc.creationDate.getTime());
	            zipOut.putNextEntry(entry);
	            in = new FileInputStream(repository.getPropertyFile(compoundDoc));
	            try {
	                copy(in,zipOut);
	            }
	            finally {
	                closeQuietly(in);
	            }
            }
        }
		
        if(includeMetaFiles && compoundDocs.length > 0) {
            ZipEntry entry = new ZipEntry(directoryName + "/" + compoundDocs[0].getGuid() + "/metadata.xml");
            File f = repository.metadataFile(compoundDocs[0]);
            entry.setTime(f.lastModified());
            zipOut.putNextEntry(entry); 
            InputStream in = new FileInputStream(f);
            try {
                copy(in,zipOut);
            }
            finally {
                closeQuietly(in);
            }
            
            entry = new ZipEntry(directoryName + "/" + compoundDocs[0].getGuid() + ".properties"); 
            f = repository.getPropertyFile(compoundDocs[0]);
            entry.setTime(f.lastModified());
            zipOut.putNextEntry(entry); 
            in = new FileInputStream(f);
            try {
                copy(in,zipOut);
            }
            finally {
                closeQuietly(in);
            }
            
        }
		
		if (log.isDebugEnabled())
            log.debug("Finished writing compound " + doc.guid + " bytesSent = " + totalBytesSent + " " + doc.contentType);
	}

    protected String getBackupLocation(SupportedDocuments docType) {
        return storageId + "/" + directoryName(docType);
    }

	/**
	 * Return the size of the specified compound document to be zipped
	 * <p>
	 * @param compoundDoc
	 * @return
	 * @throws RepositoryException 
	 */
    protected long getDocumentSize(CompoundDocumentDescriptor compoundDoc) throws RepositoryException {
        return compoundDoc.getLength();
    }

	/**
	 * Return an input stream for the specified compund document.
	 * <p>
	 * 
	 * @param compoundDoc
	 * @return
	 * @throws RepositoryException
	 */
    protected InputStream getDocumentInputStream(CompoundDocumentDescriptor compoundDoc) throws RepositoryException {
        return new BufferedInputStream(repository.get(compoundDoc));
    }
    
	private void zipSimpleDocument(ZipOutputStream zipOut, SupportedDocuments docType, DocumentRefHolder doc) throws IOException, TransactionException{
		byte data[] = new byte[BUFFER];
		if (doc.descriptor.getNDocuments() == 1){
			log.info("About to process simple document " + doc.guid );
			String directoryName = directoryName(docType);
			
			String filename = defaultFilename(doc.documentName, docType.getContentType());
			
			String documentName =  storageId + "/" + directoryName + "/" + filename;
			
			ZipEntry entry = new ZipEntry(documentName);
			
			entry.setMethod(docType.getCompressionMethod());
			if (entry.getMethod() == ZipOutputStream.STORED){
				InputStream crcStream = repository.get(doc.descriptor);
				BufferedInputStream in = new BufferedInputStream(crcStream);
				entry.setCrc(calculateCRC(in));
				entry.setSize(doc.documentLength);
				
				log.info("Setting " + doc.guid + " compression to STORED");
			}
			entry.setTime(doc.creationDate.getTime());
			
			InputStream docStream = repository.get(doc.descriptor);
			BufferedInputStream in = new BufferedInputStream(docStream);
			int count;
			long bytesSent=0;
			
			
			zipOut.putNextEntry(entry);
			while((count = in.read(data, 0, BUFFER)) != -1) {
					bytesSent+= count;
					zipOut.write(data, 0, count);    
		    }
			
			in.close();
			zipOut.closeEntry();
			
			log.info("Finished writing simple " + documentName + " length = " + 
					doc.documentLength + " bytesSent = " + bytesSent + " " + doc.contentType);
		}
	}
	
	private void addFile(ZipOutputStream zipOut, String zipPrefix, File file , String documentName) throws IOException{
		if(!file.isFile())
			throw new RuntimeException("Routine only handles files, not directories:" + file.getAbsolutePath());
		
		ZipEntry entry = new ZipEntry(zipPrefix + documentName);
		entry.setMethod(ZipOutputStream.DEFLATED);
		InputStream docStream = new FileInputStream(file);
		BufferedInputStream in = new BufferedInputStream(docStream);
		
		zipOut.putNextEntry(entry);
		try {
			copy(in, zipOut);
		}
		finally {
			closeQuietly(in);
		}
		zipOut.closeEntry();
		log.info("Finished writing compound " + documentName);
	}
	
	private void addDirectory(ZipOutputStream zipOut, String zipPrefix, File directory) throws IOException{
		String dirName = directory.getName();
		String zipName = zipPrefix + dirName + "/";
		String[] files = directory.list();
		for (int i=0;i<files.length;i++){
			File f = new File(directory, files[i]);
			if (f.isFile()){
				addFile(zipOut, zipName , f, f.getName());
			}
			else {
				addDirectory(zipOut, zipName, f);
			}
			
		}
		
	}
	
	/**
	 * Adds a set of utilities to the backup stream for viewing CCRs.
	 * 
	 * @param zipOut
	 * @throws IOException
	 * @throws TransactionException
	 */
	protected void zipUtils(ZipOutputStream zipOut) throws IOException, TransactionException{
		
		File ccrViewerDir = new File("data/CCRViewer");
		if (!ccrViewerDir.exists()){
			throw new FileNotFoundException(ccrViewerDir.getAbsolutePath());
		}
		String[] ccrViewerFiles = ccrViewerDir.list();
		for (int j = 0;j<ccrViewerFiles.length;j++){
			String documentName =  ccrViewerFiles[j];
			String directoryName = storageId + "/Utils/CCRViewer/";
			File docFile = new File(ccrViewerDir, documentName);
			if (docFile.isFile()){
				addFile(zipOut,directoryName, docFile, documentName);
			}
			else{
				addDirectory(zipOut, directoryName, docFile);
			}
			
			
		}
	}
	/**
	 * Writes the backup zip contents to the specifed output stream.
	 * 
	 * @param out
	 * @return
	 */
	public ZipOutputStream generateBackup(OutputStream out){
		
		
		ZipOutputStream zipOut = new ZipOutputStream(out);
		//zipOut.setLevel(9); // Highest compression level
		zipOut.setMethod(ZipOutputStream.DEFLATED);
		
		//SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		
		//ArrayList ccrs = getContentTypeDocs(contentType)
		try {
			
			List <DocumentRefHolder> docs = getAccountDocuments();
			for(SupportedDocuments docType: SupportedDocuments.values()) {
			    
				String contentType = docType.getContentType();
			    if(!this.includeDocumentType(docType) || !this.includeContentType(contentType)) 
			        continue;
			    
				String fileExtension = docType.getFileExtension();
				int compressionType = docType.getCompressionMethod();
				
				ArrayList<DocumentRefHolder> filteredDocs = extractContentTypeDocs(docs, contentType);
				Iterator<DocumentRefHolder> filteredIter = filteredDocs.iterator();
				while (filteredIter.hasNext()){
					DocumentRefHolder doc = filteredIter.next();
					log.debug("Processing doc " + doc.guid + " " + doc.descriptor.getNDocuments() + " " + docType.name());
					/*String parent = doc.descriptor.get
					boolean compound = false;
					if ((parent!= null) && (!"".equals(parent)))
						compound = true;
						*/
					
					if (doc.descriptor.getNDocuments() == 1){
						zipSimpleDocument(zipOut, docType, doc);
					}
					else { 
						zipCompoundDocument(zipOut, docType, doc);
					}
				}	
				
			}
			zipUtils(zipOut);
			// Write the version information - useful for restoring 
			// in the future.
			Properties p = generateVersionProperties();
			
			ZipEntry versionInfo = new ZipEntry(storageId + "/Receipt.txt");
			versionInfo.setMethod(ZipOutputStream.DEFLATED);
			zipOut.putNextEntry(versionInfo);
			p.store(zipOut, "Personal Backup");
			
			zipOut.closeEntry();
			zipOut.flush();
			
			zipOut.finish();
			
		}
		catch(Exception e){
			throw new RuntimeException("Error generating backup stream", e);
		}
		return(zipOut);
	}
	
    /**
	 * Returns true if the specified type should be included in the backup
	 * operation.  This is hard coded to true, but the presence of 
	 * this method allows subclasses to filter the backup operation
	 * by extending this class.
	 * 
	 * @param docType      the type to determine if should be included in
	 *                      output archive
	 * @return             true iff the type of document should be included
	 */
	protected boolean includeContentType(String contentType) {
        return true;
    }

    /**
	 * Returns true if the specified type should be included in the backup
	 * operation.  This is hard coded to true, but the presence of 
	 * this method allows subclasses to filter the backup operation
	 * by extending this class.
	 * 
	 * @param docType      the type to determine if should be included in
	 *                      output archive
	 * @return             true iff the type of document should be included
	 */
	protected boolean includeDocumentType(SupportedDocuments docType) {
        return true;
    }

    /**
	 * Root of filename - used to generate a default root filename
	 * if there is none associated with the document. 
	 * @param contentType
	 * @return
	 */
	private String fileRoot(SupportedDocuments supportedDocument){
		return(supportedDocument.name());
		
	}
	private String directoryName(SupportedDocuments docType){
		String dirName;
		switch(docType){
		case CCR:
			dirName="CCR"; break;
		case DICOM:
			dirName="DICOM"; break;
		case PDF:
			dirName="PDF"; break;
		case HTML:
			dirName="HTML"; break;
		case TEXT:
			dirName="TEXT"; break;
		case JPG:
			dirName="JPEG"; break;
		case PNG:
			dirName="PNG"; break;	
		default:
			dirName ="Other_Documents";
		}
		return(dirName);
	}
	
	int docCounter = 0;
	
	/**
	 * Used to remove suffixes (such as ".pdf") from document names.
	 * Some documents have them; others don't. Removing them means that
	 * it's safe to add them in a later step so that all files have
	 * appropriate suffixes.
	 * 
	 * @param root
	 * @param suffix
	 * @return
	 */
	private String stripSuffix(String root, String suffix){
		String candidateFilename = root;
		int lastIndex = candidateFilename.lastIndexOf(suffix);
		if (lastIndex != -1)
			candidateFilename = candidateFilename.substring(0, lastIndex);
		return(candidateFilename);
	}
	private String createFilenameWithSuffix(String root, String suffix){
		
		return(root + suffix);
		
	}
	private String defaultFilename(String defaultRoot, String contentType) {
		String root = defaultRoot;
		String suffix = "";
		try{
    		SupportedDocuments docType = SupportedDocuments.getDocumentType(contentType);
    		suffix = getSuffix(docType);
    		if (isBlank(root)) 
                root = fileRoot(docType);
		}
		catch(Exception e){
		    log.error("Unknown content type " + contentType);
		}
		if (isBlank(root)) 
            root = "Other_Documents";
		docCounter++;
		
		String strippedRoot = stripSuffix(root, suffix);
		String candidateFilename = createFilenameWithSuffix(strippedRoot, suffix);
		
		if (docNames.get(candidateFilename) == null)
			docNames.put(candidateFilename, candidateFilename);
		else{
			
			candidateFilename =  createFilenameWithSuffix(strippedRoot + "_" + docCounter, suffix);
			docCounter++;
			docNames.put(candidateFilename, candidateFilename); // Should test for collision
		}
			

		
		return(candidateFilename);
	}

    protected String getSuffix(SupportedDocuments docType) {
        return "." + docType.getFileExtension();
    }
	
	/**
	 * For STORED (e.g., non-compressed) streams the file's CRC
	 * must be calculated.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private long calculateCRC(InputStream in) throws IOException{
		BufferedInputStream is = new BufferedInputStream(in);
		CRC32 crc = new CRC32();

        crc.reset();

        byte [] buff = new byte[1024];

        

        while(true){
             int got = is.read(buff,0,buff.length);
             if (got <= 0) break;
             crc.update(buff,0,got);

        }

        is.close();

        return(crc.getValue());
	}
	/**
	 * This is wrong. 
	 * 
	 * Needs to do the following:
	 * <ol>
	 * <li> Return a List of inner class containing storage id/guid/nodeid.
	 * <li> Filter on this node for the moment. Handle multiple gateways, S3 later.
	 * 
	 * </ol>
	 * @return
	 * @throws ServiceException
	 */
	protected List<DocumentRefHolder> getAccountDocuments() throws ServiceException, IOException, PHRTransactionException{
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		/* There are two types of information on the documents for an account:
		 * <ol>
		 *  <li> Information on central
		 *  <li> Information in the gateway's metadata.
		 * </ol>
		 */
		ArrayList<DocumentRefHolder> docReferences = new ArrayList<DocumentRefHolder>();
		DocumentReference [] docs = documentService.queryUserDocuments(this.storageId);
		log.info("There are " + docs.length + " documents on central for " + this.storageId);
		if (log.isDebugEnabled()){
    		for (int i=0;i<docs.length;i++){
    			log.debug("central[" + i + "]" + docs[i].getGuid());
    		}
		}
		DocumentDescriptor [] docDescriptors = repository.getDocumentDescriptors(this.storageId);
		
		if(docDescriptors == null)
		    docDescriptors = new DocumentDescriptor[0];
		if (log.isDebugEnabled()){
    		for (int i=0;i<docDescriptors.length;i++){
    			log.debug("repository[" + i + "]" + docDescriptors[i].getGuid());
    		}
		}
		log.info("There are " + docDescriptors.length + " documents on the gateway for " + this.storageId);
		
		
		AccountSettings accountSettings = serviceFactory.getAccountService().queryAccountSettings(this.storageId);
		String currentCCRGuid = accountSettings.getAccountDocuments().get(AccountDocumentType.CURRENTCCR);
		ProfileService profiles = Configuration.getBean("profilesService");
        List<PHRProfile> profileNames = profiles.getProfiles(storageId);
		for (int i=0;i<profileNames.size();i++){
		    PHRProfile p = profileNames.get(i);
		    log.info("Profile " + i + " name " + p.getName() + " " + p.getGuid() + " " + p.getDate());
		}
		
		for (int i=0;i<docDescriptors.length;i++){
			DocumentDescriptor doc = docDescriptors[i];
			
			DocumentKey[] keys = documentService.getDocumentDecryptionKey(storageId, doc.getGuid(), nodeId);
			if (keys != null){
				for (int j=0;j<keys.length;j++){
					String guid = null;
					
					DocumentRefHolder ref = new DocumentRefHolder();
					ref.guid = doc.getGuid();
					guid=ref.guid;
					DocumentDescriptor descriptor = getDocumentDescriptor(ref.guid, docDescriptors);
					
					if (descriptor == null){
						throw new NullPointerException("Inconsistent document information: " +
								" document known on central does not exist in local data store:" +
								storageId + ", " +doc.getGuid());
					}
					
					if(!includeDocument(descriptor)) {
					    break;
					}

					descriptor.setStorageId(this.storageId);
					ref.contentType = descriptor.getContentType();
					ref.documentLength = descriptor.getLength();
					ref.creationDate = descriptor.getCreationDate();
					String resourceName  = defaultFilename(descriptor.getDocumentName(), ref.contentType);
					
					ref.documentName = resourceName;
					ref.descriptor = descriptor;
					if (!ref.contentType.equals(DocumentTypes.CCR_CHANGE_HISTORY_MIME_TYPE)){
					    if (ref.contentType.equals(DocumentTypes.CCR_MIME_TYPE)){
                            // Different rules for CCRs. Only bring back those that
                            // are in tabs
                            if (ref.guid.equals(currentCCRGuid)){
                                ref.documentName = "CurrentCCR.xml";
                                docReferences.add(ref);
                                log.info("Added CurrentCCR.xml reference " + ref.guid + " " + ref.contentType);
                            }
                            else{
                                log.info("looking for profile with guid " + ref.guid);
                                PHRProfile profile = getProfile(profileNames, ref.guid);
                                if (profile != null){
                                    ref.documentName = profile.getName();
                                    if (Str.blank(ref.documentName)){
                                        if (profile.getDate() != null){
                                            ref.documentName=dateFormat.format(profile.getDate());
                                        }
                                        else{
                                            ref.documentName="Unknown:" + System.currentTimeMillis();
                                        }
                                    }
                                    docReferences.add(ref);
                                    log.info("Added CCR reference " + ref.guid + " " + ref.contentType + " with profile alias " + profile.getName());
                                }
                            }
                        }
                        else{
                            ref.documentName = resourceName;
                            docReferences.add(ref);
                            log.info("Added non-CCR document " + ref.guid + " " + ref.contentType);
                        }
					}
					else{
						log.info("Skipping document of type:" + DocumentTypes.CCR_CHANGE_HISTORY_MIME_TYPE + ":" + ref.guid);
					}
					
				}
			}
			else{
				log.error("No documents available on node " + nodeId + 
						" for storage id " + storageId + ", guid " + doc.getGuid() );
			}
			 
		}
		return(docReferences);
	}
	
    /**
	 * Returns true if the specified document should be included in the backup
	 * operation.  This is hard coded to true, but the presence of 
	 * this method allows subclasses to filter the backup operation
	 * by extending this class.
	 * 
	 * @param doc          the document to determine if should be included in
	 *                      output archive
	 * @return             true iff the type of document should be included
	 */	
    protected boolean includeDocument(DocumentDescriptor descriptor) {
        return true;
    }

    /**
	 * Returns true if the specified document should be included in the backup
	 * operation.  This is hard coded to true, but the presence of 
	 * this method allows subclasses to filter the backup operation
	 * by extending this class.
	 * 
	 * @param doc          the document to determine if should be included in
	 *                      output archive
	 * @return             true iff the type of document should be included
	 */
	protected boolean includeDocument(DocumentReference doc) {
        return true;
    }

    private DocumentDescriptor getDocumentDescriptor(String guid, DocumentDescriptor[] docDescriptors){
		if (guid == null) throw new NullPointerException("null GUID specified");
		DocumentDescriptor docDescriptor = null;
		for (int i=0;i<docDescriptors.length;i++){
			DocumentDescriptor candidateDescriptor = docDescriptors[i];
			if (guid.equals(candidateDescriptor.getGuid())){
				docDescriptor = candidateDescriptor;
				break;
			}
			
		}
		return(docDescriptor);
	}
	private boolean isBlank(String s){
		
		if (s==null) return(true);
		if ("".equals(s)) return(true);
		else return(false);
	}
	
	
	
	
	/**
	 * Maybe make this a static. Need the following:
	 * <ul>
	 * <li> javascxript files
	 * <li> xsl
	 * <li> any gif, other css
	 * </ul>
	 * @return
	 */
	public File[] getStandardFiles(){
		File[] standardFiles = new File[2];
		return(standardFiles);
	}
	/**
	 * Document ref holder contains references to the document for an account for 
	 * two distinct applications:
	 * <ol>
	 *  <li> Provides information for generating the XML manifest file.
	 *  <li> Provides information for generating the ZIP output stream.
	 * </ol>
	 * @author mesozoic
	 *
	 */
	public static class DocumentRefHolder {
		public DocumentDescriptor descriptor;
		public String guid;
		public String location;
		public String contentType;
		public String documentName;
		public Date creationDate;
		public long documentLength;
		
	}
	private  Properties generateVersionProperties() {
        Properties p = new Properties();
        p.setProperty("Version", Version.getVersionString());
        p.setProperty("Revision", Version.getRevision ());
        return(p);
    }
	public static void temp() throws IOException{
		
			FileOutputStream out = new FileOutputStream("Test.zip");
			ZipOutputStream zipOut = new ZipOutputStream(out);
			zipOut.setLevel(9); // Highest compression level
			zipOut.setMethod(ZipOutputStream.DEFLATED);
			byte data[] = new byte[BUFFER];
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			String files[] = new String[3];
			files[0] = "073a1c66dd77373644fb4a1f3f58a8fe24e88db6";
			files[1] = "5b53afbe9b5b8c67cd9da76261d07f07c21f8503";
			files[2] = "676471ffb2b60cacd68b1ec8be52e2270523ba7f";
			try{
				for (int i=0;i<files.length;i++){
					String fname = "1012576340589251/" + files[i];
					
					ZipEntry entry = new ZipEntry(files[i]);
					FileInputStream in = new FileInputStream(fname);
					zipOut.putNextEntry(entry);
					int count; long bytesSent = 0;
					while((count = in.read(data, 0, BUFFER)) != -1) {
						bytesSent+= count;
						zipOut.write(data, 0, count);
			           // fileOut.write(data,0,count);
			            
					}
					zipOut.closeEntry();
					
				}
				
				zipOut.finish();
				zipOut.flush();
				
				
				zipOut.close();
				
			}
			catch(Exception e){
				throw new RuntimeException("Error generating backup stream", e);
			}
			
		}
	
	/**
	 * Return a specific profile; null if none exist.
	 * Note that a null return is expected if the guid isn't visible in the current tabs.
	 * @param profiles
	 * @param guid
	 * @return
	 */
	private PHRProfile getProfile(List<PHRProfile> profiles, String guid){
	    
	    PHRProfile profile = null;
	    for (int i=0;i<profiles.size();i++){
	        String profileGuid = profiles.get(i).getGuid();
	        if (!Str.blank(profileGuid)){
    	        if (profiles.get(i).getGuid().equals(guid)){
    	            profile= profiles.get(i);
    	            break;
    	        }
	        }
	        else{
	            log.error("Blank guid in profile " + profiles.get(i).getName());
	        }
	    }
	    return(profile);
	}
	 
	}

