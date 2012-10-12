/*
 * $Id$
 */

package net.medcommons.central.ws.log;

public class HipaaLogServiceException extends Exception {

  public HipaaLogServiceException() {
    super();
  }
  
  public HipaaLogServiceException(String msg) {
    super(msg);
  }
  
  public HipaaLogServiceException(String msg, Throwable t) {
    super(msg, t);
  }

}
