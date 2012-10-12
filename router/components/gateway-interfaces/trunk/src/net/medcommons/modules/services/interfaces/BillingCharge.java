/*
 * $Id: BillingCharge.java 2622 2008-05-27 05:49:47Z ssadedin $
 * Created on 15/05/2008
 */
package net.medcommons.modules.services.interfaces;

import org.json.JSONObject;

/**
 * An actionable charge that specifies a set of counters to be
 * decremented and an account to be charged.
 * 
 * @author ssadedin
 */
public class BillingCharge {
    
    /**
     * Account that will be charged
     */
    BillableAccount account;
    
    /**
     * Set of counters that will be decremented
     */
    BillingCounters counters = new BillingCounters();
    
    /**
     * If this charge has been executed, a transaction id identifying it
     */
    String transactionId;
    
    
    @Override
    public String toString() {
        return "account = " + ((account != null) ? account.toString() : "null") + " counters = " + counters.toString();
    }
    
    public JSONObject toJSON() {
        JSONObject e = new JSONObject();
        return e.put("account", account.toJSON())
                 .put("counters", counters.toJSON())
                 .put("transactionId", transactionId);
    }

    public BillableAccount getAccount() {
        return account;
    }

    public void setAccount(BillableAccount account) {
        this.account = account;
    }

    public BillingCounters getCounters() {
        return counters;
    }

    public void setCounters(BillingCounters counters) {
        this.counters = counters;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
