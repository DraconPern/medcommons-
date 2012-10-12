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
public class DocumentNotFoundException extends Exception {

  public DocumentNotFoundException() {
    super();
  }
  /**
   * @param message
   */
  public DocumentNotFoundException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public DocumentNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public DocumentNotFoundException(Throwable cause) {
    super(cause);
  }
}
