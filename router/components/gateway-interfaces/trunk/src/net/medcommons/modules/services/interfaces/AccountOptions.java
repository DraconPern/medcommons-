package net.medcommons.modules.services.interfaces;

import java.util.Date;

/**
 * Represents options applicable to MedCommons Accounts
 * 
 * @author ssadedin
 */
public class AccountOptions {
    
    Boolean enableSimtrak = Boolean.FALSE;
    
    Date expiryDate;

    public Boolean getEnableSimtrak() {
        return enableSimtrak;
    }

    public void setEnableSimtrak(Boolean enableSimtrak) {
        this.enableSimtrak = enableSimtrak;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
}
