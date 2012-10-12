package net.medcommons.router.services.ccrmerge;

import org.apache.log4j.Logger;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.ccr.CCRElement;

public class IDsMerger  extends AddMissingChildrenMerger{
    private static Logger log = Logger.getLogger(IDsMerger.class);
	/**
	 * An ID is the same 
	 * a) if the type and the value are the same.
	 * b) if there are no types but the values are the same.
	 * This is possibly too simplistic.
	 */
	public boolean match(CCRElement from, CCRElement to) {
		boolean matches = false;
		String fromType = getIDType(from);
		String toType = getIDType(to);
		String fromID = getID(from);
        String toID = getID(to);
        // If there is no type for either from or to  -
        // just match against ID value.
		if ((fromType == null) &&(toType == null)){
		    if ((fromID != null) && (Str.equals(fromID, toID)))
                matches = true;
		}
		// Else - both type and ID value need to match.
		else if ((fromType != null) && (Str.equals(fromType, toType))){
			
			if ((fromID != null) && (Str.equals(fromID, toID)))
				matches = true;
				
		}
		//log.info("IDs:" + fromType + ", " + toType + ", " + fromID + ", " + toID + ", " + matches);
		return(matches);
	}
	protected String getIDType(CCRElement element){
		String idType = null;
		CCRElement typeElement = element.getChild("Type");
		if (typeElement != null){
			CCRElement textElement = typeElement.getChild("Text");
			if (textElement != null){
				idType = textElement.getText();
			}
		}
			
		return(idType);
	}
	protected String getID(CCRElement element){
		String id = null;
		CCRElement idElement = element.getChild("ID");
		if (idElement != null){
				id = idElement.getText();			
		}
		return(id);
	}

}
