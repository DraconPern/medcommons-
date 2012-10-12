package net.medcommons.modules.services.client.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.medcommons.modules.services.interfaces.SecondaryRegistryService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.rest.*;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;

public class SecondaryRegistryServiceProxy implements SecondaryRegistryService {

	/**
	 * Client for which this proxy is currently being used
	 */
	private String authToken;

	/**
	 * @param id
	 */
	public SecondaryRegistryServiceProxy(String id) {
		super();
		authToken = id;
	}

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger
			.getLogger(SecondaryRegistryServiceProxy.class);

	/**
	 * Adds a CCR event to the secondary registry
	 */
	public void addCCREvent(String firstName, String middleName,
			String lastName, String notificationSubject, String accid,
			String guid, String confirmationCode, String registrySecret, String registryName)
			throws ServiceException {

		try {
            String service = (registryName != null) ? 
                            "SecondaryRegistryService."+registryName+".addCCREvent" : "SecondaryRegistryService.addCCREvent";  
            
			RESTUtil.call(authToken, service,
					"fn", firstName, "mn", middleName, "ln", lastName,
					"ns", notificationSubject, "id", accid, "guid", guid, 
					"cc", confirmationCode, "rs", registrySecret);
					
		} catch (RESTException e) {
			log.error("Error SecondaryRegistryService.addCCREvent", e);
			throw new ServiceException("Unable to register CCR with GUID "
					+ guid);
		}
	}
	public void addCCREvent(String PatientGivenName, String PatientFamilyName, String PatientSex,
			String PatientIdentifier, String PatientIdentifierSource,
			String SenderProviderId, String ReceiverProviderId, String DOB,
			String age, String ConfirmationCode, String RegistrySecret,
			String Guid, String Purpose, String CXPServerURL,
			String CXPServerVendor, String ViewerURL, String Comment, String registryUrl)
			throws ServiceException{
		try { 
            String [] params = new String[] {
                            "PatientGivenName", PatientGivenName, 
                            "PatientFamilyName", PatientFamilyName,
                            "PatientSex", PatientSex,
                            "PatientIdentifier", PatientIdentifier,
                            "PatientIdentifierSource", PatientIdentifierSource, "SenderProviderId", SenderProviderId, "ReceiverProviderId", ReceiverProviderId, 
                            "DOB", DOB,
                            "PatientAge", age,
                            "ConfirmationCode", ConfirmationCode,
                            "RegistrySecret", RegistrySecret,
                            "Guid",Guid ,
                            "Purpose", Purpose,
                            "CXPServerURL", CXPServerURL,
                            "CXPServerVendor", CXPServerVendor,
                            "ViewerURL", ViewerURL,
                            "Comment",Comment};
            
            RESTUtil.RestCall call = new RESTUtil.RestCall(registryUrl+"&opcode=wsAddCCREvent", authToken, params);
                
            RESTUtil.executeUrl(call.url);					
		} 
        catch (UnsupportedEncodingException e) {
            log.error("Error SecondaryRegistryService.addCCREvent", e);
            throw new ServiceException("Unable to register CCR with GUID "+ Guid);
        }
        catch (JDOMException e) {
            log.error("Error SecondaryRegistryService.addCCREvent", e);
            throw new ServiceException("Unable to register CCR with GUID "+ Guid);
        }
        catch (IOException e) {
            log.error("Error SecondaryRegistryService.addCCREvent", e);
            throw new ServiceException("Unable to register CCR with GUID "+ Guid);
        }
        catch (RESTException e) {
            log.error("Error SecondaryRegistryService.addCCREvent", e);
            throw new ServiceException("Unable to register CCR with GUID "+ Guid);
        }
	}
	public Document queryRLS(String PatientGivenName, String PatientFamilyName,
			String PatientIdentifier, String PatientIdentifierSource,
			String SenderProviderId, String ReceiverProviderId, String DOB,
			String ConfirmationCode,
			String limit, String registryName)
			throws ServiceException {
        
	    //String service = (registryName != null) ? 
	    //                "SecondaryRegistryService."+registryName+".queryRLS" : "SecondaryRegistryService.queryRLS";
        String service = registryName;
	    
	    Document doc = null;
	    try {
            RESTUtil.RestCall  call = new RESTUtil.RestCall(service+"&opcode=wsQueryRLS", authToken, 
                            "PatientGivenName", PatientGivenName, 
                            "PatientFamilyName", PatientFamilyName, 
                            "PatientIdentifier", PatientIdentifier, 
                            "PatientIdentifierSource", PatientIdentifierSource, 
                            "SenderProviderId", SenderProviderId, 
                            "ReceiverProviderId", ReceiverProviderId, 
                            "DOB", DOB, 
                            "ConfirmationCode", ConfirmationCode, 
                            "limit", limit);
	        doc = RESTUtil.executeUrl(call.url);
	        return(doc);
        }	        
        catch (UnsupportedEncodingException e) {
            log.error("Error SecondaryRegistryService.queryRLS", e);
            throw new ServiceException("Unable to query record locator service ");
        }
        catch (JDOMException e) {
            log.error("Error SecondaryRegistryService.queryRLS", e);
            throw new ServiceException("Unable to query record locator service ");
        }
        catch (IOException e) {
            log.error("Error SecondaryRegistryService.queryRLS", e);
            throw new ServiceException("Unable to query record locator service ");
        }
        catch (RESTException e) {
            log.error("Error SecondaryRegistryService.queryRLS", e);
            throw new ServiceException("Unable to query record locator service ");
        }
	}
}
