/*
 * $Id: QueryTrackingNumbers.java 2373 2008-01-29 07:50:22Z ssadedin $
 * Created on 19/07/2007
 */
package net.medcommons.router.selftests;

import org.apache.log4j.Logger;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.interfaces.DocumentReference;
import net.medcommons.modules.services.interfaces.DocumentRegistration;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import static net.medcommons.modules.utils.TestDataConstants.*;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestResult;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

/**
 * Tests that a tracking number previously registered can be retrieved.
 * 
 * @author ssadedin
 */
public class QueryTrackingNumbers implements SelfTest {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(QueryTrackingNumbers.class);
    
    public SelfTestResult execute(ServicesFactory services) throws Exception {
        
        String guid = RegisterTrackingNumbers.REGISTERED_TEST_GUID;
        
        if(guid == null)
            throw new Exception("Expected test guid not available from previous test");
        
        // Let's check we get the right details back
        DocumentReference [] refs = services.getDocumentService().resolve(DOCTOR_ID, guid); 
        if(refs.length == 0)
            throw new Exception("Unable to retrieve test document added to secure server as ("+DOCTOR_ID+","+guid+")");

        assert "12345".equals(refs[0].getTrackingReference().getPin()) : "Retrieved incorrect PIN for test document";

        return null;
    }

}
