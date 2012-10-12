/*
 * $Id$
 * Created on 4/08/2005
 */
package net.medcommons.modules.services.impl;


public class TrackingNumber {

    private String trackingNumber;
    private Long rightsId;
    private String encryptedPIN;
    
    public TrackingNumber() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    public String getEncryptedPIN() {
        return encryptedPIN;
    }

    public void setEncryptedPIN(String encryptedPIN) {
        this.encryptedPIN = encryptedPIN;
    }

    public Long getRightsId() {
        return rightsId;
    }

    public void setRightsId(Long rightsId) {
        this.rightsId = rightsId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
}
