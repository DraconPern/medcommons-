/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.wado;

/**
 * Thrown if an operation in copying or transforming a CCR fails
 * 
 * @author ssadedin
 */
public class CCROperationException extends Exception {

  public CCROperationException() {
    super();
  }
  /**
   * @param message
   */
  public CCROperationException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public CCROperationException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public CCROperationException(Throwable cause) {
    super(cause);
  }
}
