/*
 * $Id: PackageService.java 67 2004-05-11 00:11:47Z mquigley $
 */

package net.medcommons.central.ws.pkg;

import net.medcommons.central.storage.Storage;
import net.medcommons.conversion.*;

public class PackageService {

  public String fetchPackage(String pkgGuid) throws PackageServiceException {
    try {
      Storage s = Storage.getInstance();
      String fileData = Base64Utility.encode(s.fetchFile(pkgGuid));
      return fileData;
      
    } catch(Exception e) {
      throw new PackageServiceException("Error in fetchPackage: " + e.toString(), e); 
    }
  }
  
  public void storePackageWithExistingGuid(String guid, String pkgData) throws PackageServiceException {
    try {
      Storage s = Storage.getInstance();
      s.storeFile(guid, Base64Utility.decode(pkgData));
      
    } catch(Exception e) {
      throw new PackageServiceException("Error in storePackage: " + e.toString(), e);
    } 
  }
  
  public String storePackage(String pkgData) throws PackageServiceException {
    try {
      Storage s = Storage.getInstance();
      String guid = s.storeFile(Base64Utility.decode(pkgData));
      return guid;
      
    } catch(Exception e) {
      throw new PackageServiceException("Error in storePackage: " + e.toString(), e); 
    }
  }

}
