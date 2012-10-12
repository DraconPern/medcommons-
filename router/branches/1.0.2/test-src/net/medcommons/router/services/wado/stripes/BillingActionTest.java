/*
 * $Id: BillingActionTest.java 2887 2008-09-03 09:46:55Z ssadedin $
 * Created on 03/09/2008
 */
package net.medcommons.router.services.wado.stripes;


import static net.medcommons.modules.utils.TestDataConstants.USER1_AUTH;
import static net.medcommons.modules.utils.TestDataConstants.USER1_ID;
import static org.junit.Assert.assertEquals;

import java.util.List;

import junit.framework.JUnit4TestAdapter;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.junit.Before;
import org.junit.Test;

public class BillingActionTest {

    @Before
    public void setUp() throws Exception {
    }
    
    /**
     * Test that even if there are multiple reasons to bill a given 
     * account, it is still only returned once in the list of accounts
     * to bill.
     * 
     * @throws ServiceException
     * @throws PHRException
     */
    @Test
    public void getAccountsDuplicated() throws ServiceException, PHRException {
        
        UserSession s = new UserSession(USER1_ID,USER1_AUTH) {

            @Override
            public AccountSettings getAccountSettings() throws ServiceException {
                return new AccountSettings();
            }

            @Override
            public AccountSpec getOwnerPrincipal() throws ServiceException {
                return new AccountSpec(USER1_ID);
            }
        };
        
        
        BillingAction b = new BillingAction();
        b.setCcr(new CCRDocument() {
            @Override
            public String getPatientMedCommonsId() throws PHRException {
                return USER1_ID;
            }
            
        });
        
        b.setSession(s);
        
        List<String> accounts = b.getAccounts();
        
        assertEquals(1, accounts.size());
    }

    public static junit.framework.Test suite() { 
        return new JUnit4TestAdapter(BillingActionTest.class); 
    }
}
