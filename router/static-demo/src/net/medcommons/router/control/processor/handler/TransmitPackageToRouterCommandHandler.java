/*
 * $Id: TransmitPackageToRouterCommandHandler.java 93 2004-05-12 03:28:47Z mquigley $
 */

package net.medcommons.router.control.processor.handler;

import java.io.FileOutputStream;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;

import net.medcommons.command.RemoteCommand;
import net.medcommons.command.RemoteCommandResult;
import net.medcommons.conversion.Base64Utility;
import net.medcommons.router.configuration.Configuration;
import net.medcommons.router.control.processor.RemoteCommandHandler;

public class TransmitPackageToRouterCommandHandler extends RemoteCommandHandler {

  private static Logger log = Logger.getLogger(TransmitPackageToRouterCommandHandler.class);

  public TransmitPackageToRouterCommandHandler(RemoteCommand cmd) {
    super(cmd); 
  }

  public RemoteCommandResult handle() {
    log.info("Starting.");
    
    try {
      String guid = (String) cmd.getParameters().get("guid");
      log.info("Operating on package with (guid: " + guid + ")");
      
      byte[] data = receiveData(guid);
      log.info("Transmission complete.");
      
      storeData(guid, data);
      log.info("Stored " + data.length + " bytes for package with (guid: " + guid + ")");
      
      result.setSuccess(true);
      
    } catch(Exception e) {
      log.info("Transmission failed: " + e.toString());
      
      result.setSuccess(false);
      result.setMessage(e.toString());
    }
    
    return result;
  }
  
  private byte[] receiveData(String guid) throws Exception {
    String endpoint = (String) Configuration.getInstance().getConfiguredValue("PackageServiceUrl");

    Service service = new Service();
    Call call = (Call) service.createCall();

    call.setTargetEndpointAddress(new java.net.URL(endpoint));
    call.setOperationName("fetchPackage");
    
    long startMs = System.currentTimeMillis();
    String encodedData = (String) call.invoke(new Object[] { guid } );
    long endMs = System.currentTimeMillis();
    
    byte[] data = Base64Utility.decode(encodedData);
    
    log.info("Received " + data.length + " bytes in " + (endMs - startMs) + " milliseconds.");
    
    return data;
  }  
  
  private void storeData(String guid, byte[] data) throws Exception {
    String packageRoot = (String) Configuration.getInstance().getConfiguredValue("PackageRoot");
    String path = packageRoot + "/" + guid;
    
    FileOutputStream fos = new FileOutputStream(path);
    fos.write(data);
    fos.close(); 
  }

}
