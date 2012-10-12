package net.medcommons.router.selftests;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.repository.GatewayRepository;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.SupportedDocuments;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestResult;

/**
 * Simple test to see if repository file permissions are working correctly.
 * 
 * @author sdoyle
 *
 */
public class RepositoryPermissions  implements SelfTest {
	boolean backupEnabled = false;
	boolean encryptionEnabled = false;
	
	 public SelfTestResult execute(ServicesFactory services) throws Exception {
		 	GatewayRepository repository = null;
		 	String authToken = null;
		 	String path = "conf/config.xml";
			String propertiesPath = "conf/MedCommonsBootParameters.properties";
			Configuration.load(path, propertiesPath);
			String nodeId = Configuration.getProperty("NodeID");
			String encryptionConfig = Configuration.getProperty("EncryptionEnabled");
			String backupConfig = Configuration.getProperty("Backup_Documents");

			if ((encryptionConfig != null) && (!"".equals(encryptionConfig))) {
				encryptionEnabled = Boolean.parseBoolean(encryptionConfig);

			}
			if ((backupConfig != null) && (!"".equals(backupConfig))) {
				backupEnabled = Boolean.parseBoolean(backupConfig);

			}
		 	repository = new GatewayRepository(authToken, nodeId,
					encryptionEnabled, backupEnabled);
		 	SimpleDocumentDescriptor docDescriptor = new SimpleDocumentDescriptor();
		 	docDescriptor.setStorageId("1234567890123456");
		 	docDescriptor.setContentType(SupportedDocuments.TEXT.getContentType());
		 	String theDocument =  "This is the best of all possible worlds";
		 	ByteArrayInputStream in = new ByteArrayInputStream(theDocument.getBytes());
		 	
		 	
		 	repository.putInputStream(docDescriptor, in);
		 	
		 	InputStream docStream = repository.get(docDescriptor);
		 	StringBuffer buff = new StringBuffer();
		 	
		 	int i;
		 	byte[] buffer = new byte[8 * 1024];
			int n = -1;
			while ((n = docStream.read(buffer)) >= 0) {
				String s = new String(buffer, 0, n);
				buff.append(s);
			}
			try {
				docStream.close();
			} catch (Exception e) {
				;
			}
		 	String outputDocument = buff.toString();
		 	if (!theDocument.equals(outputDocument)){
		 		throw new RuntimeException("Input document \n" +
		 				theDocument + "\n and output document \n" +
		 				outputDocument + "\n do not match - repository error");
		 		
		 	}
	        return null;
	    }
}
