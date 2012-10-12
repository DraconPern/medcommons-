/*
 * $Id$
 * Created on 4/10/2005
 */
package net.medcommons.cxp;

import static net.medcommons.cxp.CXPConstants.*;

/**
 * A utility extension of CXPResponse representing a successful
 * CXP Query response.
 * 
 * @author ssadedin
 */
public class CXPQueryResponse extends CXPResponse {
    
    private String ccrData;
    
    private String queryString;
    
    private String uid;

    public CXPQueryResponse(String uid, String queryString, String ccrData) {
        super(200, "", COMMAND_QUERY);
        this.ccrData = ccrData;
        this.queryString = queryString;
        this.uid = uid;
    }

    public String getCcrData() {
        return ccrData;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getUid() {
        return uid;
    }

}
