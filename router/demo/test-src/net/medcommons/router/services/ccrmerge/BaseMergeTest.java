package net.medcommons.router.services.ccrmerge;

import static net.medcommons.phr.ccr.CCRElementFactory.el;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.ccrmerge.preprocess.MergeConstants;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.util.BaseTestCase;

public class BaseMergeTest extends BaseTestCase {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(BaseMergeTest.class);

    protected CCRDocument to;
    protected CCRDocument from;

    public BaseMergeTest() throws Exception {
        super();
    }

    public BaseMergeTest(String name) throws ConfigurationException, Exception {
        super(name);
    }

    private void printElement(Element element) {
         Format utfOutputFormat = Format.getPrettyFormat();
            utfOutputFormat.setEncoding("UTF-8");
            try {
                StringWriter sw = new StringWriter();
                new XMLOutputter(utfOutputFormat).output(element, sw);
                log.info(sw.toString());
                
            }
            catch (IOException e) {
              log.error("error", e);
            }
        
    }

    /**
     * Create a xxtest medication
     * 
     * @param from - document to which the medication will belong
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws PHRException 
     */
    protected CCRElement createMedication(CCRDocument from) throws JDOMException, IOException, PHRException {
        CCRElement med = el("Medication");
        med.getOrCreate("CCRDataObjectID").setText(med.generateObjectID());
        med.createPath("DateTime/ExactDateTime", med.getCurrentTime());
        med.createPath("Source/Actor/ActorID", from.getValue("patientActorID"));
        med.createPath("Product/ProductName/Text", "Vitamin C");
        med.createPath("Product/Form/Text", "Capsule");
        return med;
    }

    protected File mergeCCR(String title, String currentCCR, String incomingCCR) throws Exception {
            File merged;
            try{
                CCRDocument currentCCRDocument = CCRUtils.loadCCR(currentCCR);
                CCRDocument incomingCCRDocument = CCRUtils.loadCCR(incomingCCR);
                merged = CCRUtils.mergeCCR(MergerFactory.class, title, currentCCRDocument, incomingCCRDocument);
            }
            catch(Exception e){
                log.error("mergeCCR", e);
                throw e;
            }
            return(merged);
        }

}