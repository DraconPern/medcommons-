/*
 * $Id: ViewDesktopAction.java 3133 2008-12-09 10:07:47Z ssadedin $
 * Created on 20/05/2008
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * Renders the whole desktop, showing a specified tab by default
 * 
 * @author ssadedin
 */
@UrlBinding("/view")
public class ViewDesktopAction extends BaseActionBean {
    
    @DefaultHandler
    public Resolution view() throws NotLoggedInException {
        if(ctx.getRequest().getSession().isNew() && session.getCcrs().isEmpty())
            throw new NotLoggedInException();
        
        ctx.getRequest().setAttribute("initialContentsUrl", "view?init");
        ctx.getRequest().setAttribute("initialContents", "tab4");
        ctx.getRequest().setAttribute("readFragment", Boolean.TRUE);
        return new ForwardResolution("/platform.jsp");
    }
    
    public Resolution init() throws CCROperationException, PHRException {
        
        CCRDocument ccr = session.getCurrentCcr(this.ctx.getRequest());
        
        // Check if a new version of the CCR has been saved 
        if(ccr.getStorageMode() == StorageMode.LOGICAL && !blank(ccr.getPatientMedCommonsId())) {
            session.flushAccountSettings();  
            CCRDocument newCCR = session.getLogicalDocument(ccr.getPatientMedCommonsId(), ccr.getLogicalType());
            if(newCCR != null) {
                ccr = newCCR;
                session.setActiveCCR(ctx.getRequest(), ccr);
            }
        }
        
        return new ForwardResolution("/viewEditCCR.do");
    }
}
