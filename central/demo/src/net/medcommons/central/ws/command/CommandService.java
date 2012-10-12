/*
 * $Id: CommandService.java 68 2004-05-11 17:42:15Z mquigley $
 */

package net.medcommons.central.ws.command;

import net.medcommons.central.controller.Controller;
import net.medcommons.command.RemoteCommand;
import net.medcommons.command.RemoteCommandResult;

import org.apache.log4j.Logger;

public class CommandService {

  private static Logger log = Logger.getLogger(CommandService.class);

  public RemoteCommand[] getPendingCommands(String deviceGuid) throws CommandServiceException {
    log.info("Incoming getPendingCommands request from device (deviceGuid: " + deviceGuid + ")");
    
    try {
      Controller ctrl = Controller.getInstance();
      
      RemoteCommand[] commands = ctrl.popRemoteCommandsForExecution(deviceGuid);
      
      log.info("Returning " + commands.length + " commands to device (deviceGuid: " + deviceGuid + ")");
      
      return commands;
      
    } catch(Exception e) {
      log.error("Error handling getPendingCommands for device (deviceGuid: " + deviceGuid + "): " + e.toString());
      throw new CommandServiceException(e.toString(), e); 
    }
  }
  
  public void postCommandResult(String deviceGuid, RemoteCommandResult result) {
    try {
      Controller ctrl = Controller.getInstance(); 
      
      ctrl.pushRemoteCommandResult(deviceGuid, result);

      log.info("Received: " + result.toString());
      
    } catch(Exception e) {
      log.error("Error handling postCommandResult for device (deviceGuid: " + deviceGuid + "): " + e.toString());
    }
  }

}
