/*
 *  Copyright 2005 MedCommons Inc.   All Rights Reserved.
 * 
 * Created on Jun 28, 2005
 *
 * 
 */
package net.medcommons.registry.data;

/**
 * Data model link between a user's (e.g., patient's) identity
 * and the top-level documents that belong to the patient (the CCRs). These
 * are for the fixed content linkages only; patient desktop files
 * are modelled differently (where?)
 * <P>
 * Note that we don't have MIME/content-type at the top level - this
 * might make it easier for people to search through the system.
 * 
 * @author sean

 */
public class OwnerGuidLink {
	
	private Long id;
	/**
	 * The GUID of the document. The typical (and perhaps only?) case is that of 
	 * a GUID of a CCR. 
	 */
	private String guid = null;
	
	/**
	 * The owner of the document.
	 */
	private String mcid = null;
	
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
	
	public String getGuid(){
		return(this.guid);
	}
	public void setGuid(String guid){
		this.guid = guid;
	}
	
	public String getMcid(){
		return(this.mcid);
	}
	public void setMcid(String mcid){
		this.mcid = mcid;
	}
}
