/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.wado;

/**
 * Thrown if a login attempt fails for any reason.
 * 
 * @author ssadedin
 */
public class InvalidPINException extends Exception {

  public InvalidPINException() {
    super();
  }
  /**
   * @param message
   */
  public InvalidPINException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public InvalidPINException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public InvalidPINException(Throwable cause) {
    super(cause);
  }
}
