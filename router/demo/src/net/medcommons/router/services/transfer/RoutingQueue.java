/*
 * Created on Aug 17, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.transfer;

import java.util.Date;

/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * 
 * Note no tracking number.
 */
public class RoutingQueue {


	public static String STATUS_NEW = "NEW";
	public static String STATUS_IN_PROGRESS = "RUN";
	public static String STATUS_DONE = "DONE";
	public static String STATUS_ERROR = "ERROR";
	
	public static String TYPE_ORDER = "ORDER";
	public static String TYPE_DATA = "DATA";
	public static String COMMAND_TRANSFER = "TRANSF";
	public static String COMMAND_IMPORT = "IMPORT";
	public static String COMMAND_CSTORE= "CSTORE";
	
	public static String PROTOCOL_HTTP = "http";
	
	public String id;
	/**
	 * GUID of order.
	 */
	private String orderGuid = null;
	
	/**
	 * GUID of gateway from which this transfer is occuring.
	 */
	private String originatorGatewayGuid = null;
	
	/**
	 * GUID of gateway to which this transfer is going.
	 */
	private String destinationGatewayGuid = null;
	
	/**
	 * Identity of object being transferred.
	 */
	private String mcGUID = null;
	
	/**
	 * Protocol specified for transfer.
	 */
	private String protocol;
	
	/**
	 * Type of item in queue. This is an enumerated type with the 
	 * static strings of name TYPE_* being the permitted types.
	 */
	private String itemType = null;
	
	private String commandType = null;
	
	private Date timeEntered = null;
	private Date timeStarted = null;
	private Date timeCompleted = null;
	
	/**
	 * Status of item in queue. This is an enumerated type with the 
	 * static strings of name STATUS_* being the permitted types.
	 */
	private String globalStatus = null;
	private	long bytesTotal = -1;
	private long bytesTranferred = -1;
	private int  restartCount = -1;
	private int nObjects = 0;
	/**
	 * 
	 */
	public RoutingQueue() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void setId(String id){
			this.id = id;
		}
		public String getId(){
			return(this.id);
		}
	public void setDestinationGatewayGuid(String destinationGatewayGuid){
		this.destinationGatewayGuid = destinationGatewayGuid;
	}
	public String getDestinationGatewayGuid(){
		return(this.destinationGatewayGuid);
	}
	public void setOriginatorGatewayGuid(String originatorGatewayGuid){
		this.originatorGatewayGuid = originatorGatewayGuid;
	}
	public String getOriginatorGatewayGuid(){
		return(this.originatorGatewayGuid);
	}
	public void setMcGuid(String mcGuid){
		this.mcGUID = mcGuid;
	}
	public String getMcGuid(){
		return(this.mcGUID);
	}
	public void setProtocol(String protocol){
		this.protocol = protocol;
	}
	public String getProtocol(){
		return(this.protocol);
	}
	public void setOrderGuid(String orderGuid){
		this.orderGuid = orderGuid;
	}
	public String getOrderGuid(){
		return(this.orderGuid);
	}
	
	public void setItemType(String itemType){
		this.itemType = itemType;
	}
	public String getItemType(){
		return(this.itemType);
	}

	public void setCommandType(String commandType){
		this.commandType = commandType;
	}
	public String getCommandType(){
		return(commandType);
	}
	public void setTimeEntered(Date timeEntered){
		this.timeEntered = timeEntered;
	}
	public Date getTimeEntered(){
		return(this.timeEntered);
	}
	public void setTimeStarted(Date timeStarted){
		this.timeStarted = timeStarted;
	}
	public Date getTimeStarted(){
		return(this.timeStarted);
	}
	
	public void setTimeCompleted(Date timeCompleted){
		this.timeCompleted = timeCompleted;
	}
	public Date getTimeCompleted(){
		return(this.timeCompleted);
	}
	public void setGlobalStatus(String globalStatus){
		this.globalStatus = globalStatus;
	}
	public String getGlobalStatus(){
		return(this.globalStatus);
	}
	public void setBytesTotal(long bytesTotal){
		this.bytesTotal = bytesTotal;
	}
	public long getBytesTotal(){
		return(this.bytesTotal);
	}
	public void setBytesTransferred(long bytesTransferred){
		this.bytesTranferred = bytesTransferred;
	}
	public long getBytesTransferred(){
		return(this.bytesTranferred);
	}
	public void setRestartCount(int restartCount){
		this.restartCount = restartCount;
	}
	public int getRestartCount(){
		return(restartCount);
	}
  public String toString(){
    StringBuffer buff = new StringBuffer("RoutingQueue[");
    buff.append("ID = ");
    buff.append(this.id);
    buff.append(", Destintation=");
    buff.append(this.destinationGatewayGuid);
    buff.append(", Origin=");
    buff.append(this.originatorGatewayGuid);
    buff.append(", order guid=");
    buff.append(this.orderGuid);
    buff.append(", data guid =");
    buff.append(this.mcGUID);
    buff.append("]");
    return(buff.toString());
  }
}
