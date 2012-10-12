package net.medcommons.router.services.ccrmerge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.ccr.CCRMangler;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.ccrmerge.preprocess.MarkIncomingCCR;
import net.medcommons.router.services.ccrmerge.preprocess.MarkIncomingHealthFrameCCR;
import net.medcommons.router.services.xds.consumer.web.action.CCRByteArrayReader;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;

public class CCRUtils { 
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CCRUtils.class);
	
	private static final String XPATH_BODY_FRAGMENT = "/x:ContinuityOfCareRecord/x:Body/";
	private static final String XPATH_ACTORS_FRAGMENT = "/x:ContinuityOfCareRecord/x:Actors";
	private static final String XPATH_REFERENCES_FRAGMENT = "/x:ContinuityOfCareRecord/x:References";
	public static XPathCache xpath = null;
	 
	private static File scratchDir;
	
	private static boolean doAAFPValidation = false;
	
	private static void init(){
		if (xpath == null){
			File buildDir = new File("build");
			buildDir.mkdir();
	
			scratchDir = new File(buildDir, "Scratch");
			scratchDir.mkdir();
			
			xpath = (XPathCache) Configuration.getBean("ccrXPathCache");
		}
	}
	
	public static CCRDocument loadCCR(String file) throws Exception{
	    return loadCCR(file,true);
	}
	
	public static CCRDocument loadCCR(String file, boolean parseReferences) throws Exception{
		CCRDocument ccr = null;
		try{
			ccr = CCRDocument.createFromTemplate(
					ServiceConstants.PUBLIC_MEDCOMMONS_ID,
					file,
					CCRConstants.SCHEMA_VALIDATION_STRICT, parseReferences);
		}
		catch(JDOMParseException e){
			
			throw new IOException(file,e);
		}
		return(ccr);
	}
	

	public static File mergeCCR(Class mergerFactory, String title, CCRDocument currentCCR, CCRDocument incomingCCR) throws Exception {
		init();
		
		CCRDocument parsedMergedCCR = null;
	
		log.info("About to merge with " + mergerFactory.getCanonicalName());
		
		ElementCount[] incomingCCRCount = generateElementCounts(incomingCCR);
		
		
		ElementCount[] currentCCRCount = generateElementCounts(currentCCR);
		
		MarkIncomingCCR markIncoming = new MarkIncomingHealthFrameCCR();
		CCRDocument markedDoc = markIncoming.markIncomingCCR(incomingCCR);
		
		CCRMangler.mangleCCR(markedDoc.getRoot());
		//log.info(Str.toString(markedDoc.getRoot()));
		CCRMangler.mangleCCR(currentCCR.getRoot());
		 
		markedDoc.syncFromJDom();
		currentCCR.syncFromJDom();
		
		Class partypes[] = new Class[2];
        partypes[0] = CCRDocument.class;
        partypes[1] = CCRDocument.class;
        
		Method m = mergerFactory.getDeclaredMethod("merge", partypes);
		
		Object arglist[] = new Object[2];
		arglist[0] = markedDoc;
		arglist[1] = currentCCR;
		Change c = (Change) m.invoke(null, arglist);
		//Change c = MergerFactory.merge(markedDoc, currentCCR);
		
		currentCCR.setCreateTimeMs(System.currentTimeMillis());
		

		CCRDocument mergedCCR = currentCCR;
		markIncoming.clearMarkedAttributes(mergedCCR);
		CCRMangler.unMangleCCR(mergedCCR.getRoot());
		mergedCCR.syncFromJDom();
		String mergedCCRString = mergedCCR.getXml();
		long now = System.currentTimeMillis();
		File saveMergedCCR = new File(scratchDir,  currentCCR.getPatientFamilyName() + "_" + title + "_" + now + "_merged.xml");
		FileOutputStream out = new FileOutputStream(saveMergedCCR);
		byte[] bDocument = mergedCCRString.getBytes("UTF-8");
		 
		InputStream is = new CCRByteArrayReader(bDocument);
		writeScratchFile(is, out);
		//try{
		if (log.isDebugEnabled())
		    log.debug("About to validate merged file " + saveMergedCCR.getAbsolutePath());
		parsedMergedCCR = CCRDocument.createFromTemplate(
				ServiceConstants.PUBLIC_MEDCOMMONS_ID,
				saveMergedCCR.getAbsolutePath(),
				CCRConstants.SCHEMA_VALIDATION_STRICT);
		//}
		//catch(JDOMParseException e){
		//	log.error("Caught JDOMParseException " + e.getLocalizedMessage());
		//	throw new IOException(saveMergedCCR.getAbsolutePath(), e);
		//}
		ElementCount[] mergedCCRCount = generateElementCounts(mergedCCR);
		compareElementCounts(title + "\n" + saveMergedCCR.getAbsolutePath() + "\n" + "Current/Incoming/Merged", currentCCRCount, incomingCCRCount, mergedCCRCount);
		
		if (doAAFPValidation){
			AAFPValidator validator = new AAFPValidator();
			validator.validateCCR(saveMergedCCR);
		}
		return(saveMergedCCR);
		
		
	}
	
	protected static ElementCount[] generateElementCounts(CCRDocument document) throws JDOMException, PHRException{
		if (document == null) throw new NullPointerException("CCR document is null");
		String elements [] = CCRElement.CCR_ELEMENT_ORDER.get("Body");
		ElementCount[] counts = new ElementCount[elements.length+2];
		for (int i=0; i<elements.length; i++){
			String xpath = XPATH_BODY_FRAGMENT + elements[i];
			
			int count = countElements(document, xpath);
			counts[i] = new ElementCount(elements[i], count);
			if (log.isDebugEnabled())
			    log.debug("Element count " + xpath + " is " + count);
		}
		int c = countElements(document, XPATH_ACTORS_FRAGMENT);
		counts[elements.length] = new ElementCount("Actors", c);
		
		c = countElements(document, XPATH_REFERENCES_FRAGMENT);
		counts[elements.length+1] = new ElementCount("References", c);
		
		return(counts);
	}
	protected static void compareElementCounts(String title, ElementCount[] c1, ElementCount[] c2, ElementCount[] c3) throws JDOMException, PHRException{
		log.info(title);
		for (int i=0; i<c1.length; i++){
			if( (c1[i].count == 0) && (c2[i].count == 0) && (c3[i].count == 0))
				; //skip
			else
				 log.info("Element count:  "+ c1[i].name + ":" + c1[i].count + ", " + c2[i].count + ", " + c3[i].count);
			
		}
	}
	
	protected static void printElementCounts(String title, ElementCount[] c1) throws JDOMException, PHRException{
		log.info(title);
		for (int i=0; i<c1.length; i++){
			if(c1[i].count == 0)
				; //skip
			else
				log.info("Element count:  "+ c1[i].name + ":" + c1[i].count);
			
		}
	}
	protected static int countElements(CCRDocument doc, String elementName) throws JDOMException,PHRException{
		init();
		int count = 0;
		if (elementName == null) throw new NullPointerException("element name is null");
		if (doc == null) throw new NullPointerException("Null CCR document");
		CCRElement root = doc.getRoot();
		if (root == null){
			throw new NullPointerException("Null root document in CCR");
		}
		CCRElement element = (CCRElement) 
			(xpath.getElement(root, elementName));
		if (element == null)
		    log.debug("Element is null");
		else
		    log.debug("element = " + element.toString());
		if (element != null)
			count = element.getChildren().size();
		return(count);
	}
	
	private static int buffSize = 8 * 1024;
	
	protected static void writeScratchFile(InputStream is, OutputStream out)
			throws IOException, NoSuchAlgorithmException {
		

		if (is == null)
			throw new NullPointerException(
					"Null InputStream - can't create file ");
		try {

			
			byte[] buff = new byte[buffSize];
			int n = 0;
			long fileSize = 0;

			while ((n = is.read(buff, 0, buff.length)) != -1) {

				out.write(buff, 0, n);
				fileSize += n;
			}
			out.close();
			

		} catch (IOException e) {
			log.error("Error writing scratch file ", e);
			throw (e);
		} catch (RuntimeException e) {
			log.error("Error writing scratch file ", e);
			throw (e);
		} finally {

			if (is != null) {
				try {
					is.close();
				}

				catch (Exception e) {
					log.error("Error closing inputStream to scratch file ", e);
				}
			}

		}
		
	}
	
	
	
	
}
