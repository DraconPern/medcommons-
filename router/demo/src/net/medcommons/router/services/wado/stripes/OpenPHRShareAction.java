package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.urlencode;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.crypto.cookie.CookieCodec;
import net.medcommons.modules.crypto.cookie.CookieDecodeException;
import net.medcommons.modules.services.client.rest.IncorrectEmailAddressException;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.repository.DocumentNotFoundException;
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

    @Validate(required=true, mask="x{0,1}[a-f0-9]{5,26}")
    String reference;
    
    @DefaultHandler
    public Resolution open() throws Exception { 
        
        HttpServletRequest r = ctx.getRequest();
        
        try {
            ShareCodec codec = new ShareCodec();
            String [] decoded = codec.decode(reference);
            
            // Check for a login cookie
            CookieCodec.Values values = ctx.getLoginAttributes(); 
            if(values != null && !blank(values.auth)) {
                this.session = UserSession.clean(ctx.getRequest(), values.accid, values.auth);
            }
            
            CCRDocument ccr = LoginUtil.track(ctx.getRequest(), decoded[0], PIN.hash(decoded[1]), decoded.length>2?decoded[2]:null); 
            
            // Session many have been reinitialized by tracking login - can't rely on it
            this.session = UserSession.get(ctx.getRequest());
            
            log.info("Successfully opened share " + reference + " mapped to tracking number " + decoded[0]);
            
            return new RedirectResolution("/view#"+this.session.getCcrs().indexOf(ccr)+"v");
        }
        catch(DocumentNotFoundException e) {
            ctx.setAttribute("initialContents", "tab6");
            ctx.setAttribute("initialContentsUrl", "documentUnavailable.ftl");
            return new ForwardResolution("/platform.jsp");
        }
        catch(IncorrectEmailAddressException e) {
            // Send them to the login page
            String next = urlencode(Configuration.getProperty("RemoteAccessAddress")+"/share/"+reference); 
            String url = Configuration.getProperty("AccountsBaseUrl")+"/acct/login.php?next="+next+
                             "&email="+urlencode(e.getEmail())+
                             "&prompt=registered_email_required";
            return new RedirectResolution(url,false);
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
