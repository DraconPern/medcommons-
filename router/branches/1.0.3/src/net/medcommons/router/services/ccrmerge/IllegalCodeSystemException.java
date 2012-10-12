package net.medcommons.router.services.ccrmerge;

/**
 * Exception thrown if a coding system is unknown to the system (e.g. - it's 
 * not included in CodingSystem).
 * @author sdoyle
 *
 */
public class IllegalCodeSystemException extends IllegalArgumentException{
	public IllegalCodeSystemException(String x){
		super(x);
	}
}
