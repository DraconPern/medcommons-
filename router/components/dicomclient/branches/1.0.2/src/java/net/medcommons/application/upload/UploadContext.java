package net.medcommons.application.upload;

import net.medcommons.modules.cxp.CXPConstants;

public class UploadContext {
	protected String cxpEndpoint;
	protected String folder;
	protected String storageId;
	protected String authToken;
	protected String senderId;
	protected String paymentBypassToken;
	protected CXPConstants.MergeCCRValues mergeCCR= CXPConstants.MergeCCRValues.ALL;
	
	
	public UploadContext(String[] args){
		if (args.length != 7){
			
			throw new IllegalArgumentException("Wrong number of arguments; needs 7, not " + args.length);
			
		}
		 cxpEndpoint = args[0];
		 folder      = args[1];
		 storageId   = args[2];
		 authToken   = args[3];
		 senderId 		= args[4];
		 paymentBypassToken = args[5];
		 mergeCCR = CXPConstants.MergeCCRValues.valueOf(args[6]);
	}
	public UploadContext(String cxpEndpoint, String folder, String storageId, String authToken, String senderId, String paymentBypassToken,  CXPConstants.MergeCCRValues mergeCCR){
		this.cxpEndpoint = cxpEndpoint;
		this.folder = folder;
		this.storageId = storageId;
		this.authToken = authToken;
		this.senderId = senderId;
		this.paymentBypassToken = paymentBypassToken;
		this.mergeCCR = mergeCCR;
		
	}
	
	public String getCxpEndpoint(){
		return(this.cxpEndpoint);
	}
	public String getFolder(){
		return(this.folder);
	}
	public void setFolder(String folder){
		this.folder = folder;
	}
	public String getStorageId(){
		return(this.storageId);
	}
	public String getAuthToken(){
		return(this.authToken);
	}
	public String getSenderId(){
		return(this.senderId);
	}
	public String getPaymentBypassToken(){
		return(this.paymentBypassToken);
	}
	public  CXPConstants.MergeCCRValues getMergeCCR(){
	    return(this.mergeCCR);
	}
	public String toString(){
		StringBuffer buff = new StringBuffer("UploadContext[");
		buff.append("cxpEndpoint="); buff.append(this.cxpEndpoint);
		buff.append(",storageId="); buff.append(this.storageId);
		buff.append(",senderId="); buff.append(this.senderId);
		buff.append(",auth="); buff.append(this.authToken);
		buff.append(",folder="); buff.append(this.folder);
		buff.append(",paymentBypassToken="); buff.append(paymentBypassToken);
		buff.append(",mergeCCR="); buff.append(mergeCCR);
		buff.append("]");
		return(buff.toString());
	}
}
