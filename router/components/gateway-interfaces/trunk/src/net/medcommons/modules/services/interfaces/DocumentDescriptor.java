package net.medcommons.modules.services.interfaces;

import java.security.Key;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * A detailed description of a document.
 * 
 * @author ssadedin
 */
public class DocumentDescriptor {
    
    /**
     * Constant indicating that a field is uninitialized (not sure why null is not good enough?)
     */
	public final static String UNINITIALIZED = "UNINITIALIZED";
	
	/**
	 * An id for this descriptor, if it has one
	 * (Used by index service to store document descriptors)
	 */
	private Long id;
	
	/**
	 * The owner of the document
	 */
	private String storageId;
	
	/**
	 * The name if any, of the document
	 */
	private String documentName;
	
	/**
	 * Identifying guid - in a compound document
	 * this may be the guid identifying the collection
	 * as opposed to the individual document. See {@link #sha1}.
	 */
	private String guid = null;
	
	/**
	 * The content type of the document
	 */
	private String contentType;
	
	/**
	 * The size of the document in bytes
	 */
	private long length = -1;
	
	private String transactionHandle = UNINITIALIZED;
	
	/**
	 * The SHA-1 hash of the content of the document
	 */
	private String sha1 = UNINITIALIZED;
	
	/**
	 * Encryption key of the document, if encryption used and key known
	 */
	private Key key = null;
	
	/**
	 * Decryption key of the document, if encryption used and key known 
	 */
	private SecretKeySpec decryptionKey = null;
	
	/**
	 * Tracking number
	 */
	private String confirmationCode = null;
	
	/**
	 * PIN or other secret for accessing the document
	 */
	private String registrySecret  = null;
	
	private MetadataHandler metadataHandler = null;
	
	/**
	 * Arbitrary metadata about the document. This may be
	 * added by the metadata handler when it processes the document.
	 */
	private Set<DocumentMetadata> metadata = new HashSet<DocumentMetadata>();
	
	/**
	 * ??
	 */
	private String revision = null;
	
	/**
	 * Date / Time at which document was created
	 */
	private Date creationDate = null;
	
	/**
	 * Whether outstanding payment is required to access this document
	 */
	private boolean paymentRequired = false;
	
	/**
	 * true if the key is 'new' and not yet stored in the key cache.
	 * false if the key is stored in the key cache.
	 * 
	 * If the identity of the document is not yet known (e.g., the sha-1 hash
	 * hasn't been calculated) then the value can't be stored in the keyCache.
	 * But the key is needed first to stream the encrypted data to disk.
	 */
	private boolean keyStored = false;
	
	/**
	 * Number of documents associated with this document descriptor.
	 * For Simple documents the number is zero. For Compound documents it is the number
	 * of child documents in the directory.
	 */
	private int nDocuments = -1;
	
	
	public String getStorageId(){
		return(this.storageId);
	}
	public String getDocumentName(){
		return(this.documentName);
	}
	public String getGuid(){
		return(this.guid);
	}
	public String getContentType(){
		return(this.contentType);
	}
	public long getLength(){
		return(this.length);
	}
	
	public void setStorageId(String storageId){
		this.storageId = storageId;
	}
	public void setDocumentName(String documentName){
		this.documentName = documentName;
	}
	public void setGuid(String guid){
		this.guid = guid;
	}
	public void setContentType(String contentType){
		this.contentType = contentType;
	}
	public void setLength(long length){
		this.length = length;
	}
	public void setTransactionHandle(String transactionHandle){
		this.transactionHandle = transactionHandle;
	}
	public String getTransactionHandle(){
		return(this.transactionHandle);
	}
	public void setSha1(String sha1){
		this.sha1 = sha1;
	}
	public String getSha1(){
		return(this.sha1);
	}
	public void setNDocuments(int nDocuments){
		this.nDocuments = nDocuments;
	}
	public int getNDocuments(){
		
		return(this.nDocuments);
	}
	public void setKey(Key key){
		this.key = key;
	}
	public Key getKey(){
		return(this.key);
	}
	public void setKeyStored(boolean keyStored){
		this.keyStored = keyStored;
	}
	 
	public boolean getKeyStored(){
		return(this.keyStored);
	}
	
	public void setDecryptionKey(SecretKeySpec decryptionKey){
		this.decryptionKey = decryptionKey;
	}
	public SecretKeySpec getDecryptionKey(){
		return(this.decryptionKey);
	}
	
	public void setRegistrySecret(String registrySecret){
		this.registrySecret = registrySecret;
	}
	public String getRegistrySecret(){
		return(this.registrySecret);
	}
	public void setConfirmationCode(String confirmationCode){
		this.confirmationCode = confirmationCode;
	}
	public String getConfirmationCode(){
		return(this.confirmationCode);
	}
	
	public void setMetadataHandler(MetadataHandler metadataHandler){
		this.metadataHandler = metadataHandler;
	}
	public MetadataHandler getMetadataHandler(){
		return(this.metadataHandler);
	}
	public void setRevision(String revision){
		this.revision = revision;
	}
	public String getRevision(){
		return(this.revision);
	}
	
	public void setCreationDate(Date creationDate){
		this.creationDate = creationDate;
	}
	public Date getCreationDate(){
		return(this.creationDate);
	}
	public String toString(){
	    return ReflectionToStringBuilder.toStringExclude(this, new String[] { "metadata" });
	}
	
	public String toShortString(){
		StringBuffer buff = new StringBuffer("document[storageid=");
		buff.append(storageId);
		buff.append(",guid="); 
		buff.append(guid);
		buff.append(",name=");
		buff.append(documentName);
		buff.append("]");
		return(buff.toString());
	}
	
    public boolean getPaymentRequired() {
        return paymentRequired;
    }
    
    public void setPaymentRequired(boolean paymentRequired) {
        this.paymentRequired = paymentRequired;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Set<DocumentMetadata> getMetadata() {
        return metadata;
    }
    public void setMetadata(Set<DocumentMetadata> metadata) {
        this.metadata = metadata;
    }
    
    public String getMetadataValue(String key) {
        if(key == null)
            throw new IllegalArgumentException("Must provide non-null key for meta data attribute");
        
        for(DocumentMetadata m : this.metadata) {
            if(key.equals(m.getKey())) 
                return m.getValue();
        }
        return null;
    }
    
    public void addMetadata(String key, String value) {
        metadata.add(new DocumentMetadata(key,value)); 
    }
}
