/*
 * $Id: SendFolderCommandHandler.java 236 2004-08-03 17:32:31Z mquigley $
 */

package net.medcommons.router.control.processor.handler;

import net.medcommons.command.RemoteCommand;
import net.medcommons.command.RemoteCommandResult;
import net.medcommons.router.transfer.client.TransferClient;

import org.apache.log4j.Logger;

public class SendFolderCommandHandler extends RemoteCommandHandler {
  
  private static Logger log = Logger.getLogger(SendFolderCommandHandler.class);
  
  public SendFolderCommandHandler(RemoteCommand cmd) {
    super(cmd); 
  }
  
  public RemoteCommandResult handle() {
    log.info("Starting.");
    
    String destinationDeviceGuid = (String) cmd.getParameters().get("destinationDeviceGuid");
    String folderGuid = (String) cmd.getParameters().get("folderGuid");
    
    log.info("(folderGuid: " + folderGuid + ")");
    
    try {
      TransferClient client = (TransferClient) TransferClient.getInstance();
      client.sendFolder(folderGuid, destinationDeviceGuid);
      
      result.setSuccess(true);
      
    } catch(Exception e) {
      log.error("Error: " + e.toString());
      
      result.setSuccess(false);
    }
    
    return result;
  }

}
