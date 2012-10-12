/*
 * $Id: LogServiceException.java 71 2004-05-12 04:02:33Z mquigley $
 */

package net.medcommons.central.log;

public class LogServiceException extends Exception {

  public LogServiceException() {
    super();
  }
  
  public LogServiceException(String msg) {
    super(msg);
  }
  
  public LogServiceException(String msg, Throwable t) {
    super(msg, t);
  }

}
