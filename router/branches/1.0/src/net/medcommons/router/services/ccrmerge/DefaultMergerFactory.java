/*
 * $Id$
 * Created on 24/08/2006
 */
package net.medcommons.router.services.ccrmerge;

import java.util.HashMap;

import org.apache.log4j.Logger;

import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

public class DefaultMergerFactory extends MergerFactory {
    
   // private static HashMap<String,String> mergers = new HashMap<String, String>();
    /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(DefaultMergerFactory.class);
    static {
        // mergers.put("ContinuityOfCareRecord",)
    }

    private static HashMap<String, Class> classMap = new HashMap<String, Class>();
    public CCRMerger create(CCRElement element) {
        try {
            // Class cache helps speed out a bit here - Class.forName() is really slow.
        	String className = "net.medcommons.router.services.ccrmerge."+element.getName()+"Merger";
        	Class clazz = classMap.get(className);
        	if (clazz == null){
        		clazz = Class.forName(className, true, this.getClass().getClassLoader());
        		classMap.put(className, clazz);
        		if (log.isDebugEnabled())
        			log.debug("Loaded class " + clazz.getCanonicalName());
        	}
        	else{
        		if (log.isDebugEnabled())
        			log.debug("Loaded class from cache:" +  clazz.getCanonicalName());
        	}
          
            return (CCRMerger) clazz.newInstance();
        }
        catch (ClassNotFoundException e) {
        	
            return new AddMissingChildrenMerger();
        }
        catch (InstantiationException e) {
            return new AddMissingChildrenMerger();
        }
        catch (IllegalAccessException e) {
            return new AddMissingChildrenMerger();
        }
    }

}
