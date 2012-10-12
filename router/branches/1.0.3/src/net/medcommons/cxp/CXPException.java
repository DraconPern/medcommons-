/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.cxp;

/**
 * Indicates an error in processing a CXP Request
 * 
 * @author ssadedin
 */
public class CXPException extends Exception {

  public CXPException() {
    super();
  }
  /**
   * @param message
   */
  public CXPException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public CXPException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public CXPException(Throwable cause) {
    super(cause);
  }
}
