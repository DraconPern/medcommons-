package net.medcommons.router.services.qa;

import java.io.File;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

public class CCRSummaryUtils {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CCRSummaryUtils.class);
    
    private static final String XPATH_BODY_FRAGMENT = "/x:ContinuityOfCareRecord/x:Body/x:";
    private static final String XPATH_ACTORS_FRAGMENT = "/x:ContinuityOfCareRecord/x:Actors";
    private static final String XPATH_REFERENCES_FRAGMENT = "/x:ContinuityOfCareRecord/x:References";
    public static XPathCache xpath = null;
     
    private static File scratchDir;
    
    private static boolean doAAFPValidation = false;
    
    private  static void init(){
        if (xpath == null){
            xpath = (XPathCache) Configuration.getBean("ccrXPathCache");
        }
    }
    
    protected static String getCCRDocumentObjectID(CCRDocument document) throws JDOMException, PHRException{
        if (document == null) throw new NullPointerException("CCR document is null");
        CCRElement root = document.getRoot();
        return(root.getChildText("CCRDocumentObjectID"));
        
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
            if (log.isDebugEnabled()) log.debug("element = " + element.toString());
        if (element != null)
            count = element.getChildren().size();
        return(count);
    }
    
}
