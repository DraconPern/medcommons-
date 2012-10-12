/*
 * $Id: PingCommandHandler.java 58 2004-04-22 22:31:27Z mquigley $
 */

package net.medcommons.router.control.processor.handler;

import net.medcommons.command.RemoteCommand;
import net.medcommons.command.RemoteCommandResult;
import net.medcommons.router.control.processor.RemoteCommandHandler;

public class PingCommandHandler extends RemoteCommandHandler {

  public PingCommandHandler(RemoteCommand cmd) {
    super(cmd); 
  }

  public RemoteCommandResult handle() {
    result.setSuccess(true);
    result.setMessage("Ping Successful");
    return result;
  }

}
