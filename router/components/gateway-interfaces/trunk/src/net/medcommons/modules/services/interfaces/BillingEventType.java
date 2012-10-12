/*
 * $Id$
 * Created on 15/01/2007
 */
package net.medcommons.modules.services.interfaces;

/**
 * Enumeration of counter types known to the MedCommons billing system.
 * 
 * @author ssadedin
 */
public enum BillingEventType {
    INBOUND_FAX,
    INBOUND_DICOM,
    NEW_ACCOUNT;
    
    public String toString() {
        switch(this) {
            case INBOUND_FAX: 
                return "faxin";
            case INBOUND_DICOM:
                return "dicom";
            case NEW_ACCOUNT:
                return "acc";
            default:
                throw new IllegalArgumentException("Invalid value for billing event type " + this.name());
        }
    }
}
