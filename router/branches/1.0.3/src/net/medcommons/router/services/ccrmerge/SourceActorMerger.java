package net.medcommons.router.services.ccrmerge;

import org.apache.log4j.Logger;

import net.medcommons.phr.ccr.CCRElement;

public class SourceActorMerger extends AddMissingChildrenMerger{
	/**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SourceActorMerger.class);
	
	public boolean match(CCRElement from, CCRElement to) {
		
		boolean matches = false;
		
			String fromActorId = getActorId(from);
			String toActorId = getActorId(to);
			matches = fromActorId.equals(toActorId);
			if (log.isDebugEnabled())
				log.debug("ActorActorIDMerger: matches:" + fromActorId + "= " + toActorId + " " + matches);
		return(matches);
	}
		private String getActorId(CCRElement actorElement){
	    	CCRElement actorObject = actorElement.getChild("ActorID");
	    	
	    	if (actorObject != null){
	    		return(actorObject.getTextNormalize());
	    	}
	    	else
	    		return null;
	    	
	    }
}
