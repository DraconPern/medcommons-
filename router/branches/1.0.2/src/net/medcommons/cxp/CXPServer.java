package net.medcommons.cxp;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import net.medcommons.Version;
import net.medcommons.cxp.CXPException;
import net.medcommons.cxp.CXPQueryResponse;
import net.medcommons.cxp.CXPRequest;
import net.medcommons.cxp.CXPResponse;
import net.medcommons.cxp.Response;
import net.medcommons.document.ccr.CCRConstants;

/*
 * $Id: CXPServer.java 913 2005-10-31 17:09:11Z sdoyle $
 */


/**
 * CXPServer is a SOAP end-point for the MedCommons CXP Service. 
 * 
 * 
 */
public class CXPServer {
    
	/**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CXPServer.class);
    /**
     * Returns the version of the server
     */
    public String getVersion() {
        return "MedCommons CXP Server Version " + Version.getVersionString() + " built on " + Version.getBuildTime();
    }
    
    /**
     * Performs a CXP transfer of the given CCR to the recipient specified in the CCR itself.
     * 
     * @param ccrXml - the CCR XML data.
     * @return - the CXP XML response
     * @throws CXPException 
     */
    public Response put(String ccrXml) throws CXPException {
    	log.info("put - CCR argument only");
    	String xmlData = null;
	 	String clientId = null;
	 	String pin = null;
	 	String storageId = null;
        CXPRequest request = new CXPRequest(storageId, ccrXml, xmlData, clientId, "CXP SOAP Notification", pin, CCRConstants.SCHEMA_VALIDATION_STRICT);
        CXPResponse response = request.execute();
        Response resp = new Response();
        resp.setReason(response.getReason());
        resp.setStatus(response.getStatus());
        resp.setResponse(response.getResponse());
        resp.setUid(response.getUid());
        resp.setTrackingNumber(response.getTrackingNumber());
        resp.setPin(response.getPin());
        resp.setOpcode(response.getOpcode());
        return(resp);
    }
    
    
    public Response put(String ccrXml, String pin) throws CXPException{
    		log.info("putResponse ccr, pin=" + pin);
    	 	String xmlData = null;
    	 	String clientId = null;
    	 	String storageId = null;
            CXPRequest request = new CXPRequest(storageId, ccrXml, xmlData, clientId, "CXP SOAP Notification", pin, CCRConstants.SCHEMA_VALIDATION_STRICT);
            CXPResponse response = request.execute();
           
            Response resp = new Response();
            resp.setReason(response.getReason());
            resp.setStatus(response.getStatus());
            resp.setResponse(response.getResponse());
            resp.setUid(response.getUid());
            resp.setTrackingNumber(response.getTrackingNumber());
            resp.setPin(response.getPin());
            resp.setOpcode(response.getOpcode());
            
            return(resp);
            
    }
   
    /**
     * Performs a CXP transfer of the given CCR to the recipient specified in the CCR itself.
     * 
     * @param ccrXml - the CCR XML data.
     * @return - the CXP XML response
     * @throws CXPException 
     */
    /**
    public String command(String ccrXml, String xmlData) throws CXPException {
        CXPRequest request = new CXPRequest(ccrXml, xmlData, null, null, null, CCRConstants.SCHEMA_VALIDATION_OFF);
        CXPResponse response = request.execute();
        return response.getResponse();
    }
    */
    
    /**
     * Returns a CCR given an appropriate XML block.
     * 
     * @param ccrXml - the CCR XML data.
     * @return - the CXP XML response
     * @throws CXPException 
     */
    public String get(String xmlData) throws CXPException {
    	String storageId = null;
        CXPRequest request = new CXPRequest(storageId, null, xmlData, null, null, null,CCRConstants.SCHEMA_VALIDATION_OFF);
        CXPResponse response = request.execute();
        // HACK: queries are rendered using the JSP:  fix this
        if(response instanceof CXPQueryResponse) {
            CXPQueryResponse queryResponse = (CXPQueryResponse)response;
            return queryResponse.getCcrData();
        }
        else
            throw new CXPException("Unexpected response type received");
    }    

    /**
     * Returns a CCR given a tracking number and PIN.
     * @param trackingNumber
     * @param pin
     * @return
     * @throws CXPException
     */
    public String get(String trackingNumber, String pin) throws CXPException {
    	log.info("Get:" + trackingNumber);
    	StringBuffer buff = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    	buff.append("<CXP><OperationCode>QUERY</OperationCode><QueryString>");
    	buff.append(trackingNumber + pin);
    	buff.append("</QueryString><CXPVersion>0.9.3</CXPVersion>");
    	buff.append("<InformationSystem><Name>MedCommons</Name><Type>CXP SOAP Client</Type>");
    	buff.append("<Version>0.9.24</Version></InformationSystem></CXP>");
    	String xmlData = buff.toString();
    	String storageId = null;
        CXPRequest request = new CXPRequest(storageId, null, xmlData, null, null, null,CCRConstants.SCHEMA_VALIDATION_OFF);
        CXPResponse response = request.execute();
        
        // HACK: queries are rendered using the JSP:  fix this
        if(response instanceof CXPQueryResponse) {
            CXPQueryResponse queryResponse = (CXPQueryResponse)response;
            return queryResponse.getCcrData();
        }
        else{
        	log.error("Unexpected response: " + response.getResponse() + ", " + response.getReason());
            throw new CXPException("Unexpected response type received:" + response.getClass().getCanonicalName());
        }
    }    
}
