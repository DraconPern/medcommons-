/*
 * $Id$
 * Created on 19/07/2007
 */
package net.medcommons.router.selftests;

import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.TestDataConstants;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestResult;

public class QueryAccountServer implements SelfTest {

    public SelfTestResult execute(ServicesFactory services) throws Exception {
        
        // Can we query the ping service?
        AccountSettings settings = services.getAccountService().queryAccountSettings(TestDataConstants.USER1_ID);
        assert settings != null : "Account Settings for standard test data set were returned as null";
        
        return null;
    }
}
