package net.medcommons.cxp;

import java.io.Serializable;

/**
 * Response for PUT CXP messages
 * @author sean
 *
 */
public class PutResponse implements Serializable{

	private int status = -1;
	private String reason = null;
	private String guid = null;
	private String cxpVersion = null;
	private RegistryParameters []parameters = null;
	
	public void setStatus(int status){
		this.status = status;
	}
	public int getStatus(){
		return(this.status);
	}
	public void setReason(String reason){
		this.reason = reason;
	}
	public String getReason(){
		return(this.reason);
	}
	public void setGuid(String guid){
		this.guid = guid;
	}
	public String getGuid(){
		return(this.guid);
	}
	public void SetCxpVersion(String cxpVersion){
		this.cxpVersion = cxpVersion;
	}
	public String getCxpVersion(){
		return(this.cxpVersion);
	}
	public void setParameters(RegistryParameters []parameters){
		this.parameters = parameters;
	}
	public RegistryParameters [] getRegistryParameters(){
		return(this.parameters);
	}
	

}
