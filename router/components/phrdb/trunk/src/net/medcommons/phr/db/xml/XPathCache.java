/*
 * $Id$
 * Created on 22/03/2005
 */
package net.medcommons.phr.db.xml;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.medcommons.phr.PHRElement;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * A utility that slightly eases executing XPath statements against
 * JDOM documents as well as caching them.
 *
 * Usage:   XPathCache.getElement(jdomDoc, "<your xpath expression>");
 * 
 * NOTE: the default namespace of the jdomDoc will be added to the expression as "x",
 * so you can put references into the expression as "x:<element> without
 * doing anything else.
 * 
 * Example:  for a CCR document
 * 
 * Element patient = XPathCache.getElement(jdomDoc, "/x:ContinuityOfCareRecord/x:Patient");
 * 
 * @author ssadedin
 */
public class XPathCache {
    
    
    /**
     * Default path from which XML XPaths will be read if no value is provided in the 
     * config file for XPathMappingsConfig.
     */
    private static String DATA_XPATHS_XML = "data/xpaths.xml";
    private static String DATA_XPATHS_XML_JUNIT = "etc/configurations/xpaths.xml";
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(XPathCache.class);
    
    /**
     * The file that will be read for mappings
     */
    private String mappingFile = DATA_XPATHS_XML;
    
    /**
     * Internal class used to hold cache entries
     */
    private static class XPathCacheEntry {
        public String expression;
        public XPath path;
        public boolean multiValued = false;
        public long loadTimeMs;
    }

    /**
     * Cache of XPath instances - one per thread so we don't have to synchronize
     */
    private static ThreadLocal cachedPaths = new ThreadLocal();
    
    /**
     * If the cache gets older than this then we reload
     */
    private long maxCacheAge = 60000;
    
    private static Map<String, String> EMPTY_PARAMS = new HashMap<String, String>(); 
    
    /*
    static {    	
        maxCacheAge = Configuration.getProperty("MaxXPathCacheAgeMs", (int)maxCacheAge);
        DATA_XPATHS_XML = Configuration.getProperty("XPathMappingsConfig", DATA_XPATHS_XML);    
        File f = new File(DATA_XPATHS_XML);
        if (!f.exists()){
        	DATA_XPATHS_XML = DATA_XPATHS_XML_JUNIT;
        }
    }*/
    
    /**
     * Do not construct me.
     */
    public XPathCache() {
        super();
    }

    /**
     * Executes the given named XPath expression against the given context object.
     * The expression can be the name of an expression from the xpaths.xml file,
     * or if it is not found then it will be attempted as a literal expression.
     * 
     * @return - the result object, any JDOM type depending on the expression.
     */    
    public Element getElement(Object contextObj, String expression) throws JDOMException {        
        return getElement(contextObj, expression, Collections.EMPTY_MAP);
    }
    
    public Element getElement(Object contextObj, String expression, Map variables) throws JDOMException {        
       Object result = getXPathResult(contextObj,expression,variables,false);
       if(result == null)
           return null;
       
       // Defensive handling: the user may send us data we don't expect so 
       // look for incorrect types here
       if(result instanceof List) {
           log.warn("Element expression returned List: " + expression);
           List resultList = (List)result;
           if(resultList.size() > 0)
               result =resultList.get(0);
           else
               return null;
       }
       if(! (result instanceof Element)) {
           log.warn("Element expression '" + expression + " returned unexpected result type " + result.getClass().getName());
       }
           
       return (Element)result; 
    }
    
    public Attribute getAttribute(Object contextObj, String expression) throws JDOMException {        
       return (Attribute)getXPathResult(contextObj,expression,Collections.EMPTY_MAP,false); 
    }
    
     public Attribute getAttribute(Object contextObj, String expression, Map variables) throws JDOMException {        
       return (Attribute)getXPathResult(contextObj,expression,variables,false); 
    }
     
     /**
      * Returns true if the given expression is defined as multivalued
      * <p>
      * Note that an expression may <b>still</b> return multiple values if this method returns false.
      * Being flagged as multivalued signals that the expressions results are intended to be multivalued
      * and should not be coerced to single values automatically.
      * 
      * @throws JDOMException 
      */
     public boolean isMultiValued(String expression) throws JDOMException {
         HashMap cache = (HashMap) cachedPaths.get();
         if(cache == null) {
             loadCache();
             cache = (HashMap) cachedPaths.get();
             assert cache != null : "Cache is null after load";
         }
         
         XPathCache.XPathCacheEntry entry = (XPathCacheEntry) cache.get(expression);
         if(entry != null)
             return entry.multiValued;
         
         return false;
     }
     
     /**
      * Executes the given named XPath expression against the given context object.
      * The expression can be the name of an expression from the xpaths.xml file,
      * or if it is not found then it will be attempted as a literal expression.
      * 
      * The context object must be of type Document or Element or another class
      * extending the JDOM Content class.
      * 
      * Note that invocation of this method may cause the xpaths data file to be
      * read and parsed in its entirety if it is stale or has not yet been loaded.
      * 
      * You can change the refresh interval by setting the configuration parameter
      * "xpathCache.lastLoadTime"
      * 
      * @param contextObj - context (should be JDOM Document or Element)
      * @param expression - expression to evaluate.
      * @return - results as a list
      */
    public List<PHRElement> getXPathResults(Object contextObj, String expression) throws JDOMException {        
        return (List<PHRElement>)getXPathResult(contextObj, expression, Collections.emptyMap(), true);
    }
     
    
     /**
     * Executes the given named XPath expression against the given context object.
     * The expression can be the name of an expression from the xpaths.xml file,
     * or if it is not found then it will be attempted as a literal expression.
     * 
     * The context object must be of type Document or Element or another class
     * extending the JDOM Content class.
     * 
     * Note that invocation of this method may cause the xpaths data file to be
     * read and parsed in its entirety if it is stale or has not yet been loaded.
     * 
     * You can change the refresh interval by setting the configuration parameter
     * "xpathCache.lastLoadTime"
     * 
     * @param contextObj - context (should be JDOM Document or Element)
     * @param expression - expression to evaluate.
     * @param variables - variables to add to the expression.
     * @return - the result object, may be any JDOM node type depending on expression.
     */
    public Object getXPathResult(Object contextObj, String expression, Map variables, boolean alwaysList) throws JDOMException {        
        
        if(variables == null)
            variables = EMPTY_PARAMS;
        
        if (contextObj == null)
        	throw new NullPointerException("Null contextObj");
        Document doc = null;
        if(contextObj instanceof Document) {
            doc = (Document)contextObj;
        }
        else 
        if(contextObj instanceof Content) {
            doc = ((Content)contextObj).getDocument();
        }
        else {
            throw new JDOMException("Context object must be a Document or Element.  Object type " 
                            + contextObj.getClass().getName() + " was passed.");
        }
        
        String namespaceURI = doc.getRootElement().getNamespaceURI();
        
        HashMap cache = (HashMap) cachedPaths.get();
        if(cache == null) {
            loadCache();
            cache = (HashMap) cachedPaths.get();
            assert cache != null : "Cache is null after load";
        }
       
        XPathCacheEntry entry = (XPathCacheEntry) cache.get(expression);
        Long lastLoadTime = (Long) cache.get("xpathCache.lastLoadTime");  
        if(System.currentTimeMillis() - lastLoadTime.longValue() > maxCacheAge) { // Older than 2 seconds, check file mod
           cachedPaths.set(null);
           loadCache();
           entry = (XPathCacheEntry) cache.get(expression);
        }
	            
        if(entry == null) { // Unregistered XPath statement => name = expr
            entry = new XPathCacheEntry();
            entry.expression = expression;
            cache.put(expression, entry);            
        }
        
       XPath xpath = entry.path;
        if(entry.path== null) {
            entry.path = XPath.newInstance(entry.expression);
            if(namespaceURI!=null && namespaceURI.trim().length()>0) {
                entry.path.addNamespace("x", namespaceURI);                    
            }
        }
        
       for (Iterator iter = variables.entrySet().iterator(); iter.hasNext();) {
            Map.Entry variable = (Map.Entry) iter.next();
            entry.path.setVariable((String) variable.getKey(), variable.getValue());
        }
       
       if (log.isDebugEnabled())
    	   log.debug("Executing xpath " + entry.expression);

       List resultList = entry.path.selectNodes(contextObj);       
       Object resultObj = null;            
       
       if(!resultList.isEmpty()) {
           if((resultList.size() == 1) && (!alwaysList)) {
               resultObj = resultList.get(0);
           }
           else {
               resultObj = resultList;
           }
       }    
       else
       if(alwaysList) {
           resultObj = Collections.EMPTY_LIST;
       }
        
        // Unset the variables so they do not get used when this path
        // is taken again from the cache.
       for (Iterator iter = variables.entrySet().iterator(); iter.hasNext();) {
            Map.Entry variable = (Map.Entry) iter.next();
            entry.path.setVariable((String) variable.getKey(), "");
        }
       
       return resultObj;
    }
    
    public String getValue(Object obj, String pathName) throws JDOMException, IOException {     
        Object pathResult = this.getXPathResult(obj, pathName, Collections.EMPTY_MAP, false);
        if(pathResult == null)
            return null;

        if(pathResult instanceof List)
            throw new JDOMException("Can't get value on field referenced by path " + pathName + ".  Path returned multiple results.");
        
        if(pathResult instanceof Attribute) {
           return ((Attribute)pathResult).getValue();
        }
        else 
        if(pathResult instanceof Element) {
            return ((Element)pathResult).getTextTrim();           
        }        
        else
          return pathResult.toString();  
    }    
    
    public void loadCache() throws JDOMException {
        HashMap cache = new HashMap();        
        try {
            Document pathDoc =  new SAXBuilder().build(new File(this.mappingFile));
            Iterator iter = pathDoc.getDescendants(new ElementFilter("path"));
            while(iter.hasNext()) { 
                Element path = (Element) iter.next();
                XPathCacheEntry entry = new XPathCacheEntry();
                String name = path.getAttributeValue("name");                
                
                String pathText = path.getTextTrim();
                String expr = pathText;
                if(path.getAttributeValue("noAddNs")==null) {
                    expr = addNamespaces(expr);
                    
                }                                
                if("true".equals(path.getAttributeValue("multivalued"))) {
                    entry.multiValued = true;
                }
                entry.expression = expr;
                
                if (log.isDebugEnabled())
                	log.debug("Translated xpath entry [" + name + "] as " + entry.expression );
                cache.put(name, entry);
            }            
        }
        catch (IOException e) {
            throw new JDOMException("IOException while loading xpaths file", e);
        }        
        cache.put("xpathCache.lastLoadTime", new Long(System.currentTimeMillis()));
        cachedPaths.set(cache);
    }

    /**
     * Adds namespaces to the given XPath expression so that a plain XPath expression without
     * namespaces can be executed against an XML document containing namespaces.
     * 
     * @param expr
     * @return
     */
    public static String addNamespaces(String expr) {
        expr = expr.replaceAll("([\\[/])([A-Za-z])","$1x:$2");
        if((!expr.startsWith("/")) && (!expr.startsWith(".")) ) {
            if(!expr.matches("^[\\w]*\\(.*")) { // function - don't append namespace
                expr = "x:" + expr;
            }
        }
        return expr;
    }

    /**
     * ONLY for use by unit tests to control directly where the xpaths are loaded from.
     * @param path
     */
    public void setXPathMappingFile(String path) {
        this.mappingFile = path;
    }

    public long getMaxCacheAge() {
        return maxCacheAge;
    }

    public void setMaxCacheAge(long maxCacheAge) {
        this.maxCacheAge = maxCacheAge;
    }
}
