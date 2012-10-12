/*
 * $Id: Configuration.java 149 2004-06-22 08:25:55Z ssadedin $
 */

package net.medcommons.router.configuration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class Configuration {

  private HashMap configMap;
  
  private static Logger log = Logger.getLogger(Configuration.class);

  public static String jndiName = "medcommons/Configuration";

  public Configuration() {
    configMap = new HashMap();
  }
  
  public void loadConfig(InputStream is) throws Exception {
    log.info("Configuring.");
    
    SAXBuilder builder = new SAXBuilder();
    Document document = builder.build(is);    
    
    Element root = document.getRootElement();
    List properties = root.getChildren("property");
    
    Iterator i = properties.iterator();
    while(i.hasNext()) {
      Element attribute = (Element) i.next();
      
      String name = attribute.getAttribute("name").getValue();
      String value = attribute.getTextTrim();
      
      configMap.put(name, value);
      
      log.info("Stored configured value: (name: " + name + ")(value: " + value + ")");
    }
    
    log.info("Configuration complete.");
  }
  
  public Set keys() {
    return configMap.keySet(); 
  }
  
  public Object getConfiguredValue(String key) throws ConfigurationException {
    if(!configMap.containsKey(key)) {
      throw new ConfigurationException("No such configured value (key: " + key + ")"); 
    }
    return configMap.get(key);
  }

  public String getConfiguredValue(String key, String defaultValue) {
    if(!configMap.containsKey(key)) {
      return defaultValue; 
    }
    return (String) configMap.get(key);
  }

  public void setConfiguredValue(String key, Object value) {
    configMap.put(key, value); 
  }
  
  public static Configuration getInstance() throws ConfigurationException  {
    try {
      InitialContext ctx = new InitialContext();
      Configuration config = (Configuration) ctx.lookup(jndiName);
      return config; 
    } catch (NamingException e) {
      throw new ConfigurationException("Unable to locate configuration in JNDI under name " + jndiName, e);
    }
  }

}
