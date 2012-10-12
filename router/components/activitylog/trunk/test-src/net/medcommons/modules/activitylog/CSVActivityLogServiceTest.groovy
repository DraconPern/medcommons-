package net.medcommons.modules.activitylog

import org.apache.log4j.spi.LoggingEvent;

import net.medcommons.modules.utils.TestDataConstants;
import static net.medcommons.modules.utils.TestDataConstants.*;

import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ActivityEvent;

import org.junit.Before;
import org.apache.log4j.*;
import org.apache.log4j.varia.*;
import org.junit.Test;

import java.io.StringWriter;



public class CSVActivityLogServiceTest {

    static {
        new File('logs').mkdirs()
    }
    
    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure()
    }
    
    AccountSpec user1 = new AccountSpec(USER1_ID)
    AccountSpec user2 = new AccountSpec(USER2_ID)
    ActivityEvent evt = 
    new ActivityEvent(ActivityEventType.PHR_UPDATE, "Unit Test",
    user1, USER2_ID)
    
    StringWriter csv = new StringWriter()
    
    CSVActivityLogService svc = [
	    getReader:{new StringReader(csv.toString()) },
	    getWriter:{csv}
    ] as CSVActivityLogService
    
    @Test
    public void testSaveAndLoad() {
        
        // Write to the service
        svc.log(evt);
        
        assert csv.toString() != ""
        assert csv.toString() =~ /Unit Test/
        println csv
        
        // Let's load it again
        def evts = svc.load(USER1_ID, 0, 10)
        
        assert evts.size() == 1 : "Unexpected number of events"
        assert evts[0].description == "Unit Test"
    }
    
    /**
     * Patient activity should be sent to a log4j appender under the name
     * <code>activity.[mcid]</code>
     */
    @Test
    public void testLog4J() {
        
        LoggingEvent logEvent
        
        Appender a = [
                doAppend: { logEvent = it }
                ] as NullAppender
        
        Logger.getLogger("activity").addAppender(a)
        
        svc.log(evt)
        
        assert logEvent != null : "No activity log event sent for patient"
        assert logEvent.categoryName == "activity.${USER2_ID}"
        assert logEvent.message =~ /Unit Test/
    }
}
