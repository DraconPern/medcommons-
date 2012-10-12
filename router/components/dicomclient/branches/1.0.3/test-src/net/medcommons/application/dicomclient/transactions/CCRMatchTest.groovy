/**
 * 
 */
package net.medcommons.application.dicomclient.transactions

import java.util.List
import org.junit.Assert
import org.junit.Test
import org.junit.BeforeClassimport net.sourceforge.pbeans.Storeimport net.medcommons.application.dicomclient.utils.DBimport static net.medcommons.modules.utils.TestDataConstants.*import org.junit.Before
/**
 * @author ssadedin
 */ 
public class CCRMatchTest {
    
    def params = [ fileLocation: "foo.xml", guid: "1234567890", storageId: DOCTOR_ID, timeEntered: new Date() ]
    
    CCRRef ref = new CCRRef(params)
    
    @Before
    void setup() {
        DB.testMode()
        DB.get().insert(ref)
    }
    
    /*
    @Test
    void testGetCCRRef(){
        def ccr = CCRMatch.getCCRRef(DOCTOR_ID, ref.guid)
        assert ccr == ref : "Did not receive back expected CCR"
        
        ccr = CCRMatch.getCCRRef("xxxxxx", ref.guid)
        assert ccr == null : "returned a CCR for non-existent storage id"
        
        DB.get().insert(new CCRRef(params))
        try {
	        ccr = CCRMatch.getCCRRef(DOCTOR_ID, ref.guid)
	        assert false : "expected exception due to multiple matches"
        }
        catch(IllegalStateException ex) {
            // expected
        }
    }
    */
    
    @Test
    void testGetCCRRefs() {
        def params2 = params.clone()
        params2.guid = "111111111111111111111111111111"
        DB.get().insert(new CCRRef(params2))
        
        def refs = CCRMatch.getCCRReferences(DOCTOR_ID)
        assert refs.size() == 2 : "size was ${refs.size()}"
        
        refs = CCRMatch.getCCRReferences("212312323423423423")
        assert refs == null
    }
    
}
