package net.medcommons.application.dicomclient.utils;

import net.sourceforge.pbeans.annotations.*;

/**
 * An order placed to handle specific DICOM and registered
 * with a group.
 * 
 * @author ssadedin
 */
@PersistentClass(table="dicom_order", idField="id", autoIncrement=true,
	        indexes={
		        @PropertyIndex(unique=false,propertyNames={"callersOrderReference"}),
		        @PropertyIndex(unique=false,propertyNames={"status"})
	       }
 )
 public class DicomOrder {
    
    public final static String DOWNLOAD_COMPLETE = "DDL_ORDER_DOWNLOAD_COMPLETE";
    public final static String COMPLETE = "DDL_ORDER_COMPLETE";
    public final static String CANCELLED = "DDL_ORDER_CANCELLED";
    public final static String ERROR = "DDL_ORDER_ERROR";
    public final static String TIMEOUT_WARNING = "DDL_ORDER_TIMEOUT_WARNING";
    
    Long id;
    
    String callersOrderReference;
    
    String status;
    
    String storageId;

    String guid;
    
    Long cxpTransactionId;
    
    @Override
    public String toString() {
        return "DicomOrder[id="+id+",callersOrderReference="+callersOrderReference+",status="+status+
               "cxpTransactionId="+cxpTransactionId+",guid="+guid + "]";
    }
    
    public String getCallersOrderReference() {
        return callersOrderReference;
    }

    public void setCallersOrderReference(String callersOrderReference) {
        this.callersOrderReference = callersOrderReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public Long getCxpTransactionId() {
        return cxpTransactionId;
    }

    public void setCxpTransactionId(Long cxpTransactionId) {
        this.cxpTransactionId = cxpTransactionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
