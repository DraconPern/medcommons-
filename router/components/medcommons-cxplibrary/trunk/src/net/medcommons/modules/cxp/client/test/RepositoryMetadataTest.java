package net.medcommons.modules.cxp.client.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.medcommons.modules.utils.FileUtils;

import org.apache.log4j.Logger;

public class RepositoryMetadataTest extends CXPBase{

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("RepositoryMetadataTest");
	
	public void setUp() throws Exception{
		super.setUp();
		resources = FileUtils.resourceDirectory();
		if (!resources.exists())
			throw new FileNotFoundException(resources.getAbsolutePath());
		File propFile = new File(resources, "Junit_test.properties");
		if (!propFile.exists())
			throw new FileNotFoundException(propFile.getAbsolutePath());
		FileInputStream in = new FileInputStream(propFile);
		properties.load(in);
	}
	
	/**
	 * Tests to see that an existing Repository metadata.xml file
	 * is parseable. 
	 * Test should look at the contents - currently it only prints 
	 * them out to the JUnit log.
	 * @throws Exception
	 */
	public void testParse() throws Exception{
		/*
		String filename = properties.getProperty(DicomRepositoryMetadata);
		log.info("Filename is:" + filename);
		File file = initFile(filename);
		if (!file.exists())
			throw new FileNotFoundException(file.getAbsolutePath());
		FileInputStream in = new FileInputStream(file);
		List<DicomMetadata> dicomData = RepositoryMetadataHandler.parseMetadata(in);
		assertNotNull("DicomRepositoryMetadata parsing failed:" + file.getAbsolutePath(),dicomData);
		log.info("There are " + dicomData.size() + " files referenced in this metadata file:");
		for (int i=0;i<dicomData.size();i++){
			log.info(dicomData.get(i).toString());
		}
		*/
	}
}
