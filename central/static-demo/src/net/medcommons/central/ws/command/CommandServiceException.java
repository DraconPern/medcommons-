/*
 * $Id: CommandServiceException.java 57 2004-05-06 17:38:53Z mquigley $
 */

package net.medcommons.central.ws.command;

public class CommandServiceException extends Exception {
  
  public CommandServiceException() {
    super();
  }
  
  public CommandServiceException(String msg) {
    super(msg);
  }
  
  public CommandServiceException(String msg, Throwable t) {
    super(msg, t);
  }

}
