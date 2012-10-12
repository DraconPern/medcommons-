/*
 * $Id$
 * Created on 13/07/2005
 */
package net.medcommons.router.services;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.phr.ccr.CCRElementFactory.el;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.xml.MultipleElementException;
import net.medcommons.modules.xml.XPathUtils;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRActorElement;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.ccr.CCRStoreException;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.repository.DocumentResolver;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.wado.AccessMode;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.services.wado.utils.AccountUtil;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.apache.struts.util.MessageResources;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

/**
 * UserSession is kept at the top level in the user's session to model all the objects
 * associated with their session.
 * <p>
 * A UserSession consists of a list of CCRs that may be in various states
 * being operated on by the user.  For example, a user can edit several
 * CCRs simultaneously without saving them.
 * 
 * @author ssadedin
 */
@SuppressWarnings("serial")
public class UserSession implements Serializable, HttpSessionBindingListener {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(UserSession.class);
    
    /**
     * All UserSessions
     */
    private static Set<UserSession> allDesktops = Collections.synchronizedSet(new HashSet<UserSession>());
    
    /**
     * The list of CCR's belonging to this session
     */
    private List<CCRDocument> ccrs = new ArrayList<CCRDocument>();
    
    /**
     * Cache of known rights for this user
     */
    private Hashtable<String, EnumSet<Rights> > rightsCache = new Hashtable<String, EnumSet<Rights> >();

    /**
     * MedCommons ID of the owner of the session
     */
    private String ownerMedCommonsId;
    
    /**
     * The identity under which access to this UserSession was authenticated.
     * In most cases, this is a medcommons id, but it may also be
     * an OpenID.  This identity is the identity used to authenticate 
     * access to the resources accessed by the session.
     */
    private AccountSpec ownerPrincipal;
    
    /**
     * Identity of the user controlling the session.  This is usually the
     * same as the ownerPrincipal, but in some cases it is different, 
     * eg. when a user with an account uses a tracking number and PIN
     * to access a CCR.  Then the owner auth comes from the tracking number
     * and PIN, but the context auth comes from the user's session.
     */
    private AccountSpec contextPrincipal;
    
    
    /**
     * Auth token associated with the contextPrincipal
     */
    private String contextAuth;
    
    /**
     * Tracking number originally used to access this session (if any).
     */
    private TrackingReference accessTrackingReference;    
    
    /**
     * The PIN that was used to access this session, if any
     */
    private String accessPin;
    
    /**
     * The guid that was accessed by using the accessPin
     */
    private String accessGuid;
    
    /**
     * PIN that will be applied to reply CCRs.  Default is copied
     * from the accessPin, but then it is remembered for the session.
     */
    private String replyPin;
    
    /**
     * Returns the factory to use for accessing services
     */
    private ServicesFactory factory = null;
    
    /**
     * Tracking numbers that are known to be in the session owners account
     * For example, after adding a CCR to their account, the track# will be
     * put in here.  Similarly, if a user loads their session via a specific
     * CCR from their account page then the track# will be put in here.
     * 
     * This is used as a UI optimization to prevent displaying options like
     * "Add to Account" when in fact the tracking number concerned is already
     * in their account.
     */
    private Set<String> accountTrackingNumbers = new HashSet<String>();

    /**
     * Cached settings retrieved from central for this account
     */
    private AccountSettings accountSettings;
    
    /**
     * Cache of settings for other accounts than the owner of the session
     */
    private Map<String, AccountSettings> settingsCache;

    /**
     * Cached todir entries for this account
     */
    private List<DirectoryEntry> toDir;
    
    /**
     * The most recently accessed / updated CCR in the session
     */
    private int lastAccessedCcr;
    
    /**
     * When this Desktop was created 
     */
    private long createDateTimeMs = System.currentTimeMillis();
    
    /**
     * The authorization token owned by this gateway
     */
    private String authenticationToken;
    
    /**
     * Access mode of this session - affects options in display and behavior
     */
    private AccessMode accessMode = AccessMode.DOCTOR;
    
    /**
     * Account ID of which to display RSS icon, if any
     */
    private String rssId;
    
    /**
     * Session id of this session
     */
    private String sessionId;
    
    /**
     * For unit tests only!
     * @deprecated
     */
    public UserSession() {
        
    }
    
    /**
     * Creates an empty session for given ownerMcId and given auth token
     * 
     * @param ownerMcId
     * @param authenticationToken
     */
    public UserSession(String ownerMcId, String authenticationToken) {
        this(ownerMcId, authenticationToken,null);
    }
    
    /**
     * Creates a Desktop based on the given list of CCRs and owner id.  If the owner
     * id is null then it will be set as POPS_MEDCOMMONS_ID.
     */
    public UserSession(String ownerMcId, String authenticationToken, List<CCRDocument> ccrs) {
        super();
       
        log.info("Creating new session for owner " + ownerMcId + " with authentication token " + authenticationToken);
        this.ownerMedCommonsId = ownerMcId;
        if(ownerMedCommonsId == null)
            this.ownerMedCommonsId = ServiceConstants.PUBLIC_MEDCOMMONS_ID;
        
        this.authenticationToken = authenticationToken;
        if(this.authenticationToken == null) {
            this.authenticationToken = "";
        }
        
        if(ccrs!=null) {
            for (CCRDocument ccr : ccrs) {
                this.ccrs.add(ccr);
            }        
        }
    }
    
    /**
     * Returns true if the owner of the session has a MedCommons Account, false otherwise
     */
    public boolean hasAccount() {
        if(AccountUtil.isRealAccountId(ownerMedCommonsId))
            return true;
        else
            return false;
    }
    
    /**
     * A convenience hack to make the above method easy to call from JSP.
     */
    public boolean getHasAccount() {
        return hasAccount();
    }
    
    /**
     * Returns the CCRs associated with this UserSession.
     * <p>
     * Note that CCRs can be very large and hence it is worth
     * making an effort to remove CCRs that are no longer in use
     * from the session.  However they are referenced externally
     * (in JSPs etc.) by their indexes, so rather than removing
     * a CCRDocument from the list you should set it's position
     * to null so as not to affect the indices of other CCRs.
     */
    public List<CCRDocument> getCcrs() {
        return ccrs;
    }
    
    /**
     * Returns the TODIR list for the current account, if any 
     * 
     * @return
     * @throws ServiceException 
     * @throws IOException 
     * @throws JDOMException 
     */
    public List<DirectoryEntry> getToDir() throws ServiceException, JDOMException, IOException {
        if(toDir == null) {
            if(!Str.blank(this.getAccountSettings().getToDir())) {
                log.info("Fetching todir for account " + this.getOwnerMedCommonsId() + " using group " + this.getAccountSettings().getGroupName());
                toDir = this.getServicesFactory().getDirectoryService(this.accountSettings.getToDir()).query(
                                null, null, null, null); 
            }
            else
                this.toDir = new ArrayList<DirectoryEntry>();
        }
        return toDir;
    }
 
    /**
     * Returns the currently active CCR for the given request.
     * 
     * IMPORTANT: this is not the "Current CCR" as labeled in the UI.  It might be
     * any logical or fixed CCR.  It is the CCR corresponding to the currently active
     * tab.
     */
    public CCRDocument getCurrentCcr(HttpServletRequest request) {
        int ccrIndex = getActiveCCRIndex(request);
        
        CCRDocument ccr = null;
        if(ccrIndex >= 0 && ccrIndex < this.getCcrs().size()) { // out of range - note: occurs on session timeout
            ccr = this.getCcrs().get(ccrIndex);        
            this.lastAccessedCcr = ccrIndex;
        }
        return ccr;
    }

    public int getActiveCCRIndex(HttpServletRequest request) {
        int ccrIndex = -1;
        if(request.getAttribute("updateIndex") != null) {
            ccrIndex = Integer.parseInt((String)request.getAttribute("updateIndex"));
        }
        else
        if(request.getAttribute("ccrIndex") != null) {
            ccrIndex = Integer.parseInt((String)request.getAttribute("ccrIndex"));
        }
        else
        if(request.getParameter("updateIndex") != null) {
            ccrIndex = Integer.parseInt(request.getParameter("updateIndex"));
        }
        else
        if(request.getParameter("ccrIndex") != null) {
            ccrIndex = Integer.parseInt(request.getParameter("ccrIndex"));
        }
        return ccrIndex;
    }
    
    /**
     * Sets the given CCR as the currently Active CCR.  If the CCR is not already
     * part of the session then it is added.  
     * 
     * The Desktop supports editing multiple CCRs simultaneously.  However,
     * any single request is expected to refer to a specific one of those CCRs.
     * The Active CCR specifies this CCR.
     * 
     * @param ccr
     */
    public void setActiveCCR(HttpServletRequest r,CCRDocument ccr) {
        int index = this.ccrs.indexOf(ccr);
        if(index<0) {
          this.ccrs.add(ccr);
          index = this.ccrs.size()-1;
        }
        r.setAttribute("ccrIndex", String.valueOf(this.getCcrs().indexOf(ccr)));
    }
    
    public CCRDocument getLogicalCCR(AccountDocumentType type) throws PHRException {
        try {
            String guid = this.getAccountSettings().getAccountDocuments().get(type);
            if(guid == null)
                return null;
            else
                return resolve(guid);
        }
        catch (ServiceException e) {
            throw new PHRException("Unable to retrieve logical CCR " + type.name() + " for user " + getOwnerMedCommonsId());
        }
        catch (ConfigurationException e) {
            throw new PHRException("Unable to retrieve logical CCR " + type.name() + " for user " + getOwnerMedCommonsId());
        }
        catch (RepositoryException e) {
            throw new PHRException("Unable to retrieve logical CCR " + type.name() + " for user " + getOwnerMedCommonsId());
        }
    }

    /**
     * 
     * @uml.property name="ownerMedCommonsId"
     */
    public String getOwnerMedCommonsId() {
        
        return ownerMedCommonsId;
    }
    
    /**
     * The group that was flagged as active when this session was
     * invoked. 
     */
    public String getOwnerActiveGroupId() throws ServiceException {
        AccountSettings settings = this.getAccountSettings();
        if(settings != null) {
            if(!Str.blank(settings.getGroupId())) {
                return settings.getGroupId();
            } 
        }
        // No settings or group found.
        return null;
    }
    
    public boolean isOwner(String accid) throws ServiceException {
        if(Str.blank(accid)) {
            accid = ServiceConstants.PUBLIC_MEDCOMMONS_ID;
        }
        return accid.equals(getOwnerMedCommonsId());
    }

    public String getAccessPin() {
        return accessPin;
    }

    public void setAccessPin(String accessPin) {
        this.accessPin = accessPin;
        if(this.replyPin == null) {
            this.replyPin = accessPin;
        }
    }
    

    public CCRDocument getReplyCcr(CCRDocument srcCcr) throws CCROperationException, PHRException  {
        
        try {
              srcCcr.syncFromJDom();
               
              // Copy the existing CCR
              CCRDocument replyCcr = srcCcr.copy();
              
              Namespace ns = replyCcr.getRootNamespace();

              // Get the To to be used as From in the reply CCR
              Element toEmail = replyCcr.getPrimaryNotificationEmail(true);
              Element replyFromActor = null;
              if(toEmail != null) 
                  replyFromActor = toEmail.getParentElement().getParentElement();
              
              // If the user has an account then make the reply CCR "from" that account,
              // otherwise use the old "To" as the "from"
              if(hasAccount()) {
                  // Is the owner of the session in the CCR as an actor?
                  replyFromActor = getOrCreateOwnerActor(replyCcr);
              }
              
              // Above we tried to get the To using the same method as To was chosen for notification.
              // This is important to try and match up the outgoing From with Incoming To accurately.
              // However if that failed we can try and pick any To that has an Email
              
              
              // Get the From to be used as To in the reply CCR
              Element fromActorLink = replyCcr.getPrimaryFromActor();
              String fromActorID = null;
              if(fromActorLink != null) 
                  fromActorID = fromActorLink.getChildText("ActorID",ns);
              
              // Now remove the old From and To
              CCRElement root = replyCcr.getRoot();
              root.removeChild("To", ns);
              root.removeChild("From", ns);
              
              // Now create them again, this time using our saved nodes
              // If the actors weren't found then we create new, blank ones for ourselves
              // to use.
              Element patient = root.getChild("Patient", ns);
                  
              if(replyFromActor == null) {
                  replyCcr.createDefaultActor("From");
              }
              else {
                  CCRElement from = el("From");
                  root.addChild(from);
                  replyCcr.addActorLink(from, replyFromActor.getChildText("ActorObjectID",ns));
              }              
                  
              if(fromActorID == null) {
                  replyCcr.createDefaultActor("To");
              }
              else {
                  CCRElement to = el("To");
                  root.addChild(to);
                  replyCcr.addActorLink(to, fromActorID);
              }
              
              // Set creation date
              replyCcr.setCreateTimeMs(System.currentTimeMillis());
              
              // Reset purpose text
              if(Str.blank(XPathUtils.getValue(replyCcr.getJDOMDocument(),"purposeText"))) {
                  replyCcr.setMedCommonsComment(getMessage("medcommons.replyCcr.purposeText"));
              }
              
              // Try to find the subject text to use
              if(srcCcr.getTrackingNumber()!=null) {
                  try {
	                  String previousSubject = this.getServicesFactory().getNotifierService().querySubject(srcCcr.getTrackingNumber());                  
	                  if(previousSubject != null) {
	                      log.info("Using previous subject " + previousSubject + " for reply CCR");
	                      replyCcr.setSubjectText(previousSubject);
	                  }
                  }
                  catch(Exception e) {
                      log.warn("Error while attempting to find previous subject for tracking number " + srcCcr.getTrackingNumber(), e);
                  }
              }
              
              // Get a new tracking number is NOT NECESSARY
              // Tracking numbers are now allocated "on-demand" when the user saves.  
              replyCcr.setTrackingNumber(null);
              
              // Ensure the reply CCR has null guid - otherwise it will appear to be fixed content
              replyCcr.setGuid(null);
              
              replyCcr.syncFromJDom();
              return replyCcr;
        }
        catch (ParseException e) {
            throw new CCROperationException("Unable to create reply CCR for CCR " + srcCcr.getTrackingNumber(),e);
        }
        catch (MultipleElementException e) {
            throw new CCROperationException(e);
        }
        catch (RepositoryException e) {
            throw new CCROperationException(e);
        }
        catch (ServiceException e) {
            throw new CCROperationException(e);
        }
    }

    /**
     * Retrieve the localized message with the specified key, formatted
     * with the given objects.
     * <p>
     * This reads properties from the resource bundle(s) 
     * in src/net/medcommons/router/web/WEB-INF/classes/net/medcommons/ApplicationResources.properties 
     * <p>
     * At some stage in the future this can be made localized, for now it is
     * just a single fixed property file.
     * 
     * @param name - name of key in properties file
     * @param objects - parameter values to be substituted into key expression
     * @return
     */
    public String getMessage(String name, Object...objects) {
        return getMessages().getMessage(name,objects);
    }

    /**
     * Return the message resources bundle for this user
     */
    public MessageResources getMessages() {
        return MessageResources.getMessageResources("net.medcommons.ApplicationResources");
    }
    
    /**
     * Check if the given CCR has an actor for the owner of this session.  If so, return it,
     * otherwise, create such an actor and return it.
     */
    public Element getOrCreateOwnerActor(CCRDocument ccr) throws PHRException, ServiceException {
        Element replyFromActor;
        CCRElement ownerActor = this.getOwnerActor(ccr);
        log.info("session owner has actor " + ownerActor);
        if(ownerActor == null) {
            ownerActor =  
                 CCRActorElement.createMedCommonsActor(getOwnerMedCommonsId(),
						                               getAccountSettings().getEmail(),
						                               null /* no source actor means source will be self */);
            ccr.getRoot().getOrCreate("Actors").addChild(ownerActor);
        }
        replyFromActor = ownerActor;
        return replyFromActor;
    }
    
    /**
     * Search for and return the actor in the given CCR that corresponds to the owner of this session
     * (ie. who has the same MedCommons Account ID as the owner of this session).
     */
    public CCRElement getOwnerActor(CCRDocument ccr) throws PHRException {
        return ccr.getJDOMDocument().queryProperty("actorFromMedCommonsId", new String[]{"mcId",getOwnerMedCommonsId()});
    }

    public String getReplyPin() {
        return replyPin;
    }

    public void setReplyPin(String replyPin) {
        this.replyPin = replyPin;
    }

    public ServicesFactory getServicesFactory() throws ServiceException {
        if(factory == null) {
            String scheme = "token:";
            factory = new RESTProxyServicesFactory(scheme+this.getAuthenticationToken(), this.sessionId);
        } 
        return factory;
    }
    
    public String getAuthenticationToken() {
        /*
        if(this.authorizationToken == null) {
            String authToken = "mcid://"+this.getOwnerMedCommonsId();
            
            // Check for group accounts to use
            AccountSettings settings = this.getAccountSettings();
            if(!StringUtil.blank(settings.getGroupId())) {
                authToken += "," + settings.getGroupId();
            }            
            this.authorizationToken = authToken;
        }
        */
        return this.authenticationToken;
    }

    public void setServicesFactory(ServicesFactory factory) {
        this.factory = factory;
    }

    public String getAccessGuid() {
        return accessGuid;
    }

    public void setAccessGuid(String accessGuid) {
        this.accessGuid = accessGuid;
    }
    
    public Set<String> getAccountTrackingNumbers() {
        return accountTrackingNumbers;
    }

    public AccountSettings getAccountSettings() throws ServiceException {
        if(this.accountSettings == null) {
            if(this.authenticationToken != null) {
                this.accountSettings = this.getServicesFactory().getAccountService().queryAccountSettings(this.getOwnerMedCommonsId());
            }
            else {
                this.accountSettings = new RESTProxyServicesFactory("mcid://"+this.getOwnerMedCommonsId()).getAccountService().queryAccountSettings(this.getOwnerMedCommonsId());
            }
        }
        return accountSettings;
    }
    
    /**
     * Attempts to retrieve account settings for the specified account id.
     * This may result in a remote call.  Account settings are cached on a 
     * session basis, so retrieving more than once in a session will not
     * result in more than one network call.
     * 
     * @param accountId
     * @return
     * @throws ServiceException
     */
    public AccountSettings getAccountSettings(String accountId) throws ServiceException {
        if(blank(accountId))
            throw new IllegalArgumentException("Account id must be provided");
        
        if(accountId.equals(this.getOwnerMedCommonsId())) {
            return this.getAccountSettings();
        }
        
        if(this.settingsCache == null)
            this.settingsCache = new Hashtable<String, AccountSettings>();
        
        AccountSettings settings = this.settingsCache.get(accountId);
        if(settings == null) {
            settings = this.getServicesFactory().getAccountService().queryAccountSettings(accountId);
            this.settingsCache.put(accountId, settings);
        }
        return settings;
    } 
    
    /**
     * Flush all account settings from this session
     */
    public void flushAccountSettings() {
        if(this.settingsCache != null)
            this.settingsCache.clear();
        this.accountSettings = null;
    }
    
    /**
     * Determine a storage id to use for the given CCR and it's associated content 
     * and return it.
     * 
     * If no storage id can be determined then the CCR's storage id will be set to null
     * and this method will return null. 
     * 
     * @throws IOException 
     * @throws JDOMException 
     */
    public String updateStorageId(CCRDocument ccr) throws PHRException {
        if(!Str.blank(ccr.getPatientMedCommonsId())) {
            ccr.setStorageId(ccr.getPatientMedCommonsId());
        }
        else {
            for(CCRDocument c : this.ccrs) {
                if(!blank(c.getPatientMedCommonsId())) {
                    ccr.setStorageId(c.getPatientMedCommonsId());
                    return ccr.getPatientMedCommonsId();
                }
            }
            
            if(this.hasAccount())
                ccr.setStorageId(this.getOwnerMedCommonsId());
            else
                ccr.setStorageId(ServiceConstants.PUBLIC_MEDCOMMONS_ID);
        }
        return ccr.getStorageId();
    }
    

    /**
     * Attempt to resolve the given guid, which must refer to a CCR, for this user.
     * 
     * Note: may return a CCR already loaded into the user's session / session
     * if they have already accessed the requested content.
     * 
     * @param g - the guid to resolve
     * @return A CCRDocument or null if the guid cannot be resolved for this user.
     * @throws PHRException 
     */
    public CCRDocument resolve(String g) throws ServiceException, ConfigurationException, RepositoryException, PHRException {
        // First check if we already loaded the guid in this session
        for (CCRDocument ccr : this.ccrs) {
            if(ccr != null && g.equals(ccr.getGuid())) {
                return ccr;
            }
        }
        
        // Not found in cached CCRs - try to resolve
        try {
            DocumentResolver resolver = new DocumentResolver(getServicesFactory());
            CCRDocument ccr = resolver.resolveCCR(this.getOwnerMedCommonsId(), g);
            
            if(ccr != null) {
                String patientMedCommonsId = ccr.getPatientMedCommonsId();
                if(ccr.getGuid() != null && patientMedCommonsId != null) {
                    // Since the CCR has a patient id, check if it is a logical document
                    AccountSettings patientSettings = getAccountSettings(patientMedCommonsId);
                    Map<AccountDocumentType, String> logicalDocs = patientSettings.getAccountDocuments();
                    for(AccountDocumentType t : logicalDocs.keySet()) {
                        if(logicalDocs.get(t).equals(ccr.getGuid())) {
                            ccr.setLogicalType(t);
                            ccr.setStorageMode(StorageMode.LOGICAL);
                            break;
                        }
                    }
                }
                this.ccrs.add(ccr);
            }
            
	        return ccr;
        }
        catch(InsufficientRightsException exNotAuth) {
            log.info("User " + this.getOwnerMedCommonsId() + " has insufficient access privileges to load guid " + g);
            return null;
        }
    }
   
    /**
     * Return the CCR of requested logical type, if the user has one.  If 
     * the user does not have a document of that type, return null.
     * @throws CCROperationException 
     */
    public CCRDocument getLogicalDocument(String storageId, AccountDocumentType type) throws CCROperationException {
        try {
            String guid = this.getAccountSettings(storageId).getAccountDocuments().get(type);
            if(guid == null)
                return null;
            
            return resolve(guid);
        }
        catch (ServiceException e) {
            throw new CCROperationException("Unable to resolve logical document of type " + type.name() + " for user " + storageId, e);
        }
        catch (ConfigurationException e) {
            throw new CCROperationException("Unable to resolve logical document of type " + type.name() + " for user " + storageId, e);
        }
        catch (RepositoryException e) {
            throw new CCROperationException("Unable to resolve logical document of type " + type.name() + " for user " + storageId, e);
        }
        catch (PHRException e) {
            throw new CCROperationException("Unable to resolve logical document of type " + type.name() + " for user " + storageId, e);
        }
    }

    public int getLastAccessedCcr() {
        return lastAccessedCcr;
    }

    public void setLastAccessedCcr(int lastAccessedCcr) {
        this.lastAccessedCcr = lastAccessedCcr;
    }

    public void valueBound(HttpSessionBindingEvent arg0) {
        log.info("Binding session " + this + " owned by user " + this.getOwnerMedCommonsId());
        allDesktops.add(this);
    }

    public void valueUnbound(HttpSessionBindingEvent arg0) {
        log.info("Unbinding session " + this + " owned by user " + this.getOwnerMedCommonsId());
        allDesktops.remove(this);        
    }

    public static Set<UserSession> getAllDesktops() {
        return allDesktops;
    } 

    public long getCreateDateTimeMs() {
        return createDateTimeMs;
    }
    
    public boolean isPatientMode() {
        return this.getAccessMode() == AccessMode.PATIENT;
    }
    
    public String getDefaultAccountStatus() throws ServiceException {
        List<String> values = this.getAccountSettings().getStatusValues();
        if(values.isEmpty())
            return "";
        else
            return values.get(0);
    }

    /**
     * Returns true if the requested permission is available for accessing the given
     * account's documents by the current owner of this session. 
     * 
     * @param storageId
     * @param requiredRights
     * @return
     * @throws ServiceException
     */
    public boolean checkPermissions(String storageId, String requiredRights) throws ServiceException {
        
        if(storageId == null) { 
            storageId = ServiceConstants.PUBLIC_MEDCOMMONS_ID;
        }
        
        if(!this.rightsCache.containsKey(storageId)) {
            EnumSet<Rights> p = this.getServicesFactory().getDocumentService().getAccountPermissions(storageId);
            log.info("Caching permission " + p.toString() + " to account " + storageId);
            this.rightsCache.put(storageId,p);
        } 
        EnumSet<Rights> actualRights = this.rightsCache.get(storageId);
        return actualRights.containsAll(Rights.toSet(requiredRights));
    }
    
    /**
     * Begin a transaction for storing the given CCR for this user.
     * 
     * @return an initialized StoreTransaction, ready to store the CCR.
     */
    public StoreTransaction tx(CCRDocument ccr) throws CCRStoreException, ServiceException {
        StoreTransaction tx = new StoreTransaction(this.getServicesFactory(), this.getAccountSettings(), ccr);
        try {
            String patientMcId = ccr.getPatientMedCommonsId();
            if(!hasAccount() || !getOwnerMedCommonsId().equals(patientMcId))
                tx.setPatientAccountSettings(getAccountSettings(patientMcId));
                
            return tx;
        } catch (PHRException e) {
            throw new CCRStoreException(e);
        }
    }
    
    /**
     * Retrieves the current session from the request, or creates a new one automatically,
     * using values "accid" and "auth" from the request, if they are there.  If they are not
     * there then a POPS session will be created (no accid, no auth).
     * 
     * @param request
     * @return
     * @throws ServiceException 
     */
    public static UserSession get(HttpServletRequest request) throws ServiceException {
      String accountId = (String) request.getSession().getAttribute("accid");
      if(accountId != null) {
          request.getSession().removeAttribute("accid");
      }
      else 
          accountId = request.getParameter("accid");
      
      String auth = request.getParameter("auth");
      
      if(blank(auth)) {
          auth = (String) request.getAttribute("oauth_token");
          if(!blank(auth))
              log.info("Using auth from oauth_token " + auth);
      }
      else
          log.debug("Using supplied auth " + auth);
      
      return get(request, accountId, auth);
    }
    
    /**
     * Either returns the current session from the session or creates a fresh
     * one for the given account and returns that.
     * @throws ServiceException 
     */
     public static UserSession get(HttpServletRequest request, String accid, String auth) throws ServiceException {
         return get(request,accid,auth,true);
    }
    
    /**
     * Either returns the current session from the session or, if the create parameter
     * is true then creates a fresh one for the given account and returns that.
     * 
     * @throws ServiceException if identity provided in auth cannot be verified 
     */
    public static UserSession get(HttpServletRequest request, String accid, String auth, boolean create) throws ServiceException {
        UserSession session = (UserSession) request.getSession().getAttribute("desktop");
        if(session == null && create) {
            log.info("Creating session for session id " + request.getSession().getId());
            
            // Validate auth
            if(!blank(auth) && !"Gateway".equals(auth) && !auth.matches("[a-z0-9]{40}")) 
                throw new IllegalArgumentException("Invalid value supplied for auth: " + auth);
            
            if(blank(auth) && !blank((String)request.getAttribute("oauth_token"))) {
                auth = (String) request.getAttribute("oauth_token");
                log.info("Using auth from oauth token: " + auth);
            }
            
            session = new UserSession(accid, auth, new ArrayList<CCRDocument>());
            request.getSession().setAttribute("desktop", session);
            if(Str.equals(request.getParameter("am"), "p")) { 
                session.setAccessMode(AccessMode.PATIENT);
            } 
            session.sessionId = request.getSession().getId(); 
            if(request.getAttribute("oauth_principal")!=null) {
                session.ownerPrincipal = (AccountSpec) request.getAttribute("oauth_principal");
            }
                
            session.ownerMedCommonsId = session.getOwnerPrincipal().getMcId();
        }
        return session;
    }
    
    /**
     * Returns the current Desktop from the session, throwing an exception if not available
     * 
     * @throws NotLoggedInException if user / client does not already have a session
     */
    public static UserSession required(HttpServletRequest request) throws NotLoggedInException {
        UserSession desktop = (UserSession) request.getSession().getAttribute("desktop");
        if(desktop == null) {
            throw new NotLoggedInException();
        }
        return desktop;
    }
    
    /**
     * Removes the current session from the session, replacing it with a fresh one.
     * @throws ServiceException 
     */
    public static UserSession clean(HttpServletRequest request, String accid, String auth) throws ServiceException {
        
        request.getSession().removeAttribute("desktop");
        
        UserSession desktop = get(request, accid, auth);
        if(Str.equals(request.getParameter("am"), "p")) { 
            desktop.setAccessMode(AccessMode.PATIENT);
        }
        return desktop;
    }
    
    /**
     * Removes the current session from the session, replacing it with a fresh one.
     * @throws ServiceException 
     */
    public static UserSession clean(HttpServletRequest request) throws ServiceException {
        return clean(request, request.getParameter("accid"), request.getParameter("auth"));
    }
    
    /**
     * Returns true if the current user has a session already, false otherwise
     * @throws ServiceException 
     */
    public static boolean has(HttpServletRequest request) throws ServiceException {
        return get(request, null, null, false) != null; 
    }
    
    /**
     * Same as 'clean' but preserves existing account id and auth if there is an existing session.
     * If there is no existing session, creates and returns a new one with given credentials.
     * @throws ServiceException 
     */
    public static UserSession sweep(HttpServletRequest request, String accid, String auth) throws ServiceException {
        UserSession old = get(request);
        String oldAuth = (old!=null && !blank(old.getAuthenticationToken())) ? old.getAuthenticationToken() : auth;
        return clean(request, old!=null?old.getOwnerMedCommonsId():accid,oldAuth);
    }

    public TrackingReference getAccessTrackingReference() {
        return accessTrackingReference;
    }

    public void setAccessTrackingReference(TrackingReference accessTrackingNumber) {
        this.accessTrackingReference = accessTrackingNumber;
    }

    public AccountSpec getOwnerPrincipal() throws ServiceException {
        if(ownerPrincipal == null) {
            log.info("Querying principal for auth token " + this.getAuthenticationToken());
            ownerPrincipal = getServicesFactory().getDocumentService().queryPrincipal();
            log.info("Principal returned as " + ownerPrincipal);
            if(ownerPrincipal == null)
                ownerPrincipal = new AccountSpec(ServiceConstants.PUBLIC_MEDCOMMONS_ID);
        }
        return ownerPrincipal;
    }
    
    /**
     * Returns true if at least one CCR in the session has a 
     * MedCommons Account Id for the patient, false otherwise.
     * @throws PHRException 
     */
    public boolean getAnyPatientHasAccountId() throws PHRException {
        for(CCRDocument ccr : this.ccrs) {
            if(!blank(ccr.getPatientMedCommonsId())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Attempt to find a unique patient id in any CCR in the session.
     * The first patient id found is returned.  If no patient 
     * ids are found then null is returned.
     * @throws PHRException 
     * @throws CCROperationException 
     */
    public String getPatientAccountId() throws PHRException, CCROperationException {
        String patientId = null;
        for(CCRDocument ccr : this.ccrs) {
            if(!blank(patientId)) {
                if(!blank(ccr.getPatientMedCommonsId()) && !patientId.equals(ccr.getPatientMedCommonsId()))
                    throw new CCROperationException("Multiple Patient Account Ids were found in the current session.  This operation cannot proceed with ambiguous Patient Account Id");
            }
            else
                patientId = ccr.getPatientMedCommonsId();
        }
        return patientId;
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    public String getRssId() {
        return rssId;
    }

    public void setRssId(String rssId) {
        this.rssId = rssId;
    }
    public AccountSpec getContextPrincipal() {
        return contextPrincipal;
    }

    public void setContextPrincipal(AccountSpec contextPrincipal) {
        this.contextPrincipal = contextPrincipal;
        this.ownerMedCommonsId = contextPrincipal.getMcId();
    }
    
    public String getContextAuth() {
        return contextAuth;
    }

    public void setContextAuth(String contextAuth) {
        this.contextAuth = contextAuth;
    }
}
