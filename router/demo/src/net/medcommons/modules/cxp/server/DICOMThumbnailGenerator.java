package net.medcommons.modules.cxp.server;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.itk.ITKCacheManager;
import net.medcommons.modules.itk.ImageTransformDimensions;
import net.medcommons.modules.itk.RescaleWindowCenter;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.services.interfaces.ThumbnailGenerator;
import net.medcommons.modules.utils.FileUtils;

/**
 * Generates a thumbnail of a specific DICOM image.
 * TODO:If the images are multiframe - must generate a jpeg for
 * each frame.
 * TODO: Generate a css sprite of the thumbnails per series
 * @author sdoyle
 *
 */
public class DICOMThumbnailGenerator implements ThumbnailGenerator{
	private static boolean useITK;
	private static Logger log = Logger.getLogger("DICOMThumbnailGenerator");
	static{
	 	  try{
	 		  String useITKPipeline = Configuration.getProperty("Image_Pipeline");
	 		  if (useITKPipeline != null){
	 			  if ("ITK".equalsIgnoreCase(useITKPipeline)){
	 				  useITK = true;
	 			  }
	 		  }
	 	  }
	 	  catch(ConfigurationException e){
	 		  log.error("Can't get Use_ITK_Pipeline configuration value", e);
	 	  }
	 	  log.info("Image pipeline using ITK: " + useITK);
	   }
	
	
	public void generateThumbnail(File in, File out, Object metadata) throws IOException{
		if (!useITK){return;}
		log.info("Generate thumbnail source=" + in.getAbsolutePath() + ", dest="+
				out.getAbsolutePath());
		File scratchDir = ITKCacheManager.getCacheDirectory();
		DicomMetadata dicomMetadata = null;
		if (metadata instanceof DicomMetadata){
			dicomMetadata = (DicomMetadata) metadata;
		}
		
		RescaleWindowCenter rescale = new RescaleWindowCenter(scratchDir);
		ImageTransformDimensions imageTransformDimensions = new ImageTransformDimensions();
		imageTransformDimensions.setOutputMaxHeight(140);
		imageTransformDimensions.setOutputMaxWidth(140);
		if (dicomMetadata != null){
			int window = Integer.MIN_VALUE;
			int level= Integer.MIN_VALUE;
			try{
				window = Integer.parseInt(dicomMetadata.getWindowWidth());
				level = Integer.parseInt(dicomMetadata.getWindowCenter());
			}
			catch(Exception e){
				log.error("error extracting window/level for " + in.getAbsolutePath(), e);
			}
			if ((window != Integer.MIN_VALUE) && (level != Integer.MIN_VALUE)){
				imageTransformDimensions.setWindow(window);
				imageTransformDimensions.setLevel(level);
			}
		}
		else{
			log.error("DicomMetadata not defined for " + in.getAbsolutePath());
		}
		
		try{
			File f = rescale.generateJPEG(in, imageTransformDimensions);
			FileUtils.copyFile(f, out);
		}
		catch(IOException e){
			log.error("Error generating thumbnail for " + in.getAbsolutePath(), e);
		}
		
	}
	                                                 
}
