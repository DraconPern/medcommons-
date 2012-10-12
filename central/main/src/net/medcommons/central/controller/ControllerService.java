/*
 * $Id: ControllerService.java 55 2004-05-06 16:52:35Z mquigley $
 */

package net.medcommons.central.controller;

import javax.naming.InitialContext;
import javax.naming.Name;

import net.medcommons.command.RemoteCommand;

import org.apache.log4j.Logger;
import org.jboss.naming.NonSerializableFactory;

public class ControllerService implements ControllerServiceMBean {

  private static Logger log = Logger.getLogger(ControllerService.class);

  public void start() throws Exception {
    log.info("Starting.");
    
    InitialContext ctx = new InitialContext();
    Name name = ctx.getNameParser("").parse(Controller.JNDI_NAME);
    NonSerializableFactory.rebind(name, new Controller(), true);
    
    log.info("Bound Controller instance into JNDI at: " + name);
    
    log.info("Controller service started.");
  }

  public void stop() {
    try {
      log.info("Stopping.");
      
      InitialContext ctx = new InitialContext();
      ctx.unbind(Controller.JNDI_NAME);
      NonSerializableFactory.unbind(Controller.JNDI_NAME);
      
      log.info("Removed Controller instance from JNDI at: " + Controller.JNDI_NAME);
      
      log.info("Controller service stopped.");
      
    } catch(Exception e) {
      log.error("Error unbinding Controller: " + e.toString()); 
    }
  }
  
  public void pushPingCommand(String routerGuid) {
    try {
      InitialContext ctx = new InitialContext();
      Controller ctrl = (Controller) ctx.lookup("medcommons/Controller");
      
      RemoteCommand cmd = new RemoteCommand();
      cmd.setOperation(RemoteCommand.PING_OPERATION);
      
      String guid = ctrl.pushRemoteCommand(routerGuid, cmd);
      
      log.info("Pushed RemoteCommand with operation 'ping' (guid: " + guid + ")");
      
    } catch(Exception e) {
      log.error("Error: " + e.toString()); 
    }
  }  

}
