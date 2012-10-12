/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.Validate;

/**
 * Attempts to resolve the requested email into an account id 
 * 
 * @author ssadedin
 */
public class ResolveEmailAction extends CCRActionBean {
    
    @Validate(required=true, converter=EmailTypeConverter.class)
    private String toEmail;

    public ResolveEmailAction() {
        super();
    } 
    
    @DefaultHandler
    public Resolution resolveEmail() throws Exception {
        if(this.session == null)
            throw new NotLoggedInException();

       String[] results = this.session.getServicesFactory().getAccountCreationService().translateAccounts(new String[]{ toEmail });
       
       assert results.length > 0 : "Results size should match input size for translateAccounts";
       
       return new JavaScriptResolution(results[0]);
    }
    
    public Resolution getSourcePageResolution() {
        return new JavaScriptResolution("");
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }    
}
