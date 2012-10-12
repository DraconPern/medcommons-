package net.medcommons.modules.services.interfaces;

import java.io.*;

import org.jdom.JDOMException;

/**
 * Handles metadata generation for compound documents. A handler is kept for each
 * incoming transaction.
 * <P>
 * @author mesozoic
 *
 */
public interface MetadataHandler {
	
	/**
	 * Should test the documentType, then have the handler parse 
	 * the inputStream and generate a generic 'metadata' type.
	 * <p>
	 * In practice, this generates a {@link DicomMetadata} object and sets various
	 * fields from the meta data (such as series id) onto the document descriptor
	 * so that they can be used in later processing.
	 * <p>
	 * Might be more efficient to parse this file on the way into the system - 
	 * perhaps it should be a I/O pipeline stage.
	 * <p>
	 * For the typical file in a repository - it will be encrypted on
	 * disk and will have to be decrypted here. 
	 * @param documentType
	 * @param inputStream
	 */
	public Object addDocument(CompoundDocumentDescriptor document, InputStream inputStream) throws IOException;
	
	/**
	 * Generates a persistent metadata object into the specified file.
	 * @param f
	 * @param add TODO
	 * @throws IOException
	 * @throws ServiceException 
	 */
	public void generateMetadataFile(File f, boolean add) throws IOException, JDOMException, ServiceException;
	
	/**
	 * Clears all document references so that instance can be reused.
	 *
	 */
	public void clear();
	
	/**
	 * Sets up a thumbnail generator for this handler - something that (for example) takes
	 * a DICOM file and generates a small JPG.
	 * @param t
	 */
	public void setThumbnailGenerator(ThumbnailGenerator t);
	
	/**
	 * Returns a thumbnail generator or null if there is no matching thumbnail generator.
	 * @return
	 */
	public ThumbnailGenerator getThumbnailGenerator();

}
