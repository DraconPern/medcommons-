/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.crypto.PIN;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.actions.LoginUtil;
import net.medcommons.router.services.wado.actions.NotificationForm;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.Validate;

/**
 * Loads a CCR and displays an editor for a specific section
 * @author ssadedin
 */
public class EditSectionAction extends CCRActionBean {
    
    @Validate(required=true)
    private String trackingNumber = null;
    
    @Validate(required=true)
    private String pin = null;
    
    @Validate(required=true)
    private String section = null;

    public EditSectionAction() {
        super();
    }
     
    @DefaultHandler
    public Resolution open() throws Exception {                
        
        if((trackingNumber == null) || (pin == null)) {
            throw new IllegalArgumentException("trackingNumber and pin are required");
        }
         
        LoginUtil.track(this.ctx.getRequest(), trackingNumber,PIN.hash(pin), null);
        
        this.session = UserSession.required(this.ctx.getRequest());
        this.ccr = this.session.getCurrentCcr(this.ctx.getRequest());
        
        NotificationForm.get(this.ctx.getRequest());
                
        ctx.getRequest().setAttribute("sectionName", section.toLowerCase());
        ctx.getRequest().setAttribute("ccrIndex", "0");
        return new ForwardResolution("/editSection.jsp");
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

}
