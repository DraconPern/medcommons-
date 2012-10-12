/*
 * $Id: NewPatientAction.java 3876 2010-10-13 07:26:28Z ssadedin $
 * Created on 10/10/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.phr.ccr.CCRElementFactory.el;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.AccountOptions;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.AccountType;
import net.medcommons.modules.services.interfaces.ActivationDetails;
import net.medcommons.modules.services.interfaces.Application;
import net.medcommons.modules.services.interfaces.ExternalShare;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.ccr.CCRStoreException;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.ccrmerge.MergeException;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.services.xds.consumer.web.InvalidCCRException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.JSONResolution;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * Creates a new patient based on supplied parameters
 * 
 * @author ssadedin
 */
public class NewPatientAction implements ActionBean {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(NewPatientAction.class);

    private String givenName;

    private String familyName;

    private String dateOfBirth;

    private String sex;

    private String height;

    private String weight;
    
    private String city;
    
    private String state;
    
    private String photoUrl;
    
    private String country;
    
    private String purpose;
    
    private boolean createCCR = true;
    
    private String password;
    
    /**
     * Time / date in seconds since GM
     */
    private long expiryDateTimeSeconds;
    
    /**
     * An auth token that (if provided) will be set as the parent of
     * the auth token created for the new account 
     */
    private String auth;
    
    /**
     * Captures the oauth consumer if this call is made
     * as a fully authenticated oauth call.  In that case,
     * patient is created with an authentication token linked
     * to the calling application
     */
    private String oauth_consumer_key;
    
    private String sponsorAccountId;
    
    private String activationKey;
    
    private String activationProductCode;
    
    private String email;
    
    private boolean enableSimtrak = false;
    
    /**
     * Comma separated list of accounts that should be granted rw access to the created patient
     */
    private String consents;

    /**
     * External URL to which patient activity notifications will be sent
     */
    private String registryUrl = "";
    
    private boolean publicAccount = false;

    private ActionBeanContext ctx;

    /**
     * Creates a new patient account with the attributes specified in the
     * input parameters.
     * 
     * @return JSON encoded object with attributes:
     *             <li>patientMedCommonsId
     *             <li>currentCCRGuid
     *             <li>auth
     *             <li>secret
     */
    @DefaultHandler
    public Resolution create() {

        try {
            log.info("Creating new patient with name " + givenName + " " + familyName + " DOB=" + dateOfBirth);
            
            if(this.auth == null) {
                this.auth = (String) this.ctx.getRequest().getAttribute("oauth_token");
                log.info("Using auth from oauth_token " + this.auth);
            }
            else
                log.info("Using supplied auth " + this.auth);
            
            ActivationDetails activation = null;
            if(!blank(this.activationKey)) {
                activation = new ActivationDetails(); 
                activation.setActivationKey(this.activationKey);
                activation.setActivationProductCode(this.activationProductCode);
            }
            else 
            if(!blank(sponsorAccountId)) {
                activation = new ActivationDetails();
                activation.setAccountId(sponsorAccountId);
            }
            
            ServicesFactory svc = new RESTProxyServicesFactory("token:" + auth);
            
            if(!blank(sponsorAccountId) && blank(registryUrl)) {
                AccountSettings settings = svc.getAccountService().queryAccountSettings(sponsorAccountId);
                registryUrl = settings.getRegistry();
            }
            
            AccountOptions options = new AccountOptions(); 
            options.setEnableSimtrak(enableSimtrak);
            
            if(expiryDateTimeSeconds > 0) {
                Date expiryDate = new Date(expiryDateTimeSeconds*1000);
                log.info("Created account will expire at " + expiryDate);
                options.setExpiryDate(expiryDate);
            }

            // Create a patient first
            String [] results = svc.getAccountCreationService().register(AccountType.SPONSORED, email, password, givenName,
                            familyName, "", "", registryUrl, photoUrl, auth, activation, options); 
            
            String patientId = results[0];
            String authToken = results[1];
            String secret = results[2];
            
            log.info("Created patient " + patientId + " for new patient CCR");

            // Recreate services factory, this time with the token authorized to
            // access the user's account.  If there is a sponsor who is already
            // being given access then don't replace so that actions get logged
            // in activity log as the sponsor
            if(blank(sponsorAccountId)) {
                svc = new RESTProxyServicesFactory("token:" + authToken);
            }
            
            String guid = null;
            if(createCCR) 
                guid = savePatientCCR(svc, patientId);
            
            // Grant account level access to new patient's ccrs to sponsor account
            if(!blank(sponsorAccountId)) 
                svc.getDocumentService().grantAccountAccess(patientId, sponsorAccountId, Rights.ALL);
            
            if(!blank(consents)) {
                svc.getDocumentService().grantAccountAccess(patientId, consents, Rights.ALL);
            }
            
            // Was this call made by an application? If so, make sure the application has
            // rights to the created account via a correctly linked token.
            // Note that the token returned by the register call above is created with the
            // patient's account id as the authority - that's not correct when the caller is an application.
            if(!blank(this.oauth_consumer_key)) {
                Application app = svc.getAccountService().queryApplicationInfo(this.oauth_consumer_key); 
                ExternalShare share = new ExternalShare();
                share.setIdentityType(ExternalShare.IdentityType.Application);
                share.setIdentity(app.getName());
                
                // We set the incoming auth so that the auth created by granting access will
                // have the incoming as it's parent
                share.setAuth(Str.bvl(this.auth,this.oauth_consumer_key));
                
                ExternalShare es = svc.getDocumentService().grantAccountAccess(patientId,share,Rights.ALL);
                
                // When an external share was created, we don't return the patient's auth token directly.
                // Rather we return the auth token associated with the external share that was created.
                // The patient's one still exists, but we just discard it.
                authToken = es.getAuth();
                secret = es.getSecret();
            }
            
            // We use text/plain encoding because this action is sometimes loaded into the browser dom
            return new JSONResolution("text/plain")
                                       .put("patientMedCommonsId", patientId)
                                       .put("currentCCRGuid", guid)
                                       .put("auth", authToken)
                                       .put("secret", secret);
            
        }
        catch (Exception e) {
            log.error("Failed to create new patient " + this.givenName + " " + this.familyName,e);
            return new JSONResolution(e);
        }
    }

    /**
     * Create and save a CCR for the patient specified for this action,
     * setting it as the Current CCR for the patient.
     */
    private String savePatientCCR(ServicesFactory svc, String patientId) throws JDOMException, IOException,
                    ParseException, RepositoryException, NoSuchAlgorithmException, PHRException, InvalidCCRException,
                    CCROperationException, ServiceException, CCRStoreException, MergeException {
        // Create a new CCR for the patient
        CCRDocument ccr = getBaseCCR(patientId);
        ccr.setCreateTimeMs(System.currentTimeMillis());
        ccr.addPatientId(patientId, CCRConstants.MEDCOMMONS_PATIENT_ID_TYPE);
        ccr.syncFromJDom();
        ccr.getValidatedJDCOMDocument();
        log.info("New CCR validated");
        // Set the data values
        setDataValues(ccr);

        // Validate
        List<Exception> errors = ccr.getJDOMDocument().validate().errors;
        if (!errors.isEmpty()) {
            throw new CCROperationException("Supplied parameters result in invalid CCR: "
                            + errors.get(0).getMessage());
        }

        // Save it
        log.info("Storing new CCR for patient " + patientId);
        AccountSettings acctSettings = svc.getAccountService().queryAccountSettings(patientId);
        StoreTransaction tx = new StoreTransaction(svc, acctSettings, ccr);
        ArrayList<String[]> rights = new ArrayList<String[]>();
        if(!Str.blank(sponsorAccountId)) {
            rights.add(new String[] { sponsorAccountId, Rights.ALL });
        } 
        if(publicAccount)
            rights.add(new String[] { ServiceConstants.PUBLIC_MEDCOMMONS_ID, Rights.ALL});
            
        tx.registerDocument(null, rights.toArray(new String[2][]));
        tx.storeDocument();
        tx.notifyRegistry();
        tx.merge();
        log.info("Successfully stored new CCR for patient " + patientId + " with result guid " + tx.getDocumentGuid());
        return tx.getDocumentGuid();
    }

    protected CCRDocument getBaseCCR(String patientId) throws JDOMException, IOException, ParseException,
                    RepositoryException, NoSuchAlgorithmException, PHRException {
        CCRDocument ccr = CCRDocument.createFromTemplate(patientId);
        return ccr;
    }

    /**
     * Attempt to set the various provided values on the CCR
     * @param ccr
     */
    private void setDataValues(CCRDocument ccr) throws PHRException, InvalidCCRException {
        ccr.createPath("patientGivenName").setText(givenName);
        ccr.createPath("patientFamilyName").setText(familyName);
        
        
        if(!blank(sex)) 
            ccr.createPath("patientGender").setText(sex);
        
        if(!blank(dateOfBirth)) 
            ccr.setPatientDateOfBirth(dateOfBirth);
        
        if(!blank(city)) 
            ccr.createPath("patientCity").setText(city);
        
        if (!blank(state)) 
            ccr.createPath("patientState").setText(state);
        
        if(!blank(country)) 
            ccr.createPath("patientCountry").setText(country);
        
        if(!blank(purpose)) 
            ccr.createPath("ccrPurpose").setText(purpose);
        

        // Height and Weight are a little more complicated: create
        // VitalSigns
        if (!blank(height) || !blank(weight)) {
            String patientActorID = ccr.getJDOMDocument().queryProperty("patientActorID").getTextTrim();
            CCRElement rs = ccr.getRoot().getOrCreate("Body").getOrCreate("VitalSigns").getOrCreate("Result");
            rs.createPath("CCRDataObjectID", ccr.generateObjectID());
            rs.createPath("Description/Text", "Height and Weight");
            rs.createPath("Source/Actor/ActorID", patientActorID); // not really, but something has to go here.  Should query principal for auth, eventually, 
            
            if (!blank(height)) {
                CCRElement test = el("Test");
                test.createPath("CCRDataObjectID", ccr.generateObjectID());
                test.createPath("Type/Text", "Observation");
                test.createPath("Description/Text", "Height");
                test.createPath("Source/Actor/ActorID", patientActorID);
                String[] heightParts = height.split(" ");
                test.createPath("TestResult/Value", heightParts[0]);
                test.createPath("TestResult/Units/Unit", heightParts.length > 1 ? heightParts[1] : "cm");
                rs.addChild(test);
            }
            
            if (!blank(weight)) {
                CCRElement test = el("Test");
                test.createPath("CCRDataObjectID", ccr.generateObjectID());
                test.createPath("Type/Text", "Observation");
                test.createPath("Description/Text", "Weight");
                test.createPath("Source/Actor/ActorID", patientActorID);
                String[] weightParts = weight.split(" ");
                test.createPath("TestResult/Value", weightParts[0]);
                test.createPath("TestResult/Units/Unit", weightParts.length > 1 ? weightParts[1] : "lb");
                rs.addChild(test);
            }
        }
        
    }

    public ActionBeanContext getContext() {
        return ctx;
    }

    public void setContext(ActionBeanContext ctx) {
        this.ctx = ctx;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getSponsorAccountId() {
        return sponsorAccountId;
    }

    public void setSponsorAccountId(String sponsorAccountId) {
        this.sponsorAccountId = sponsorAccountId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean getPublic() {
        return publicAccount;
    }

    public void setPublic(boolean publicAccount) {
        this.publicAccount = publicAccount;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getActivationProductCode() {
        return activationProductCode;
    }

    public void setActivationProductCode(String activationProductCode) {
        this.activationProductCode = activationProductCode;
    }

    public String getOauth_consumer_key() {
        return oauth_consumer_key;
    }

    public void setOauth_consumer_key(String oauth_consumer_key) {
        this.oauth_consumer_key = oauth_consumer_key;
    }

    public boolean getCreateCCR() {
        return createCCR;
    }

    public void setCreateCCR(boolean createCCR) {
        this.createCCR = createCCR;
    }

    public static Logger getLog() {
        return log;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getPublicAccount() {
        return publicAccount;
    }

    public ActionBeanContext getCtx() {
        return ctx;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConsents() {
        return consents;
    }

    public void setConsents(String consents) {
        this.consents = consents;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public boolean isEnableSimtrak() {
        return enableSimtrak;
    }

    public void setEnableSimtrak(boolean enableSimtrak) {
        this.enableSimtrak = enableSimtrak;
    }

    public long getExpiryDateTimeSeconds() {
        return expiryDateTimeSeconds;
    }

    public void setExpiryDateTimeSeconds(long expiryDateTimeSeconds) {
        this.expiryDateTimeSeconds = expiryDateTimeSeconds;
    }
}
