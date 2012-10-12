/*
 * $Id: InsufficientPrivilegeException.java 2195 2007-10-25 08:24:33Z ssadedin $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.wado;

/**
 * Thrown if a login attempt fails for any reason.
 * 
 * @author ssadedin
 */
public class InsufficientPrivilegeException extends Exception {

  public InsufficientPrivilegeException() {
    super();
  }
  /**
   * @param message
   */
  public InsufficientPrivilegeException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public InsufficientPrivilegeException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public InsufficientPrivilegeException(Throwable cause) {
    super(cause);
  }
}
