package net.medcommons.modules.services.interfaces;


/**
 * Represents a document that belongs to a collection of documents.
 * <p>
 * Inside the repository {@link CompoundDocumentDescriptor}'s represent
 * a single document (a DICOM instance), and the sha1 is the actual sha1 of the document and
 * the 'guid' is the <i>identifying</i> guid for the collection that the
 * document belongs to.
 * <p>
 * When used in indexing however, {@link CompoundDocumentDescriptor} takes
 * a subtley different role:  it sits in the hierarchy of Study / Series / Instance
 * as a parent document.   In this role the sha1 identifies the series and the guid
 * identifies the parent of the series, the Study.  This is totally consistent with
 * the above usage, you just have to think of the Series as being a member of a 
 * collection of series forming a compound document that is associated with a 
 * Study.
 * <p>
 * Due to this, you typically have to 
 * perform translation if you get a {@link CompoundDocumentDescriptor} representing
 * a series from the {@link DocumentIndexService} and then want to reference
 * DICOM images belonging to the series.  Typically you would query for the
 * members of the series using something like LocalFileRepository#loadMetas. 
 * 
 * @author ssadedin
 */
public class CompoundDocumentDescriptor extends DocumentDescriptor {

	private String parentName = null;
	public void setParentName(String parentName){
		this.parentName = parentName;
	}
	public String getParentName(){
		return(this.parentName);
	}
	public String toString(){
		StringBuffer buff = new StringBuffer(this.getClass().getCanonicalName());
		buff.append("[storageid=");
		buff.append(getStorageId());
		buff.append(", guid=");
		buff.append(getGuid());
		buff.append(", sha1=");
		buff.append(getSha1());
		buff.append(", name=");
		buff.append(getDocumentName());
		buff.append(", parentName=");
		buff.append(getParentName());
		buff.append(", transactionHandle=");
		buff.append(getTransactionHandle());
		buff.append(", contentType=");
		buff.append(getContentType());
		buff.append(", length=");
		buff.append(getLength());
		if (this.getKey()!= null){
			buff.append(",key=");
			buff.append(this.getKey());
		}
		if (this.getDecryptionKey() != null){
			buff.append(",decryption key =");
			buff.append(getDecryptionKey());
		}
		buff.append("]");
		return(buff.toString());
		
	}
}
