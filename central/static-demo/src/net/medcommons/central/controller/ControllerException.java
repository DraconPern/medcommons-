/*
 * $Id: ControllerException.java 69 2004-05-11 18:08:36Z mquigley $
 */

package net.medcommons.central.controller;

public class ControllerException extends Exception {
  
  public ControllerException() {
    super();
  }
  
  public ControllerException(String msg) {
    super(msg);
  }
  
  public ControllerException(String msg, Throwable t) {
    super(msg, t);
  }

}
