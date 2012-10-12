/*
 * $Id: BillableAccount.java 2622 2008-05-27 05:49:47Z ssadedin $
 * Created on 15/05/2008
 */
package net.medcommons.modules.services.interfaces;

import org.json.JSONObject;

public class BillableAccount {
    /**
     * Account to which this token is bound.  Note that a billing token may be bound to multiple
     * accounts.  The {account, token} pair specifies a billing event customer.
     */
    String account;
    
    /**
     * Token for use with billing service.
     */
    String token;
    
    public BillableAccount(String account, String token) {
        this.account = account;
        this.token = token;
    }
    
    @Override
    public String toString() {
        return "{account: " + account + ", token: " + token + "}";
    }
    
    public JSONObject toJSON() {
        JSONObject e = new JSONObject();
        return e.put("account", account)
                 .put("token", token);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
