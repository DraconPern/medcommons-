/*
 * $Id: StudyServiceException.java 137 2004-06-17 21:28:08Z mquigley $
 */

package net.medcommons.router.services.study;

public class StudyServiceException extends Exception {
  
  public StudyServiceException() {
    super();
  }
  
  public StudyServiceException(String msg) {
    super(msg);
  }
  
  public StudyServiceException(String msg, Throwable t) {
    super(msg, t);
  }

}
