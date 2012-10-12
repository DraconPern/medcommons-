package net.medcommons.router.services.ccrmerge;

import java.util.HashMap;

import org.apache.log4j.Logger;

import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

/**
 * Special MergerFactory for merging in References and patient ids (e.g., 
 * HealthFrame).
 * 
 * Copies methods from MergerFactory because of statics - must be
 * more elegant way to do this.
 * 
 * @author mesozoic
 *
 */
public  class ReferenceIDsMergerFactory extends MergerFactory{
	 private static ReferenceIDsMergerFactory instance;
	 
	 private static HashMap<String,String> mergers = new HashMap<String, String>();
    /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(ReferenceIDsMergerFactory.class);
    static {
        // mergers.put("ContinuityOfCareRecord",)
    }

    private static HashMap<String, Class> classMap = new HashMap<String, Class>();
    /**
     * Creates a merger for the root level document in to and calls it with
     * the root node of the to and from CCRs to merge them.
     */
    public static Change merge(CCRDocument from, CCRDocument to) throws MergeException {
    	log.info("RefIdMerger: merge");
        try {
            CCRElement fromRoot = from.getRoot();
            return ReferenceIDsMergerFactory.getInstance().create(fromRoot).merge(fromRoot, to, (CCRElement)to.getJDOMDocument().getRootElement());
        }
       
        catch (PHRException e) {
            throw new MergeException(e);
        }
    }
    
    public CCRMerger create(CCRElement element) {
        try {
            // Class cache helps speed out a bit here - Class.forName() is really slow.
        	String name = element.getName();
        	if ("ContinuityOfCareRecord".equals(name)){
        		name = "ReferenceIDsContinuityOfCareRecord";
        	}
        	String className = "net.medcommons.router.services.ccrmerge."+name+"Merger";
        	log.info("RefIDMerge: about to load " + className);
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
    public static MergerFactory getInstance() {
        if(instance == null){
        	
            instance = new ReferenceIDsMergerFactory(); 
        }
        return instance;
     }

}
