/*
 * $Id: StudyServiceClient.java 137 2004-06-17 21:28:08Z mquigley $
 */

package net.medcommons.router.services.study;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class StudyServiceClient {
  public static void main(String [] args) {
    try {
      String endpoint = "http://medcommons.net:8080/jboss-net/services/StudyService";

      Service service = new Service();
      Call call = (Call) service.createCall();
      call.setTargetEndpointAddress(new java.net.URL(endpoint));
      call.setOperationName("selectStudyGuids");

      String[] ret = (String[]) call.invoke(new Object[] { null });

      for(int i = 0; i < ret.length; i++) {
        System.out.println("StudyGUID" + i + ": " + ret[i]); 
        
        Service service2 = new Service();
        Call call2 = (Call) service2.createCall();
        call2.setTargetEndpointAddress(new java.net.URL(endpoint));
        call2.setOperationName("retrieveStudyData");
        
        String studyData = (String) call2.invoke(new Object[] { ret[i] });
        
        System.out.println("Retrieved Length: " + studyData.length());
        System.out.println("Data:\n\n" + studyData.substring(0, 1000) + "\n");
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}