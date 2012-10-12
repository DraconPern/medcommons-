/*
 * $Id: ConfigurationService.java 383 2005-01-03 04:59:23Z ssadedin $
 */

package net.medcommons.modules.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.naming.InitialContext;
import javax.naming.Name;

import org.apache.log4j.Logger;

/**
 * A utility class for facilitating embedding into containers.  This class behaves as both a
 * standard ModelMBean and a Tomcat LifecycleListener to allow easy embedding into JMX-enabled
 * containers and tomcat.
 * @author ssadedin
 */
public class ConfigurationService implements ConfigurationServiceMBean /*, LifecycleListener */ {

  /**
   * The path to the XML configuration file
   */
  private String configPath="config.xml";

  private static Logger log = Logger.getLogger(ConfigurationService.class);

  public ConfigurationService() {
  	log.info("Created configuration service");
  }

  public void start() throws Exception {
    log.info("Starting.");
    log.info("Configuration path is " + configPath + ".  Binding to JNDI under name " + Configuration.jndiName);

    Configuration config = new Configuration();
    File confDirectory = new File("conf");
    if (!confDirectory.exists()){
    	throw new FileNotFoundException("Configuration directory " + confDirectory.getAbsolutePath());
    }
    File confFile = new File(confDirectory, configPath);
    if (!confFile.exists()){
    	throw new FileNotFoundException("Configuration file " + confFile.getAbsolutePath());
    }
    if (!confFile.canRead()){
    	throw new RuntimeException("Configuration file:" + confFile.getAbsolutePath() + " can not be read ");
    }
    log.info("About to load configuration file " + confFile.getAbsolutePath());
    InputStream configStream = new FileInputStream(confFile);
    if (configStream == null){
    	throw new NullPointerException("Null input stream from configuration file " + confFile.getAbsolutePath());
    }
    config.loadConfig(configStream);
    configStream.close();

    InitialContext ctx = new InitialContext();
    Name name = ctx.getNameParser("").parse(Configuration.jndiName);
    ctx.bind(name, config);

    log.info("Bound Configuration instance into JNDI at: " + Configuration.jndiName);

    log.info("Configuration service started.");
  }

  public void stop() {
    try {
      log.info("Stopping.");

      InitialContext ctx = new InitialContext();
      ctx.unbind(Configuration.jndiName);
      // NonSerializableFactory.unbind(Configuration.jndiName);

      log.info("Removed Configuration instance from JNDI at: " + Configuration.jndiName);

      log.info("Configuration service stopped.");

    } catch(Throwable t) {
      log.error("Error unbinding Configuration: " + t.toString());
    }
  }

  public void setConfigPath(String configPath) {
    this.configPath = configPath;
  }

  public String getConfigPath() {
    return null;
  }


  /**
   * Handles Tomcat Lifecycle events
   */
  /*public void lifecycleEvent(LifecycleEvent event) {
    if (Lifecycle.START_EVENT.equals(event.getType())) {
      try {
        this.start();
      } catch (Exception e) {
        log.error("Error starting ConfigurationService", e);
}
    }
    else
    if (Lifecycle.STOP_EVENT.equals(event.getType())) {
      this.stop();
    }
  }  */
}
