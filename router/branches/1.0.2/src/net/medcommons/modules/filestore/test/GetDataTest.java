package net.medcommons.modules.filestore.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import net.medcommons.modules.crypto.io.FileGuid;
import net.medcommons.modules.crypto.io.SHA1InputStream;
import net.medcommons.modules.filestore.RepositoryFileProperties;
import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.services.interfaces.CompoundDocumentDescriptor;
import net.medcommons.modules.utils.FileUtils;

import org.apache.log4j.Logger;

public class GetDataTest extends RepositoryBase implements RepositoryFileProperties{
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("GetDataTest");
	
	protected final static int buffSize = 1024 * 32;
	
	public GetDataTest(String s){
		super(s);
	}
	
	public void setUp(){
		super.setUp();
		
	}
	/**
	 * Gets data from the respository
	 * Tests retrieved metadata (SHA-1 hash, file length) against known values to ensure that the 
	 * data was correct.
	 * @throws Exception
	 */
	public void testGetJpeg() throws Exception{
		String imageName = properties.getProperty(DocumentJpeg);
		String storageId = properties.getProperty(AccountID1);
		
		File imageFile = FileUtils.getTestResourceFile(imageName);
		long localFileLength = imageFile.length();
		if(!imageFile.exists())
			throw new FileNotFoundException(imageFile.getAbsolutePath());
		
		
		
		String localFileGuid = FileGuid.calculateFileGuid(imageFile);
		
		SimpleDocumentDescriptor docDescriptor = new SimpleDocumentDescriptor();
		docDescriptor.setStorageId(storageId);
		docDescriptor.setGuid(localFileGuid);
		
		InputStream is = repository.get(docDescriptor);
		SHA1InputStream sIs = new SHA1InputStream(is);
		
		byte[] buff = new byte[buffSize];
		int n = 0;
		long streamSize = 0;

		while ((n = sIs.read(buff, 0, buff.length)) != -1) {
			// A real program would write() the data somewhere
			// Here we're just throwing it away - we only care
			// about the SHA-1 hash and the length for this test.
			// out.write(buff, 0, n);
			streamSize += n;
		}
		is.close();
		sIs.close();
		String streamHash = sIs.getHash();
		
		assertEquals("Length of file", localFileLength, streamSize);
		assertEquals("SHA1 hash mismatch", streamHash,localFileGuid );
		
		
		
	}
	
	
	public void testCompoundDocument() throws Exception{
		String guid = properties.getProperty(DICOMSeries1 + suffixGUID);
		String storageId = properties.getProperty(AccountID1);
		
		CompoundDocumentDescriptor docDescriptor = new CompoundDocumentDescriptor();
	
		docDescriptor.setStorageId(storageId);
		docDescriptor.setGuid(guid);
		
		
		CompoundDocumentDescriptor docs[] = repository.getCompoundDocumentDescriptors(storageId, guid);
		//ArrayList<DocumentInfo> retrievedDocuments = new ArrayList<DocumentInfo>();
		
		log.info("Initial docDescriptor:" + docDescriptor);
		for (int i=0;i<docs.length;i++){
			docs[i].setStorageId(storageId);
			log.info("About to get:" + docs[i]);
			InputStream is = repository.get(docs[i]);
			readInfo r = readInputstream(is);
			assertEquals("SHA1 mismatch", r.sha1, docs[i].getSha1());
			assertEquals("Length mismatch", r.length, docs[i].getLength());
		}
		

		
	}
	private readInfo readInputstream(InputStream is) throws IOException, NoSuchAlgorithmException{
		readInfo r = new readInfo();
		
		SHA1InputStream sIs = new SHA1InputStream(is);
		
		byte[] buff = new byte[buffSize];
		int n = 0;
		long streamSize = 0;

		while ((n = sIs.read(buff, 0, buff.length)) != -1) {
			// A real program would write() the data somewhere
			// Here we're just throwing it away - we only care
			// about the SHA-1 hash and the length for this test.
			// out.write(buff, 0, n);
			streamSize += n;
			
		}
		log.info("streamsize is " + streamSize);
		is.close();
		sIs.close();
		String streamHash = sIs.getHash();
		r.length= streamSize;
		r.sha1 = streamHash;
		return(r);
	}
	
	private class readInfo{
		String sha1 = null;
		long length = -1;
	}
	/*
	public void testPutCompoundDocument() throws java.lang.Exception {
		
		String seriesName = properties.getProperty(DICOMSeries1);
		String storageId = properties.getProperty(AccountID1);
		String knownGuid = properties.getProperty(DICOMSeries1 + suffixGUID);
		
		 
		File seriesDir = FileUtils.getTestResourceFile(seriesName);
		// Want to filter out files like .DS_Store - any file that starts with '.'.
		SpecialFileFilter filter = new SpecialFileFilter();
		filter.filterType = ".";
		
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
			aDoc.sha1 = FileUtils.calculateFileGuid(aDoc.f);
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
		String name = null;
		String sha1 = null;
		long size = -1;
		String contentType = null;
	}
	private static class SpecialFileFilter implements FilenameFilter{
		String filterType = null;
		public boolean accept(File dir, String name) {
			boolean accept = false;
			if (name!=null){
				if (name.indexOf(filterType) != 0)
					accept = true;
			}
			return accept;
		}
	}

	


}
