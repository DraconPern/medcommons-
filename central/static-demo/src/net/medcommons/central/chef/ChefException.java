/*
 * $Id: ChefException.java 55 2004-05-06 16:52:35Z mquigley $
 */

package net.medcommons.central.chef;

public class ChefException extends Exception {

  public ChefException() {
    super();
  }
  
  public ChefException(String msg) {
    super(msg);
  }

  public ChefException(String msg, Throwable t) {
    super(msg, t);
  }
  
}
