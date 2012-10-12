/*
 * $Id: AuthenticateOpenIDAction.java 3570 2009-12-30 06:52:37Z ssadedin $
 * Created on 17/09/2007
 */
package net.medcommons.router.services.wado.stripes;

import java.util.List;

import javax.servlet.http.HttpSession;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.repository.DocumentResolver;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;

/**
 * Action for handling OpenID authentication for accessing a CCR 
 * 
 * @author ssadedin
 */
public class AuthenticateOpenIDAction extends BaseActionBean {
    
    /**
     * Value in session where tracking number is stored for openid transaction
     */
    private static final String OPENID_TRACKING_NUMBER_SESSION = "openid-trackingNumber";

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AuthenticateOpenIDAction.class);
    
    String openid_url;
    
    String trackingNumber="";
    
    /**
     * Guid is output.  It is translated from trackingNumber upon successful login.
     */
    String guid;    
    
    /**
     * Attempt to authenticate the given OpenID
     */
    @DefaultHandler
    public Resolution authenticate() throws Exception {
        
        ConsumerManager manager = new ConsumerManager();
        HttpSession session = this.ctx.getRequest().getSession();
        session.setAttribute("openid-manager", manager);
        
        this.trackingNumber = trackingNumber.replaceAll("[ \\t]", "");
        
        // configure the return_to URL where your application will receive
        // the authentication responses from the OpenID provider
        String returnToUrl = Configuration.getProperty("RemoteAccessAddress") + "/AuthenticateOpenID.action?result";
        
        session.setAttribute(OPENID_TRACKING_NUMBER_SESSION,this.trackingNumber);

        // perform discovery on the user-supplied identifier
        List discoveries = manager.discover(openid_url);

        // attempt to associate with the OpenID provider
        // and retrieve one service endpoint for authentication
        DiscoveryInformation discovered = manager.associate(discoveries);

        // store the discovery information in the user's session
        session.setAttribute("openid-disc", discovered);

        // obtain a AuthRequest message to be sent to the OpenID provider
        AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

        return new RedirectResolution(authReq.getDestinationUrl(true), false);
    }
    
    /**
     * Handles the callback from the OpenID provider
     */
    public Resolution result() throws Exception {
        
        HttpSession session = ctx.getRequest().getSession();
        this.trackingNumber = (String) session.getAttribute(OPENID_TRACKING_NUMBER_SESSION);
        
        session.removeAttribute(OPENID_TRACKING_NUMBER_SESSION);
        
        // extract the parameters from the authentication response
        // (which comes in as a HTTP request from the OpenID provider)
        ParameterList openidResp = new ParameterList(ctx.getRequest().getParameterMap());

        // retrieve the previously stored discovery information
        DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute("openid-disc");

        // extract the receiving URL from the HTTP request
        StringBuffer receivingURL = ctx.getRequest().getRequestURL();
        String queryString = ctx.getRequest().getQueryString();
        if (queryString != null && queryString.length() > 0)
            receivingURL.append("?").append(ctx.getRequest().getQueryString());
         
        ConsumerManager manager = (ConsumerManager) session.getAttribute("openid-manager");

        // verify the response
        VerificationResult verification = manager.verify(receivingURL.toString(), openidResp, discovered);

        // examine the verification result and extract the verified identifier
        Identifier verified = verification.getVerifiedId();

        if(verified != null) {
            UserSession d = UserSession.get(ctx.getRequest());
            
            // Determine the correct guid
            guid = d.getServicesFactory().getTrackingService().queryGuid(trackingNumber);
            
            // Get an auth token for the openid
            String auth = d.getServicesFactory().getDocumentService().createAuthToken(verified.getIdentifier(), null, "openid");
            
            d = UserSession.clean(ctx.getRequest(), null, auth);
            
            DocumentResolver resolver = new DocumentResolver(d.getServicesFactory());
            
            CCRDocument ccr = resolver.resolveCCR(d.getOwnerMedCommonsId(), guid);
            d.getCcrs().add(ccr);
            
            // Check permissions
            if(ccr != null) {
                log.info("Resolved guid " + guid + " for tracking number " + trackingNumber + " with auth " + auth);
                return new ForwardResolution("/openidSuccess.ftl");
            }
            
        }
        
        // If we got here, things went wrong :-(
        return new ForwardResolution("/openidFailure.ftl");
    }
    
    /**
     * Show a page while the open id provider validates the given OpenID
     * @return
     */
    public Resolution waitForProvider() {
        return new ForwardResolution("/validatingOpenId.ftl");
    }

    public String getOpenid_url() {
        return openid_url;
    }

    public void setOpenid_url(String openidUrl) {
        this.openid_url = openidUrl;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
