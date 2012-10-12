/*
 * $Id$
 * Created on 25/08/2006
 */
package net.medcommons.router.services.ccrmerge;

import java.util.ArrayList;
import java.util.Iterator;

import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.filter.ElementFilter;

/**
 * Specialized merge algorithm for ActorObjectID elements.
 * 
 * Simply replaces the children of to with those of from.
 * 
 * @author ssadedin
 */
public class ActorObjectIDMerger implements CCRMerger {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ActorObjectIDMerger.class);
    
    public ActorObjectIDMerger() {
    }

    /**
     * Merge has to update references to ActorIds in other portions of document.
     */
    public Change merge(CCRElement from, CCRDocument toDocument, CCRElement to) throws MergeException {
        String oldId = to.getTextNormalize();
        String newId = from.getTextNormalize();
       
        if(log.isDebugEnabled())
            log.debug("Updating Actor Object ID " + oldId + " to " + newId);
        
        to.setText(newId);
        
        // Update references to id
        try {
            ArrayList<CCRElement> toUpdate = null;
            Iterator<CCRElement> actorIds = toDocument.getRoot().getDescendants(new ElementFilter("ActorID"));
            int updateCount = 0;
            while(actorIds.hasNext()) {
                CCRElement  e = actorIds.next();
                if(oldId.equals(e.getTextNormalize())) {
                    if(toUpdate == null)
                        toUpdate = new ArrayList<CCRElement>();
                    toUpdate.add(e);
                    ++updateCount;
                }
            }
            if(toUpdate!=null) {
                for (CCRElement e : toUpdate) {
                    e.setText(newId);
                }
            }
            if(log.isDebugEnabled())
                log.debug("Updated " + updateCount + " references to old actor id " + oldId + " to " + newId);
        }
        catch (PHRException e) {
            throw new MergeException("Unable to update references to actor id " + oldId + " to " + newId);
        }
        
        return new XPathChange(to.getName(), ChangeOperation.UPDATE);
    }

    public boolean match(CCRElement from, CCRElement to) {
    	String newId = from.getTextNormalize();
    	String toId = to.getTextNormalize();
    	if (log.isDebugEnabled())
    		log.debug("testing for match of element with " + newId + " to " + toId);
        return from.getName().equals(to.getName());
    }

    public Change importNode(CCRElement from, CCRDocument toDocument, CCRElement toParent) throws MergeException {
    	
        String newId = from.getTextNormalize();
        if (log.isDebugEnabled())
        	log.debug("about to ignore importNode:" + newId);
        return null;
    }

}
