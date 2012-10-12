/*
 * $Id: GuidFactoryService.java 71 2004-05-12 04:02:33Z mquigley $
 */

package net.medcommons.central.guid;

import javax.naming.InitialContext;
import javax.naming.Name;

import org.apache.log4j.Logger;
import org.jboss.naming.NonSerializableFactory;

public class GuidFactoryService implements GuidFactoryServiceMBean {

  private static Logger log = Logger.getLogger(GuidFactoryService.class);

  public void start() throws Exception {
    log.info("Starting.");
    
    InitialContext ctx = new InitialContext();
    Name name = ctx.getNameParser("").parse(GuidFactory.JNDI_NAME);
    NonSerializableFactory.rebind(name, new GuidFactory(), true);
    
    log.info("Bound GuidFactory instance into JNDI at: " + name);
    
    log.info("GuidFactory service started.");
  }

  public void stop() {
    try {
      log.info("Stopping.");
      
      InitialContext ctx = new InitialContext();
      ctx.unbind(GuidFactory.JNDI_NAME);
      NonSerializableFactory.unbind(GuidFactory.JNDI_NAME);
      
      log.info("Removed GuidFactory instance from JNDI at: " + GuidFactory.JNDI_NAME);
      
      log.info("GuidFactory service stopped.");
      
    } catch(Exception e) {
      log.error("Error unbinding GuidFactory: " + e.toString()); 
    }
  }
}
