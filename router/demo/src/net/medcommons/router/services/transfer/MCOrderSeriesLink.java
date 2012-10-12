/*
 * Created on Aug 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.transfer;

/**
 * Provides mapping of Order to series.
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MCOrderSeriesLink {

	public Long id;
	String orderGuid = null;
	String mcGuid = null;
  
	/**
	 * Creates a new MCOrderSeriesLink to link the given order and series together.
	 */
  public MCOrderSeriesLink(String orderGuid, String mcGuid){
    this.orderGuid = orderGuid;
    this.mcGuid = mcGuid;
  }
	
  
	public MCOrderSeriesLink(){
		super();
	}

  public void setOrderGuid(String orderGuid){
		this.orderGuid = orderGuid;
	}
	public String getOrderGuid(){
		return(this.orderGuid);
	}
	
	public void setMcGuid(String mcGuid){
		this.mcGuid = mcGuid;
	}
	public String getMcGuid(){
		return(this.mcGuid);
	}
	public void setId(Long id){
		this.id = id;
	}
	
	public Long getId(){
		return(this.id);
	}
}
