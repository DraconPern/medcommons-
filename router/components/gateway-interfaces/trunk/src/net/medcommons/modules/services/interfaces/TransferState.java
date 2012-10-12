/*
 * $Id: TransferState.java 3179 2009-01-12 03:38:09Z ssadedin $
 * Created on 12/12/2008
 */
package net.medcommons.modules.services.interfaces;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.json.JSONObject;

/**
 * The status of an individual DICOM upload / download
 * 
 * @author ssadedin
 */
public class TransferState {

    /**
     * Unique identifier for the transfer
     */
    String key;
    
    /**
     * Identifier for the DDL
     */
    String ddlIdentifier;
    
    /**
     * Patient storage account into which this DICOM was stored.
     */
    String accountId;
    
    /**
     * The account id of the user who initiated the transaction
     */
    String ownerAccountId;
    
    /**
     * A text value describing the status of the upload.  For now this is not
     * defined by any domain of values but we should probably make an enum
     * for it eventually.
     */
    String status;
    
    /**
     * Type of transaction
     */
    DICOMTransactionType type;
    
    /**
     * Fractional progress of the upload.  EG: 65% finished == 0.65
     */
    Double progress;
    
    /**
     * An optional message that may be set to inform more detail about errors
     */
    String message;
    
    /**
     * Date that status for this DICOM transaction was last updated
     */
    Date modified;
    
    /**
     * Date when this DICOM transaction was created
     */
    Date created;
    
    /**
     * Number of series to be transferred
     */
    Integer numSeries = -1;        
    
    /**
     * Total images to be transferred
     */
    Integer totalImages = -1;      
    
    /**
     * Total bytes to be transferred
     */
    Long totalBytes;
    
    /**
     * Total bytes actually transferred
     */
    Long bytesTransferred;
    
    /**
     * Transfer rate as measured on DDL end
     */
    Float kbPerSecond;
    
    /**
     * The file name of the CCR (if any) associated with this transfer
     */
    String ccrFileName;
    
    /**
     * URL for viewing the uploaded data
     */
    String viewUrl;
    
    /**
     * DICOM study instance UID
     */
    String studyInstanceUID;
    
    /**
     * Hostname to / from which transfer occurred
     */
    String host;
    
    /**
     * URL path to which tranfer occurred
     */
    String path;
    
    /**
     * The protocol (http / https) used by the transfer
     */
    String protocol;
    
    /**
     * Group account id in context of which transfer occurred
     */
    String groupAccountId;
    
    /**
     * Name of group
     */
    String groupName;
    
    /**
     * Auth token used as security context for transfer
     */
    String auth;
    
    /**
     * Version number for detecting conflicts in writes
     */
    int version;
    
    
    /**
     * Creates a DICOM Status
     * 
     * @param key           unique identifier for this DICOM transaction.  Must be unique for
     *                       all DICOM transactions for the given accountId
     * @param accountId     mcid of account that is storing the DICOM
     * @param status        status of upload
     */
    public TransferState(String key, String accountId, String ownerAccountId, DICOMTransactionType type, String status) {
        this.key = key;
        this.status = status;
        this.accountId = accountId;
        this.created = this.modified = new Date();
    }
    
    /**
     * Noargs constructor for hibernate to use
     * 
     * @deprecated  This is only for hibernate and other libraries
     */
    public TransferState() {
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString(); 
    }

    public JSONObject toJSON() { 
        return new JSONObject().put("key", key)
                                .put("status", status)
                                .put("accountId", accountId)
                                .put("created", created != null ? created.getTime() : -1)
                                .put("modified", modified != null ? modified.getTime() : -1)
                                .put("version", version)
                                .put("type", type.name())
                                .put("host", host)
                                .put("path", path)
                                .put("protocol", protocol)
                                .put("groupName", groupName)
                                .put("auth", auth)
                                .put("groupAccountId", groupAccountId)
                                .put("studyInstanceUID", studyInstanceUID)
                                .put("viewUrl", viewUrl)
                                ;
    }
    
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public String getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(String ownerAccountId) {
        this.ownerAccountId = ownerAccountId;
    }

    public String getTypeValue() {
        return type.name();
    }

    public void setTypeValue(String type) {
        this.type = DICOMTransactionType.valueOf(type);
    }
    
    public DICOMTransactionType getType() {
        return type;
    }

    public void setType(DICOMTransactionType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDdlIdentifier() {
        return ddlIdentifier;
    }

    public void setDdlIdentifier(String ddlIdentifier) {
        this.ddlIdentifier = ddlIdentifier;
    }

    public Integer getNumSeries() {
        return numSeries;
    }

    public void setNumSeries(Integer numSeries) {
        this.numSeries = numSeries;
    }

    public Integer getTotalImages() {
        return totalImages;
    }

    public void setTotalImages(Integer totalImages) {
        this.totalImages = totalImages;
    }

    public Long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(Long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public Long getBytesTransferred() {
        return bytesTransferred;
    }

    public void setBytesTransferred(Long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
    }

    public Float getKbPerSecond() {
        return kbPerSecond;
    }

    public void setKbPerSecond(Float kbPerSecond) {
        this.kbPerSecond = kbPerSecond;
    }

    public String getCcrFileName() {
        return ccrFileName;
    }

    public void setCcrFileName(String ccrFileName) {
        this.ccrFileName = ccrFileName;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getGroupAccountId() {
        return groupAccountId;
    }

    public void setGroupAccountId(String groupAccountId) {
        this.groupAccountId = groupAccountId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

}
