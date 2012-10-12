package net.medcommons.document;

import org.jdom.JDOMException;
/**
 * Exception thrown when CCR has failed schema validation
 * @author sean
 *
 */
public class CCRParseException extends JDOMException{
	
	public CCRParseException(String message){
		super(message);
	}
}
