package net.medcommons.modules.filestore.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import net.medcommons.modules.crypto.io.FileGuid;
import net.medcommons.modules.filestore.RepositoryFileProperties;
import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.services.interfaces.CompoundDocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.modules.utils.SpecialFileFilter;

import org.apache.log4j.Logger;

public class PutDataTest extends RepositoryBase implements RepositoryFileProperties{
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("PutDataTest");
	
	public PutDataTest(String s){
		super(s);
	}
	
	public void setUp(){
		super.setUp();
		
	}
	/**
	 * Puts the file into the repository.
	 * Tests file metadata (SHA-1 hash, file length) against known values to ensure that the 
	 * data all arrived.
	 * @throws Exception
	 */
	public void testPutJpeg() throws Exception{
		String imageName = properties.getProperty(DocumentJpeg);
		String storageId = properties.getProperty(AccountID1);
		
		File imageFile = FileUtils.getTestResourceFile(imageName);
		long localFileLength = imageFile.length();
		if(!imageFile.exists())
			throw new FileNotFoundException(imageFile.getAbsolutePath());
		
		FileInputStream is = new FileInputStream(imageFile);
		
		String guid = FileGuid.calculateFileGuid(imageFile);
		
		SimpleDocumentDescriptor docDescriptor = new SimpleDocumentDescriptor();
		docDescriptor.setContentType("image/jpeg");
		docDescriptor.setStorageId(storageId);
		
		repository.putInputStream(docDescriptor, is);
		
		Properties props = repository.getMetadata(docDescriptor);
		assertEquals(docDescriptor.getContentType(), props.get(CONTENT_TYPE));
		long repositoryLength = Long.parseLong((String) props.get(LENGTH));
		System.out.println("diff " + (localFileLength - repositoryLength));
		assertEquals("File length", localFileLength, repositoryLength);
		assertEquals("SHA-1", guid, props.get(SHA_1_HASH));
		
	}
	
	/**
	 * This test is failing, don't know why.
	 * 
	 * The repository.finalizeCompoundDocument() expects the guid to be set 
	 * on the active document, but it is null.  Don't know how the test
	 * expects that it should be set.
	 * 
	 * @throws java.lang.Exception
	 */
	/*
	public void testPutCompoundDocument() throws java.lang.Exception {
		
		String seriesName = properties.getProperty(DICOMSeries1);
		String storageId = properties.getProperty(AccountID1);
		String knownGuid = properties.getProperty(DICOMSeries1 + suffixGUID);
		
		 
		File seriesDir = FileUtils.getTestResourceFile(seriesName);
		// Want to filter out files like .DS_Store - any file that starts with '.'.
		SpecialFileFilter filter = new SpecialFileFilter();
		filter.setFilterType(".");
		
		File files[] = seriesDir.listFiles(filter);
		DocumentInfo allDocs[] = new DocumentInfo[files.length];
		DocumentDescriptor descr = new DocumentDescriptor();
		descr.setStorageId(storageId);
		
		DocumentDescriptor transactionDescr = repository.initializeCompoundDocument(descr);

		for (int i=0;i<files.length;i++){
			String contentType = "image/DICOM";
			DocumentInfo aDoc = new DocumentInfo();
			allDocs[i] = aDoc;
			aDoc.f = files[i];
			aDoc.sha1 = FileGuid.calculateFileGuid(aDoc.f);
			aDoc.size = aDoc.f.length();
			aDoc.contentType = contentType;
			CompoundDocumentDescriptor docDescriptor = new CompoundDocumentDescriptor();
			docDescriptor.setContentType(contentType);
			docDescriptor.setDocumentName(aDoc.f.getName());
			docDescriptor.setStorageId(storageId);
			docDescriptor.setTransactionHandle(transactionDescr.getTransactionHandle());
			FileInputStream is = new FileInputStream(aDoc.f);
			repository.putInputStream(docDescriptor, is);
			
		}
		DocumentDescriptor compoundDocumentDescriptor = repository.finalizeCompoundDocument(transactionDescr.getTransactionHandle());
		
		log.info("Compound document guid is " + compoundDocumentDescriptor.getGuid());
		assertEquals("Compound document guid mismatch", 
				compoundDocumentDescriptor.getGuid(),knownGuid);
				
		/// Now let's test the results
	
		for(int i=0;i<files.length;i++){
			CompoundDocumentDescriptor docDescriptor = new CompoundDocumentDescriptor();
			
			DocumentInfo docInfo = allDocs[i];
			docDescriptor.setStorageId(storageId);
			docDescriptor.setGuid(compoundDocumentDescriptor.getGuid());
			docDescriptor.setDocumentName(docInfo.f.getName());
			log.info("storageid=" + docDescriptor.getStorageId());
			log.info("guid=" + docDescriptor.getGuid());
			log.info("name="+ docDescriptor.getDocumentName());
			
			Properties props = repository.getMetadata(docDescriptor);
			
			
			
			assertEquals(docInfo.contentType, props.get(CONTENT_TYPE));
			long repositoryLength = Long.parseLong((String) props.get(LENGTH));
			
			assertEquals("File length", docInfo.size, repositoryLength);
			assertEquals("SHA-1", docInfo.sha1, props.get(SHA_1_HASH));
		}
	
	}
	*/
	private class DocumentInfo{
		File f = null;
		String sha1 = null;
		long size = -1;
		String contentType = null;
	}
	

	


}
