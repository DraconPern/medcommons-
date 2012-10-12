/*
 * $Id: CCRActorElement.java 3652 2010-04-02 17:45:49Z ssadedin $
 * Created on 12/09/2008
 */
package net.medcommons.phr.ccr;
 
import static net.medcommons.modules.utils.Str.blank;

import java.util.List;

import org.jdom.Namespace;

/**
 * Adds some useful behaviors for manipulating actors
 * 
 * @author ssadedin
 */
public class CCRActorElement extends CCRElement {
 
    public CCRActorElement(String arg0, Namespace arg1) {
        super(arg0, arg1);
    }

    public CCRActorElement(String arg0, String arg1, String arg2) {
        super(arg0, arg1, arg2);
    }

    public CCRActorElement(String arg0, String arg1) {
        super(arg0, arg1);
    }

    public CCRActorElement(String arg0) {
        super(arg0);
    }

    public CCRActorElement() {
        this("Actor");
    }
    
    /**
     * Create a valid actor representing a MedCommons Account holder using
     * the given details.
     * 
     * @param accountId
     * @param email
     * @param sourceActorObjectID   source, if null will use "self" as source
     */
    public static CCRActorElement createMedCommonsActor(String accountId, String email, String sourceActorObjectID) {
        CCRActorElement actor = new CCRActorElement();
        String objectId = actor.generateObjectID();
        if(sourceActorObjectID == null)
            sourceActorObjectID = actor.generateObjectID();
        
        actor.createPath("ActorObjectID",objectId);
        actor.createPath("IDs/Type/Text",Constants.MEDCOMMONS_ACCOUNT_ID_TYPE);
        actor.createPath("IDs/ID",accountId);
        actor.createPath("IDs/Source/Actor/ActorID", sourceActorObjectID);
        actor.createPath("EMail/Value",email);
        actor.createPath("InformationSystem/Name","MedCommons Notification");
        actor.createPath("InformationSystem/Type","Repository");
        actor.createPath("InformationSystem/Version","V1.0");
        actor.createPath("Source/Actor/ActorID", sourceActorObjectID); 
        return actor;
    }
    

    /**
     * Return the ActorID of this actor
     */
    public String getID() {
        return getChildTextTrim("ActorObjectID");
    }
    
    /**
     * Returns this actor's email address, or null
     * if they have no email address
     */
    public String getEmail() { 
        CCRElement e = getChild("EMail");
        if(e == null) 
            return null;
        
        return e.getChildTextTrim("Value");
    }
    
    public void setEmail(String e) {
        if(blank(e)) {
            this.removeChild("EMail");
        }
        else {
            if(!e.equals(this.getEmail())) {
	            this.createPath("EMail/Value", e);
	            this.removeAccountId();
            }
        }
            
    }
    
    public String getGivenName() {
        return eval("Person/Name/CurrentName/Given");
    }
    
    public String getFamilyName() {
        return eval("Person/Name/CurrentName/Family");
    }
    
    public void setGivenName(String value) {
        getOrCreate("Person").getOrCreate("Name").getOrCreate("CurrentName").getOrCreate("Given").setText(value);
    }
    
    public void setFamilyName(String value) {
        getOrCreate("Person").getOrCreate("Name").getOrCreate("CurrentName").getOrCreate("Family").setText(value);
    }
    
     /**
     * Returns this actor's MedCommons ID or null
     * if they do not have one
     */
    public String getAccountId() {
        for(CCRElement e : this.getChildren("IDs")) {
            if(Constants.MEDCOMMONS_ACCOUNT_ID_TYPE.equals(e.getPath("Type/Text").getTextTrim())) 
                return e.getChildTextTrim("ID");
        }
        return null;
    }
    
    /**
     * Set this actor's Medcommons account id
     * @param accid
     * @param sourceActorID
     */
    public void setAccountId(String accid, String sourceActorID) {
        this.createPath("IDs/Type/Text", Constants.MEDCOMMONS_ACCOUNT_ID_TYPE);
        this.createPath("IDs/Source/Actor/ActorID", sourceActorID);
        this.createPath("IDs/ID", accid);
    }
    
    public void removeAccountId() {
        List<CCRElement> ids = this.getChildren("IDs");
        for(CCRElement e : ids) {
            if(Constants.MEDCOMMONS_ACCOUNT_ID_TYPE.equals(e.getPath("Type/Text").getTextTrim())) {
                this.removeChild(e);
                break;
            }
        }
    }

}
