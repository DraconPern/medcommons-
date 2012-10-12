/*
 * $Id: ExternalShare.java 3696 2010-04-28 08:00:44Z ssadedin $
 * Created on 18/04/2008
 */
package net.medcommons.modules.services.interfaces;

/**
 * ExternalShare represents a consent to an entity that does not have
 * a MedCommons Account ID, or alternatively, whose identity is verified
 * via a mechanism other than MedCommons sign in.   In some cases this is a 
 * third party mechanism that MedCommons trusts (eg: a white listed OpenID provider) 
 * or in others it is intrinsic (tracking number / PIN).
 * <p>
 * The actual consent referenced by an {@link ExternalShare} is not
 * modeled in this class, but rather in the <code>rights</code> table / class which
 * links to {@link ExternalShare} by foreign key.
 * 
 * @author ssadedin
 */
public class ExternalShare {
    
    /**
     * Domain of possible values for identity type field
     * <p>
     * <em>Note: the capitalization of these important as these affect directly what is
     *     stored in the database and needs to stay consisten.  Please don't change it.</em>
     */
    public static enum IdentityType {
        Application, Phone, Email, openid, PIN
    }
    
    Long id;
    
    String identity;
    
    String identityType;
    
    String firstName;
    
    String lastName;
    
    /**
     * Tracking Number to which this share is related.
     * Used when a tracking number is linked to one or more
     * registered email addresses (identityType = Email)
     */
    String trackingNumber;
    
    String auth;
    
    String secret;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
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

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setIdentityType(IdentityType idType) {
        setIdentityType(idType.name());
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
}
