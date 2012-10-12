/*
 * $Id: Storage.java 67 2004-05-11 00:11:47Z mquigley $
 */

package net.medcommons.central.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.naming.InitialContext;

import net.medcommons.central.guid.GuidFactory;

import org.apache.log4j.Logger;

public class Storage {

  public static String JNDI_NAME = "medcommons/Storage";

  private String rootPath;  
  private static Logger log = Logger.getLogger(Storage.class);
  
  public Storage(String rootPath) throws Exception {
    this.rootPath = rootPath;
    init();
    
    log.info("Created new Storage instance.");
  }
  
  public String storeFile(byte[] data) throws StorageException {
    String fileGuid = null;
    try {
      fileGuid = GuidFactory.getInstance().allocateGuid();
    } catch(Exception e) {
      throw new StorageException("Unable to allocate guid: " + e.toString(), e); 
    }
    
    storeFile(fileGuid, data);
    return fileGuid;
  }
      
      
  public void storeFile(String guid, byte[] data) throws StorageException {
    try {
      File f = new File("../server/central/data/" + guid);
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(data);
      fos.close();
      
    } catch(Exception e) {
      throw new StorageException("Error storing file: " + e.toString(), e); 
    } 
  }
  
  public byte[] fetchFile(String guid) throws StorageException {
    try {
      File f = new File("../server/central/data/" + guid);
      FileInputStream fis = new FileInputStream(f);
     
      int bytesRead = 0;
      byte[] buffer = new byte[10240];
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      while((bytesRead = fis.read(buffer, 0, 10240)) != -1) {
        outputStream.write(buffer, 0, bytesRead); 
      }
      
      fis.close();
      
      return outputStream.toByteArray();
      
    } catch(Exception e) {
      throw new StorageException("Error fetching file: " + e.toString(), e);
    }
  }
  
  public static Storage getInstance() throws Exception {
    InitialContext ctx = new InitialContext();
    return (Storage) ctx.lookup(JNDI_NAME);
  }
  
  private void init() throws Exception {
    File rootDir = new File(rootPath);
    if(rootDir.mkdirs()) {
      log.info("Created storage root: " + rootPath); 
    }
  }

}
