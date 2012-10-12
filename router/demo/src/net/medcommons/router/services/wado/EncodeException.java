/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.wado;

/**
 * Thrown if a non-existant Order is requested for viewing
 * 
 * @author ssadedin
 */
public class EncodeException extends Exception {

  public EncodeException() {
    super();
  }
  /**
   * @param message
   */
  public EncodeException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public EncodeException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public EncodeException(Throwable cause) {
    super(cause);
  }
}
