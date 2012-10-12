/*
 * $Id$
 * Created on 19/07/2007
 */
package net.medcommons.router.selftests;

import org.jdom.Document;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.rest.RESTUtil;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestFailure;
import net.medcommons.router.selftest.SelfTestResult;
import static net.medcommons.modules.utils.Str.*;

public class AccountServerConnectivity implements SelfTest {

    public SelfTestResult execute(ServicesFactory services) throws Exception {
        
        // Is the configuration there?
        String accountServerURL = Configuration.getProperty("AccountServer");
        if(blank(accountServerURL)) {
            return new SelfTestFailure("Configuration for Account Server URL not found");
        }
        
        // Can we query the ping service?
        Document d = RESTUtil.executeUrl(accountServerURL + "/pingService.php");
        
        return null;
    }
}
