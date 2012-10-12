/*
 * $Id: TrackAction.java 3491 2009-09-21 12:39:05Z ssadedin $
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.router.services.wado.DocumentNotFoundException;
import net.medcommons.router.services.wado.InvalidCredentialsException;
import net.medcommons.router.services.wado.LoginFailedException;
import net.medcommons.router.services.wado.actions.LoginUtil;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

/**
 * Locates an displays a document by tracking number and PIN
 */
public class TrackAction extends BaseActionBean {

  /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(TrackAction.class);
	
	/**
	 * Tracking number submitted by user
	 */
	@Validate(required=true)
	private String trackingNumber = "";
	  
    /**
	 * PIN submitted by use
	 */
	@Validate(required=true)
	private String pin = "";
	
	/**
	 * Set to true if invalid credentials were supplied
	 */
	private boolean invalid = false;
	
	@DontValidate
	public Resolution show() {
	    return new ForwardResolution("/logon.jsp");
	}

	/**
	 * Method execute
	 * 
	 * @return ActionForward
	 * @throws
	 */
	@DefaultHandler
	public Resolution track() throws Exception {      
        
      String tn = getTrackingNumber().replaceAll(" ","");
      
      HttpServletRequest request = ctx.getRequest();
      try {
          
          CCRDocument ccr = LoginUtil.track(request, tn, PIN.hash(pin));
          
          request.setAttribute("cleanPatient", "true"); 
          session.setAccessPin(pin);

          if(ccr == null) 
              throw new DocumentNotFoundException();
              
          String patientId = ccr.getPatientMedCommonsId();
          if(!blank(patientId)) {
              ActivityEvent activity = 
                  new ActivityEvent(ActivityEventType.PHR_ACCESS, "PHR Accessed", 
                      session.getOwnerPrincipal(), patientId, tn,null);
              session.getServicesFactory().getActivityLogService().log(activity);
          }
          
          session.setActiveCCR(ctx.getRequest(), ccr);
          
          // Set flag to show quick reply button in UI
          // will be removed once shown
          ctx.getRequest().getSession().setAttribute("showQuickReply", "true");  
          
          // return new ForwardResolution("/viewEditCCR.do?mode=view"); 
          ctx.getRequest().setAttribute("view", "0e");
          return new ForwardResolution("/resetView.ftl"); 
      }
      catch(InvalidCredentialsException e) {
          return loginFailedResolution();
      }
      catch(LoginFailedException e) {
          return loginFailedResolution();
      }
      catch(Exception e) {
          log.error("Error attempting login for tn=" + tn,e);
          throw e;
      }
    }

    private Resolution loginFailedResolution() {
        log.info("Invalid tracking number " + trackingNumber);
        invalid = true;
        ctx.getRequest().setAttribute("message", session.getMessage("medcommons.invalidCredentials"));
	    return new ForwardResolution("/logon.jsp");
	}


    public String getTrackingNumber() {
        return trackingNumber;
    }


    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }


    public String getPin() {
        return pin;
    }


    public void setPin(String pin) {
        this.pin = pin;
    }


    public boolean isInvalid() {
        return invalid;
    }


    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }
}
    

