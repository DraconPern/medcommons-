/*
 * $Id: TransferClientService.java 193 2004-07-13 01:53:15Z mquigley $
 */

package net.medcommons.router.transfer.client;

import javax.naming.InitialContext;
import javax.naming.Name;

import org.apache.log4j.Logger;
import org.jboss.naming.NonSerializableFactory;

public class TransferClientService implements TransferClientServiceMBean {

  private static Logger log = Logger.getLogger(TransferClientService.class);

  public void start() throws Exception {
     log.info("Starting.");
    
     InitialContext ctx = new InitialContext();
     Name name = ctx.getNameParser("").parse(TransferClient.JNDI_NAME);
     NonSerializableFactory.rebind(name, new TransferClient(), true);
    
     log.info("Bound TransferClient instance into JNDI at: " + name);
    
     log.info("TransferClient service started.");    
   }

   public void stop() {
     try {
       log.info("Stopping.");
      
       InitialContext ctx = new InitialContext();
       ctx.unbind(TransferClient.JNDI_NAME);
       NonSerializableFactory.unbind(TransferClient.JNDI_NAME);
      
       log.info("Removed TransferClient instance from JNDI at: " + TransferClient.JNDI_NAME);
      
       log.info("TransferClient service stopped.");
      
     } catch(Exception e) {
       log.error("Error unbinding TransferClient: " + e.toString()); 
     }    
   }  
  
}
