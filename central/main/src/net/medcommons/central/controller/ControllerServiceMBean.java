/*
 * $Id: ControllerServiceMBean.java 41 2004-04-22 22:22:12Z mquigley $
 */

package net.medcommons.central.controller;

public interface ControllerServiceMBean {

  public void start() throws Exception;
  public void stop();

  public void pushPingCommand(String routerGuid);

}
