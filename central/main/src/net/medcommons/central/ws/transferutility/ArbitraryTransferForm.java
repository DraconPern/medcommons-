/*
 * $Id: ArbitraryTransferForm.java 86 2004-07-13 03:38:51Z mquigley $
 */

package net.medcommons.central.ws.transferutility;

import org.apache.struts.action.ActionForm;

public class ArbitraryTransferForm extends ActionForm {

  private String folderGuid;
  private String sourceDeviceGuid;
  private String destinationDeviceGuid;
  
  public String getDestinationDeviceGuid() {
    return destinationDeviceGuid;
  }
  public void setDestinationDeviceGuid(String destinationGuid) {
    this.destinationDeviceGuid = destinationGuid;
  }
  public String getFolderGuid() {
    return folderGuid;
  }
  public void setFolderGuid(String folderPath) {
    this.folderGuid = folderPath;
  }
  public String getSourceDeviceGuid() {
    return sourceDeviceGuid;
  }
  public void setSourceDeviceGuid(String sourceGuid) {
    this.sourceDeviceGuid = sourceGuid;
  }
}
