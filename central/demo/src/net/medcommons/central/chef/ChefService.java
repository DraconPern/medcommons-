/*
 * $Id: ChefService.java 55 2004-05-06 16:52:35Z mquigley $
 */

package net.medcommons.central.chef;

import javax.naming.InitialContext;
import javax.naming.Name;

import org.apache.log4j.Logger;
import org.jboss.naming.NonSerializableFactory;

public class ChefService implements ChefServiceMBean {

  private static Logger log = Logger.getLogger(ChefService.class);

  public void start() throws Exception {
    log.info("Starting.");
    
    InitialContext ctx = new InitialContext();
    Name name = ctx.getNameParser("").parse(Chef.JNDI_NAME);
    NonSerializableFactory.rebind(name, new Chef(), true);
    
    log.info("Bound Chef instance into JNDI at: " + name);
    
    log.info("Chef service started.");
  }

  public void stop() {
    try {
      log.info("Stopping.");
      
      InitialContext ctx = new InitialContext();
      ctx.unbind(Chef.JNDI_NAME);
      NonSerializableFactory.unbind(Chef.JNDI_NAME);
      
      log.info("Removed Chef instance from JNDI at: " + Chef.JNDI_NAME);
      
      log.info("Chef service stopped.");
      
    } catch(Exception e) {
      log.error("Error unbinding Controller: " + e.toString()); 
    }
  }

}
