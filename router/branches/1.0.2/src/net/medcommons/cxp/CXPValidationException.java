/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.cxp;

/**
 * Indicates a schema validation error in processing a CXP Request
 * 
 * @author ssadedin
 */
public class CXPValidationException extends CXPException {

  public CXPValidationException() {
    super();
  }
  /**
   * @param message
   */
  public CXPValidationException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public CXPValidationException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public CXPValidationException(Throwable cause) {
    super(cause);
  }
}
