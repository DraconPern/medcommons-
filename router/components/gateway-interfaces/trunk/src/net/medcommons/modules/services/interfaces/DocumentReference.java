/*
 * $Id$
 * Created on 4/08/2005
 */
package net.medcommons.modules.services.interfaces;

import java.io.Serializable;
import java.util.Date;

/**
 * A reference to a document. It identifies the document and 
 * also it's location and possibly other useful metadata.
 * 
 * @author ssadedin
 */
public class DocumentReference implements Serializable {

    /**
     * Guid of the document.
     */
    private String guid;
    
    /**
     * Name/id of node where this document is located.  We may need
     * a richer description of this.
     */
    private String location;
    
    /**
     * The key of the node on which this document belongs
     */
    private String locationKey;
    
    /**
     * Timestamp for creation of the document.  Not certain if this is needed, but
     * it may be necessary to include this kind of metadata so that the recipient
     * of a reference can evaluate it to determine if they are interested in
     * retrieving the whole document.  We also have to account for privacy
     * constraints.
     */
    private Date creationDate;
    
    /**
     * The account id under which the document is stored.
     */
    private String storageAccountId;
    
    /**
     * The outstanding charge, if any associated with the document
     */
    private BillingEvent outstandingCharge;
    
    /**
     * The tracking reference for this document, if one has been assigned.
     */
    private TrackingReference trackingReference;
    
    
    public Date getCreationDate() {
        return creationDate;
    }


    public String getGuid() {
        return guid;
    }


    public String getLocation() {
        return location;
    }


    public DocumentReference() {
        super();
    }


    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }


    public void setGuid(String guid) {
        this.guid = guid;
    }


    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @param date
     * @param guid
     * @param location
     */
    public DocumentReference(String storageAccountId, Date date, String guid, String location, String locationKey) {
        super();
        creationDate = date;
        this.guid = guid;
        this.location = location;
        this.storageAccountId = storageAccountId;
        this.locationKey = locationKey;
    }

    public String getStorageAccountId() {
        return storageAccountId;
    }


    public void setStorageAccountId(String storageAccountId) {
        this.storageAccountId = storageAccountId;
    }


    public TrackingReference getTrackingReference() {
        return trackingReference;
    }


    public void setTrackingReference(TrackingReference trackingReference) {
        this.trackingReference = trackingReference;
    }


    public String getLocationKey() {
        return locationKey;
    }


    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }


    public BillingEvent getOutstandingCharge() {
        return outstandingCharge;
    }


    public void setOutstandingCharge(BillingEvent outstandingCharge) {
        this.outstandingCharge = outstandingCharge;
    }

}
