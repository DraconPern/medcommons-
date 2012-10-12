/*
 * $Id: DemoGraphicMatchResult.java 2756 2008-07-18 08:27:20Z ssadedin $
 * Created on 16/07/2008
 */
package net.medcommons.router.services.ccrmerge;

import java.util.HashSet;
import java.util.Set;

import org.apache.struts.util.MessageResources;

public class DemoGraphicMatchResult extends PolicyResult {
    
    /**
     * Primary attribute that caused match to fail
     */
    String attribute;
    
    /**
     * Set of all mismatching fields
     */
    Set<String> mismatches = new HashSet<String>();
    
    public Set<String> getMismatches() {
        return mismatches;
    }

    public void setMismatches(Set<String> mismatches) {
        this.mismatches = mismatches;
    }

    public DemoGraphicMatchResult(boolean allowed, String reason, String attribute) {
        super(allowed, reason);
        if(attribute != null) {
            this.attribute = attribute;
            this.mismatches.add(attribute);
        }
    }

    public String format(MessageResources resources) {
        return resources.getMessage("mergepolicy.violation.demographicMismatch", 
                    resources.getMessage("mergepolicy.violation.demographicMismatch."+attribute));
     }
}
