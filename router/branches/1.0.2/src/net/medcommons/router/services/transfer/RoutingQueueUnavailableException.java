/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.transfer;

/**
 * Thrown if the pink box cannot be contacted
 * 
 * @author ssadedin
 */
public class RoutingQueueUnavailableException extends Exception {

  public RoutingQueueUnavailableException() {
    super();
  }
  /**
   * @param message
   */
  public RoutingQueueUnavailableException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public RoutingQueueUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public RoutingQueueUnavailableException(Throwable cause) {
    super(cause);
  }
}
