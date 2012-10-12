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
public class OrderNotFoundException extends Exception {

  public OrderNotFoundException() {
    super();
  }
  /**
   * @param message
   */
  public OrderNotFoundException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public OrderNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public OrderNotFoundException(Throwable cause) {
    super(cause);
  }
}
