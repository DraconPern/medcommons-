package net.medcommons.router.services.ccrmerge;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;

public class CCRCodedDataObjectTypeMerger extends AddMissingChildrenMerger {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger
			.getLogger(CCRCodedDataObjectTypeMerger.class);

	public Change merge(CCRElement from, CCRDocument toDocument, CCRElement to)
			throws MergeException {
		if (to.getReplaceOnMerge()){
			to.removeContent();
			
			to.addContent(from.removeContent());
			return new XPathChange(to.getName(), ChangeOperation.UPDATE, from.getText());
		}
		else
			return(super.merge(from, toDocument, to));
		
	}

	
	
	
	/**
	 * A bit unclear.
	 * Two models. If they are marked on input - the CCRDataObjectID don't matter (they aren't even examined).
	 * If they aren't marked - then the CCRDataObjectID determines identity.
	 * 
	 * If CCRDataObjectID - they match. 
	 * If the CCR 
	 * Otherwise - go to content to see.
	 */
	public boolean match(CCRElement from, CCRElement to) {
		
		
		boolean matches = false;
		try{
			String ccrDataObjectID = from.getChildTextTrim("CCRDataObjectID");
			//log.info("Match " + from.getName());
			/*
			 * If datetime is present - a mismatched date means no match. But no date means
             * it isn't yet known if it is a match or not.
			 */
			 
            String fromDate = ElementUtils.getDateTime(from);
            String toDate =  ElementUtils.getDateTime(to);
            //log.info("##dates in match " + fromDate + ", " + toDate);
            if ((fromDate != null) && (toDate != null)){
                matches = ElementUtils.matchDateTime(from, to);
                if (!matches){
                    return(false); // If dates don't match - it's not a match.
                }
            }
	        if (log.isDebugEnabled())
	        		log.debug("match?"+ from.getName() + "==" + to.getName());
	        if(!Str.blank(ccrDataObjectID)) { // it is a data element
	            if (log.isDebugEnabled())
	            	log.debug("match " + from.getName() + " from id = " + ccrDataObjectID + " to id = " +to.getChildTextTrim("CCRDataObjectID") );
	            matches = Str.equals(ccrDataObjectID, to.getChildTextTrim("CCRDataObjectID"));
	            if (!ElementUtils.isMarked(from)){
	            	if (matches){ 
	            		if (log.isDebugEnabled())
	            			log.debug("Element is not marked; CCRDataObjectID matches; marking to replace on merge");
	            		to.setReplaceOnMerge(true);
	            		return true;
	            		}
	            	else {
	            		if(log.isDebugEnabled())
	            			log.debug("Element is not marked; CCRDataObjectID does not match.");
	            		matches = false;
	            	}
	            }
	            else 
	            	if (log.isDebugEnabled())
	            		log.debug(" marked " + from.getName());
	        }
	        // Next - try matching coded values if present
	        List<CodedValue> fromCodes = codedValues(from);
	        List<CodedValue> toCodes = codedValues(to);
	        if ((fromCodes != null) && (toCodes != null)){
	        	// Only compare if both non-null.
	        	// Basic algorithm:
	        	// if the codedValues match, then the element matches.
	        	// if coding systems match - but the values don't -then it doesn't match.
	        	// All other cases indeterminate - continue for other matching logic below.
	        	for (int i=0;i<fromCodes.size();i++){
	        		CodedValue fCode = fromCodes.get(i);
	        		for (int j = 0;j<toCodes.size(); j++){
	        			CodedValue tCode = toCodes.get(j);
	        			if (log.isDebugEnabled())
	        				log.debug("Code testing  match: from " + fCode + " to " + tCode);
	        			if (tCode.codingSystem == fCode.codingSystem){
	        				matches = fCode.matches(tCode);
	        				if (log.isDebugEnabled())
	        					log.debug("matches? =" + matches);
	        				return(matches);
	        			}
	        		}
	        	}
	        	
	        }
	        boolean finished = false;
			//matches = from.getName().equals(to.getName());
			/*
			 
			 * 
			 * Basic matching rule below - if either element is non-null - they have
			 * to match. Any non-null element mis-matches means no match.
			 */
			
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
			if (!finished){
				String fromDescription = ElementUtils.getDescription(from);
				String toDescription = ElementUtils.getDescription(to);
				if ((fromDescription != null) || (toDescription != null)){
					if (fromDescription != null){
						matches = fromDescription.equals(toDescription);
					}
					else{
						matches = toDescription.equals(fromDescription);
					}
					if (!matches){
						finished = true;
					}
				}
					
			}
			if (!finished){
				String fromStatus = ElementUtils.getStatus(from);
				String toStatus = ElementUtils.getStatus(to);
				if ((fromStatus != null) || (toStatus != null)){
					if (fromStatus != null){
						matches = fromStatus.equals(toStatus);
					}
					else{
						matches = toStatus.equals(fromStatus);
					}
					if (!matches){
						finished = true;
					}
				}
					
			}
			/*
			log.info("CDT - matches = " + matches + " from=\n" +
					from.toXMLString() + 
					" \n + to=\n" +
					to.toXMLString());
					*/
	        
		}
		catch(ParseException e){
			matches = false;
			log.error("Parsing exception", e);
			throw new RuntimeException(e);
		}
		
		return matches;
	}

	public Change importNode(CCRElement from, CCRDocument toDocument,
			CCRElement toParent) throws MergeException {
		return(super.importNode(from, toDocument, toParent));
	}
	
	/**
	 * Returns a List of CodedValues for easier comparison.
	 * Note that the universe of CodedValues is smaller than the 
	 * number of Description/Code/CodingSystems - CodedValues
	 * handles some synonyms.
	 * @param element
	 * @return
	 */
	protected List<CodedValue> codedValues(CCRElement element){
		List<CodedValue> values = null;
		CCRElement description = element.getChild("Description");
		if (description != null){
			List<CCRElement> codes = description.getChildren("Code");
			
			if (codes != null){
				if (log.isDebugEnabled())
					log.debug("There are " + codes.size() + " coded values in " + element.getName());
				values = new ArrayList<CodedValue>();
				Iterator <CCRElement> iter = codes.iterator();
				while (iter.hasNext()){
					CCRElement codeElement = iter.next();
					String value = codeElement.getChildTextNormalize("Value");
					String codingSystem = codeElement.getChildTextNormalize("CodingSystem");
					String version = codeElement.getChildTextNormalize("Version");
					if ((!"".equals(codingSystem)) &&((!"".equals(value)))){
						CodingSystem c = CodingSystem.factory(codingSystem, version);
						CodedValue cValue = new CodedValue(c, value);
						values.add(cValue);
					}
				}
				
			}
		}
		return(values);
		
	}
}
