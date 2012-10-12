package net.medcommons.modules.filestore;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.nvl;
import static org.apache.commons.io.FileUtils.moveFile;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import net.medcommons.Version;
import net.medcommons.documentum.DocumentRetrievalService;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.crypto.io.FileGuid;
import net.medcommons.modules.crypto.io.SHA1InputStream;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.repository.RepositoryEvent;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryListener;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * Simple respository. There are six types of files in the repository:
 * 
 * Root/
 * <ul>
 * <li> &lt;guid&gt; <br/> Simple document</li>
 * <li> &lt;guid&gt;.properties <br/> Property file for Simple Document</li>
 * <li> &lt;guid&gt;/<br/> Folder for compound document</li>
 * <li> &lt;guid&gt;/&lt;document name&gt; <br/> Document within a compound
 * document.</li>
 * <li> &lt;guid&gt;/&lt;document name&gt;.properties <br/> Property file for a
 * document within a compound document.</li>
 * <li> &lt;guid&gt;/metadata.xml <br/> Metadata file for documents within
 * compound document. </li>
 * </ul>
 * 
 * When working with compound documents it is important to use the same 
 * repository object for a whole transaction because a list of transaction handles
 * is cached and used to correctly close off the open transactions when
 * {@link #closeCompoundDocument(String)} is called.
 * <p>
 * TODO: Data cleanup on failure. If a file can't be deleted - the files just stay around.
 * Perhaps need some type of transaction log for cleanup.
 * TODO: Design flaw. Need to move transactional web services to initialize/finalize; make
 * these required for the simple documents as well. 
 * <p>
 * @author mesozoic
 * @auther ssadedin@medcommons.net
 */

public class SimpleRepository implements Repository, RepositoryFileProperties {
	private static Logger log = Logger.getLogger("SimpleRepository");
	
	protected ArrayList<RepositoryListener> listeners = new ArrayList<RepositoryListener>();
	
	private DocumentIndexService indexService = Configuration.getBean("documentIndexService");

	private String repositoryName = "SimpleRepository";

	private enum FILE_TYPES {
		SIMPLE, COMPOUND_ELEMENT, METADATA
	};

	private static String METADATA_FILENAME = "metadata.xml";
	private static File rootDirectory = null;
	
	private File scratchDirectory = null;

	HashMap<String, DocumentDescriptor> activeTransactions = new HashMap<String, DocumentDescriptor>();

	/**
	 * Simple counter for transactions. Used to generate transaction ids because
	 * the System.currentTimeMillis() is a very poor clock in Windows (it has a
	 * 30-60msec sampling frequency);
	 */
	private long transactionCounter = 0;
	
	public void setRootDirectory(File dir){
		rootDirectory = dir;
	}

	public File getRootDirectory() {
        return rootDirectory;
    }

    // Typically the EMC document service
    DocumentRetrievalService documentRetrievalService = null;

    /**
     * Shared date formatter for writing dates
     */
    private static FastDateFormat DATE_FORMATTER = FastDateFormat.getInstance(PROPERTY_DATE_FORMAT);

    public void setDocumentRetrievalService(DocumentRetrievalService documentRetrievalService){
        this.documentRetrievalService = documentRetrievalService;
    }
    /**
	 * For SimpleRepository - properties are ignored.
	 */
	public void init(Properties properties) throws IOException {
		boolean success = false;

		if (rootDirectory == null)
			rootDirectory = new File(repositoryName);
		
		success = rootDirectory.exists();
		if (!success) {
			success = rootDirectory.mkdirs();
			if (!success)
				throw new RuntimeException(
						"Repository directory not found/could not be created:"
								+ rootDirectory.getAbsolutePath());
		}
		scratchDirectory = new File(rootDirectory, "Scratch");
		success = scratchDirectory.exists();
		if (!success){
			success = scratchDirectory.mkdir();
			if (!success)
				throw new RuntimeException(
						"Repository scratch directory not found/could not be created:"
								+ scratchDirectory.getAbsolutePath());
		}
		log.info("Repository '" + repositoryName + "' initialized with root directory " + rootDirectory.getAbsolutePath() +
				", scratch directory (for temporary files) is initialized to " + scratchDirectory.getAbsolutePath());
	}
    
	public File getScratchDirectory(){
		return(scratchDirectory);
	}
    
	public void delete(DocumentDescriptor document) throws RepositoryException {
		
		RepositoryEvent evt = new RepositoryEvent(document);
		for(RepositoryListener l : listeners) {
		    l.onDelete(evt);
		}
		
		StorageFileDescriptor descriptor = StorageFile(document);
		File f = descriptor.f;
		if (f.exists()) {
			if (f.isDirectory())
				descriptor.fileType = FILE_TYPES.COMPOUND_ELEMENT;
			else
				descriptor.fileType = FILE_TYPES.SIMPLE;
		}
		
		log.info("in delete for " + descriptor);
		try {
            if (descriptor.fileType == FILE_TYPES.SIMPLE)
            	deleteSimpleDocument(descriptor);
            else if (descriptor.fileType == FILE_TYPES.COMPOUND_ELEMENT)
            	deleteCompoundDocument(descriptor);
            else if (descriptor.fileType == FILE_TYPES.METADATA)
            	deleteMetaDataDocument(descriptor);

            // Always delete the storageDir if it's empty
            File storageDir = StorageDir(document.getStorageId());
            File[] documents = storageDir.listFiles();
            if (documents.length == 0) {
            	boolean success = storageDir.delete();
            	if (!success) {
            		throw new IOException(
            				"Failed to delete empty storage directory :"
            						+ storageDir.getAbsolutePath());
            	} else {
            		log.info("Deleted empty storage directory:"
            				+ storageDir.getAbsolutePath());
            	}
            }
        } 
		catch (IOException e) {
		    throw new RepositoryException(e);
        }
	}

	/**
	 * Deletes the document and its associated property file.
	 * 
	 * @param descriptor
	 * @throws IOException
	 */
	private void deleteSimpleDocument(StorageFileDescriptor descriptor) throws IOException {

		if (descriptor == null)
			throw new IllegalArgumentException("Null StorageFileDescriptor");

		File f = descriptor.f;
		log.info("Deleting simple document:" + f.getAbsolutePath());
		if (!f.exists()) {
			throw new FileNotFoundException(
					"File does not exist; can not be deleted:"
							+ f.getAbsolutePath());
		}
		
		if(!f.delete()) {
			throw new IOException("File exists but can not be deleted:"
					+ f.getAbsolutePath());
		}

		File propertyFile = descriptor.propertyFile;
		if(propertyFile.exists()) {
			if(!propertyFile.delete()) {
				throw new IOException(
						"Property file exists but can not be deleted:"
								+ propertyFile.getAbsolutePath());
			} 
		}
		else {
		    log.error("Property file does not exist; can not be deleted:"
						+ propertyFile.getAbsolutePath());
		}

	}

	/**
	 * Deletes the contents of the directory then the directory itself.
	 * 
	 * @param descriptor
	 * @throws IOException
	 */
	private void deleteCompoundDocument(StorageFileDescriptor descriptor)
			throws IOException {
		File f = descriptor.f;
		boolean success = false;
		if (!f.exists()) {
			throw new FileNotFoundException(
					"Compound document root does not exist; can not be deleted:"
							+ f.getAbsolutePath());
		}
		if (!f.isDirectory()) {
			throw new IOException(
					"Compound document root is not a directory; can not be deleted:"
							+ f.getAbsolutePath());
		}
		File files[] = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			success = files[i].delete();
			if (!success) {
				throw new IOException("Deletion failed: "
						+ files[i].getAbsolutePath());
			}
		}
		success = f.delete();
		if (!success) {
			throw new IOException(
					"Directory contents deleted but directory can not be deleted:"
							+ f.getAbsolutePath());
		}
		File propertyFile = descriptor.propertyFile;
		if (propertyFile == null){
			throw new NullPointerException("Missing propertyFile in descriptor");
			
		}
		if (propertyFile.exists()){
			success = propertyFile.delete();
			if (!success)
				throw new IOException(
						"Directory contents deleted but metadata file can not be deleted:"
								+ propertyFile.getAbsolutePath());
		}
		else{
			throw new IOException(
					"Directory contents deleted but metadata file does not exist; can not be deleted:"
							+ propertyFile.getAbsolutePath());
		}
	}

	private void deleteMetaDataDocument(StorageFileDescriptor descriptor)
			throws IOException {
		File f = descriptor.f;
		if (!f.exists()) {
			throw new FileNotFoundException(
					"File does not exist; can not be deleted:"
							+ f.getAbsolutePath());
		}
		boolean success = f.delete();
		if (!success) {
			throw new IOException("File exists but can not be deleted:"
					+ f.getAbsolutePath());
		}
	}

	public InputStream get(DocumentDescriptor document) throws RepositoryException {
	    
		StorageFileDescriptor descriptor = StorageFile(document);
		File documentFile = descriptor.f;
		if (documentFile == null)
			throw new IllegalArgumentException("Unexpected null file reference in storage descriptor for document " + document);

		if(!documentFile.exists()) {
		    DocumentDescriptor resolvedDescriptor = indexService.getDocument(document.getStorageId(), document.getGuid());
		    RepositoryEvent evt = new RepositoryEvent(resolvedDescriptor);
		    for(RepositoryListener l : listeners) {
		        l.onFileUnavailable(evt, documentFile);
		    }
		}
		
		try {

            // EMC Data path
            if (documentRetrievalService != null & !documentFile.exists()){
                retrieveDocument(documentFile, documentFile.getName());
                
            }
            else if (documentRetrievalService != null){
                File f = lockedFiles.get(documentFile.getName());
                if (f!= null){
                    // The file is being written.
                    try{
                        Thread.sleep(100); 
                        // Don't really lock - just delay. 100msec should be
                        // long enough for now. Need a real mechanism in the future.
                        // [The design problem is that images and thumbnails will both be requested at the 
                        // same time from Documentum by the browser]
                        log.info("Lock conflict for file, delay until cleared:" + f.getAbsolutePath());
                    }
                    catch(Exception e){
                        ;
                    }
                }
            }
        }
		catch (IOException e) {
		    throw new RepositoryException(e);
        }
		
		if (!documentFile.exists())
			throw new RepositoryException("File not found:" + documentFile.getAbsolutePath());
		
		try {
		    RepositoryEvent evt = new RepositoryEvent(document, new FileInputStream(documentFile));
			for(RepositoryListener l : listeners) {
	            l.onInput(evt);
	        }
            return evt.in;
        }
		catch (IOException e) {
		    throw new RepositoryException(e);
        }
	}
	
	private HashMap<String, File> lockedFiles = new HashMap<String, File>();
	
	private void retrieveDocument(File f, String documentId) throws IOException{
	    FileOutputStream fout = new FileOutputStream(f);
	    
	    
	    InputStream in = null;
	    try{
	        lockedFiles.put(documentId, f);
            in = documentRetrievalService.retrieveDocument(documentId);
            byte b[] = new byte[4096];
            int i;
            while ((i = in.read(b)) != -1) {
                fout.write(b,0,i);
            }
	    }
	    finally{
	        lockedFiles.remove(documentId);
	        try{if (in != null) in.close();} catch(IOException e){;}
	        try{fout.close();} catch(IOException e){;}
	       
	    }
        
	    
	}
	
	
	/**
	 * Retrieve a reference to the raw file storing the document.  Note that it
	 * may / will be encrypted.  Do not use this method to test if the file exists
	 * as it may attempt recovery from backup (exception will be thrown).
	 */
	public File getFile(DocumentDescriptor document) throws IOException , TransactionException {
		StorageFileDescriptor descriptor = StorageFile(document);
		File documentFile = descriptor.f;
		if (documentFile == null){
			throw new NullPointerException("Unexpected null file reference in storage descriptor for document " + document);
		}

		if(!documentFile.exists()) {
			DocumentDescriptor resolvedDescriptor = indexService.getDocument(document.getStorageId(), document.getGuid());
			RepositoryEvent evt = new RepositoryEvent(resolvedDescriptor);
			for(RepositoryListener l : listeners) {
			    l.onFileUnavailable(evt, documentFile);
			}
		}
		
		if (!documentFile.exists())
			throw new FileNotFoundException("File not found:" + documentFile.getAbsolutePath());

		return (documentFile);
	}


	public void putInputStream(DocumentDescriptor document, InputStream is) throws RepositoryException {
		
		StorageFileDescriptor descriptor = StorageFile(document);
		
		try {
            if (descriptor.fileType == FILE_TYPES.SIMPLE)
            	putSimpleDocument((SimpleDocumentDescriptor) document, is);
            else if (descriptor.fileType == FILE_TYPES.COMPOUND_ELEMENT)
            	putCompoundDocument((CompoundDocumentDescriptor) document, is);
            else if (descriptor.fileType == FILE_TYPES.METADATA)
            	putMetaDataDocument(document, is);
            else {
            		log.info("putInputStream: unknown fileType: " +
            				descriptor.fileType + " for document " + document);
            }
            
            indexService.index(document);
        } 
		catch (IOException e) {
		    throw new RepositoryException(e);
        }
        catch (ServiceException e) {
            throw new TransactionException("Unable to index document " + document.toShortString(),e);
        }
	}

	public void putMetaDataDocument(DocumentDescriptor document, InputStream is)
			throws IOException {
		throw new IOException("Not implemented yet");
	}

	/**
	 * Writes out file into a scratch directory; renames with SHA-1 hash after
	 * the file is complete.
	 * 
	 * @param document
	 * @param is
	 * @throws IOException
	 */
	public void putSimpleDocument(SimpleDocumentDescriptor document, InputStream is) throws RepositoryException {
	    
		try {
		    RepositoryEvent evt = new RepositoryEvent(document);
		    for(RepositoryListener l : listeners) {
		        l.onBeginStoreDocument(evt);
		    }
		}
		catch(TransactionException e){
			log.error("Error in web services or key generation", e);
			throw new RuntimeException("Error in web services or key generation", e);
		}
		String transactionId = generateTransactionId();
		
		OutputStream outProperties = null;
		SHA1InputStream sIn = null;
		File propertyFile = null;
		File scratchFile = null;
		OutputStream out = null;
		
		// Not sure if this is efficient or not. SimpleDateFormat is not
		// threadsafe so creating a new
		// DateFormat for each saved file.
		boolean success = false;
		try {
		      scratchFile = getScratchFileForDocument(document, transactionId);
		        
		    RepositoryEvent evt = new RepositoryEvent(document,new FileOutputStream(scratchFile));
		    for(RepositoryListener l : listeners) {
		        l.onOutput(evt);
            }
		    out = evt.out;

			File dir = scratchFile.getParentFile();
			dir.mkdirs();
			if (!dir.exists()) {
				throw new IOException(
						"Unable to find or create directory for file '"
								+ dir.getAbsolutePath() + "'");

			}
			/*
			log.info("Dir is " + dir.getAbsolutePath() + "\n, exists = "
					+ dir.exists() + "\n is directory = " + dir.isDirectory()
					+ "\n (dir) is writable = " + dir.canWrite()
					+ "\n (file) is writable = " + scratchFile.canWrite());
			*/
			// File f = new File(dir, documentFile.getName());

			
			ScratchFileInfo fileInfo = writeScratchFile(is, out);
			document.setGuid(fileInfo.hash);
			document.setSha1(fileInfo.hash);
			document.setLength(fileInfo.inputStreamLength);
			document.setNDocuments(1);
			//long fileSize = scratchFile.length();
			Properties props = createProperties(document);
			
			propertyFile = getPropertyFile(document);
			outProperties = new FileOutputStream(propertyFile);
			props.store(outProperties, "File for account:"
					+ document.getStorageId());

			StorageFileDescriptor descriptor = StorageFile(document);
			File permanentFile = descriptor.f;
			success = renameTo(scratchFile, permanentFile);

			if (!success)
				throw new IOException("Unable to rename scratch file "
						+ scratchFile.getAbsolutePath()
						+ "\n to new permanent file "
						+ permanentFile.getAbsolutePath());

			
            for(RepositoryListener l : listeners) {
                l.onEndStoreDocument(evt);
            }
			
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Configuration problem with SHA1 class files", e);
		}
		catch(TransactionException e){
			e.printStackTrace(System.out);
			throw new RuntimeException(
					"Problem with web services", e);
		}
		catch(IOException ex) {
		    throw new RepositoryException(ex);
		}
		finally {
			if (out != null){
				try {
					out.close();
				} catch (Exception e) {
					log.error("Error closing file "
							+ scratchFile.getAbsolutePath(), e);
				}
			}
			if (outProperties != null) {
				try {
					outProperties.close();
				} catch (Exception e) {
					log.error("Error closing file "
							+ propertyFile.getAbsolutePath(), e);
				}
			}
			if (sIn != null) {
				try {
					sIn.close();
				} catch (Exception e) {
					log.error("Error input SHA1 input stream for output file"
							+ scratchFile.getAbsolutePath(), e);
				}
			}
			if (!success) {
				scratchFile.delete();
			}
		}

	}

	private class ScratchFileInfo {
		String hash;
		long inputStreamLength;
	}
	
	/**
	 * Writes from input stream to output stream; returns the sha1 hash of the file written
	 * out and the length of the input stream. Note that the length of the output stream may
	 * or may not match the length of the input stream (for unencrypted output they will match;
	 * encrypted files have a few more bits).
	 * 
	 * @param is
	 * @param out
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private ScratchFileInfo writeScratchFile(InputStream is, OutputStream out)
			throws IOException, NoSuchAlgorithmException {
		ScratchFileInfo fileInfo = new ScratchFileInfo();
		SHA1InputStream sIn = null;
		
		if (is == null)
			throw new NullPointerException("Null InputStream - can't create file ");
		try {
			

			sIn = new SHA1InputStream(is);

			byte[] buff = new byte[buffSize];
			int n = 0;
			long fileSize = 0;

			while ((n = sIn.read(buff, 0, buff.length)) != -1) {

				out.write(buff, 0, n);
				fileSize += n;
			}
			out.close();
			fileInfo.hash = sIn.getHash();
			fileInfo.inputStreamLength = fileSize;

		} 
		catch(IOException e){
			log.error("Error writing scratch file ", e);
			throw(e);
		}
		catch(RuntimeException e){
			log.error("Error writing scratch file " , e);
			throw(e);
		}
		finally {
			
			if (sIn != null) {
				try {
					sIn.close();
				} 
				
				catch (Exception e) {
					log.error("Error closing inputStream to scratch file " , e);
				}
			}

		}
		return (fileInfo);
	}

	public void putCompoundDocument(CompoundDocumentDescriptor document,
			InputStream is) throws IOException {
		String transactionId = document.getTransactionHandle();

		File scratchFile = ScratchDocument(document, transactionId);
		
		// File documentFile = descriptor.f;
		RepositoryEvent evt = new RepositoryEvent(document, new FileOutputStream(scratchFile));
		for(RepositoryListener l : listeners) {
		    l.onOutput(evt);
        }
		OutputStream out = evt.out;
		File propertyFile = null;
		FileOutputStream outProperties = null;
		InputStream documentStream = null;
		
		boolean success = false;
		try {

			File dir = scratchFile.getParentFile();
			dir.mkdirs();
			if (!dir.exists()) {
				throw new IOException(
						"Unable to find or create directory for file '"
								+ dir.getAbsolutePath() + "'");

			}
			
			// File f = new File(dir, documentFile.getName());

			
			ScratchFileInfo fileInfo = writeScratchFile(is, out);

			//long fileSize = scratchFile.length();
			
			document.setLength(fileInfo.inputStreamLength);
			document.setSha1(fileInfo.hash);
			document.setNDocuments(1);
			Properties props = createProperties(document);
			
			propertyFile = ScratchPropertyFile(document);
			File propertyParentDir = propertyFile.getParentFile();
			if (!propertyParentDir.exists()) {
				boolean dirCreated = propertyParentDir.mkdirs();
				if (!dirCreated)
					throw new IOException("Could not create scratch directory:"
							+ propertyParentDir.getAbsolutePath());
			}
			outProperties = new FileOutputStream(propertyFile);
			props.store(outProperties, "File for account:"
					+ document.getStorageId());

			
			MetadataHandler metadataHandler = document.getMetadataHandler();
			
			// Read the data back from the respository to process data.
			// Not very efficient. Would be much smoother if everything
			// could be processed as a stream before encryption.
			if (metadataHandler != null){
				
			    evt.in = new FileInputStream(scratchFile);
			    for(RepositoryListener l : listeners) {
			        l.onInput(evt);
                }
			    documentStream = evt.in;
				
				Object metadata = metadataHandler.addDocument(document, documentStream);
				ThumbnailGenerator t = metadataHandler.getThumbnailGenerator();
				if (t != null){
					
					File thumbnail = ThumbnailFile(document);
					if (!thumbnail.exists()){
						DicomMetadata dicomMetadata = (DicomMetadata) metadata;
					
						t.generateThumbnail(scratchFile, thumbnail, dicomMetadata);
					}
				}
				String seriesInstanceUID = ((DicomMetadata) metadata).getSeriesInstanceUid();
				
				if (Str.blank(seriesInstanceUID)){
				    throw new IllegalArgumentException("Empty seriesInstanceUID");
				}
				SHA1 sha1 = new SHA1();
                sha1.initializeHashStreamCalculation();
                
				document.setGuid(sha1.calculateStringHash(seriesInstanceUID));
					 
				
			}
			success = true;

		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(
					"Configuration problem with SHA1 class files", e);
		}
		finally {

			if (!success) {
				scratchFile.delete();
			}
			if (out != null){
				try {
					out.close();
				} catch (Exception e) {
					log.error("Error closing file "
							+ scratchFile.getAbsolutePath(), e);
				}
			}
			if (outProperties != null) {
				try {
					outProperties.close();
				} catch (Exception e) {
					log.error("Error closing file "
							+ propertyFile.getAbsolutePath(), e);
				}
			}
			if (documentStream != null){
				try{
					documentStream.close();
				}
				catch (Exception e) {
					log.error("Error closing stream for parsing metadata ", e);
							
				}
			}
		}

	}

	public Properties getMetadata(DocumentDescriptor document) throws RepositoryException {
        try {
            if(document instanceof SimpleDocumentDescriptor)
                return getMetadata((SimpleDocumentDescriptor) document);
            else if(document instanceof CompoundDocumentDescriptor)
                return (getMetadata((CompoundDocumentDescriptor) document));
            else
                throw new RuntimeException("Unknown document class:" + document.getClass().getCanonicalName());
        }
        catch (IOException e) {
            throw new RepositoryException(e);
        }
	}

	/**
	 * Returns true if the document exists in the repository, false otherwise.
	 * 
	 * The actual test is on the properties file.
	 * 
	 * @param document
	 * @return
	 */
	public boolean exists(DocumentDescriptor document){
	    File propertyFile = null;
	    if (document instanceof SimpleDocumentDescriptor)
            propertyFile = getPropertyFile((SimpleDocumentDescriptor) document);
        else if (document instanceof CompoundDocumentDescriptor)
            propertyFile = getPropertyFile((CompoundDocumentDescriptor) document);
        else
            throw new RuntimeException("Unknown document class:"
                    + document.getClass().getCanonicalName());
	    
	    return(propertyFile.exists());
	}
	
	private Properties getMetadata(SimpleDocumentDescriptor document)
			throws IOException {
	    
	    if(document.getGuid() == null)
	        throw new IllegalArgumentException("Attempt to load metadata for document with null guid");
	        
		File propertyFile = getPropertyFile(document);
		
		if ((propertyFile == null) || (!propertyFile.exists()) ){
		    
		    DocumentDescriptor resolvedDescriptor = indexService.getDocument(document.getStorageId(), document.getGuid());
		    RepositoryEvent evt = new RepositoryEvent(resolvedDescriptor);
		    for(RepositoryListener l : listeners) {
		        l.onFileUnavailable(evt, getFile(document));
		    }
		    propertyFile = getPropertyFile(document);
		}
		
		if(propertyFile == null)   
			throw new NullPointerException("Property file for document missing: " + document);
		
		if (!propertyFile.exists())
			throw new FileNotFoundException("Missing document property file:"
					+ propertyFile.getAbsolutePath());
		
		Properties props = loadProperties(propertyFile);

		return (props);

	}

	private Properties getMetadata(CompoundDocumentDescriptor document)
			throws IOException {
		File propertyFile = getPropertyFile(document);
		if (!propertyFile.exists())
			throw new FileNotFoundException("Missing document property file:"
					+ propertyFile.getAbsolutePath());
		Properties props = loadProperties(propertyFile);

		return (props);
	}

	private Properties loadProperties(File f) throws IOException{
	    
		if(!f.exists()) 
		    return null;
		
		FileInputStream is = null;
		try {
			Properties p = new Properties();
			is = new FileInputStream(f);
			p.load(is);
			return p;
		}
		finally {
		    closeQuietly(is);
		}
	}
	
	/**
	 * Extracts the values of a properties file
	 * @param descriptor
	 * @param p
	 * @return
	 */
	private DocumentDescriptor extractProperties(DocumentDescriptor descriptor, Properties p){
		SimpleDateFormat dateFormat = new SimpleDateFormat(PROPERTY_DATE_FORMAT);
		descriptor.setContentType(p.getProperty(CONTENT_TYPE));
		String sLength = p.getProperty(LENGTH);
		if (sLength != null){
			descriptor.setLength(Long.parseLong(sLength));
		}
		descriptor.setSha1(p.getProperty(SHA_1_HASH));
		descriptor.setDocumentName(p.getProperty(NAME));
		descriptor.setGuid(p.getProperty(GUID));
		descriptor.setRevision(p.getProperty(REVISION));
		try{
		    if(!blank(p.getProperty(CREATION_DATE)))
		        descriptor.setCreationDate(dateFormat.parse(p.getProperty(CREATION_DATE)));
		}
		catch(ParseException e){
			log.error("Can't parse '" + p.getProperty(CREATION_DATE) + "'");
		}
	
		String nDocs = p.getProperty(N_DOCUMENTS);
		if (nDocs != null)
			descriptor.setNDocuments(Integer.parseInt(nDocs));
		
		if("true".equals(p.getProperty(PAYMENT_REQUIRED))) {
		    descriptor.setPaymentRequired(true);
		}
		return(descriptor);
	}
	
	private DocumentDescriptor createDocumentDescriptor (Properties p){
		DocumentDescriptor descriptor = null;
		String parent = p.getProperty(RepositoryFileProperties.PARENT_NAME);
		boolean compound = false;
		if ((parent!= null) && (!"".equals(parent)))
			compound = true;
		if (compound){
			descriptor = new CompoundDocumentDescriptor();
			extractProperties(descriptor, p);
			((CompoundDocumentDescriptor) descriptor).setParentName(parent);
		}
		else{
			descriptor = new DocumentDescriptor();
			extractProperties(descriptor, p);
		}
		return(descriptor);
	}

	private CompoundDocumentDescriptor createCompoundDocumentDescriptor (Properties p){
		CompoundDocumentDescriptor descriptor = new CompoundDocumentDescriptor();
		extractProperties(descriptor, p);
		descriptor.setParentName(p.getProperty(PARENT_NAME));
		return(descriptor);
	}
	
	
	public static Properties createProperties(DocumentDescriptor document) {
		if (document == null)
			throw new NullPointerException("Null DocumentDescriptor; can not obtain properties");
		
		Properties props = new Properties();
		if (document.getContentType() == null){
			document.setContentType("UNINITIALIZED");
		}
		props.setProperty(CONTENT_TYPE, document.getContentType());
		props.setProperty(LENGTH, Long.toString(document.getLength()));
		props.setProperty(CREATION_DATE, DATE_FORMATTER.format(new Date()));
		props.setProperty(SHA_1_HASH, document.getSha1());
		props.setProperty(REVISION, Version.getRevision());
		String name = document.getDocumentName();
		if (name != null)
			props.setProperty(NAME, name);
		String guid = document.getGuid();
		if (guid!=null)
			props.setProperty(GUID, document.getGuid());
		int nDocuments = document.getNDocuments();
		props.setProperty(N_DOCUMENTS, Integer.toString(nDocuments));
		if (document instanceof CompoundDocumentDescriptor){
			CompoundDocumentDescriptor cDocument = (CompoundDocumentDescriptor) document;
			if (cDocument.getParentName() != null)
				props.setProperty(PARENT_NAME, cDocument.getParentName());
			
		}
		
		if(document.getPaymentRequired())
		    props.put(PAYMENT_REQUIRED,"true");
		
		return props;
	}
	
	/**
	 * Updates an existing properties file for a parent of a compound document
	 * when more child documents are added to it.
	 */
	public static Properties mergeProperties(Properties in, DocumentDescriptor doc) {
        in.setProperty(LENGTH, 
                Long.toString(doc.getLength() + Long.parseLong(in.getProperty(LENGTH))));
        in.setProperty(N_DOCUMENTS, 
                Integer.toString(doc.getNDocuments() + Integer.parseInt(in.getProperty(N_DOCUMENTS))));
        if(doc.getPaymentRequired())
            in.put(PAYMENT_REQUIRED,"true");
        return in;
	}
	
	public static Properties createCompoundDocumentProperties(DocumentDescriptor doc){
		Properties props = createProperties(doc);
		return(props);
	}
	
	/**
	 * Returns a transaction id.
	 * <P>
	 * Note that this is really only safe within the context of the storage id
	 * folder. The value is the current time in msec plus the string "_" +
	 * transactionCounter.
	 * <P>
	 * The current time is accurate to a msec or so in Linux and MacOSX. In
	 * Windows there is a 30 or 60 msec time-module thang so we add a
	 * transactionCounter that is incremented for each id.
	 * 
	 * @return
	 */
	private String generateTransactionId() {
		long time = System.currentTimeMillis();
		String transactionHandle = Long.toString(time) + "_"
				+ ++transactionCounter;
		return (transactionHandle);
	}

	/**
	 * Used in CXP2Impl
	 */
	public DocumentDescriptor initializeCompoundDocument(
			DocumentDescriptor document) throws RepositoryException {
	
		String transactionHandle = generateTransactionId();
		document.setTransactionHandle(transactionHandle);
		if (document.getMetadataHandler()!=null)
			document.getMetadataHandler().clear();
		try {
            RepositoryEvent evt = new RepositoryEvent(document);
            for(RepositoryListener l : listeners) {
                l.onBeginStoreDocument(evt);
            }
		}
		catch(TransactionException e) {
			log.error("Error in lower repository layers while initializing transaction for document " + document.toShortString(), e);
			throw new RuntimeException("Error in web services or key generation", e);
		}
		
		log.debug("Initializing transaction for storageid " + document.getStorageId() + " with handle " + transactionHandle);
		activeTransactions.put(transactionHandle, document);
		
		return (document);

	}
	/**
	 * REST API handler; used to incrementally add 
	 * documents to a 'compound' document; Initializes 
	 * repository if this is the first document to arrive.
	 */
	public DocumentDescriptor addCompoundDocument(
            DocumentDescriptor document) throws IOException {
    
	    String transactionHandle = document.getTransactionHandle();
	    if(blank(transactionHandle) || DocumentDescriptor.UNINITIALIZED.equals(transactionHandle)){
	        transactionHandle = generateTransactionId();
	        document.setTransactionHandle(transactionHandle);
	        log.info("Generated transaction handle " + transactionHandle + " for new compound document");
	        activeTransactions.put(transactionHandle, document);
	    }
       
        if (document.getMetadataHandler()!=null)
            document.getMetadataHandler().clear();
        
        try {
            RepositoryEvent evt = new RepositoryEvent(document);
            for(RepositoryListener l : listeners) {
                l.onBeginStoreDocument(evt);
            }
        }
        catch(TransactionException e){
            log.error("Error in web services or key generation", e);
            throw new RuntimeException("Error in web services or key generation", e);
        }
        log.debug("Initializing transaction for storageid " + document.getStorageId() + " with handle " + transactionHandle);
        
        return (document);

    }

	/**
	 * Closes a compoundDocument. It may be re-opened by a subsequent addCompoundDocument().
	 * @param transactionHandle
	 * @return
	 * @throws IOException
	 */
    public DocumentDescriptor closeCompoundDocument(String transactionHandle) throws IOException {
        
        log.info("Closing transaction on compound document " + transactionHandle);
        DocumentDescriptor doc = activeTransactions.get(transactionHandle);
        
        FileOutputStream outProperties = null;
        if(doc == null) 
            throw new IllegalStateException("Null DocumentDescriptor handle for " + transactionHandle);
        
        try {
            activeTransactions.remove(transactionHandle);
            
            File storageDir = StorageDir(doc.getStorageId());
            File transactionDirectory = new File(storageDir, transactionHandle);
            //log.info("Transaction directory is " + transactionDirectory.getAbsolutePath());
            
            String documentSha1 = FileGuid.calculateDirectorySHA1(
                    transactionDirectory, ".properties", SHA_1_HASH);
            
            doc.setSha1(documentSha1);
            
            long guidBytecount = FileUtils.calculateDirectorySize(transactionDirectory, ".properties", LENGTH);
            doc.setLength(guidBytecount);
            
            //doc.setGuid(transactionGuid);
            File[]propFiles = FileUtils.propertyFiles(transactionDirectory);
            doc.setNDocuments(propFiles.length);
            File permanentFile = new File(storageDir, doc.getGuid());
            log.info("Old document GUID is " + documentSha1 + ", new is " + doc.getGuid());
            
            // If the file already exists that is ok - compound documents are 
            // no longer fixed content any more: new documents can be added
            boolean existingCompoundDocument = permanentFile.exists();
            if(existingCompoundDocument) { // exists
                log.info("Desitination folder " + permanentFile + " already exists - moving content there");
                moveAllFiles(transactionDirectory,permanentFile);
            }
            else 
                if(!renameTo(transactionDirectory, permanentFile))  { // doesn't exist - move whole folder
                    throw new IOException("Unable to rename scratch file "
                            + transactionDirectory.getAbsolutePath()
                            + "\n to new permanent file "
                            + permanentFile.getAbsolutePath());
                    
                }
            
            
            
            File propertyFile = propertyFile(doc.getStorageId(), doc.getGuid());
            
            Properties props;
            if(existingCompoundDocument) {
                props = loadProperties(propertyFile);
                props = mergeProperties(props, doc);
            }
            else {
                props = createProperties(doc);
            }
            
            outProperties = new FileOutputStream(propertyFile);
            props.store(outProperties, "Compound document");
            
            RepositoryEvent evt = new RepositoryEvent(doc);
            for(RepositoryListener l : listeners) {
                l.onEndStoreDocument(evt);
            }
            
            MetadataHandler metadataHandler = doc.getMetadataHandler();
            if (metadataHandler != null){
                File metadataFile = metadataFile((CompoundDocumentDescriptor) doc);
                metadataHandler.generateMetadataFile(metadataFile, true);
                metadataHandler.clear();
            }
            
            if(doc.getCreationDate() == null)
                doc.setCreationDate(new Date());
            
            // Generate thumbnails
            
            // Index the document
            indexService.index(doc);
            
            return doc;
        } 
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1 Library failing", e);
        }
        catch(TransactionException e){
            throw new RuntimeException("Error in web services", e);
        }
        catch(JDOMException e){
            throw new RuntimeException("Error generating metadata", e);
        }
        catch (ServiceException e) {
            throw new RuntimeException("Error indexing document", e);
        }
        finally {
            closeQuietly(outProperties);
        }
    }
	   
	/**
	 * Move all files in the source directory to the destination directory.
	 * If a file exists already in the destionation then do nothing.
	 * @param srcDir
	 * @param destDir
	 * @throws IOException 
	 */
	private void moveAllFiles(File srcDir, File destDir) throws IOException {
	    File [] files= srcDir.listFiles();
	    for(File f : files) {
	        File dest = new File(destDir, f.getName());
	        if(dest.exists())
	            continue;
	        
	        moveFile(f, dest);
	    }
    }
	
    public DocumentDescriptor finalizeCompoundDocument(String transactionHandle) throws RepositoryException {
		
		DocumentDescriptor doc = activeTransactions.get(transactionHandle);
		
		FileOutputStream outProperties = null;
		if (doc == null) {
			throw new IllegalStateException(
					"Null DocumentDescriptor handle for " + transactionHandle);
		}
		try {
			activeTransactions.remove(transactionHandle);

			File storageDir = StorageDir(doc.getStorageId());
			File transactionDirectory = new File(storageDir, transactionHandle);
			//log.info("Transaction directory is " + transactionDirectory.getAbsolutePath());
			
			String documentSha1 = FileGuid.calculateDirectorySHA1(
					transactionDirectory, ".properties", SHA_1_HASH);
			//log.debug("Finalizing transaction for storageid=" + doc.getStorageId() + " with handle =" + transactionHandle +
			//		" to compound document guid=" + transactionGuid);
			doc.setSha1(documentSha1);
		
			long guidBytecount = FileUtils.calculateDirectorySize(transactionDirectory, ".properties", LENGTH);
			doc.setLength(guidBytecount);
			
			//doc.setGuid(transactionGuid);
			File[]propFiles = FileUtils.propertyFiles(transactionDirectory);
			doc.setNDocuments(propFiles.length);
			File permanentFile = new File(storageDir, doc.getGuid());
			log.info("Old document GUID is " + documentSha1 + ", new is " + doc.getGuid());
			boolean success = renameTo(transactionDirectory, permanentFile);

			if (!success)
				throw new IOException("Unable to rename scratch file "
						+ transactionDirectory.getAbsolutePath()
						+ "\n to new permanent file "
						+ permanentFile.getAbsolutePath());
			
			Properties props = createProperties(doc);
			
			File propertyFile = propertyFile(doc.getStorageId(), doc.getGuid());
			
			outProperties = new FileOutputStream(propertyFile);
			props.store(outProperties, "Compound document");
			
            RepositoryEvent evt = new RepositoryEvent(doc);
            for(RepositoryListener l : listeners) {
                l.onEndStoreDocument(evt);
            }
			
			MetadataHandler metadataHandler = doc.getMetadataHandler();
			if(metadataHandler != null) {
				File metadataFile = metadataFile((CompoundDocumentDescriptor) doc);
				metadataHandler.generateMetadataFile(metadataFile, false);
				metadataHandler.clear();
			}
			
			if(doc.getCreationDate() == null)
			    doc.setCreationDate(new Date());
			
			// Generate thumbnails
			
			// Index the document
			indexService.index(doc);
			
			return (doc);
		} 
		catch (NoSuchAlgorithmException e) {
			throw new RepositoryException("SHA1 Library failing", e);
		}
		catch(TransactionException e){
			throw new RepositoryException("Error in web services", e);
		}
		catch(JDOMException e){
			throw new RepositoryException("Error generating metadata", e);
		}
        catch (ServiceException e) {
			throw new RepositoryException("Error indexing document", e);
        }
        catch (IOException e) {
            throw new RepositoryException(e);
        }
		finally{
			if (outProperties != null){
				try{outProperties.close();}catch(Exception e){;}
			}
		}
	}

	public static File StorageDir(String storageId) {
		if(storageId == null) 
			throw new NullPointerException("Null storageId");
		File storageDir = new File(rootDirectory, storageId);
		return (storageDir);
	}

	/**
	 * Returns a File for the document in the repository. The file might or
	 * might not (yet) exist.
	 * 
	 * @param doc
	 * @return
	 */
	private StorageFileDescriptor StorageFile(DocumentDescriptor doc) {
		if (doc instanceof CompoundDocumentDescriptor) {
			return (StorageFile((CompoundDocumentDescriptor) doc, false));
		} else if (doc instanceof SimpleDocumentDescriptor)
			return (StorageFile((SimpleDocumentDescriptor) doc));
		else{
			return(MinimalStorageFile(doc));
		}
	}

	// Only returns the file reference. No type set.
	public static StorageFileDescriptor MinimalStorageFile(DocumentDescriptor doc){
		if (doc == null)
			throw new NullPointerException("Null document descriptor");
		StorageFileDescriptor descriptor = new StorageFileDescriptor();
		String storageId = doc.getStorageId();
		if (storageId == null)
			throw new NullPointerException("Null storage id");
		File storageDir = StorageDir(storageId);
		String guid = doc.getGuid();
		if (guid==null){
			throw new NullPointerException("Null guid");
		}
		
		File docFile = new File(storageDir, guid);
		descriptor.f = docFile;
		return(descriptor);
	}
	private StorageFileDescriptor StorageFile(CompoundDocumentDescriptor doc,
			boolean createDefaultName) {
		StorageFileDescriptor descriptor = new StorageFileDescriptor();
		File storageDir = StorageDir(doc.getStorageId());
		String name = doc.getDocumentName();
		File documentFile = null;
		String guid = doc.getGuid();
		if (name == null){
			log.debug("Null document name for doc" + doc.toShortString());
			doc.setDocumentName(guid);
			name = guid;
		}
			
		if (guid != null) {
			File documentDir = new File(storageDir, doc.getGuid());
			
			documentFile = new File(documentDir, name);
		}
		descriptor.fileType = FILE_TYPES.COMPOUND_ELEMENT;
		descriptor.f = documentFile;
		descriptor.propertyFile = getPropertyFile(doc);
		return (descriptor);
	}

	private StorageFileDescriptor StorageFile(SimpleDocumentDescriptor doc) {
		StorageFileDescriptor descriptor = new StorageFileDescriptor();
		File storageDir = StorageDir(doc.getStorageId());
		String localFileName = nvl(doc.getGuid(), doc.getSha1());
		log.info("StorageFileDescriptor A " + doc.getStorageId() + " " + localFileName);
		File documentFile = null;
		if (localFileName != null)
			documentFile = new File(storageDir, localFileName);
		descriptor.fileType = FILE_TYPES.SIMPLE;
		descriptor.f = documentFile;
		descriptor.propertyFile = getPropertyFile(doc);
		return (descriptor);
	}

	/**
	 * For a simple document - a scratch file is created in the storageId
	 * directory with the name of the transaction id. This file is renamed with
	 * the SHA-1 hash value after this is calculated.
	 * 
	 * @param doc
	 * @param transactionId
	 * @return
	 * @throws IOException
	 */
	private File getScratchFileForDocument(SimpleDocumentDescriptor doc,
			String transactionId) throws IOException {
		File storageDir = StorageDir(doc.getStorageId());
		if (!storageDir.exists()) {
			boolean success = storageDir.mkdir();
			if (!success)
				throw new IOException("Unable to create storage id directory "
						+ storageDir.getAbsolutePath());
		}
		File documentFile = null;
		documentFile = new File(storageDir, transactionId);

		return (documentFile);
	}

	/**
	 * Creates a scratch document in the transaction folder.
	 * Note that if a DocumentName is not specified that one will be created.
	 * @param doc
	 * @param transactionId
	 * @return
	 * @throws IOException
	 */
	private File ScratchDocument(CompoundDocumentDescriptor doc,
			String transactionId) throws IOException {
		File storageDir = StorageDir(doc.getStorageId());
		if (!storageDir.exists()) {
			boolean success = storageDir.mkdir();
			if (!success)
				throw new IOException("Unable to create storage id directory "
						+ storageDir.getAbsolutePath());
		}
		File transactionDir = new File(storageDir, transactionId);
		if (!transactionDir.exists()) {
			boolean success = transactionDir.mkdir();
			if (!success)
				throw new IOException("Unable to create transaction directory "
						+ transactionDir.getAbsolutePath());
		}
		String name = doc.getDocumentName();
		if (name == null){
			name = generateTransactionId();
			doc.setDocumentName(name);
		}
			
		File documentFile = null;
		
		documentFile = new File(transactionDir, name);

		return (documentFile);
	}

	/**
	 * Returns a File reference to the property file associated with this
	 * document. The file may or may not (yet) exist.
	 * 
	 * @param doc
	 * @return
	 */
	public static File getPropertyFile(SimpleDocumentDescriptor doc) {
	    
	    String identifier = nvl(doc.getGuid(), doc.getSha1());
		if (identifier == null)
			return null;
		
		File storageDir = StorageDir(doc.getStorageId());
		File documentFile = null;
		String localFileName = identifier + METADATA_SUFFIX;
		documentFile = new File(storageDir, localFileName);
		return documentFile;
	}
	

	public static File CompoundFileMemberPropertyFile(CompoundDocumentDescriptor doc) {
		
		String name = doc.getDocumentName();
		
		if (name == null)
			throw new NullPointerException(
					"Document name is null; must be defined for writing out scratch property file");

		File storageDir = StorageDir(doc.getStorageId());
		File guidDir = new File(storageDir,doc.getGuid());
		
		
		File documentFile = new File(guidDir, name + METADATA_SUFFIX);

		return (documentFile);
	}
	private File ScratchPropertyFile(CompoundDocumentDescriptor doc) {
        String transactionId = doc.getTransactionHandle();
        String name = doc.getDocumentName();
        if (transactionId == null)
            throw new NullPointerException(
                    "TransactionID is null; must be defined for writing out scratch property file");
        if (name == null)
            throw new NullPointerException(
                    "Document name is null; must be defined for writing out scratch property file");

        File storageDir = StorageDir(doc.getStorageId());
        File documentDir = new File(storageDir, transactionId);
        File documentFile = new File(documentDir, name + METADATA_SUFFIX);

        return (documentFile);
    }
	private File ThumbnailFile(CompoundDocumentDescriptor doc) {
		String transactionId = doc.getTransactionHandle();
		String name = doc.getDocumentName();
		if (transactionId == null)
			throw new NullPointerException(
					"TransactionID is null; must be defined for writing out scratch property file");
		if (name == null)
			throw new NullPointerException(
					"Document name is null; must be defined for writing out scratch property file");

		File storageDir = StorageDir(doc.getStorageId());
		File documentDir = new File(storageDir, transactionId);
		File documentFile = new File(documentDir, name + THUMBNAIL_SUFFIX);

		return (documentFile);
	}
	/**
	 * Returns a reference for the metadata file for
	 * a compound document.
	 * @param doc
	 * @return
	 */
	public static File metadataFile(CompoundDocumentDescriptor doc) {
		String guid = doc.getGuid();
		
		if (guid == null)
			return (null);
		

		File storageDir = StorageDir(doc.getStorageId());
		File documentDir = new File(storageDir, guid);
		File metadataFile = new File(documentDir, METADATA_FILENAME);

		return (metadataFile);
	}

	/**
	 * Returns a reference for a property file for a document <i>within</i>
	 * a compound document.
	 * @param doc
	 * @return
	 */
	public static File getPropertyFile(CompoundDocumentDescriptor doc) {
		String guid = doc.getGuid();
		String name = doc.getDocumentName();
		if (guid == null)
			return (null);
		if (name == null)
			return (null);

		File storageDir = StorageDir(doc.getStorageId());
		File documentDir = new File(storageDir, guid);
		File documentFile = new File(documentDir, name + METADATA_SUFFIX);

		return (documentFile);
	}
	/**
	 * Returns a file reference to a property file for a compound document.
	 * In this implementation - this is a directory.
	 * 
	 * @param storageId
	 * @param guid
	 * @return
	 */
	public static File propertyFile(String storageId, String guid) {
		
		

		File storageDir = StorageDir(storageId);
		File documentFile = new File(storageDir, guid + METADATA_SUFFIX);
		

		return (documentFile);
	}

	private static class StorageFileDescriptor {
		File f = null;

		FILE_TYPES fileType = null;

		String transactionId = null;

		File propertyFile = null;

		public String toString() {
			StringBuffer buff = new StringBuffer("<StorageFileDescriptor ");
			buff.append(" file='");
			if (f == null)
				buff.append("");
			else
				buff.append(f.getAbsoluteFile());
			buff.append("' type='");
			buff.append(fileType);
			buff.append("' transactionid='");
			buff.append(transactionId);
			buff.append("' propertyFile='");
			if (propertyFile == null)
				buff.append("");
			else
				buff.append(propertyFile.getAbsoluteFile());
			buff.append("'/>");
			return (buff.toString());

		}
	}
	/**
	 * Renames the file to its permanent name. If there is an existing file 
	 * by this name it is overwritten.
	 * @param from
	 * @param to
	 * @return
	 * @throws IOException
	 */
	private boolean renameTo(File from, File to) throws IOException{
		
		boolean success = false;
		if (to.exists()){
			log.info("Found existing permanent file  " + to.getAbsolutePath()  + 
					", will overwrite with file " +from.getAbsolutePath() );
			if (to.isDirectory())
				success = deleteDirectory(to);
			else
				success = to.delete();
			
			if (!success)
				throw new IOException("Can't delete existing destination file in rename: " + to.getAbsolutePath());
		}
		success = from.renameTo(to);
		
		return(success);
	}

	private boolean deleteDirectory(File dir) throws IOException{
		boolean success = false;
		if (!dir.isDirectory()) 
			throw new RuntimeException("File is not a directory; can't delete" + dir.getAbsolutePath());
		
		File files[] = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			success = files[i].delete();
			if (!success) {
				throw new IOException("Deletion failed: "
						+ files[i].getAbsolutePath());
			}
		}
		success = dir.delete();
		return(success);
	}
	
	/**
	 * Returns a list of documents for a given storageId on a specified repository.
	 * 
	 * @param storageID
	 * @return
	 * @throws IOException
	 */
	public DocumentDescriptor[] getDocumentDescriptors(String storageId) throws RepositoryException {
		try {
            File storageDir = StorageDir(storageId);
            if (!storageDir.exists())
            	return(null);
            
            File propertyFiles[] = FileUtils.propertyFiles(storageDir);
            DocumentDescriptor docs[] = new DocumentDescriptor[propertyFiles.length];
            for (int i=0;i<docs.length;i++){
            	Properties p = loadProperties(propertyFiles[i]);
            	docs[i] = createDocumentDescriptor(p);
            }
            return(docs);
        }
		catch (IOException e) {
		    throw new RepositoryException(e);
        }
	}
	
	/**
	 * Returns alist of 
	 * @param storageId
	 * @param guid
	 * @return
	 * @throws IOException
	 */
	public CompoundDocumentDescriptor[] getCompoundDocumentDescriptors(String storageId, String guid) throws RepositoryException {
		File storageDir = StorageDir(storageId);
		if (!storageDir.exists())
			return(null);
		
		File docFolder = new File(storageDir, guid);
		File propertyFiles[] = FileUtils.propertyFiles(docFolder);
		CompoundDocumentDescriptor docs [] = new CompoundDocumentDescriptor[propertyFiles.length];
		try {
            for (int i=0;i<docs.length;i++){
            	Properties p = loadProperties(propertyFiles[i]);
            	
            	docs[i] = createCompoundDocumentDescriptor(p);
            	docs[i].setGuid(guid);
            	docs[i].setStorageId(storageId);

            }
            return(docs);
        } 
		catch(IOException e) {
		    throw new RepositoryException(e);
        }
	}
	
	public InputStream getCompoundDocumentManifest(CompoundDocumentDescriptor documentDescriptor) throws RepositoryException {
		try {
            File metadata = metadataFile(documentDescriptor);
            if (metadata == null){
            	throw new NullPointerException("Repository metadata returned null");
            }
            
            // It might be that the meta data needs to be restored from backup
            if(!metadata.exists()) {
                RepositoryEvent evt = new RepositoryEvent(documentDescriptor);
                for(RepositoryListener l : listeners) {
                    l.onFileUnavailable(evt, metadata);
                }
            }
            
            if(!metadata.exists())
            	throw new RepositoryException("File " + metadata.getAbsolutePath() + " not found",
            	        new FileNotFoundException("File " + metadata + " not found"));
            
            return new FileInputStream(metadata);
        } 
		catch (FileNotFoundException e) {
		    throw new RepositoryException(e);
        }
	}
	
	public void deleteCompoundDocumentManifest(CompoundDocumentDescriptor documentDescriptor) throws RepositoryException {
	    File metadata = metadataFile(documentDescriptor);
	    if (metadata == null)
	        throw new NullPointerException("Repository metadata returned null");
	    
	    if(metadata.exists()) {
	        if(!metadata.delete())
	            throw new RepositoryException("Unable to delete meta data file: " + metadata.getAbsolutePath());
	    }
	    
	    if(metadata.getParentFile().listFiles().length == 0)
	        metadata.getParentFile().delete();
	}
	
	public void putCompoundDocumentManifest(CompoundDocumentDescriptor documentDescriptor, InputStream in){
		;
	}
	public void setRepositoryName(String repositoryName){
		this.repositoryName = repositoryName;
	}
	public String getRepositoryName(){
		return(this.repositoryName);
	}
}
