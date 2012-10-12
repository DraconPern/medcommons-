/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr.db.xml;

import javax.naming.NamingException;

import net.medcommons.phr.BaseTest;
import net.medcommons.phr.PHRDocument;
import net.medcommons.phr.PHRException;

public class XMLPHRDBTest extends BaseTest {

    public XMLPHRDBTest(String arg0) throws NamingException {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    XMLPHRDB db = new XMLPHRDB();
    
    public void testOpenExisting() throws Exception {
        PHRDocument doc = db.open("ccr_lots_of_data");
        assertNotNull(doc);
    }

    public void testOpenNonExistent() throws Exception {
        try {
            PHRDocument doc = db.open("this_doesnt_exist");
            fail("Opening non-existent document did not throw execption");
        }
        catch (PHRException e) {
            
        }        
    }
    
    public void testSave() throws Exception {
        
        // First open the test document
        PHRDocument doc = db.open("ccr_lots_of_data");
        
        // Now save it
        db.save("ccr_copy",doc);
        
        // Now check it can be opened independently
        PHRDocument doc2 = db.open("ccr_copy");
        
        assertNotNull("Unable to open saved document", doc2);
        
        // Let's delete it
        db.delete("ccr_copy");
        
        // Now we should not be able to open it
        try {
            PHRDocument doc3 = db.open("ccr_copy");
            fail("Should not be able to open deleted document");
        }
        catch (PHRException e) {
            // failed as expected
        }
        
    }
}
