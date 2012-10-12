/*
 * $Id: LoginUtil.java 3711 2010-05-07 06:52:48Z ssadedin $
 * Created on 12/05/2005
 */
package net.medcommons.router.services.wado.actions;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.eq;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.client.rest.IncorrectEmailAddressException;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.rest.RESTException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.repository.DocumentNotFoundException;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.wado.InvalidCredentialsException;
import net.medcommons.router.services.wado.LoginFailedException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;

/**
 * Various routines to facilitate validating logins and initializing user sessions. 
 *  
 * @author ssadedin
 */
public class LoginUtil {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(LoginUtil.class);
    
   

    /**
     * Examines a request to determine if it contains a deep linked autologin from
     * Central. If so, validates the request against Central and logs the user in.
     * 
     * @param request
     * @throws LoginFailedException - the login failed due to system error
     * @throws InvalidCredentialsException  - the login failed due to invalid parameters
     * @throws RepositoryException 
     * @throws ServiceException 
     */
    public static void checkAutoLogin(HttpServletRequest request) throws LoginFailedException, InvalidCredentialsException, RepositoryException, ServiceException {
        HttpSession session = request.getSession();
        if (!"true".equals(session.getAttribute("autoLogon"))) { // no autologin
            return;
        }
       
        String username = (String) session.getAttribute("autoLogonUsername");
        String trackingNumber = (String) session.getAttribute("trackingNumber");
        String guid = (String) session.getAttribute("guid");
        String accid = (String) session.getAttribute("accid");
        log.info("TrackingNumber:'" + trackingNumber + "', guid:'" + guid + "', accid:" + accid);
        String hpass = (String) session.getAttribute("hpass");
        
        session.setAttribute("acctid", accid);
        
        // Case of guid specified
        if(!Str.blank(guid)) {
            session.removeAttribute("pin");
            session.removeAttribute("autoLogon");
            resolveGuid(request, guid, accid);
            if(session.getAttribute("trackingNumber")!=null) {
                UserSession desktop = UserSession.get(request);
                if(!desktop.getCcrs().isEmpty()) {
                    desktop.getCcrs().get(0).setTrackingNumber((String)session.getAttribute("trackingNumber"));
                    session.removeAttribute("trackingNumber");                        
                }                    
            }
        }
        else
        // case of tracking number specified
        if(!Str.blank(trackingNumber)) {
            try {
	            String pin = (String)session.getAttribute("pin");
                // This hack allows legacy code to pass an already hashed PIN
                // TODO: Remove this if the date is after Nov 2005
	            String hpin = (pin.length() == 40) ? pin : PIN.hash(pin);            
	            session.removeAttribute("pin");
	            session.removeAttribute("autoLogon");
                track(request, trackingNumber, hpin, null);
                UserSession desktop = UserSession.get(request);
                if(pin.length() == 5) {
                    desktop.setAccessPin(pin);            
                }
            }
            catch (NoSuchAlgorithmException e) {
            	log.error("Error checking login:" , e);
                throw new LoginFailedException(e);
            }
        }
         
    }
    
    /**
     * Resolves a document with the specified guid and initializes the user's session
     * with the corresponding documents.
     * Note: There are serious security holes with this approach- if you know a guid
     * then you're in. There should be some type of shared secret between the server
     * redirecting to this address and this one; perhaps other details too.
     * @param accid - the account id under which to resolve the guid
     * @throws DocumentNotFoundException 
     * @throws ServiceException 
     */
    public static void resolveGuid(HttpServletRequest request, String guid, String accid) 
        throws LoginFailedException, InvalidCredentialsException, DocumentNotFoundException, ServiceException
    {
        try {
            request.getSession().setAttribute("loginAttempted",Boolean.TRUE);
            // TODO:
            // The cast to CCRDocument here is problematic. In the short
            // run - no problemo. But until the Desktop handles all types
            // of RegistryDocuments - this may break a few things.
            CCRDocument currentCcr = (CCRDocument) RepositoryFactory.getLocalRepository().queryDocument(accid,guid);
            UserSession desktop = UserSession.clean(request, accid, request.getParameter("auth"));
            desktop.getCcrs().add(currentCcr);
            log.info("Desktop now created");
        }       
        catch(DocumentNotFoundException e) {
            throw e;
        }
        catch (RepositoryException e) {
        	log.error("Track failed:RepositoryException", e);
            throw new LoginFailedException(e);
        }        
    }
    /**
     * Validates the given tracking number and PIN and initializes the user's session
     * with the corresponding documents.
     * @param esId TODO
     * @throws DocumentNotFoundException 
     */
    public static CCRDocument track(HttpServletRequest request, String tn, String hpin, String esId) 
        throws LoginFailedException, InvalidCredentialsException, DocumentNotFoundException, IncorrectEmailAddressException
    {
        try {
            request.getSession().setAttribute("loginAttempted",Boolean.TRUE);
            
            UserSession session = UserSession.get(request);
            
            ServicesFactory factory = session.getServicesFactory();
            TrackingService trackingService = factory.getTrackingService();
            TrackingReference ref = trackingService.validate(tn, hpin, esId);
            if((ref == null) || (ref.getMcId()==null)) 
                throw new InvalidCredentialsException("No valid medcommons id found for tracking number " + tn + " hpin=" + hpin);
            
            log.info("Validated tracking number " + tn + ", return mcId = " + ref.getMcId());
            
            // Try to access using existing login credentials if they exist
            CCRDocument accessedCCR = null;
            AccountSpec contextPrincipal = null;
            String contextAuth = null;
            if(UserSession.has(request)) {
                session = UserSession.get(request);
                contextPrincipal = session.getOwnerPrincipal();
                accessedCCR = session.resolve(ref.getDocument().getGuid());
                contextAuth = session.getAuthenticationToken();
            }
            
            // Found?
            String accessedGuid = null;
            if(accessedCCR == null) { // not found, try it using tracking number
                session = UserSession.clean(request, ServiceConstants.PUBLIC_MEDCOMMONS_ID, ref.getAuth());      
                accessedGuid = ref.getDocument().getGuid();                
                accessedCCR = session.resolve(ref.getDocument().getGuid());
                
                // If the use already had a login session then log them back into that session
                // Note: this might prevent them later accessing documents 
                // that are resolved with the tracking number - probably a bug
                if((contextPrincipal != null) && !blank(contextPrincipal.getMcId()) && !eq(contextPrincipal.getMcId(), ServiceConstants.PUBLIC_MEDCOMMONS_ID)) {
                    session.setContextPrincipal(contextPrincipal);
                    session.setContextAuth(contextAuth);
                }
            }
            
            if(accessedCCR == null)
                throw new DocumentNotFoundException("Document corresponding to this tracking number is not available.");
            
            log.info("Retrieved CCR " + accessedGuid + " from repository");
            accessedCCR.setTrackingNumber(tn);
            
            // See if we can find the notification subject for this CCR
            String trackingNumber = accessedCCR.getTrackingNumber();
            try {
                String subject = factory.getNotifierService().querySubject(trackingNumber);                  
                if(subject != null) {
                    log.info("Notification subject " + subject + " found for CCR " + trackingNumber);
                    accessedCCR.setSubjectText(subject);
                }
            }
            catch(Exception e) {
                log.debug("Error while attempting to find subject for tracking number " + trackingNumber + ": " + e.getMessage());
            }
            
            session.getCcrs().add(accessedCCR);
            session.setAccessGuid(accessedGuid); 
            session.setAccessTrackingReference(ref);
            log.info("Successfully validated tracking number " + trackingNumber + " and retrieved document " + accessedGuid);
            return accessedCCR;
        }
        catch (IncorrectEmailAddressException e) {
            throw e;
        }
        catch (ServiceException e) {
            log.error("Tracking number validation / lookup failed for tracking number " + tn + " hpin = " + hpin, e);
            throw new LoginFailedException(e);
        }
        catch(DocumentNotFoundException e) {
            throw e;
        }
        catch (RepositoryException e) {
            log.error("Track failed:RepositoryException", e);
            throw new LoginFailedException(e);
        }
        catch (ConfigurationException e) {
            log.error("Tracking number validation / lookup failed for tracking number " + tn + " hpin = " + hpin, e);
            throw new LoginFailedException(e);
        }
        catch (PHRException e) {
            log.error("Tracking number validation / lookup failed for tracking number " + tn + " hpin = " + hpin, e);
            throw new LoginFailedException(e);
        }        
    }
    
    /**
     * Creates a blank, new session state for the given user, loading their details
     * from the repository.
     * 
     * @param mcId
     * @throws LoginFailedException
     */
    public static void initializeSession(HttpServletRequest request,String mcId) throws LoginFailedException {
        try {
            // Load the user's CCRs from the repository
            List<CCRDocument> ccrs = new ArrayList<CCRDocument>();// Right now generates an empty list. Needs to be populated from Central.           
            if (ccrs.size() == 0) {
                CCRDocument ccr = CCRDocument.createFromTemplate(mcId);
                ccr.setCreateTimeMs(System.currentTimeMillis());
                ccrs.add(ccr);
            }
            CCRDocument ccr = (CCRDocument) ccrs.get(0);
            log.warn("CCR is " + ccr);
            
            String auth = request.getParameter("auth");
            if(Str.blank(auth)) {
                auth = (String) request.getSession().getAttribute("auth");
                if(!Str.blank(auth)) {
                    log.info("Found auth in session: " + auth);
                    request.getSession().removeAttribute("auth");
                }
            }
            UserSession session = UserSession.clean(request,mcId,auth);
            session.getCcrs().addAll(ccrs);
            
            request.getSession().setAttribute("desktop", session);
        }
        catch (RepositoryException e) {
        	log.error("initializeSession:RepositorySession failed.", e);
            throw new LoginFailedException("Unable to initialzie session for id " + mcId, e);
        }
        catch (JDOMException e) {
        	log.error("initializeSession:JDOMException failed.", e);
            throw new LoginFailedException("Unable to initialzie session for id " + mcId, e);
        }
        catch (IOException e) {
        	log.error("initializeSession:IOException failed.", e);
            throw new LoginFailedException("Unable to initialzie session for id " + mcId, e);
        }
        catch (ParseException e) {
        	log.error("initializeSession:ParseException failed.", e);
            throw new LoginFailedException("Unable to initialzie session for id " + mcId, e);
        }
        catch (NoSuchAlgorithmException e) {
            log.error("initializeSession:ParseException failed.", e);
            throw new LoginFailedException("Unable to initialzie session for id " + mcId, e);
        }
        catch (PHRException e) {
            log.error("initializeSession:ParseException failed.", e);
            throw new LoginFailedException("Unable to initialzie session for id " + mcId, e);
        }
        catch (ServiceException e) {
            log.error("initializeSession:ParseException failed.", e);
            throw new LoginFailedException("Unable to initialzie session for id " + mcId, e);
        }
    }

}
