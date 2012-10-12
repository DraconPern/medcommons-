package net.medcommons.application.dicomclient.http.utils;


import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Simple wrapper of responses from Stripe action beans in DDL.
 * 
 * There are three values: 
 * status
 * message
 * contents
 * 
 * This wrapper permits some additinal error information to be sent back to the 
 * calling routine.
 * 
 * @author mesozoic
 *
 */
public class ResponseWrapper {
	
	public  enum Status  {OK, ERROR, WARNING, UNINITIALIZED};
	private Status status = Status.UNINITIALIZED;
	private String message;
	
	private Object contents;
	
	public void setStatus(Status status){
		this.status = status;
	}
	public Status getStatus(){
		return(this.status);
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	public String getMessage(){
		return(this.message);
	}
	public void setContents(Object contents){
		this.contents = contents;
	}
	public Object getContents(){
		return(this.contents);
	}
	public static String throwableToString(Throwable t){
		StringWriter w = new StringWriter();
		PrintWriter out = new PrintWriter(w);
		t.printStackTrace(out);
		return(w.toString());
		
	}
	
}
