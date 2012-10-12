/*
 * $Id: DataManagerService.java 86 2004-05-07 18:53:10Z mquigley $
 */

package net.medcommons.router.data;

import javax.naming.InitialContext;
import javax.naming.Name;

import org.apache.log4j.Logger;
import org.jboss.naming.NonSerializableFactory;

public class DataManagerService implements DataManagerServiceMBean {

  private static Logger log = Logger.getLogger(DataManagerService.class);

  public void start() throws Exception {
     log.info("Starting.");
    
     InitialContext ctx = new InitialContext();
     Name name = ctx.getNameParser("").parse(DataManager.JNDI_NAME);
     NonSerializableFactory.rebind(name, new DataManager(), true);
    
     log.info("Bound DataManager instance into JNDI at: " + name);
    
     log.info("DataManager service started.");    
   }

   public void stop() {
     try {
       log.info("Starting.");
      
       InitialContext ctx = new InitialContext();
       ctx.unbind(DataManager.JNDI_NAME);
       NonSerializableFactory.unbind(DataManager.JNDI_NAME);
      
       log.info("Removed Configuration instance from JNDI at: " + DataManager.JNDI_NAME);
      
       log.info("DataManager service stopped.");
      
     } catch(Exception e) {
       log.error("Error unbinding DataManager: " + e.toString()); 
     }    
   }

}
