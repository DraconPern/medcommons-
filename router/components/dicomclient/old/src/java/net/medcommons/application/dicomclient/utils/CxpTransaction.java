package net.medcommons.application.dicomclient.utils;

import java.util.List;

import net.medcommons.modules.services.interfaces.DicomMetadata;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;



/**
 * Pojo container for selected CXP metadata.
 *
 * @author mesozoic
 */ 
public class CxpTransaction implements ManagedTransaction {

	private Long id;

	private String patientName;

	private String displayName;

	private int nSeries;

	private int totalImages;

	private long totalBytes;

	private String status;

	private long elapsedTime;

	private String transactionType;

	private long bytesTransferred = 0;

	private long timeStarted;

	private String statusMessage; // Used for error messages

	private int retryCount;

	private String ccrFilename;

	private String transactionFolder;

	private String exportFolder;

	private String viewUrl;

	private String studyInstanceUid;
	
	private String kbPerSecond;
	
	private Long contextStateId;
	
	private String dashboardStatusId;
	
	/**
	 * The unique id assigned to this transaction for communication
	 * with the gateway
	 */
	private String transferKey;
	
	public List<DicomMetadata> getMetaData(Session s) {
        List<DicomMetadata> metadata = null;
        Criteria metadataWithStudyInstanceUID = s.createCriteria(DicomMetadata.class);
        metadataWithStudyInstanceUID.add(Expression.eq("studyInstanceUid",this.getStudyInstanceUid()));
        metadataWithStudyInstanceUID.add(Expression.eq("transactionStatus",DicomMetadata.STATUS_READY_TO_DELETE));
        metadata = metadataWithStudyInstanceUID.list();
        return metadata;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return (this.id);
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getPatientName() {
		return (this.patientName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return (this.displayName);
	}

	public void setnSeries(int nSeries) {
		this.nSeries = nSeries;
	}

	public int getnSeries() {
		return (this.nSeries);
	}

	public void setTotalImages(int totalImages) {
		this.totalImages = totalImages;
	}

	public int getTotalImages() {
		return (this.totalImages);
	}

	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}

	public long getTotalBytes() {
		return (totalBytes);
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return (this.status);
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public long getElapsedTime() {
		return (this.elapsedTime);
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getTransactionType() {
		return (this.transactionType);
	}

	public void setBytesTransferred(long bytesTransferred) {
		this.bytesTransferred = bytesTransferred;
	}

	public long getBytesTransferred() {
		return (this.bytesTransferred);
	}

	public void setTimeStarted(long timeStarted) {
		this.timeStarted = timeStarted;
	}

	public long getTimeStarted() {
		return (this.timeStarted);
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public String getStatusMessage() {
		return (this.statusMessage);
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public int getRetryCount() {
		return (this.retryCount);
	}
	
	public void setCcrFilename(String ccrFilename){
		this.ccrFilename = ccrFilename;
	}
	public String getCcrFilename(){
		return(this.ccrFilename);
	}
	public void setTransactionFolder(String transactionFolder){
		this.transactionFolder = transactionFolder;
	}
	public String getTransactionFolder(){
		return(this.transactionFolder);
	}
	public void setExportFolder(String exportFolder){
		this.exportFolder = exportFolder;
	}
	public String getExportFolder(){
		return(this.exportFolder);
	}
	public void setViewUrl(String viewUrl){
		this.viewUrl = viewUrl;
	}
	public String getViewUrl(){
		return(this.viewUrl);
	}
	
	public void setStudyInstanceUid(String studyInstanceUid){
		this.studyInstanceUid = studyInstanceUid;
	}
	public String getStudyInstanceUid(){
		return(this.studyInstanceUid);
	}
	public void setKbPerSecond(String kbPerSecond){
		this.kbPerSecond = kbPerSecond;
	}
	public String getKbPerSecond(){
		return(kbPerSecond);
	}
	
	public void setDashboardStatusId(String dashboardStatusId){
		this.dashboardStatusId = dashboardStatusId;
	}
	public String getDashboardStatusId(){
		return(this.dashboardStatusId);
	}
	
	public void setContextStateId(Long contextStateId){
	    this.contextStateId = contextStateId;
	}
	public Long getContextStateId(){
	    return(this.contextStateId);
	}
	 /**
     * Creates a URL for viewing the newly uploaded content.
    */
    public String makeViewUrl(String gatewayRoot, String storageId, String senderId, String guid, String auth){
    	String newURL = gatewayRoot + "/router/currentccr?a=";
    	newURL+= storageId;
    	newURL+="&aa=";
    	newURL+=senderId;
    	newURL+="&g=";
    	newURL+=guid;
    	newURL+="&auth=";
    	newURL+=auth;
    	newURL+="&at=";
    	newURL+=auth;
    	
    	
    	return(newURL);
    }
	public String toString(){
		StringBuffer buff = new StringBuffer("CxpTransaction[");

		buff.append("patientName:");
		buff.append(patientName);
		buff.append(",status:");
		buff.append(status);
		buff.append(", bytesTransferred:");
		buff.append(bytesTransferred);
		buff.append(", contextStateId:");
		buff.append(contextStateId);
	
		buff.append("]");
		return(buff.toString());
	}

    public String getTransferKey() {
        return transferKey;
    }

    public void setTransferKey(String transferKey) {
        this.transferKey = transferKey;
    }

}
