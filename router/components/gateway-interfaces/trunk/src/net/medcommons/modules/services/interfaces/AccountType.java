/*
 * $Id$
 * Created on 18/04/2007
 */
package net.medcommons.modules.services.interfaces;

/**
 * Valid Account Types
 * 
 * @author ssadedin
 */
public enum AccountType {
    
    /**
     * Not used
     */
    PROVISIONAL, 
    
    /**
     * Default state for passwordless accounts created by 3rd parties
     */
    SPONSORED, 
    
    /**
     * In the process of being claimed, but not completed
     */
    CLAIMED,
    
    /**
     * Claimed, but email not verified
     */
    UNVALIDATED,
    
    /**
     * Full account, representing group
     */
    GROUP, 
    
    /**
     * Full account, representing individual user
     */
    USER, 
    
    /**
     * This appears in the tables but is undocumented - treat like USER
     */
    VOUCHER, 
    
    /**
     * A USER account that has been set for immediate expiry via console
     */
    EXPIRE_IMMEDIATE,
    
     /**
     * Account is a USER account that has been deleted (PHI is gone)
     */
    DELETED,
    
    /**
     * Account is isuer Account that was deleted but with warnings
     */
    DELETE_WARN,
    
    /**
     * Account is isuer Account that was deleted but with delete failures
     */
    DELETE_FAIL
}
