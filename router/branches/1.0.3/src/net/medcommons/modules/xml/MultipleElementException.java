package net.medcommons.modules.xml;

import net.medcommons.phr.PHRException;

/**
 * Thrown by functions that expect a single atomic match in an XML
 * document where there are multiple matches.
 * 
 * @author mesozoic
 *
 */

public class MultipleElementException extends PHRException {
	public MultipleElementException() {
		super();
	}

	/**
	 * @param message
	 */
	public MultipleElementException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MultipleElementException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public MultipleElementException(Throwable cause) {
		super(cause);
	}

}
