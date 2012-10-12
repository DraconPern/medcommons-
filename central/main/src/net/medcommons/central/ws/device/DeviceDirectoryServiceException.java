/*
 * $Id: DeviceDirectoryServiceException.java 84 2004-07-12 21:48:29Z mquigley $ 
 */

package net.medcommons.central.ws.device;

public class DeviceDirectoryServiceException extends Exception {

  public DeviceDirectoryServiceException() {
    super();
  }
  
  public DeviceDirectoryServiceException(String msg) {
    super(msg);
  }
  
  public DeviceDirectoryServiceException(String msg, Throwable t) {
    super(msg, t);
  }
  
}
