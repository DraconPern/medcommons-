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
public class SessionExpiredException extends Exception {

  public SessionExpiredException() {
    super();
  }
  /**
   * @param message
   */
  public SessionExpiredException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public SessionExpiredException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public SessionExpiredException(Throwable cause) {
    super(cause);
  }
}
