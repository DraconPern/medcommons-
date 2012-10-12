package net.medcommons.modules.services.interfaces;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Details about a patient.
 * 
 * @author ssadedin
 */
public class PatientDemographics {
    
    String accountId;
    
    String givenName;
    
    String familyName;
    
    String sex;
    
    Date dateOfBirth;

    /**
     * A non-MedCommons ID in the domain specified by {@link #idDomain}
     */
    String id;
    
    /**
     * The affinity domain of the {@link #id}
     */
    String idDomain;
    
    
    
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String firstName) {
        this.givenName = firstName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String lastName) {
        this.familyName = lastName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdDomain() {
        return idDomain;
    }

    public void setIdDomain(String idDomain) {
        this.idDomain = idDomain;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
