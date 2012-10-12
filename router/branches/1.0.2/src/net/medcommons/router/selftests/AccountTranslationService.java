/*
 * $Id: AccountTranslationService.java 2110 2007-10-08 06:47:32Z ssadedin $
 */
package net.medcommons.router.selftests;

import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.TestDataConstants;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestResult;

public class AccountTranslationService implements SelfTest {

    public SelfTestResult execute(ServicesFactory services) throws Exception {
        
        // let's translate some test data accounts to emails
        String testEmails [] = { TestDataConstants.DOCTOR_EMAIL, TestDataConstants.USER1_EMAIL };
        String testAccounts [] = { TestDataConstants.DOCTOR_ID, TestDataConstants.USER1_ID };
        
        String [] translatedAccounts = services.getAccountCreationService().translate(testAccounts);
        
        assert translatedAccounts[0].equals(testEmails[0]);
        assert translatedAccounts[1].equals(testEmails[1]);
        
        String [] translatedEmails = services.getAccountCreationService().translateAccounts(testEmails);
        assert !translatedEmails[0].equals(testEmails[0]); 
        assert translatedEmails[1].equals(testEmails[1]);
        
        return null;
    }
}
