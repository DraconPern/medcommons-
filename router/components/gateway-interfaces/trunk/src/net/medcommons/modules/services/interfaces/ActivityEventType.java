/*
 * $Id: ActivityEventType.java 3418 2009-07-08 05:51:58Z ssadedin $
 * Created on 02/11/2007
 */
package net.medcommons.modules.services.interfaces;

/**
 * Categories of ActivityEvent.
 * 
 * @author ssadedin
 */
public enum ActivityEventType {
    /**
     * Legacy activity log entries will be set to this value
     */
    UNKNOWN,
    PHR_ACCESS,
    PHR_UPDATE,
    PHR_SEND,
    CONSENT_UPDATE,
    CXP_IMPORT,
    CXP_EXPORT,
    ACOUNT_DOCUMENT_ADDED,
    SUMMARY;
    
    public String desc(int size) {
        boolean multiple = size > 1;
        String desc = size + " ";
        switch(this) {
            case PHR_ACCESS:
                return desc + (multiple ? "Accesses" : "Access");
                
            case PHR_UPDATE:
                return desc + (multiple ? "Updates" : "Update");
                
            case PHR_SEND:
                return desc + (multiple ? "Sends" : "Send");
                
            case CONSENT_UPDATE:
                return desc + (multiple ? "Consent Updates" : "Consent Update");
                
            case CXP_IMPORT:
                return desc + (multiple ? "CXP Imports" : "CXP Import");
                
            case CXP_EXPORT:
                return desc + (multiple ? "CXP Exports" : "CXP Export");
                
            case SUMMARY:
                return desc + (multiple ? "Summary" : "Summaries");
                
            case ACOUNT_DOCUMENT_ADDED:
                return desc + (multiple ? "Added Documents" : "Added Document");
                
            default:
                return "Unknown";
        }
    }
}
