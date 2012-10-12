/*
 * $Id: BillingService.java 3322 2009-04-22 05:56:41Z ssadedin $
 * Created on 22/05/2008
 */
package net.medcommons.modules.services.impl;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static net.medcommons.modules.utils.Algorithm.map;
import static net.medcommons.modules.utils.Algorithm.sum;
import static net.medcommons.modules.utils.Str.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.services.interfaces.BillableAccount;
import net.medcommons.modules.services.interfaces.BillingCharge;
import net.medcommons.modules.services.interfaces.BillingCounters;
import net.medcommons.modules.services.interfaces.BillingEvent;
import net.medcommons.modules.services.interfaces.BillingEventType;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.rest.RESTException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.modules.utils.Algorithm;
import net.medcommons.modules.utils.Function;
import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * Concrete implementation of BillingService that implements storage of underlying
 * counters by calling PHP web service implementation written by Bill.
 * 
 * @author ssadedin
 */
public class BillingService implements net.medcommons.modules.services.interfaces.BillingService {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(BillingService.class);
    
    private String authToken;
    
    private SHA1 sha1 = new SHA1().initializeHashStreamCalculation();
    
    public BillingService(String authToken) {
        this.authToken = authToken;
    }

    public void charge(BillingCharge charge) throws ServiceException {
        
        try {
            ArrayList<String> params = new ArrayList<String>(); 
            params.add("btk");
            params.add(charge.getAccount().getToken());
            for(BillingEventType type : charge.getCounters().keySet()) {
                params.add(type.toString());
                int count = charge.getCounters().get(type);
                
                if(count < 0) 
                    throw new ServiceException("Charge must be greater or equal to zero.  Value provided = " + count);
                
                // The counter needs to be decremented by the amount of the charge
                count = count * -1;
                
                params.add(String.valueOf(count));
            }
            
            RESTUtil.call(authToken, "BillingService.wsCounters", params.toArray(new String[] {}));
            
            // TODO: This transaction id should be coming from the billing service
            charge.setTransactionId(this.sha1.calculateStringHash( charge.getAccount().getToken() + System.currentTimeMillis() + Math.random()));
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to charge billing charge " + charge.toString(),e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<BillingCharge> resolvePayer(List<String> accounts, Set<BillingEvent> events) throws ServiceException {
        try {
            // In the current implementation, we can only query counters by passing the billing tokens
            // Since we don't know the billing tokens, we have to query them first (painful)
            // The API should change so for now we tolerate this, but if it doesn't
            // should push the caching of the billing tokens back to the caller so that we
            // don't do this repeatedly
            Document doc = RESTUtil.call(authToken, "BillingService.wsCounters", "accid",join(accounts,","));
            
            List<BillingCharge> charges = new ArrayList<BillingCharge>();
            
            // Get the list of tokens
            ArrayList<String> tokens = new ArrayList<String>();
            List<Element> bindings = doc.getRootElement().getChildren("binding");
            for(Element b : bindings) {
                String billingId = b.getChildTextTrim("billingid");
                String accid = b.getChildTextTrim("accid");
                 
                if(Str.blank(billingId))  {
                    log.debug("no billing id for account " + accid);
                    continue;
                }
                    
                BillingCharge c = new BillingCharge();
                c.setAccount(new BillableAccount(accid, billingId));
                tokens.add(billingId);
                charges.add(c);
            }
            
            if(log.isInfoEnabled())
                log.info("Found billingids " + join(tokens,",") + " for payment resolution");
            
            Map<String, Element> counterIndex = queryAvailableCredits(tokens);
            
            BillingCounters requiredCredits = calculateRequiredCredits(events);
            
            // See who, if anyone can afford to pay this bill
            List<BillingCharge> result = new ArrayList<BillingCharge>();
            for(BillingCharge c : charges) {
                
                // Will set to false if one of the charges is not satisfied
                boolean suffcientCredit = true;
                
                // The billing events must all be satisfied by the available counters
                for(BillingEventType eventType : BillingEventType.values()) {
                    
                    Element counter = counterIndex.get(c.getAccount().getToken()) .getChild(eventType.toString());
                    
                    int requiredCredit = requiredCredits.get(eventType);
                    
                    int counterValue = (counter == null) ? 0 : parseInt(counter.getTextTrim());
                   
                    if(counterValue < requiredCredit) {
                        log.debug(format("Insufficient credit (%d) in account (%s) to satisfy billing charge (%d) for counter (%s)",
                                        counterValue, c.getAccount().toString(), requiredCredit, eventType.toString()));
                        suffcientCredit = false;
                        break;
                    }
                    
                    if(requiredCredit > 0)
                        c.getCounters().put(eventType, requiredCredit);
                }
                
                // If we did not satisfy the charge from this account, continue looking for more
                if(!suffcientCredit)
                    continue;
                
                result.add(c);
            }
            return result;
        }
        catch (RESTException e) {
            throw new ServiceException("Failed while resolve payers for accounts " + Str.join(accounts,",") + " for " + events.size() + " billed items",e);
        }
    }
    
    public List<String> queryBillingTokens(List<String> accounts) throws ServiceException {
        try {
            // In the current implementation, we can only query counters by passing the billing tokens
            // Since we don't know the billing tokens, we have to query them first (painful)
            // The API should change so for now we tolerate this, but if it doesn't
            // should push the caching of the billing tokens back to the caller so that we
            // don't do this repeatedly
            Document doc = RESTUtil.call(authToken, "BillingService.wsCounters", "accid",join(accounts,","));
            
            List<BillingCharge> charges = new ArrayList<BillingCharge>();
            
            // Get the list of tokens
            ArrayList<String> tokens = new ArrayList<String>(accounts.size());
            List<Element> bindings = doc.getRootElement().getChildren("binding");
            for(Element b : bindings) {
                String billingId = b.getChildTextTrim("billingid");
                String accid = b.getChildTextTrim("accid");
                int index = accounts.indexOf(accid);
                if(Str.blank(billingId))  {
                    log.debug("no billing id for account " + accid);
                    continue;
                }
                while(index >= tokens.size())
                    tokens.add(null);
                tokens.set(index,billingId);
            }
            return tokens;
        }
        catch (RESTException e) {
            throw new ServiceException("Failed to query billing tokens for accounts " + join(accounts,","),e);
        }
    }
    
    public BillingCounters queryAvailableCredits(String accountId) throws ServiceException {
        try {
            List<String> tokens = queryBillingTokens(singletonList(accountId));
            
            if(tokens.isEmpty()) {
                log.info("User " + accountId + " does not have a billing token");
                return new BillingCounters();
            }
            
            log.info("Found billing token " + tokens.get(0) + " for user " + accountId);
            
            Map<String, Element> credits = queryAvailableCredits(tokens);

            // Get the counters for our token
            Element counters = credits.get(tokens.get(0));
            BillingCounters result = new BillingCounters();
            for(BillingEventType eventType : BillingEventType.values()) {
                Element e = counters.getChild(eventType.toString());
                if(e != null) {
                    result.put(eventType, parseInt(e.getTextTrim()));
                }
            }
            log.debug("Retrieved counters " + result + " for user " + accountId);
            return result;
        }
        catch (RESTException e) {
            throw new ServiceException("Failed to query available credits for account " + accountId,e);
        }
    }

    /**
     * Query billing service for available credits for the given list of billing tokens.
     * 
     * Return a map indexed on billing token, each token mapped to JDOM Element with returned
     * info on available credits in child elements.
     * 
     * @param tokens
     * @return
     * @throws RESTException
     */
    private Map<String, Element> queryAvailableCredits(List<String> tokens) throws RESTException {
        Document doc;
        // Get all the counters
        doc = RESTUtil.call(authToken, "BillingService.wsCounters", "btk", Str.join(tokens,","));
        
        List<Element> counters = doc.getRootElement().getChildren("counters");
        
        // Create an index of each billing token pointing to it's credits
        Map<String,Element> counterIndex = Algorithm.index(counters, new Function<String,Element>() {
            public String $(Element u) {
                return u.getChildText("billingid");
            }
        });
        return counterIndex;
    }

    /**
     * Sum over all the given events to generate a total count for all 
     * tokens passed, and return as a single BillingCounters object.
     * 
     * @param events
     * @return
     */
    protected BillingCounters calculateRequiredCredits(Set<BillingEvent> events) {
        // Create an index of the total required credits of each type
        BillingCounters requiredCredits = new BillingCounters();
        for(final BillingEventType eventType : BillingEventType.values()) {
            
            // May have more than one billing event of the same type - so
            // sum them all to find the required total credit.
            int requiredCredit = sum(map(events, new Function<Integer,BillingEvent>() {
                public Integer $(BillingEvent u) {
                    return u.getType() == eventType ? u.getQuantity() : 0;
                } 
            })).intValue();
            
            requiredCredits.put(eventType, requiredCredit);
        }
        return requiredCredits;
    }

}
