/*
 * $Id: DeviceRegistrationServiceException.java 57 2004-05-06 17:38:53Z mquigley $
 */

package net.medcommons.central.ws.device;

public class DeviceRegistrationServiceException extends Exception {

  public DeviceRegistrationServiceException() {
    super();
  }
  
  public DeviceRegistrationServiceException(String msg) {
    super(msg);
  }
  
  public DeviceRegistrationServiceException(String msg, Throwable t) {
    super(msg, t);
  }

}
