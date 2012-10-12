/*
 * $Id: StorageException.java 55 2004-05-06 16:52:35Z mquigley $
 */

package net.medcommons.central.storage;

public class StorageException extends Exception {

  public StorageException() {
    super();
  }
  
  public StorageException(String msg) {
    super(msg);
  }
  
  public StorageException(String msg, Throwable t) {
    super(msg, t);
  }

}
