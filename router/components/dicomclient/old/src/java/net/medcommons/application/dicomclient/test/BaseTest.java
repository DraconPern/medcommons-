package net.medcommons.application.dicomclient.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import junit.framework.TestCase;
import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.DICOMClient;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;

import org.apache.log4j.Logger;

import com.meterware.httpunit.ClientProperties;

public class BaseTest extends TestCase {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("BaseTest");


	private static Properties properties = null;

	/**
	 * Initializes parameters
	 * Creates a copy of DICOMClient (the main application in DDL)
	 */
	public void setUp() throws Exception{
		super.setUp();

		if (properties == null){
			properties = new Properties();
			File defaults = new File("etc/test/junit.properties");
			if (!defaults.exists()){
				throw new FileNotFoundException("No default properties file found at " + defaults.getAbsolutePath());
			}
			FileInputStream defaultProperties = new FileInputStream(defaults);
			properties.load(defaultProperties);
			log.info("Loaded in default properties: " + defaults.getAbsolutePath());
			File localOverrides = new File("junit.properties");
			if (localOverrides.exists()){
				FileInputStream overrides = new FileInputStream(localOverrides);
				properties.load(overrides);
				log.info("Loaded local properties overrides:" + localOverrides.getAbsolutePath());
			}
			File tempDatabaseDir = new File("JUnitDB");
			if (!tempDatabaseDir.exists()){
				tempDatabaseDir.mkdir();
			}
			TransactionUtils.setUseDB(false);
			DICOMClient.initializeFiles();
			ContextManager cm = ContextManager.getContextManager();
			Configurations configs = cm.getConfigurations();

			/*
			 * Note the switch of local/remote in the following.
			 * The dicomParameters.DICOMLocal* values are the 'local'
			 * values of the DDL. Here we are sending data to the DDL -
			 * so the 'local' values are the 'remote' ones from the point
			 * of view of transactions from these JUnit classes.
			 */
			DicomClientParameters dicomParams = getDicomClientParameters();
			configs.setDicomLocalAeTitle("JUNIT");
			configs.setDicomLocalPort(dicomParams.DICOMLocalDicomPort);
			configs.setDicomRemoteAeTitle(dicomParams.DICOMLocalAETitle);
			configs.setDicomRemotePort(dicomParams.DICOMLocalDicomPort);
			configs.setDicomRemoteHost(dicomParams.DICOMRemoteHost); // Assume it's on this host?

			//LocalHibernateUtil.initializeSessionFactory(tempDatabaseDir);
			/*
			String args[] = new String[2];
			args[0] = "src/meta/war/app/DDL.properties";
			args[1] = "http://localhost:9080";

			DICOMClient.main(args);
			*/
			// Set the HttpUnit client not to redirect - this causes problems w/Ajax
			ClientProperties clientProperties = ClientProperties.getDefaultProperties();
	        clientProperties.setAutoRedirect(false);

		}
	}

	public Properties getProperties(){
		return(properties);
	}

	protected AccountFocusParameters getAccountFocusInfo(){
		AccountFocusParameters info = new AccountFocusParameters();
		info.accountId = properties.getProperty("info.accountId");
		info.auth = properties.getProperty("info.auth");
		info.cxpHost = properties.getProperty("info.cxpHost");
		info.cxpPort = properties.getProperty("info.cxpPort");
		info.cxpProtocol = properties.getProperty("info.cxpProtocol");
		info.cxpPath = properties.getProperty("info.cxpPath");
		info.groupName = properties.getProperty("info.groupName");
		info.groupAccountId = properties.getProperty("info.groupAccountId");
		return(info);
	}



	protected DicomClientParameters getDicomClientParameters(){
		DicomClientParameters param = new DicomClientParameters();
		param.ExportMethod = properties.getProperty("ExportMethod");
		param.DICOMRemoteHost = properties.getProperty("DICOMRemoteHost");
		param.DICOMLocalDicomPort = Integer.parseInt(properties.getProperty("DICOMLocalDicomPort"));
		param.ExportDirectory = properties.getProperty("ExportDirectory");
		param.DICOMRemotePort = Integer.parseInt(properties.getProperty("DICOMRemotePort"));
		return(param);

	}
}
