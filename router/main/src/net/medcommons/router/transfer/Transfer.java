package net.medcommons.router.transfer;

public class Transfer {
  
  private String folderGuid;
  private String path;
  private byte[] data;
  
  public byte[] getData() {
    return data;
  }
  public void setData(byte[] data) {
    this.data = data;
  }

  public String getFolderGuid() {
    return folderGuid;
  }

  public void setFolderGuid(String folderGuid) {
    this.folderGuid = folderGuid;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
  
  public String toXml() {
    String xml = "";
    
    xml += "<transfer>\n";
    xml += "  <folder-guid>" + folderGuid + "</folder-guid>\n";
    xml += "  <path>" + path + "</path>\n";
    xml += "</transfer>\n\n";
    
    return xml;
  }
  
}
