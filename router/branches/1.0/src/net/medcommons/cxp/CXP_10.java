package net.medcommons.cxp;

import net.medcommons.Version;
import net.medcommons.cxp.utils.IdHandling;
import net.medcommons.cxp.utils.ParameterHandling;
import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.ServicesFactory;

import org.apache.log4j.Logger;


/**
 * CXP_10 is a SOAP end-point for the MedCommons CXP Service, version 1.0.
 * 
 * Note: This service probably will be renamed in the future as CXPServer - but an older version of 
 * the protocol is running concurrently with this one.
 * 
 * 
 */
public class CXP_10 implements CXPConstants2 {

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CXP_10.class);

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
		PutResponse resp = null;
		String storageId = null;
		try{
		CXPRequest request = new CXPRequest(storageId, ccrXml, xmlData, clientId,
				"CXP SOAP Notification", pin,
				CCRConstants.SCHEMA_VALIDATION_STRICT);
		CXPResponse response = request.execute();
		resp = new PutResponse();
		resp.setReason(response.getReason());
		resp.setStatus(response.getStatus());
		resp.SetCxpVersion(CXP_VERSION);

		resp.setGuid(response.getUid());
		RegistryParameters rParameters = new RegistryParameters();
		rParameters.setRegistryName(MEDCOMMMONS_REGISTRY);
		rParameters.setRegistryId(MEDCOMMMONS_REGISTRY_ID);
		Parameter parameters[] = ParameterHandling.createParameterBlock(4);

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
		}
		catch (CXPException e){
			log.error("CXP exception:", e);
			throw e;
		}
		return (resp);
	}

	/**
	 * Places a document into the repository.
	 * @param ccrXml
	 * @param inputRegistryParameters
	 * @return
	 * @throws CXPException
	 */
	public PutResponse put(String ccrXml, RegistryParameters[] inputRegistryParameters)
			throws CXPException {
		try{
		log.info("PUT CCR length = " + ccrXml.length());
		String xmlData = null;
		boolean isEmergency = false;
		
		int nRegistries = inputRegistryParameters.length;
		
		Parameter[] inputParameters = inputRegistryParameters[0].getParameters();
		log.info("Registry parameters: " + inputRegistryParameters[0].getRegistryName());
		if (inputParameters == null){
			throw new CXPException("Null parameter array for registry " + inputRegistryParameters[0].getRegistryName());
			
		}
		String pin = IdHandling.normalizeId(ParameterHandling.getParameterValue(REGISTRY_SECRET, inputParameters, null)) ;
		if ("".equals(pin))
			pin = null;
		String notificationSubject = ParameterHandling.getParameterValue(NOTIFICATION_SUBJECT, inputParameters, "") ;
		String clientId = ParameterHandling.getParameterValue(SENDER_ID, inputParameters, null);
		
		String medCommonsId = IdHandling.normalizeId(ParameterHandling.getParameterValue(COMMONS_ID, inputParameters, null));
		
		// TODO: Remove after HIMSS? Or is this a good feature?
		String emergency = ParameterHandling.getParameterValue(EMERGENCY, inputParameters, null);
		if (emergency != null){
			if ("true".equalsIgnoreCase(emergency))
				isEmergency = true;
		}
		
		CXPRequest request = new CXPRequest(medCommonsId, ccrXml, xmlData, clientId,
				notificationSubject, pin,
				CCRConstants.SCHEMA_VALIDATION_STRICT);
		
		
		if (nRegistries > 1){
			Parameter[] regParameters = inputRegistryParameters[1].getParameters();
			if (regParameters == null)
				throw new CXPException("Null parameter array for " +
						inputRegistryParameters[1].getRegistryName());
			for (int i=0;i<regParameters.length;i++){
				Parameter p = regParameters[i];
				String name = p.getName();
				String value = p.getValue();
				log.info("Secondary Registry:" + inputRegistryParameters[1].getRegistryName() +
						": name=" + name + ", value=" + value);
				
				if (name.equalsIgnoreCase(SenderProviderId)){
					request.setSenderProviderId(value);
				}
				if (name.equalsIgnoreCase(ReceiverProviderId)){
					request.setReceiverProviderId(value);
				}
				if (name.equalsIgnoreCase(DOB)){
					request.setDOB(value);
				}
				if (name.equalsIgnoreCase(Purpose)){
					request.setPurpose(value);
				}
				if (name.equalsIgnoreCase(PatientIdentifier)){
					request.setPatientIdentifier(value);
				}
			}
		}
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
		Parameter parameters[] = ParameterHandling.createParameterBlock(4);

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
						" does not  input value " + medCommonsId);
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
	 * Places a document into the repository.
	 * @param ccrXml
	 * @param inputRegistryParameters
	 * @return
	 * @throws CXPException
	 */
	public PutResponse put(String ccrXml, RegistryParameters[] inputRegistryParameters, CXPAttachment[] attachments)
			throws CXPException {
		try{
		log.info("PUT CCR length = " + ccrXml.length());
		String xmlData = null;
		boolean isEmergency = false;
		
		
		Parameter[] inputParameters = inputRegistryParameters[0].getParameters();
		String pin = IdHandling.normalizeId(ParameterHandling.getParameterValue(REGISTRY_SECRET, inputParameters, null)) ;
		if ("".equals(pin))
			pin = null;
		String notificationSubject = ParameterHandling.getParameterValue(NOTIFICATION_SUBJECT, inputParameters, "") ;
		String clientId = ParameterHandling.getParameterValue(SENDER_ID, inputParameters, null);
		
		String medCommonsId = IdHandling.normalizeId(ParameterHandling.getParameterValue(COMMONS_ID, inputParameters, null));
		
		// TODO: Remove after HIMSS? Or is this a good feature?
		String emergency = ParameterHandling.getParameterValue(EMERGENCY, inputParameters, null);
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
		Parameter parameters[] = ParameterHandling.createParameterBlock(4);

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
		
		CXPRequest request = new CXPRequest(null, null, xmlData, null, null, null,
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

	public GetResponse get (RegistryParameters []inputRegistryParameters) throws CXPException{
		Parameter[] inputParameters = inputRegistryParameters[0].getParameters();
		String registrySecret = IdHandling.normalizeId(ParameterHandling.getParameterValue(REGISTRY_SECRET, inputParameters, null)) ;
		String confirmationCode = IdHandling.normalizeId(ParameterHandling.getParameterValue(CONFIRMATION_CODE, inputParameters, null)) ;
		GetResponse gResponse = null;
		log.info("Get:" + confirmationCode + ", " + registrySecret);
		try{
		 gResponse = new GetResponse();
			
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
		/**
		 * TODO: Big kludge. Don't put parameters into XML just to parse them out again. Need to refactor CXPRequest object. 
		 */
		StringBuffer buff = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buff.append("<CXP><OperationCode>QUERY</OperationCode><QueryString>");
		buff.append(confirmationCode + registrySecret);
		buff.append("</QueryString><CXPVersion>0.9.3</CXPVersion>");
		buff
				.append("<InformationSystem><Name>MedCommons</Name><Type>CXP SOAP Client</Type>");
		buff.append("<Version>0.9.24</Version></InformationSystem></CXP>");
		String xmlData = buff.toString();
		CXPRequest request = new CXPRequest(null, null, xmlData, null, null, null,
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
		catch(Exception e){
			log.error("Exception handling CXP GET request:", e);
			gResponse.setStatus(CXPConstants.CXP_STATUS_SERVER_ERROR);
			gResponse.setReason(e.getMessage());
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
	public DeleteResponse delete(RegistryParameters []parameters){
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



private boolean statusOK(int status){
	if ((status <200) || (status > 299))
		return(false);
	else
		return(true);
}

}
