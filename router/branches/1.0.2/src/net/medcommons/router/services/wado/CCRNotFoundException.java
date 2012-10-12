/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.wado;

/**
 * Thrown if a non-existant CCR is requested for processing
 * 
 * @author ssadedin
 */
public class CCRNotFoundException extends Exception {

  public CCRNotFoundException() {
    super();
  }
  /**
   * @param message
   */
  public CCRNotFoundException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public CCRNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public CCRNotFoundException(Throwable cause) {
    super(cause);
  }
}
