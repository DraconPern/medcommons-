/*
 * $Id: ShareByPhoneAction.java 3124 2008-12-05 22:30:11Z ssadedin $
 * Created on 02/12/2008
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ExternalShare;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.router.web.stripes.JSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

/**
 * Shares access to a patient by sending a code to a phone
 * 
 * @author ssadedin
 */
public class ShareByPhoneAction extends JSONActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ShareByPhoneAction.class);
    
    @Validate(required=true, mask=MCID_PATTERN)
    String shareAccountId;
    
    @Validate(required=true, mask="[0-9]{10}")
    String phoneNumber;
    
    @Validate(required=true, mask="[a-z]{2,6}")
    String carrier;
    
    @Validate(required=true, mask="[a-zA-Z']{2,60}")
    String firstName;
    
    @Validate(required=true, mask="[a-zA-Z']{2,60}")
    String lastName;
    
    @DefaultHandler
    public Resolution share() throws Exception {
        
        log.debug("Sharing account with phone number: " + this.toString());
        
        ExternalShare share = new ExternalShare();
        share.setIdentity(this.phoneNumber);
        share.setIdentityType("Phone");
        share.setFirstName(this.firstName); 
        share.setLastName(this.lastName); 
        share.setAuth(this.session.getAuthenticationToken());
        
        ServicesFactory svc = this.session.getServicesFactory(); 
        svc.getActivityLogService().log(new ActivityEvent(ActivityEventType.CONSENT_UPDATE, 
                                        "Shared with Phone Number " + phoneNumber, 
                                        session.getOwnerPrincipal(), 
                                        shareAccountId, null,null));
        
        svc.getDocumentService().grantAccountAccess(shareAccountId, share, Rights.ALL);
        String accessCode = svc.getAccountService().createPhoneAccessCode(phoneNumber, carrier, shareAccountId);
        result.put("accessCode", accessCode); 
        
        return new StreamingResolution("text/plain", result.toString());
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getShareAccountId() {
        return shareAccountId;
    }

    public void setShareAccountId(String shareAccountId) {
        this.shareAccountId = shareAccountId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
