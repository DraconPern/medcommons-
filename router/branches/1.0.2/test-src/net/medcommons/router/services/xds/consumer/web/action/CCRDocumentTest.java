/*
 * $Id$
 * Created on 26/09/2005
 */
package net.medcommons.router.services.xds.consumer.web.action;

import static net.medcommons.document.ccr.CCRConstants.CCR_NAMESPACE_URN;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.crypto.GuidGenerator;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.ccr.CCRElementFactory;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.util.BaseTestCase;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;


public class CCRDocumentTest extends BaseTestCase {
    
    private GuidGenerator guidGenerator = null;
    
    private String ccrXml = null;
    
    private CCRDocument ccr = null;
    
    private Namespace ccrns = Namespace.getNamespace(CCR_NAMESPACE_URN);

    public static void main(String[] args) {
    }

    public CCRDocumentTest(String arg0) throws NoSuchAlgorithmException, Exception {
        super(arg0);
        this.guidGenerator = new GuidGenerator();
        CCRDocument.setTemplatePath("etc/static-files/xds-templates");
        Configuration.getAllProperties().setProperty("CCRXSDLocation","etc/schema/ccr/CCR_20051109.xsd");
    }

    private String read(String fileName) throws FileNotFoundException, IOException {
        FileInputStream in = new FileInputStream(fileName);
        byte [] buffer = new byte[100000];
        int read = in.read(buffer);
        return new String(buffer,0,read);
    }

    protected void setUp() throws Exception {
        super.setUp();        
        this.ccr = getCcr("test-src/net/medcommons/router/services/xds/consumer/web/action/minimalCcr.xml");
    }
    
    /**
     * Test that address components are created in the correct order
     */
    public void testAddressOrder() throws Exception {        
        // Note we create them OUT of order
        ccr.createPath("patientCountry").setText("patientCountry");
        ccr.createPath("patientPostalCode").setText("patientPostalCode");
        ccr.createPath("patientState").setText("patientState");
        ccr.createPath("patientCity").setText("patientCity");
        ccr.createPath("patientAddress1").setText("patientAddress1");
        
        // They should end up IN order.
        ccr.syncFromJDom();
        CCRDocument validated = new CCRDocument(ServiceConstants.PUBLIC_MEDCOMMONS_ID,"foo", "01234567890", ccr.getXml(), CCRConstants.SCHEMA_VALIDATION_STRICT);
    }
    
    /**
     * That a CCR containing window level presets can be parsed
     */
    public void testParseWindowLevel() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        builder.setFactory(new CCRElementFactory());
        Document d = builder.build(new FileInputStream("tests/jane_window_level.xml"));

        CCRElement root = (CCRElement) d.getRootElement();
        CCRElement ref = (CCRElement) root.getChild("References").getChildren().get(1);
        MCSeries s = new MCSeries();
        ccr.parseWindowLevel(ref, s); 
        
        String refXML = Str.toString(ref);
        
        
        // Check that it has window level presets
        assertEquals(2,s.getPresets().size());
        assertEquals(s.getPresets().get(0).getName(), "BONE");
        assertEquals(s.getPresets().get(1).getName(), "ABDOMEN");
        assertEquals(s.getPresets().get(0).getWindow(), 1500);
        assertEquals(s.getPresets().get(0).getLevel(), 300);
    }
    
    public void testSetFrom() throws Exception {
        CCRDocument ccr = getCcr("tests/ccr_minimal_multiple_froms.xml");
        
        // Set existing froms
        CCRElement f = ccr.getRoot().getChild("From");
        ccr.setActorLinks(f,"bminimal@medcommons.net, bminimaless@medcommons.net");
        
        // Should be only 2 actors - don't create new ones if email addresses match!
        assertEquals(2, ccr.getRoot().getChild("Actors").getChildren("Actor").size());
        
        List<CCRElement> froms = ccr.getRoot().getChild("From").getChildren("ActorLink");
        CCRElement actor = ccr.getJDOMDocument().queryProperty("actorFromEmail", new String[]{"email","bminimal@medcommons.net"});
        assertEquals(froms.get(0).getChildText("ActorID"),actor.getChildText("ActorObjectID"));
        
        ccr.setActorLinks(f,"ssadedin@badboy.com.au,ssadedin@gmail.com");
        
        // Should have 2 froms, both new actors
        froms = ccr.getRoot().getChild("From").getChildren("ActorLink");
        assertEquals(2,froms.size());
        
        actor = ccr.getJDOMDocument().queryProperty("actorFromEmail", new String[]{"email","ssadedin@badboy.com.au"});
        assertEquals(froms.get(0).getChildText("ActorID"),actor.getChildText("ActorObjectID"));
        
        // Should be 4 actors in the CCR now
        assertEquals(4, ccr.getRoot().getChild("Actors").getChildren("Actor").size());
    }
    
    public void testGetReference() throws Exception {
        /*
        CCRDocument ccr = getCcr("tests/ccr-multiple-unconfirmed-references.xml");
        MCSeries series = ccr.getSeriesList().get(1);
        CCRElement ref = ccr.findSeriesReferenceElement(series);
        assertEquals(ref.queryTextProperty("referenceDisplayName"), "Test Document 4");
        */
    }
    
    /**
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws ParseException
     * @throws RepositoryException 
     * @throws PHRException 
     */
    private CCRDocument getCcr(String fileName) throws JDOMException, IOException, ParseException, RepositoryException, PHRException {
        String xml = read(fileName);
        return new CCRDocument(ServiceConstants.PUBLIC_MEDCOMMONS_ID,  
                                this.guidGenerator.generateGuid((new Date()).toString().getBytes()), 
                                "000000000000",  
                                xml, 
                                CCRConstants.SCHEMA_VALIDATION_STRICT);
    }
}
