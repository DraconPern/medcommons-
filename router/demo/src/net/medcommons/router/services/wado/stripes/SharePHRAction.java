/*
 * $Id: SharePHRAction.java 3794 2010-08-06 07:08:09Z ssadedin $
 * Created on 05/05/2008
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.nvl;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.Str;
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
 * with a variety of possible controls on security.
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
    
    @Validate(required=true, converter=EmailTypeConverter.class)
    private String toEmail;
    
    @Validate(required=false,mask="[0-9]{5}")
    private String pin;
    
    @Validate(required=false,maxlength=120)
    private String subject;
    
    @Validate(required=false,maxlength=255)
    private String message;
    
    /**
     * The type of control over the privacy of the shared
     * document. One of 'pin', 'one', 'registrationn', or 'public'
     */
    @Validate(required=false)
    private String control;
    
    @Validate(required=false)
    private boolean sendEmail = true;
    
    DocumentService documentService;
    
    @DefaultHandler
    public Resolution share() throws Exception {
        
        String auth = this.session.getAuthenticationToken();
        try {
            
            documentService = session.getServicesFactory().getDocumentService();
            
            if(blank(fromAccount) && this.ccr != null)
                fromAccount = this.ccr.getPatientMedCommonsId();  
            
            // Check that we are allowed to share
            String currentCcrGuid;
            if(fromAccount != null) {
                if(!this.session.checkPermissions(fromAccount, "R")) 
                    throw new InsufficientPrivilegeException("Authorization token " + auth 
                            + " does not have read permission for account " + fromAccount);
                currentCcrGuid = session.getAccountSettings(fromAccount).getCurrentCcrGuid();
            }
            else { 
                fromAccount = ServiceConstants.PUBLIC_MEDCOMMONS_ID;
                currentCcrGuid = this.ccr.getGuid();
            }
             
            // Get user's current CCR
            
            if(blank(currentCcrGuid)) { 
                throw new IllegalArgumentException("Account " + fromAccount + " does not have a Current CCR");
            }
            
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
            TrackingAccessConstraint accessConstraint = determineAccessConstraint();
            tx.registerDocument(actualPin, accessConstraint);
            tx.storeDocument();
            tx.writeActivity(ActivityEventType.PHR_SEND, "PHR Shared with " + nvl(toEmail,"PIN"));
            
            for(String email : toEmail.split(",")) {
                Long esId = null;
                if("register".equals(control)) {
                    ExternalShare share = new ExternalShare();
                    share.setIdentity(email.trim());
                    share.setIdentityType(ExternalShare.IdentityType.Email);
                    share.setTrackingNumber(ccr.getTrackingNumber());
                    share.setAuth(session.getAuthenticationToken());
                    esId = documentService.grantAccountAccess(tx.getPatientAcctId(), share, Rights.ALL).getId();
                }
                
                // Return the tracking number
                result.put("trackingNumber", ccr.getTrackingNumber());
                
                if(!blank(pin)) { // send normal notification email
                    tx.sendEmail(null, null, subject, message);
                    result.put("pin", actualPin);
                }
                else { // no PIN - send different email
                    
                    // Create a share link and return it in the result
                    ShareCodec codec =new ShareCodec();
                    String code = codec.encode(ccr.getTrackingNumber(), actualPin, esId);
                    String accessUrl = Configuration.getProperty("RemoteAccessAddress")+"/share/"+code;
                    result.put("url", accessUrl);
                    result.put("to", email); 
                    
                    subject = Str.bvl(subject, "MedCommons HealthURL Share Notification");
                    
                    // Send an email containing the sharing link
                    if(sendEmail)
                        session.getServicesFactory().getNotifierService().sendLinkShareEmail(
                                        toEmail, subject, accessUrl, message);
             }
            }
            
            log.info("Successfully shared CCR with " + toEmail + " tracking number = " + ccr.getTrackingNumber() + " email = "+sendEmail);
        }
        catch (Exception e) {
            log.error("Failed to share PHR for account " + fromAccount + " to email " + toEmail + "using auth " + auth,e);
            throw e; 
        }
        return new StreamingResolution("text/plain", result.toString());
    }

    /**
     * Returns the access constraint for the tracking number created
     * by this sharing transaction.  For PIN shares or public shares
     * returns unlimited constraint;  for one time link shares returns
     * ONE_TIME constraint;  for registered email returns REGISTERED_EMAIL
     * constraint.
     */
    private TrackingAccessConstraint determineAccessConstraint() {
        if(!blank(pin))
            return TrackingAccessConstraint.UNLIMITED;
            
        if("public".equals(control))
            return TrackingAccessConstraint.UNLIMITED;
        
        if("register".equals(control))
            return TrackingAccessConstraint.REGISTERED_EMAIL;
            
        if("one".equals(control))
            return TrackingAccessConstraint.ONE_TIME;
        
        throw new IllegalArgumentException("Unknown sharing mode " + control);
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

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String comment) {
        this.message = comment;
    }

}
