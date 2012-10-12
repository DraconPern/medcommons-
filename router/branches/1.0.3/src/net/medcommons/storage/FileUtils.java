package net.medcommons.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.AES;
import net.medcommons.router.services.repository.DocumentNotFoundException;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;

import org.apache.log4j.Logger;

/**
 * SWD Notes: 
 * This file should be deleted and utilities should be pointed at the net.medcommons.modules files.
 * 
 * A set of simple utilities to resolve abstract addresses (guids) into file
 * references for direct file access.
 * <P>
 * MedCommons has two distinct ways of accessing a file from the storage system:
 * <ol>
 * <li> Via WebDAV. 
 * <li> Direct file access.
 * </ol>
 * Either of these mechanisms can be used to refer to a file. However - the names of the
 * files in the file system is not the same as the WebDAV one. 
 * The name of a file for (say) a PDF might be:
 * <pre>
 * 0c0e565f57f694c920efc6d6f440fa670e345f6d
 * </pre>
 * but the name in WebDAV would be 
 * <pre>
 * 0c0e565f57f694c920efc6d6f440fa670e345f6d_1.0
 * </pre>
 * Even though WebDAV has been configured not to perform file versioning a version number is still 
 * appended to the filename.
 * <P>
 * Need to describe directory structures; how the configuration of Domain.xml and config.xml have to be 
 * synchronized.
 * <P>
 * Need to describe the temporary file system used by the DICOM import.
 * <P>
 * Need to factor this into two classes - a utils and a encrypted file system class.
 * @author sean
 * 
 */

public class FileUtils {
	private static File imageRootDirectory = null;

	private static File fixedContentRootDirectory = null;

	final static String fixedContentPrefix = "/fixedcontent/";
	private static File webdavContentDirectory = null;
	  private static File webdavMetadataDirectory = null;
	  public final static String WEBDAV_VERSION_SUFFIX = "_1.0";
	  public final static String WEBDAV_METADATA_SUFFIX = ".def.xml";

	final static Logger log = Logger.getLogger(FileUtils.class);
	
	

	/**
	 * Resolves the GUID and the root file system to a filename.
	 * 
	 * In the future this will be a much more complex function. For now it
	 * generates a filename that is a function of the GUID: {root
	 * directory}/{first three letters of GUID}/{GUID}
	 * 
	 * @param rootDir
	 * @param guid
	 * @return
	 */
	public static File resolveGUIDAddress(File rootDir, String guid) {
		if (rootDir == null)
			throw new NullPointerException("root directory is null");
		if (guid == null)
			throw new NullPointerException("guid is null");
		File file = rootDir;
		// file = new File(file, guid.substring(0, 3));
		file = new File(file, guid);
		
		
		return (file);
	}

	public static File resolveGUIDAddress(String guid) throws IOException,
			ConfigurationException {
		return (resolveGUIDAddress(getRootDirectory(), guid));
	}

	public static void init() throws IOException, ConfigurationException {
		LocalFileRepository localFileRepository = (LocalFileRepository) RepositoryFactory.getLocalRepository();
		imageRootDirectory = localFileRepository.getScratchDirectory();
		
		fixedContentRootDirectory = localFileRepository.getScratchDirectory();
		
		log.info("(Obsolete) Fixed content directory set to:" + fixedContentRootDirectory.getAbsolutePath());
		log.info("(Obsolete) Temp image directory set to:   " + imageRootDirectory.getAbsolutePath());
	}

	 /**
	   * Returns the root directory for writing out temporary files. DICOM import and uploaded files from CXP are 
	   * placed in this directory before being transferred to the repository.
	   * <p>
	   * This is a temporary location; the information is written out unencrypted. The
	   * data is moved out of this directory and placed into the repository as soon 
	   * as possible.
	   * <p>
	   * Although writing unencrypted data to disk is risky - several DICOM
	   * implementations will fail if the data is read slowly over the network.
	   * TODO Look at using AES encryption for local storage with an in-memory key.
	   * 
	   * @return
	   * @throws IOException
	   * @throws ConfigurationException
	   * @throws FileNotFoundException
	   */
	public static File getRootDirectory() throws IOException,
			ConfigurationException {
		if (imageRootDirectory == null)
			init();
		return (imageRootDirectory);
	}

	public static File getFixedContentRootDirectory() throws IOException,
			ConfigurationException {
		if (fixedContentRootDirectory == null)
			init();
		return (fixedContentRootDirectory);
	}

	/**
	 * Fixed content collections are of the form:
	 * 
	 * <pre>
	 *  /fixedcontent/8c2ff8f8b86ad82b9ddb09ddbe1de669f4a439e2/f906d68d752eb0e7455c67b8ac809dfca9718198
	 * </pre>
	 * 
	 * Fixed content simple document (such as a CCR or PDF) has only one level
	 * of directory.
	 * 
	 * <pre>
	 *  /fixedcontent/1bec25d5b857a0620cead1840177673c400e99b0
	 * </pre>
	 * 
	 * Throws an exception if the root collection is not 'fixedcontent'. The
	 * assumptions for how files are organized may differ in non-fixed content
	 * directories; this needs to be enforced here.
	 * 
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	public static boolean isFixedContentCollection(String uri)
			throws IOException {

		boolean isCollection = false;
		int fixedContentIndex = uri.indexOf(fixedContentPrefix);
		if (fixedContentIndex != 0)
			throw new IOException("Fixed content URI is not in '"
					+ fixedContentPrefix + "' directory");

		String nodename = getNodenameFromURI(uri);
		int collectionDelimiterIndex = nodename.indexOf("/");
		isCollection = (collectionDelimiterIndex == -1) ? false : true;
		//log.error("Object " + nodename + " is a collection:" + isCollection);
		return (isCollection);
	}

	/**
	 * Returns the UID associated with the URI. In the case of a collection the
	 * UID is the name of the collection; in the case of a single document it is
	 * the SHA-1 hash of the document itself.
	 * <p>
	 * The UID is the index for the decryption keys.
	 * 
	 * @param uri
	 * @return
	 */
	public static String parseUIDValueFromURI(String uri) throws IOException {

		String sha1Value = null;
		String nodename = getNodenameFromURI(uri);

		if (isFixedContentCollection(uri)) {
			int collectionDelimiterIndex = nodename.indexOf("/");
			sha1Value = nodename.substring(0, collectionDelimiterIndex);

		} else {
			sha1Value = nodename;
		}
		
		return (sha1Value);
	}

	/**
	 * Returns the SHA1 hash associated with the URI. In the case of a
	 * collection this is the name of the object in the collection; in the case
	 * of a single document is the name of the document itself.
	 * <p>
	 * This assumes that the SHA-1 hashes have been validated on import.
	 * 
	 * @param uri
	 * @return
	 */
	public static String parseSHA1ValueFromURI(String uri) throws IOException {

		String sha1Value = null;
		String nodename = getNodenameFromURI(uri);

		if (isFixedContentCollection(uri)) {
			int collectionDelimiterIndex = nodename.indexOf("/");
			sha1Value = nodename.substring(collectionDelimiterIndex + 1);

		} else {
			sha1Value = nodename;
		}
	
		return (sha1Value);
	}

	public static String getNodenameFromURI(String uri) {
		return (uri.substring(fixedContentPrefix.length()));
	}


	


	  
	

		
	  public static InputStream getFixedContentInputStream(File f, SecretKeySpec sks) throws RepositoryException{
		  // Open the file as a stream
		    FileInputStream encryptedStream = null;
		    InputStream is = null;
		    
		    try{
		    	AES aes = new AES();                
              
		    	encryptedStream = new FileInputStream(f);
                
                log.debug("Creating aes inputstream");
		    	is = aes.createInputStream(encryptedStream, sks);
		    }
		    catch(FileNotFoundException e){
		    	throw new RepositoryException("File not found:" + f.getAbsolutePath(), e);
		    }
		    catch(Exception e){
		    	throw new RepositoryException("Exception:" + f.getAbsolutePath(), e); // Need to clean up
		    }
		   
		    return(is);
	  }
	  
	  /**
	   * Returns a decrypted input stream to calling program.
	   * 
	   * TODO: Currently all exceptions are caught and rethrown as RepositoryExceptions.
	   * We may want to make this finer-grained - decryption exceptions might be separated
	   * from I/O exceptions.
	   * @param is
	   * @param sks
	   * @return
	   * @throws RepositoryException
	   */
	  public static InputStream getFixedContentInputStream(InputStream is, SecretKeySpec sks) throws RepositoryException{
		  // Open the file as a stream
		    InputStream decryptedStream = null;
		    try{
		    	AES aes = new AES();
		    	decryptedStream = aes.createInputStream(is, sks);
		    }
		    catch(Exception e){
		    	throw new RepositoryException("Unable to create decrypted data stream:", e);
		    }
		    
		    
		   
		    return(decryptedStream);
	  }
	  /**
	   * Returns a decrypted input stream from the local file repository.
	   * TODO: Think about calculating the SHA-1 hash on output; throwing an error if it doesn't match.
	   * TODO: Think about replacing this method with a new one where the decryption key is specified. The 
	   * lower level libraries (like this one) shouldn't be making choices about which calling routine can/can't
	   * have keys.
	   * 
	   * @param is
	   * @param uidValue
	   * @param sha1Value
	   * @deprecated
	   * @return
	   * @throws RepositoryException
	   */

	  public static InputStream getFixedContentInputStream(InputStream is, String uidValue, String sha1Value, SecretKeySpec key) throws RepositoryException{
		  // Open the file as a stream
		    
		    
		    AES aes = new AES();
		   
		    // Need to put a switch in here.
		    // If the file isn't encrypted - then 'encryptedStream' needs to be assigned
		    // to 'inputStream' below. Or - maybe this should be located in aes.createInputStream?
		    InputStream inputStream = null;
		    
		  
		   
		    try{
		    	//log.error("Is avail:" + is.available());
		    	// if key isn't dummy - 
		    	inputStream = aes.createInputStream(is, key);
		    }
		    catch(InvalidAlgorithmParameterException e){
		    	throw new RepositoryException("Failed to get decrypted input stream for uid " + uidValue + 
		    			", sha1Value = " + sha1Value, e);
		    }
		    catch(InvalidKeyException e){
		    	throw new RepositoryException("Invalid key for UID " +  uidValue + 
		    			", sha1Value = " + sha1Value, e);
		    }
		    catch(IOException e){
		    	throw new RepositoryException("Failed to get input stream for UID " +  uidValue + 
		    			", sha1Value = " + sha1Value, e);
		    } 
		    catch(NoSuchAlgorithmException e){
		    	throw new RepositoryException("Internal Server Error " , e);
		    }
		    catch(NoSuchPaddingException e){
		    	throw new RepositoryException("Internal Server Error " , e);
		    }
		    return(inputStream);
	  }

	  	/**
	  	 * Recursive directory delete.
	  	 * <P>
	  	 * Note that sometimes this fails - which seems to not occur
	  	 * after a System.gc() - which would imply that there is a 
	  	 * finalizer that has a handle to one of the input or output
	  	 * streams for the file.
	  	 * <P> 
	  	 * Perhaps this should throw an exception on failure?
	  	 * @param dir
	  	 * @return
	  	 */
	    public static boolean deleteDir(File dir) {
	    	log.debug("About to delete file:" + dir.getAbsolutePath());
	        if (dir.isDirectory()) {
	            String[] children = dir.list();
	            for (int i=0; i<children.length; i++) {
	                boolean success = deleteDir(new File(dir, children[i]));
	               if (!success) {
	                    return false;
	                }
	                
	            }
	        }
	        
	        boolean status  = dir.delete();
	        // The directory is now empty so delete it
	        if (!status){
	        	log.info("Failed to delete file:" + dir.getAbsolutePath() + " exists " + dir.exists());
	        }
	        else
	        	log.debug("==Successfully deleted file:" + dir.getAbsolutePath());
	        return status;
	    }

}
