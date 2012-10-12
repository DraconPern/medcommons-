/*
 * $Id: GuidServiceException.java 57 2004-05-06 17:38:53Z mquigley $
 */

package net.medcommons.central.ws.guid;

public class GuidServiceException extends Exception {
  
  public GuidServiceException() {
    super();
  }
  
  public GuidServiceException(String msg) {
    super(msg);
  }
  
  public GuidServiceException(String msg, Throwable t) {
    super(msg, t);
  }

}
