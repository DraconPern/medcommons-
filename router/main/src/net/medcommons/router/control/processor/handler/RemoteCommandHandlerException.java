/*
 * $Id: RemoteCommandHandlerException.java 201 2004-07-14 19:25:02Z mquigley $
 */

package net.medcommons.router.control.processor.handler;

public class RemoteCommandHandlerException extends Exception {

  public RemoteCommandHandlerException() {
    super();
  }
  
  public RemoteCommandHandlerException(String msg) {
    super(msg);
  }
  
  public RemoteCommandHandlerException(String msg, Throwable t) {
    super(msg, t); 
  }

}
