package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.router.services.wado.WADOViewerForm;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

/** * Initializes a WADO Viewer with parameters passed on the URL or
 * appropriate default parameters.  Any old settings are cleared. */
@UrlBinding("/viewer/{mcid}")
public class ViewerAction extends CCRActionBean {
    
    @UrlBinding("/v2/{mcid}")
    public static class V2Action extends ViewerAction {{ JSP_PATH="/viewer.jsp";}}
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ViewerAction.class);
    
    protected String JSP_PATH = "/wado.jsp";
    
    /**
     * Optional mcid.  If provided, will load CCR for 
     * corresponding patient.  If not provided, 
     * CCR corresponding to patient already loaded
     * in session will be shown.
     */
    @Validate(required=false, mask=MCID_PATTERN)
    String mcid;
    
    /**
     * Viewer config to be presented
     */
    @ImportProperties
    WADOViewerForm viewerConfig = new WADOViewerForm();
    
    @DefaultHandler
    public Resolution show() throws Exception {
        
        resolveCCR();
        
        // CCR not found?  Show error
        if(this.ccr == null) 
            return new ForwardResolution("/wadoNoCcr.jsp");
        
        if(!blank(ccr.getPatientMedCommonsId()))
            ctx.setAttribute("patientAccountSettings", session.getAccountSettings(ccr.getPatientMedCommonsId()));
        
        ctx.setAttribute("viewerForm", viewerConfig);
        session.setActiveCCR(ctx.getRequest(), ccr);
        
        return new ForwardResolution(JSP_PATH);
    }
    
    private void resolveCCR() throws Exception {
        if(this.ccr == null) {
            
            if(blank(mcid))
                throw new IllegalArgumentException("Account ID must be passed in URL if invoking this page directly");
            
            AccountSettings settings = this.session.getServicesFactory().getAccountService().queryAccountSettings(mcid);
            if(blank(settings.getCurrentCcrGuid()))
                throw new IllegalArgumentException("Requested account " + mcid + " does not have a Current CCR");
            
            this.ccr = this.session.resolve(settings.getCurrentCcrGuid());
        }
    }
    
    public String getMcid() {
        return mcid;
    }
    
    public void setMcid(String accid) {
        this.mcid = accid;
    }
    
    public WADOViewerForm getViewerConfig() {
        return viewerConfig;
    }
    
    public void setViewerConfig(WADOViewerForm viewerConfig) {
        this.viewerConfig = viewerConfig;
    }
}