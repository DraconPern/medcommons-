/*
 * $Id$
 * Created on 12/14/2005
 */
package net.medcommons.cxp;

/**
 * This is a simple bean used to hold the values returned from a CXP call. 
 * 
 * It is serialized and accessed by the SOAP client.
 * @author sean
 *
 */
public class Response  {
    
    private String opcode = null;

    private String reason = null;
    
    private int status;
    
    private String response = null;
    
    private String trackingNumber= null;
    
    private String pin = null;
    
    private String uid = null;
    
    public void setReason(String reason){
    	this.reason=reason;
    }
    public String getReason() {
        return reason;
    }

    public void setResponse(String response){
    	this.response = response;
    }
    public String getResponse() {
        return response;
    }

    public void setStatus(int status){
    	this.status = status;
    }
    public int getStatus() {
        return status;
    }
    public void setOpcode(String opcode){
    	this.opcode = opcode;
    }
    public String getOpcode() {
        return opcode;
    }
    
    public void setUid(String uid){
    	this.uid = uid;
    }
    public String getUid(){
    	return(uid);
    }
    public void setTrackingNumber(String trackingNumber){
    	this.trackingNumber = trackingNumber;
    }
    public String getTrackingNumber(){
    	return(this.trackingNumber);
    }
    public void setPin(String pin){
    	this.pin = pin;
    }
    public String getPin(){
    	return(this.pin);
    }
}
