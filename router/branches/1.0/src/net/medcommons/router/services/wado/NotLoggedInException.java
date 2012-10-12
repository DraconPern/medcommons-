/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.wado;

/**
 * Thrown by various actions and pages if the user is not logged in.
 * 
 * @author ssadedin
 */
public class NotLoggedInException extends Exception {

  public NotLoggedInException() {
    super("You must be logged in to perform this action.  Your session may have expired.");
  }
  /**
   * @param message
   */
  public NotLoggedInException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public NotLoggedInException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public NotLoggedInException(Throwable cause) {
    super(cause);
  }
}
