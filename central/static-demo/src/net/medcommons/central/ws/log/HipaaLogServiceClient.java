/*
 * $Id$
 */

package net.medcommons.central.ws.log;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class HipaaLogServiceClient {
  public static void main(String [] args) {
    try {
      String endpoint = "http://medcommons.net:8080/jboss-net/services/HipaaLogService";
      if(args.length > 0 && "local".equals(args[0])) {
        endpoint = "http://localhost:8080/jboss-net/services/HipaaLogService"; 
      }
      
      writeLog(endpoint);
      readLog(endpoint);
      
    } catch (Exception e) {
      System.err.println(e.toString());
    }
  }
  
  private static void writeLog(String endpoint) throws Exception {
    Service service = new Service();
    Call call = (Call) service.createCall();
    
    call.setTargetEndpointAddress(new java.net.URL(endpoint));
    call.setOperationName("write");
    
    call.invoke(new Object[] { "This is some log text." });
    
    System.out.println("Done.");
  }
  
  private static void readLog(String endpoint) throws Exception {
    Service service = new Service();
    Call call = (Call) service.createCall();
    
    call.setTargetEndpointAddress(new java.net.URL(endpoint));
    call.setOperationName("read");
    
    String[] results = (String[]) call.invoke(new Object[] { "text" } );
    
    if(results != null) {
      System.out.println("Returned " + results.length + " results");
    } else {
      System.out.println("Returned no results.");
    }
    
    for(int i = 0; i < results.length; i++) {
      System.out.println("Result " + i + ": " + results[i]); 
    }
  }
  
}
