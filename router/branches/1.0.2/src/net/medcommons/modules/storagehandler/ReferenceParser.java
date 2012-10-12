package net.medcommons.modules.storagehandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;

import org.apache.log4j.Logger;

/**
 * Utility function for parsing references from a CCR and returning more generic objects
 * suitable for passing to third party libraries.
 * 
 * @author sean
 *
 */
public class ReferenceParser {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ReferenceParser.class);
    /**
     * Returns a Map of the attribute/value pairs in a CCR Reference's StorageHandler 
     * ObjectAttribute.
     * 
     * @param reference
     * @return
     * @throws PHRException
     */
    public Map<String, String> getStorageServiceParameters(CCRElement reference) throws PHRException{
        CCRElement storageHandler = reference.queryProperty("referenceStorageHandler");
        log.info("reference namespace is " + reference.getNamespace());
        if (storageHandler == null){
            return(null);
        }
        Map<String, String> parameters = new HashMap<String, String>();
        for (Iterator iter = storageHandler.getChildren().iterator(); iter.hasNext();) {
           
            CCRElement objectAttribute = (CCRElement) iter.next();
            String attribute = objectAttribute.getChildText("Attribute");
            CCRElement objectValue = objectAttribute.getChild("AttributeValue");
            String value = objectValue.getChildText("Value");
            parameters.put(attribute, value);
               
        }
        return(parameters);
    }
}
