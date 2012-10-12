/*
 * $Id: TestServiceClient.java 57 2004-05-06 17:38:53Z mquigley $
 */

package net.medcommons.central.ws.test;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class TestServiceClient {
  public static void main(String [] args) {
    try {
      String endpoint = "http://medcommons.net:8080/jboss-net/services/TestService";
      if(args.length > 0 && "local".equals(args[0])) {
        endpoint = "http://localhost:8080/jboss-net/services/TestService"; 
      }
      
      Service service = new Service();
      Call call = (Call) service.createCall();

      call.setTargetEndpointAddress(new java.net.URL(endpoint));
      call.setOperationName("getMessage");
      call.setReturnType(org.apache.axis.Constants.XSD_STRING);

      String ret = (String) call.invoke(new Object[] { });

      System.out.println("(guid: " + ret + ")");
      
    } catch (Exception e) {
      System.err.println(e.toString());
    }
  }
}