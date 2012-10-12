/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.xds.consumer.web;

/**
 * Thrown if an operation is passed CCR xml which turns out not to be valid
 * 
 * @author ssadedin
 */
public class InvalidCCRException extends Exception {

  public InvalidCCRException() {
    super();
  }
  /**
   * @param message
   */
  public InvalidCCRException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public InvalidCCRException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public InvalidCCRException(Throwable cause) {
    super(cause);
  }
}
