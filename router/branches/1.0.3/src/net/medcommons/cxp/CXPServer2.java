package net.medcommons.cxp;

import net.medcommons.Version;
import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.ServicesFactory;

import org.apache.log4j.Logger;


/**
 * CXPServer2 is a SOAP end-point for the MedCommons CXP Service. 
 * 
 * Note: This service probably will be renamed in the future as CXPServer - but an older version of 
 * the protocol is running concurrently with this one.
 * Note: This is now obsolete as well; it's the version of CXP running at HIMSS. If HealthFrame
 * is updated to use CXP_10 then this service should be deleted.
 * 
 * 
 */
public class CXPServer2 implements CXPConstants2 {

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CXPServer2.class);

	/**
	 * Returns the version of the server
	 */
	public String getVersion() {
		return "MedCommons CXP Server2  Version " + Version.getVersionString()
				+ " built on " + Version.getBuildTime() + ", CXP Version:"
				+ CXP_VERSION;
	}

	/**
	 * Performs a CXP transfer of the given CCR to the recipient specified in the CCR itself.
	 * 
	 * @param ccrXml - the CCR XML data.
	 * @return - the CXP XML response
	 * @throws CXPException 
	 */
	public PutResponse put(String ccrXml) throws CXPException {
		log.info("put - CCR argument only");
		String xmlData = null;
		String clientId = null;
		String pin = null;
		String storageId = null;
		CXPRequest request = new CXPRequest(storageId, ccrXml, xmlData, clientId,
				"CXP SOAP Notification", pin,
				CCRConstants.SCHEMA_VALIDATION_STRICT);
		CXPResponse response = request.execute();
		PutResponse resp = new PutResponse();
		resp.setReason(response.getReason());
		resp.setStatus(response.getStatus());
		resp.SetCxpVersion(CXP_VERSION);

		resp.setGuid(response.getUid());
		RegistryParameters rParameters = new RegistryParameters();
		rParameters.setRegistryName(MEDCOMMMONS_REGISTRY);
		rParameters.setRegistryId(MEDCOMMMONS_REGISTRY_ID);
		Parameter parameters[] = createParameterBlock(4);

		parameters[0].setName(REGISTRY_SECRET);
		parameters[0].setValue(response.getPin());

		parameters[1].setName(CONFIRMATION_CODE);
		parameters[1].setValue(response.getTrackingNumber());

		parameters[2].setName(COMMONS_ID);
		parameters[2].setValue(response.getMedcommonsId());

		parameters[3].setName(VERSION);
		parameters[3].setValue(Version.getVersionString());

		rParameters.setParameters(parameters);
		RegistryParameters []allParameters = new RegistryParameters[1];
		allParameters[0] = rParameters;
		resp.setParameters(allParameters);
		return (resp);
	}

	/**
	 * Places a document into the repository.
	 * @param ccrXml
	 * @param inputRegistryParameters
	 * @return
	 * @throws CXPException
	 */
	public PutResponse put(String ccrXml, RegistryParameters inputRegistryParameters)
			throws CXPException {
		try{
		log.info("PUT CCR length = " + ccrXml.length());
		String xmlData = null;
		boolean isEmergency = false;
		
		
		Parameter[] inputParameters = inputRegistryParameters.getParameters();
		String pin = getParameterValue(REGISTRY_SECRET, inputParameters, null) ;
		if ("".equals(pin))
			pin = null;
		String notificationSubject = getParameterValue(NOTIFICATION_SUBJECT, inputParameters, "") ;
		String clientId = getParameterValue(SENDER_ID, inputParameters, null);
		
		String medCommonsId = getParameterValue(COMMONS_ID, inputParameters, null);
		String sponsorAccountId = getParameterValue(SPONSOR_ACCOUNT_ID, inputParameters, null);
		
		
		// TODO: Remove after HIMSS? Or is this a good feature?
		String emergency = getParameterValue(EMERGENCY, inputParameters, null);
		if (emergency != null){
			if ("true".equalsIgnoreCase(emergency))
				isEmergency = true;
		}
		
		CXPRequest request = new CXPRequest(medCommonsId, ccrXml, xmlData, clientId,
				notificationSubject, pin,
				CCRConstants.SCHEMA_VALIDATION_STRICT);
		
		CXPResponse response = request.execute();

		PutResponse resp = new PutResponse();
		resp.setReason(response.getReason());
		resp.setStatus(response.getStatus());
		resp.setGuid(response.getUid());
		
		//resp.setTrackingNumber(response.getTrackingNumber());
		//resp.setPin(response.getPin());
		RegistryParameters rParameters = new RegistryParameters();
		rParameters.setRegistryName(MEDCOMMMONS_REGISTRY);
		rParameters.setRegistryId(MEDCOMMMONS_REGISTRY_ID);
		Parameter parameters[] = createParameterBlock(4);

		parameters[0].setName(REGISTRY_SECRET);
		parameters[0].setValue(response.getPin());

		parameters[1].setName(CONFIRMATION_CODE);
		parameters[1].setValue(response.getTrackingNumber());

		parameters[2].setName(COMMONS_ID);
		parameters[2].setValue(response.getMedcommonsId());
		
		
		parameters[3].setName(VERSION);
		parameters[3].setValue(Version.getVersionString());

		rParameters.setParameters(parameters);
		RegistryParameters []allParameters = new RegistryParameters[1];
		allParameters[0] = rParameters;
		resp.setParameters(allParameters);
		
		log.info("About to respond - status is " + resp.getStatus());
		for (int i=0;i<parameters.length;i++){
			log.info(parameters[i].getName() + " = " + parameters[i].getValue());
		}
		// If the MedCommons ID was specified on the way in - then 
		// test to makes sure that these are the same on exit.
		if ((medCommonsId != null) && (!"".equals(medCommonsId))){
			if (!medCommonsId.equals(response.getMedcommonsId())){
				// TODO Not sure what could cause this except for a bug.
				// Need a spec here. 
				log.error("CXP Response MedCommonsID " + response.getMedcommonsId() + 
						" does not match input value " + medCommonsId);
			}
				
		}
		if (statusOK(response.getStatus())){
			if (isEmergency){
				String patientEmail = request.getPatientEmail();
				log.info("Patient email in CCR is " + patientEmail);
				/*
				if ((null == patientEmail) || ("".equals(patientEmail))){
					resp.setStatus(CXPConstants.CXP_STATUS_BAD_REQUEST);
					resp.setReason("Patient Email address must be specified for Emergency CCR");
					resp.setGuid(null);
					resp.setParameters(null);
					log.error("Patient Email address must be specified for Emergency CCR");
					return(resp);
				}
				*/
				ServicesFactory factory = new RESTProxyServicesFactory(clientId); // for now, null client id.  Maybe one day we will track it using a cookie.

                // ssadedin: extract emergency info to pass up to acct service
                CCRDocument ccr = response.getCcr();
                String einfo = "";
                if(ccr != null) {
                  einfo = ccr.getEmergencyInfo().toString();
                }
				factory.getAccountService().setEmergencyCCR(response.getMedcommonsId(), response.getUid(), einfo);
				
				
				
			}
		}
		return (resp);
		}
		catch(Throwable t){
			log.error("PUT", t);
			throw new CXPException(t.getMessage());
		}

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
	public GetResponse get(String xmlData) throws CXPException {
		String storageId = null;
		CXPRequest request = new CXPRequest(storageId, null, xmlData, null, null, null,
				CCRConstants.SCHEMA_VALIDATION_OFF);
		CXPResponse response = request.execute();
		// HACK: queries are rendered using the JSP:  fix this
		if (response instanceof CXPQueryResponse) {
			CXPQueryResponse queryResponse = (CXPQueryResponse) response;
			GetResponse resp = new GetResponse();
			resp.setContent(queryResponse.getCcrData());
			resp.setStatus(CXPConstants.CXP_STATUS_SUCCESS);
			resp.setReason("OK");
			resp.setContentType("CCR");
			resp.setGuid(queryResponse.getUid());
			
			return resp;
		} else
			throw new CXPException("Unexpected response type received");
	}

	public GetResponse get (RegistryParameters inputRegistryParameters) throws CXPException{
		Parameter[] inputParameters = inputRegistryParameters.getParameters();
		String registrySecret = getParameterValue(REGISTRY_SECRET, inputParameters, null) ;
		String confirmationCode = getParameterValue(CONFIRMATION_CODE, inputParameters, null) ;
		String storageId = null;
		log.info("Get:" + confirmationCode + ", " + registrySecret);
		GetResponse gResponse = new GetResponse();
			
		if (confirmationCode == null){
			gResponse.setStatus(CXPConstants.CXP_STATUS_BAD_REQUEST);
			gResponse.setReason("ConfirmationCode must not be null");
			return(gResponse);
		}
		if (confirmationCode.length() != 12){
			gResponse.setStatus(CXPConstants.CXP_STATUS_BAD_REQUEST);
			gResponse.setReason("ConfirmationCode must be 12 characters long in this registry, not " + confirmationCode.length());
			return(gResponse);
		}
		if (registrySecret==null ){
			registrySecret = ""; // Not sure if this is legal. Are empty registrySecrets valid?
		}
		StringBuffer buff = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buff.append("<CXP><OperationCode>QUERY</OperationCode><QueryString>");
		buff.append(confirmationCode + registrySecret);
		buff.append("</QueryString><CXPVersion>0.9.3</CXPVersion>");
		buff
				.append("<InformationSystem><Name>MedCommons</Name><Type>CXP SOAP Client</Type>");
		buff.append("<Version>0.9.24</Version></InformationSystem></CXP>");
		String xmlData = buff.toString();
		CXPRequest request = new CXPRequest(storageId, null, xmlData, null, null, null,
				CCRConstants.SCHEMA_VALIDATION_OFF);
		CXPResponse response = request.execute();

		// HACK: queries are rendered using the JSP:  fix this
		if (response instanceof CXPQueryResponse) {
			CXPQueryResponse queryResponse = (CXPQueryResponse) response;
			gResponse.setStatus(CXPConstants.CXP_STATUS_SUCCESS);
			gResponse.setReason("OK");
			gResponse.setContent(queryResponse.getCcrData());
			return gResponse;
		} else {
			gResponse.setStatus(CXPConstants.CXP_STATUS_SERVER_ERROR);
			gResponse.setReason("Unexpected response:" + response.getReason());
			log.error("Unexpected response: " + response.getResponse() + ", "
					+ response.getReason());
			return(gResponse);
		}
	}
	/**
	 * Returns a CCR given a tracking number and PIN.
	 * @param trackingNumber
	 * @param pin
	 * @return
	 * @throws CXPException
	 */
	/*
	public GetResponse get(String confirmationCode, String registrySecret) throws CXPException {
		log.info("Get:" + confirmationCode + ", " + registrySecret);
		GetResponse gResponse = new GetResponse();
			
		if (confirmationCode == null){
			gResponse.setStatus(CXPConstants.CXP_STATUS_BAD_REQUEST);
			gResponse.setReason("ConfirmationCode must not be null");
			return(gResponse);
		}
		if (confirmationCode.length() != 12){
			gResponse.setStatus(CXPConstants.CXP_STATUS_BAD_REQUEST);
			gResponse.setReason("ConfirmationCode must be 12 characters long in this registry, not " + confirmationCode.length());
			return(gResponse);
		}
		if (registrySecret==null ){
			registrySecret = ""; // Not sure if this is legal. Are empty registrySecrets valid?
		}
		StringBuffer buff = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buff.append("<CXP><OperationCode>QUERY</OperationCode><QueryString>");
		buff.append(confirmationCode + registrySecret);
		buff.append("</QueryString><CXPVersion>0.9.3</CXPVersion>");
		buff
				.append("<InformationSystem><Name>MedCommons</Name><Type>CXP SOAP Client</Type>");
		buff.append("<Version>0.9.24</Version></InformationSystem></CXP>");
		String xmlData = buff.toString();
		CXPRequest request = new CXPRequest(null, xmlData, null, null, null,
				CCRConstants.SCHEMA_VALIDATION_OFF);
		CXPResponse response = request.execute();

		// HACK: queries are rendered using the JSP:  fix this
		if (response instanceof CXPQueryResponse) {
			CXPQueryResponse queryResponse = (CXPQueryResponse) response;
			gResponse.setStatus(CXPConstants.CXP_STATUS_SUCCESS);
			gResponse.setReason("OK");
			gResponse.setContent(queryResponse.getCcrData());
			return gResponse;
		} else {
			gResponse.setStatus(CXPConstants.CXP_STATUS_SERVER_ERROR);
			gResponse.setReason("Unexpected response:" + response.getReason());
			log.error("Unexpected response: " + response.getResponse() + ", "
					+ response.getReason());
			return(gResponse);
		}
	}
	*/
	public DeleteResponse delete(String guid){
		DeleteResponse response = new DeleteResponse();
		response.setStatus(CXPConstants.CXP_STATUS_SERVER_ERROR);
		response.setReason("Not implemented");
		return(response);
	}
	public DeleteResponse delete(RegistryParameters parameters){
		DeleteResponse response = new DeleteResponse();
		response.setStatus(CXPConstants.CXP_STATUS_SERVER_ERROR);
		response.setReason("Not implemented");
		return(response);
	}
	public DeleteResponse delete(String guid, RegistryParameters parameters){
		DeleteResponse response = new DeleteResponse();
		response.setStatus(CXPConstants.CXP_STATUS_SERVER_ERROR);
		response.setReason("Not implemented");
		return(response);
	}

private Parameter[] createParameterBlock(int nElements){
    	Parameter params[] = new Parameter[nElements];
    	for (int i=0;i<nElements;i++){
    		params[i] = new Parameter();
    	}
    	
    	return(params);
    }

/**
 * Returns the value of the specified parameter from the parameter array 
 * or the default value if there is no match.
 * 
 * @param name
 * @param parameters
 * @return
 */
private String getParameterValue(String name, Parameter[] parameters, String defaultValue){
	String value = null;
	for (int i=0;i<parameters.length;i++){
		String pName = parameters[i].getName();
		if (pName.equalsIgnoreCase(name)){
			value = parameters[i].getValue();
			log.info("getParameterValue: name=" + name + ", value=" + value);
			return(value);
		}
	}
	
	if (value == null) 
		value = defaultValue;
	log.info("getParameterValue: name=" + name + ", value=" + value);
	return(value);
	
}

private boolean statusOK(int status){
	if ((status <200) || (status > 299))
		return(false);
	else
		return(true);
}

}
