/*
 * $Id$
 * Created on 06/01/2009
 */
package net.medcommons.modules.services.interfaces;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A message from a DDL
 * 
 * @author ssadedin
 */
public class TransferMessage {

    
    Long id;
    
    /**
     * Unique identifier of DDL
     * 
     * @see {@link TransferState#ddlIdentifier}
     */
    String ddlIdentifier;
    
    /**
     * Optional key of transfer if this message is relevant to a specific transfer
     * 
     * @see {@link TransferState#key}
     */
    String transferKey;
    
    /**
     * Account id if this message is relevant to a particular user
     */
    String accountId;
    
    /**
     * Type of message -  ERROR, INFO 
     */
    String category;
    
    /**
     * The actual text of the message
     */
    String message;
    
    /**
     * Timestamp when message created
     */
    Date createDateTime;
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getDdlIdentifier() {
        return ddlIdentifier;
    }

    public void setDdlIdentifier(String ddlIdentifier) {
        this.ddlIdentifier = ddlIdentifier;
    }

    public String getTransferKey() {
        return transferKey;
    }

    public void setTransferKey(String transferKey) {
        this.transferKey = transferKey;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(Date createDateTime) {
        this.createDateTime = createDateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
