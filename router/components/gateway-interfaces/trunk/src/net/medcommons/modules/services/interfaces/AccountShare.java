/*
 * $Id$
 * Created on 29/01/2007
 */
package net.medcommons.modules.services.interfaces;

import java.util.ArrayList;

public class AccountShare {
    
    private String practiceId;
    
    private String practiceName;
    
    private String groupAcctId;
    
    private String accessRights;
    
    private String identityType;
    
    private String applicationToken;
    
    private Long esId;
    
    private ArrayList<AccountHolderRight> accounts;

    public String getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(String accessRights) {
        this.accessRights = accessRights;
    }

    public String getGroupAcctId() {
        return groupAcctId;
    }

    public void setGroupAcctId(String groupAcctId) {
        this.groupAcctId = groupAcctId;
    }

    public ArrayList<AccountHolderRight> getAccounts() {
        return accounts;
    }

    public void setAccounts(ArrayList<AccountHolderRight> permissions) {
        this.accounts = permissions;
    }

    public String getPracticeId() {
        return practiceId;
    }

    public void setPracticeId(String practiceId) {
        this.practiceId = practiceId;
    }

    public String getPracticeName() {
        return practiceName;
    }

    public void setPracticeName(String practiceName) {
        this.practiceName = practiceName;
    }

    /**
     * @param practiceId
     * @param practiceName
     * @param groupAcctId
     * @param accessRights
     * @param accounts
     */
    public AccountShare(String practiceId, String practiceName, String groupAcctId, String accessRights) {
        super();
        this.practiceId = practiceId;
        this.practiceName = practiceName;
        this.groupAcctId = groupAcctId;
        this.accessRights = accessRights;
        this.accounts = new ArrayList<AccountHolderRight>();
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    public Long getEsId() {
        return esId;
    }

    public void setEsId(Long esId) {
        this.esId = esId;
    }
}
