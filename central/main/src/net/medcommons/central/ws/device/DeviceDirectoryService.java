/*
 * $Id: DeviceDirectoryService.java 84 2004-07-12 21:48:29Z mquigley $
 */

package net.medcommons.central.ws.device;

import java.util.HashMap;

public class DeviceDirectoryService {

  private HashMap directory;
  
  public DeviceDirectoryService() {
    directory = new HashMap();
    directory.put("QLOCAL1", "http://sophie.quigley.com:9080/");
    directory.put("QLOCAL2", "http://cheyenne.quigley.com:9080/");
  }
  
  public String getDeviceUrl(String deviceGuid) throws DeviceDirectoryServiceException {
    if(!directory.containsKey(deviceGuid)) {
      throw new DeviceDirectoryServiceException("No such device GUID: (deviceGuid: " + deviceGuid + ")");
    }
    return (String) directory.get(deviceGuid);
  }
  
}
