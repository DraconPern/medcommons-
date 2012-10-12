/*
 * $Id: ActivationDetails.java 3149 2008-12-19 05:58:04Z ssadedin $
 * Created on 08/04/2008
 */
package net.medcommons.modules.services.interfaces;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ActivationDetails {
    
    private String activationKey;
    
    private String activationProductCode;
    
    /**
     * Existing Account ID from which activation details should be
     * inherited
     */
    private String accountId;
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getActivationProductCode() {
        return activationProductCode;
    }

    public void setActivationProductCode(String activationProductCode) {
        this.activationProductCode = activationProductCode;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

}
