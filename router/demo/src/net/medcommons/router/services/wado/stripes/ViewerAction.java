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
@UrlBinding("/viewer/{mcid}/{guid}")
public class ViewerAction extends CCRActionBean {
    
    @UrlBinding("/v2/{mcid}/{guid}")
    public static class V2Action extends ViewerAction {{ JSP_PATH="/viewer.jsp";}}
    
    @UrlBinding("/ipad/{mcid}/{guid}")
    public static class IPadViewerAction extends ViewerAction {{ JSP_PATH="/ipadviewer.jsp";}}
    
    @UrlBinding("/bpad/{mcid}/{guid}")
    public static class BPadViewerAction extends ViewerAction {{ JSP_PATH="/bpad.jsp";}}
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ViewerAction.class);
    
    /**
     * Path of JSP to use.  This is different for the next generation 
     * viewer.
     */
    protected String JSP_PATH = "/browserviewer.jsp";
    
    /**
     * Path of JSP to use for browsers when rendering inside
     * a frame
     */
    protected String FRAMED_JSP_PATH = "/framedbrowserviewer.jsp";
    
    /**
     * Optional mcid.  If provided, will load CCR for corresponding 
     * patient.  If not provided, CCR corresponding to patient already 
     * loaded in session will be shown.
     */
    @Validate(required=false, mask=MCID_PATTERN)
    String mcid;
    
    /**
     * Optional guid
     */
    @Validate(required=false, mask=GUID_PATTERN)
    String guid;
    
    /**
     * Viewer config to be presented
     */
    @ImportProperties
    WADOViewerForm viewerConfig = new WADOViewerForm();
    
    /**
     * Whether to enable tools in the viewer or not
     */
    boolean enableTools=true;
    
    /**
     * Whether the viewer renders inside a frame with menus
     * on top
     */
    boolean framed = false;
    
    @DefaultHandler
    public Resolution show() throws Exception {
        
        resolveCCR();
        
        // CCR not found?  Show error
        if(this.ccr == null) 
            return new ForwardResolution("/wadoNoCcr.jsp");
        
        if(!blank(ccr.getPatientMedCommonsId()))
            ctx.setAttribute("patientAccountSettings", session.getAccountSettings(ccr.getPatientMedCommonsId()));
        
        
        // By default, show the *most recent* series, which happens to be the last
        viewerConfig.setInitialSeriesIndex(ccr.getSeriesList().size()-1);
        
        ctx.setAttribute("viewerForm", viewerConfig);
        ctx.setAttribute("enableTools", enableTools);
        
        session.setActiveCCR(ctx.getRequest(), ccr);
         
        // String path = framed ? FRAMED_JSP_PATH : JSP_PATH; 
    
        log.info("Forwarding to JSP path " + JSP_PATH);
        return new ForwardResolution(JSP_PATH);
    }
    
    /**
     * Attempt to resolve a CCR to display in the viewer.
     * <p>
     * If a CCR is specified using the ccrIndex parameter then that 
     * will be used, otherwise the Current CCR is loaded.
     */
    private void resolveCCR() throws Exception {
        if(this.ccr == null) {
            
            if(blank(mcid))
                throw new IllegalArgumentException("Account ID must be passed in URL if invoking this page directly");
            
            if(blank(guid)) {
                AccountSettings settings = this.session.getServicesFactory().getAccountService().queryAccountSettings(mcid);
                if(blank(settings.getCurrentCcrGuid()))
                    throw new IllegalArgumentException("Requested account " + mcid + " does not have a Current CCR");
                
                this.guid = settings.getCurrentCcrGuid();
            }
            
            this.ccr = this.session.resolve(this.guid);
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

    public boolean isEnableTools() {
        return enableTools;
    }

    public void setEnableTools(boolean enableTools) {
        this.enableTools = enableTools;
    }

    public boolean isFramed() {
        return framed;
    }

    public void setFramed(boolean framed) {
        this.framed = framed;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}