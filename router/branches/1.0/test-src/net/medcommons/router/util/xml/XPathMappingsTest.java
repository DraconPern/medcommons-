/*
 * $Id$
 * Created on 8/04/2005
 */
package net.medcommons.router.util.xml;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jdom.JDOMException;
import org.junit.Test;

import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.phr.ccr.CCRBuilder;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.router.util.BaseTestCase;

/**
 * @author ssadedin
 */
public class XPathMappingsTest extends BaseTestCase {
    
    public XPathMappingsTest(String name) throws ConfigurationException, Exception {
        super(name);
    }

    /**
     * Optionally can specify a result to be expected from a given path
     * as a string value in the hashmap below.
     * (key = path name,  value = expected result from sample file)
     */
    HashMap<String,String> sampleResults = new HashMap<String, String>();
    
    /**
     * Sample document that contains XML to be tested against
     */
    XMLPHRDocument sampleDoc;
   
    public void setUp() throws JDOMException, IOException {
        sampleResults.put("sourceName", "sourceGivenName sourceFamilyName");
        sampleResults.put("patientName", "patientGivenName patientFamilyName");
        sampleDoc = (XMLPHRDocument) new CCRBuilder().build(new File("test-src/net/medcommons/router/util/xml/sample1.0.xml"));
    }

    public void testMappings() throws Exception {

        List<String> testPaths = Arrays.asList("ccrDateTime", 
                        "toEmail", 
                        "patientEmail", 
                        "patientPhoneNumber", 
                        "purposeText", 
                        "patientGivenName", 
                        "patientFamilyName",
                        "sourceName",
                        "patientName"
                        );
        
        for(String name : testPaths) {
            
            String result = name;
            if(sampleResults.containsKey(name)) {
                result = sampleResults.get(name);
            }
            
            System.out.print("Path " + name + " -");
            System.out.flush();

            // Execute the path on the sample document
            assertEquals(result, xpath.getValue(sampleDoc, name));

            System.out.println(" succeeded.");
        }
    }
    
    @Test
    public void testActorMedCommonsId() throws Exception, IOException {
        CCRElement actor = (CCRElement) sampleDoc.getRoot().getChild("Actors").getChildren().get(0);
        
        assertNotNull(actor);
        
        assertEquals("actorMedCommonsId", xpath.getValue(actor, "actorMedCommonsId"));
    }
    
    @Test
    public void testReferenceSize() throws Exception, IOException {
        CCRElement ref = (CCRElement) sampleDoc.getRoot().getChild("References").getChildren().get(1);
        assertNotNull(ref);
        assertEquals("360000", xpath.getValue(ref, "referenceSize"));
    }
}
