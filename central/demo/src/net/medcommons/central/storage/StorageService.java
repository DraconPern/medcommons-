/*
 * $Id: StorageService.java 55 2004-05-06 16:52:35Z mquigley $
 */

package net.medcommons.central.storage;

import javax.naming.InitialContext;
import javax.naming.Name;

import org.apache.log4j.Logger;
import org.jboss.naming.NonSerializableFactory;

public class StorageService implements StorageServiceMBean {

  private String storagePath;
  private static Logger log = Logger.getLogger(StorageService.class);

  public String getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(String storagePath) {
    this.storagePath = storagePath;
  }

  public void start() throws Exception {
    log.info("Starting.");
    
    Storage store = new Storage(storagePath);
    
    InitialContext ctx = new InitialContext();
    Name name = ctx.getNameParser("").parse(Storage.JNDI_NAME);
    NonSerializableFactory.rebind(name, store, true);
    
    log.info("Bound Store instance into JNDI at: " + name);
    
    log.info("Storage service started.");     
  }

  public void stop() {
    try {
      log.info("Stopping.");
      
      InitialContext ctx = new InitialContext();
      ctx.unbind(Storage.JNDI_NAME);
      NonSerializableFactory.unbind(Storage.JNDI_NAME);
      
      log.info("Removed Store instance from JNDI at: " + Storage.JNDI_NAME);
      
      log.info("Storage service stopped.");
      
    } catch(Exception e) {
      log.error("Error unbinding Store: " + e.toString()); 
    }    
  }

}
