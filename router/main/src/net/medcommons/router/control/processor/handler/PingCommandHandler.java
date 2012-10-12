/*
 * $Id: PingCommandHandler.java 200 2004-07-14 17:45:30Z mquigley $
 */

package net.medcommons.router.control.processor.handler;

import net.medcommons.command.RemoteCommand;
import net.medcommons.command.RemoteCommandResult;

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
