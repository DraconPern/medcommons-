/*
 * $Id: DeleteAllVisitor.java 201 2004-07-14 19:25:02Z mquigley $
 */

package net.medcommons.router.data.filesystem;

import java.io.File;

import org.apache.log4j.Logger;

public class DeleteAllVisitor implements DirectoryTreeVisitor {

  private static Logger log = Logger.getLogger(DeleteAllVisitor.class);

  public void visit(String path) {
    File f = new File(path);
    if(f.delete()) {
      log.info("Deleted: " + path); 
    } else {
      log.info("**** NOT DELETED: " + path); 
    }
  }  

}
