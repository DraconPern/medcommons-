/*
 * $Id: PolicyResult.java 2756 2008-07-18 08:27:20Z ssadedin $
 * Created on 05/07/2008
 */
package net.medcommons.router.services.ccrmerge;

import org.apache.struts.util.MessageResources;

public class PolicyResult { 

    public PolicyResult(boolean allowed, String reason) {
        super();
        this.allowed = allowed;
        this.reason = reason;
    }
    
    public String format(MessageResources resources) {
        return this.reason;
    }
            
    /**
     * Whether the action was a allowed
     */
    public boolean allowed;
    
    /**
     * Reason for decision (if any)
     */
    public String reason;
}