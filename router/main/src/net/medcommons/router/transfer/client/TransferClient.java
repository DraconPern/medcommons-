/*
 * $Id: TransferClient.java 235 2004-08-03 03:50:46Z mquigley $
 */

package net.medcommons.router.transfer.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.medcommons.router.configuration.Configuration;
import net.medcommons.router.data.DataManager;
import net.medcommons.router.transfer.Transfer;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

public class TransferClient {
  
  public static final String JNDI_NAME = "medcommons/TransferClient";
  
  private static Logger log = Logger.getLogger(TransferClient.class);

  public TransferClient() throws Exception {
    log.info("Created new TransferClient instance.");
  }
  
  public static TransferClient getInstance() throws TransferClientException {
    try {
      InitialContext ctx = new InitialContext();
      TransferClient client = (TransferClient) ctx.lookup(JNDI_NAME);
      return client;
    } catch (NamingException e) {
      throw new TransferClientException("Unable to locate TransferClient in JNDI under name: " + JNDI_NAME, e);
    }    
  }
  
  public void sendFolder(String folderGuid, String destinationDeviceGuid) throws TransferClientException {
    try {
      log.info("Sending folder.");
      
      String deviceUrl = getDeviceUrl(destinationDeviceGuid);
      
      DataManager dmgr = DataManager.getInstance();
      String[] folderContents = dmgr.getFolderContents(folderGuid);
      
      for(int i = 0; i < folderContents.length; i++) {
      	String folderPath = folderContents[i];
      	File folderFile = dmgr.getFolderFile(folderGuid, folderPath);
      	
      	log.info("Sending (folderPath: " + folderPath +") for (folderGuid: " + folderGuid + ")");
      	
      	sendFolderPath(folderGuid, folderPath, folderFile, deviceUrl);
      	
      	log.info("Send of (folderPath: " + folderPath + ") for (folderGuid: " + folderGuid + ") is complete.");
      }
      
    } catch(Exception e) {
      throw new TransferClientException("Unable to complete transfer: (e: " + e.toString() + ")", e);
    }
  }
  
  private String getDeviceUrl(String deviceGuid) throws Exception {
    String deviceDirectoryServiceUrl = (String) Configuration.getInstance().getConfiguredValue("DeviceDirectoryServiceUrl");
    log.info("Using DeviceDirectoryService at (deviceDirectoryServiceUrl: " + deviceDirectoryServiceUrl + ")");
    
    Service service = new Service();
    Call call = (Call) service.createCall();

    call.setTargetEndpointAddress(new java.net.URL(deviceDirectoryServiceUrl));
    call.setOperationName("getDeviceUrl");

    String deviceUrl = (String) call.invoke(new Object[] { deviceGuid });
    log.info("Resolved device URL: (deviceUrl: " + deviceUrl + ")");
    
    return deviceUrl;
  } 
  
  private void sendFolderPath(String folderGuid, String folderPath, File folderFile, String deviceUrl) throws TransferClientException {
  	PostMethod method = null;
  	
  	try {
      String endpointUrl = deviceUrl + "router/TransferEndpoint";
      
      // Write to a byte array.
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(baos);

      // Header.
      Transfer xfer = new Transfer();
      xfer.setFolderGuid(folderGuid);
      xfer.setPath(folderPath);
      out.print(xfer.toXml());
      
      // File data.
      FileInputStream fis = new FileInputStream(folderFile);
      byte[] buffer = new byte[10240];
      int read = 0;
      while((read = fis.read(buffer, 0, 10240)) != -1) {
      	char[] charBuffer = new char[read];
      	for(int i = 0; i < read; i++) {
      	  charBuffer[i] = (char) buffer[i];
      	}
      	out.write(charBuffer);
      }
      fis.close();
     
      out.close();
      baos.close();
      
      // Transmit.
      method = new PostMethod(endpointUrl);
      
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      method.setRequestBody(bais);

      HttpClient client = new HttpClient();
      int result = client.executeMethod(method);

      log.info("Response Code: " + result);   
      
  	} catch(Exception e) {
  	  throw new TransferClientException("Unable to connect to transfer endpoint: " + e.toString(), e);
  	  
  	} finally {
  	  if(method != null) { method.releaseConnection(); }	
  	}
  }

}
