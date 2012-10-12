/*
 * $Id: ConfigurationServletTest.java 2485 2008-03-18 03:12:59Z ssadedin $
 * Created on 18/03/2008
 */
package net.medcommons.router.configuration.tomcat;

import static org.junit.Assert.assertEquals;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.router.util.BaseTestCase;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationServletTest {
    
    @Before
    public void setUp() throws Exception {
        BaseTestCase.initializeTestEnvironment();
    }

    @Test
    public void testSetAccountsBaseUrl() throws Exception {
        Configuration.getAllProperties().setProperty("AccountServer", "http://foo.bar.baz/acct/ws");
        
        ConfigurationServlet servlet = new ConfigurationServlet();
        servlet.setAccountsBaseUrl();
        
        assertEquals("http://foo.bar.baz/", Configuration.getProperty("AccountsBaseUrl"));
        
    }

}
