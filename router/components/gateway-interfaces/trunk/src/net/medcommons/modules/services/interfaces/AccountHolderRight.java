/*
 * $Id$
 * Created on 29/01/2007
 */
package net.medcommons.modules.services.interfaces;

import java.util.Date;

/**
 * A transport object that is used to carry rights for an account in various
 * service calls. 
 * 
 * @author ssadedin
 */
public class AccountHolderRight {
    /**
     * The account id that this right is for
     */
    public String accountId;
    
    /**
     * External Share ID if this account comes from an external share
     */
    public Long esId;
    
    /**
     * Type of identity if this account comes from external share
     */
    public String identityType;
    
    /**
     * Email, if any for the account (optional)
     */
    public String email;
    
    /**
     * Rights set / to be set for account
     */
    public String rights;
    
    /**
     * First name of the user of the account (if any)
     */
    public String firstName;
    
    /**
     * Last name of the user of the account (if any)
     */
     public String lastName;
     
     /**
      * Date / Time on which this entry was created, if available
      */
     public Date createDateTime;

    /**
     * @param accountId
     * @param rights
     */
    public AccountHolderRight(String accountId, String rights) {
        super();
        this.accountId = accountId;
        this.rights = rights;
    }

    /**
     * Create an AccountHolder right for an external share
     */
    public AccountHolderRight(String accountId, String rights, String firstName, String lastName, String email, Long esId, String identityType, long createDateTimeSec) {
        super();
        this.accountId = accountId;
        this.rights = rights;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.esId = esId;
        this.identityType = identityType;
        this.createDateTime = createDateTimeSec > 0 ? new Date(createDateTimeSec*1000) : null;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public Long getEsId() {
        return esId;
    }

    public void setEsId(Long esId) {
        this.esId = esId;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public Date getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(Date createDateTime) {
        this.createDateTime = createDateTime;
    }
}

