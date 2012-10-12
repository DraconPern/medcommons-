/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.modules.services.interfaces;

/**
 * Thrown when a REST call fails
 * 
 * @author ssadedin
 */
public class ServiceException extends Exception {

  public ServiceException() {
    super();
  }
  /**
   * @param message
   */
  public ServiceException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public ServiceException(Throwable cause) {
    super(cause);
  }
}
