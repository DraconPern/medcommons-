package net.medcommons.modules.dicom;




import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import net.medcommons.modules.services.interfaces.DicomMetadata;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;


/**
 * @author mesozoic
 *
 */
public class ParseFile  {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(ParseFile.class.getName());

  
    private DicomInputStream input = null;
   
    /**
     * 
     */
    public ParseFile(File dicomFile) throws IOException{
    	this.input = new DicomInputStream(dicomFile);
    	
    }
    
    /**
     * Parses from an InputStream. Useful in the repository
     * case - the file may be encrypted but the stream can be
     * exposed for reading.
     * 
     * @param in
     * @throws IOException
     */
    public ParseFile(InputStream in) throws IOException{
    	this.input = new DicomInputStream(in);
    }
    
    public DicomMetadata extractMetadata() throws IOException, NoSuchAlgorithmException{
    	DicomMetadata metadata = new DicomMetadata();
    	
    	 
    	 if (input == null){
    		 throw new IOException("Input Stream is null");
    	 }
    	 try{
	         input.setHandler(new StopTagInputHandler(Tag.PixelData));
	         DicomObject dcmobj =  input.readDicomObject();
	      
	        
	         metadata.setPatientId(dcmobj.getString(Tag.PatientID));
	         metadata.setPatientName(dcmobj.getString(Tag.PatientName));
	         metadata.setWindowCenter(formatIntegerValue(dcmobj.getString(Tag.WindowCenter)));
	         metadata.setWindowWidth(formatIntegerValue(dcmobj.getString(Tag.WindowWidth)));
	         metadata.setPatientDateOfBirth(dcmobj.getString(Tag.PatientBirthDate));
	         String scratchInstanceNumber = dcmobj.getString(Tag.InstanceNumber);
	         if (scratchInstanceNumber != null){
	        	 try{
	        		 int instanceNumber = Integer.parseInt(scratchInstanceNumber.trim());
	        		 metadata.setInstanceNumber(instanceNumber);
	        	 }
	        	 catch(Exception e){
	        		 log.error("Unable to parse instance number '" + scratchInstanceNumber.trim() + "' into an integer value");
	        	 }
	         }
	         
	         metadata.setStudyInstanceUid(dcmobj.getString(Tag.StudyInstanceUID));
	         String studyDate = dcmobj.getString(Tag.StudyDate);
	         if (studyDate != null){
	        	 String studyTime = dcmobj.getString(Tag.StudyTime);
	        	 // Need to parse values here. 
	        	 metadata.setStudyDate(new Date());
	        	 //log.info("Series date = " + seriesDate + ", time=" + seriesTime);
	         }
	         metadata.setStudyDescription(dcmobj.getString(Tag.StudyDescription));
	         metadata.setSeriesInstanceUid(dcmobj.getString(Tag.SeriesInstanceUID));
	         metadata.setSeriesDescription(dcmobj.getString(Tag.SeriesDescription));
	         metadata.setSopInstanceUid(dcmobj.getString(Tag.SOPInstanceUID));
	        
	         String seriesDate = dcmobj.getString(Tag.SeriesDate);
	         if (seriesDate != null){
	        	 String seriesTime = dcmobj.getString(Tag.SeriesTime);
	        	 // Need to parse values here. 
	        	 metadata.setSeriesDate(new Date());
	        	 //log.info("Series date = " + seriesDate + ", time=" + seriesTime);
	         }
	         metadata.setModalities(dcmobj.getString(Tag.ModalitiesInStudy));
	         metadata.setModality( dcmobj.getString(Tag.Modality));
	        
	         String scratchSeriesNumber = dcmobj.getString(Tag.SeriesNumber);
	         if (scratchSeriesNumber != null){
	        	 try{
	        		 int seriesNumber = Integer.parseInt(scratchSeriesNumber.trim());
	        		 metadata.setSeriesNumber(seriesNumber);
	        	 }
	        	 catch(Exception e){
	        		 log.error("Unable to parse series number '" + scratchSeriesNumber.trim() + "' into an integer value");
	        	 }
	        	 
	         }
	         String scratchFrames = dcmobj.getString(Tag.NumberOfFrames);
	         if (scratchFrames != null){
	        	 try{
	        		 int frames = Integer.parseInt(scratchFrames.trim());
	        		 metadata.setFrames(frames);
	        	 }
	        	 catch(Exception e){
	        		 log.error("Unable to parse frames  '" + scratchFrames.trim() + "' into an integer value");
	        	 }
	        	 
	         }
	         return(metadata);
	         
	        
    	 }
    	 finally{
    		 input.close();
    	 }
    }
   /**
    * Window/level values should be null or int. Sometimes they are floats 
    * in the DICOM header; we convert them here.
    * @param value
    * @return
    */
    public static String formatIntegerValue(String value){
    	String returnedVal = null;
    	int val = Integer.MIN_VALUE;
    	if (value != null){
    		int i = value.indexOf(".");
    		if (i != -1){
    			val = Integer.parseInt(value.substring(0,i));
    		}
    		else{
    			val = Integer.parseInt(value);
    		}
    	}
    	if (val != Integer.MIN_VALUE)
    		returnedVal = Integer.toString(val);
    	
    	return(returnedVal);
    }
}
