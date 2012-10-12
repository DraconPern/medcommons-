/*
 * $Id: AccountImportActionTest.java 3808 2010-08-18 11:20:14Z ssadedin $
 * Created on 01/07/2008
 */
package net.medcommons.router.services.wado.stripes;

import junit.framework.JUnit4TestAdapter;
import net.medcommons.router.util.BaseTestCase;

import org.junit.Before;
import org.junit.Test;

public class AccountImportActionTest extends BaseTestCase {
    
    public AccountImportActionTest() throws Exception {
        super();
    }

    AccountImportAction action = null;

    @Before
    public void setUp() throws Exception {
        action = new AccountImportAction();
    }

    @Test
    public void testParseHealthURL() {
        
        action.setSourceUrl("http://yowie:7080/mctest/1234567890123456");
        action.parseHealthURL();
        assertEquals("http://yowie:7080/mctest/", action.getHealthURLHost());
        assertEquals("1234567890123456",action.getSourceAccount());
        
        action.setSourceUrl("http://yowie:7080/mctest/tracking/123456789012");
        action.parseHealthURL();
        assertEquals("http://yowie:7080/mctest/", action.getHealthURLHost());
        assertEquals("123456789012",action.getSourceAccount());
    }
    
    /*
    @Test
    public void testCancel() throws Exception {
        final ArrayList<String> cancellations = new ArrayList<String>();
        action.status = new AccountImportStatus();
        action.status.setDownloader(new DownloadFileAgent("", ServiceConstants.PUBLIC_MEDCOMMONS_ID, ServiceConstants.PUBLIC_MEDCOMMONS_ID, new String[] {},new File(".")) {
            @Override
            public void cancelStream() {
                cancellations.add("true");
            }
        });
        action.setSession(new UserSession(null,null));
        action.cancel();
        assertEquals(1, cancellations.size());
        assertEquals(State.CANCELLATION_REQUESTED,action.status.getState());
    }
    */
    
    public static junit.framework.Test suite() { 
        return new JUnit4TestAdapter(AccountImportActionTest.class); 
    }

}
