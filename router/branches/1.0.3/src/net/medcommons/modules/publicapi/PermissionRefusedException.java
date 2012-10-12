package net.medcommons.modules.publicapi;

/**
 * Exception generated when the caller doesn't have the authorization
 * for a transaction.
 * @author sdoyle
 *
 */
public class PermissionRefusedException extends RuntimeException{
	String token;
	public PermissionRefusedException(String token){
		super(token);
		this.token = token;
	}

}
