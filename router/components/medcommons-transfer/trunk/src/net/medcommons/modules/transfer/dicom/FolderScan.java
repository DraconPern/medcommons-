package net.medcommons.modules.transfer.dicom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.crypto.io.SHA1InputStream;
import net.medcommons.modules.dicom.ParseFile;
import net.medcommons.modules.services.interfaces.DicomMetadata;

import org.apache.log4j.Logger;

/**
 * Scans over the DICOM files in a folder and imports the files.
 * This means:
 * a) Copying the files to a new directory. No matter
 *    what the structure of the supplied input directory
 *    the output of this routine is
 *    <medcommons-id>/<series guid>/<dicom files>
 *    plus
 *    <medcommons-id>/<ccr-guid>
 *    and 
 *    <medcommions-id>/<series guid>/metadata.xml
 *    (for the series metadata)
 * b) Right now we simple iterate over the directory 
 *    and log the filenames. Creating the ccr and the 
 *    metadata is the next step.
 * After this scan is complete - 
 * 
 * File scratchDirectory = patient id.
	Then put things into series folders by guid. [done]
	then rename folder as guid folder. [done]
	then generate metadata.
	Then generate ccr
	Then push.
 * 
 * @author mesozoic
 *
 */
public class FolderScan {
	File parentFolder = null;
	String medcommonsId = null;
	int nFiles = 0;
	long nBytes = 0;
	
	ArrayList<SeriesData> series = new ArrayList<SeriesData>();
	
	File parentScratchFolder = new File("ScratchInput");

	private final int BUFFERSIZE = 32*1024;
	
	SHA1 sha1 = null;

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(FolderScan.class);
	public FolderScan(String medcommonsId, File parentFolder){
		this.parentFolder = parentFolder;
		this.medcommonsId = medcommonsId;
		sha1 = new SHA1();
		
	}
	
	public String process() throws Exception{
		long start = System.currentTimeMillis();
		scanFolder(parentFolder);
		String ccrGuid = createCCR();
		long end = System.currentTimeMillis();
		log.info("Total time:" + (end-start) + " msec");
		log.info("Total # of images: " + nFiles);
		log.info("Time per image = " + ((end-start) * 1.0) / (nFiles * 1.0));
		log.info("Total number of MB:" + (nBytes * 1.0)/(1024.0 * 1024.0));
		
		importFiles();
		return(ccrGuid);
	}
	
	/**
	 * Returns the CCR guid reference
	 * @return
	 */
	private String createCCR() throws IOException{
		String ccrGuid = "I AM NOT A GUID...";
		return(ccrGuid);
	}
	
	/**
	 * Recursively walks down the directory structure. 
	 * 
	 * <P>
	 * Any *.dcm files encountered are parsed and 
	 * metadata is inserted into the series array.
	 * @param dir
	 * @throws Exception
	 */
	private void scanFolder(File dir) throws Exception{
		
		
		DcmFileFilter dcmFilter = new DcmFileFilter();
		File dcmFiles[] = dir.listFiles(dcmFilter);
		if (dcmFiles.length != 0){
			nFiles += dcmFiles.length;
			scanDICOMFiles(dcmFiles);
		}
		DirFileFilter dirFileFilter = new DirFileFilter();
		File dirFiles[] = dir.listFiles(dirFileFilter);
		for (int i=0;i<dirFiles.length;i++){
			scanFolder(dirFiles[i]);
		}
		
		
	
	}
	private void scanDICOMFiles(File files[]) throws IOException, NoSuchAlgorithmException{
		
		log.info("About to process " + files.length + " DICOM files");
		for (int i =0;i<files.length; i++){
			//log.info("Reading in DICOM file :" + files[i].getAbsolutePath());
			nBytes += files[i].length();
			ParseFile parseFile = new ParseFile(files[i]);
			DicomMetadata dicomMetadata = parseFile.extractMetadata();
			SeriesData seriesData = getMatchingSeries(dicomMetadata.getSeriesInstanceUid());
			if (seriesData != null){
				seriesData.addInstance(dicomMetadata);
			}
			else{
				seriesData = new SeriesData();
				seriesData.addInstance(dicomMetadata);
				seriesData.setMedcommonsId(medcommonsId);
				series.add(seriesData);
				
			}
		}
	}
	
	/**
	 * File filter that only returns files that end in ".dcm".
	 * @author mesozoic
	 *
	 */
	private static class DcmFileFilter implements FilenameFilter{
		String filenameSuffix = ".dcm";
		
		public boolean accept(File dir, String name) {
			boolean accept = false;
			if (name!=null){
				if (name.indexOf(filenameSuffix) != -1)
					accept = true;
			}
			return accept;
		}
	}
	
	private void importFiles() throws IOException, NoSuchAlgorithmException{
		File medCommonsIdFolder = new File(parentScratchFolder, medcommonsId);
		if (!medCommonsIdFolder.exists()){
			boolean success = medCommonsIdFolder.mkdirs();
			if (!success) throw new RuntimeException("Failed to create medcommons id folder:" + medCommonsIdFolder.getAbsolutePath());
		}
		
		log.info("About to import files to " + medCommonsIdFolder.getAbsolutePath());
		
		for (int i=0;i<series.size();i++){
			SeriesData seriesData = series.get(i);
			importSeries(medCommonsIdFolder, seriesData);
		}
	}
	private void importSeries(File scratchFolder, SeriesData seriesData) throws IOException, NoSuchAlgorithmException{
		File scratchSeriesFolder = new File(scratchFolder, seriesData.getSeriesInstanceUID());
		log.info("About to put series data into scratch file " + scratchSeriesFolder.getAbsolutePath());
		scratchSeriesFolder.mkdir();
		
		// Sort the data
		ArrayList<DicomMetadata> instances = seriesData.getInstances();
		for(int i=0;i<instances.size();i++){
			DicomMetadata instance = instances.get(i);
			FileInputStream in = new FileInputStream(instance.getFile());
			SHA1InputStream sha1Input  = null;
			FileOutputStream out = null;
			try{
				sha1Input = new SHA1InputStream(in);
				
				File outputFile = new File(scratchSeriesFolder, instance.getSopInstanceUid());
				out = new FileOutputStream(outputFile);
				byte buff [] = new byte[BUFFERSIZE];
				int nBytes = 0;
				while ((nBytes = sha1Input.read(buff)) != -1){
						out.write(buff,0,nBytes);
				}
				out.close(); out=null;
				in.close(); in = null;
				
				String guid = sha1Input.getHash();
				sha1Input.close(); 
				sha1Input = null;
				instance.setGuid(guid);
				File guidFile = new File(scratchSeriesFolder, guid);
				boolean success = outputFile.renameTo(guidFile);
				
				if (!success)
					throw new IOException("Failed to rename file \n" + outputFile.getAbsolutePath() + " to \n" +
							guidFile.getAbsolutePath());
				//log.info("Renamed file from  \n" + outputFile.getAbsolutePath() + " to \n" +
				//			guidFile.getAbsolutePath() + ", bytes = " + guidFile.length());
			}
			finally{
				if (in != null){
					in.close();
					in = null;
				}
				if (sha1Input != null){
					sha1Input.close();
					sha1Input = null;
				}
				if (out!=null){
					out.close();
					out = null;
				}
			}
			
			
			
			
		}
		
		File[] files = scratchSeriesFolder.listFiles();
		if ((files == null)|| (files.length==0))
			throw new IOException("No files found in scratch series directory:" + scratchSeriesFolder.getAbsolutePath());
		List<File> lFiles = Arrays.asList(files);
		Collections.sort(lFiles);
		String hashes[] = new String[lFiles.size()];
		for (int i=0;i<lFiles.size();i++){
			hashes[i] = lFiles.get(i).getName();
			
		}
		sha1.initializeHashStreamCalculation();
		String seriesHash = sha1.calculateStringNameHash(hashes);
		seriesData.setGuid(seriesHash);
		File guidSeriesFolder = new File(scratchFolder, seriesHash);
		boolean success = scratchSeriesFolder.renameTo(guidSeriesFolder);
		if (!success)
			throw new IOException("Failed to rename series folder from " + scratchSeriesFolder.getAbsolutePath() + 
					" to " + guidSeriesFolder.getAbsolutePath());
		
		log.info("Renamed " +scratchSeriesFolder.getAbsolutePath() + 
					" to " + guidSeriesFolder.getAbsolutePath());
					
		
	}
	/**
	 * File filter that only returns directories.
	 * @author mesozoic
	 *
	 */
	private static class DirFileFilter implements FilenameFilter{
		
		
		public boolean accept(File dir, String name) {
			boolean accept = false;
			
			if (name!=null){
				File f = new File(dir, name);
				accept = f.isDirectory();
				
			}
			return accept;
		}
	}
	private SeriesData getMatchingSeries(String seriesInstanceUID){
		SeriesData match = null;
		for (int i=0;i<series.size(); i++){
			String candidateUID = series.get(i).getSeriesInstanceUID();
			if (seriesInstanceUID.equals(candidateUID)){
				match= series.get(i);
				break;
			}
				
		}
		return(match);
	}

}

