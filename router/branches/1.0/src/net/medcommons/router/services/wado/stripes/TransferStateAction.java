/*
 * $Id: TransferStateAction.java 3736 2010-06-03 11:21:01Z ssadedin $
 * Created on 12/12/2008
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.DICOMTransactionType;
import net.medcommons.modules.services.interfaces.OutOfDateException;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.TransferMessage;
import net.medcommons.modules.services.interfaces.TransferState;
import net.medcommons.modules.services.interfaces.TransferStatusService;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.web.stripes.JSONActionBean;
import net.medcommons.router.web.stripes.MsDateConverter;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.log4j.Logger;

/**
 * Simple action for querying and updating the status of a DICOM transfer.
 * 
 * @author ssadedin
 */
public class TransferStateAction extends JSONActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(TransferStateAction.class);
    
    /**
     * Service used to update dicom transaction status 
     */
    TransferStatusService svc = Configuration.getBean("dicomStatusService");
   
    
    @ValidateNestedProperties({
        @Validate(field="key",required=true,mask=GUID_PATTERN,on="put"),
        @Validate(field="accountId",required=true,mask=MCID_PATTERN,on="put"),
        @Validate(field="status",required=true,on="put"),
        @Validate(field="progress",required=true,minvalue=0.0, maxvalue=1.0,on="put"),
        @Validate(field="modified", required=false, converter=MsDateConverter.class,on="put"),
        @Validate(field="ddlIdentifier", required=true, mask=GUID_PATTERN,on="put")
    })
    TransferState status;
    
    /**
     * Store or update a dicom transaction
     * @throws ServiceException 
     */
    public Resolution put() throws Exception {
        log.info("put status account=" + status.getAccountId() + ", key =" + status.getKey() 
                + ", progress ="
                + status.getProgress()); 
        
        // Temporarily we support some defaults - once DDL is sending 
        // them we can delete these
        if(status.getType() == null) {
            log.warn("Inferring transaction type from status value " + status.getStatus());
            status.setType("Uploading".equals(status.getStatus()) ? DICOMTransactionType.UPLOAD : DICOMTransactionType.DOWNLOAD);
        }
        
        if(blank(status.getOwnerAccountId())) {
            log.warn("Inferring owner account from storage account " + status.getAccountId());
            status.setOwnerAccountId(status.getAccountId());
        }
        
        try {
            status = svc.put(status);
        }
        catch(OutOfDateException exOutOfdate) {
            result.put("status", "failed");
            result.put("error", "invalid version");
            status = svc.get(status.getAccountId(), status.getKey());
        }
        
        notifyGroup();
        
        result.put("transferState", status.toJSON());
       
        return new StreamingResolution("text/plain", result.toString());
    }

    
    @Validate(on="get",required=true)
    String accountId;
    
    @Validate(on="get",required=true)
    String key;
    
    public Resolution get() throws Exception {
        
        TransferState status = svc.get(accountId, key);
        result.put("transferState", status.toJSON());
        return new StreamingResolution("text/plain", result.toString());
    }
    
    @ValidateNestedProperties({
        @Validate(field="transferKey",required=false,mask=GUID_PATTERN,on="addMessage"),
        @Validate(field="ddlIdentifier", required=true, mask=GUID_PATTERN,on="addMessage"),
        @Validate(field="message",required=true,on="addMessage"),
        @Validate(field="category",required=true,on="addMessage")
    })
    TransferMessage message;
    
    /**
     * Save a message for the group authenticated for this session.
     * 
     * @return
     * @throws Exception
     */
    public Resolution addMessage() throws Exception {
        
        String groupAccountId = this.session.getAccountSettings().getGroupId();
        if(blank(groupAccountId))
            throw new IllegalArgumentException("Auth token does not specify valid group");
        
        log.info("Received message " + message.getMessage() + " for group " + groupAccountId);
        
        message.setCreateDateTime(new Date());
        message.setAccountId(groupAccountId);
        
        svc.addMessage(message);
        
        notifyGroup();
            
        return new StreamingResolution("text/plain", result.toString()); 
    }

    /**
     * Triggers a notification to the group to which this user belongs
     * that a messages has arrived.  Releases any threads waiting for
     * events.  See {@link #waitForEvent()}.
     * 
     * @throws ServiceException
     */
    public void notifyGroup() throws ServiceException {
        notifyGroup(this.session);
    }
    
    /**
     * Notify group assicated with specified session
     * 
     * @param s
     * @throws ServiceException
     */
    public static void notifyGroup(UserSession s) throws ServiceException {
        
        String groupAccountId = s.getAccountSettings().getGroupId();
        if(groupAccountId == null) {
            log.debug("No group identified for session " + s.getAuthenticationToken() + ": no notifications triggered");
            return;
        }
        
        Object eventSignal = eventSignals.get(groupAccountId);
        
        if(eventSignal != null) {
            log.info("Notifying group " + groupAccountId + " about message");
	        synchronized (eventSignal) {
		        eventSignal.notifyAll();
	        }
        }
    }
    
    /**
     * Time after which waitForEvent should return even if no
     * event activity is noticed.
     */
    long timeoutMs = 30000;
    
    /**
     * Optional jsonp callback
     */
    String jsonp;
    
    /**
     * Object used to notify of events
     */
    static Hashtable<String, Object> eventSignals = new Hashtable<String, Object>();
    
    /**
     * Blocks while waiting for an event of interest to occur.  Returns
     * after {@link #timeoutMs} even if no event occurs.  This is important
     * to ensure that threads do not "leak".
     * 
     * @throws Exception 
     */
    public Resolution waitForEvent() throws Exception {
        
        String groupAccountId = this.session.getAccountSettings().getGroupId();
        if(groupAccountId == null) 
            throw new IllegalArgumentException("Cannot wait for events unless you are a group member");
        
        Object eventSignal = eventSignals.get(groupAccountId);
        if(eventSignal == null) {
            eventSignal = new Object();
            eventSignals.put(groupAccountId, eventSignal); 
        }
        
        log.info("Thread " + Thread.currentThread().getId() + " waiting for ddl events for group " + groupAccountId);
        synchronized(eventSignal) {
	        eventSignal.wait(timeoutMs);
	    }
        
        result.put("gwUrl",Configuration.getProperty("RemoteAccessAddress"));
        String resultText;
        if(!blank(jsonp)) {
            resultText = jsonp + "(" + result.toString() + ");";
        }
        else 
            resultText = result.toString();
        
        log.info("Returning status " + resultText + " to ddl event watcher");
        
        return new StreamingResolution("text/plain", resultText); 
    }
    
    @Override
    protected void checkSID() {
        // DO NOT CHECK FOR SID - required for now until DDL can pass signed requests up
        // super.checkSID();
    }
    
    public TransferState getStatus() {
        return status;
    }

    public void setStatus(TransferState status) {
        this.status = status;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public TransferMessage getMessage() {
        return message;
    }

    public void setMessage(TransferMessage message) {
        this.message = message;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getJsonp() {
        return jsonp;
    }

    public void setJsonp(String jsonp) {
        this.jsonp = jsonp;
    }
}
