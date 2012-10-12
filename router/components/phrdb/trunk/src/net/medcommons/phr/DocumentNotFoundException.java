/*
 * $Id$
 */
package net.medcommons.phr;

/**
 * Thrown if a non-existant document is requested from the repository
 * 
 * @author ssadedin
 */
public class DocumentNotFoundException extends PHRException {

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
