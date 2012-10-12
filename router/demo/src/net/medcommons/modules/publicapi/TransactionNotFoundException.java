package net.medcommons.modules.publicapi;

/**
 * Thrown if the transaction does not exist.
 * @author sdoyle
 *
 */
public class TransactionNotFoundException extends RuntimeException{
	public TransactionNotFoundException(String token){
		super(token);
	}
}
