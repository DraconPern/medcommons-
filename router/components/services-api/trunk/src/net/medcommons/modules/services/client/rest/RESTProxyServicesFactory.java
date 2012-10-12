/*
 * $Id$
 * Created on 22/09/2005
 */
package net.medcommons.modules.services.client.rest;

import java.net.URL;

import net.medcommons.modules.activitylog.CSVActivityLogService;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.services.interfaces.*;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.transport.http.EasySSLProtocolSocketFactory;

/**
 * A factory that returns implementations of most of the services
 * via REST calls to PHP services on central.
 * 
 * In fact, some are done via SOAP calls to the identity services and
 * some are actually local services that are not remote at all. 
 * 
 * @author ssadedin
 */
public class RESTProxyServicesFactory implements ServicesFactory {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(RESTProxyServicesFactory.class);
    
    /**
     * NOTE: also declared in LocalFileRepository, but to avoid dependencies
     * it is redeclared here.  Needs refactoring.
     */
	public final static String POPS_MEDCOMMONS_ID = "0000000000000000";
    
    /**
     * The client Id for which this factory was created
     */
    private String authToken;
    
    /**
     * List of comma separated authentication tokens that describe the full
     * security context.  If null, then authToken is assumed to describe the
     * full security context
     */
    private String contextAuth;
    
    /**
     * Session id for this factory
     */
    private String sessionId;
    
    
    SHA1 sha = new SHA1().initializeHashStreamCalculation();
    
    /**
     * Creates a RESTProxyServicesFactory with the given authentication token
     * for communicating with central services
     * 
     * @param authToken
     */
    public RESTProxyServicesFactory(String authToken,String sessionId) {
        super();
        if(authToken == null) {
            authToken = "";
        }
        this.authToken = authToken;        this.sessionId = sessionId;
    }
    
    public RESTProxyServicesFactory(String authToken) {
        super();
        if(authToken == null) {
            authToken = "";
        }
        this.authToken = authToken;
        String rawSessionId = System.currentTimeMillis() + String.valueOf(Math.random()) + authToken;
        
        this.sessionId = sha.calculateByteHash(rawSessionId.getBytes());
    }

    public NotifierService getNotifierService() {        
        return new NotifierServiceProxy(authToken);
    }

    public HipaaService getHipaaService() {
        throw new UnsupportedOperationException();
    }

    public DocumentService getDocumentService() throws ServiceException {
        return new DocumentServiceProxy(authToken, contextAuth); 
    }

    public TrackingService getTrackingService() throws ServiceException {
        return new TrackingServiceProxy(authToken);
    }
    
    public AccountService getAccountService(){
    	return new AccountServiceProxy(authToken);
    }
    
    public SecondaryRegistryService getSecondaryRegistryService(){
    	return(new SecondaryRegistryServiceProxy(authToken));
    }

    public AccountCreationService getAccountCreationService() throws ServiceException {
        try {
            Service serviceModel = new ObjectServiceFactory().create(AccountCreationService.class,
                            "AccountCreationServiceImpl", "http://ws.identity.medcommons.net", null);

            URL accountCreationServiceURL = new URL(Configuration.getProperty("AccountCreationService.url"));
            registerSocketProtocol(accountCreationServiceURL);
    		  
            return (AccountCreationService)
            new XFireProxyFactory().create(
                            serviceModel, 
                            Configuration.getProperty("AccountCreationService.url"));
        }
        catch (Exception e) {
            throw new ServiceException("Unable to create AccountCreationService", e);
        } 
    }


    /*
    public ExpireService getExpiryService() throws ServiceException {
        try {
            Service serviceModel = new ObjectServiceFactory().create(ExpireService.class,
                            "expire", "http://orders", null); 
            URL accountCreationServiceURL = new URL(Configuration.getProperty("ExpireService.url"));
            registerSocketProtocol(accountCreationServiceURL);
            return (ExpireService)
            new XFireProxyFactory().create(
                            serviceModel, 
                            Configuration.getProperty("ExpireService.url"));
        }
        catch (Exception e) {
            throw new ServiceException("Unable to create AccountCreationService", e);
        } 
    }
    */
    
    public TransferStatusService getDICOMStatusService() throws ServiceException {
        try {
            Service serviceModel = new ObjectServiceFactory().create(TransferStatusService.class,
                            "TransferStatusServiceImpl", "http://ws.identity.medcommons.net", null);

            String url = Configuration.getProperty("DICOMStatusService.url");
            URL dicomStatusServiceURL = new URL(url);
            registerSocketProtocol(dicomStatusServiceURL);
            return (TransferStatusService)new XFireProxyFactory().create(serviceModel, url);
        }
        catch (Exception e) {
            throw new ServiceException("Unable to create DICOMStatusService", e);
        }
         
    }

    private static void registerSocketProtocol(URL url) {
        String serviceProtocol = url.getProtocol();
        //log.info("serviceProtocol is :" +serviceProtocol);
        if("https".equals(serviceProtocol)){
        	ProtocolSocketFactory easy = new EasySSLProtocolSocketFactory();
        	
        	// Note: if SSL URL without port then URL returns -1 for port.  However 
        	// protocol needs 443 to work
        	int port = url.getPort();
        	if(port == -1) 
        	    port = 443;
            Protocol protocol = new Protocol("https", easy, port);
        	Protocol.registerProtocol("https", protocol);
        }
    }
    

    public DirectoryService getDirectoryService(String url) {
        return new DirectoryServiceProxy(authToken,url);
    }

    public ActivityLogService getActivityLogService() {
        return new CSVActivityLogService(this.sessionId);
    }
    
    @SuppressWarnings("unchecked")
    public BillingService getBillingService() throws ServiceException {
        
        return new net.medcommons.modules.services.impl.BillingService(authToken);
        
        /*
         An experiment with using groovy to build a service
        GroovyClassLoader loader = new GroovyClassLoader();
        try {
            Class clazz = loader.parseClass(new FileInputStream("webapps/router/WEB-INF/classes/net/medcommons/modules/services/impl/BillingService.groovy"));
            return (BillingService)clazz.newInstance();
        }
        catch (CompilationFailedException e) {
            throw new ServiceException("Failed to load / parse service from source", e);
        }
        catch (FileNotFoundException e) {
            throw new ServiceException("Failed to load / parse service from source", e);
        }
        catch (InstantiationException e) {
            throw new ServiceException("Failed to load / parse service from source", e);
        }
        catch (IllegalAccessException e) {
            throw new ServiceException("Failed to load / parse service from source", e);
        }
        */
    }
    
    /**
     * Returns a session id for this factory uniquely identifying the session.
     * The session id is used to group a set of calls within a session for logging purposes. 
     * Where possible a single session id should be preserved across multiple calls by 
     * to different services by a single user.
     */
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setAuthContext(String contextAuth) {
        this.contextAuth = contextAuth;
    }
}
