/*
 * $Id: LogService.java 71 2004-05-12 04:02:33Z mquigley $
 */

package net.medcommons.central.log;

import javax.naming.InitialContext;
import javax.naming.Name;

import org.apache.log4j.Logger;
import org.jboss.naming.NonSerializableFactory;

public class LogService implements LogServiceMBean {

  private static Logger log = Logger.getLogger(LogService.class);

  public void start() throws Exception {
    log.info("Starting.");
    
    InitialContext ctx = new InitialContext();
    Name name = ctx.getNameParser("").parse(Log.JNDI_NAME);
    NonSerializableFactory.rebind(name, new Log(), true);
    
    log.info("Bound Log instance into JNDI at: " + name);
    
    log.info("Log service started.");
  }

  public void stop() {
    try {
      log.info("Stopping.");
      
      InitialContext ctx = new InitialContext();
      ctx.unbind(Log.JNDI_NAME);
      NonSerializableFactory.unbind(Log.JNDI_NAME);
      
      log.info("Removed Log instance from JNDI at: " + Log.JNDI_NAME);
      
      log.info("Log service stopped.");
      
    } catch(Exception e) {
      log.error("Error unbinding Log: " + e.toString()); 
    }
  }

}
