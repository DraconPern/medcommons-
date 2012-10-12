package net.medcommons.router.services.ccrmerge;

import java.io.File;
import java.io.IOException;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.phr.ccr.CCRBuilder;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.phr.resource.Spring;
import net.medcommons.router.services.ccrmerge.preprocess.MergeConstants;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.util.BaseTestCase;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;

/**
 * Basic idea of test suite:
 * <ol>
 *  <li>Load in known CCRs for merging (to, from)</li>
 *  <li>Import elements into them. Add them to the original or to the incoming one. </li>
 *  <li>Test to see . </li>
 * </ol>
 * @author mesozoic
 *
 */
public class CCRMergeComponentsTest extends BaseTestCase  implements MergeConstants{
	File scratchDir;
	File sourceDir;
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CCRMergeComponentsTest.class);

	public CCRMergeComponentsTest(String arg0) throws Exception {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		 XPathCache xpath = (XPathCache) Spring.getBean("ccrXPathCache");
		File buildDir = new File("build");
		buildDir.mkdir();

		scratchDir = new File(buildDir, "Scratch");
		scratchDir.mkdir();

		sourceDir = new File("tests");
	}
	
	/**
	 * Add a medication to a CCR and then merge with the original. The resulting CCR should have a single
	 * @throws Exception
	 */
	public void testMergeMedicationAddition() throws Exception{
		
		String currentCCRFilename = "tests/StellaPattersonCurrentCCR.xml";
		CCRElement newMedication;
		newMedication = getCCRFragment("tests/CurrentCCRMedication.xml");
		
		CCRDocument incomingCCR = mergeIntoCCR(currentCCRFilename, "/x:ContinuityOfCareRecord/x:Body/x:Medications", newMedication);
		
		CCRDocument currentCCR = CCRDocument.createFromTemplate(
				ServiceConstants.PUBLIC_MEDCOMMONS_ID,
				currentCCRFilename,
				CCRConstants.SCHEMA_VALIDATION_STRICT);	
		incomingCCR.syncFromJDom();
		currentCCR.syncFromJDom();
		log.info("== Current CCR");
		ElementCount[] currentCCRCount = CCRUtils.generateElementCounts(currentCCR);
		
		File mergedCCRfile = CCRUtils.mergeCCR(MergerFactory.class, "AddMedication", currentCCR, incomingCCR);
		CCRDocument mergedCCR = CCRUtils.loadCCR(mergedCCRfile.getAbsolutePath());
		mergedCCR.syncFromJDom();
		log.info("== Merged CCR" + mergedCCRfile.getAbsolutePath());
		ElementCount[] mergedCCRCount = CCRUtils.generateElementCounts(mergedCCR);
		for (int i =0;i<currentCCRCount.length; i++){
			String sectionName = currentCCRCount[i].name;
			if ("Medications".equals(sectionName)){
				assertElementCount("Medication was added", mergedCCRCount, "Medications", currentCCRCount[i].count +1);
			}
			else{
				assertElementCount(sectionName + "was added", mergedCCRCount, currentCCRCount[i].name, currentCCRCount[i].count);
			}
		}		
	}
	
	private CCRElement getCCRFragment(String filename) throws JDOMException, IOException{
		 CCRBuilder builder = new CCRBuilder();

     	Document doc = builder.build(filename);
     	CCRElement root = (CCRElement) doc.getRootElement().clone();
     	return(root);
	}
	
	public CCRDocument mergeIntoCCR(String ccrFile, String parentXPath, CCRElement element) throws Exception {
		CCRDocument  incomingCCR = null;
		
		try{
			incomingCCR = CCRDocument.createFromTemplate(
					ServiceConstants.PUBLIC_MEDCOMMONS_ID,
					ccrFile,
					CCRConstants.SCHEMA_VALIDATION_STRICT);
		}
		catch(JDOMParseException e){
			throw new IOException(ccrFile,e);
		}
		CCRElement rootDoc = incomingCCR.getRoot();
	
		CCRElement parent = (CCRElement) (xpath.getElement(rootDoc,parentXPath));
		parent.addChild(element);
		incomingCCR.syncFromJDom();
		
		incomingCCR.getValidatedJDCOMDocument(); // Will throw an exception if invalid.
		
		return(incomingCCR);
		
	}
	protected  void assertElementCount(String title, ElementCount[] counts, String elementName, int predictedValue){
		for (int i=0; i< counts.length;i++){
			if (elementName.equals(counts[i].name)){
				int v = counts[i].count;
				assertEquals("Element count " + title , v, predictedValue);
				break;
			}
		}
	}
	
}
