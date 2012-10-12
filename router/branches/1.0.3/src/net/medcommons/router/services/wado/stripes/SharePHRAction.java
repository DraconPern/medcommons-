/*
 * $Id: SharePHRAction.java 3636 2010-03-23 07:28:11Z ssadedin $
 * Created on 05/05/2008
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.nvl;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.services.interfaces.TrackingAccessConstraint;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.wado.DocumentNotFoundException;
import net.medcommons.router.services.wado.InsufficientPrivilegeException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

/**
 * JSON service that shares a user's Current CCR by Email
 * using tracking number and PIN
 * 
 * @author ssadedin
 */
public class SharePHRAction extends CCRJSONActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SharePHRAction.class);
    
    @Validate(required=false, mask=MCID_PATTERN) 
    private String fromAccount;
    
    @Validate(required=false, converter=EmailTypeConverter.class)
    private String toEmail;
    
    @Validate(required=false,mask="[0-9]{5}")
    private String pin;
    
    /**
     * The type of control over the privacy of the shared
     * document. One of 'pin', 'one', 'registrationn', or 'public'
     */
    @Validate(required=false)
    private String control;
    
    @DefaultHandler
    public Resolution share() throws Exception {
        
        String auth = this.session.getAuthenticationToken();
        try {
            ServicesFactory svc = this.session.getServicesFactory();
            
            if(blank(fromAccount) && this.ccr != null)
                fromAccount = this.ccr.getPatientMedCommonsId();  
            
            // Check that we are allowed to share
            if(!this.session.checkPermissions(fromAccount, "R")) 
                throw new InsufficientPrivilegeException("Authorization token " + auth 
                        + " does not have read permission for account " + fromAccount);
             
            // Get user's current CCR
            String currentCcrGuid = session.getAccountSettings(fromAccount).getCurrentCcrGuid();
            if(blank(currentCcrGuid)) 
                throw new IllegalArgumentException("Account " + fromAccount + " does not have a Current CCR");
            
            log.info("Sharing current CCR for user " + fromAccount + " guid="+currentCcrGuid + " with " + toEmail + " auth=" + auth);
            
            CCRDocument ccr = session.resolve(currentCcrGuid);
            if(ccr == null)
                throw new DocumentNotFoundException("Unable to load document " + currentCcrGuid);
            
            if(!blank(toEmail))
                ccr.getJDOMDocument().setValue("toEmail", toEmail);
            
            // If the type of control is specified specifically as 'pin' then
            // forcibly generate a random pin even if one was not set
            if("pin".equals(control) && blank(pin)) {
                pin = PIN.generate();
            }
            
            // Even if the user doesn't want a pin, we generate one
            // we just don't tell them explicitly about it
            String actualPin = blank(pin) ? PIN.generate() : pin;
                
            // Share it to the required email address
            StoreTransaction tx = session.tx(ccr);
            tx.registerDocument(actualPin, 
                    blank(pin)?TrackingAccessConstraint.ONE_TIME:TrackingAccessConstraint.UNLIMITED);
            tx.storeDocument();
            tx.writeActivity(ActivityEventType.PHR_SEND, "PHR Shared with " + nvl(toEmail,"PIN"));
            
            // Return the tracking number
            result.put("trackingNumber", ccr.getTrackingNumber());
            
            if(!blank(pin)) { // send normal notification email
	            tx.sendEmail(null);
	            result.put("pin", actualPin);
            }
            else { // no PIN - send different email
                
                // Create a share link and return it in the result
                ShareCodec codec =new ShareCodec();
	            String code = codec.encode(ccr.getTrackingNumber(), actualPin);
	            String accessUrl = Configuration.getProperty("RemoteAccessAddress")+"/share/"+code;
	            result.put("url", accessUrl);
	            result.put("to", toEmail); 
	            
	            // Send an email containing the sharing link
                session.getServicesFactory().getNotifierService().sendLinkShareEmail(
                        toEmail, "MedCommons HealthURL Share Notification", accessUrl);
             }
            
            log.info("Successfully shared CCR with " + toEmail + " tracking number = " + ccr.getTrackingNumber());
        }
        catch (Exception e) {
            log.error("Failed to share PHR for account " + fromAccount + " to email " + toEmail + "using auth " + auth,e);
            throw e;
        }
        return new StreamingResolution("text/plain", result.toString());
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getToEmail() {
        return toEmail;
    }

    public String getPin() {
        return pin;
    }

    public void setFromAccount(String accid) {
        this.fromAccount = accid;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

}
