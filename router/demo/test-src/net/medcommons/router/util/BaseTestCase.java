/*
 * $Id$
 * Created on 09/08/2006
 */
package net.medcommons.router.util;

import junit.framework.TestCase;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.rest.RESTConfiguration;
import net.medcommons.rest.RESTConfigurationException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.test.interfaces.ResourceNames;

import org.apache.log4j.BasicConfigurator;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;


public class BaseTestCase extends TestCase implements ServicesFactory {
    
    public static XPathCache xpath = null;
    private static boolean initializedLogs = false;

    public BaseTestCase() throws Exception {
        init();
    }

    /**
     * @throws Exception
     * @throws ConfigurationException
     */
    private void init() throws Exception, ConfigurationException {
        initializeTestEnvironment();
        
        if(xpath == null) 
            xpath = (XPathCache) Configuration.getBean("ccrXPathCache");

    }

    public static void initializeTestEnvironment() throws Exception, ConfigurationException {
        // Prevent logging trying to read the normal config by configuring it early
        
    	if (!initializedLogs) {
    		// BasicConfigurator.resetConfiguration();
    		BasicConfigurator.configure();
    		initializedLogs = true;
    	}
      
        System.setProperty("medcommons.spring.config.path",ResourceNames.DEFAULT_SPRING_CONFIG_PATH);
        
        Configuration.load("etc/configurations/config.xml",
                           "etc/configurations/MedCommonsBootParameters.properties",
                           ResourceNames.DEFAULT_SPRING_CONFIG_PATH);
        CCRDocument.setTemplatePath("etc/static-files/xds-templates");
        Configuration.getAllProperties().setProperty("CCRXSDLocation","etc/schema/ccr/CCR_20051109.xsd");
        
        RESTUtil.init( new RESTConfiguration() {
            public String getProperty(String name) throws RESTConfigurationException {
                try {
                    return Configuration.getProperty(name);
                }
                catch (ConfigurationException e) {
                    throw new RESTConfigurationException("Failed retrieving configuration value " + name, e);
                }
            }
            public String getProperty(String name, String defaultValue) {
                return Configuration.getProperty(name,defaultValue);
            }

            public int getProperty(String name, int defaultValue) {
                return Configuration.getProperty(name, defaultValue);
            }

            public boolean getProperty(String name, boolean defaultValue) {
                return Configuration.getProperty(name,defaultValue);
            }            
        });
    }
    
    public BaseTestCase(String name) throws ConfigurationException, Exception {
        super(name);
        init();
    }

    public AccountCreationService getAccountCreationService() throws ServiceException {
        try {
            Service serviceModel = new ObjectServiceFactory().create(AccountCreationService.class,
                            "AccountCreationServiceImpl", "http://ws.identity.medcommons.net", null);

            return (AccountCreationService)
            new XFireProxyFactory().create(
                            serviceModel, 
                            Configuration.getProperty("AccountCreationService.url"));
        }
        catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public AccountService getAccountService() {
        return new net.medcommons.modules.services.client.rest.AccountServiceProxy("JUnitTest");
    }

    public DirectoryService getDirectoryService(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    public DocumentService getDocumentService() {
        // TODO Auto-generated method stub
        return null;
    }

    public HipaaService getHipaaService() {
        // TODO Auto-generated method stub
        return null;
    }

    public NotifierService getNotifierService() {
        // TODO Auto-generated method stub
        return null;
    }

    public SecondaryRegistryService getSecondaryRegistryService() {
        // TODO Auto-generated method stub
        return null;
    }

    public TrackingService getTrackingService() {
        // TODO Auto-generated method stub
        return null;
    }

    public ActivityLogService getActivityLogService() {
        // TODO Auto-generated method stub
        return null;
    }

    public BillingService getBillingService() throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAuthContext(String contextAuth) {
            // TODO Auto-generated method stub
            
    }

    /*
    @Override
    public ExpireService getExpiryService() throws ServiceException {
            // TODO Auto-generated method stub
            return null;
    }
    */
}
