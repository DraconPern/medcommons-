package net.medcommons.router.services.ccrmerge;

import org.apache.log4j.Logger;

import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

public class DoseMerger extends AddMissingChildrenMerger {
	public DoseMerger(){
		super();
		//log.info("DoseMerger constructor");
	}
	 /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DoseMerger.class);
    
	 public Change importNode(CCRElement from, CCRDocument toDocument, CCRElement toParent) throws MergeException {
		// log.info("DoseMerger from " +  from.getName() + " to " + toParent.getName());
	 return(super.importNode(from, toDocument, toParent));
	 }
}
