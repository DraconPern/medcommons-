package net.medcommons.modules.repository.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import junit.framework.TestCase;
import net.medcommons.modules.configuration.ConfigurationUtils;
import net.medcommons.modules.repository.metadata.RepositoryElement;
import net.medcommons.modules.repository.metadata.RepositoryElementConstants;
import net.medcommons.modules.repository.metadata.RepositoryLoader;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.modules.xml.MedCommonsConstants;
import net.medcommons.test.interfaces.ResourceNames;

import org.apache.log4j.Logger;
import org.jdom.Document;

public class MetadataTest extends TestCase implements ResourceNames {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("MetadataTest");
	
	protected Properties properties = new Properties();
	
	protected File resources;
	public void setUp() throws Exception{
		super.setUp();
		try{
    		ConfigurationUtils.initConfiguration();
    	}
    	catch(Exception e){
    		throw new RuntimeException(e);
    	}
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
	 * Tests to see that the MC_REPOSITORY_TEMPLATE_COMPOUNDDOCUMENT can 
	 * be loaded and that all elements defined in RepositoryElementConstants
	 * exist.
	 * 
	 * @throws Exception
	 */
	public void testLoadCompoundDocumentTemplate() throws Exception{
		String templateDirectory = properties.getProperty(TEMPLATE_DIRNAME);
		
		Document doc = RepositoryLoader.loadTemplate(templateDirectory + "/" + MedCommonsConstants.MC_REPOSITORY_TEMPLATE_COMPOUNDDOCUMENT);
		log.info("Loaded " + MedCommonsConstants.MC_REPOSITORY_TEMPLATE_COMPOUNDDOCUMENT + ":"+ doc);
		RepositoryElement ref = (RepositoryElement) doc.getRootElement();
		log.info(ref.toXml());
		for (int i=0;i<RepositoryElementConstants.CompoundDocumentElements.length;i++){
			String elementName = RepositoryElementConstants.CompoundDocumentElements[i];
			RepositoryElement element = ref.getChild(elementName);
			assertNotNull("Missing Element " + elementName + " in " + MedCommonsConstants.MC_REPOSITORY_TEMPLATE_COMPOUNDDOCUMENT, element);
		}
		
	}
	
	public void testLoadDICOMFileReferenceTemplate() throws Exception{
		String templateDirectory = properties.getProperty(TEMPLATE_DIRNAME);
		Document doc = RepositoryLoader.loadTemplate(templateDirectory + "/" + MedCommonsConstants.MC_REPOSITORY_TEMPLATE_DICOM_FILEREFERENCE);
		log.info("Loaded " + MedCommonsConstants.MC_REPOSITORY_TEMPLATE_DICOM_FILEREFERENCE + ":"+ doc);
		RepositoryElement instanceReference = (RepositoryElement) doc.getRootElement();
		log.info(instanceReference.toXml());
		for (int i=0;i<RepositoryElementConstants.DICOMFileElements.length;i++){
			String elementName = RepositoryElementConstants.DICOMFileElements[i];
			RepositoryElement element = instanceReference.getChild(elementName);
			assertNotNull("Missing Element " + elementName + " in " + MedCommonsConstants.MC_REPOSITORY_TEMPLATE_DICOM_FILEREFERENCE, element);
		}
		
		
		
	}
	public void testLoadDICOMMetadataTemplate() throws Exception{
		String templateDirectory = properties.getProperty(TEMPLATE_DIRNAME);
		Document doc = RepositoryLoader.loadTemplate(templateDirectory + "/" + MedCommonsConstants.MC_REPOSITORY_TEMPLATE_DICOM_METADATA);
		log.info("Loaded " + MedCommonsConstants.MC_REPOSITORY_TEMPLATE_DICOM_METADATA + ":"+ doc);
		
		RepositoryElement root = (RepositoryElement) doc.getRootElement();
		log.info(root.toXml());
		for (int i=0;i<RepositoryElementConstants.DICOMMetadataElements.length;i++){
			String elementName = RepositoryElementConstants.DICOMMetadataElements[i];
			RepositoryElement element = root.getChild(elementName);
			assertNotNull("Missing Element " + elementName + " in " + MedCommonsConstants.MC_REPOSITORY_TEMPLATE_DICOM_METADATA, element);
		}
	}

}
