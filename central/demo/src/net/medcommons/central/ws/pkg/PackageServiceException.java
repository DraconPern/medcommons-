/*
 * $Id: PackageServiceException.java 63 2004-05-10 19:18:50Z mquigley $
 */

package net.medcommons.central.ws.pkg;

public class PackageServiceException extends Exception {
  
  public PackageServiceException() {
    super();
  }
  
  public PackageServiceException(String msg) {
    super(msg);
  }
  
  public PackageServiceException(String msg, Throwable t) {
    super(msg, t);
  }

}
