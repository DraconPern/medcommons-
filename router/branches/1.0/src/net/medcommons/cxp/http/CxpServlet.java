package net.medcommons.cxp.http;

import static net.medcommons.cxp.CXPConstants.COMMAND_UNDEFINED;
import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.cxp.CXPConstants;
import net.medcommons.cxp.CXPException;
import net.medcommons.cxp.CXPQueryResponse;
import net.medcommons.cxp.CXPRequest;
import net.medcommons.cxp.CXPResponse;
import net.medcommons.cxp.CXPValidationException;
import net.medcommons.cxp.CXPVersionException;
import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;

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
public class CxpServlet extends HttpServlet {

	private String schemaValidationPreference = null;

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CxpServlet.class);

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
			throw new IllegalArgumentException("Schema validiation");
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			
			String ccrData = null;
			String xmldatablock = null;
			String registryEnabled = null;
			String senderProviderId = null;
			String receiverProviderId = null;
			String commonsId = null;
			
			Enumeration e = request.getParameterNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String[] value = request.getParameterValues(key);
				//log.info("POST key = " + key);
				//log.info("value size is = " + value[0]);
				if ("ccrdata".equals(key)) {
					ccrData = value[0];
				} else if ("xmldata".equals(key)) {
					xmldatablock = value[0];
					//log.info("xmldatablock = " + xmldatablock);
				}
				else if ("registryEnabled".equalsIgnoreCase(key)){
					registryEnabled = value[0];
					log.info("RegistryEnabled=" + registryEnabled);
				} 
				else if ("SenderProviderId".equalsIgnoreCase(key)){
					senderProviderId = value[0];
					log.info("SenderProviderId=" + senderProviderId);
				} 
				else if ("ReceiverProviderId".equalsIgnoreCase(key)){
					receiverProviderId = value[0];
					log.info("receiverProviderId=" + receiverProviderId);
				}
				else if ("CommonsId".equalsIgnoreCase(key)){
					commonsId = value[0];
					log.info("commonsId=" + commonsId);
				}
				else {
					log.info(" Other (ignored) URL parameter:" + key);
				}
			}
			log.info("Incoming CXP");
			String xmlResponse = handleCXP(request, response, ccrData,
					xmldatablock,senderProviderId, receiverProviderId, commonsId, registryEnabled);
			
			if (xmlResponse != null) // handleCXP can return null if it forwards to jsp
				response.getOutputStream().print(xmlResponse);
			
		} catch (Throwable t) {
			log.fatal("Error in POST method", t);
		}
	}

	private String handleCXP(HttpServletRequest request,
			HttpServletResponse response, String ccrData, String xmlData,
			String senderProviderId, String receiverProviderId, String commonsId, String registryEnabled) {

		CXPRequest cxpRequest = null;
		try {
		    
			// If client id is passed, use it
			String auth = Str.nvl(request.getParameter("clientId"), request.getParameter("auth"));
			
			// Hack:  if no clientId is provided, handle the transaction under gateway's authority
			// Needs to change, must convert to use real auth token for all gateway instances
			// probably based on client key
			if(blank(auth))
			    throw new IllegalArgumentException("Must provide valid authentication token");
			
			String storageId = null;
			cxpRequest = new CXPRequest(storageId, ccrData, xmlData, auth,
			                request.getParameter("subject"), request.getParameter("pin"),
			                schemaValidationPreference);
			
			if(!blank(registryEnabled) && !"NONE".equals(registryEnabled))
				cxpRequest.setRegistryEnabled(registryEnabled);
			
			if(!blank(senderProviderId) && !"NONE".equals(senderProviderId))
				cxpRequest.setSenderProviderId(senderProviderId);
			
			if(!blank(receiverProviderId) && !"NONE".equals(receiverProviderId))
				cxpRequest.setReceiverProviderId(receiverProviderId);
			
			if(!blank(commonsId) && !"NONE".equals(commonsId)) 
				cxpRequest.setMedcommonsId(commonsId);
			
			CXPResponse result = cxpRequest.execute();

			// HACK: queries are rendered using the JSP:  fix this
			if (result instanceof CXPQueryResponse) {
				CXPQueryResponse queryResponse = (CXPQueryResponse) result;
				String encodedCcr = URLEncoder.encode(
						queryResponse.getCcrData(), "UTF-8").replaceAll("\\+",
						"%20");
				request.setAttribute("encodedCcr", encodedCcr);
				request.setAttribute("queryString", queryResponse
						.getQueryString());
				request.setAttribute("uid", queryResponse.getUid());
				request.setAttribute("status", CXPConstants.CXP_STATUS_SUCCESS);
				RequestDispatcher dispatcher = request
						.getRequestDispatcher("cxpQueryResponse.jsp");
				dispatcher.forward(request, response);
			}

			return result.getResponse();

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
}


