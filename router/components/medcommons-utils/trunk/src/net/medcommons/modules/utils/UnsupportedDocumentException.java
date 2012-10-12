package net.medcommons.modules.utils;

/**
 * Exception for an unsupported document type. 
 * @author mesozoic
 *
 */
public class UnsupportedDocumentException extends Exception {

	private String docType;
	 public UnsupportedDocumentException() {
		    super();
		  }
		  
		  public UnsupportedDocumentException(String msg) {

		    super(msg);
		  }
		  
		  public UnsupportedDocumentException(String msg, Throwable t) {
		    super(msg, t); 
		  }
}
