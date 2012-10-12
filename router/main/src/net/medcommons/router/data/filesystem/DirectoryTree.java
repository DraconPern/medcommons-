/*
 * $Id: DirectoryTree.java 201 2004-07-14 19:25:02Z mquigley $
 */

package net.medcommons.router.data.filesystem;

import java.io.File;

public class DirectoryTree {
  
  public static void recurse(String path, DirectoryTreeVisitor visitor) throws FilesystemException {
    File thisRoot = new File(path);
    
    if(!thisRoot.isDirectory()) {
      throw new FilesystemException("Can only recurse directories!"); 
    }
    
    File[] contents = thisRoot.listFiles();
    
    for(int i = 0; i < contents.length; i++) {
      File f = contents[i];
      if(f.isDirectory() && f.canRead()) {
        String newPath = PathUtility.simplify(path + "/" + f.getName());
        recurse(newPath, visitor); 
        visitor.visit(newPath);
      } else {
        if(f.isFile() && f.canRead()) {
          String visitPath = PathUtility.simplify(path + "/" + f.getName());
          visitor.visit(visitPath);
        } 
      }
    }
  }

  public static void createParentPath(String path) throws Exception {
    String parentPath = PathUtility.getParentPath(path);
    File pf = new File(parentPath);
    if(!pf.exists()) {
      pf.mkdirs();
    }
  }
		
}
