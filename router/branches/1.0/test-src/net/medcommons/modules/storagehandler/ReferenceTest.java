package net.medcommons.modules.storagehandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.medcommons.document.ValidatingParserFactory;
import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.storagehandler.DocumentRetrievalServiceFactory;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.ccr.CCRElementFactory;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.router.util.BaseTestCase;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

public class ReferenceTest extends BaseTestCase {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ReferenceTest.class);

    File ccrFile = new File("etc/tests/modules/CCR-with-StorageHandler.xml");

    public ReferenceTest() throws Exception{
        super();
    }
    public void setUp() throws Exception {
        super.setUp();
        log.info("in setup..");
       
    }

   
    /**
     * Just tests that the parameters can be parsed from the CCR.
     * @throws Exception
     */
    public void testHandler() throws Exception{
        ReferenceParser parser = new ReferenceParser();;
        
        XMLPHRDocument doc = getCCR(ccrFile);
        Namespace namespace = doc.getRootElement().getNamespace();
        CCRElement refs = doc.queryProperty("references");   
        for (Iterator iter = refs.getDescendants(new ElementFilter("Reference",namespace)); iter.hasNext();) {
            CCRElement ref = (CCRElement) iter.next();
            log.info(ref.toXml());
            Map<String, String> refInfo = parser.getStorageServiceParameters(ref);
            assertNotNull("DocBase",refInfo.get("DocBase"));
            assertNotNull("DocumentIdentifier",refInfo.get("DocumentIdentifier"));
            assertNotNull("StorageHandler",refInfo.get("StorageHandler"));
            assertNotNull("UserName",refInfo.get("UserName"));
            assertNotNull("PassWord",refInfo.get("PassWord"));
            assertNotNull("QueryDocumentIdentifier",refInfo.get("QueryDocumentIdentifier"));
            
            Set<String> keys= refInfo.keySet();
            for (String key:keys){
                log.info("key:" + key + "=>" + refInfo.get(key));
            }
            
        }
    }
   
    public XMLPHRDocument getCCR(File f) throws JDOMException, IOException {
        if (!f.exists()){
            throw new FileNotFoundException(f.getAbsolutePath());
        }
        SAXBuilder builder = null;
        XMLPHRDocument jdomDocument = null;
        try {
            ValidatingParserFactory factory = new ValidatingParserFactory(
                    Configuration.getProperty("CCRXSDLocation",CCRConstants.XSD_LOCATION),CCRConstants.CCR_NAMESPACE_URN);
            builder = (SAXBuilder) factory.makeObject();
           // builder = new CCRBuilder("org.apache.xerces.parsers.SAXParser",
            //        true);
        } catch (Exception e) {

            throw new JDOMException("Validating Parser Pool Exception", e);
        }
        try {

            builder.setFactory(new CCRElementFactory());

            jdomDocument = (XMLPHRDocument) builder.build(f);

          

        } catch (Exception e) {
            throw new JDOMException("Error parsing CCR:", e);
        }

       
        return (jdomDocument);
    }
}
