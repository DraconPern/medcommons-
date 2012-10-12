package net.medcommons.application.dicomclient.utils;



import java.io.*;
import java.security.NoSuchAlgorithmException;
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
 *
 * @author mesozoic
 *
 */
public class ExtractFileMetadata  {

	 public static final String DICOM_DATE_FORMAT =  "yyyyMMdd";
	 // Note that dates in DICOM can be in (at least) two different formats. Welcome to DICOM  :-)
	 public static final String DICOM_TIME_FORMAT = "kkmmss.SSSSSS";
	 public static final String DICOM_TIME_FORMAT_SIMPLE = "kkmmss";
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(ExtractFileMetadata.class.getName());

    private DicomInputStream input = null;
    private DicomObject dcmobj;
    

    /**
     *
     */
    public ExtractFileMetadata(File dicomFile) throws IOException{
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
    public ExtractFileMetadata(InputStream in) throws IOException{
    	this.input = new DicomInputStream(in);
    }

    /**
     * Initializes from an existing DicomObject that has already
     * been read.
     */
    public ExtractFileMetadata(DicomObject obj) {
        this.dcmobj = obj;
    }
    
    public DicomMetadata parse() throws IOException, NoSuchAlgorithmException {
        
        readDICOMObject();
        
        DicomMetadata metadata = new DicomMetadata();
        metadata.setCxpJob(DicomMetadata.UNITIALIZED_CXPJOB);
        
        metadata.setPatientId(dcmobj.getString(Tag.PatientID));
        String patientName2 = extractPatientName(dcmobj);
        
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
        
        if(studyDateObj != null) {
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
        
        Date seriesDateObj = extractSeriesDate(dcmobj);
        if(seriesDateObj != null) {
            metadata.setSeriesDate(seriesDateObj);
        }
        
        metadata.setModalities(dcmobj.getString(Tag.ModalitiesInStudy));
        metadata.setModality( dcmobj.getString(Tag.Modality));
        
        String scratchSeriesNumber = dcmobj.getString(Tag.SeriesNumber);
        if(scratchSeriesNumber != null) {
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

    /**
     * Pull the whole patient name field out of the dicom object.
     * <p>
     * SS: originally written by Sean.  I'm very dubious about the logic here
     * wrt the encoding.   I don't see how it can possibly make sense.  However
     * Sean generally knows what he is doing with this stuff.
     * 
     * @param dcmobj
     * @return patient name field extracted and converted to Java encoding
     */
    public static String extractPatientName(DicomObject dcmobj) throws UnsupportedEncodingException {
        String patientName = dcmobj.getString(Tag.PatientName);
        // .trim() very important here to remove trailing unicode characters
        String patientName2 = new String(patientName.getBytes("8859_1"), "utf-8").trim();
        return patientName2;
    }

    /**
     * Attempt to extract a series date from the given DICOM object, 
     * first by parsing the SeriesDate tag but if that fails, but 
     * examining the date / time of secondary capture.
     * 
     * @return  Date extracted or null
     */
    public static Date extractSeriesDate(DicomObject dcmobj) {
        String seriesDate = dcmobj.getString(Tag.SeriesDate);
        String seriesTime = dcmobj.getString(Tag.SeriesTime);
        
        Date seriesDateObj = null;
        try{
            seriesDateObj = dicomToJavaDateTime(seriesDate, seriesTime);
        }
        catch(Exception e){
            log.error("Error parsing seriesDate " + seriesDate + "," + seriesTime + 
                    " for study object " + dcmobj.toString());
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
                    " for study " + dcmobj.toString()
            );
        }
        return seriesDateObj;
    }
    
    /**
     * Reads the DICOM object associated with the stream unless it has
     * already been initialized.
     * 
     * @throws IOException
     */
    private void readDICOMObject() throws IOException {
        if(dcmobj != null)
            return;
        
        if (input == null){
    		 throw new IOException("Input Stream is null");
    	 }
    	 
    	 try {
	         input.setHandler(new StopTagInputHandler(Tag.PixelData));
	         dcmobj = input.readDicomObject();
    	 }
    	 finally {
    		 input.close();
    	 }
    }

    private static Date dicomToJavaDateTime(String sDate, String sTime) throws ParseException{
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


