package net.medcommons.application.dicomclient.utils;


public class DicomOutputTransaction implements ManagedTransaction {
	private Long id;
	private int retryCount;
	private String status;
	private String statusMessage;
	private String directory;
	private long bytesTransferred;
	private long totalBytes;
	private int objectCount;
	private long timeStarted;
	private String patientName;
	private String studyDescription;
	private String seriesDescription;
	private String patientId;
	private String patientIdType;
	private String exportMethod;
	private String exportFolder;
	private String dicomRemoteHost;
	private String dicomRemoteAeTitle;
	private int dicomRemotePort;
	private String dicomLocalAeTitle;
	private String transactionFolder;
	private String dashboardStatusId;
	private Long cxpJob;
	private String transferKey;
	
	public final static Long UNITIALIZED_CXPJOB = new Long(Long.MIN_VALUE);
	public final static int UNITIALIZED_DICOM_PORT = Integer.MIN_VALUE;

	public DicomOutputTransaction(){
		super();
		cxpJob = UNITIALIZED_CXPJOB;
		dicomRemotePort = UNITIALIZED_DICOM_PORT;
	}
	public Long getId(){
		return(this.id);
	}
	public void setId(Long id){
		this.id = id;
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
	public void setStatusMessage(String statusMessage){
		this.statusMessage = statusMessage;
	}
	public String getStatusMessage(){
		return(this.statusMessage);
	}
	public void setDirectory(String directory){
		this.directory = directory;
	}
	public String getDirectory(){
		return(this.directory);
	}
	public void setBytesTransferred(long bytesTransferred){
		this.bytesTransferred = bytesTransferred;
	}
	public long getBytesTransferred(){
		return(this.bytesTransferred);
	}
	public long getTimeStarted(){
		return(this.timeStarted);
	}
	public void setTimeStarted(long timeStarted){
		this.timeStarted = timeStarted;
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
	public void setSeriesDescription(String seriesDescription){
		this.seriesDescription = seriesDescription;
	}
	public String getSeriesDescription(){
		return(this.seriesDescription);
	}
	public void setPatientId(String patientId){
		this.patientId = patientId;
	}
	public String getPatientId(){
		return(this.patientId);
	}
	public void setPatientIdType(String patientIdType){
		this.patientIdType = patientIdType;
	}
	public String getPatientIdType(){
		return(this.patientIdType);
	}
	public void setExportMethod(String exportMethod){
		this.exportMethod = exportMethod;
	}
	public String getExportMethod(){
		return(this.exportMethod);
	}
	public void setDicomRemoteHost(String dicomRemoteHost){
		this.dicomRemoteHost = dicomRemoteHost;
	}
	public String getDicomRemoteHost(){
		return(this.dicomRemoteHost);
	}
	public void setDicomRemoteAeTitle(String dicomRemoteAeTitle){
		this.dicomRemoteAeTitle = dicomRemoteAeTitle;
	}
	public String getDicomRemoteAeTitle(){
		return(this.dicomRemoteAeTitle);
	}
	public void setDicomRemotePort(int dicomRemotePort){
		this.dicomRemotePort = dicomRemotePort;
	}
	public int getDicomRemotePort(){
		return(this.dicomRemotePort);
	}
	public void setDicomLocalAeTitle(String dicomLocalAeTitle){
		this.dicomLocalAeTitle = dicomLocalAeTitle;
	}
	public String getDicomLocalAeTitle(){
		return(this.dicomLocalAeTitle);
	}
	public void setExportFolder(String exportFolder){
		this.exportFolder = exportFolder;
	}
	public String getExportFolder(){
		return(this.exportFolder);
	}
	public void setTransactionFolder(String transactionFolder){
		this.transactionFolder = transactionFolder;
	}
	public String getTransactionFolder(){
		return(this.transactionFolder);
	}
	public void setCxpJob(long cxpJob){
		this.cxpJob = cxpJob;
	}
	public Long getCxpJob(){
		return(this.cxpJob);
	}
	public void setDashboardStatusId(String dashboardStatusId){
		this.dashboardStatusId = dashboardStatusId;
	}
	public String getDashboardStatusId(){
		return(this.dashboardStatusId);
	}

	public String toString(){
		StringBuffer buff = new StringBuffer("DicomOutputTransaction[");
		buff.append("dicomRemoteHost="); buff.append(this.dicomRemoteHost);
		buff.append(",dicomRemotePort="); buff.append(this.dicomRemotePort);
		buff.append(",dicomRemoteAeTitle="); buff.append(this.dicomRemoteAeTitle);
		buff.append(",dicomLocalAeTitle="); buff.append(dicomLocalAeTitle);
		buff.append(",patientName="); buff.append(patientName);
		buff.append(",cxpJob="); buff.append(cxpJob);

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
