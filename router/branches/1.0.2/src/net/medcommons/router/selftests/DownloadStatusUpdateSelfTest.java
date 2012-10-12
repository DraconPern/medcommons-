/*
 * $Id: DownloadStatusUpdateSelfTest.java 2223 2007-11-02 11:05:47Z ssadedin $
 * Created on 31/10/2007
 */
package net.medcommons.router.selftests;

import net.medcommons.modules.services.interfaces.AccountService;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.TestDataConstants;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestResult;

public class DownloadStatusUpdateSelfTest implements SelfTest {

    public SelfTestResult execute(ServicesFactory services) throws Exception {
        AccountService acctService = services.getAccountService();
        acctService.updateWorkflow(String.valueOf(Math.random()+System.currentTimeMillis()), TestDataConstants.GROUP_ACCT_ID, TestDataConstants.USER1_ID, "SelfTest", "Foo");
        return null;
    }

}
