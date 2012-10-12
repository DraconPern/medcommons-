package net.medcommons.application.dicomclient.utils;

import java.util.List;

import org.apache.log4j.Logger;

import net.medcommons.application.dicomclient.UploadHandler;
import net.medcommons.application.dicomclient.transactions.CCRReference;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.PatientMatch;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.utils.Str;


/**
 * This holds state for the basic DICOM unit of transaction in this application - the series.
 *
 * @author mesozoic
 *
 */

public class DicomTransaction implements ManagedTransaction{
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DicomTransaction.class);
	
    public final static Long UNITIALIZED_CXPJOB = new Long(Long.MIN_VALUE);

	private Long id;
	private String storageId;
	private String seriesInstanceUid;
	private String seriesDescription;
	private String studyInstanceUid;
	private String seriesSha1;
	private long lastModifiedTime;
	private long timeStarted;
	private String status;
	private String statusMessage;
	private long timeCompleted;
	private long totalBytes;
	private int objectCount;
	private String patientName;
	private String studyDescription;
	private int retryCount;
	private long bytesTransferred;
	private Long cxpJob;
	
	/**
	 * A context state that may be associated to this DICOM transaction before
	 * it has been queued for upload
	 */
	private ContextState contextState;
	
    /**
     * This method returns a context state if the patient is known the current system.
     *
     * The logic of this method shouldn't be changed lightly; patient matching is a big
     * risk.
     *
     * The current logic is - if the {affinity domain, affinity id} match the DICOM
     * institution and patient id AND if the patient's last name is the same - then
     * it's a match. 
     * 
     * If no match - test against the pending queue to see if there is anything that matches there.
     * The criteria here is weaker - it's testing against the name only.
     * 
     * If the object in the pending queue has no first or last name - merge automatically
     * (this is sort of a wildcard match)l
     * 
     * If still no match - the constant UNKNOWN_MEDCOMMONS_ACCOUNT is returned.
     *
     * @param transaction
     * @return
     */
    public ContextState resolveContextState() {
        
        ContextState contextState = null;
        
        String storageId = UploadHandler.UNKNOWN_MEDCOMMONS_ACCOUNT;
        
        DicomNameParser dicomNameParser = new DicomNameParser();
        String familyName = dicomNameParser.familyName(patientName);
        log.info("dicom name = '" + patientName + "', family name is '" + familyName + "'");
        
        PixIdentifierData pixId = PatientMatch.getIdentifier(this);
        if(pixId != null) {
            // Now look up the patient
            PixDemographicData patient =
                PatientMatch.getPatient(pixId.getAffinityDomain(),pixId.getAffinityIdentifier());
            
            if(patient != null) {
                String pixPatientFamilyName = patient.getFamilyName();
                // test to see that patient names are the same.
                if ((familyName != null) && (pixPatientFamilyName != null) && (familyName.equalsIgnoreCase(pixPatientFamilyName))){
                    PixIdentifierData medCommmonsIdentifer =  PatientMatch.getIdentifier(patient.getId(), UploadHandler.MEDCOMMONS_AFFINITY_DOMAIN);
                    // if so - get the storage id
                    if (medCommmonsIdentifer != null){
                        storageId = medCommmonsIdentifer.getAffinityIdentifier();
                        log.info("Matched to an existing account for storageId " + storageId);
                        Long contextStateId = medCommmonsIdentifer.getContextStateId();
                        contextState = TransactionUtils.getContextState(contextStateId);
                    }
                }
            }
        }
        
        if (storageId.equals(UploadHandler.UNKNOWN_MEDCOMMONS_ACCOUNT)){
            // Try match against pending queue.
            List <CCRReference> pending = TransactionUtils.getCCRReferences();
            if (pending.size() == 1){
                CCRReference potentialMatch = pending.get(0);
                Long pixDemographicId = potentialMatch.getPixDemographicDataId();
                PixDemographicData patient = PatientMatch.getPatient(pixDemographicId);
                String givenName = dicomNameParser.givenName(patientName);
                if (Str.equalNormalized(givenName, patient.getGivenName())  &&
                        Str.equalNormalized(familyName, patient.getFamilyName())){
                    PixIdentifierData medCommmonsIdentifer =  PatientMatch.getIdentifier(patient.getId(), UploadHandler.MEDCOMMONS_AFFINITY_DOMAIN);
                    if (medCommmonsIdentifer != null){
                        storageId = medCommmonsIdentifer.getAffinityIdentifier();
                        Long contextStateId = medCommmonsIdentifer.getContextStateId();
                        contextState = TransactionUtils.getContextState(contextStateId);
                    }
                    log.info("Names match; automatic upload for into account " + medCommmonsIdentifer + ", dicom patient name= " + patientName);
                     StatusDisplayManager.getStatusDisplayManager().setMessage("Automatic match for " + givenName + " "
                             + familyName, "Automatic match to pending upload; upload is now queued");
                }
                else 
                if((patient.getFamilyName() == null) && (patient.getGivenName() == null)) {
                    // Blank name in Add DICOM queue; this is a wildcard. 
                    PixIdentifierData medCommmonsIdentifer =  PatientMatch.getIdentifier(patient.getId(), UploadHandler.MEDCOMMONS_AFFINITY_DOMAIN);
                    if (medCommmonsIdentifer != null){
                        storageId = medCommmonsIdentifer.getAffinityIdentifier();
                        Long contextStateId = medCommmonsIdentifer.getContextStateId();
                        contextState = TransactionUtils.getContextState(contextStateId);
                        patient.setFamilyName(familyName);
                        patient.setGender(givenName);
                        TransactionUtils.saveTransaction(patient);
                    }
                }
                else{
                    log.info("No automatic match for DICOM family name = '" + familyName + "', given name = '" + givenName +
                            "', pix demographic family name = '" + patient.getFamilyName() + "', given name = '" + 
                            patient.getGivenName() + "'");
                }
            }
        }

        return(contextState);
    }
    
	public Long getId(){
		return(this.id);
	}
	public void setId(Long id){
		this.id = id;
	}
	public String getStorageId(){
		return(this.storageId);
	}
	public void setStorageId(String storageId){
		this.storageId = storageId;
	}
	public String getSeriesInstanceUid(){
		return(this.seriesInstanceUid);
	}
	public void setSeriesInstanceUid(String seriesInstanceUid){
		this.seriesInstanceUid = seriesInstanceUid;
	}
	public void setStatus(String status){
		this.status = status;
	}
	public String getStatus(){
		return(this.status);
	}

	public void setRetryCount(int retryCount){
		this.retryCount = retryCount;
	}
	public int getRetryCount(){
		return(this.retryCount);
	}
	public void setBytesTransferred(long bytesTransferred){
		this.bytesTransferred = bytesTransferred;
	}
	public long getBytesTransferred(){
		return(this.bytesTransferred);
	}

	public void setStatusMessage(String statusMessage){
		this.statusMessage = statusMessage;
	}
	public String getStatusMessage(){
		return(this.statusMessage);
	}
	public String getSeriesDescription(){
		return(this.seriesDescription);
	}
	public void setSeriesDescription(String seriesDescription){
		this.seriesDescription = seriesDescription;
	}
	public String getStudyInstanceUid(){
		return(this.studyInstanceUid);
	}
	public void setStudyInstanceUid(String studyInstanceUid){
		this.studyInstanceUid = studyInstanceUid;
	}
	public String getSeriesSha1(){
		return(this.seriesSha1);
	}
	
	public void setSeriesSha1(String seriesSha1){
	    // if(this.seriesSha1 != null && !Str.eq(seriesSha1, this.seriesSha1)) {
	        //log.info("seriesSha1: " +this.id + ": " + this.seriesSha1 + " => " + seriesSha1);
	    //}
		this.seriesSha1 = seriesSha1;
	}
	public long getLastModifiedTime(){
		return(this.lastModifiedTime);
	}
	public void setLastModifiedTime(long lastModifiedTime){
		this.lastModifiedTime = lastModifiedTime;
	}
	public long getTimeStarted(){
		return(this.timeStarted);
	}
	public void setTimeStarted(long timeStarted){
		this.timeStarted = timeStarted;
	}



	public long getTimeCompleted(){
		return(this.timeCompleted);
	}

	public void setTimeCompleted(long timeCompleted){
		this.timeCompleted = timeCompleted;
	}
	public long getTotalBytes(){
		return(this.totalBytes);
	}
	public void setTotalBytes(long totalBytes){
		this.totalBytes = totalBytes;
	}
	public void incrementTotalBytes(long bytes){
		this.totalBytes+=bytes;
	}
	public int getObjectCount(){
		return(this.objectCount);
	}
	public void setObjectCount(int objectCount){
		this.objectCount = objectCount;
	}
	public void incrementObjectCount(){
		this.objectCount++;
	}
	public void setPatientName(String patientName){
		this.patientName = patientName;
	}
	public String getPatientName(){
		return(this.patientName);
	}
	public void setStudyDescription(String studyDescription){
		this.studyDescription = studyDescription;
	}
	public String getStudyDescription(){
		return(this.studyDescription);
	}
	public void setCxpJob(long cxpJob){
		this.cxpJob = cxpJob;
	}
	public Long getCxpJob(){
		return(this.cxpJob);
	}
	public String toString(){
		StringBuffer buff = new StringBuffer("DicomTransaction[id="+id +", ");
		buff.append("status="); buff.append(status);
		buff.append(", StudyInstanceUid=");buff.append(studyInstanceUid);
		buff.append(", SeriesInstanceUid=");buff.append(seriesInstanceUid);
		buff.append(", SeriesSha1=");buff.append(seriesSha1);
		buff.append(", ObjectCount="); buff.append(objectCount);
		buff.append(", TotalBytes="); buff.append(totalBytes);
		buff.append(", TimeLastModified="); buff.append(System.currentTimeMillis()- lastModifiedTime); buff.append("msec ago");
		buff.append("]");
		return(buff.toString());
	}
	public ContextState getContextState() {
        return contextState;
    }
    public void setContextState(ContextState contextState) {
        this.contextState = contextState;
    }
}
