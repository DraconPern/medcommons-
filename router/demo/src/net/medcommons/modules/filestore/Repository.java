package net.medcommons.modules.filestore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.medcommons.modules.services.interfaces.CompoundDocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.router.services.repository.RepositoryException;

/**
 * Very basic repository API.
 * 
 * Repositories are locations where files are stored. Different implementations may have different
 * policies and behaviors. 
 * <P>
 * At its core - the repository saves files and some metadata; these files and metadata can be retreived.
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
 *                                
 * TODO:
 * Define difference between properties and manifests.
 * 
 * @author mesozoic
 *
 */
public interface Repository extends RepositoryConstants{
	
	public void init(Properties properties) throws IOException;
	
	/**
	 * Places a document into the repository. Also creates a separate metadata
	 * object (either separate file or in a database).
	 * <P>
	 * @see RepositoryFileProperties for dictionary of available metadata.
	 * 
	 * 
	 * @param document
	 * @param is
	 */
	public void putInputStream(DocumentDescriptor document, InputStream is) throws RepositoryException;
	
	/**
	 * Deletes a document from a repository.
	 * 
	 * @param document
	 */
	public void delete(DocumentDescriptor document) throws RepositoryException;
	
	/**
	 * Returns a document from the repository.
	 * @param document
	 * @return
	 */
	public InputStream get(DocumentDescriptor document) throws RepositoryException;
	
	/**
	 * Returns a list of properties associated with the document.
	 * Not sure if this the right interface - what if the underlying structure 
	 * isn't Properties? 
	 * 
	 * @param document
	 * @return
	 */
	public Properties getMetadata(DocumentDescriptor document) throws RepositoryException;
	
	/**
	 * Returns a list of documents for a given storageId on a specified repository.
	 * 
	 * @param storageID
	 * @return
	 * @throws IOException
	 */
	public DocumentDescriptor[] getDocumentDescriptors(String storageId) throws RepositoryException;
	
	/**
	 * Returns alist of 
	 * @param storageId
	 * @param guid
	 * @return
	 * @throws IOException
	 */
	public CompoundDocumentDescriptor[] getCompoundDocumentDescriptors(String storageId, String guid) throws RepositoryException;
	
	/**
	 * Initializes a compound document transaction. The returned DocumentDescriptor
	 * contains a transaction handle which must be put into all documents in the same
	 * transaction. 
	 * <P>
	 * Transactions are initialized before the first call to putInputStream(); finalizeCompoundDocument()
	 * is called after the last putInputStream(). The guid for the compound document is calculated
	 * from the contents of all of the documents 'put' into the system (with the exception of the 
	 * special metadata.xml which is calculated by the repository).
	 * 
	 * @param document
	 * @return
	 */
	public DocumentDescriptor initializeCompoundDocument(DocumentDescriptor document) throws RepositoryException;
	
	/**
	 * Finalizes a component document transaction.
	 * The getGuid() in the returned DocumentDescriptor is the guid of the
	 * compound document.
	 */
	public DocumentDescriptor finalizeCompoundDocument(String transactionHandle) throws RepositoryException;
	
	/**
	 * Returns a XML manifest file for the contents of a compound document.
	 * @param documentDescriptor
	 * @return 
	 */
	public InputStream getCompoundDocumentManifest(CompoundDocumentDescriptor documentDescriptor) throws RepositoryException;
	
	/**
	 * Stores a manifest file for a compound document. If encryption is enabled the manifest file
	 * is encrypted with the same key as the <i>storageId, guid</i> document on this node.
	 * @param documentDescriptor
	 * @param in
	 */
	public void putCompoundDocumentManifest(CompoundDocumentDescriptor documentDescriptor, InputStream in);
		
	public void setRepositoryName(String repositoryName);
	
	public String getRepositoryName();
	
	public void setRootDirectory(File rootDirectory);
	
	

}
