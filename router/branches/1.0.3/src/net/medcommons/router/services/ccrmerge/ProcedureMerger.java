package net.medcommons.router.services.ccrmerge;

import org.apache.log4j.Logger;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.ccr.CCRElement;

public class ProcedureMerger extends AddMissingChildrenMerger{
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger
			.getLogger(ProcedureMerger.class);
	public boolean match(CCRElement from, CCRElement to) {
		boolean matches;
		
		try{
			String ccrDataObjectID = from.getChildTextTrim("CCRDataObjectID");
	        //log.info("match?"+ from.getName() + "==" + to.getName());
	        if(!Str.blank(ccrDataObjectID)) { // it is a data element
	            if (log.isDebugEnabled())
	            	log.debug("match from id = " + ccrDataObjectID + " to id = " +to.getChildTextTrim("CCRDataObjectID") );
	            matches = Str.equals(ccrDataObjectID, to.getChildTextTrim("CCRDataObjectID"));
	            if (!ElementUtils.isMarked(from)){
	            	if (matches){ 
	            		if (log.isInfoEnabled())
	            			log.info("Element is not marked; CCRDataObjectID matches; marking to replace on merge");
	            		to.setReplaceOnMerge(true);
	            		return true;
	            		}
	            	else {
	            		if(log.isInfoEnabled())
	            			log.info("Element is not marked; CCRDataObjectID does not match.");
	            		return(false);
	            	}
	            }
	        }
	        boolean finished = false;
			matches = from.getName().equals(to.getName());
			if (matches){
				String fromDate = ElementUtils.getDateTime(from);
				String toDate =  ElementUtils.getDateTime(to);
				if ((fromDate != null) || (toDate != null)){
					matches = ElementUtils.matchDateTime(from, to);
					if (!matches){
						finished = true;
					}
				}
			}
			if (!finished){
				String fromType = ElementUtils.getType(from);
				String toType = ElementUtils.getType(to);
				if ((fromType != null) || (toType != null)){
					if (fromType != null){
						matches = fromType.equals(toType);
					}
					else{
						matches = toType.equals(fromType);
					}
					if (!matches){
						finished = true;
					}
				}
					
			}
		}
		catch(Exception e){
			throw new RuntimeException("Error matching element of type " + from.getName() + " to " + to.getName(), e);
		}
		return(matches);
	}
	
}
