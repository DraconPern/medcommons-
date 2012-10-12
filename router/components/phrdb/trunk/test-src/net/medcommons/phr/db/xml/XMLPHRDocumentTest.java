/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr.db.xml;

import net.medcommons.phr.BaseTest;
import net.medcommons.phr.PHRDocument;
import net.medcommons.phr.ValidationResult;

public class XMLPHRDocumentTest extends BaseTest {
    
    public static String [] PROPERTIES = {"patientAge", "patientGender", "patientExactDateOfBirth", "patientDICOMId", "patientAddress1", "patientCity",
        "patientState", "patientPostalCode", "patientCountry", "purposeText", "objectiveText", "assessmentText",
        "planText", "toEmail", "toEmail", "patientEmail", "sourceEmail", "patientGivenName", "patientFamilyName",
        "patientMiddleName","patientPhoneNumber" };


    public XMLPHRDocumentTest(String arg0) {
        super(arg0);
    }
    
    XMLPHRDB db = new XMLPHRDB();
    PHRDocument d = null;

    protected void setUp() throws Exception {
        super.setUp();
        d = db.open("ccr_lots_of_data");
    }

    public void testAddValue() {
        fail("Not yet implemented");
    }

    public void testDelete() throws Exception {
        
        assertEquals("Failed to retrieve name value", "Jane",d.getValue("patientGivenName"));
        
        // Delete patient name
        d.remove("patientGivenName");
        
        assertEquals("Failed to delete name value", null,d.getValue("patientGivenName"));
    }

    public void testGetValue() throws Exception {
        assertEquals("Failed to retrieve simple value", "Jane",d.getValue("patientGivenName"));
    }

    public void testSetValue() throws Exception {
        d.setValue("patientGivenName", "Fred");
        assertEquals("failed to set patient name", "Fred", d.getValue("patientGivenName"));
        
        // Set a value that does not exist
        String objText = d.getValue("objectiveText");
        assertNull("Should be no objective text", objText);
        d.setValue("objectiveText", "Objectivity roolz");
        assertEquals("Failed to set non-existent field", "Objectivity roolz", d.getValue("objectiveText"));
        
        // Test every property
        for(String p : PROPERTIES) {
            d.setValue(p, "Foo Bar");
            assertEquals("Failed to set property " + p, "Foo Bar", d.getValue(p));
        }
        
    }
    
    public void testValidate() throws Exception {
        // Default document should validate
        ValidationResult v = d.validate();
        
        assertTrue("Test document failed to validate clear", v.isClear());
        
        // Delete the patient node - should now fail
        d.getRoot().removeChild(d.getRoot().getChild("Patient"));
        
        v = d.validate(); 
        
        assertFalse("Test document failed to validate clear", v.isPassed());
    }
}
