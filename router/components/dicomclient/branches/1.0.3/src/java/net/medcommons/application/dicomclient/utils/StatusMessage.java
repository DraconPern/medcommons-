package net.medcommons.application.dicomclient.utils;

/**
 * Class for keeping track of status messages.
 *
 * @author mesozoic
 *
 */

public class StatusMessage  {

	private int retryCount = 0;
	private String status = "";
	private String statusMessage = "";
	private long bytesTransferred = 0;
	private long timeStarted = 0;
	public final static String INFO = "INFO";
	public final static String WARNING = "WARNING";
	public final static String ERROR = "ERROR";

	public StatusMessage(String status, String statusMessage){
		this.status = status;
		this.statusMessage = statusMessage;
		timeStarted = System.currentTimeMillis();
	}
	public void setRetryCount(int retryCount){
		this.retryCount = retryCount;
	}
	public int getRetryCount(){
		return(this.retryCount);
	}

	public void setStatus(String status){
		this.status = status;
	}
	public String getStatus(){
		return(this.status);
	}

	public void setStatusMessage(String statusMessage){
		this.statusMessage = statusMessage;
	}
	public String getStatusMessage(){
		return(this.statusMessage);
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

}
