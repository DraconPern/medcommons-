/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.wado;

/**
 * Thrown if a login attempt fails for any reason.
 * 
 * @author ssadedin
 */
public class LoginFailedException extends Exception {

  public LoginFailedException() {
    super();
  }
  /**
   * @param message
   */
  public LoginFailedException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public LoginFailedException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public LoginFailedException(Throwable cause) {
    super(cause);
  }
}
