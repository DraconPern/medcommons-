/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.empty;
import static net.medcommons.modules.utils.Str.eq;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.Voucher;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.CCRChangeElement;
import net.medcommons.router.services.repository.DocumentNotFoundException;
import net.medcommons.router.services.wado.AccessMode;
import net.medcommons.router.services.wado.actions.NotificationForm;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.ActionBeanContext;
import net.medcommons.router.web.stripes.RootForwardResolution;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;

import org.apache.log4j.Logger;
import org.apache.struts.util.MessageResources;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 * Provides external access into gateway to access a document. 
 * <p/>
 * The parameters are deliberately terse for this action since
 * it is designed to be called externally. 
 * <p/>
 * Access can be to either a tracking number or guid.  At least
 * one of these is required.  Authorization account is 
 * required also.  
 * 
 * @param t  - tracking number
 * @param g  - guid
 * @param a  - authorization account
 * @param at - authorization token - token as proof of auth account
 * @param l - logical type - if set, will load given logical document
 *              for patient specified by guid,tracking number instead of 
 *              the actual document sepcified by guid
 * @param p  - provisional account id
 * @param c  - display context, a legacy parameter that can switch on 
 *             certain behaviors such as emergency ccr display
 * @param m  - display mode, chooses between 'view' and 'edit' modes of
 *             the resulting CCR tab.
 * @author ssadedin
 */
@UrlBinding("/access")
public class AccessAction implements ActionBean, ValidationErrorHandler {
    
    public static final String ACCOUNT_MASK="([0-9]{4} *[0-9]{4} *[0-9]{4} *[0-9]{4} *)|(fbid://[0-9]{1,16}$)";
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AccessAction.class);
    
    /**
     * Tracking number
     */
    @Validate(required=false,mask="[0-9\\s]{12}")
    private String t = null;
    
    /**
     * Guid
     */
    @Validate(required=false,mask="(medcommons.*)|([0-9a-fA-F]{40})")
    private String g = null;
    
    /**
     * Account number for authorization
     */
    @Validate(on="access",required=false,mask=ACCOUNT_MASK)
    private String a = null;
    
    /**
     * Optional display mode - possible values are [ 'edit', 'view' ]
     * 
     * Note: changing this value will change the default view seen by people if no
     * 'm' parameter is passed in the URL.  
     */
    private String m = "view";
    
    /**
     * Optional context - used to tweak various aspects of the UI, eg. Emergency CCR 
     */
    private String c = "";
    
    /**
     * Provisional account id, if any
     */
    @Validate(on="provision",required=true, mask=ACCOUNT_MASK)
    private String p = "";
    
    /**
     * Authentication Token - provides evidence that user was authenticated at central
     */
    private String at = "";
    
    /**
     * Alternate parameter for authentication token 
     */
    private String auth = "";
    
    /**
     * Logical type - optional, logical document type to load
     */
    private String l = "";
            
    /**
     * XPath describing the part of the CCR to return
     */
    private String xp = null;
    
    /**
     * If set to non-null, will cause output to be in JSON form
     */
    private String json = null;
    
    /**
     * If set to non-null, will cause output to be in XML form
     */
    private String xml = null;
    
    /**
     * Format parameter - "json" or "xml" are recognized values,
     * note they can also be set as separate parameters, see above.
     */
    private String fmt = null;
    
    /**
     * Desktop initialized by this action
     */
    protected UserSession session = null;
    
    /**
     * CCR Loaded by this action (if any)
     */
    protected CCRDocument ccr = null;
    
    /**
     * Context of this action
     */
    protected ActionBeanContext ctx;
    
    /**
     * Access the given document from external site, creates new Desktop
     * @return
     */
    @DefaultHandler
    public Resolution access() throws Exception {
        try {
            // HACK:  even with the <base> tag, IE still tries to query some paths
            // under the non-rebased URL.  This can result in us coming here for images!
            if(!blank(ctx.getRequest().getPathInfo()) && ctx.getRequest().getPathInfo().indexOf("/images/")>=0) { 
                return null;
            }
            
            if(blank(at))
                at = auth;
            
            this.session = UserSession.clean(this.ctx.getRequest(), this.a, this.at);
            
            Resolution errorResolution = this.loadCcr();   
            if(errorResolution != null) {
                ctx.getResponse().setStatus(500);
                return errorResolution;
            }
            
            if(ccr != null) { 
                String patientId = ccr.getPatientMedCommonsId();
                if(!blank(patientId)) {
                    session.getServicesFactory().getActivityLogService().log(
                                    new ActivityEvent(ActivityEventType.PHR_ACCESS, "PHR Accessed", session.getOwnerPrincipal(), patientId, ccr.getTrackingNumber(), null));
                }
            }
            
            return getResolution();        
        }
        catch(Exception e) {
            ctx.getResponse().setStatus(500);
            throw e;
        }
    }

    /**
     * @return
     * @throws ServiceException 
     */
    private Resolution getResolution() throws PHRException, IOException, ServiceException {
        if(m==null){
            m = "view";
        }
        
        if(c=="null") 
            c = "";
        
        if("v".equals(c)) {
            this.session.setAccessMode(AccessMode.PATIENT);
        }
        else {
            String patientId = ccr.getPatientMedCommonsId();
            if(!empty(patientId) && !eq(patientId,session.getOwnerMedCommonsId())) {
                AccountSettings patientSettings = session.getAccountSettings(patientId);
                Voucher voucher = patientSettings.getVoucher();
                if(voucher != null && voucher.getProviderAccountId().equals(session.getOwnerMedCommonsId()) && eq("issued",voucher.getStatus())) {
                    this.session.setAccessMode(AccessMode.INCOMPLETE_VOUCHER);
                }  
            }
        }
        
        if("ir".equals(c)) {
            this.session.setAccessMode(AccessMode.ACCOUNT_IMPORT_RESULT);
        }
        
        this.session.setAccessGuid(g);
        this.ctx.getRequest().setAttribute("initialContentsUrl","viewEditCCR.do?ccrIndex=0&mode="+m+"&clean=true&displayMode="+c);
        this.ctx.getRequest().getSession().setAttribute("notificationForm", new NotificationForm());
        
        if(!blank(this.xp)) {
            CCRElement e = this.ccr.getJDOMDocument().queryProperty(XPathCache.addNamespaces(xp));
            if(e != null) {
                if(this.json== null) {
                    Document d = new Document();
                    d.setRootElement((Element) e.clone());
                    ctx.getResponse().setContentType("text/xml");
                    return new StreamingResolution("text/xml", Str.toString(d));
                }
                else {
                    return new StreamingResolution("text/javascript", e.getJSON());
                }
            }
            else {
                ctx.getResponse().sendError(404, xp);
                return new StreamingResolution("text/plain", "Requested Content Not Found in Target Object");
            }
        }
        else
        if(xml != null) {
            return new StreamingResolution("text/xml", Str.toString(this.ccr.getJDOMDocument()));
        }
        else
        if(json != null)
            return new StreamingResolution("text/javascript", this.ccr.getRoot().getJSON());
        
            
        return new ForwardResolution("/platform.jsp");
    }
    
    public Resolution widget() throws Exception {
        this.session = UserSession.clean(this.ctx.getRequest(), this.a, this.at);
        this.session.setAccessGuid(g);
        Resolution errorResolution = this.loadCcr();   
        if(errorResolution != null)
            return errorResolution;
        return new ForwardResolution(CurrentCCRWidgetAction.class);
    }
    
    /**
     * Provisioning adds the provisional id to the CCR as the patient MedCommons Account id
     * and flags that the content will be moved out of provisional status when the
     * document is saved. 
     */
    public Resolution provision() throws Exception {
        
        this.session = UserSession.clean(this.ctx.getRequest(), this.a, this.at);
        
        Resolution errorResolution = this.loadCcr();   
        if(errorResolution != null)
            return errorResolution; 
        
        // Add patient id
        if(this.ccr.getPatientMedCommonsId()==null) {
            this.ccr.addPatientId(this.p, CCRConstants.MEDCOMMONS_PATIENT_ID_TYPE);
        }
        Resolution successResolution =  this.getResolution();
        this.ccr.setGuid(null);  // Since we modified the CCR, set it's guid to null      
        this.ccr.setProvisionalCcr(true);
        return successResolution;
    }

    
    private Resolution loadCcr() {
        return loadCcr(true);
    }
    
    /**
     * Attempt to load the CCR specified by parameters a,g,t,lt and set as the "ccr"
     * attribute.   Optionally, return a validation resolution if the CCR cannot
     * be located, determined by the failIfNotFound flag.
     * <p>
     * If the guid {@link #g} is blank then an attempt to resolve the guid 
     * will be made by first using the tracking number {@link #t} and if
     * that is unsuccessful, the Current CCR of the requested account
     * {@link #a} will be queried and returned.
     * 
     * @param failIfNotFound - if set, return resolution for not found failures
     * @return
     */
    private Resolution loadCcr(boolean failIfNotFound) {
        
        // For complicated reasons these are sometimes not set by stripes (argh!)
        if(this.json == null)
            this.json = ctx.getRequest().getParameter("json");
        if(this.xml == null)
            this.xml = ctx.getRequest().getParameter("xml");
        
        try {
            // If tracking number provided, translate it to guid
            if(blank(g) &&  !blank(t)) {
                this.g = session.getServicesFactory().getTrackingService().queryGuid(t);
            }
            
            // If no guid but an account, the guid is the current ccr of the account
            if(blank(g) && !blank(a)) 
                this.g = session.getAccountSettings(this.a).getCurrentCcrGuid();
            
            this.ccr = session.resolve(this.g);
            
            if(ccr == null) {
                if(failIfNotFound) {
                    // If we are displaying a GUI, show the error page, 
                    // otherwise throw a proper error
                    if(this.json == null && this.xml == null)
                        return validationResolution("access.documentNotFound");
                    else
                        throw new DocumentNotFoundException();
                }
                else
                    return null;
            }
            
            String patientId = ccr.getPatientMedCommonsId();
            
            if(!blank(patientId)) {
                session.setRssId(patientId);
            }
            
            // Check if we are actually meant to load a logical CCR
            if(!blank(l)) { // logical type provided - try to resolve it
                log.info("Attempting to resolve logical document for patient " + patientId + " extracted from CCR " + ccr.getGuid());
                if(blank(patientId))
                    return failIfNotFound ? validationResolution("access.documentTypeNotFound") : null;

                // Get the logical document for that type
                String ltGuid = session.getAccountSettings(patientId).getAccountDocuments().get(AccountDocumentType.valueOf(l));
                if(ltGuid == null) { // No such document ?!
                    log.info("Document of type " + l + " not found for patient " + patientId + " (Accessed by user " + a + ")");
                    this.ccr = null;
                    return failIfNotFound ? validationResolution("access.documentTypeNotFound") : null;
                }

                log.info("Resolved guid " + ltGuid + " for document type " + l + " for patient " + patientId);
                this.ccr = this.session.resolve(ltGuid);
                if(this.ccr == null) {
                    return failIfNotFound ? validationResolution("access.documentNotFound") : null;
                }
            }
            
            if(!blank(t)) {
                ccr.setTrackingNumber(t);
            }
           
            session.setActiveCCR(this.ctx.getRequest(), ccr);
            
            if(!blank(ccr.getPatientMedCommonsId())) {
	            ctx.getRequest().setAttribute("expiryDate",session.getAccountSettings(ccr.getPatientMedCommonsId()).getExpiryDate());
            }
            return null;
        }
        catch(DocumentNotFoundException ex) {
            log.error("CCR " + this.g + " not found",ex);
            return new RootForwardResolution("/wadoError.jsp",HttpServletResponse.SC_NOT_FOUND);
        }
        catch(Throwable t) {
            log.error("Error while loading ccr " + this.g,t);
            this.ctx.getRequest().setAttribute("org.apache.struts.action.EXCEPTION", t);            
            return new RootForwardResolution("/wadoError.jsp",HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Set given message as a validation error and return appropriate validation failure resolution. 
     */
    private Resolution validationResolution(String messageResource) {
        ValidationErrors errors = new ValidationErrors();
        errors.add( "error", new LocalizableError(messageResource) );
        getContext().setValidationErrors(errors);
        return this.ctx.getSourcePageResolution();
    }

    /**
     * Load a document into the existing Desktop
     * 
     * @return
     * @throws ServiceException 
     */
    public Resolution load() throws Exception {
        this.session = UserSession.get(ctx.getRequest());
        if(blank(g) && !blank(l)) {
            this.ccr = session.getCcrs().get(session.getLastAccessedCcr());
            this.g = session.getAccountSettings(ccr.getPatientMedCommonsId()).getAccountDocuments().get(AccountDocumentType.valueOf(l));
        }
        Resolution errorResolution = this.loadCcr();   
        if(errorResolution != null)
            return errorResolution;        
        
        return new ForwardResolution("viewEditCCR.do?mode="+this.m);
    }
    
    /**
     * Same as load, but instead of returning an error page if the document is not found,
     * returns a screen offering logon
     * 
     * @return
     * @throws ServiceException 
     */
    public Resolution tryload() throws Exception {
        this.session = UserSession.get(this.ctx.getRequest());
        
        if(blank(g) && blank(t) && !blank(l)) {
            CCRDocument referenceCCR = session.getCcrs().get(session.getLastAccessedCcr());
            String storageId = referenceCCR.getPatientMedCommonsId();
            if (blank(storageId)){
                throw new NullPointerException("CCR has a null MedCommons ID");
            }
            this.g = session.getAccountSettings(referenceCCR.getPatientMedCommonsId()).getAccountDocuments().get(AccountDocumentType.valueOf(l));
        }
  
        Resolution errorResolution = this.loadCcr(false /* don't fail if not found */);   
        if(errorResolution != null)
            return errorResolution;        
        
        if(this.ccr != null)
            return new ForwardResolution("viewEditCCR.do?mode=view");
        else {
           this.ctx.getRequest().setAttribute("trackingNumber", this.t); 
           String msg = "access.enterAuthTnPin";
           if(!Str.blank(this.t)) {
               msg = "access.enterAuthPin";               
           }
           this.ctx.getRequest().setAttribute("message",
               MessageResources.getMessageResources("net.medcommons.ApplicationResources").getMessage(msg));
           return new ForwardResolution("/logon.jsp");
        }
    }
    
    public Resolution displayUpdates() throws ServiceException {
        this.session = UserSession.get(this.ctx.getRequest(),this.a, this.at);
        Resolution errorResolution = this.loadCcr();   
        if(errorResolution != null)
            return errorResolution;        
        
        // Get the updates sorted in correct order
        TreeSet<CCRChangeElement> changes = new TreeSet<CCRChangeElement>(new Comparator<CCRChangeElement>() {
            public int compare(CCRChangeElement o1, CCRChangeElement o2) {
                try {
	                return o2.getDateTime().compareTo(o1.getDateTime());
                }
                catch(Exception e) {
                    log.warn("Failed to parse CCR change date " + o1.getChildText("DateTime") + " or " + o2.getChildText("DateTime"));
                    return 0;
                }
            }
        }); 
        if(this.ccr.getChangeHistory()!=null) {
            for (Iterator iter = this.ccr.getChangeHistory().getDescendants(new ElementFilter("ChangeSet")); iter.hasNext();) {     
                changes.add((CCRChangeElement) iter.next());
            }
        }
        this.ctx.getRequest().setAttribute("changes", changes);
        
        return new ForwardResolution("/ccrUpdates.jsp");
    }
    
    
    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        if(!blank(fmt)) {
            // Figure out at least one of the invalid fields
            String msg = errors.entrySet().iterator().next().getValue().get(0).getMessage(this.ctx.getLocale());
            this.ctx.getResponse().setStatus(400, msg);
        }
        return null;
    }
 
    public void setContext(net.sourceforge.stripes.action.ActionBeanContext ctx) {
        this.ctx = (ActionBeanContext) ctx;
        this.ctx.setSourcePageResolution(new RootForwardResolution("wadoError.jsp"));
        
        try {
            // Carry the authentication from previous desktop, if it has one
            if(UserSession.has(ctx.getRequest())) {
                this.at = Str.bvl(this.at, UserSession.get(ctx.getRequest()).getAuthenticationToken());
            }
            
            if(ctx.getRequest().getAttribute("oauth_token")!=null) {
                this.at = (String) ctx.getRequest().getAttribute("oauth_token");
            }
        }
        catch (ServiceException e) {
            throw new RuntimeException("Unable to create user session", e);
        }
    }

    public ActionBeanContext getContext() {
        return ctx;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getM() {
        return m;
    }

    public void setM(String mode) {
        this.m = mode;
    }

    public String getC() {
        return c;
    }

    public void setC(String displayContext) {
        this.c = displayContext;
    }

    public CCRDocument getCcr() {
        return ccr;
    }

    public String getAt() {
        return at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getL() {
        return l;
    }

    public void setL(String l) {
        this.l = l;
    }

    public String getXp() {
        return xp;
    }

    public void setXp(String xp) {
        this.xp = xp;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getFmt() {
        return fmt;
    }

    public void setFmt(String fmt) {
        if("xml".equals(fmt))
            this.xml = "true";
        
        if("json".equals(fmt))
            this.json = "true";
        
        this.fmt = fmt;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

}
