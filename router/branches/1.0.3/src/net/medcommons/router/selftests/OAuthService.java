/*
 * $Id: OAuthService.java 3321 2009-04-22 05:42:30Z ssadedin $
 * Created on 31/10/2007
 */
package net.medcommons.router.selftests;

import org.apache.log4j.Logger;
import org.jdom.Document;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.rest.RESTUtil;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestResult;

public class OAuthService implements SelfTest {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(OAuthService.class);

    public SelfTestResult execute(ServicesFactory services) throws Exception {
        String url = Configuration.getProperty("OAuthService.verifyOAuthRequest.url","");
        url += "?url=http://www.google.com&oauth_consumer_key=dpf43f3p2l4k3l03&oauth_nonce=kllo9940pd9333jh&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1191242096&oauth_token=nnch734d00sl2jdk&oauth_version=1.0";
        
        Document result = RESTUtil.executeUrl(url);
        
        return null;
    }
}
