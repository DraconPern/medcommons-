/*
 * $Id: BillingService.java 2636 2008-06-02 22:39:02Z ssadedin $
 * Created on 15/05/2008
 */
package net.medcommons.modules.services.interfaces;

import java.util.List;
import java.util.Set;

/**
 * A service that can execute billing operations to charge users for activity
 * that they are performing on the MedCommons network.
 * <p>
 * The archetypal pattern of usage for this class is to first call {@link #resolvePayer(List, Set)}
 * with a list of accounts to resolve a set of potential charges that can be executed
 * to pay for the operation.   One of the returned {@link BillingCharge}</code> objects 
 * can then be passed to the {@link #charge(BillingCharge)} operation to execute the transaction. 
 * 
 * @see BillingCharge
 * @see BillingEvent
 * 
 * @author ssadedin
 */
public interface BillingService {
    
    /**
     * Attempts to resolve a payer for the specified transaction.
     * <p>
     * Multiple payers may be identified in which case a list of payers
     * will be returned.  In the case of multiple payers returned,
     * each payer can pay for the complete operation.  The caller can
     * then select which of these payers should actually be charged based
     * on whatever logic they wish (even consulting the end user, if desired).
     * <p>
     * Once the desired payer is known, the selected BillingCharge should
     * be passed to the <code>charge()</code> operation to execute the transaction.
     */
    List<BillingCharge> resolvePayer(List<String> accounts, Set<BillingEvent> events) throws ServiceException;
    
    
    /**
     * Execute the given billing charge
     */
    void charge(BillingCharge charge) throws ServiceException;


    /**
     * Query for the available credits for the given account and return them
     * 
     * @param accountId MedCommons Account Id of user to query
     * @return counters indicating the current level of credit for the specified user
     */
    BillingCounters queryAvailableCredits(String accountId) throws ServiceException;

}
