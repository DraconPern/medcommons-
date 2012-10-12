package net.medcommons.modules.utils;

import java.io.IOException;

/**
 * Exception thrown when a stream is cancelled by user action. The stream detects some state and throws this exception
 * instead of the next read() or write().
 * @author mesozoic
 *
 */
public class CancelledStreamException extends RuntimeException{
	public CancelledStreamException(){
		super();
	}
	public CancelledStreamException(String reason){
		super(reason);
	}
	public CancelledStreamException(String reason, Throwable t){
		super(reason, t);
	}
}
