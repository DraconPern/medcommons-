/*
 * $Id: NotificationForm.java 2687 2008-06-27 03:54:05Z ssadedin $
 * Created on 17/03/2005
 */
package net.medcommons.router.services.wado.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.services.xds.consumer.web.InvalidCCRException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.util.struts.JDomForm;
import net.sourceforge.stripes.action.ActionBeanContext;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

/** * NotificationForm receives information for final confirmation of * a referral.  It confirms the toEmail addresses and patient information * to be usd for the referral. *  * @author ssadedin */
public class NotificationForm extends ActionForm implements JDomForm {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(NotificationForm.class);

    /**
     * Stripes context - used when this form is populated by stripes.
     */
    private ActionBeanContext ctx = null;
    
    /**
     * Tracking number for the referral
     */
    private String trackingNumber;
    
   /**
     * PIN that may be used for accessing the referral
     */
    private String pin;

    /**
     * The JDOM Document holding the CCR
     * 
     * @uml.property name="ccr"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private CCRDocument ccr = null;
    
    
    /**
     * Date of Birth which may be submitted by user
     */
    private String dateOfBirth;
    
    /**
     * Set to true if a new patient is created for this notification
     */
    private boolean isNewMedcommonsPatient = false;

    private UserSession desktop;
    
    /**
     * Set to true when this form is modified by struts
     */
    private boolean modified;
    
    /**
     * Set to true when the terms of use box has been checked
     */
    private boolean termsOfUse;

    /**
     * Creates a NotificationForm
     */
    public NotificationForm() {
        super();        
    }

    /**
     * 
     * @uml.property name="pin"
     */
    public String getPin() {
        return pin;
    }

    /**
     * 
     * @uml.property name="pin"
     */
    public void setPin(String pin) {
        this.pin = pin;
    }

    /**
     * 
     * @uml.property name="trackingNumber"
     */
    public String getTrackingNumber() {
        return trackingNumber;
    }

    /**
     * 
     * @uml.property name="trackingNumber"
     */
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }


    public boolean getNewMedcommonsPatient() {
        return isNewMedcommonsPatient;
    }

    /**
     * 
     * @uml.property name="isNewMedcommonsPatient"
     */
    public void setNewMedcommonsPatient(boolean isNewMedcommonsPatient) {
        this.isNewMedcommonsPatient = isNewMedcommonsPatient;
    }

    /**
     * returns the JDOM Document containing data for this form
     * @throws IOException
     * @throws JDOMException
     * @throws CCROperationException 
     * @throws PHRException 
     * @throws RepositoryException 
     */
    public XMLPHRDocument getDocument(HttpServletRequest request) throws CCROperationException, PHRException {
        try {
            desktop = UserSession.get(request);
            if(desktop==null)
                return null;
            
            this.ccr = desktop.getCurrentCcr(request);

            if(this.ccr != null)
                return this.ccr.getJDOMDocument();        
            else
                return null;
        }
        catch (ServiceException e) {
            throw new PHRException(e);
        }
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        
       return null;
    }
    
    /**
     * Returns the default namespace for this form
     */
    public Namespace getNamespace() {
        return Namespace.getNamespace("x","urn:astm-org:CCR");
    }

    /**
     * 
     * @uml.property name="ccr"
     */
    public CCRDocument getCcr() {
        return ccr;
    }

    /**
     * 
     * @uml.property name="dateOfBirth"
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @uml.property name="dateOfBirth"
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Element createPath(String path) throws PHRException {
        return ccr.createPath(path);
    }
    
    public void setReplyPin(String pin) {
        if(desktop == null)
            return;
        desktop.setReplyPin(pin);        
    }

    public void setModified(HttpServletRequest request) throws JDOMException, IOException {
        if(!this.modified) {
	        this.modified = true;
        }
    }
    
    public void setPatientDateOfBirth(String dob) throws CCROperationException, InvalidCCRException, PHRException {
        if(this.ccr != null)
	        this.ccr.setPatientDateOfBirth(dob);
    }

    public boolean isTermsOfUse() {
        return termsOfUse;
    }

    public void setTermsOfUse(boolean termsOfUse) {
        this.termsOfUse = termsOfUse;
    }
    
    public void setSourceEmails(String from) throws PHRException {
        if(this.ccr != null) {
            CCRElement parent = this.ccr.getRoot().getOrCreate("From");
            this.ccr.setActorLinks(parent,from);
        }
    }
    
    public void setToEmails(String to) throws PHRException {
        if(this.ccr != null) {
            CCRElement parent = this.ccr.getRoot().getOrCreate("To");
            this.ccr.setActorLinks(parent,to);
        }
    }

    public void setContext(ActionBeanContext ctx) {
        this.ctx = ctx;
    }

    public ActionBeanContext getContext() {
        return this.ctx;
    }
    
    /**
     * Return the session instance of the notification form.
     * <p>
     * Note: not thread safe.  Whole NotificationForm should be removed.
     * @throws ServiceException 
     */
    public static NotificationForm get(HttpServletRequest req) throws ServiceException {
        NotificationForm form = (NotificationForm)req.getSession().getAttribute("notificationForm");
        if(form == null) {
            req.getSession().setAttribute("notificationForm", new NotificationForm());
            return get(req);
        }
        form.ccr = UserSession.get(req).getCurrentCcr(req);
        return form;
    }
}
