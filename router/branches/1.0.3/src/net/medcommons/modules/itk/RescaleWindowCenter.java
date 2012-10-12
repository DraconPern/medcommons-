package net.medcommons.modules.itk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;


public class RescaleWindowCenter {
	private static Logger log = Logger.getLogger("RescaleWindowCenter");
	static File nativeExecutable = new File("/opt/gateway/native/CacheResizeWindowLevel");
	private File tempDirectory;
	public RescaleWindowCenter(File tempDirectory){
		this.tempDirectory = tempDirectory;
	}
	public static boolean itkInstalled(){
		boolean installed = false;
		if (!nativeExecutable.exists()){
			log.info("ITK executable not installed:" + nativeExecutable.getAbsolutePath());
		}
		else if (!nativeExecutable.canExecute()){
			log.info("ITK executable is not executable " + nativeExecutable.getAbsolutePath());
		}
		else{
			installed = true;
		}
		return(installed);
	}
	
	/**
	 * Should return a data structure with 
	 * <ol>
	 * <li> cache file name
	 * <li> generated JPEG name </li>
	 * <li> time it took to execute (used for estimating costs in the caching algorithm).
	 * </ol>
	 * so that the cache files can be managed.
	 * @param inputDicom
	 * @param maxHeight
	 * @param maxWidth
	 * @param window
	 * @param level
	 * @return
	 * @throws IOException
	 */
	public File generateJPEG(File inputDicom, ImageTransformDimensions imageTransformDimensions) throws IOException{

		File jpegFile = null;
		long startTime = System.currentTimeMillis();
		String executablePath = nativeExecutable.getAbsolutePath();
		String outputDir = tempDirectory.getAbsolutePath() + "/";
		if (!inputDicom.exists()){
			throw new FileNotFoundException("Missing DICOM file in repository " + inputDicom.getAbsolutePath());
		}
		String inputDir = inputDicom.getParentFile().getAbsolutePath() + "/";
		log.debug("outputdir = '" + outputDir + "'");
		log.debug("inputDir = '" + inputDir + "'");

		ProcessBuilder p = null;
		
		if (!imageTransformDimensions.inputSubregionSpecified){
			
			p = new ProcessBuilder(executablePath,
				outputDir,
				inputDir,
				inputDicom.getName(),
				Integer.toString(imageTransformDimensions.getOutputMaxHeight()),
				Integer.toString(imageTransformDimensions.getOutputMaxWidth()),
				Integer.toString(imageTransformDimensions.getWindow()),
				Integer.toString(imageTransformDimensions.getLevel()));
		}
		else{
			log.info("image subregion specified");
			p = new ProcessBuilder(executablePath,
					outputDir,
					inputDir,
					inputDicom.getName(),
					Integer.toString(imageTransformDimensions.getOutputMaxHeight()),
					Integer.toString(imageTransformDimensions.getOutputMaxWidth()),
					Integer.toString(imageTransformDimensions.getWindow()),
					Integer.toString(imageTransformDimensions.getLevel()),
					Double.toString(imageTransformDimensions.getInputRegionTopLeftX()),
					Double.toString(imageTransformDimensions.getInputRegionTopLeftY()),
					Double.toString(imageTransformDimensions.getInputRegionBottomRightX()),
					Double.toString(imageTransformDimensions.getInputRegionBottomRightY())
					
					);
		}
		p.directory(tempDirectory);
		p.redirectErrorStream(true);
		Process process = p.start();
		
		//log.debug("external process launched");
		String line;
		
		/*
		InputStream perror = process.getErrorStream();
		InputStreamReader errorReader = new InputStreamReader(perror);
		BufferedReader errorOut = new BufferedReader(errorReader);
		
		
		while((line = errorOut.readLine())!=null){
			log.error(line);
		}
		*/
		InputStream pout = process.getInputStream();
		InputStreamReader pReader = new InputStreamReader(pout);
		BufferedReader processOut = new BufferedReader(pReader);
		
		boolean foundFile = false;
		
		while((line = processOut.readLine())!=null){ 
			log.info("'" + line + "'");
			if (line.indexOf("JPEG:")== 0){
				String filename = line.substring(5);
				log.info("Filename is " + filename);
				File f = new File(filename);
				if (f.exists()){
					log.info("File exists:" + f.getAbsolutePath());
					jpegFile = f;
					foundFile = true;
				}
				else{
					log.info("File does not exist:" + f.getAbsolutePath());
				}
			}
			else if (line.indexOf("CACHEDFILE:")== 0){
				String filename = line.substring(11);
				log.info("Cached name is " + filename);
				File f = new File(filename);
				if (f.exists()){
					
					boolean success = f.setLastModified(System.currentTimeMillis());
					if (success){
						log.info("Updated cached file modified time:" + f.getAbsolutePath());
					}
					else{
						log.error("Unable to update cached file modified time " +
								f.getAbsolutePath());
					}
				}
				else{
					log.info("Cached File does not exist:" + f.getAbsolutePath());
				}
			}
			else if (line.indexOf("INFO:") == 0){
				log.info(line);
			}
			
			
		} 
		if (!foundFile){
			throw new RuntimeException("Failed to generate JPEG file");
		}
		long stopTime = System.currentTimeMillis();
		log.info("Process completed: elapsed time: "+ (stopTime - startTime) + "msec");
		
		
	return(jpegFile);	
	}
	
}
