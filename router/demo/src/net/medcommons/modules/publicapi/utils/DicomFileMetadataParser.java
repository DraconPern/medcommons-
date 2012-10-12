package net.medcommons.modules.publicapi.utils;



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.medcommons.modules.services.interfaces.DicomMetadata;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;


/**
 * Parses DICOM metadata. All irregularities of DICOM metadata mapping into Java objects occurs here.
 * [Almost identical to ExtractFileMetadata in DDL project
 *
 * @author mesozoic
 *
 */
public class DicomFileMetadataParser  {

	 public static final String DICOM_DATE_FORMAT =  "yyyyMMdd";
	 // Note that dates in DICOM can be in (at least) two different formats. Welcome to DICOM  :-)
	 public static final String DICOM_TIME_FORMAT = "kkmmss.SSSSSS";
	 public static final String DICOM_TIME_FORMAT_SIMPLE = "kkmmss";
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(DicomFileMetadataParser.class.getName());


    private DicomInputStream input = null;

    /**
     *
     */
    public DicomFileMetadataParser(File dicomFile) throws IOException{
    	this.input = new DicomInputStream(dicomFile);

    }

    /**
     * Parses from an InputStream. 
     *
     * @param in
     * @throws IOException
     */
    public DicomFileMetadataParser(InputStream in) throws IOException{
    	this.input = new DicomInputStream(in);
    }

    public DicomMetadata parse() throws IOException{
    	DicomMetadata metadata = new DicomMetadata();


    	 if (input == null){
    		 throw new IOException("Input Stream is null");
    	 }
    	 try{
    	     // Don't parse the whole dicom file - only up to the Tag.PixelData
	         input.setHandler(new StopTagInputHandler(Tag.PixelData));
	         DicomObject dcmobj =  input.readDicomObject();
	         

	         metadata.setCxpJob(DicomMetadata.UNITIALIZED_CXPJOB);

	         metadata.setPatientId(dcmobj.getString(Tag.PatientID));
	         String patientName = dcmobj.getString(Tag.PatientName);
	         
	         if(patientName == null) 
	             throw new IllegalArgumentException("Invalid DICOM file: could not extract patient name");
	                     
	         // .trim() very important here to remove trailing unicode characters
	         String patientName2 = new String(patientName.getBytes("8859_1"), "utf-8").trim();
	       
	         metadata.setPatientName(patientName2);
	         metadata.setPatientSex(dcmobj.getString(Tag.PatientSex));
	         metadata.setWindowCenter(dcmobj.getString(Tag.WindowCenter));
	         metadata.setWindowWidth(dcmobj.getString(Tag.WindowWidth));
	         metadata.setPatientDateOfBirth(dcmobj.getString(Tag.PatientBirthDate));
	         metadata.setWindowCenterWidthExplanation(dcmobj.getString(Tag.WindowCenterWidthExplanation));
	         String reason = dcmobj.getString(Tag.ReasonForStudyRET);
	         if (reason == null){
	        	 reason = dcmobj.getString(Tag.ReasonForTheRequestedProcedure);
	        	 if (reason == null){
	        		 reason = dcmobj.getString(Tag.ReasonForTheImagingServiceRequestRET);
	        		 if (reason == null){
		        		 reason = dcmobj.getString(Tag.ReasonForRequestedProcedureCodeSequence);
		        	 }
	        	 }
	         }
	         metadata.setReasonForStudy(reason);

	         String bodyPart = dcmobj.getString(Tag.BodyPartExamined);
	         if (bodyPart == null){
	        	 DicomElement anat =  dcmobj.get(Tag.AnatomicRegionSequence);
	        	 if (anat != null){
	        		 DicomObject obj = anat.getDicomObject();
	        		 bodyPart = obj.getString(Tag.CodeMeaning);
	        	 }

	         }
	         if (bodyPart != null)
	        	 metadata.setBodyPart(bodyPart);

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
	         String studyTime = dcmobj.getString(Tag.StudyTime);
	         Date studyDateObj = null;
	         try{
	        	 studyDateObj = dicomToJavaDateTime(studyDate, studyTime);
	         }
	         catch(Exception e){
	        	 log.error("Error processing study date " + studyDate + ", " + studyTime);
	         }
	         if (studyDateObj != null){
	        	 metadata.setStudyDate(studyDateObj);
	         }
	         metadata.setStudyDescription(dcmobj.getString(Tag.StudyDescription));
	         metadata.setSeriesInstanceUid(dcmobj.getString(Tag.SeriesInstanceUID));


	         metadata.setSeriesDescription(dcmobj.getString(Tag.SeriesDescription));
	         metadata.setSopInstanceUid(dcmobj.getString(Tag.SOPInstanceUID));
	         metadata.setInstitutionName(dcmobj.getString(Tag.InstitutionName));
	         metadata.setInstitutionAddress(dcmobj.getString(Tag.InstitutionAddress));
	         metadata.setReferringPhysicianName(dcmobj.getString(Tag.ReferringPhysicianName));
	         metadata.setReferringPhysicianTelephoneNumber(dcmobj.getString(Tag.ReferringPhysicianTelephoneNumbers));
	         metadata.setReferringPhysicianAddress(dcmobj.getString(Tag.ReferringPhysicianAddress));
	         metadata.setPerformingPhysicianName(dcmobj.getString(Tag.PerformingPhysicianName));
	         metadata.setPhysicianOfRecord(dcmobj.getString(Tag.PhysiciansOfRecord));
	         metadata.setAccessionNumber(dcmobj.getString(Tag.AccessionNumber));
	         metadata.setImageType(dcmobj.getString(Tag.ImageType));
	         metadata.setSopClassUid(dcmobj.getString(Tag.SOPClassUID));
	         metadata.setStationName(dcmobj.getString(Tag.StationName));
	         metadata.setManufacturer(dcmobj.getString(Tag.Manufacturer));
	         metadata.setManufacturerModelName(dcmobj.getString(Tag.ManufacturerModelName));
	         metadata.setPatientAddress(dcmobj.getString(Tag.PatientAddress));
	         metadata.setPatientAge(dcmobj.getString(Tag.PatientAge));
	         metadata.setPatientTelephoneNumber(dcmobj.getString(Tag.PatientTelephoneNumbers));

	         String seriesDate = dcmobj.getString(Tag.SeriesDate);
	         String seriesTime = dcmobj.getString(Tag.SeriesTime);

	         Date seriesDateObj = null;
	         try{
	        	 seriesDateObj = dicomToJavaDateTime(seriesDate, seriesTime);
	         }
	         catch(Exception e){
	        	 log.error("Error parsing seriesDate " + seriesDate + "," + seriesTime + 
	        			 " for study object " + metadata.toString());
	         }
	         try{
		         if (seriesDateObj == null){
		        	 seriesDate = dcmobj.getString(Tag.DateOfSecondaryCapture);
		        	 seriesTime = dcmobj.getString(Tag.TimeOfSecondaryCapture);
		        	 seriesDateObj = dicomToJavaDateTime(seriesDate, seriesTime);
		         }
	         }
	         catch(Exception e){
	        	 log.error("Error parsing date of secondary capture " + 
	        			 	dcmobj.getString(Tag.DateOfSecondaryCapture) + 
	        			" ," + dcmobj.getString(Tag.TimeOfSecondaryCapture) + 
	        			" for study " + metadata.toString()
	        			);
	         }
	         if (seriesDateObj != null){
	        	 metadata.setSeriesDate(seriesDateObj);
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
	         
	         /*
	          
	          A bit of experimental code to check if we can find the largest and smallest pixel value whish is 
	          useful when trying ton constrain W/L values.   Unfortunately it turnes out the at these are null quite often and
	          hence not terribly useful (or reliable ).
	          
	         String minPixelValue = dcmobj.getString(Tag.SmallestImagePixelValue);
	         String maxPixelValue = dcmobj.getString(Tag.LargestImagePixelValue);
	         
	         log.info("Smallest pixel value = " + minPixelValue);
	         log.info("Largest pixel value = " + maxPixelValue);
	         */
	          
	         return(metadata);
    	 }
    	 finally{
    		 input.close();
    	 }
    }

    private Date dicomToJavaDateTime(String sDate, String sTime) throws ParseException{
    	if(log.isDebugEnabled()){
    		log.debug("dicom " + sDate + " , time= " + sTime);
    	}
    	Date newDate = null;
    	if(sDate==null)
    		return(null);

       

        	 if (sTime == null){
        		 SimpleDateFormat dicomDate = new SimpleDateFormat(DICOM_DATE_FORMAT);
        		 newDate = dicomDate.parse(sDate);
        	 }
        	 else if (sTime.length() < 7){
        		 String dateTime = sDate + sTime;
        		 SimpleDateFormat dicomDate = new SimpleDateFormat(DICOM_DATE_FORMAT + DICOM_TIME_FORMAT_SIMPLE);
        		 newDate = dicomDate.parse(dateTime);
        	 }
        	 else{
        		 String dateTime = sDate + sTime;
        		 SimpleDateFormat dicomDate = new SimpleDateFormat(DICOM_DATE_FORMAT + DICOM_TIME_FORMAT);
        		 newDate = dicomDate.parse(dateTime);
        	 }
        	 log.debug("Series date = " + sDate +
        			 ", time=" + sTime + ", parsed =" +
        			 newDate.toLocaleString());
       	
       	
       	 return(newDate);
        }
    }


