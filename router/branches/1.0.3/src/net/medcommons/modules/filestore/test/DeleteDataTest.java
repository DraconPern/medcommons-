package net.medcommons.modules.filestore.test;

import java.io.File;
import java.io.FileNotFoundException;

import net.medcommons.modules.filestore.RepositoryFileProperties;
import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.utils.FileUtils;

public class DeleteDataTest extends RepositoryBase implements RepositoryFileProperties{
	public DeleteDataTest(String s){
		super(s);
	}
	public void setUp(){
		super.setUp();
		
	}
	public void testDeleteJPeg() throws Exception{
		String imageName = properties.getProperty(DocumentJpeg);
		String storageId = properties.getProperty(AccountID1);
		String guid = properties.getProperty(DocumentJpeg + suffixGUID);
		

		
		
		SimpleDocumentDescriptor docDescriptor = new SimpleDocumentDescriptor();
		//docDescriptor.setContentType("image/jpeg");
		docDescriptor.setStorageId(storageId);
		docDescriptor.setGuid(guid);
		repository.delete(docDescriptor);
	}
	
	/**
	 * This tests that a failure message is generated when a deletion is attempted 
	 * on the same file as was deleted above.
	 * @throws Exception
	 */
	public void testDeleteJPeg2() throws Exception{
		boolean throwsFileNotFound = false;
		try{
			testDeleteJPeg();
		}
		catch(FileNotFoundException  e){
			throwsFileNotFound = true;
			System.out.println("Successfully generated exception for deletion of missing file");
		}
		
		assertTrue("Failed to generate FileNotFoundException for deleting non-existant file",
				throwsFileNotFound);
		
	}
	public void testDeleteCompoundDocument() throws Exception{
		String seriesName = properties.getProperty(DICOMSeries1);
		String storageId = properties.getProperty(AccountID1);
		String guid = properties.getProperty(DICOMSeries1 + suffixGUID);
		

		SimpleDocumentDescriptor docDescriptor = new SimpleDocumentDescriptor();
		//docDescriptor.setContentType("image/jpeg");
		docDescriptor.setStorageId(storageId);
		docDescriptor.setGuid(guid);
		repository.delete(docDescriptor);
	}

}
