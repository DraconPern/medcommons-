/*
 * $Id: LogServiceMBean.java 71 2004-05-12 04:02:33Z mquigley $
 */

package net.medcommons.central.log;

public interface LogServiceMBean {

  public void start() throws Exception;
  public void stop();

}
