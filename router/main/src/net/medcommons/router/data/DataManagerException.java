/*
 * $Id: DataManagerException.java 201 2004-07-14 19:25:02Z mquigley $
 */

package net.medcommons.router.data;

public class DataManagerException extends Exception {

  public DataManagerException() {
    super();
  }
  
  public DataManagerException(String msg) {
    super(msg);
  }
  
  public DataManagerException(String msg, Throwable t) {
    super(msg, t);
  }
  
}
