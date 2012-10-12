/*
 *  Copyright 2005 MedCommons Inc.   All Rights Reserved.
 * 
 * Created on Jun 28, 2005
 *
 * 
 */
package net.medcommons.registry.data;

/**
 * @author sean
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Documents {
	private Long id;
	private String mcGuid = null;
	private String key = null;
	private String location = null;
	private String validityTimestamp = null;
	
	/**
	 * Returns the MedCommons GUID for the document. 
	 * @return
	 */
	public String getMcGuid(){
		return(this.mcGuid);
	}
	public void setMcGuid(String mcGuid){
		this.mcGuid = mcGuid;
	}
	public String getKey(){
		return(this.key);
	}
	public void setKey(String key){
		this.key=key;
	}
	public String getLocation(){
		return(this.location);
	}
	public void setLocation(String location){
		this.location = location;
	}
	public String getValidityTimestamp(){
		return(this.validityTimestamp);
	}
	public void setValidityTimestamp(String validityTimestamp){
		this.validityTimestamp=validityTimestamp;
	}
	/**
	   * @return Returns the id.
	   */
	  public Long getId() {
	    return id;
	  }
	  /**
	   * @param id The id to set.
	   */
	  public void setId(Long id) {
	    this.id = id;
	  }
}/*
<class name="net.medcommons.registry.data.Documents" table="REGISTRY_DOCUMENTS">
<property name="mcGuid" column="REGISTRY_DOCUMENTS_MCGUID"   />
<property name="key"="REGISTRY_DOCUMENTS_KEY"/>
<property name="location" column="REGISTRY_DOCUMENTS_LOCATION"/>
<property name="validityTimestamp" column="REGISTRY_DOCUMENTS_VALIDITY_TIMESTAMP"/>
</class>
*/