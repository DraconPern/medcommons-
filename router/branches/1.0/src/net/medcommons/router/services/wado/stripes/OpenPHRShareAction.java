package net.medcommons.router.services.wado.stripes;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.crypto.PIN;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.InvalidCredentialsException;
import net.medcommons.router.services.wado.LoginFailedException;
import net.medcommons.router.services.wado.actions.LoginUtil;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

/**
 * Action to display a PHR shared via anonymous link
 * 
 * @author ssadedin
 */
@UrlBinding("/share/{reference}")
public class OpenPHRShareAction extends BaseActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(OpenPHRShareAction.class);

    @Validate(required=true, mask="[a-f0-9]{5,17}")
    String reference;
    
    @DefaultHandler
    public Resolution open() throws Exception { 
        
        HttpServletRequest r = ctx.getRequest();
        
        try {
            ShareCodec codec = new ShareCodec();
            String [] decoded = codec.decode(reference);
            
            CCRDocument ccr = LoginUtil.track(ctx.getRequest(), decoded[0], PIN.hash(decoded[1])); 
            
            // Session many have been reinitialized by tracking login - can't rely on it
            this.session = UserSession.get(ctx.getRequest());
            
            log.info("Successfully opened share " + reference + " mapped to tracking number " + decoded[0]);
            
            return new RedirectResolution("/view#"+this.session.getCcrs().indexOf(ccr)+"v");
        }
        catch(LoginFailedException e) {
            return new ForwardResolution("/invalidShare.ftl");
        }
        catch(InvalidCredentialsException e) {
            return new ForwardResolution("/invalidShare.ftl");
        }
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

}
