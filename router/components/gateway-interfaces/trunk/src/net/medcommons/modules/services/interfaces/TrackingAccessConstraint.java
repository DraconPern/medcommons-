package net.medcommons.modules.services.interfaces;

/**
 * Defines modes of access for tracking numbers
 * 
 * @author ssadedin
 */
public enum TrackingAccessConstraint {
    /**
     * The tracking number lasts forever with unlimited accesses
     */
    UNLIMITED,  
    
    /**
     * The tracking number can only be used once
     */
    ONE_TIME, 
    
    /**
     * The tracking number can be accessed only by users with specified
     * email addresses. The email addresses that may be used 
     * are linked via external_share entries that reference the tracking
     * number.
     */
    REGISTERED_EMAIL, 
    
    /**
     * The tracking number has expired and can no longer be used
     */
    EXPIRED
}
