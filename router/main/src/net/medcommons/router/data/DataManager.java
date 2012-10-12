/*
 * $Id: DataManager.java 237 2004-08-06 04:23:15Z mquigley $
 */

package net.medcommons.router.data;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.medcommons.router.configuration.Configuration;
import net.medcommons.router.data.filesystem.DirectoryTree;
import net.medcommons.router.data.filesystem.FilePathListVisitor;
import net.medcommons.router.data.filesystem.PathUtility;

import org.apache.log4j.Logger;

public class DataManager {

  public static final String JNDI_NAME = "medcommons/DataManager";
  
  private String rootPath;
  
  private static Logger log = Logger.getLogger(DataManager.class);
  
  public DataManager() throws Exception {
    init();
    log.info("Created new DataManager instance.");
  }
  
  public void init() throws Exception {
    rootPath = (String) Configuration.getInstance().getConfiguredValue("DataRoot");
    File root = new File(rootPath);
    if(root.mkdirs()) {
      log.info("Created root at: " + rootPath); 
    }
  }
  
  public static DataManager getInstance() throws DataManagerException {
    try {
      InitialContext ctx = new InitialContext();
      DataManager mgr = (DataManager) ctx.lookup(JNDI_NAME);
      return mgr;
    } catch (NamingException e) {
      throw new DataManagerException("Unable to locate DataManager in JNDI under name: " + JNDI_NAME, e);
    }    
  }
  
  public String[] getFolderContents(String folderGuid) throws DataManagerException {
    try {
      String folderPath = PathUtility.simplify(rootPath + "/" + folderGuid);
      
      FilePathListVisitor pv = new FilePathListVisitor();
      DirectoryTree.recurse(folderPath, pv);
      
      ArrayList paths = pv.getFilePaths();
      String[] pathArr = new String[paths.size()];
      for(int i = 0; i < paths.size(); i++) {
        pathArr[i] = PathUtility.removeLeadingDirs((String) paths.get(i), PathUtility.getComponentCount(folderPath));
      }
      
      return pathArr;
    } catch(Exception e) {
      throw new DataManagerException("Unable to list contents of folder: (folderGuid: " + folderGuid + "): " + e.toString(), e);
    }
  }
  
  public File getFolderFile(String folderGuid, String folderPath) throws DataManagerException {
    String absolutePath = PathUtility.simplify(rootPath + "/" + folderGuid + "/" + folderPath);
  	  
  	File f = new File(absolutePath);
  	
  	if(!f.exists()) {
  	  throw new DataManagerException("No such (folderPath: " + folderPath + ") for (folderGuid: " + folderGuid + ")");
  	}
  	  
  	return f; 
  }
  
  public void  putFolderFile(String folderGuid, String folderPath, byte[] data) throws DataManagerException {
    try {
      // Absolute path to file.
	    String absolutePath = PathUtility.simplify(rootPath + "/" + folderGuid + "/" + folderPath);
	    
	    // Make parent directories.
	    String parentPath = PathUtility.getParentPath(absolutePath);
	    File folder = new File(parentPath);
	    folder.mkdirs();
	    
	    // Write.
	    File f = new File(absolutePath);
	    FileOutputStream fos = new FileOutputStream(f);
	    fos.write(data);
	    fos.close();

    } catch(Exception e) {
      throw new DataManagerException("Unable to put file in folder (folderGuid: " + folderGuid + ")(folderPath: " + folderPath + "):" + e.toString(), e);
    }
  }

}
