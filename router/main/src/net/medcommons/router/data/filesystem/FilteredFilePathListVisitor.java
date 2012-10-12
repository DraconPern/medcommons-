/*
 * $Id: FilteredFilePathListVisitor.java 201 2004-07-14 19:25:02Z mquigley $
 */

package net.medcommons.router.data.filesystem;

import java.io.File;
import java.util.ArrayList;

public class FilteredFilePathListVisitor implements DirectoryTreeVisitor {

	private String fileExtensionFilter;
  private ArrayList excludeList;
	private ArrayList pathList;

	public FilteredFilePathListVisitor(String fileExtensionFilter) {
	  this.fileExtensionFilter = fileExtensionFilter; 
	  excludeList = new ArrayList();
    pathList = new ArrayList();
	}
  
	public void visit(String path) {
	  File node = new File(path);
	  if(node.isFile() && node.canRead()) {
		  if(path.endsWith(fileExtensionFilter) && !isExcludedPath(path)) {
		    pathList.add(path);
		  }
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
      String excludeFilter = (String) excludeList.get(i);
      if(path.indexOf(excludeFilter) != -1) {
        excluded = true; 
      } 
    }
    
    return excluded;
  }

}
