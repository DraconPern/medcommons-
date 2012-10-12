package net.medcommons.modules.publicapi;

/**
 * Exception thrown if the reference for a document supplied to the API
 * is bad in some way.
 * 
 * @author sdoyle
 *
 */
public class BadReferenceException extends RuntimeException{
		public BadReferenceException(String token){
			super(token);
			
		}

}
