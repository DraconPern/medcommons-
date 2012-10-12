/*
 * $Id: ConfigurationService.java 86 2004-05-07 18:53:10Z mquigley $
 */

package net.medcommons.router.configuration;

import java.io.InputStream;

import javax.naming.InitialContext;
import javax.naming.Name;

import org.apache.log4j.Logger;
import org.jboss.naming.NonSerializableFactory;

public class ConfigurationService implements ConfigurationServiceMBean {

  private String configPath;
  private static Logger log = Logger.getLogger(ConfigurationService.class);

  public void start() throws Exception {
    log.info("Starting.");
    
    Configuration config = new Configuration();
    
    InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath);
    config.loadConfig(configStream);
    configStream.close();
    
    InitialContext ctx = new InitialContext();
    Name name = ctx.getNameParser("").parse(Configuration.jndiName);
    NonSerializableFactory.rebind(name, config, true);
    
    log.info("Bound Configuration instance into JNDI at: " + name);
    
    log.info("Configuration service started.");    
  }

  public void stop() {
    try {
      log.info("Starting.");
      
      InitialContext ctx = new InitialContext();
      ctx.unbind(Configuration.jndiName);
      NonSerializableFactory.unbind(Configuration.jndiName);
      
      log.info("Removed Configuration instance from JNDI at: " + Configuration.jndiName);
      
      log.info("Configuration service stopped.");
      
    } catch(Exception e) {
      log.error("Error unbinding Configuration: " + e.toString()); 
    }    
  }
  
  public void setConfigPath(String configPath) {
    this.configPath = configPath;
  }
  
  public String getConfigPath() {
    return null;
  }

}
