/*
 * $Id: ConfigurationException.java 56 2004-04-22 21:53:45Z mquigley $
 */

package net.medcommons.router.configuration;

public class ConfigurationException extends Exception {

  public ConfigurationException() {
    super();
  }
  
  public ConfigurationException(String msg) {
    super(msg);
  }
  
  public ConfigurationException(String msg, Throwable t) {
    super(msg, t); 
  }

}
