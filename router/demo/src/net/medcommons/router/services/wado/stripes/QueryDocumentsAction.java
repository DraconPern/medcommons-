/*
 * $Id$
 * Created on 11/08/2006
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.wado.InsufficientPrivilegeException;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.medcommons.router.web.stripes.JSON;
import net.medcommons.router.web.stripes.JSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.*;

import org.apache.log4j.Logger;

import flexjson.JSONSerializer;

/**
 * Queries all documents for the patient for the active CCR 
 * and returns them as a JSON string.
 * 
 * @author ssadedin
 */
public class QueryDocumentsAction extends JSONActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(QueryDocumentsAction.class);
    
    private DocumentIndexService indexService = Configuration.getBean("documentIndexService");
    
    /**
     * Optional input. Specifies patient account id if there is no existing session.
     * Auth token must be provided in that case to allow access to the patient.
     */
    @Validate(required=true, mask=MCID_PATTERN)
    String accid;
    
    @DefaultHandler
    @JSON
    public Resolution query() throws Exception {
        
        HashMap<String, Object> results = new HashMap<String, Object>();
        
        if(!session.checkPermissions(accid, "R")) 
            throw new InsufficientPrivilegeException("Current session does not have necessary access to patient " + accid);
        
        log.info("Using patient  = " + accid);
        
        Map<AccountDocumentType, String> accountDocuments = session.getAccountSettings(accid).getAccountDocuments();
        results.put("status", "ok");
        List<DocumentDescriptor> allDocuments = indexService.getDocuments(accid, null, null, null);
        
        for(DocumentDescriptor documentDescriptor : allDocuments) { 
            if(accountDocuments.containsValue(documentDescriptor.getSha1())) {
                for(AccountDocumentType adt : accountDocuments.keySet()) {
                    if(accountDocuments.get(adt).equals(documentDescriptor.getSha1()))
                        documentDescriptor.setDocumentName(adt.name());
                }
            }
        }
        
        results.put("documents",allDocuments);

        return new StreamingResolution("text/plain",
                new JSONSerializer().exclude("class").serialize(results)); 
    }
    
    /**
     * We allow external access as long as the patient account id and explicit authentication token is set
     */
    @Override
    protected void checkSID() {
        HttpServletRequest r = this.ctx.getRequest();
        if(!blank(r.getParameter("accid")) && !blank(r.getParameter("auth"))) 
            return;
        super.checkSID();
    }

    public String getAccid() {
        return accid;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }
}
