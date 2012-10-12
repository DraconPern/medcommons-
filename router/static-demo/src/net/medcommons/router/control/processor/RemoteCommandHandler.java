/*
 * $Id: RemoteCommandHandler.java 88 2004-05-10 21:02:46Z mquigley $
 */

package net.medcommons.router.control.processor;

import net.medcommons.command.RemoteCommand;
import net.medcommons.command.RemoteCommandResult;

/**
 * The base class for all RemoteCommandHandler instances.
 * @author <a href="mailto:michael@quigley.com">Michael Quigley</a>
 */
public abstract class RemoteCommandHandler {

  protected RemoteCommand cmd;
  protected RemoteCommandResult result;
  
  /**
   * Construct a new RemoteCommandHandler instance.
   * @param attributes a Hashtable of attributes to this command.
   */
  public RemoteCommandHandler(RemoteCommand cmd) {
    this.cmd = cmd;
    
    result = new RemoteCommandResult();
    result.setGuid(cmd.getGuid());
  }
  
  /**
   * Entrance point into the handler.
   * @return a RemoteCommandResult instance to reflect the command status.
   */
  public abstract RemoteCommandResult handle();

}
