/*
 * $Id: EmailEntry.java 2914 2008-09-12 08:44:02Z ssadedin $
 * Created on 15/08/2008
 */
package net.medcommons.router.services.account;

public class EmailEntry {
    
    /**
     * Email Entry
     */
    public EmailEntry(String account, String email) {
        this.email = email;
        this.timestamp = System.currentTimeMillis();
        this.accountId = account;
    }

    public String email;
    
    public String accountId;
    
    public long timestamp;

}
