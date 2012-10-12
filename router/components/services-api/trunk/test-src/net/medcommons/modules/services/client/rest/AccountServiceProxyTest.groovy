package net.medcommons.modules.services.client.rest;

import static org.junit.Assert.*;

import org.junit.Before;

import net.medcommons.modules.services.interfaces.AccountHolderRight;
import net.medcommons.modules.utils.TestDataConstants;
import net.medcommons.rest.RESTUtil;
import static net.medcommons.modules.utils.TestDataConstants.*;
import static net.medcommons.modules.services.interfaces.Rights.*;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

class AccountServiceProxyTest extends GroovyTestCase {
    
    static {
        RESTUtil.init(new TestConfig());
        BasicConfigurator.configure();
    }
    
    AccountServiceProxy a = new AccountServiceProxy(TestDataConstants.USER1_AUTH);
    DocumentServiceProxy ds = new DocumentServiceProxy(TestDataConstants.DOCTOR3_AUTH)
    
    @Before
    public void setUp() throws Exception {
        RESTUtil.testResponse = null
    }
    
    @Test
    public void testQueryFeatures() {
        
        // Default call should work, but we can't rely on what it will return
        def features = a.queryFeatures();
        
        // It's more interesting to test specific results
        RESTUtil.testResponse = "{status:'ok',result: [ { name: 'hello', description: 'world',  enabled: false }] }";
        features = a.queryFeatures()
        
        assert features.size() == 1
        assert features[0].name == 'hello'
        assert !features[0].enabled
    }
    
    @Test
    public void testUpdateConsents() {
        a.updateSharingRights(USER2_ID, [
             new AccountHolderRight(DOCTOR3_ID, "RW")
        ])
        assert [READ,WRITE] as Set == ds.getAccountPermissions(USER2_ID)
        
        a.updateSharingRights(USER2_ID, [
             new AccountHolderRight(DOCTOR3_ID, "R")
        ])
        assert [READ] as Set == ds.getAccountPermissions(USER2_ID)
    }
    
}
