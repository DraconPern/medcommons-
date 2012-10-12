/*
 * $Id: RemoteCommandHandlerFactory.java 201 2004-07-14 19:25:02Z mquigley $
 */

package net.medcommons.router.control.processor.handler;

import net.medcommons.command.RemoteCommand;

public class RemoteCommandHandlerFactory {

  public static RemoteCommandHandler getHandler(RemoteCommand cmd) throws RemoteCommandHandlerException {
    // Ping.
    if(cmd.getOperation().equals(RemoteCommand.PING_OPERATION)) {
      return new PingCommandHandler(cmd);  
    }
    
    // Send folder.
    if(cmd.getOperation().equals(RemoteCommand.SEND_FOLDER_OPERATION)) {
      return new SendFolderCommandHandler(cmd); 
    }
    
    // Receive folder.
    if(cmd.getOperation().equals(RemoteCommand.RECEIVE_FOLDER_OPERATION)) {
      return null;
    }

    // Unknown command.
    throw new RemoteCommandHandlerException("Unknown router command (operation: " + cmd.getOperation() + ")");
  }

}
