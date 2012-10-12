/*
 * $Id$
 * Created on 4/10/2005
 */
package net.medcommons.cxp;

import static net.medcommons.cxp.CXPConstants.*;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class CXPResponse implements Serializable {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CXPResponse.class);
        
    private String opcode = null;

    private String reason = null;
    
    private int status;
    
    private String response = null;
    
    private String trackingNumber= null;
    
    private String trackingInfo = null;
    
    private String pin = null;
    
    private String uid = null;
    
    private String medcommonsId = null;
    
    private CXPRegistryResponse registryResponse = null;
    
    private CCRDocument ccr = null;
    
    /**
     * Creates a CXPResponse.
     * 
     * @param reason
     * @param response
     * @param status
     */
    public CXPResponse(int status, String reason, String opcode)  {
        super();
        this.reason = reason;
        this.status = status;
        this.opcode = opcode;
        this.response = toXMLString();
    }

    /*
    
    private String createErrorResponse() {
        String response = null;
        Element rootNode = new Element(CXP);
        
        Element opcodeNode = new Element(CXP_OPCODE);
        opcodeNode.setText(opcode);
        rootNode.addContent(opcodeNode);
        
        Element statusNode = new Element(CXP_STATUS);
        statusNode.setText(Integer.toString(status));
        rootNode.addContent(statusNode);
        
        Element reasonNode = new Element(CXP_REASON);
        reasonNode.setText(reason);
        rootNode.addContent(reasonNode);
        
        try {
            StringWriter sw = new StringWriter();
            new XMLOutputter().output(rootNode, sw);
            response = sw.toString();
        }
        catch (Exception e) {
            log.error("Failure to write out error XML", e);
        }
        return (response);
    }
    */
    

    /**
     * @param status
     * @param reason
     * @param opcode
     * @param info
     * @param uid
     * @throws IOException 
     */
    public CXPResponse(int status, String reason, String opcode, String trackingNumber, String pin, String uid, CCRDocument ccr) {
        super();
        // TODO Auto-generated constructor stub
        this.status = status;
        this.reason = reason;
        this.opcode = opcode;
        this.trackingNumber = trackingNumber;
        this.pin = pin;
        trackingInfo = trackingNumber + pin;
        this.uid = uid;
        this.response = this.toXMLString();
        this.ccr = ccr;
    }
    /**
     * Geneartes CXPResponse with null UID. Typically used to construct an error response.
     * @param status
     * @param reason
     * @param opcode
     * @param info
     * @throws IOException
     */
    public CXPResponse(int status, String reason, String opcode, String trackingNumber, String pin, CCRDocument ccr)  {
        this(status, reason, opcode, trackingNumber, pin, null, ccr);
    }

    /**
     * Generates a XML representation of response.
     * 
     * @param code
     * @param trackingInfo
     * @return
     */
    private String toXMLString() 
    {
    	 String response = null;
    	try{
       
	        Element rootNode = new Element(CXP);
	        
	        Element opcodeNode = new Element(CXP_OPCODE);
	        opcodeNode.setText(opcode);
	        rootNode.addContent(opcodeNode);
	        
	        
	        Element statusNode = new Element(CXP_STATUS);
	        statusNode.setText(Integer.toString(status));
	        rootNode.addContent(statusNode);
	        
	        if (uid != null){
		        Element uidNode = new Element(CXP_UID);
		        uidNode.setText(uid);
		        rootNode.addContent(uidNode);
	        }
	        
	        Element reasonNode = new Element(CXP_REASON);
	        reasonNode.setText(reason);
	        rootNode.addContent(reasonNode);
	        
	        if (trackingInfo != null){
		        Element txidNode = new Element(CXP_TXID);
		        txidNode.setText(trackingInfo);
		        rootNode.addContent(txidNode);
	        }
	         StringWriter sw = new StringWriter();
	         new XMLOutputter().output(rootNode, sw);
	         response= sw.toString();
    	}
    	catch(IOException e){
    		log.error("Error constructing response", e);
    		response = "INTERNAL ERROR";
    	}
        return (response);
    }
    private String toCXPRestString() 
    {
    	 String response = null;
    	try{
       
	        Element rootNode = new Element(CXP);
	        
	        Element opcodeNode = new Element(CXP_OPCODE);
	        opcodeNode.setText(opcode);
	        rootNode.addContent(opcodeNode);
	        
	        
	        Element statusNode = new Element(CXP_STATUS);
	        statusNode.setText(Integer.toString(status));
	        rootNode.addContent(statusNode);
	        
	        if (uid != null){
		        Element uidNode = new Element(CXP_GUID);
		        uidNode.setText(uid);
		        rootNode.addContent(uidNode);
	        }
	        
	        Element reasonNode = new Element(CXP_REASON);
	        reasonNode.setText(reason);
	        rootNode.addContent(reasonNode);
	        
	        Element registrySecretNode = new Element(CXP_RegistrySecret);
	        registrySecretNode.setText(pin);
	        rootNode.addContent(registrySecretNode);
	        if (trackingInfo != null){
		        Element txidNode = new Element(CXP_ConfirmationCode);
		        txidNode.setText(trackingInfo.substring(0,12)); // Chop off the PIN on the end of the tracking number.
		        rootNode.addContent(txidNode);
	        }
	         StringWriter sw = new StringWriter();
	         new XMLOutputter().output(rootNode, sw);
	         response= sw.toString();
    	}
    	catch(IOException e){
    		log.error("Error constructing response", e);
    		response = "INTERNAL ERROR";
    	}
        return (response);
    }
    
    public String getReason() {
        return reason;
    }

    public String getResponse() {
        return response;
    }

    public int getStatus() {
        return status;
    }

    public String getOpcode() {
        return opcode;
    }
    public String getUid(){
    	return(uid);
    }
    public String getTrackingNumber(){
    	return(trackingNumber);
    }
    public String getPin(){
    	return(pin);
    }
    public void setMedcommonsId(String medcommonsId){
    	this.medcommonsId = medcommonsId;
    }
    public String getMedcommonsId(){
    	return(this.medcommonsId);
    }
    public String getCXPRestResponse(){
    	return(toCXPRestString());
    }

    public CCRDocument getCcr() {
        return ccr;
    }
}
