/*
 * Created on Aug 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.transfer;

/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MCOrderStudyLink {

	public Long id;
	String orderGuid = null;
	String studyInstanceUID = null;
	/**
	 * 
	 */
	public MCOrderStudyLink(String orderGuid, String studyInstanceUID) {
		super();
		// TODO Auto-generated constructor stub
		this.orderGuid = orderGuid;
		this.studyInstanceUID = studyInstanceUID;
	}
	public MCOrderStudyLink(){
		super();
	}
	public void setOrderGuid(String orderGuid){
		this.orderGuid = orderGuid;
	}
	public String getOrderGuid(){
		return(this.orderGuid);
	}
	
	public void setStudyInstanceUID(String studyInstanceUID){
		this.studyInstanceUID = studyInstanceUID;
	}
	public String getStudyInstanceUID(){
		return(this.studyInstanceUID);
	}
	public void setId(Long id){
		this.id = id;
	}
	
	public Long getId(){
		return(this.id);
	}
}
