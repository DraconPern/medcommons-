package net.medcommons.application.dicomclient.utils;

public interface ManagedTransaction {
	public final static String TRANSACTION_PUT = "PUT";
	public final static String TRANSACTION_GET = "GET";

	public final static String STATUS_QUEUED = "Queued";
	public final static String STATUS_WAIT_PENDING_MATCH = "WaitPendingMatch";
	public final static String STATUS_QUEUED_CCRONLY = "QueuedCCROnly";
	public final static String STATUS_ADD_DICOM = "AddDICOM";
	public final static String STATUS_ACTIVE = "Active";
	public final static String STATUS_COMPLETE = "Complete";
	public final static String STATUS_CANCELLED = "Cancelled";
	public final static String STATUS_READY_FOR_UPLOAD ="ReadyForUpload";
	public final static String STATUS_WAITING_FOR_DOWNLOAD ="WaitingForDownload";
	public final static String STATUS_PERMANENT_ERROR = "Permanent Error";
	public final static String STATUS_TEMPORARY_ERROR = "Error";

	public final static String UNKNOWN = "UNKNOWN";
	public final static String CXP_ENDPOINT = "/gateway/services/CXP2";


	public void setRetryCount(int retryCount);
	public int getRetryCount();

	public void setStatus(String status);
	public String getStatus();

	public void setStatusMessage(String statusMessage);
	public String getStatusMessage();

	public void setBytesTransferred(long bytesTransferred);
	public long getBytesTransferred();

	public long getTimeStarted();
	public void setTimeStarted(long timeStarted);


}
