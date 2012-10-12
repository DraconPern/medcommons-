/*
 * $Id: ConfigurationException.java 56 2004-04-22 21:53:45Z mquigley $
 */

package net.medcommons.router.services.selection.action;

public class SelectionException extends Exception {

  public SelectionException() {
    super();
  }
  
  public SelectionException(String msg) {
    super(msg);
  }
  
  public SelectionException(String msg, Throwable t) {
    super(msg, t); 
  }

}
