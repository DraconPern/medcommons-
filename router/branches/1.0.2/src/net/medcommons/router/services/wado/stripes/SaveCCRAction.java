/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import java.util.HashMap;

import net.medcommons.router.services.wado.actions.NotificationForm;
import net.medcommons.router.services.wado.actions.SaveReplyAction;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;
import net.sourceforge.stripes.validation.Validate;

/**
 * Saves a CCR.  This is currently a front end for the 
 * existing Struts action does the real save.  This action 
 * performs validation and forwards on.
 * 
 * @author ssadedin
 */
public class SaveCCRAction extends CCRActionBean {

    //@Validate(converter=EmailTypeConverter.class)
    private String toEmail;
    
    private String trackingNumber = null;
    
    @Validate(required=false, mask="[0-9]{5}") 
    private String replyPin = null;
    
    /**
     * Optional format parameter - if set to "json" will return json result
     * instead of rendering status form.
     */
    private String fmt = "";
    
    /**
     * Next action to invoke
     */
    private String next=null;

    public SaveCCRAction() {
        super();
    }
     
    @DefaultHandler
    public Resolution save() throws Exception {                
        return new ForwardResolution("/saveReply.do");
    }
    
    public Resolution forward() {
        
        SaveReplyAction sra = new SaveReplyAction();         
        try {
            sra.saveCcr(this.ctx.getRequest());
        }
        catch(Exception e) {
            ctx.getRequest().setAttribute("sendStatus", "FAILED");
            ctx.getRequest().setAttribute("sendError", e.getMessage());
            return new ForwardResolution("/sendResult.jsp");
        }
        
        return new ForwardResolution(next);
     }
    
    public Resolution saveJson() throws Exception {                
        SaveReplyAction sra = new SaveReplyAction();         
        HashMap status = new HashMap();
        try {
            NotificationForm form = NotificationForm.get(this.ctx.getRequest());
            sra.saveCcr(form, this.ctx.getRequest(), true);
            status.put("status","ok");
            status.put("trackingNumber",form.getTrackingNumber());        
        }
        catch(Exception e) {
            status.put("status","failed");
            status.put("error",e.toString());                    
        }
        
        return new JavaScriptResolution(status);
    }
     
    public String getReplyPin() {
        return replyPin;
    }

    public void setReplyPin(String pin) {
        this.replyPin = pin;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

}
