/*
 * $Id$
 * Created on 10/01/2007
 */
package net.medcommons.modules.services.interfaces;

import org.json.JSONObject;

/**
 * An event affecting an account which is logged in the Account Activity log
 *  
 * @author ssadedin
 */
public class ActivityEvent {
    
    private long timeStampMs;
    private String sessionId;
    private String description;
    private AccountSpec sourceAccount;
    private String affectedAccountId;
    private String trackingNumber;
    private String pin;
    private ActivityEventType type = ActivityEventType.UNKNOWN;
    private BillingCharge charge;
    
    /**
     * Creates an ActivityEvent for logging with no tracking number or pin
     * 
     * @param type
     * @param description
     * @param sourceAccount
     * @param affectedAccountId
     */
    public ActivityEvent(ActivityEventType type, String description, AccountSpec sourceAccount, String affectedAccountId) {
        this(type,description,sourceAccount, affectedAccountId,null,null);
    }


    /**
     * Create a new ActivityEvent
     * 
     * @param timeStampMs
     * @param sessionId - id for session to which this event belongs
     * @param description
     * @param sourceAccountId
     * @param affectedAccountId
     * @param trackingNumber
     */
    public ActivityEvent(long timeStampMs, String sessionId, String description, AccountSpec sourceAccount, String affectedAccountId, String trackingNumber, String pin) {
        super();
        this.timeStampMs = timeStampMs;
        this.description = description;
        this.sourceAccount = sourceAccount;
        this.affectedAccountId = affectedAccountId;
        this.trackingNumber = trackingNumber;
        this.sessionId = sessionId;
        this.pin = pin;
    }
    
    
    /**
     * Create a new ActivityEvent, defaulting timestamp to current time
     * @param type TODO
     * @param description
     * @param sourceAccountId
     * @param affectedAccountId
     * @param trackingNumber
     */
    public ActivityEvent(ActivityEventType type, String description, AccountSpec sourceAccount, String affectedAccountId, String trackingNumber, String pin) {
        super();
        this.timeStampMs = System.currentTimeMillis();
        this.description = description;
        this.sourceAccount = sourceAccount;
        this.affectedAccountId = affectedAccountId;
        this.trackingNumber = trackingNumber;
        this.type = type; 
        this.pin = pin;
    }
    
    public JSONObject toJSON() {
        JSONObject e = new JSONObject();
        return e.put("timeStamp", timeStampMs/1000)
                 .put("description", description)
                 .put("sourceAccount", sourceAccount.toJSON())
                 .put("affectedAccountId", affectedAccountId)
                 .put("trackingNumber", trackingNumber)
                 .put("charge", charge)
                 .put("trackingNumber", trackingNumber)
                 .put("type", type.name());
    }


    public String getAffectedAccountId() {
        return affectedAccountId;
    }
    public void setAffectedAccountId(String affectedAccountId) {
        this.affectedAccountId = affectedAccountId;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getSourceAccountId() {
        return sourceAccount.getId();
    }
    public long getTimeStampMs() {
        return timeStampMs;
    }
    public void setTimeStampMs(long timeStampMs) {
        this.timeStampMs = timeStampMs;
    }
    public String getTrackingNumber() {
        return trackingNumber;
    }
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }


    public String getSessionId() {
        return sessionId;
    }


    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    public ActivityEventType getType() {
        return type;
    }


    public void setType(ActivityEventType type) {
        this.type = type;
    }


    public AccountSpec getSourceAccount() {
        return sourceAccount;
    }


    public void setSourceAccount(AccountSpec sourceAccount) {
        this.sourceAccount = sourceAccount;
    }


    public String getPin() {
        return pin;
    }


    public void setPin(String pin) {
        this.pin = pin;
    }


    public BillingCharge getCharge() {
        return charge;
    }


    public void setCharge(BillingCharge charge) {
        this.charge = charge;
    }
}
