/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.router.services.dicom.util;

/**
 * Thrown if a non-existant Order is requested for viewing
 * 
 * @author ssadedin
 */
public class SeriesCreationError extends Exception {

  public SeriesCreationError() {
    super();
  }
  /**
   * @param message
   */
  public SeriesCreationError(String message) {
    super(message);
  }
  /**
   * @param message
   * @param cause
   */
  public SeriesCreationError(String message, Throwable cause) {
    super(message, cause);
  }
  /**
   * @param cause
   */
  public SeriesCreationError(Throwable cause) {
    super(cause);
  }
}
