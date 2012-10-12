package net.medcommons.modules.services.interfaces;

import org.jdom.Document;

/**
 * REST interface to secondary registry
 * This is a bit of demoware for TEPR; hopefully it will evolve past it.
 * <p/>
 * SS: No such luck with evolution - maybe we should have thrown our weight 
 * behind intelligent design instead.  See notes on groupId below.
 *  
 * @author sean
 * @auther ssadedin
 */
public interface SecondaryRegistryService {

	
	/**
	 * Puts record in Secondary Registry (aka Record Locator Service)
     * 
	 * @param PatientGivenName
	 * @param PatientFamilyName
	 * @param PatientIdentifier
	 * @param PatientIdentifierSource
	 * @param SenderProviderId
	 * @param ReceiverProviderId
	 * @param DOB
	 * @param age TODO
	 * @param ConfirmationCode
	 * @param RegistrySecret
	 * @param Guid
	 * @param Purpose
	 * @param CXPServerURL
	 * @param CXPServerVendor
	 * @param ViewerURL
	 * @param Comment
	 * @param registryName TODO
	 * @throws ServiceException
	 */
	public void addCCREvent(
            String PatientGivenName, String PatientFamilyName, String PatientSex,
			String PatientIdentifier, String PatientIdentifierSource,
			String SenderProviderId, String ReceiverProviderId, String DOB,
			String age, String ConfirmationCode, String RegistrySecret,
			String Guid, String Purpose, String CXPServerURL,
			String CXPServerVendor, String ViewerURL, String Comment, String registryUrl)
			throws ServiceException;

	/**
	 * Queries Record Locator Service
	 * 
	 * @param PatientGivenName
	 * @param PatientFamilyName
	 * @param PatientIdentifier
	 * @param PatientIdentifierSource
	 * @param SenderProviderId
	 * @param ReceiverProviderId
	 * @param DOB
	 * @param ConfirmationCode
	 * @param limit
	 * @param registryName TODO
	 * @throws ServiceException
	 */
	public Document queryRLS(
            String PatientGivenName, 
            String PatientFamilyName,
			String PatientIdentifier, 
            String PatientIdentifierSource,
			String SenderProviderId, 
            String ReceiverProviderId, 
            String DOB,
			String ConfirmationCode, 
			String limit, 
            String registryName)
			throws ServiceException;

	//SecondaryRegistryService.addCCREvent.url=${SecondaryRegistry}/reg2/ws/addCCREvent.php?PatientGivenName=#PatientGivenName#&mn=#mn#&PatientFamilyName=#PatientFamilyName#&ns=#ns#&id=#id#&guid=#guid#&ConfirmationCode=#cc#&RegistrySecret=#rs#
	//SecondaryRegistryService.queryCCREvent.url=${SecondaryRegistry}reg2/ws/queryCCREvent.php?fn=#fn#&mn=#mn#&ln=#ln#&ns=#ns#&id=#id#&guid=#guid#&cc=#cc#&rs=#rs#

}
