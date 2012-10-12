package net.medcommons.modules.keymanagement;

import java.security.NoSuchAlgorithmException;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;

import org.apache.log4j.Logger;

public class EncryptedKeyCacheFactory {
	
	private static Logger log = Logger.getLogger(EncryptedKeyCacheFactory.class);
	private static EncryptedKeyCache encryptedKeyCache = null;
	private static boolean unableToInitialize = false;
	
	/**
     * The factory that will be used to access services
     */
	private static ServicesFactory serviceFactory = Configuration.getBean("systemServicesFactory");
	
	/**
	 * Returns the singleton EncryptedKeyCache. Throws error if the 
	 * EncryptedKeyCache can not be initialized.
	 * 
	 * @return
	 */
	public static EncryptedKeyCache encryptedKeyCache(String nodeId) {
	    
		if(unableToInitialize) {
			log.error("Unable to initialize EncryptedKeyCache");
			throw new RuntimeException("EncryptedKeyCache not initialized");
		}
		
		try {
		    if (encryptedKeyCache == null)
		        encryptedKeyCache = new EncryptedKeyCache(nodeId, serviceFactory.getDocumentService());
		}
		catch(NoSuchAlgorithmException e){
			log.error("Creation of EncryptedKeyCache failed", e);
			unableToInitialize = true;
		}
        catch (ServiceException e) {
			log.error("Creation of EncryptedKeyCache failed", e);
			unableToInitialize = true;
        }
        return encryptedKeyCache;
	}

}
