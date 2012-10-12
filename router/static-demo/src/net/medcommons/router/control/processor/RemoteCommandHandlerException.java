/*
 * $Id: RemoteCommandHandlerException.java 137 2004-06-17 21:28:08Z mquigley $
 */

package net.medcommons.router.control.processor;

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
