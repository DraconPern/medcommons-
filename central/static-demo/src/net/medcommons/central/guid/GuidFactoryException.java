/*
 * $Id: GuidFactoryException.java 52 2004-04-30 22:04:26Z mquigley $
 */

package net.medcommons.central.guid;

public class GuidFactoryException extends Exception {

  public GuidFactoryException() {
    super();
  }
  
  public GuidFactoryException(String msg) {
    super(msg);
  }
  
  public GuidFactoryException(String msg, Throwable t) {
    super(msg, t);
  }

}
