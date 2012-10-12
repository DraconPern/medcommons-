package net.medcommons.client.utils;

public class CCRParseException extends RuntimeException {
	public CCRParseException(){
		super();
	}
	public CCRParseException(String message){
		super(message);
	}
	public CCRParseException(String message, Throwable t){
		super(message, t);
	}

}
