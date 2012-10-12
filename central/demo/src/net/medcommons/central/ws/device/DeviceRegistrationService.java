/*
 * $Id: DeviceRegistrationService.java 57 2004-05-06 17:38:53Z mquigley $
 */

package net.medcommons.central.ws.device;

import java.util.Date;

import net.medcommons.device.DeviceRegistration;

public class DeviceRegistrationService {

  private static long guid = new Date().getTime();

  public synchronized String registerDevice(DeviceRegistration reg) throws DeviceRegistrationServiceException {
    return "" + guid++;
  }

}
