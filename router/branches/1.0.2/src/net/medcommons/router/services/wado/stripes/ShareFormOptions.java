package net.medcommons.router.services.wado.stripes;

import static net.medcommons.router.web.stripes.BaseActionBean.MCID_PATTERN;
import net.medcommons.modules.services.interfaces.CustomFields;
import net.sourceforge.stripes.action.ImportProperties;
import net.sourceforge.stripes.validation.Validate;

/**
 * Parameters that may / must be passed in when invoking
 * the 'quick' sharing forms.
 * 
 * @author ssadedin
 */
public class ShareFormOptions {
    
    @Validate(required=true, mask=MCID_PATTERN)
    String patientId;
    
    String to;
    
    String subject;
    
    String message;
    
    String next;
    
    @ImportProperties
    CustomFields customFields;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CustomFields getCustomFields() {
        return customFields;
    }

    public void setCustomFields(CustomFields customFields) {
        this.customFields = customFields;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
