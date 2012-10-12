/*
 * $Id: TransmitPackageToCentralCommandHandler.java 93 2004-05-12 03:28:47Z mquigley $
 */

package net.medcommons.router.control.processor.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;

import net.medcommons.command.RemoteCommand;
import net.medcommons.command.RemoteCommandResult;
import net.medcommons.conversion.Base64Utility;
import net.medcommons.router.configuration.Configuration;
import net.medcommons.router.control.processor.RemoteCommandHandler;

public class TransmitPackageToCentralCommandHandler extends RemoteCommandHandler {
  
  private static Logger log = Logger.getLogger(TransmitPackageToCentralCommandHandler.class);

  public TransmitPackageToCentralCommandHandler(RemoteCommand cmd) {
    super(cmd); 
  }

  public RemoteCommandResult handle() {
    log.info("Starting.");
    
    try {
      String guid = (String) cmd.getParameters().get("guid");
      log.info("Operating on package with (guid: " + guid + ")");
      
      byte[] data = loadData(guid);
      log.info("Loaded " + data.length + " bytes for (guid: " + guid + ")");
      
      transmitData(guid, data);
      log.info("Transmission complete.");
      
      result.setSuccess(true);
      
    } catch(Exception e) {
      log.info("Transmission failed: " + e.toString());
      
      result.setSuccess(false);
      result.setMessage(e.toString());
    }
    
    return result;
  }
  
  private byte[] loadData(String guid) throws Exception {
    String packageRoot = (String) Configuration.getInstance().getConfiguredValue("PackageRoot");
    String path = packageRoot + "/" + guid;
    
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

  private void transmitData(String guid, byte[] data) throws Exception {
    String endpoint = (String) Configuration.getInstance().getConfiguredValue("PackageServiceUrl");

    Service service = new Service();
    Call call = (Call) service.createCall();

    call.setTargetEndpointAddress(new java.net.URL(endpoint));
    call.setOperationName("storePackageWithExistingGuid");
    
    String encodedData = Base64Utility.encode(data);
    
    long startMs = System.currentTimeMillis();
    call.invoke(new Object[] { guid, encodedData } );
    long endMs = System.currentTimeMillis();
    
    log.info("Transmitted " + data.length + " bytes in " + (endMs - startMs) + " milliseconds.");
  }

}
