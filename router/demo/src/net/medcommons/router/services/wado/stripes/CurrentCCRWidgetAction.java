/*
 * $Id$
 * Created on 11/08/2006
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.nvl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.AccountShare;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityLogService;
import net.medcommons.modules.services.interfaces.ActivitySession;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentIndexService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Algorithm;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.utils.Algorithm.Filter;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;

/**
 * Displays a widget showing information about a patient's account.
 * <p>
 * Supports an extended widget showing 4 separate widgets altogether with
 * combined info about the patient's account.
 * <p>
 * By default this action will attempt to figure out the Current CCR
 * <i>implicitly</i> - that is, it will check the current user's
 * session to find the active account id (either the owner of the desktop,
 * or the account id of the patient that is currently displayed by the
 * gateway).
 * <p>
 * However the "implicit" account id determination can be over-ridden
 * by supplying an 'accid' parameter.  If the 'accid' parameter is
 * provided then an 'auth' parameter should also be supplied containing
 * an auth token acquired from login with central server.  
 * 
 * @author ssadedin
 */
public class CurrentCCRWidgetAction extends CCRActionBean {
    
    private static final String WIDGET_RETURN_URL = "widgetReturnUrl";

    private AccountSettings patientSettings = null;
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CurrentCCRWidgetAction.class);
    
    /**
     * The return url, if any
     */
    private String returnUrl = null;
    
    /**
     * Optional account id that may be provided.  It will only be used if a current ccr cannot be
     * resolved via other methods
     */
    private String accid;
    
    /**
     * Specific guid to display: overrides automatic detection of current CCR, and use of accid
     */
    private String g;
    
    /**
     * Account id to access above specified guid as
     */
    private String a;
    
    /**
     * Auth token for provided accid (if any)
     */
    private String auth;
    
    /**
     * Adds margin around widget (in pixels)
     */
    private int margin = 0;
    
    /**
     * Whether to suppress patient demograhics header at top
     */
    private boolean noheader = false;
    
    private String cssClass;

    private CCRDocument currentCcr;
    
    /**
     * AccountSharingAction to which some actions are delegated
     */
    private AccountSharingAction accountSharingAction = new AccountSharingAction();
    
    
    private Map<String, String> rightsValues = accountSharingAction.getRightsValues();
    
    /**
     * Output - list of all sessions of activities for this user
     */
    private List<ActivitySession> activitySessions = new ArrayList<ActivitySession>(); 
    
    /**
     * Output - list of documents for this user
     */
    private List<DocumentDescriptor> documents;
    
    /**
     * Service used to query user documents
     */
    private DocumentIndexService indexService = Configuration.getBean("documentIndexService");
    
    /**
     * Fetches either the currently open CCR or the current user's Current CCR 
     * (if they have one) and renders it.
     * 
     * If no CCR available then redirects back to the return url provided.
     * 
     * @return
     * @throws Exception
     */
    @DefaultHandler
    public Resolution display() throws Exception {
        Resolution errorResult = initializeCCR();
        if(errorResult != null) {
            return errorResult;
        }
        // All must be ok - return result page
        return new ForwardResolution("/currentCCRWidget.jsp");
    }
    
    /**
     * Displays a combined widget that shows all the patient widgets
     * in one page.
     */
    public Resolution combined() throws Exception {
        
        long start = System.currentTimeMillis();
        
        // To show the combined widgets we must initialize them all
        
        // Current CCR
        Resolution errorResult = initializeCCR();
        if(errorResult != null) { // error
            return errorResult; 
        }
        
        // Activity Log
        initializeActivityLog();
        
        accountSharingAction.setContext(this.ctx); 
        ForwardResolution asaResult = (ForwardResolution) accountSharingAction.display(); // Ignore forward returned
        this.getContext().getRequest().setAttribute("sharingPage", asaResult.getPath());
        
        
        log.info("Rendering combined widget took " + (System.currentTimeMillis() - start));
        
        if(this.noheader)
            this.getContext().setAttribute("noheader", "true");
        
        
        if(!blank(cssClass))
            this.getContext().setAttribute("cssClass", cssClass);
         
        return new ForwardResolution("/patientInfo.jsp");
    }
    
    /**
     * Displays a combined widget that shows all the patient widgets
     * in one page.
     */
    public Resolution framedForms() throws Exception {
        
        // Current CCR
        Resolution errorResult = initializeCCR();
        if(errorResult != null) { // error
            return errorResult; 
        }
        
        return new ForwardResolution("/framedForms.jsp");
    }
    
    /**
     * Displays a combined widget that shows all the patient widgets
     * in one page.
     */
    public Resolution framedDocuments() throws Exception {
        
        // Current CCR
        Resolution errorResult = initializeCCR();
        if(errorResult != null) { // error
            return errorResult; 
        }
        
        this.documents = indexService.getDocuments(currentCcr.getPatientMedCommonsId(), null, null, null);
        
        this.documents = Algorithm.filter(documents, new Filter<DocumentDescriptor>() {
            public boolean $(DocumentDescriptor d) {
                return !DocumentTypes.DICOM_SERIES_MIME_TYPE.equals(d.getContentType());
            }
        });
        
        return new ForwardResolution("/framedDocuments.jsp");
    }
     
    
     /**
     * Fetches either the currently open CCR or the current user's Current CCR 
     * (if they have one) and renders updates for it.
     * @return
     */
    public Resolution displayActivity() throws Exception {
        Resolution errorResult = initializeCCR();
        if(errorResult!=null)
            return errorResult;    
        
        initializeActivityLog();
        
        return new ForwardResolution("/accountActivity.jsp");
    }
    
    
     /**
     * Fetches either the currently open CCR or the current user's Current CCR 
     * (if they have one) and renders updates for it.
     * @return
     */
    public Resolution miniActivity() throws Exception {
        Resolution errorResult = initializeCCR();
        if(errorResult!=null)
            return errorResult;    
        
        initializeActivityLog();
        
        return new ForwardResolution("/miniActivity.jsp");
    }
    
     /**
     * Fetches either the currently open CCR or the current user's Current CCR 
     * and shows an appropriate hipaa gadget form
     * @return
     */
    public Resolution forms() throws Exception {
        Resolution errorResult = initializeCCR();
        if(errorResult!=null)
            return errorResult;    
        
        return new ForwardResolution("/hipaaGadget.jsp");
    }
 
    /**
     * @throws PHRException
     * @throws IOException
     * @throws ServiceException
     */
    private void initializeActivityLog() throws PHRException, IOException, ServiceException {
        // Load the activity log
        if(this.currentCcr != null) {
	        String patientId = this.currentCcr.getPatientMedCommonsId();
	        if(this.session != null && !blank(patientId)) {
	            Collection<ActivityEvent> events = 
	                this.session.getServicesFactory().getActivityLogService().load(patientId, 0, ActivityLogService.READ_ALL);
	            
	            this.activitySessions = ActivitySession.fromEvents(events, ActivityAction.DEFAULT_ACTIVITY_SESSION_TIMEOUT_MS);
	            
	            this.ctx.getRequest().setAttribute("events", events);
	        }
        }
    }
    
    /**
     * Fetches either the currently open CCR or the current user's Current CCR 
     * (if they have one) and renders updates for it.
     * @return
     */
    public Resolution displayUpdates() throws Exception {
        Resolution errorResult = initializeCCR();
        if(errorResult!=null)
            return errorResult;    
        return new ForwardResolution("/access?displayUpdates&g="+this.currentCcr.getGuid());
    }
    
    /**
     * Attempts to resolve the currently open CCR or the user's current CCR and if found,
     * returns null.  If not found, returns a resolution corresponding to the
     * return redirect present in the session / input parameter. 
     * @throws PHRException 
     */
    private Resolution initializeCCR() throws ServiceException, ConfigurationException, RepositoryException, PHRException {
        if(!blank(returnUrl)) {
            this.ctx.getRequest().getSession().setAttribute(WIDGET_RETURN_URL, returnUrl);
        }  
        
        if(blank(this.auth) && !blank(session.getAuthenticationToken()))
            this.auth = session.getAuthenticationToken();
          
        log.info("Resolving CCR for desktop = " + session + " a=" + a + " g=" + g + " auth=" + auth + " accid=" + accid);
        
        // Ensure that an old desktop with conflicting auth cannot be used
        /*
        if(!blank(this.auth) && (session!=null) && !this.auth.equals(session.getAuthenticationToken())) { 
            log.info("Found existing auth = " + session.getAuthenticationToken() + " different to incoming auth = " + this.auth + ".  Cleaning desktop ...");
            this.session = UserSession.clean(this.ctx.getRequest(),this.a, this.auth);
        }
        */
         
        currentCcr = getDisplayCCR(); 
        if(currentCcr == null && !blank(this.g)) {
            log.info("Failed to resolve CCR implicitly: attempting  to use provided guid " + this.g);
            this.currentCcr = session.resolve(g);
        } 
        
        if(currentCcr != null) {
            this.ctx.getRequest().setAttribute("ccr", currentCcr);
            this.session.setActiveCCR(ctx.getRequest(), currentCcr); 
        
            String patientId = currentCcr.getPatientMedCommonsId();
            if(!blank(patientId)) {
                // Get the patient's settings so page can access them
                this.setPatientSettings(session.getAccountSettings(patientId));                
            }
        }
        else {
            log.info("No CCR resolved by current CCR widget.");
            String redirectUrl = nvl(returnUrl,(String) this.ctx.getRequest().getSession().getAttribute(WIDGET_RETURN_URL));
            if(!blank(redirectUrl)) {  
                log.info("no ccr available to display - redirecting to return url: " + redirectUrl);
                return new RedirectResolution(redirectUrl, false);
            }
            else 
                return new ForwardResolution("/noCurrentCCRFound.ftl");
        }
        return null;
    }
    
    public Resolution guid() throws Exception {
        HashMap<String, String> result = new HashMap<String, String>();
        CCRDocument displayCcr = getDisplayCCR();
        if(displayCcr != null) {
            result.put("accessGuid", displayCcr.getGuid());
        }
        else 
           result.put("accessGuid", null);
        return new JavaScriptResolution(result);
    }
    
    public Resolution patientIdUpdate() throws Exception {
        CCRDocument displayCcr = getDisplayCCR();
        String result = "var patientId = null;";
        if(displayCcr!=null) {
            result = "updatePatientId('" + displayCcr.getPatientMedCommonsId() + "');";
        }
        return new StreamingResolution("text/javascript",result);
    }
    

    /**
     * Attempts to load a CCR on which to base display of the widget
     * based on the input parameters to this action and the current
     * session. 
     * <p>
     * If there is an active session already with CCRs loaded then 
     * the active CCR from that session is queried for the patient id
     * and the Current CCR of that patient is loaded.
     * <p>
     * If there is no active session then the "g" parameter is
     * checked and if supplied used as a guid to try and load
     * a CCR to use.
     * <p>
     * If no guid is parameter is supplied then the account id
     * is checked and the Current CCR for the account is loaded.
     * <p>
     * If none of these works, null is returned.
     */
    protected CCRDocument getDisplayCCR() throws ServiceException, ConfigurationException, RepositoryException, PHRException {
        CCRDocument currentCcr = null;
        
        if(this.session != null) {
            log.debug("Showing current ccr widget for " + this.session.getOwnerMedCommonsId());

            // Figure out or fetch the patient's current ccr
            CCRDocument patientCcr = session.getCcrs().isEmpty() ? null : session.getCcrs().get(0);
            if(patientCcr != null) {
                String patientId = patientCcr.getPatientMedCommonsId();
                log.debug("first opened ccr is " + patientCcr.getGuid() + " patient is " + patientId);
                if(!Str.blank(patientId)) {
                    AccountSettings settings = session.getAccountSettings(patientId);
                    if(!Str.blank(settings.getCurrentCcrGuid())) {
                        // Get the current ccr for the patient
                        currentCcr = session.resolve(settings.getCurrentCcrGuid());
                        log.info("current ccr for " + patientId + " is " + (currentCcr!=null?currentCcr.getGuid():"null"));
                    }
                    else
                        log.info("Patient " + patientId + " does not have a current ccr set");
                }
            }
            
            // If no patient ccr, return whatever ccr they have loaded last
            // TODO: add access time to each ccr to track which one was last viewed
            if(currentCcr == null) {
                if(!session.getCcrs().isEmpty()) {
                    currentCcr = session.getCcrs().get(0);
                }
            }
        }
        
        // If no ccr found, see if a guid was provided for it
        if(currentCcr == null && !blank(this.g) && !blank(this.a) && !blank(this.auth)) {
            if(this.session == null)
                this.session = UserSession.clean(this.ctx.getRequest(), this.a, this.auth);
            this.currentCcr = session.resolve(this.g);
        }
        
        if(currentCcr == null) {
            // If no CCR found, see if there is an account id provided
            if(this.accid != null && this.auth != null) { 
                this.session = UserSession.clean(this.ctx.getRequest(), accid, this.auth);
                AccountSettings settings = session.getAccountSettings(accid);
                this.setPatientSettings(settings);                
                if(!Str.blank(settings.getCurrentCcrGuid())) {
                    // Get the current ccr for the patient
                    currentCcr = session.resolve(settings.getCurrentCcrGuid());
                    log.info("current ccr for " + accid + " is " + (currentCcr!=null?currentCcr.getGuid():"null"));
                }  
                else
                    log.info("Patient " + accid + " does not have a current ccr set");
             }
        }
        return currentCcr;
    }

    public AccountSettings getPatientSettings() {
        return patientSettings;
    }

    public void setPatientSettings(AccountSettings patientSettings) {
        this.patientSettings = patientSettings;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public CCRDocument getCurrentCcr() {
        return currentCcr;
    }

    public void setCurrentCcr(CCRDocument currentCcr) {
        this.currentCcr = currentCcr;
    }

    public String getAccid() {
        return accid;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
    
    public String getSharedRights() {
        return this.accountSharingAction.getSharedRights();
    }
    
    public ArrayList<AccountShare> getShares() {
        return this.accountSharingAction.getShares();
    }

    public Map<String, String> getRightsValues() {
        return rightsValues;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public List<ActivitySession> getActivitySessions() {
        return activitySessions;
    }

    public void setActivitySessions(List<ActivitySession> activitySessions) {
        this.activitySessions = activitySessions;
    }

    public List<DocumentDescriptor> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentDescriptor> documents) {
        this.documents = documents;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public boolean isNoheader() {
        return noheader;
    }

    public void setNoheader(boolean noheader) {
        this.noheader = noheader;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }
}
