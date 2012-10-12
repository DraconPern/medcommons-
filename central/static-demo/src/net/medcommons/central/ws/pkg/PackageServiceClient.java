/*
 * $Id: PackageServiceClient.java 65 2004-05-10 20:13:29Z mquigley $
 */

package net.medcommons.central.ws.pkg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import net.medcommons.conversion.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class PackageServiceClient {
  public static String endpoint = "http://medcommons.net:8080/jboss-net/services/PackageService";
  
  public static void main(String [] args) {
    try {
      String sourceFile = null;
      if(args.length > 0 && "local".equals(args[0])) {
        endpoint = "http://localhost:8080/jboss-net/services/PackageService";
        
        if(args.length > 1) {
          sourceFile = args[1]; 
        }
      } else if(args.length > 0) {
        sourceFile = args[0];
      }

      byte[] data = loadData(sourceFile);
      System.out.println("Loaded " + data.length + " bytes.");
      
      String guid = transmitData(data);
      byte[] fetchedData = retrieveData(guid);
      compareData(data, fetchedData);

    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  private static byte[] loadData(String path) throws Exception {
    File f = new File(path);
    FileInputStream fis = new FileInputStream(f);
    
    int bytesRead = 0;
    byte[] buffer = new byte[10240];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while((bytesRead = fis.read(buffer, 0, buffer.length)) != -1) {
      baos.write(buffer, 0, bytesRead); 
    }
  
    fis.close();
    return baos.toByteArray(); 
  }
  
  private static String transmitData(byte[] data) throws Exception {
    Service service = new Service();
    Call call = (Call) service.createCall();

    call.setTargetEndpointAddress(new java.net.URL(endpoint));
    call.setOperationName("storePackage");
    
    String encodedData = Base64Utility.encode(data);
    
    long startMs = System.currentTimeMillis();
    String guid = (String) call.invoke(new Object[] { encodedData } );
    long endMs = System.currentTimeMillis();
    
    System.out.println("Transmitted " + data.length + " bytes in " + (endMs - startMs) + " ms.");
    System.out.println("The assigned guid is " + guid);
    
    return guid;
  }
  
  private static byte[] retrieveData(String guid) throws Exception {
    Service service = new Service();
    Call call = (Call) service.createCall();

    call.setTargetEndpointAddress(new java.net.URL(endpoint));
    call.setOperationName("fetchPackage"); 
    
    long startMs = System.currentTimeMillis();
    String data = (String) call.invoke(new Object[] { guid } );
    long endMs = System.currentTimeMillis();
    
    byte[] decodedData = Base64Utility.decode(data);
    
    System.out.println("Received " + decodedData.length + " bytes in " + (endMs - startMs) + " ms.");
       
    return decodedData;
  }
  
  private static void compareData(byte[] originalData, byte[] newData) throws Exception {
    for(int i = 0; i < originalData.length; i++) {
      if(originalData[i] != newData[i]) {
        System.out.println("**** COMPARISON FAILED ****");
        return;
      } 
    }
    System.out.println("Data comparison succeeded!");
  }
  
}