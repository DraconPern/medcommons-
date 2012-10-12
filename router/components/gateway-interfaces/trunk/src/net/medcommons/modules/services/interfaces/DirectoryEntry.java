/*
 * $Id$
 * Created on 14/07/2006
 */
package net.medcommons.modules.services.interfaces;

/**
 * An entry in a TODIR, including a person's name, contact information, alias etc.
 * 
 * @author ssadedin
 */
public class DirectoryEntry {
    
    private String name;
    private String context;
    private String externalId;
    private String alias;
    private String contact;
    private String accid;
        
    public DirectoryEntry() {    
    }
    
    /**
     * Create a DirectoryEntry
     * 
     * @param name
     * @param context
     * @param externalId
     * @param alias
     * @param contact
     */
    public DirectoryEntry(String name, String context, String externalId, String alias, String contact, String accid) {
        super();
        this.name = name;
        this.context = context;
        this.externalId = externalId;
        this.alias = alias;
        this.contact = contact;
        this.accid = accid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccid() {
        return accid;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }


}
