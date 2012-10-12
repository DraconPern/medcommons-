/*
 * $Id: RegisterTrackingNumbers.java 3214 2009-02-05 05:11:10Z ssadedin $
 * Created on 19/07/2007
 */
package net.medcommons.router.selftests;

import static net.medcommons.modules.utils.TestDataConstants.DOCTOR_ID;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.interfaces.DocumentRegistration;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestResult;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;

public class RegisterTrackingNumbers implements SelfTest {
    
    public static String REGISTERED_TEST_GUID = null;

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(RegisterTrackingNumbers.class);
    
    public SelfTestResult execute(ServicesFactory services) throws Exception {
        
        CCRDocument doc = CCRDocument.createFromTemplate(DOCTOR_ID); 
        doc.setCreateTimeMs(System.currentTimeMillis());
        String guid = doc.calculateGuid();
        
        // Let's get ourselves a tracking number 
        String trackingNumber = services.getTrackingService().allocateTrackingNumber();
        log.info("Validated connectivity with tracking service by acquiring tracking number " + trackingNumber);
        
        services.getDocumentService().registerDocument(guid, DOCTOR_ID);
        DocumentRegistration reg = services.getTrackingService().registerTrackDocument(DOCTOR_ID, guid, PIN.hash("12345"), "12345", new Long(60), null);

        REGISTERED_TEST_GUID = guid;
        
        return null;
    }

}
