/*
 * $Id: RemoteCommandHandlerFactory.java 88 2004-05-10 21:02:46Z mquigley $
 */

package net.medcommons.router.control.processor;

import net.medcommons.command.RemoteCommand;
import net.medcommons.router.control.processor.handler.*;

public class RemoteCommandHandlerFactory {

  public static RemoteCommandHandler getHandler(RemoteCommand cmd) throws RemoteCommandHandlerException {
    // Ping.
    if(cmd.getOperation().equals(RemoteCommand.PING_OPERATION)) {
      return new PingCommandHandler(cmd);  
    }
    
    // Move Study to Central.
    if(cmd.getOperation().equals(RemoteCommand.TRANSMIT_PACKAGE_TO_CENTRAL_OPERATION)) {
      return new TransmitPackageToCentralCommandHandler(cmd); 
    }
    
    if(cmd.getOperation().equals(RemoteCommand.TRANSMIT_PACKAGE_TO_ROUTER_OPERATION)) {
      return new TransmitPackageToRouterCommandHandler(cmd); 
    }

    // Unknown command.
    throw new RemoteCommandHandlerException("Unknown router command (operation: " + cmd.getOperation() + ")");
  }

}
