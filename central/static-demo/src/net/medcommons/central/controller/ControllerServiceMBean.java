/*
 * $Id: ControllerServiceMBean.java 78 2004-06-17 21:10:13Z mquigley $
 */

package net.medcommons.central.controller;

public interface ControllerServiceMBean {

  public void start() throws Exception;
  public void stop();

  public void pushPingCommand(String routerGuid);

}
