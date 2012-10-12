/*
 * $Id: FilePathListVisitor.java 201 2004-07-14 19:25:02Z mquigley $
 */

package net.medcommons.router.data.filesystem;

import java.io.File;
import java.util.ArrayList;

public class FilePathListVisitor implements DirectoryTreeVisitor {

  private ArrayList excludeList;  
  private ArrayList pathList;

  public FilePathListVisitor() {
    excludeList = new ArrayList();
    pathList = new ArrayList();
  }
  
  public void visit(String path) {
    File node = new File(path);
    if(node.isFile() && node.canRead() && !isExcludedPath(path)) {
      pathList.add(path);
    }
  }
  
  public ArrayList getFilePaths() {
    return pathList;
  }
  
  public void addExcludeFilter(String filter) {
    excludeList.add(filter);
  }

  private boolean isExcludedPath(String path) {
    boolean excluded = false;
    
    for(int i = 0; i < excludeList.size(); i++) {
      String filter = (String) excludeList.get(i);
      if(path.indexOf(filter) != -1) {
        excluded = true; 
      } 
    }
    
    return excluded; 
  }

}
