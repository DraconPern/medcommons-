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

public class CommonsServerConnectivity implements SelfTest {

    public SelfTestResult execute(ServicesFactory services) throws Exception {
        
        // Is the configuration there?
        String commonsServerURL = Configuration.getProperty("CommonsServer");
        if(blank(commonsServerURL)) {
            return new SelfTestFailure("Commons Server Configuration not found");
        }
        
        // Can we query the ping service?
        Document d = RESTUtil.executeUrl(commonsServerURL + "/pingService.php");
        
        return null;
    }
}
