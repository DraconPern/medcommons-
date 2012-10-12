package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.services.interfaces.InsufficientRightsException;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;

/**
 * Fronts the various share actions
 * 
 * @author ssadedin
 */
@UrlBinding("/form/{type}")
public class ShareAction extends BaseActionBean {
    
    /**
     * In the future will allow the different types of forms to be specified
     * in the URL.  For now, unused as we are only supporting a single form 
     * with this action.
     */
    String type;
    
    @ImportProperties
    ShareFormOptions options;
    
    @DefaultHandler
    public Resolution init() throws Exception {
        
        if(!this.session.checkPermissions(options.patientId, "R")) {
            throw new InsufficientRightsException("Account " + this.session.getOwnerPrincipal().toString()
                    + " is not authorized to access account " + options.patientId);
        }
        
        return new ForwardResolution("/shareForm.jsp");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ShareFormOptions getOptions() {
        return options;
    }

    public void setOptions(ShareFormOptions options) {
        this.options = options;
    }
}
