/**
 * 
 */
package net.medcommons.application.dicomclient

import org.junit.Before
import org.apache.log4j.BasicConfiguratorimport net.medcommons.application.dicomclient.utils.StatusDisplayManagerimport java.util.logging.Levelimport org.junit.Test

/**
 * @author ssadedin
 */
public class ImportHandlerTest {
    
    static {
        BasicConfigurator.configure()
        StatusDisplayManager.testMode = true
        Logger.getLogger("net.sourceforge.pbeans").level = Level.WARNING
     }

    
    @Before
    void setup() {
        DB.testMode()
        DB.get().insert(ref)
    }

    @Test
    void testProcessSOPInstance() {
        
    }
    
}
