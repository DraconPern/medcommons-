package net.medcommons.modules.services.interfaces;



import static net.medcommons.modules.utils.FormatUtils.parseIntegerValue;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;

import net.sourceforge.pbeans.annotations.PersistentClass;
import net.sourceforge.pbeans.annotations.PropertyIndex;
import net.sourceforge.pbeans.annotations.TransientProperty;

/**
 * Pojo container for selected DICOM metadata. Used to pass DICOM 
 * metadata between modules.
 *
 * @author mesozoic, ssadedin
 * TODO: Add SopClassUID
 */
// This annotation is used by the DDL.  Would be nice to remove it
// but that requires a little working on the pbeans source because
// it can only recognize classes with pbeans annotations
@PersistentClass(table="dicom_meta_data", idField="id", autoIncrement=true,
        indexes={
	        @PropertyIndex(unique=false,propertyNames={"seriesInstanceUid"}),
	        @PropertyIndex(unique=false,propertyNames={"studyInstanceUid"})
       }
)
public class DicomMetadata { 
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DicomMetadata.class);

    public static final int INT_MISSING_VALUE = Integer.MIN_VALUE;
    private Long id;
    private String patientId = null;
    private String patientName = null;
    private String patientDateOfBirth = null;
    private String patientSex = null;
    private String windowCenter = null;
    private String windowWidth = null;
    private String windowCenterWidthExplanation = null;

    private int instanceNumber = INT_MISSING_VALUE;
    private String studyInstanceUid = null;
    private String studyDescription = null;
    private String seriesInstanceUid = null;
    private String seriesDescription = null;
    private String sopInstanceUid = null;
    private int seriesNumber = INT_MISSING_VALUE;
    private Date seriesDate = null;
    private Date studyDate = null;
    private String modalities = null;
    private String modality = null;
    private int frames = INT_MISSING_VALUE;
    private String sha1 = null;
    private int displayOrder = INT_MISSING_VALUE;
    private String institutionName;
    private String institutionAddress;
    private String referringPhysicianName;
    private String referringPhysicianAddress;
    private String referringPhysicianTelephoneNumber;
    private String referringPhysicianEmail;
    private String performingPhysicianName;
    private String physicianOfRecord;
    private String accessionNumber;
    private String imageType;
    private String sopClassUId;
    private String stationName;
    private String manufacturer;
    private String manufacturerModelName;
    private String patientAge;
    private String patientAddress;
    private String patientTelephoneNumber;
    private String patientEmail;
    private long length;
    private String transactionStatus;
    private String callingAeTitle;
    private String calledAeTitle;
    private String reasonForStudy;
    private String bodyPart;
    private String guid;

    private Long cxpJob;
    public final static Long UNITIALIZED_CXPJOB = new Long(Long.MIN_VALUE);

    public static final String EXACT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String STATUS_COMPLETE = "COMPLETE";
    public static final String STATUS_READY_TO_DELETE = "READY_TO_DELETE";

    private String documentName = null;

    private String fileName = null;

    public void setId(Long id){
        this.id = id;
    }
    public Long getId(){
        return(this.id);
    }
    public void setPatientId(String patientId){
        this.patientId = patientId;
    }
    public String getPatientId(){
        return(this.patientId);
    }
    public void setPatientName(String patientName){
        this.patientName = patientName;
    }
    public String getPatientName(){
        return(this.patientName);
    }
    public void setPatientDateOfBirth(String patientDateOfBirth){
        this.patientDateOfBirth = patientDateOfBirth;
    }
    public String getPatientDateOfBirth(){
        return(this.patientDateOfBirth);
    }
    public void setPatientSex(String patientSex){
        this.patientSex = patientSex;
    }
    public String getPatientSex(){
        return(this.patientSex);
    }
    public void setWindowCenter(String windowCenter){
        this.windowCenter = windowCenter;
    }
    public String getWindowCenter(){
        return(this.windowCenter);
    }
    public void setWindowWidth(String windowWidth){
        this.windowWidth = windowWidth;
    }
    public String getWindowWidth(){
        return(this.windowWidth);
    }
    public void setWindowCenterWidthExplanation(String windowCenterWidthExplanation){
        this.windowCenterWidthExplanation = windowCenterWidthExplanation;
    }
    public String getWindowCenterWidthExplanation(){
        return(windowCenterWidthExplanation);
    }
    public void setInstanceNumber(int instanceNumber){
        this.instanceNumber = instanceNumber;
    }
    public int getInstanceNumber(){
        return(this.instanceNumber);
    }
    public void setStudyInstanceUid(String studyInstanceUid){
        this.studyInstanceUid = studyInstanceUid;
    }
    public String getStudyInstanceUid(){
        return(this.studyInstanceUid);
    }
    public void setStudyDescription(String studyDescription){
        this.studyDescription = studyDescription;
    }
    public String getStudyDescription(){
        return(this.studyDescription);
    }
    public void setStudyDate(Date studyDate){
        this.studyDate = studyDate;
    }
    public Date getStudyDate(){
        return(this.studyDate);
    }
    public void setSeriesInstanceUid(String seriesInstanceUid){
        this.seriesInstanceUid = seriesInstanceUid;
    }
    public String getSeriesInstanceUid(){
        return(this.seriesInstanceUid);
    }
    public void setSopInstanceUid(String sopInstanceUid){
        this.sopInstanceUid = sopInstanceUid;
    }
    public String getSopInstanceUid(){
        return(this.sopInstanceUid);
    }
    public void setSeriesDescription(String seriesDescription){
        this.seriesDescription = seriesDescription;
    }
    public String getSeriesDescription(){
        return(this.seriesDescription);
    }
    public void setSeriesNumber(int seriesNumber){
        this.seriesNumber = seriesNumber;
    }
    public int getSeriesNumber(){
        return(this.seriesNumber);
    }
    public void setSeriesDate(Date seriesDate){
        this.seriesDate = seriesDate;
    }
    public Date getSeriesDate(){
        return(this.seriesDate);
    }
    public void setModalities(String modalities){
        this.modalities = modalities;
    }
    public String getModalities(){
        return(this.modalities);
    }
    public void setModality(String modality){
        this.modality = modality;
    }
    public String getModality(){
        return(this.modality);
    }
    public void setFrames(int frames){
        this.frames = frames;
    }
    public int getFrames(){
        return(this.frames);
    }
    public void setSha1(String sha1){
        this.sha1 = sha1;
    }
    public String getSha1(){
        return(this.sha1);
    }
    public void setDocumentName(String documentName){
        this.documentName = documentName;
    }
    public String getDocumentName(){
        return(this.documentName);
    }
    @TransientProperty
    public void setFile(File file){
        this.fileName = file == null ? null : file.getAbsolutePath();
    }
    @TransientProperty
    public File getFile(){
        return(new File(this.fileName));
    }
    public void setDisplayOrder(int displayOrder){
        this.displayOrder = displayOrder;
    }
    public int getDisplayOrder(){
        return(this.displayOrder);
    }
    public void setInstitutionName(String instititutionName){
        this.institutionName = instititutionName;
    }
    public String getInstitutionName(){
        return(this.institutionName);
    }

    public void setInstitutionAddress(String institutionAddress){
        this.institutionAddress = institutionAddress;
    }
    public String getInstitutionAddress(){
        return(this.institutionAddress);
    }
    public void setReferringPhysicianName(String referringPhysicianName){
        this.referringPhysicianName = referringPhysicianName;
    }
    public String getReferringPhysicianName(){
        return(this.referringPhysicianName);
    }
    public void setReferringPhysicianAddress(String referringPhysicianAddress){
        this.referringPhysicianAddress = referringPhysicianAddress;
    }
    public String getReferringPhysicianAddress(){
        return(this.referringPhysicianAddress);
    }
    public void setReferringPhysicianTelephoneNumber(String referringPhysicianTelephoneNumber){
        this.referringPhysicianTelephoneNumber = referringPhysicianTelephoneNumber;
    }
    public String getReferringPhysicianTelephoneNumber(){
        return(this.referringPhysicianTelephoneNumber);
    }
    
    public void setReferringPhysicianEmail(String referringPhysicianEmail){
        this.referringPhysicianEmail = referringPhysicianEmail;
    }
    public String getReferringPhysicianEmail(){
        return(this.referringPhysicianEmail);
    }
    public void setPerformingPhysicianName(String performingPhysicianName){
        this.performingPhysicianName = performingPhysicianName;
    }
    public String getPerformingPhysicianName(){
        return(this.performingPhysicianName);
    }
    public void setPhysicianOfRecord(String physicianOfRecord){
        this.physicianOfRecord = physicianOfRecord;
    }
    public String getPhysicianOfRecord(){
        return(this.physicianOfRecord);
    }
    public void setAccessionNumber(String accessionNumber){
        this.accessionNumber = accessionNumber;
    }
    public String getAccessionNumber(){
        return(this.accessionNumber);
    }
    public void setImageType(String imageType){
        this.imageType = imageType;
    }
    public String getImageType(){
        return(this.imageType);
    }
    public void setSopClassUid(String sopClassUId){
        this.sopClassUId = sopClassUId;
    }
    public String getSopClassUid(){
        return(this.sopClassUId);
    }
    public void setStationName(String stationName){
        this.stationName = stationName;
    }
    public String getStationName(){
        return(this.stationName);
    }
    public void setManufacturer(String manufacturer){
        this.manufacturer = manufacturer;
    }
    public String getManufacturer(){
        return(this.manufacturer);
    }
    public void setManufacturerModelName(String manufacturerModelName){
        this.manufacturerModelName = manufacturerModelName;
    }
    public String getManufacturerModelName(){
        return(this.manufacturerModelName);
    }
    public void setPatientAge(String patientAge){
        this.patientAge = patientAge;
    }
    public String getPatientAge(){
        return(this.patientAge);
    }
    public void setPatientAddress(String patientAddress){
        this.patientAddress = patientAddress;
    }
    public String getPatientAddress(){
        return(this.patientAddress);
    }
    public void setPatientTelephoneNumber(String patientTelephoneNumber){
        this.patientTelephoneNumber = patientTelephoneNumber;
    }
    public String getPatientTelephoneNumber(){
        return(this.patientTelephoneNumber);
    }
    public void setPatientEmail(String patientEmail){
        this.patientEmail = patientEmail;
    }
    public String getPatientEmail(){
        return(this.patientEmail);
    }
    public void setLength(long length){
        this.length = length;
    }
    public long getLength(){
        return(this.length);
    }
    public void setTransactionStatus(String transactionStatus){
        this.transactionStatus = transactionStatus;
    }

    public String getTransactionStatus(){
        return(this.transactionStatus);
    }
    public void setCallingAeTitle(String callingAeTitle){
        this.callingAeTitle = callingAeTitle;
    }
    public String getCallingAeTitle(){
        return(this.callingAeTitle);
    }

    public void setCalledAeTitle(String calledAeTitle){
        this.calledAeTitle = calledAeTitle;
    }
    public String getCalledAeTitle(){
        return(this.calledAeTitle);
    }
    public void setCxpJob(long cxpJob){
        this.cxpJob = cxpJob;
    }
    public Long getCxpJob(){
        return(this.cxpJob);
    }

    public void setReasonForStudy(String reasonForStudy){
        this.reasonForStudy = reasonForStudy;
    }

    public String getReasonForStudy(){
        return(this.reasonForStudy);
    }
    public void setBodyPart(String bodyPart){
        this.bodyPart = bodyPart;
    }
    public String getBodyPart(){
        return(this.bodyPart);
    }
    
    public String getGuid(){
        return(this.guid);
    }
    public void setGuid(String guid){
        this.guid = guid;
    }
    public String toShortString(){
        StringBuilder buff = new StringBuilder("DicomMetadata[");

        buff.append("Study=");
        buff.append(this.studyInstanceUid);
        buff.append(", Series=");
        buff.append(this.seriesInstanceUid);
        buff.append(", SOP=");
        buff.append(this.sopInstanceUid);
        buff.append("]");
        return(buff.toString());
    }
    public String toString(){
        StringBuilder buff = new StringBuilder("DicomMetadata[");
        buff.append("Sha1=");
        buff.append(sha1);
        buff.append(", StudyInstanceUid=");
        buff.append(this.studyInstanceUid);
        buff.append(", SeriesInstanceUid=");
        buff.append(this.seriesInstanceUid);
        buff.append(", SOPInstanceUid=");
        buff.append(this.sopInstanceUid);
        buff.append(",DocumentName=");
        buff.append(this.documentName);
        buff.append(",PatientName=");
        buff.append(this.patientName);
        buff.append(", StudyDescription=");
        buff.append(this.studyDescription);
        buff.append(", SeriesDescription=");
        buff.append(this.seriesDescription);
        buff.append(", Modality=");
        buff.append(this.modality);
        buff.append(", SeriesDate=");
        if (this.seriesDate ==null) buff.append("null");
        else {
            DateFormat df = new SimpleDateFormat(EXACT_DATE_TIME_FORMAT);
            buff.append(df.format(this.seriesDate));
            }
        buff.append(", WindowCenter=");
        buff.append(this.windowCenter);
        buff.append(", WindowWidth=");
        buff.append(this.windowWidth);
        buff.append(", SopInstanceUid=");
        buff.append(this.sopInstanceUid);

        buff.append(", InstanceNumber=");
        buff.append(this.instanceNumber);
        buff.append(", SeriesNumber=");
        buff.append(this.seriesNumber);
        buff.append(", DisplayOrder=");
        buff.append(this.displayOrder);
        buff.append(", File=");
        if (this.fileName == null)
            buff.append("null");
        else
            buff.append(fileName);
        buff.append("]");
        return(buff.toString());
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /**
     * Return a suitable display name for the given DICOM
     * @return
     */
    public String getDisplayName() {
        String	displayName = getSeriesDescription();
        if (displayName == null){
        	displayName= getModality() + " Series";
        }
        return displayName;
    }
    

    
    /**
     * A utility struct for holding DICOM Preset information
     */
    public static class DicomPreset{
        public int windowWidth;
        public int windowCenter;
        public String windowCenterWidthExplanation;
    }

    /**
     * TODO needs unit tests for parsing values.
     * Generates a list of window/level presets derived from the DICOM metadata.
     * @param dicomMetadata
     * @return
     */
    public List<DicomPreset> getPresetArray() {
        ArrayList<DicomPreset> presets = new ArrayList<DicomPreset>();
        String delimiters = "\\^";
        String modality = this.getModality();

        String windowCenter = this.getWindowCenter();
        String windowWidth = this.getWindowWidth();
        if (log.isDebugEnabled())
            log.debug("windowCenter = " + windowCenter + ", windowWidth=" + windowWidth);
        if ((windowCenter == null) || (windowWidth == null))
            return(null);
        String windowCenterWidthExplanation = this.getWindowCenterWidthExplanation();
        StringTokenizer windowCenterTokens = new StringTokenizer(windowCenter,delimiters);
        StringTokenizer windowWidthTokens = new StringTokenizer(windowWidth,delimiters);
        StringTokenizer explanations = null;
        if (windowCenterWidthExplanation != null)
                explanations = new StringTokenizer(windowCenterWidthExplanation,delimiters);
        DicomPreset preset = null;
        int presetCount = 0;
        if(log.isDebugEnabled())
            log.debug("windowCenterTokens size = " + windowCenterTokens.countTokens());
        
        if (windowCenterTokens.countTokens() > 0){
            while(windowCenterTokens.hasMoreTokens()){
                presetCount++;
                preset = new DicomPreset();
                String wc = windowCenterTokens.nextToken();
                String ww = windowWidthTokens.nextToken();
                String explanation = null;
                if ((explanations != null) && (explanations.hasMoreTokens()))
                    explanation = explanations.nextToken();
                if ((explanation == null) || ("".equals(explanation))){
                    explanation = "Preset " + presetCount + " w=" + ww + ",c=" + wc;
                }
                int wCenter = parseIntegerValue(wc);
                int wWidth = parseIntegerValue(ww);

                preset.windowCenter = wCenter;
                preset.windowWidth = wWidth;
                preset.windowCenterWidthExplanation = explanation;
                presets.add(preset);
                if (log.isDebugEnabled())
                    log.debug("Added preset " + preset.windowCenterWidthExplanation + " width=" + wWidth + " center=" + wCenter  );
            }
        }
        
        //window width; 1500 HU,. window level; -600 HU)
        if (modality.equals("CT")){
            
            preset = new DicomPreset();
            
            preset.windowCenter = -600;
            preset.windowWidth = 1500;
            preset.windowCenterWidthExplanation = "Lung";
            presets.add(preset);
/*
            preset = new DicomPreset();
            preset.windowCenter = 40;
            preset.windowWidth = 350;
            preset.windowCenterWidthExplanation = "Abdomen";
            presets.add(preset);
*/
            preset = new DicomPreset();
            preset.windowCenter = 300;
            preset.windowWidth = 1500;
            preset.windowCenterWidthExplanation = "Bone";
            presets.add(preset);
            
            preset = new DicomPreset();
            preset.windowCenter = 50;
            preset.windowWidth = 150;
            preset.windowCenterWidthExplanation = "Brain";
            presets.add(preset);
            
            preset = new DicomPreset();
            preset.windowCenter = 50;
            preset.windowWidth = 350;
            preset.windowCenterWidthExplanation = "Soft Tissue";
            presets.add(preset);

        }
        return(presets);
    }
}
