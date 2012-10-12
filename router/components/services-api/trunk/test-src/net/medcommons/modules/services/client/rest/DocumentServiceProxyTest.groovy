package net.medcommons.modules.services.client.rest;

import static org.junit.Assert.*;

import static net.medcommons.modules.utils.TestDataConstants.*;
import static net.medcommons.modules.services.interfaces.Rights.*;

import org.junit.Before;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.rest.RESTConfiguration;
import net.medcommons.rest.RESTConfigurationException;
import net.medcommons.rest.RESTUtil;

import org.junit.Assert;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

public class DocumentServiceProxyTest extends GroovyTestCase {
    
    static {
        RESTUtil.init(new TestConfig());
        BasicConfigurator.configure();
    }
    
    @Before
    public void setUp() throws Exception {
    }
    
    static String KEY = "Zr0yrPiRMcD33XdTt59K7w=="

    DocumentServiceProxy ds = new DocumentServiceProxy(USER1_AUTH)
        
    
    @Test
    public void testRegisterKey() {
        
        // Remove any existing key
        ds.deleteKey(USER1_ID)
        
        // No key should be returned
        def keys = ds.getDocumentDecryptionKey(USER1_ID, USER1_AUTH, null)
        assert keys.size() == 0
        
        // Register a new key
        ds.registerKey(USER1_ID, KEY, KEY)
        
        // Should not be able to register another key for this user!
        shouldFail {
            ds.registerKey(USER1_ID, "Ar0yrPiRMcD33XdTt59K7w==", "Ar0yrPiRMcD33XdTt59K7w==")
        }
        
        // Should get back the same key as we registered
        keys = ds.getDocumentDecryptionKey(USER1_ID, USER1_AUTH, null)
        
        assert keys.size() == 1
        assert keys[0].key == KEY : "Incorrect key value returned"
        
    }
    
    /**
     * Test that "NONE" is accepted as an encryption key
     */
    @Test
    public void testNoEncryption() {
        
        // Remove any existing key
        ds.deleteKey(USER1_ID)
        ds.registerKey(USER1_ID, "NONE", "NONE")
        
        // Should not be able to register another key for this user!
        shouldFail {
            ds.registerKey(USER1_ID, "Ar0yrPiRMcD33XdTt59K7w==", "Ar0yrPiRMcD33XdTt59K7w==")
        }
        
        def keys = ds.getDocumentDecryptionKey(USER1_ID, USER1_AUTH, null)
        
        assert keys.size() == 1
        assert keys[0].key == "NONE" : "Incorrect Key value returned"
    }
    
    /**
     * Test that access can be granted to accounts 
     */
    @Test
    public void testGrantAccountAccess() {
        
        println "Testing grant account access"
        
        ds.grantAccountAccess(USER1_ID, USER1_ID, "RW")
        
        assert [READ,WRITE] as Set == ds.getAccountPermissions(USER1_ID)
        
    }
}
