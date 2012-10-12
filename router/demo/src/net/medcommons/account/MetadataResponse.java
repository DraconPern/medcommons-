package net.medcommons.account;

/**
 * Return parameter block for metadata messages.
 * @author sean
 *
 */
public class MetadataResponse {

	/**
	 * The status of the request. The numbers follow the
	 * HTTP codes: 200 is the status for success; any 2xx
	 * status code is success but perhaps with warnings.
	 */
	private int status = -1;
	
	/**
	 * The reason for the status. For a status of 200 the
	 * value will be "OK". The status should be meaningful
	 * to the user - it may be displayed by the client
	 * application.
	 */
	private String reason = null;
	
	/**
	 * The results of the message.
	 */
	private CCRInfo[] ccrResults = null;
	
	public void setReason(String reason){
		this.reason = reason;
	}
	public String getReason(){
		return(this.reason);
	}
	public void setStatus(int status){
		this.status = status;
	}
	public int getStatus(){
		return(this.status);
	}
	public void setCcrResults(CCRInfo[] ccrResults){
		this.ccrResults = ccrResults;
	}
	public CCRInfo[] getCcrResults(){
		return(this.ccrResults);
	}
}
