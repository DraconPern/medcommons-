package net.medcommons.modules.utils;

import java.util.zip.ZipOutputStream;

/**
 * Defines all supported document types in MedCommons with 
 * some of their simple attributes.
 * 
 * @author mesozoic
 *
 *TODO: Note that compression type might need to move to an enum in the future
 *as we support more compression methods. Some might be appropriate for S3 backup,
 *others for personal backup, others for on the gateway itself.
 */
public enum SupportedDocuments {
	

	
	CCR (DocumentTypes.CCR_MIME_TYPE, "xml", ZipOutputStream.DEFLATED),
	DICOM (DocumentTypes.DICOM_MIME_TYPE, "dcm", ZipOutputStream.STORED),
	PDF (DocumentTypes.PDF_MIME_TYPE, "pdf", ZipOutputStream.DEFLATED),
	JPG (DocumentTypes.JPG_MIME_TYPE, "jpg", ZipOutputStream.DEFLATED),
	PNG (DocumentTypes.PNG_MIME_TYPE, "png", ZipOutputStream.DEFLATED),
	HTML(DocumentTypes.HTML_MIME_TYPE, "html",ZipOutputStream.DEFLATED),
	TEXT(DocumentTypes.TEXT_MIME_TYPE, "text",ZipOutputStream.DEFLATED),
	HISTORY(DocumentTypes.CCR_CHANGE_HISTORY_MIME_TYPE, "xml",ZipOutputStream.DEFLATED);
	
	private final String contentType;
	private final String fileExtension;
	private final int compressionMethod;
	SupportedDocuments(String contentType, String fileExtension, int compressionMethod){
		this.contentType = contentType;
		this.fileExtension = fileExtension;
		this.compressionMethod = compressionMethod;
	}
	
	public String getContentType(){return(this.contentType);}
	public String getFileExtension(){return(this.fileExtension);}
	public int getCompressionMethod(){return(this.compressionMethod);}
	
	public static SupportedDocuments getDocumentType(String contentType) throws UnsupportedDocumentException{
		SupportedDocuments docType = null;
		for (SupportedDocuments candidate: SupportedDocuments.values()){
			if (candidate.getContentType().equals(contentType)){
				docType = candidate;
				break;
			}
		}
		if (docType == null){
			throw new UnsupportedDocumentException("Unsupported document type:" + contentType);
		}
		return(docType);
	}

}
