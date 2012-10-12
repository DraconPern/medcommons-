/*
 * $Id$
 * Created on 22/09/2005
 */
package net.medcommons.modules.services.interfaces;

/**
 * ServicesFactory defines an interface for factories that create
 * references to service implementations and their components.
 * 
 * By using a ServicesFactory you can abstract your code from the 
 * specific services implementation you are using.
 * 
 * @author ssadedin
 */
public interface ServicesFactory {
    
    /**
     * Returns a NotifierService implementation
     */
    NotifierService getNotifierService();

    /**
     * Returns a HipaaService implementation
     */
    HipaaService getHipaaService();

    /**
     * Returns a DocumentService implementation
     * @throws ServiceException 
     */
    DocumentService getDocumentService() throws ServiceException;
    
    /**
     * Returns a TrackingService implementation
     * @throws ServiceException 
     */
    TrackingService getTrackingService() throws ServiceException;
    
    /**
     * Returns a DirectoryService implementation
     * @return
     */
    DirectoryService getDirectoryService(String url);
    
    /**
     * Returns an AccountService implementation.
     * @return
     */
    AccountService getAccountService();
    
    /**
     * Returns an AccountCreationService implementation
     * @return
     * @throws ServiceException 
     */
    AccountCreationService getAccountCreationService() throws ServiceException;
    
    
    /**
     * Returns a service for managing expiry of accounts
     */
    /*
    ExpireService getExpiryService() throws ServiceException;
    */
    
    /**
     * Returns a SecondaryRegistryService implementation.
     */
    SecondaryRegistryService getSecondaryRegistryService();
    
    /**
     * Returns an ActivityLogService implementation
     */
    ActivityLogService getActivityLogService();
    
    /**
     * Returns a BillingService instance
     * @throws ServiceException
     */
    BillingService getBillingService() throws ServiceException;

    /**
     * Auth token(s) that describe the full security context of calls being made
     */
    void setAuthContext(String contextAuth);
}
