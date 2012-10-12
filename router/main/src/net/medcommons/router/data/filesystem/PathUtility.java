/*
 * $Id: PathUtility.java 234 2004-08-03 03:25:28Z mquigley $
 */

package net.medcommons.router.data.filesystem;

import java.util.StringTokenizer;

public class PathUtility {

  public static String removeLeadingDirs(String path, int count) {
    String outputPath = simplify(path);
    
    for(int i = 0; i < count; i++) {
      int firstSep = outputPath.indexOf('/');
      if(firstSep != -1) {
        outputPath = outputPath.substring(firstSep + 1, outputPath.length()); 
      }  
    }
    
    return outputPath;
  }

  public static String removeTrailingSlash(String path) {
    String outputPath = path;

    int index = outputPath.lastIndexOf('/');
    if(index == (outputPath.length() - 1) && index != -1) {
      outputPath = outputPath.substring(0, outputPath.length() - 1);
    }
    return outputPath;
  }
  
  public static String removeLeadingSlash(String path) {
    String inputPath = simplify(path);
    if(inputPath.substring(0, 1).equals("/")) {
      inputPath = inputPath.substring(1, inputPath.length()); 
    } 
    return inputPath;
  }
  
  public static String getPathComponentsFromBeginning(String path, int count) {
    String workPath = simplify(path);
    StringTokenizer workPathTokens = new StringTokenizer(workPath, "/");
    
    String outputPath = "";
    int i = 0;
    while(workPathTokens.hasMoreTokens() && i < count) {
      i++;
      outputPath += workPathTokens.nextToken() + "/";
    }
    outputPath = removeTrailingSlash(outputPath);
    
    return outputPath;
  }

  
  public static String changeFileExtension(String path, String newExtension) {
    String newPath = path;

    int extSep = path.lastIndexOf('.');
    if(extSep != -1) {
      newPath = path.substring(0, extSep);
      newPath += newExtension;
    } 
    
    return newPath;
  }
  
  public static String simplify(String path) {
		String newPath = path;
    while(newPath.indexOf("//") != -1) {
		 newPath = newPath.replaceAll("//", "/");
    }
    return newPath;
  }

  public static String getParentPath(String path) {
    int sepIdx = path.lastIndexOf("/");
    if(sepIdx == -1) {
      return ".";
    } else {
      return path.substring(0, sepIdx); 
    }
  }
  
  public static int getComponentCount(String path) {
    String bestPath = removeLeadingSlash(removeTrailingSlash(simplify(path)));
    String[] components = bestPath.split("/");
    return components.length;
  }

}
