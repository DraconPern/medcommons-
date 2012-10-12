/*
 * $Id: AccountImportAction.java 3487 2009-09-11 07:20:28Z ssadedin $
 * Created on 13/06/2008
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.eqi;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.modules.transfer.DownloadFileAgent;
import net.medcommons.modules.transfer.UploadFileAgent;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRBuilder;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.api.ApplianceAPI;
import net.medcommons.router.api.AuthenticationResult;
import net.medcommons.router.api.InvalidCredentialsException;
import net.medcommons.router.oauth.api.APIException;
import net.medcommons.router.oauth.api.RemoteApplianceAPI;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.ccrmerge.DemoGraphicMatchResult;
import net.medcommons.router.services.ccrmerge.MergeException;
import net.medcommons.router.services.ccrmerge.MergePolicy;
import net.medcommons.router.services.ccrmerge.MergePolicyViolationException;
import net.medcommons.router.services.ccrmerge.PolicyResult;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.wado.AccountImportException;
import net.medcommons.router.services.wado.AccountImportStatus;
import net.medcommons.router.services.wado.ImportCancelledException;
import net.medcommons.router.services.wado.AccountImportStatus.State;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.oauth.OAuthAccessor;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontBind;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.ajax.JavaScriptResolution;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.json.JSONObject;

/**
 * Manages process of importing contents of one account
 * into another.
 * 
 * @author ssadedin
 */
@StrictBinding
public class AccountImportAction extends BaseActionBean implements ValidationErrorHandler {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AccountImportAction.class); 
    
    @Validate(required=true, on= {"!authenticate","!authenticateVoucher"}, mask=TOKEN_PATTERN)
    String sourceAuth;
    
    @Validate(required=false, mask=TOKEN_PATTERN)
    String sourceSecret;
    
    @Validate(required=true, on= {"!authenticateVoucher"}, mask="https{0,1}://.*")
    String sourceUrl;
    
    @Validate(required=false, mask=MCID_PATTERN)
    String toAccount;
    
    @Validate(required=true, on={"authenticate", "authenticateVoucher"})
    String password;
    
    @Validate(required=true, on= {"authenticateVoucher"})
    String voucherId;
    
    @Validate(required=false)
    boolean auto = false;
    
    /**
     * Status of the current import operation
     * <p>
     * Note this is stored in session and set in setContext()
     */
    AccountImportStatus status = null;
    
    @Override
    public void setContext(ActionBeanContext ctx) {
        super.setContext(ctx);
        this.status = (AccountImportStatus) ctx.getRequest().getSession().getAttribute("importStatus");
        if(this.status == null)
            ctx.getRequest().getSession().setAttribute("importStatus", status = new AccountImportStatus());
    }

    /**
     * Set to true if validation failed
     */
    boolean hasValidationErrors = false;
    
    /**
     * Whether or not to merge the contents of the remote account
     */
    @Validate(required=false)
    boolean merge = false;
    
    /**
     * Internal variable, parsed host of source HealthURL
     */
    private String healthURLHost;

    /**
     * Internal variable, parsed account id of source HealthURL
     */
    private String sourceAccount;
    
    /**
     * The CCR of the owner of the session, into whose account the 
     * content will be imported
     */
    private CCRDocument targetCCR;
    
    /**
     * Internal variable, source CCR as retrieved from remote appliance
     */
    private CCRDocument sourceCCR;
    
    /**
     * Result of testing source and target CCR for compatibility
     */
    private Map<String,String> mismatches = null;
    
    
    /**
     * Output parameter containing URL for imported CCR / Current CCR
     */
    private String resultURL;

    /**
     * Top level entry point.  Captures necessary information in session 
     * and then redirects to a top level page.  This cleans up 
     * the URL in the location bar and ensures that a browser refresh will not
     * cause issues.  If you are already targeting the content frame,
     * use the {@link #showConfirm} method.
     */
    @DefaultHandler
    public Resolution confirm() throws Exception {
        log.info("Beginning comfirmation of import of account " 
                  + sourceUrl + " into account " + getSession().getOwnerMedCommonsId() 
                  + " with remote auth " + sourceAuth + " and secret " + sourceSecret);
        initialize();
        return new RedirectResolution("/AccountImport.action?showConfirm");
    }

    private void initialize() throws PHRException, ServiceException, JDOMException, IOException, ParseException,
                    RepositoryException, MergeException, APIException {
        this.mismatches = new HashMap<String,String>();
        ApplianceAPI api = this.getApplianceApi();
        String ccrXML = api.getCCR(sourceAccount, "xml"); 
        
        sourceCCR = new CCRDocument(this.sourceAccount, ccrXML, CCRConstants.SCHEMA_VALIDATION_STRICT);
        
        this.targetCCR = session.getLogicalCCR(AccountDocumentType.CURRENTCCR);
        if(targetCCR != null) {
            MergePolicy policy = Configuration.getBean("mergePolicy");
            PolicyResult mergePolicyResult = policy.canMerge(sourceCCR, targetCCR);
            
            if(mergePolicyResult instanceof DemoGraphicMatchResult) {
                DemoGraphicMatchResult m = (DemoGraphicMatchResult) mergePolicyResult;
                for(String mismatch : m.getMismatches()) {
                    mismatches.put(mismatch,mismatch);
                }
                
                // Slight hack:  ease the logic on the front end by adding a single key when
                // there is a mismatch in either part of the name
                if(mismatches.containsKey("patientGivenName") || mismatches.containsKey("patientFamilyName")) {
                    mismatches.put("patientName", "patientName");
                }
            }
            
        }
        else { // No CCR - Do the matching as best we can on the account details
            AccountSettings settings = session.getAccountSettings();
            String targetFirstName = settings.getFirstName();
            String sourceGivenName = sourceCCR.getPatientGivenName();
            String sourceFamilyName = sourceCCR.getPatientFamilyName();
            if((!blank(targetFirstName) && !blank(sourceGivenName)) && !eqi(targetFirstName, sourceGivenName)) {
                mismatches.put("patientName", "patientName");
            }
            else 
            if((!blank(settings.getLastName()) && !blank(sourceFamilyName)) && !eqi(settings.getLastName(), sourceFamilyName)) {
                mismatches.put("patientName", "patientName");
            }
        }
        
        if(!this.mismatches.isEmpty()) {
            log.info("Found " + this.mismatches.size() + " mismatches in demographic information");
            
            // Do not let the import automatically start
            this.auto = false; 
        }
        
        ctx.getRequest().getSession().setAttribute("importActionBean",this);
    }
    
    public Resolution refresh() throws Exception {
        initialize();
        ctx.getRequest().getSession().setAttribute("importAction",this);
        return new ForwardResolution("/demographicMatchTable.ftl");
    }
    
    /**
     * Displays top level page with content frame set to display a confirmation 
     * screen allowing user to choose options and confirm 
     * they want to go ahead.
     */
    @DontValidate
    @DontBind
    public Resolution showConfirm() throws Exception {
        
        HttpSession httpSession = ctx.getRequest().getSession();
        AccountImportAction importActionBean = (AccountImportAction) httpSession.getAttribute("importActionBean");
        
        // We copy the reference from the first session instance in case the use refreshes
        // the browser.
        httpSession.setAttribute("importAction",importActionBean);
        httpSession.setAttribute("initialContentsUrl", "importAccount.ftl");
        httpSession.setAttribute("initialContents", "tab4"); 
        httpSession.setAttribute("baseUrl", ctx.getRequest().getAttribute("baseUrl"));  
        return new ForwardResolution("/platform.jsp");
    }
    
    public Resolution displayConfirm() throws Exception {
        initialize();
        return new ForwardResolution("/importAccount.ftl");
    }
    
    /**
     * Attempt to authenticate the user on the remote appliance
     * specified by source parameters.  
     * 
     * @return Javascript object with status = ok and token = authentication
     *          token if successful.
     */
    public Resolution authenticate() {
        HashMap<String,String> results = new HashMap<String, String>();
        try  {
            ApplianceAPI api = getApplianceApi();
            AuthenticationResult result = api.authenticate(this.sourceAccount, this.password);
            this.sourceAuth = result.getToken();
            this.sourceUrl = this.healthURLHost + result.getAccountId();
            results.put("status", "ok");
            results.put("token", sourceAuth);
            results.put("sourceUrl", sourceUrl);
        }
        catch(InvalidCredentialsException e) {
            log.warn("Failed to authenticate account from " + this.sourceUrl, e);
            results.put("status", "invalid credentials");
            results.put("error", getSession().getMessage("accountImport.auth.failed"));
        }
        catch(Exception e) {
            log.error("Failed to authenticate account from " + this.sourceUrl, e);
            results.put("status", "failed");
            results.put("error", e.getMessage());
        }
        return new JavaScriptResolution(results);
    }
    
    public Resolution authenticateVoucher() {
        HashMap<String, String> results = new HashMap<String, String>();
        try {
            // Locate the appliance for the voucher
            // this.healthURLHost = locateVoucherHost();
            this.healthURLHost = Configuration.getProperty("AccountsBaseUrl").replaceFirst("acct/.*$", "");
            
            log.info("Identified host " + this.healthURLHost + " for voucher " + voucherId);
            
            // Call the server to authenticate the voucher
            ApplianceAPI api = this.getApplianceApi();
            AuthenticationResult authResult = api.authenticate(voucherId, password);
            
            results.put("sourceUrl",this.healthURLHost + "/" + authResult.getAccountId());
            results.put("token",authResult.getToken());
            results.put("status","ok");
        }
        catch (Exception e) {
            log.error("Failed to authenticate voucher " + this.voucherId, e);
            results.put("status", "failed");
            results.put("error", e.getMessage());
        }
        return new JavaScriptResolution(results);
    }

    /**
     * This code is deprecated!
     * <p>
     * Attempts to find the voucher for a host by querying the global lookup service
     * that determines where the voucher lives.  We now no longer support the 
     * clustered configuration for vouchers and all vouchers live on the local
     * host.  This code is only of historical interest.
     * 
     * @deprecated
     * @return
     */
    protected String locateVoucherHost() throws ConfigurationException, UnsupportedEncodingException,
            IOException, MalformedURLException, ParseException {
        String serviceUrl = Configuration.getProperty("AccountsBaseUrl") + "/site/lookup_voucher_appliance.php";
        serviceUrl += "?voucherId=" + URLEncoder.encode(voucherId, "UTF-8");
        StringWriter resultStr = RESTUtil.fetchUrlResponse(serviceUrl);
        JSONObject result = new JSONObject(resultStr.toString());
        return result.getString("server");
    }
    
    /**
     * Do actual import of content of specified remote account 
     * into specified local account.
     * <p>
     * This is the main import operation and may be a long
     * running request that takes minutes.  The status can
     * be polled by using {@link #status()}, and the long
     * running transfer can be interrupted using {@link #cancel()}
     * 
     * @return Javascript with attribute status = ok if successful
     */
    public Resolution begin() {
        HashMap<String,String> results = new HashMap<String, String>();
        
        // In case there is a previous one in the session, clear flag
        status.setErrorFlag(false);
        status.setStatus(getSession().getMessage("accountImport.status.initializing"));
        
        File destination = null;
        try {
            results.put("status","ok"); 
            String targetAccount = Str.bvl(toAccount, getSession().getOwnerPrincipal().getMcId());
            if(blank(targetAccount) || Str.eq(targetAccount, ServiceConstants.PUBLIC_MEDCOMMONS_ID))
                throw new IllegalArgumentException("Parameter toAccount not specified and auth token does not resolve to valid MedCommons Account");
            
            log.info("Importing from " + this.sourceUrl  + " to local account " + targetAccount);
            
            ApplianceAPI api = getApplianceApi();
            
            status.setProgress(0.05f);
            status.setStatus(getSession().getMessage("accountImport.status.getccr"));
        
            // Resolve remote gateway
            String gw = api.findStorage(sourceAccount);
            
            checkCancelled();
            
            status.setProgress(0.10f);
            
            // Fetch the Current CCR
            String ccrXML = api.getCCR(sourceAccount, "xml");
            
            checkCancelled();
            
            status.setProgress(AccountImportStatus.PROGRESS_BEGIN_DOWNLOAD);
            status.setStatus(getSession().getMessage("accountImport.status.downloading"));
            status.setState(AccountImportStatus.State.DOWNLOADING);
            
            // We manufacture a unique import id
            String importId = String.valueOf(System.currentTimeMillis()) + String.valueOf(Math.round(Math.random()*1000));
            log.info("Import id for import is " + importId);
            destination = new File("data/Repository/"+targetAccount+"/import_" + importId + "/" + targetAccount + "/");
            
            downloadReferences(sourceAccount, gw, ccrXML, destination);
            
            // Debug:  sleep to see if strange failures are due to race conditions / timing dependent
            log.info("Beginning 5 second debug sleep");
            System.gc();
            Thread.sleep(5000);
            System.gc();
            log.info("Finished 5 second debug sleep");
            
            status.setProgress(AccountImportStatus.PROGRESS_BEGIN_UPLOAD);
            
            status.setStatus(getSession().getMessage("accountImport.status.importing"));
            status.setState(AccountImportStatus.State.UPLOADING);
            log.info("Import complete");
            
            // Now load them in
            String localCXPEndPoint = Configuration.getProperty("RemoteAccessAddress").replaceAll("/router/{0,1}$","/gateway/services/CXP2"); 
            UploadFileAgent uploadAgent = new UploadFileAgent(localCXPEndPoint, getSession().getAuthenticationToken(), targetAccount, sourceAccount, destination);
            uploadAgent.setPaymentBypass(getSession().getAuthenticationToken());
            status.setUploader(uploadAgent);
            
            
            checkCancelled();
            uploadAgent.upload();
            checkCancelled();
            status.setProgress(AccountImportStatus.PROGRESS_BEGIN_MERGE);
            status.setStatus(getSession().getMessage("accountImport.status.merging"));
            status.setState(AccountImportStatus.State.MERGING);
            
            // Change the patient id in the CCR, merge and save it
            mapAndStoreCCR(targetAccount, sourceAccount, ccrXML);
            
            status.setProgress(1.0f);
            status.setStatus(getSession().getMessage("accountImport.status.finished"));
            status.setState(AccountImportStatus.State.FINISHED);
            results.put("resultURL", this.resultURL);
        }
        catch(ImportCancelledException e) {
            setCancelledState();
        }
        catch(MergePolicyViolationException e) {
            log.warn("Failed to import account from " + this.sourceUrl + " failed due merge constraint violation", e);
            results.put("status", "merge failed");
            results.put("error", e.getMessage());
            status.setProgress(1.0f);
            status.setStatus(session.getMessage("accountImport.status.failed", e.getResult().format(session.getMessages())));
            status.setState(State.FAILED_MERGE);
            status.setErrorFlag(true);
        }
        catch(Exception e) {
            // Cancelling while in downloading state causes an actual stream exception
            // to occur because the CXPClient interrupts the stream to achieve the 
            // abort - hence we need to handle that here.
            if(status.getState() == State.CANCELLATION_REQUESTED) {
                setCancelledState();
            }
            else {
                log.error("Failed to import account from " + this.sourceUrl, e);
                results.put("status", "failed");
                results.put("error", e.getMessage());
                status.setProgress(1.0f);
                status.setStatus(getSession().getMessage("accountImport.status.failed", e.getMessage()));
                status.setErrorFlag(true);
            }
        }
        finally {
            if(destination != null && destination.exists()) {
                if(!FileUtils.deleteDir(destination.getParentFile())) { 
                    log.warn("Failed to delete temporary imported files"); 
                }
            }
        }
        return new JavaScriptResolution(results);
    }
    
    private void setCancelledState() {
        status.setProgress(1.0f);
        status.setState(AccountImportStatus.State.CANCELLED);
        status.setStatus(getSession().getMessage("accountImport.status.cancelled"));
        status.setErrorFlag(false);
    }
    
    /**
     * Check if the current import operation has been asynchronously
     * cancelled and if so, throw an exception to abort the current
     * call stack
     * 
     * @throws ImportCancelledException if the current import is in CANCELLATION_REQUESTED state
     */
    private void checkCancelled() throws ImportCancelledException {
        if(status.getState() == State.CANCELLATION_REQUESTED) 
            throw new ImportCancelledException();
    }

    /**
     * Cancels an in-progress account import.
     * <p>
     * Since imports are asysnchronous the import is not cancelled immediately,
     * rather the import is placed into a pending cancellation state and 
     * the background import process will notice the cancellation flag and
     * abort at the next opportunity.
     * <p>
     * To be sure that a cancellation has been effected a client should check
     * that the state in the session importStatus (see {@link AccountImportStatus})
     * has transitioned to CANCELLED
     * 
     * @return
     */
    @DontValidate
    public Resolution cancel() {
        HashMap<String,String> results = new HashMap<String, String>();
        if(status.getState() == State.DOWNLOADING && status.getDownloader() != null) {
            status.getDownloader().cancelStream();
        }
        status.setState(State.CANCELLATION_REQUESTED);
        status.setStatus(getSession().getMessage("accountImport.status.cancelling"));
        results.put("status","ok"); 
        return new JavaScriptResolution(results);
    }

    /**
     * Create and return an instance of the ApplianceAPI configured
     * to make calls to the remote appliance from which content
     * is being imported.
     */
    private ApplianceAPI getApplianceApi() {
        OAuthAccessor accessor = new OAuthAccessor(null);
        accessor.accessToken = this.sourceAuth;
        accessor.tokenSecret = this.sourceSecret;
        
        ApplianceAPI api = new RemoteApplianceAPI(accessor, healthURLHost);
        return api;
    }

    /**
     * Executes before all events and does pre-parsing of the sourceUrl to 
     * determine the account id and host parts.  This means all event handlers
     * can rely on the healthURLHost and sourceAccount instance variables being
     * set if the sourceUrl is present.
     */
    @Before(stages=LifecycleStage.EventHandling)
    public void parseHealthURL() {
        if(blank(sourceUrl))
            return;
            
        // Parse the health url
        Matcher sourceUrlParts = Pattern.compile("^(.*)/([0-9]{12,16})/{0,1}").matcher(sourceUrl);
        if(!sourceUrlParts.matches())
            throw new IllegalArgumentException("Bad format for source url " + sourceUrl);
        
        this.healthURLHost = sourceUrlParts.group(1);
        
        // We support the clean url form for tracking numbers
        // eg.  https://foo.bar.com/tracking/0123456789012345
        this.healthURLHost = this.healthURLHost.replaceFirst("/tracking$", "/");
            
        if(!this.healthURLHost.endsWith("/"))
            this.healthURLHost += "/";
            
        this.sourceAccount = sourceUrlParts.group(2);
    }

    /**
     * Download all references from the given CCR located on the remote
     * gateway specified by gw to the specified destination directory.
     * 
     * @param sourceAccount account to download from on remote gateway
     * @param gw            url of remote gateway
     * @param ccrXML        XML of CCR containing references to download
     * @param destination   directory to place files in 
     */
    private void downloadReferences(String sourceAccount, String gw, String ccrXML, final File destination)
                    throws JDOMException, IOException, Exception {
        // Enumerate the references in the CCR
        Document doc = new CCRBuilder().build(new StringReader(ccrXML));
        List<String> guids = extractReferenceGuids(doc);            
        
        log.info("Found " + guids.size() + " remote references in document: " + Str.join(guids,","));
        
        // Iterate references in remote CCR, accumulate guids
        String cxpEndPoint = gw.replaceAll("/router/{0,1}$","/gateway/services/CXP2");  
        
        // Use download file agent to import them
        destination.mkdirs();
        DownloadFileAgent downloader = new DownloadFileAgent(cxpEndPoint, sourceAccount, null, guids.toArray(new String[0]), destination) {
            @Override
            public File getDownloadFolder() {
                return destination;
            }
        };
        status.setDownloader(downloader); 
        downloader.download();
        
        checkCancelled();
    }
    

    /**
     * Store the given CCR in the target account, mapping any patient ids
     * to match the storage id
     * 
     * @param targetAccount storage id of account to store in, and to which to map patient ids
     * @param ccrXML  xml of CCR to store
     * @throws AccountImportException 
     * @throws MergePolicyViolationException 
     */
    private void mapAndStoreCCR(String targetAccount, String fromAccount, String ccrXML) throws AccountImportException, MergePolicyViolationException {
        try {
            checkCancelled();
            CCRDocument ccr = new CCRDocument(targetAccount, ccrXML, CCRConstants.SCHEMA_VALIDATION_STRICT);
            CCRElement actors = ccr.getRoot().getChild("Actors");
            for(CCRElement actor : actors.getChildren("Actor")) {
                CCRElement actorId = actor.queryProperty("actorMedCommonsId");
                if(actorId != null && fromAccount.equals(actorId.getTextTrim())) {
                    log.info("Replacing actor with id " + actorId.getText() + " with id " + targetAccount);
                    actorId.setText(targetAccount);
                }
                else {
                    if(actorId == null)
                        log.info("Actor mcid is null");
                    else
                        log.info("Actor id " + actorId.getTextTrim() + " does not match fromAccount " + fromAccount);
                }
                    
            }
            
            if(!targetAccount.equals(ccr.getPatientMedCommonsId()))
                log.warn("After replacing source account with target account, patient acount " + ccr.getPatientMedCommonsId() + " is different to target account " + targetAccount);
            
            ccr.setStorageId(targetAccount);
           
            // HACK: we need to stop the series from thinking their
            // storage id has been modified.
            // see StoreTransaction.java:462
            for(MCSeries s : ccr.getSeriesList()) {
                s.resetOriginalStorageId();
            }
            
            // Store the CCR
            StoreTransaction tx = getSession().tx(ccr);
            checkCancelled();
            tx.registerDocument(null);
            checkCancelled();
            tx.storeDocument();
            if(this.merge) {
                tx.merge();
                this.resultURL = "ccrs/" + targetAccount + "?c=ir&auth=" + session.getAuthenticationToken();
            }
            else {
                this.resultURL = "ccrs/" + targetAccount + "/" + tx.getDocumentGuid() + "?c=ir&auth=" + session.getAuthenticationToken();
            } 
            checkCancelled();
            tx.writeActivity(ActivityEventType.PHR_UPDATE, "Imported CCR from account " + fromAccount);
            
            checkCancelled();
            
            // If the account to which import is occurring had no existing Current CCR,
            // make the import be the Current CCR
            if(blank(this.session.getAccountSettings().getCurrentCcrGuid())) {
               tx.merge(); 
            }
            
            checkCancelled();
        }
        catch(AccountImportException e) { // don't double wrap
            throw e;
        }
        catch(MergePolicyViolationException e) { // don't double wrap
            throw e;
        }
        catch (Exception e) {
            throw new AccountImportException("Failed to map and store Current CCR into account " + targetAccount, e);
        }
    }

    /**
     * Extract guids for each MedCommons attachment reference in the CCR.
     * 
     * @param doc JDOM document of CCR XML
     * @return Array of guids found
     */
    private List<String> extractReferenceGuids(Document doc) throws JDOMException, IOException {
        final XPathCache xpath = (XPathCache) Configuration.getBean("ccrXPathCache");
        CCRElement refs = (CCRElement) xpath.getElement(doc,"references");
        ArrayList<String> guids = new ArrayList<String>();
        if(refs == null)
            return guids;
        
        long totalByteCount = 0; 
        for(CCRElement ref : refs.getChildren("Reference")) {
            String guid = xpath.getValue(ref,"referenceURL");
            if(!blank(guid) && guid.startsWith("mcid://")) {
                guids.add(guid.substring(7));
            }
            String size = xpath.getValue(ref,"referenceSize");
            log.info("reference " + Str.toString(ref) + " has size = " + size);
            if(size != null)
                totalByteCount += Long.parseLong(size);
        }
        this.status.setExpectedByteCount(totalByteCount);
        return guids;
    }
    
    /**
     * Return status of an ongoing transfer, if any
     */
    public Resolution status() {
        return new JavaScriptResolution(this.status, File.class, DownloadFileAgent.class, UploadFileAgent.class);
    }

    @Override
    public Resolution getSourcePageResolution() {
        return new ForwardResolution("/importAccount.ftl");
    }

    public String getSourceAuth() {
        return sourceAuth;
    }

    public void setSourceAuth(String sourceAuth) {
        this.sourceAuth = sourceAuth;
    }

    public String getSourceSecret() {
        return sourceSecret;
    }

    public void setSourceSecret(String sourceSecret) {
        this.sourceSecret = sourceSecret;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        this.hasValidationErrors = true;
        if("authenticate".equals(this.ctx.getEventName()) || "authenticateVoucher".equals(this.ctx.getEventName())) {
            JSONObject obj = new JSONObject();
            obj.put("status", "failed");
            obj.put("error", "invalid input for field " + errors.keySet().iterator().next());
            return new StreamingResolution("text/plain", obj.toString());
        }
        return null;
    }

    public boolean getHasValidationErrors() {
        return hasValidationErrors;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public synchronized String getPassword() {
        return password;
    }

    public synchronized void setPassword(String password) {
        this.password = password;
    }

    public synchronized boolean getMerge() {
        return merge;
    }

    public synchronized void setMerge(boolean merge) {
        this.merge = merge;
    }
    
    public synchronized String getHealthURLHost() {
        return healthURLHost;
    }

    public synchronized void setHealthURLHost(String healthURLHost) {
        this.healthURLHost = healthURLHost;
    }

    public synchronized String getSourceAccount() {
        return sourceAccount;
    }

    public synchronized void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public CCRDocument getSourceCCR() {
        return sourceCCR;
    }

    public void setSourceCCR(CCRDocument sourceCCR) {
        this.sourceCCR = sourceCCR;
    }

    public CCRDocument getTargetCCR() {
        return targetCCR;
    }

    public void setTargetCCR(CCRDocument targetCCR) {
        this.targetCCR = targetCCR;
    }

    public Map<String, String> getMismatches() {
        return mismatches;
    }

    public void setMismatches(Map<String, String> mismatches) {
        this.mismatches = mismatches;
    }

    public boolean getAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getResultURL() {
        return resultURL;
    }

    public void setResultURL(String resultURL) {
        this.resultURL = resultURL;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }
}
