/*
 * $Id$
 * Created on 16/06/2008
 */
package net.medcommons.router.api;

import net.medcommons.router.oauth.api.APIException;

public interface ApplianceAPI {

    /**
     * Locate a gateway that has storage for the given account and return 
     * it's URL.  This is often the first step of using the API if you
     * need to do operations at a gateway level, which require the 
     * gateway url as a parameter.
     * 
     * @param accid
     * @return URL of gateway that has storage for the given account.
     * @throws APIException 
     * @throws APIException 
     */
    String findStorage(String accid) throws APIException;
    
    
    /**
     * Find and return the Current CCR for a given account id.
     * 
     * @param accid   account id for which to return CCR
     * @param format  one of "xml" or "json"
     * @return CCR in requested format
     * @throws APIException 
     */
    String getCCR(String accid, String format) throws APIException;
    
    
    /**
     * Attempts to authenticate against the given account using
     * the given password.  If successful, returns an authentication
     * token granting access to the account.
     * 
     * @param accid account or tracking number to authenticate as
     * @param password password or PIN to verify
     * @return authentication token
     * @throws APIException 
     */
    AuthenticationResult authenticate(String accid, String password) throws APIException;
}
