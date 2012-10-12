/*
 * $Id$
 * Created on 17/07/2006
 */
package net.medcommons.modules.services.interfaces;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import flexjson.JSONSerializer;

/**
 * Contextual information about an account within MedCommons.
 * 
 * @author ssadedin
 */
public class AccountSettings implements Serializable {
    
    /**
     * Account ID for which these settings apply
     */
    private String accountId;
    
    /**
     * First name of account owner
     */ 
    private String firstName;
    
    /**
     * Last name of account owner
     */
    private String lastName;
    
    /**
     * Email of account owner
     */
    private String email;
    
    /**
     * URL to photo of user
     */
    private String photoUrl;
    
    /**
     * Name of primary or active group associated with the account
     */
    private String groupName;
    
    /**
     * Account ID of primary group of this user
     */
    private String groupId;
    
    /**
     * Group Instance ID (note, this is an internal id, not an account id).
     */
    private String groupInstanceId;
    
    /**
     * Id of the practice associated with the user's active group (if any)
     */
    private Long practiceId;
    
    /**
     * Time / Date on which user's group was created
     */
    private Timestamp groupCreateDateTime;
    
    /**
     * URL of secondary registry configured for the account
     */
    private String registry;
    
    /**
     * URL of todir configured for the account
     */
    private String todir;
    
    /**
     * List of guids of documents for which notifications are pending for the current account.
     */
    private List<String> pendingNotifications;
    
    /**
     * List of accounts that gain rights to documents created by this account
     */
    private List<String> creationRights = new ArrayList<String>();
    
    /**
     * List of valid status values for this account.
     */
    private List<String> statusValues = new ArrayList<String>();
    
    /**
     * Expiry date of this account if it is a temporary account
     */
    private Date expiryDate = null;
    
    /**
     * Voucher details for this account, if it was created from a Voucher
     */
    private Voucher voucher = null;
    
    /**
     * True iff the user has vouchers / services enabled
     */
    boolean vouchersEnabled = false;
    
    /**
     * Amazon user token
     */
    private String amazonUserToken;
    
    /**
     * Amazon product token
     */
    private String amazonProductToken;
    
    /**
     * Amazon PID identifying the user's purchase transaction for their own account
     */
    private String amazonPid;
    
    /**
     * Which tips the user has turned off
     */
    private long tipState;
    
    /**
     * Map of logical documents stored for this account, keyed on
     * document type with values for the guids of associated documents.
     */
    private Map<AccountDocumentType, String> accountDocuments = new HashMap<AccountDocumentType, String>();
    
    /**
     * List of applications that this user has connected to
     */
    private List<Application> applications = new ArrayList<Application>();
    
    /**
     * AETitle for user's remote workstation, if they have one
     */
    private String dicomAeTitle = null;
    
    /**
     * Host / ip address for user's remote workstation, if they have one
     */
    private String dicomHost = null;
    
    /**
     * Port for user's remote workstation, if they have one
     */
    private int dicomPort = -1;

    public AccountSettings() {
        super();
    }

    public String getGroupName() {
        return groupName;
    }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }


    public String getRegistry() {
        return registry;
    }


    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public AccountSettings(String accountId, String groupId, String groupName, String registry, String todir,
                    String groupInstanceId, String statusValues,
                    String firstName, String lastName, String email, String photoUrl) 
    {
        super();
        this.accountId = accountId;
        this.groupName = groupName;
        this.registry = registry;
        this.todir = todir;
        this.groupId = groupId;
        this.groupInstanceId = groupInstanceId;
        if(statusValues != null)
            this.statusValues = Arrays.asList(Pattern.compile(",").split(statusValues));
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.photoUrl = photoUrl;
    }
    
    public String getJSON() {
        JSONSerializer json = new JSONSerializer().exclude("*.class").exclude("*.JSON").include("voucher");
        return json.serialize(this);
    }
    
    public String getToDir() {
        return this.todir;
    }

    public List<String> getCreationRights() {
        return creationRights;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupInstanceId() {
        return groupInstanceId;
    }

    public void setGroupInstanceId(String groupInstanceId) {
        this.groupInstanceId = groupInstanceId;
    }

    public String getCurrentCcrGuid() {
        return this.accountDocuments.get(AccountDocumentType.CURRENTCCR);
    }

    public void setCurrentCcrGuid(String currentCcrGuid) {
        this.accountDocuments.put(AccountDocumentType.CURRENTCCR, currentCcrGuid);
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getEmergencyCcrGuid() {
        return this.accountDocuments.get(AccountDocumentType.EMERGENCYCCR);
    }

    public void setEmergencyCcrGuid(String emergencyCcrGuid) {
        this.accountDocuments.put(AccountDocumentType.EMERGENCYCCR, emergencyCcrGuid);
    }

    public List<String> getStatusValues() {
        return statusValues;
    }

    public Map<AccountDocumentType, String> getAccountDocuments() {
        return accountDocuments;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Timestamp getGroupCreateDateTime() {
        return groupCreateDateTime;
    }

    public void setGroupCreateDateTime(Timestamp groupCreateDateTime) {
        this.groupCreateDateTime = groupCreateDateTime;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public synchronized Date getExpiryDate() {
        return expiryDate;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public synchronized void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean getVouchersEnabled() {
        return vouchersEnabled;
    }

    public void setVouchersEnabled(boolean vouchersEnabled) {
        this.vouchersEnabled = vouchersEnabled;
    }

    public String getAmazonUserToken() {
        return amazonUserToken;
    }

    public void setAmazonUserToken(String amazonUserToken) {
        this.amazonUserToken = amazonUserToken;
    }

    public String getAmazonProductToken() {
        return amazonProductToken;
    }

    public void setAmazonProductToken(String amazonProductToken) {
        this.amazonProductToken = amazonProductToken;
    }

    public String getAmazonPid() {
        return amazonPid;
    }

    public void setAmazonPid(String amazonPid) {
        this.amazonPid = amazonPid;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public String getDicomAeTitle() {
        return dicomAeTitle;
    }

    public void setDicomAeTitle(String dicomAeTitle) {
        this.dicomAeTitle = dicomAeTitle;
    }

    public String getDicomHost() { 
        return dicomHost;
    }

    public void setDicomHost(String dicomHost) {
        this.dicomHost = dicomHost;
    }

    public int getDicomPort() {
        return dicomPort;
    }

    public void setDicomPort(int dicomPort) {
        this.dicomPort = dicomPort;
    }

    public Long getPracticeId() {
        return practiceId;
    }

    public void setPracticeId(Long practiceId) {
        this.practiceId = practiceId;
    }

    public long getTipState() {
        return tipState;
    }

    public void setTipState(long tipState) {
        this.tipState = tipState;
    }

}
