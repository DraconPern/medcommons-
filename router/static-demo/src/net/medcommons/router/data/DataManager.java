/*
 * $Id: DataManager.java 86 2004-05-07 18:53:10Z mquigley $
 */

package net.medcommons.router.data;

import java.io.File;

import net.medcommons.router.configuration.Configuration;

import org.apache.log4j.Logger;

public class DataManager {

  public static final String JNDI_NAME = "medcommons/DataManager";
  
  private static Logger log = Logger.getLogger(DataManager.class);
  
  public DataManager() throws Exception {
    init();
    log.info("Created new DataManager instance.");
  }
  
  public void init() throws Exception {
    String rootPath = (String) Configuration.getInstance().getConfiguredValue("DataRoot");
    File root = new File(rootPath);
    if(root.mkdirs()) {
      log.info("Created root at: " + rootPath); 
    }
    
    String imagePath = rootPath + "/images/";
    File images = new File(imagePath);
    if(images.mkdirs()) {
      log.info("Created images at: " + imagePath); 
    }
  }

}
