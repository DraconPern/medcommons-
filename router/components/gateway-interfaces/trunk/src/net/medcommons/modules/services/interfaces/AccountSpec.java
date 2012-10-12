/*
 * $Id: AccountSpec.java 2684 2008-06-26 06:37:38Z ssadedin $
 * Created on 07/12/2007
 */
package net.medcommons.modules.services.interfaces;

import static net.medcommons.modules.utils.Str.eq;

import org.json.JSONObject;

/**
 * Identifies a user by account id, idp, possibly other information. 
 * <p>
 * Note:equals() and hashcode() are overridden, so this object
 * behaves as a value type in collections.
 * 
 * @author ssadedin
 */
public class AccountSpec {
    
    private static final String MEDCOMMONS_ID_TYPE = "MedCommons";

    /**
     * Create an account spec for an Account Id for arbitrary IDP
     */
    public AccountSpec(String id, String idp) {
        this.id = id;
        this.idType = idp;
        if(MEDCOMMONS_ID_TYPE.equals(idType))
            this.mcid = id;
    }
    
    public AccountSpec(String id, String idType, String firstName, String lastName) {
        this(id,idType);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Create an AccountSpec for a MedCommons Account Id
     * @param accountId
     */
    public AccountSpec(String accountId) {
        this(accountId, MEDCOMMONS_ID_TYPE);
    }

    /**
     * Account ID corresponding to IDP of IdType
     */
    private String id;
    
    /**
     * Identifies IDP that is responsible for id
     */
    private String idType;
    
    /**
     * Account holder first name
     */
    private String firstName;
    
    /**
     * Account holder last name
     */
    private String lastName;
    
    /**
     * Medcommons Account Id - different to id if IDP is not Medcommons
     */
    private String mcid = ServiceConstants.PUBLIC_MEDCOMMONS_ID;
    
    @Override
    public boolean equals(Object obj) {
        
        if(!(obj instanceof AccountSpec))
            return false;
        
        AccountSpec other = (AccountSpec) obj;
            
        if(super.equals(obj))
            return true;
        
        return eq(id, other.id) && eq(idType,other.idType) && eq(mcid,other.mcid) && eq(firstName,other.firstName) && eq(lastName, other.lastName);
    }
    
    @Override
    public int hashCode() {
        String hash = id+idType+mcid+firstName+lastName;
        return hash.hashCode();
    }

    @Override
    public String toString() {
        return super.toString() + String.format("[ id=%s, idType= %s]", id, idType);
    }
    
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        return obj.put("id", id).put("idType",idType).put("firstName", firstName).put("lastName",lastName).put("mcid", mcid);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idp) {
        this.idType = idp;
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

    public String getMcId() {
        return mcid;
    }

    public void setMcId(String mcid) {
        this.mcid = mcid;
    }

}
