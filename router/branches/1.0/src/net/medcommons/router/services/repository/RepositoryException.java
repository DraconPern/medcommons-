/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.repository;

import java.io.IOException;

/**
 * Thrown when a Repository has problems retrieving or storing content
 * 
 * @author ssadedin
 */
public class RepositoryException extends IOException {

  public RepositoryException() {
    super();
  }
  /**
   * @param message
   */
  public RepositoryException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public RepositoryException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public RepositoryException(Throwable cause) {
    super(cause);
  }
}
