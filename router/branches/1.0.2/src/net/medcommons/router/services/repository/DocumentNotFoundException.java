/*
 * $Id: DocumentNotFoundException.java 874 2005-10-11 07:57:04Z ssadedin $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.repository;

/**
 * Thrown if a non-existant document is requested from the repository
 * 
 * @author ssadedin
 */
public class DocumentNotFoundException extends RepositoryException {

  public DocumentNotFoundException() {
    super();
  }
  /**
   * @param message
   */
  public DocumentNotFoundException(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public DocumentNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public DocumentNotFoundException(Throwable cause) {
    super(cause);
  }
}
