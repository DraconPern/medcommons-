/*
 * $Id$
 * Created on 13/06/2006
 */
package net.medcommons.modules.services.interfaces;

/**
 * A service that supports creating and managing creation of Medcommons accounts. 
 * 
 * @author ssadedin
 */
public interface AccountCreationService {

        /**
         * Creates the requested account.
         * @param accountType - the type of account to create (REQUIRED)
         * @param email - email address (REQUIRED)
         * @param password - desired password (REQUIRED)
         * @param firstName - first name
         * @param lastName  - last name
         * @param telephoneNumber - telephone number
         * @param notificationRecipient - the registry to be notified (if any)
         * @param rlsUrl - an optional rls URL
         * @param photoUrl TODO
         * @param auth TODO
         * @param activation    Amazon credentials to activate account with, if it
         *                      should be activated
         * @param options       optional attributes to be set on the account (may be null)
         * 
         * @return - an array containing 3 elements:  account id of newly created account, 
         *                                             an access token that permits access to the account, and 
         *                                             a corresponding secret for the created token.
         */
        public abstract String [] register(AccountType accountType, String email, String password, String firstName,
                        String lastName, String telephoneNumber, String notificationRecipient, String rlsUrl, String photoUrl, String auth, ActivationDetails activation,
                        AccountOptions options
                        ) throws ServiceException;
        
        /**
         * Confirms the given  
         * 
         * @param accountId
         */
        public void confirmAccount(String accountId) throws ServiceException;

        /**
         * Queries for accounts created since a specified date.
         * 
         * @param since
         * @param recipient - recipient's id/registry/group
         * @param delete - if set to true then the call will delete the notification
         * @return - XML feed of new accounts created since the specified date
         * @throws ServiceException 
         */
        public abstract String queryCreated(String recipient, Long since, Boolean delete) throws ServiceException;

        /**
         * Translates the given emails to account ids or null
         * 
         * @param emails - array of email addresses
         * @return - array with one member for each email in the input array.
         * @throws ServiceException 
         */
        public String [] translateAccounts(String [] emails) throws ServiceException;

        /**
         * Translates the given accounts to email addresses, where available.
         * @throws ServiceException 
         */
        public String [] translate(String[] array) throws ServiceException;
        
        /**
         * Return all accounts that are past their 'expired' date but have not been set to 'DELETED'
         * 
         * @throws ServiceException
         */
        public String[] queryExpiredAccounts() throws ServiceException;
        
        /**
         * Set the specified account to 'deleted' status
         * 
         * @param deleteStatus      would normally be 'DELETED' but may be 'DELETE_FAIL' or 'DELETE_WARN'
         *                          if content was not successfully or fully deleted.
         * @throws ServiceException
         */
        public void deleteExpiredAccount(String accid, String deleteStatus) throws ServiceException;
}