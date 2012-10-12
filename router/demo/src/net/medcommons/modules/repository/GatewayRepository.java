package net.medcommons.modules.repository;

import java.io.File;
import java.io.IOException;

import net.medcommons.modules.backup.BackupQueue;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.filestore.SimpleRepository;
import net.medcommons.modules.keymanagement.EncryptedKeyCacheFactory;
import net.medcommons.modules.services.interfaces.BackupService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.router.services.repository.EncryptionRepositoryListener;

import org.apache.log4j.Logger;

public class GatewayRepository extends SimpleRepository {
    
    /**
     * The root path under which data files for the repository will be stored 
     */
	public static final String FILE_STORE_PATH = "data/Repository";
    
    private static Logger log = Logger.getLogger("GatewayRepository");
    
    
	public GatewayRepository(String authToken, String nodeId, boolean encryptionEnabled, boolean backupEnabled) throws ServiceException{
		super();
		
		if(backupEnabled) {
		    log.info("Adding backup queue storage layer handler");
		    BackupService backupService = Configuration.getBean("backupService");
			listeners.add(new BackupQueue(backupService));
		}
		
		if(encryptionEnabled) {
			listeners.add(new EncryptionRepositoryListener(nodeId, EncryptedKeyCacheFactory.encryptedKeyCache(nodeId)));
		}
		
		File defaultDirectory = new File(FILE_STORE_PATH);
		setRootDirectory(defaultDirectory);
		setRepositoryName("GatewayRepository");
		try{
			init(null);
		}
		catch(IOException e){
			log.error("Error initializating GatewayRepository", e);
		}
	}
}
