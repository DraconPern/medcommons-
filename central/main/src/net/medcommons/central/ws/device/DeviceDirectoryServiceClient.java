/*
 * $Id: DeviceDirectoryServiceClient.java 85 2004-07-12 22:19:00Z mquigley $
 */

package net.medcommons.central.ws.device;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class DeviceDirectoryServiceClient {
  public static void main(String [] args) {
    try {
      String endpoint = "http://medcommons.net:8080/jboss-net/services/DeviceDirectoryService";
      String deviceGuid = "QLOCAL1";
      if(args.length > 0 && "local".equals(args[0])) {
        endpoint = "http://localhost:8080/jboss-net/services/DeviceDirectoryService";
        if(args.length > 1) {
          deviceGuid = args[1];
        }
      } else {
        if(args.length > 0) {
          deviceGuid = args[0];
        }
      }
      
      Service service = new Service();
      Call call = (Call) service.createCall();

      call.setTargetEndpointAddress(new java.net.URL(endpoint));
      call.setOperationName("getDeviceUrl");

      String ret = (String) call.invoke(new Object[] { deviceGuid });

      System.out.println("(URL: " + ret + ")");
      
    } catch (Exception e) {
      System.err.println(e.toString());
    }
  }
}
