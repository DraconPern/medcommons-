package net.medcommons.phr.ccr;

import java.util.Collections;
import java.util.List;

import net.medcommons.phr.PHRException;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.phr.resource.Spring;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
/**
 * "Mangles" the CCR so that merging can take place unambiguously.
 * 
 * The orderings defined in CCRElement assume that element names like 
 * "Product" are tied to unique element orderings in the schema. 
 * Unfortunately - this is not true. A "Product" could be in a 
 * medication (or any other structured project type) or in a 
 * "Products" element. 
 * 
 * What mangling does is change the names of certain elements so 
 * that the uniqueness assumption in CCRElement is true. Thus - 
 * "Product" elements which match MANGLE_PRODUCT_XPATH are transformed 
 * to be "ProductsProduct" elements. 
 * 
 * Unmangling reverses the process. 
 * 
 * @author mesozoic
 *
 */
public class CCRMangler {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CCRMangler.class);
	
	public static final String MANGLE_PRODUCT_XPATH = "//x:Products/x:Product";
	public static final String MANGLE_FORM_XPATH    = "//x:Fulfillment/x:Form";
	public static final String MANGLE_ACTOR_XPATH    = "//x:Source/x:Actor";
	public static final String MANGLED_PRODUCTNAME  = "ProductsProduct";
	public static final String MANGLED_FORMNAME      = "FulfillmentForm";
	public static final String MANGLED_ACTORNAME      = "SourceActor";
	
	
	public static final String UNMANGLE_PRODUCT_XPATH = "//x:Products/x:" + MANGLED_PRODUCTNAME;
	public static final String UNMANGLE_FORM_XPATH = "//x:Fulfillment/x:" + MANGLED_FORMNAME;
	public static final String UNMANGLE_ACTOR_XPATH = "//x:Source/x:" + MANGLED_ACTORNAME;
	
	/**
	 * Returns a mangled version of the CCR. 
	 * 
	 * The CCR is modified in place. 
	 * 
	 * @param rootDoc
	 * @return
	 * @throws JDOMException
	 * @throws PHRException
	 */
	public static CCRElement mangleCCR(CCRElement rootDoc) throws JDOMException, PHRException{
		XPathCache xpath = (XPathCache) Spring.getBean("ccrXPathCache");
		
        List<CCRElement> results = (List<CCRElement>)xpath.getXPathResult(rootDoc, MANGLE_PRODUCT_XPATH, Collections.EMPTY_MAP, true);
        if (log.isDebugEnabled())
        	log.debug("Number of elements to mangle via " + MANGLE_PRODUCT_XPATH + " is  " + results.size());
        for (CCRElement element : results) {
        	if (log.isDebugEnabled())
        		log.debug("Mangle element " + element.getName());
        	element.setName(MANGLED_PRODUCTNAME);
        	
        }
        
        results = (List<CCRElement>)xpath.getXPathResult(rootDoc, MANGLE_FORM_XPATH, Collections.EMPTY_MAP, true);
        if (log.isDebugEnabled())
        	log.info("Number of elements to mangle via " + MANGLE_FORM_XPATH + " is  " + results.size());
        for (CCRElement element : results) {
        	if (log.isDebugEnabled())
        		log.debug("Mangle element " + element.getName());
        	element.setName(MANGLED_FORMNAME);
        	
        }
        
        results = (List<CCRElement>)xpath.getXPathResult(rootDoc, MANGLE_ACTOR_XPATH, Collections.EMPTY_MAP, true);
        if (log.isDebugEnabled())
        	log.info("Number of elements to mangle via " + MANGLE_ACTOR_XPATH + " is  " + results.size());
        for (CCRElement element : results) {
        	if (log.isDebugEnabled())
        		log.debug("Mangle element " + element.getName());
        	element.setName(MANGLED_ACTORNAME);
        	
        }
     
        return(rootDoc);  
		
	}
	
	/**
	 * Unmangles a mangled CCR. Unmangling a CCR which has not been mangled should be a no-op.
	 * 
	 * @param rootDoc
	 * @return
	 * @throws JDOMException
	 * @throws PHRException
	 */
	public static CCRElement unMangleCCR(CCRElement rootDoc)throws JDOMException, PHRException{
		XPathCache xpath = (XPathCache) Spring.getBean("ccrXPathCache");
		
        List<CCRElement> results = (List<CCRElement>)xpath.getXPathResult(rootDoc, UNMANGLE_PRODUCT_XPATH, Collections.EMPTY_MAP, true);
                        
        if (log.isDebugEnabled())
        	log.debug("Number of elements to unmangle via " + UNMANGLE_PRODUCT_XPATH + " is  " + results.size());
        for (CCRElement element : results) {
        	if (log.isDebugEnabled())
        		log.debug("Unmangle element " + element.getName());
        	element.setName("Product");
        	
        }
        results = (List<CCRElement>)xpath.getXPathResult(rootDoc, UNMANGLE_FORM_XPATH, Collections.EMPTY_MAP, true);
        if (log.isDebugEnabled())
        	log.info("Number of elements to unmangle via " + UNMANGLE_FORM_XPATH + " is  " + results.size());
        for (CCRElement element : results) {
        	if (log.isDebugEnabled())
        		log.debug("Unmangle element " + element.getName());
        	element.setName("Form");
        	
        }
        results = (List<CCRElement>)xpath.getXPathResult(rootDoc, UNMANGLE_ACTOR_XPATH, Collections.EMPTY_MAP, true);
        if (log.isDebugEnabled())
        	log.info("Number of elements to unmangle via " + UNMANGLE_ACTOR_XPATH + " is  " + results.size());
        for (CCRElement element : results) {
        	if (log.isDebugEnabled())
        		log.debug("Unmangle element " + element.getName());
        	element.setName("Actor");
        	
        }
        return(rootDoc);  
		
	}
}
