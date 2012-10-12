package net.medcommons.application.dicomclient.utils;

public class MessageDisplay {
	public final static String TYPE_ERROR = "ERROR";
	public final static String TYPE_INFO = "INFO";
	private Long id; // Used by hibernate
	private String message;
	private String messageType;
	private long messageTime;
	
	public void setId(Long id){
		this.id = id;
	}
	public Long getId(){
		return(this.id);
	}
	public void setMessage(String message){
		this.message = message;
	}
	public String getMessage(){
		return(this.message);
	}
	public void setMessageType(String messageType){
		this.messageType = messageType;
	}
	public String getMessageType(){
		return(this.messageType);
	}
	public void setMessageTime(long messageTime){
		this.messageTime = messageTime;
	}
	public long getMessageTime(){
		return(this.messageTime);
	}
}
