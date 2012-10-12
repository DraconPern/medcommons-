package net.medcommons.router.services.dicom.util;

/**
 * Error thrown when there is an error with the respository I/O or integrity.
 * 
 * TODO Probably need to have subclasses with validation errors, I/O problems, programming errors, other inconsistencies.
 * @author sean
 *
 */
public class RepositoryError extends Error{
	public RepositoryError() {
	    super();
	  }
	  /**
	   * @param message
	   */
	  public RepositoryError(String message) {
	    super(message);
	  }
	  /**
	   * @param message
	   * @param cause
	   */
	  public RepositoryError(String message, Throwable cause) {
	    super(message, cause);
	  }
	  /**
	   * @param cause
	   */
	  public RepositoryError(Throwable cause) {
	    super(cause);
	  }

}
