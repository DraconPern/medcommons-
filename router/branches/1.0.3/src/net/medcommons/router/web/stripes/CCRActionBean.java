/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.web.stripes;

import net.medcommons.router.services.wado.actions.NotificationForm;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.services.xds.consumer.web.action.UpdateActiveCCRAction;
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.Validate;

/**
 * A base class that is useful for any action that operates on a specific 
 * CCR within the user's session.
 *
 * A user may have any number of CCRs open at once.  These are all held in memory
 * in the session as part of a list in the "desktop" object.
 * 
 * When an action occurs on a CCR, the CCR affected must be specified by an index
 * into the list of CCRs in the desktop.  This is passed as a "ccrIndex" parameter.
 * 
 * @author ssadedin
 */
public class CCRActionBean extends BaseActionBean {
    
    protected CCRDocument ccr = null;
    
    private NotificationForm form;
    
    @Validate(ignore=true)
    int ccrIndex;
    
    public CCRActionBean() {
        super();
    }
    
    public NotificationForm getCCRForm() {
        if(form != null)
            return form;
        
        form = 
          (NotificationForm) this.ctx.getRequest().getSession().getAttribute("notificationForm");
        
        if(form == null) {
            form = new NotificationForm();
        }
        return form;
    }

    public ActionBeanContext getContext() {
        return ctx;
    }

    public void setContext(net.sourceforge.stripes.action.ActionBeanContext ctx) {
        super.setContext(ctx);
        this.ccrIndex = session.getActiveCCRIndex(ctx.getRequest());
        this.ccr = session.getCurrentCcr(ctx.getRequest());
    }
    
    @After(stages={LifecycleStage.BindingAndValidation})
    public void updateActors() throws Exception {
        if(ccr != null)
	        UpdateActiveCCRAction.resolveActorAccountIds(this.session.getServicesFactory(), ccr, this.session.getAccountSettings());
    }

    public CCRDocument getCcr() {
        return ccr;
    }

    public void setCcr(CCRDocument ccr) {
        this.ccr = ccr;
    }

    public int getCcrIndex() {
        return ccrIndex;
    }

    public void setCcrIndex(int ccrIndex) {
        this.ccrIndex = ccrIndex;
    }
}
