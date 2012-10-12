/*
 * $Id: CreateVoucherAction.java 3782 2010-07-01 22:38:29Z ssadedin $
 * Created on 04/12/2008
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.nvl;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.phr.PHRException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.User;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.ccr.CCRStoreException;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.db.DB;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.JSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONObject;

/**
 * Wrapper service that passes through to MOD voucher creation
 * routine to allow easy creation of vouchers for any service
 * a user has defined.
 * 
 * @author ssadedin
 */
public class CreateVoucherAction extends JSONActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CreateVoucherAction.class);
    
    @Validate(required=true, maxlength=60)
    String firstName;
    
    @Validate(required=true, maxlength=60)
    String lastName;
    
    @Validate(required=true, mask=MCID_PATTERN)
    String accid;
    
    @Validate(required=false)
    String sex;
    
    @Validate(required=true, maxlength=255)
    String service;
    
    @Validate(required=false, mask=MCID_PATTERN)
    String storageId;
    
    @Validate(required=false)
    String purpose;
    
    @DefaultHandler
    public Resolution create() throws Exception {
        
        String accountsBaseUrl = Configuration.getProperty("AccountsBaseUrl");
        
        
        RESTUtil.RestCall call = new RESTUtil.RestCall(
                        accountsBaseUrl + "/mod/ws/createVoucher.php", 
                        session.getAuthenticationToken(), 
                        "fn",firstName,
                        "ln",lastName,
                        "sex",nvl(sex,""),
                        "auth", session.getAuthenticationToken(),
                        "accid", session.getOwnerMedCommonsId(),
                        "svc", service,
                        "storageId", storageId
                        );
        
        if(!blank(storageId) && !blank(purpose)) { 
                setCurrentCCRPurpose();
        }
        
        // If the patient name is null, update it to match the voucher
        Session db = DB.currentSession();
        try {
            db.beginTransaction();
            User user = (User) db.get(User.class, new Long(session.getOwnerMedCommonsId()));
            if(blank(user.getFirstName()) && blank(user.getLastName())) {
                log.info("User " + session.getOwnerMedCommonsId() + " has blank name: updating with voucher details");
                user.setFirstName(firstName);
                user.setLastName(lastName);
            }
            db.update(user);
            db.getTransaction().commit();
        }
        finally {
            DB.closeSession();
        }
        
        JSONObject resultObj = call.fetchJSONResponse();
        
        // Slight hack: ping the transfer state action so that it can 
        // send back events to notify that there has been a change worth
        // reflecting on the dashboard
        TransferStateAction.notifyGroup(this.session);
        
        return new StreamingResolution("text/plain", resultObj.toString());
    }
    

    private void setCurrentCCRPurpose() throws ServiceException, ConfigurationException, RepositoryException, PHRException, CCRStoreException {
        AccountSettings patientSettings = session.getAccountSettings(storageId);
        if(blank(patientSettings.getCurrentCcrGuid())) {
            log.warn("Cannot set purpose - patient " + storageId + " has no current CCR");
            return;
        }
            
        CCRDocument ccr = session.resolve(patientSettings.getCurrentCcrGuid());
        if(ccr == null) {
            log.warn("Unable to set purpose " + purpose + " on CCR for user " + storageId + " because user has no Current CCR");
            return;
        }
        ccr.getJDOMDocument().setValue("ccrPurpose", purpose);
        
        StoreTransaction tx = session.tx(ccr);
        tx.registerDocument(null);
        tx.storeDocument();
        tx.notifyRegistry();
    }


    @Override
    protected void checkSID() {
        // DO NOT CHECK SID
        // We allow this method to be called by 3rd parties
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

    public String getAccid() {
        return accid;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }


    public String getSex() {
        return sex;
    }


    public void setSex(String sex) {
        this.sex = sex;
    }


    public String getStorageId() {
        return storageId;
    }


    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }


    public String getPurpose() {
        return purpose;
    }


    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }



}
