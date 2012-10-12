package net.medcommons.cxp.http;

import static net.medcommons.cxp.CXPConstants.COMMAND_UNDEFINED;
import static net.medcommons.cxp.CXPConstants.CXP;
import static net.medcommons.cxp.CXPConstants.CXP_OPCODE;
import static net.medcommons.cxp.CXPConstants.CXP_REASON;
import static net.medcommons.cxp.CXPConstants.CXP_STATUS;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.cxp.CXPConstants;
import net.medcommons.cxp.CXPConstants2;
import net.medcommons.cxp.CXPException;
import net.medcommons.cxp.CXPQueryResponse;
import net.medcommons.cxp.CXPRequest;
import net.medcommons.cxp.CXPResponse;
import net.medcommons.cxp.CXPValidationException;
import net.medcommons.cxp.CXPVersionException;
import net.medcommons.cxp.GetResponse;
import net.medcommons.cxp.utils.IdHandling;
import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * 
 * Implements CXP protocol.
 * 
 * <p> Default behaviors </p>
 * <ul>
 * <li> If XML block not specified, default command is "TRANSFER".
 * <li> If unspecified by XML block, email notification to CCR <To> field is the default.
 * </ul>
 * <p> Assumptions </p>
 * <ul>
 * <li> MedCommons User ID is now set to be the tracking number with four zeros prepended on the front.
 * </ul>
 * TODO Error handling/logging. RepositoryErrors can be of different types - need to send more info back to calling program.
 * TODO Think about memory usage and streaming - can attached documents be read all at once?
 * TODO Test on attachments of file type needed.
 * TODO Consistency tests on input CCR needed (validation, &amp;etc.)
 * @author sean
 * 
 */
public class CxpRestServlet extends HttpServlet {

	private String schemaValidationPreference = null;

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CxpRestServlet.class);

	public void init() {
		schemaValidationPreference = Configuration.getProperty(
				"CCR_Schema_Validation", CCRConstants.SCHEMA_VALIDATION_STRICT);
		log.info("CCR_Schema_Validation is " + schemaValidationPreference);
		if (!(
			(CCRConstants.SCHEMA_VALIDATION_OFF.equals(schemaValidationPreference)) ||
			(CCRConstants.SCHEMA_VALIDATION_STRICT.equals(schemaValidationPreference)) ||
			(CCRConstants.SCHEMA_VALIDATION_STRICT.equals(schemaValidationPreference))
			))
			{
			throw new IllegalArgumentException("Schema validation");
		}
	}

	protected void doGet(HttpServletRequest request,
	HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			
			String ccrData = null;
			log.info("About to process parameters..");
			URLParameters urlParameters = new URLParameters();
		
			Enumeration e = request.getParameterNames();
			
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String[] value = request.getParameterValues(key);
				log.info("POST key = " + key);
				//log.info("value  is = " + value[0]);
				if ("ccrdata".equals(key)) {
					ccrData = URLDecoder.decode(value[0]);
				} 
				else if ("registryEnabled".equalsIgnoreCase(key)){
					urlParameters.RegistryEnabled = value[0];
					log.info(key + "=" + value[0]);
				} 
				else if ("PatientFamilyName".equalsIgnoreCase(key)){
					urlParameters.PatientFamilyName = value[0];
					
				} 
				else if ("PatientGivenName".equalsIgnoreCase(key)){
					urlParameters.PatientGivenName = value[0];
					
				} 
				else if ("PatientIdentifier".equalsIgnoreCase(key)){
					urlParameters.PatientIdentifier = value[0];
					
				} 
				else if ("PatientIdentifierSource".equalsIgnoreCase(key)){
					urlParameters.PatientIdentifierSource = value[0];
					
				} 
				else if ("SenderProviderId".equalsIgnoreCase(key)){
					urlParameters.SenderProviderId = value[0];
					
				} 
				else if ("ReceiverProviderId".equalsIgnoreCase(key)){
					urlParameters.ReceiverProviderId = value[0];
					
				} 
				else if ("DOB".equalsIgnoreCase(key)){
					urlParameters.DOB = value[0];
					
				} 
				else if ("Purpose".equalsIgnoreCase(key)){
					urlParameters.Purpose = value[0];
					
				} 
				else if ("RegistrySecret".equalsIgnoreCase(key)){
					urlParameters.RegistrySecret = IdHandling.normalizeId(value[0]);
					
				} 
				else if ("ConfirmationCode".equalsIgnoreCase(key)){
					urlParameters.ConfirmationCode = IdHandling.normalizeId(value[0]);
					
				} 
				else if ("CXPServerURL".equalsIgnoreCase(key)){
					urlParameters.CXPServerURL = value[0];
					
				} 
				
				else if ("CXPServerVendor".equalsIgnoreCase(key)){
					urlParameters.CXPServerVendor = value[0];
					
				} 
				else if ("Comment".equalsIgnoreCase(key)){
					urlParameters.Comment = value[0];
					
				} 
				else if ("SenderID".equalsIgnoreCase(key)){
					urlParameters.SenderID = value[0];
					
				} 
				else if ("Guid".equalsIgnoreCase(key)){
					urlParameters.Guid = value[0];
					
				} 
				else if ("Command".equalsIgnoreCase(key)){
					urlParameters.Command = value[0];
					
				} 
				else if ("ViewerURL".equalsIgnoreCase(key)){
					urlParameters.ViewerUrl = value[0];
					
				} 
				else if(CXPConstants2.COMMONS_ID.equalsIgnoreCase(key)){
					urlParameters.CommonsID = IdHandling.normalizeId(value[0]);
				}
				else {
					log.info(" Other (ignored) URL parameter:" + key);
				}
			}

			String xmlResponse = handleCXP(request, response, ccrData,
					urlParameters);
			//response.setContentType("application/xml;charset=UTF-8"); 
			if (xmlResponse != null) // handleCXP can return null if it forwards to jsp
				//response.getWriter().write(xmlResponse);
				response.getOutputStream().print(xmlResponse);
			
		} catch (Throwable t) {
			log.fatal("Error in POST method", t);
		}
	}

	private String handleCXP(HttpServletRequest request,
			HttpServletResponse response, String ccrData, 
			URLParameters urlParameters) {

		CXPRequest cxpRequest = null;
		try {
			//log.info("CCR data is " + ccrData);
			String storageId = null;
			cxpRequest = new CXPRequest(storageId, ccrData, 
					null, // XML data is null
					urlParameters.SenderID, 
					urlParameters.Comment, 
					urlParameters.RegistrySecret,
					schemaValidationPreference);
			
			if ((urlParameters.RegistryEnabled != null )&& (!"NONE".equals(urlParameters.RegistryEnabled)) && (!"".equals(urlParameters.RegistryEnabled)))
				cxpRequest.setRegistryEnabled(urlParameters.RegistryEnabled);
			if ((urlParameters.SenderProviderId != null ) && (!"".equals(urlParameters.SenderProviderId)))
				cxpRequest.setSenderProviderId(urlParameters.SenderProviderId);
			if ((urlParameters.ReceiverProviderId != null ) && (!"".equals(urlParameters.ReceiverProviderId)))
				cxpRequest.setReceiverProviderId(urlParameters.ReceiverProviderId);
			if ((urlParameters.Command != null ) && (!"".equals(urlParameters.Command)))
				cxpRequest.setCommand(urlParameters.Command);	
			if ((urlParameters.Guid != null ) && (!"".equals(urlParameters.Guid)))
				cxpRequest.setRequestGUID(urlParameters.Guid);
			if ((urlParameters.RegistrySecret != null ) && (!"".equals(urlParameters.RegistrySecret)))
				cxpRequest.setRegistrySecret(urlParameters.RegistrySecret);
			if ((urlParameters.ConfirmationCode != null ) && (!"".equals(urlParameters.ConfirmationCode)))
				cxpRequest.setConfirmationCode(urlParameters.ConfirmationCode);
			if ((urlParameters.PatientFamilyName != null ) && (!"".equals(urlParameters.PatientFamilyName)))
				cxpRequest.setPatientFamilyName(urlParameters.PatientFamilyName);
			if ((urlParameters.PatientGivenName != null ) && (!"".equals(urlParameters.PatientGivenName)))
				cxpRequest.setPatientGivenName(urlParameters.PatientGivenName);
			if ((urlParameters.PatientIdentifier != null ) && (!"".equals(urlParameters.PatientIdentifier)))
				cxpRequest.setPatientIdentifier(urlParameters.PatientIdentifier);
			if ((urlParameters.PatientIdentifierSource != null ) && (!"".equals(urlParameters.PatientIdentifierSource)))
				cxpRequest.setPatientIdentifierSource(urlParameters.PatientIdentifierSource);
			if ((urlParameters.DOB != null ) && (!"".equals(urlParameters.DOB)))
				cxpRequest.setDOB(urlParameters.DOB);
			if ((urlParameters.Purpose != null ) && (!"".equals(urlParameters.Purpose)))
				cxpRequest.setPurpose(urlParameters.Purpose);
			if ((urlParameters.CXPServerURL != null ) && (!"".equals(urlParameters.CXPServerURL)))
				cxpRequest.setCXPServerURL(urlParameters.CXPServerURL);
			if ((urlParameters.CXPServerVendor != null ) && (!"".equals(urlParameters.CXPServerVendor)))
				cxpRequest.setCXPServerVendor(urlParameters.CXPServerVendor);
			if ((urlParameters.ViewerUrl != null ) && (!"".equals(urlParameters.ViewerUrl)))
				cxpRequest.setViewerUrl(urlParameters.ViewerUrl);
			if((urlParameters.CommonsID != null) && (!"".equals(urlParameters.CommonsID)))
				cxpRequest.setMedcommonsId(urlParameters.CommonsID);
		
			/*
		\
		
		
		String Purpose = null;
		String CXPServerURL = null;
		String CXPServerVendor = null;
		String ViewerUrl = null;
		String Comment = null;
		String Guid = null;
		String RegistryEnabled = null;
		String SenderID = null;
		String Command = null;
			 */
			// TODO
			// Finish filling in the parameters.
			// Format the output 
			// Make sure that the API supports all the values.
			
			 cxpRequest.setMergeIncomingCCR(net.medcommons.modules.cxp.CXPConstants.MergeCCRValues.ALL);
			CXPResponse result = cxpRequest.execute();
			
			if (cxpRequest.getCommand().equalsIgnoreCase(CXPConstants.COMMAND_GET)){
				CXPQueryResponse qResult = (CXPQueryResponse) result;
				return(qResult.getCcrData());
			}
			else
				return(result.getCXPRestResponse());
		

			

		} catch (CXPVersionException e) {
			String command = (cxpRequest != null) ? cxpRequest.getCommand()
					: COMMAND_UNDEFINED;
			CXPResponse resp = new CXPResponse(
					CXPConstants.CXP_STATUS_BAD_CXP_VERSION,
					"CXP Version Mismatch:" + e.toString(), command, e
							.getMessage(), "",null);
			return (resp.getResponse());
		} catch (CXPValidationException e) {
			String command = (cxpRequest != null) ? cxpRequest.getCommand()
					: COMMAND_UNDEFINED;
			CXPResponse resp = new CXPResponse(
					CXPConstants.CXP_STATUS_BAD_REQUEST,
					"CCR Validation Failure:" + e.toString(), command, e
							.getMessage(), "",null);
			return (resp.getResponse());
		} catch (CXPException e) {
			String command = (cxpRequest != null) ? cxpRequest.getCommand()
					: COMMAND_UNDEFINED;
			log.error("Error executing CXP command " + command, e);
			CXPResponse resp = new CXPResponse(
					CXPConstants.CXP_STATUS_SERVER_ERROR,
					"Internal Server Error:" + e.toString(), command, e
							.getMessage(), "",null);
			return (resp.getResponse());
		} catch (NullPointerException e) { // error message from null pointer is "null", very uninformative
			String command = (cxpRequest != null) ? cxpRequest.getCommand()
					: COMMAND_UNDEFINED;
			log.error("Error executing CXP command " + command, e);
			CXPResponse resp = new CXPResponse(
					CXPConstants.CXP_STATUS_SERVER_ERROR,
					"Internal Server Error: NullPointerException", command,
					"Internal error (NullPointerException)", "",null);
			return (resp.getResponse());
		} catch (Exception e) {
			String command = (cxpRequest != null) ? cxpRequest.getCommand()
					: COMMAND_UNDEFINED;
			log.error("Error executing CXP command " + command, e);
			CXPResponse resp = new CXPResponse(
					CXPConstants.CXP_STATUS_SERVER_ERROR,
					"Internal Server Error:" + e.toString(), command, e
							.getMessage(), "",null);

			return (resp.getResponse());
		}

	}
	private class URLParameters{
		String PatientFamilyName = null;
		String PatientGivenName = null;
		String PatientIdentifier = null;
		String PatientIdentifierSource = null;
		String SenderProviderId = null;
		String ReceiverProviderId = null;
		String DOB = null;
		String ConfirmationCode = null;
		String RegistrySecret = null;
		String Purpose = null;
		String CXPServerURL = null;
		String CXPServerVendor = null;
		String ViewerUrl = null;
		String Comment = null;
		String Guid = null;
		String RegistryEnabled = null;
		String SenderID = null;
		String Command = null;
		String CommonsID = null;
		
		
	}
}


