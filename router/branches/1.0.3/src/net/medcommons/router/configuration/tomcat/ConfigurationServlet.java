/*
 * $Id$
 * Created on Dec 15, 2004
 */
package net.medcommons.router.configuration.tomcat;

import static net.medcommons.modules.utils.Str.blank;

// import groovy.ui.Console;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.*;

import net.medcommons.modules.backup.BackupThread;
import net.medcommons.modules.backup.SmallFileBackupService;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.configuration.ConfigurationService;
import net.medcommons.modules.services.interfaces.BackupService;
import net.medcommons.modules.services.interfaces.DocumentIndexService;
import net.medcommons.modules.utils.Str;
import net.medcommons.rest.RESTConfiguration;
import net.medcommons.rest.RESTConfigurationException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.router.selftests.All;
import net.medcommons.router.services.expiry.ExpireAccountsTask;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.wado.WADOImageJob2;

import org.apache.log4j.Logger;

/**
 * ConfigurationServlet is the first servlet run at gateway startup.  It's primary
 * responsibility is to load the configuration and ensure that it is available
 * to services that follow.  However it also bootstraps a number of other important
 * features such as initializing the SSL configuration and registering the gateway.
 * 
 * @author ssadedin
 */
public class ConfigurationServlet implements Servlet {
    
  /**
   * Interval at which this gateway should re-register itself automatically
   */
  private static final int NODE_REGISTRATION_INTERVAL = 15*60*1000;

/**
   * The service object, bound into JNDI
   */
  private ConfigurationService service = null;
  
  /**
   * Repeating registerNode task to registe this node at regular intervals
   */
  private RegisterNodeTask registerNodeTask;
  
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(ConfigurationServlet.class);
  
  /**
   * Force Self Tests to be registered.
   */
  private static All all = new All();
  
  

  /**
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    if (this.service == null) {
      this.service = new ConfigurationService();
      try {
        service.start();
        
        // Ensure the REST module is configured
        // We just configure it with a pass-through to the static Configuration class
        log.info("Initializing REST configuration ...");
        RESTUtil.init( new RESTConfiguration() {
            public String getProperty(String name) throws RESTConfigurationException {
                try {
                    return Configuration.getProperty(name);
                }
                catch (ConfigurationException e) {
                    throw new RESTConfigurationException("Failed retrieving configuration value " + name, e);
                }
            }
            public String getProperty(String name, String defaultValue) {
                return Configuration.getProperty(name,defaultValue);
            }

            public int getProperty(String name, int defaultValue) {
                return Configuration.getProperty(name, defaultValue);
            }

            public boolean getProperty(String name, boolean defaultValue) {
                return Configuration.getProperty(name,defaultValue);
            }            
        });
        
                
        // Initialize SSL
        // HACK:  this should probably be initialized somewhere else, but this servlet handily runs
        // first thing on start up.
        RESTUtil.initSSL();        
        
        // Defaults
        String host = InetAddress.getLocalHost().getHostName();
        if(Configuration.getProperty("RemoteHost")==null) {
            Configuration.getAllProperties().setProperty("RemoteHost", host);
        }
        
        // Default CommonsBase to CommonsServer - /ws
        String commonsBase = Configuration.getProperty("CommonsBase");
        if(commonsBase==null) {
            String commonsServer = Configuration.getProperty("CommonsServer");
            if(blank(commonsServer)) {
                log.error("CommonsServer value not defined!");
                throw new ServletException("Unable to resolve value for CommonsServer configuration value");                
            }
            commonsBase = commonsServer.replaceAll("/ws$", "");
            log.info("Computed CommonsBase as " + commonsBase);
            Configuration.getAllProperties().setProperty("CommonsBase", commonsBase);
        }
        
        setAccountsBaseUrl();
        
        Timer timer = new Timer();
        // Unless disabled, register this node with central
        if(!"false".equals(config.getInitParameter("registerNode"))) {
            log.info("Creating registration task at interval " + NODE_REGISTRATION_INTERVAL);
            // Register task to periodically renew our registration with central
            this.registerNodeTask = new RegisterNodeTask();
            timer.scheduleAtFixedRate(registerNodeTask, 0, NODE_REGISTRATION_INTERVAL);
        }
        
        String nodeId = Configuration.getProperty("NodeID");
        if(Str.blank(nodeId) || "UNKNOWN".equals(nodeId)) {
            log.info("NodeID is empty:  This instance will be disabled until a NodeID is entered");
        }
        else {
            config.getServletContext().setAttribute("NodeID", nodeId);
        }
        
        
        // Log a warning message if remote log is on
        if(Configuration.getProperty("EnableRemoteLog",false)) {
            log.warn("REMOTE LOG IS ENABLED.  THIS GATEWAY IS INSECURE!!!!!!");
        }
        
        // Add a task to watch the boot parameters file and reload it
        timer.scheduleAtFixedRate(new TimerTask() {
            long oldFileTimeStamp = 0;
            @Override
            public void run() {
                if(Configuration.loadedBootParametersPath != null) {
                    File f = new File(Configuration.loadedBootParametersPath);
                    long lastModified = f.lastModified();
                    if(oldFileTimeStamp != 0 && f.lastModified() > oldFileTimeStamp) {
                        try {
                            Configuration.reload();
                        }
                        catch (ConfigurationException e) {
                            log.warn("Failed to reload configuration",e);
                        } 
                    }
                    oldFileTimeStamp = lastModified;
                }
            }
        }, 5, Configuration.getProperty("PropertiesReloadPollInterval",60000));
        
        // Add a task to expire old backups
        ExpireAccountsTask expireTask = Configuration.getBean("expiryService");
        if(expireTask != null) {
            timer.schedule(expireTask, 0, Configuration.getProperty("AccountExpiryPollInterval", 24 * 3600 * 1000));
        }
        
        // Note: trying to move these services earlier in the process results in 
        // problems because they depend on configuration which is initialized above
        BackupService backupService = Configuration.getBean("backupService");
        DocumentIndexService indexService = Configuration.getBean("documentIndexService");
        
        Runnable backupThread = new BackupThread(indexService, backupService);
        new Thread(backupThread,"BackupService").start();
        
        
        /*
        Console console = new Console();
        console.setVariable("expireTask", expireTask);
        console.setVariable("config", Configuration.getAllProperties());
        console.setVariable("services", Configuration.getBean("systemServicesFactory"));
        console.run();
        */
      } 
      catch (Exception e) {
        log.fatal("Exception while initializing",e);
        throw new ServletException("Unable to configure: " + e.toString(), e);
      }      
      
    }
    
    
    // The idea here is to force these classes to get instantiated and therefore initialized
    if(WADOImageJob2.class != null);
    if(SmallFileBackupService.class != null);
    
    if(Configuration.getProperty("EncryptionEnabled",true)) {
        LocalFileRepository repo = (LocalFileRepository) RepositoryFactory.getLocalRepository();
    }
  }

  
  /**
   * Adds a "virtual" config property that is computed from the 
   * Account Server web service url.   This is the best url for
   * referring to content that should be relative to the generic 
   * "appliance". 
   * 
   * @throws ConfigurationException
   * @throws ServletException
   * @throws MalformedURLException
   */
  public void setAccountsBaseUrl() throws ConfigurationException, ServletException, MalformedURLException {
      // Compute the base appliance URL from the account property
      String accountsBaseUrl = Configuration.getProperty("AccountsBaseUrl");
      if(blank(accountsBaseUrl)) {
          // Get the URL of the account server
          String accountServer = Configuration.getProperty("AccountServer");
          if(blank(accountServer)) 
              throw new ServletException("Unable to resolve value for AccountServer configuration value");                
          
          
          URL baseUrl = new URL(accountServer);
          
          accountsBaseUrl = baseUrl.getProtocol() + "://" + baseUrl.getHost();
          if(baseUrl.getPort() != 443 && baseUrl.getPort() != 80 && baseUrl.getPort()>0)
              accountsBaseUrl += ":" + baseUrl.getPort();
          accountsBaseUrl += "/";
          
          log.info("Computed accountsBaseUrl as " + accountsBaseUrl);
          Configuration.getAllProperties().setProperty("AccountsBaseUrl", accountsBaseUrl);
      }
  }

  /**
   * Dummy
   */
  public ServletConfig getServletConfig() {
    return null;
  }

  /**
   * Dummy
   */
  public void service(ServletRequest arg0, ServletResponse arg1)
      throws ServletException, IOException 
  {
    log.error("Request sent to ConfigurationServlet:  should be used only for initialization");    
  }

  /**
   * Dummy
   * @see javax.servlet.Servlet#getServletInfo()
   */
  public String getServletInfo() {
    return null;
  }

  /**
   * Unbind the configuration info
   */
  public void destroy() {
    if(this.service != null)
      this.service.stop();
  }
}
