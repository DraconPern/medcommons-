/*
 * $Id$
 * Created on 10/08/2005
 */
package net.medcommons.modules.services.interfaces;

import java.io.Serializable;

public class DocumentRegistration implements Serializable {    
    private String trackingNumber;
    
    private String storageId;
    
    private String status;
    
    /**
     * Creates a RegistrationResult
     * 
     * @param status
     * @param number
     */
    public DocumentRegistration(String status, String number, String storageId) {
        super();
        this.status = status;
        trackingNumber = number;
        this.storageId = storageId;
    }
    
    public String getStatus() {
        return status;
    }
    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }
}
