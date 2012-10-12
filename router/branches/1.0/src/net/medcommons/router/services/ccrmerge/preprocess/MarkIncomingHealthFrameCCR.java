package net.medcommons.router.services.ccrmerge.preprocess;

import java.util.Collections;
import java.util.List;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

public class MarkIncomingHealthFrameCCR implements MarkIncomingCCR, MergeConstants{
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(MarkIncomingHealthFrameCCR.class);

	
	public CCRDocument markIncomingCCR(CCRDocument doc) throws JDOMException,PHRException{
		XPathCache xpath = (XPathCache) Configuration.getBean("ccrXPathCache");
		
        List<CCRElement> results = (List<CCRElement>)xpath.getXPathResult(doc.getRoot(), HEALTHFRAME_SOURCES, Collections.EMPTY_MAP, true);
        log.debug("Number of matches to " + HEALTHFRAME_SOURCES + " is  " + results.size());
        for (CCRElement element : results) {
        	
        	CCRElement sourceElement = element.getParentElement();
        	
        	sourceElement.setAttribute("marked", "true");
        }
        results = (List<CCRElement>)xpath.getXPathResult(doc.getRoot(), HEALTHFRAME_FUZZYDATES, Collections.EMPTY_MAP, true);
        log.debug("Number of matches to " + HEALTHFRAME_FUZZYDATES + " is  " + results.size());
        for (CCRElement element : results) {
        	
        	CCRElement sourceElement = element.getParentElement();
        	
        	
        	sourceElement.setAttribute("marked", "true");
        	
        }
        results = (List<CCRElement>)xpath.getXPathResult(doc.getRoot(), CCROBJECTIDS, Collections.EMPTY_MAP, true);
        log.debug("Number of matches to " + CCROBJECTIDS + " is  " + results.size());
        for (CCRElement element : results) {
        	
        	CCRElement sourceElement = element.getParentElement();
        	
        	
        	sourceElement.setAttribute("marked", "true");
        	
        }
        
        return(doc);  
		
	}
	
	public CCRDocument clearMarkedAttributes(CCRDocument doc) throws JDOMException,PHRException{
		XPathCache xpath = (XPathCache) Configuration.getBean("ccrXPathCache");
		
        List<CCRElement> results = (List<CCRElement>)xpath.getXPathResult(doc.getRoot(), MARKED_ATTRIBUTE, Collections.EMPTY_MAP, true);
        log.debug("Number of matches to " + MARKED_ATTRIBUTE + " is  " + results.size());
        for (CCRElement element : results) {
        	
        	element.removeAttribute("marked");
        	
        }
        return(doc);  
		
	}
}
