/*
 * $Id: AuthenticationResult.java 2698 2008-07-01 06:53:17Z ssadedin $
 * Created on 01/07/2008
 */
package net.medcommons.router.api;

/**
 * Result of authentication to a remote appliance
 * 
 * @author ssadedin
 */
public class AuthenticationResult {
    
    String token;
    
    public AuthenticationResult(String accountId, String token) {
        this.accountId = accountId;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    String accountId;

}
