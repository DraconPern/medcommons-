/*
 * $Id: BillingAction.java 2957 2008-10-08 03:56:13Z ssadedin $
 * Created on 23/05/2008
 */
package net.medcommons.router.services.wado.stripes;

import static java.util.Collections.singleton;
import static net.medcommons.modules.utils.Str.blank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.BillingCharge;
import net.medcommons.modules.services.interfaces.BillingCounters;
import net.medcommons.modules.services.interfaces.BillingEvent;
import net.medcommons.modules.services.interfaces.BillingEventType;
import net.medcommons.modules.services.interfaces.BillingService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.log4j.Logger;

import flexjson.JSONSerializer;

/**
 * Handles requests to check accounts for sufficent credit and also to 
 * actually charge them for various items.
 * 
 * @author ssadedin
 */
public class BillingAction extends CCRJSONActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(BillingAction.class);
    
    private String type;
    
    private int count;
    
    private int seriesIndex = -1;
    
    @DefaultHandler 
    public Resolution check() throws Exception {
        List<String> accounts = null;
        
        accounts = getAccounts();
        BillingService billingService = session.getServicesFactory().getBillingService();
        List<BillingCharge> charges = billingService.resolvePayer(accounts, Collections.singleton(new BillingEvent(BillingEventType.INBOUND_DICOM)));
        
        JSONSerializer json = new JSONSerializer().exclude("*.class");
        HashMap<String, Object> output = new HashMap<String, Object>();
        output.put("status", "ok");
        output.put("charges", charges);
        if(Configuration.getProperty("EnableBilling",false))  {
            if(charges.isEmpty()) {
                output.put("credit","insufficent");
            }
            else
                output.put("credit",charges.isEmpty() ? "insufficent" : "ok");
            
            addBillingCounters(output);
        }
        else
            output.put("credit","ok"); // Billing not enabled, credit is always good!
        
        return new StreamingResolution("text/plain", json.serialize(output));
    }
    
    /**
     * Query for the current user's billing credits and add them to 
     * the given output hash under the name "counters"
     */
    private void addBillingCounters(HashMap<String, Object> output) throws ServiceException {
        // Get the user's credits so they can be displayed
        String accId = session.getOwnerPrincipal().getMcId();
        if(!blank(accId)) {
            BillingCounters counters = session.getServicesFactory().getBillingService().queryAvailableCredits(accId);
            output.put("counters",counters);
        }
    }
    
    /**
     * Execute the specified charge against the user's account, also validating
     * payment for the specified series at the same time.
     */
    public Resolution charge() throws Exception {
        List<String> accounts = new ArrayList<String>();
        accounts = getAccounts();
        BillingService billingService = session.getServicesFactory().getBillingService();
        BillingEvent billingEvent = new BillingEvent(BillingEventType.valueOf(type));
        billingEvent.setQuantity(this.count);
        List<BillingCharge> charges = 
            billingService.resolvePayer(accounts, singleton(billingEvent));
        
        JSONSerializer json = new JSONSerializer().exclude("*.class");
        HashMap<String, Object> output = new HashMap<String, Object>();
        output.put("charges", charges);
        
        boolean creditOK = true;
        if(Configuration.getProperty("EnableBilling",false)) {
            if(charges.isEmpty())
                creditOK = false;
            
            addBillingCounters(output);
            output.put("credit",creditOK ? "ok" : "insufficient");
            output.put("status",creditOK ? "ok" : "failed");
        }
        else {
            output.put("credit","ok"); // Billing not enabled, credit is always good!
            output.put("status","ok");
        }
        
        output.put("accounts",accounts);
        
        
        if(!charges.isEmpty() ) {
            billingService.charge(charges.get(0));
            if(seriesIndex >= 0) {
                log.info("Validating series " + seriesIndex);
                MCSeries series = this.ccr.getSeriesList().get(seriesIndex);
                series.setPendingBillingEvent(null);
                series.setPaymentRequired(false);
                session.getServicesFactory().getDocumentService().registerPayment(ccr.getStorageId(), series.getMcGUID(), charges.get(0));
            }
        }
        
        return new StreamingResolution("text/plain", json.include("accounts").include("counters").serialize(output));
    }

    protected List<String> getAccounts() throws ServiceException, PHRException {
        List<String> accts = new ArrayList<String>();
        
        // First priority is the user's group, if they have one
        String groupAccId = session.getAccountSettings().getGroupId();
        if(!blank(groupAccId)) 
            accts.add(groupAccId);
        
        // Then we check if the current logged in user can pay themselves
        String accid = session.getOwnerPrincipal().getMcId();
        if(!blank(accid)) {
            if(!accts.contains(accid))
	            accts.add(accid);
        }
        
        // Finally, see if the patient can pay 
        if(!blank(ccr.getPatientMedCommonsId()) && !accts.contains(ccr.getPatientMedCommonsId())) 
            accts.add(ccr.getPatientMedCommonsId());
        
        return accts;
    }
    

    public String getType() {
        return type;
    }

    public void setType(String check) {
        this.type = check;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(int seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    public static Logger getLog() {
        return log;
    }
}
