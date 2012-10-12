/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.wado;

/**
 * Thrown if an operation is attempted for which the user has insufficient
 * rights to perform that operation.
 * 
 * @author ssadedin
 */
public class InvalidCredentialsException extends Exception {

  public InvalidCredentialsException() {
    super();
  }
  /*
   * @param message
   */
  public InvalidCredentialsException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public InvalidCredentialsException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public InvalidCredentialsException(Throwable cause) {
    super(cause);
  }
}
