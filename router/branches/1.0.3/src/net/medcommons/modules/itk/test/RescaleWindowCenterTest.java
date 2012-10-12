package net.medcommons.modules.itk.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import net.medcommons.modules.itk.ImageTransformDimensions;
import net.medcommons.modules.itk.RescaleWindowCenter;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.test.interfaces.ResourceNames;
import junit.framework.TestCase;

public class RescaleWindowCenterTest extends TestCase implements ResourceNames{
	
	protected File resources;
	private static Logger log = Logger.getLogger("RescaleWindowCenterTest");
	protected Properties properties = new Properties();
	protected File scratch;
	public void setUp() {
		try {
			

			resources = FileUtils.resourceDirectory();
			if (!resources.exists())
				throw new FileNotFoundException(resources.getAbsolutePath());
			log.info("Loading resources from " + resources.getAbsolutePath());
			File propFile = new File(resources, "Junit_test.properties");
			if (!propFile.exists())
				throw new FileNotFoundException(propFile.getAbsolutePath());
			FileInputStream in = new FileInputStream(propFile);
			properties.load(in);

			String scratchDir = properties.getProperty(ScratchDirectory);
			scratch = new File(scratchDir);
			
			
			

		} catch (Exception e) {
			throw new RuntimeException("Error in setup", e);
		}

	}
	/**
	 * Test to see if window/levelling a single image returns
	 * a file. In the future - should test images against known output.
	 * 
	 * @throws IOException
	 */
	public void testMRImage_small() throws IOException{
		if (RescaleWindowCenter.itkInstalled()){
			RescaleWindowCenter rescale = new RescaleWindowCenter(scratch);
			String filename = properties.getProperty(DocumentSingleDICOMFile_MR); 
			File inputDicom = initFile(filename);
			int maxHeight = 64;
			int maxWidth = 64;
			int window = 400;
			int level = 200;
			ImageTransformDimensions imageDimensions = new ImageTransformDimensions();
			imageDimensions.setOutputMaxHeight(maxHeight);
			imageDimensions.setOutputMaxWidth(maxWidth);
			imageDimensions.setWindow(window);
			imageDimensions.setLevel(level);
			File f = rescale.generateJPEG(inputDicom, imageDimensions);
			assertTrue(f.exists());
		}
		else{
			log.error("Test can not be run: ITK libaries not installed");
		}
	}
	public void testMRImage_big() throws IOException{
		if (RescaleWindowCenter.itkInstalled()){
			RescaleWindowCenter rescale = new RescaleWindowCenter(scratch);
			String filename = properties.getProperty(DocumentSingleDICOMFile_MR); 
			File inputDicom = initFile(filename);
			int maxHeight = 720;
			int maxWidth = 768;
			int window = 600;
			int level = 300;
			ImageTransformDimensions imageDimensions = new ImageTransformDimensions();
			imageDimensions.setOutputMaxHeight(maxHeight);
			imageDimensions.setOutputMaxWidth(maxWidth);
			imageDimensions.setWindow(window);
			imageDimensions.setLevel(level);
			File f = rescale.generateJPEG(inputDicom, imageDimensions);
			assertTrue(f.exists());
		}
		else{
			log.error("Test can not be run: ITK libaries not installed");
		}
	} 
	public void testRGBImage_big() throws IOException{
		if (RescaleWindowCenter.itkInstalled()){
			RescaleWindowCenter rescale = new RescaleWindowCenter(scratch);
			String filename = properties.getProperty(DocumentSingleDICOMFile_RGB); 
			File inputDicom = initFile(filename);
			int maxHeight = 720;
			int maxWidth = 768;
			int window = 600;
			int level = 300;
			ImageTransformDimensions imageDimensions = new ImageTransformDimensions();
			imageDimensions.setOutputMaxHeight(maxHeight);
			imageDimensions.setOutputMaxWidth(maxWidth);
			imageDimensions.setWindow(window);
			imageDimensions.setLevel(level);
			File f = rescale.generateJPEG(inputDicom, imageDimensions);
			assertTrue(f.exists());
		}
		else{
			log.error("Test can not be run: ITK libaries not installed");
		}
	}
	protected File initFile(String imageName) throws FileNotFoundException {

		if (imageName == null){
			throw new NullPointerException("initFile passed null argument");
		}
		File dir = FileUtils.resourceDirectory();
		if (!dir.exists()) {
			throw new FileNotFoundException("Directory not found:"
					+ dir.getAbsolutePath());
		}
		File imageFile = new File(dir, imageName);
		if (!imageFile.exists()) {
			throw new FileNotFoundException("Image not found:"
					+ imageFile.getAbsolutePath());
		}
		return (imageFile);
	}
}
