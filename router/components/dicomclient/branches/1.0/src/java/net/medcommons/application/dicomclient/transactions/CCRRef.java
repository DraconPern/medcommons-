package net.medcommons.application.dicomclient.transactions;

import java.util.Date;

import net.sourceforge.pbeans.annotations.PersistentClass;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Contains references to CCRs for orders or DICOM downloads.
 *
 * @author mesozoic
 */
@PersistentClass(table="ccr_ref", idField="id", autoIncrement=true)
public class CCRRef { 
    Long id;
	String fileLocation;
	String guid;
	String storageId; 
	Date timeEntered;
	Long pixDemographicDataId;
	Long contextStateId;

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
	    return ToStringBuilder.reflectionToString(this);
	}
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
