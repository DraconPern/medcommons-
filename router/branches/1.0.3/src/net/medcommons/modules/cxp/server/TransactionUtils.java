package net.medcommons.modules.cxp.server;

import java.security.NoSuchAlgorithmException;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.crypto.PIN.Hasher;
import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.services.interfaces.DocumentRegistration;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.services.interfaces.TrackingService;
import net.medcommons.modules.utils.DocumentTypes;

public class TransactionUtils implements CXPConstants{
	
	/**
	 * Generates the RegistryParameter entries for the TrackingNumber/PIN
	 * <p>
	 * Note: if PIN is passed as null, a random PIN will be generated automatically.
	 * To cause no PIN to be created, pass a blank string.
	 * 
	 * @param storageId
	 * @param registrySecretDigester TODO
	 * @param document
	 * @param pin
	 * @return
	 * @throws ServiceException
	 */
	public  String generateConfirmationCode(ServicesFactory serviceFactory, String contentType, String storageId,
			 String guid, String registrySecret, Hasher registrySecretDigester) throws ServiceException {

		String confirmationCode = null; 
		String rSecret = registrySecret;
		if (DocumentTypes.CCR_MIME_TYPE.equals(contentType)) {

			if (rSecret == null) {
				rSecret = PIN.generate();
			}
			
			TrackingService trackingService = serviceFactory.getTrackingService();
            
			String pinHash = null;
			try {
			    pinHash = (registrySecretDigester != null) ? registrySecretDigester.hash(rSecret) : rSecret;
			} 
            catch (NoSuchAlgorithmException e) {
				throw new ServiceException("Error generating PIN", e);
			}
            
            long expirySeconds = Configuration.getProperty("TrackingNumberPinExpirySeconds", 0);
			DocumentRegistration result = trackingService.registerTrackDocument(storageId,guid,pinHash, rSecret, expirySeconds, null);
			String summaryStatus = result.getStatus();
		    confirmationCode = result.getTrackingNumber();
		}
		return(confirmationCode);
	}
}
