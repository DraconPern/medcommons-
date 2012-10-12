/*
 * $Id: StorageServiceMBean.java 55 2004-05-06 16:52:35Z mquigley $
 */

package net.medcommons.central.storage;

public interface StorageServiceMBean {
  
  public void start() throws Exception;
  public void stop();
  
  public void setStoragePath(String storagePath);
  public String getStoragePath();

}
