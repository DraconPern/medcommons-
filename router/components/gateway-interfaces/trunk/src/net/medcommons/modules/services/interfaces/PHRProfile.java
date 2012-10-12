/*
 * $Id$
 * Created on 14/10/2008
 */
package net.medcommons.modules.services.interfaces;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * A CCR Profile represents a named view of a user's health record.
 * The name may be just a date on which the snapshot or view 
 * of the record was created or it may be a human assigned moniker,
 * or a predefined name such as "Current CCR"
 * <p>
 * The profileId uniquely identifies this profile from any other.
 * 
 * @author ssadedin
 */
public class PHRProfile implements Serializable{
    

    /**
     * Unique identifier for this profile
     */
    String profileId;
    
    /**
     * Name of this profile (logical identifier), if any
     */
    String name;
    
    /**
     * Guid of this profile (physical identifier of referenced CCR, if any)
     */
    String guid;
    
    /**
     * Time at which this profile was created
     */
    Date date;
    
    /**
     * Create a profile for a fixed CCR.  Such a profile
     * is identified by it's date rather than it's name, 
     * and the document is identified by it's guid.
     * 
     * @param guid
     */
    public PHRProfile(String guid) {
        this.guid = guid;
        this.date = new Date();
        this.profileId = guid +"."+date.getTime();
    }
    
    /**
     * Create a new PHRProfile
     * 
     * @param name - name of this profile (if any)
     * @param guid - guid of CCR referenced by this profile (if any)
     */
    public PHRProfile(String name, String guid) {
        this.name = name;
        this.guid = guid;
        this.date = new Date();
        
        StringBuilder id = new StringBuilder();
        if(name != null){
            for(int i = 0; i<name.length(); ++i) {
                id.append((int)name.charAt(i));
            }
            id.append(".");
        }
        id.append(guid).append(".").append(date.getTime());
        this.profileId = id.toString();
    }
    
    public PHRProfile() {
        
    }
    
    public JSONObject toJSON() { 
        return new JSONObject().put("name", name)
						       .put("profileId", profileId)
						       .put("guid", guid)
						       .put("date", this.date == null ? null : date.getTime());
    }
    
    public String toString() {        
        return ToStringBuilder.reflectionToString(this);
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
