/*
 * $Id: ConfigurationServiceMBean.java 56 2004-04-22 21:53:45Z mquigley $
 */

package net.medcommons.router.configuration;

public interface ConfigurationServiceMBean {

  public void start() throws Exception;
  public void stop();

  public void setConfigPath(String configPath);
  public String getConfigPath();

}
