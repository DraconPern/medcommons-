package net.medcommons.application.dicomclient.transactions;

import java.util.Date;

/**
 * Contains references to CCRs for orders or DICOM downloads.
 *
 * @author mesozoic
 *
 */
public class CCRReference {

	private Long id; // Used by hibernate
	String fileLocation;
	String guid;
	String storageId;
	Date timeEntered;
	Long pixDemographicDataId;
	Long contextStateId;

	public void setId(Long id){
		this.id = id; 
	}
	public Long getId(){
		return(this.id);
	}
	public void setFileLocation(String fileLocation){
		this.fileLocation = fileLocation;
	}
	public String getFileLocation(){
		return(this.fileLocation);
	}
	public void setGuid(String guid){
		this.guid = guid;
	}
	public String getGuid(){
		return(this.guid);
	}
	public void setStorageId(String storageId){
		this.storageId = storageId;
	}
	public String getStorageId(){
		return(this.storageId);
	}
	public void setTimeEntered(Date timeEntered){
		this.timeEntered = timeEntered;
	}
	public Date getTimeEntered(){
		return(this.timeEntered);
	}
	public void setPixDemographicDataId(Long pixDemographicDataId){
		this.pixDemographicDataId = pixDemographicDataId;
	}
	public Long getPixDemographicDataId(){
		return(this.pixDemographicDataId);
	}
	public void setContextStateId(Long contextStateId){
	    this.contextStateId = contextStateId;
	}
	public Long getContextStateId(){
	    return(this.contextStateId);
	}
	public String toString(){
		StringBuffer buff = new StringBuffer("CCRReference[");
		buff.append("id="); buff.append(id); buff.append(",");
		buff.append("storageId="); buff.append(storageId); buff.append(",");
		buff.append("guid="); buff.append(guid); buff.append(",");
		buff.append("contextStateId="); buff.append(contextStateId); buff.append(",");
		buff.append("timeEntered="); buff.append(timeEntered); buff.append("]");
		return(buff.toString());
	}
}
