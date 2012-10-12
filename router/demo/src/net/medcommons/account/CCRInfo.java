package net.medcommons.account;

import java.io.Serializable;

/**
 * Parameter block for return of CCR information. At the moment it pretty much mirrors the CCR log table.
 * @author sean
 *
 */
public class CCRInfo implements Serializable{

	
	private String acctId = null;
	private String idp = null;
	private String guid = null;
	private String status = null;
	private String date = null;
	private String src = null;
	private String dest = null;
	private String subject = null;
	
	public String toString(){
		StringBuffer buff = new StringBuffer();
		buff.append("accId="); buff.append(acctId); buff.append(",");
		buff.append("idp="); buff.append(idp); buff.append(",");
		buff.append("guid="); buff.append(guid);
		return(buff.toString());
	}
	public void setAcctId(String acctId){
		this.acctId = acctId;
	}
	public String getAcctId(){
		return(acctId);
	}

	public void setIdp(String idp){
		this.idp = idp;
	}
	public String getIdp(){
		return(idp);
	}
	
	
	public void setGuid(String guid){
		this.guid = guid;
	}
	public String getGuid(){
		return(guid);
	}
	public void setStatus(String status){
		this.status = status;
	}
	public String getStatus(){
		return(status);
	}
	public void setDate(String date){
		this.date = date;
	}
	public String getDate(){
		return(date);
	}
	
	public void setSrc(String src){
		this.src = src;
	}
	public String getSrc(){
		return(src);
	}
	
	public void setDest(String dest){
		this.dest = dest;
	}
	public String getDest(){
		return(dest);
	}
	public void setSubject(String subject){
		this.subject = subject;
	}
	public String getSubject(){
		return(subject);
	}
	
	
}
