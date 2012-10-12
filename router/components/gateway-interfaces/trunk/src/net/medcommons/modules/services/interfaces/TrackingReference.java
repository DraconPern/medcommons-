/*
 * $Id$
 * Created on 8/08/2005
 */
package net.medcommons.modules.services.interfaces;

import java.io.Serializable;

/**
 * Represents a Tracking number and it's corresponding rights.
 * 
 * @author ssadedin
 */
public class TrackingReference implements Serializable {

    /**
     * The Tracking Number
     */
    private String trackingNumber;
    
    /**
     * The PIN related to this tracking number.  This may be null
     * if the registrant of the encrypted pin did not provide the clear pin
     * when registering the original PIN.
     */
    private String pin;
    
    /**
     * The MedCommons Id associated with the Tracking Number
     */
    private String mcId;
    
    /**
     * The current access constraint on this tracking number
     */
    private TrackingAccessConstraint constraint;
    
    /**
     * Authentication token created confirming correct validation of tracking number / pin
     * This auth token can be used by recipient to gain access to resources
     * linked to the tracking number.
     */
    private String auth;
    
    /**
     * The guid for the document referred to by the TrackingNumber
     */
    private DocumentReference document;
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    public String getMcId() {
        return mcId;
    }
    public void setMcId(String mcId) {
        this.mcId = mcId;
    }
    public DocumentReference getDocument() {
        return document;
    }
    public void setDocument(DocumentReference document) {
        this.document = document;
    }
    public String getPin() {
        return pin;
    }
    public void setPin(String pin) {
        this.pin = pin;
    }
    public String getAuth() {
        return auth;
    }
    public void setAuth(String auth) {
        this.auth = auth;
    }
    public TrackingAccessConstraint getConstraint() {
        return constraint;
    }
    public void setConstraint(TrackingAccessConstraint constraint) {
        this.constraint = constraint;
    }
}
