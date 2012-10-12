/*
 * $Id: GuidServiceClient.java 59 2004-05-06 18:27:31Z mquigley $
 */

package net.medcommons.central.ws.guid;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class GuidServiceClient {
  public static void main(String [] args) {
    try {
      String endpoint = "http://medcommons.net:8080/jboss-net/services/GuidService";
      if(args.length > 0 && "local".equals(args[0])) {
        endpoint = "http://localhost:8080/jboss-net/services/GuidService";
      }

      Service service = new Service();
      Call call = (Call) service.createCall();

      call.setTargetEndpointAddress(new java.net.URL(endpoint));
      call.setOperationName("allocateGuid");
      call.setReturnType(org.apache.axis.Constants.XSD_STRING);

      String ret = (String) call.invoke(new Object[] { });

      System.out.println("Returned message: " + ret);
      
    } catch (Exception e) {
      System.err.println(e.toString());
    }
  }
}
