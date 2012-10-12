package net.medcommons.router.services.ccrmerge;

import net.medcommons.phr.ccr.CCRElement;

import org.apache.log4j.Logger;

public class MedicationMerger extends StructuredProductTypeMerger{
	private static Logger log = Logger.getLogger(MedicationMerger.class);
	public boolean match(CCRElement from, CCRElement to) {
		boolean matches = super.match(from, to);
		if (log.isDebugEnabled()){
			log.debug("+++ productName matches? = " + matches + " from = " + getProductName(from) + " to =" + getProductName(to));
			if(matches){
				log.debug("== Medication matches from \n" + from.toXMLString() + 
						"\n to \n" + to.toXMLString());
			}
			
		}
		return(matches);
	}

}
