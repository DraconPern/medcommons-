/*
 * $Id: ActivitySessionTest.java 2581 2008-05-01 23:19:50Z ssadedin $
 * Created on 02/11/2007
 */
package net.medcommons.modules.services.interfaces;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ActivitySessionTest {

    @Test
    public void testEventOrder() {
        ActivityEvent access = evt(ActivityEventType.PHR_ACCESS);
        ActivityEvent consent = evt(ActivityEventType.CONSENT_UPDATE);
        ActivityEvent send = evt(ActivityEventType.PHR_SEND);
        
        // Access by itself should return access
        ActivitySession s = new ActivitySession("1234");
        s.addEvent(access);
        assertEquals(access, s.getPrimaryEvents().get(0));
        
        // Consent should override access
        s.addEvent(consent);
        assertEquals(consent, s.getPrimaryEvents().get(0));
        assertEquals(1, s.getPrimaryEvents().size());
        
        // Send should leave Send in place
        s.addEvent(send);
        assertEquals(consent, s.getPrimaryEvents().get(0));
        assertEquals(2, s.getPrimaryEvents().size());
    }
    
    @Test
    public void testCombineSessions() {
        ActivityEvent e1 = evt(ActivityEventType.PHR_ACCESS);
        ActivityEvent e2 = evt(ActivityEventType.PHR_ACCESS);
        
        // Same session id, should have same session
        assertEquals(true,ActivitySession.isSharedSession(e1, e2, 10000));
        
        // Change session id
        e2.setSessionId("adifferentsessionid");
        assertEquals(true,ActivitySession.isSharedSession(e1, e2, 10000));
        
        // Change time
        e2.setTimeStampMs(0);
        assertEquals(false,ActivitySession.isSharedSession(e1, e2, 10000));
    }
    
    public ActivityEvent evt(ActivityEventType type) {
        ActivityEvent e = new ActivityEvent(type,"Foo", new AccountSpec("1"),"2", "1234", "12345");
        return e;
    }
    
}
