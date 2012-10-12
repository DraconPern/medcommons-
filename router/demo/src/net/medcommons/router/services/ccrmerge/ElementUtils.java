package net.medcommons.router.services.ccrmerge;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.medcommons.phr.ccr.CCRElement;

public class ElementUtils {
	
	protected static  String getDateTime(CCRElement element){
		String date = null;
		CCRElement dateElement = element.getChild("DateTime");
		if (dateElement != null){
			CCRElement d = dateElement.getChild("ExactDateTime");
			if (d != null){
				date = d.getTextNormalize();
			}
			else d = dateElement.getChild("ApproximateDateTime");
			if (d != null){
				CCRElement dText = d.getChild("Text");
				if (dText != null){
					date = dText.getTextNormalize();
				}
			}
			else{
				d = dateElement.getChild("DateTimeRange");
				if (d != null){
					CCRElement beginElement = d.getChild("BeginRange");
					if (beginElement != null){
						CCRElement eElement = beginElement.getChild("ExactDateTime");
						if (eElement != null){
							date = eElement.getTextNormalize();
						}
					}
				}
			}
				
			
		}
		return(date);
	}
	protected static  String getCDTValue(CCRElement codedDataObject, String elementName){
		String value = null;
		CCRElement codedType = codedDataObject.getChild(elementName);
		if (codedType != null){
			CCRElement textType = codedType.getChild("Text");
			if (textType != null){
				value = textType.getTextNormalize();
			}
		}
		return(value);
	}
	protected static  String getType(CCRElement codedDataObject){
		String value = getCDTValue(codedDataObject, "Type");
		return(value);
	}
	protected static  String getStatus(CCRElement codedDataObject){
		String value = getCDTValue(codedDataObject, "Status");
		return(value);
	}
	protected static  String getDescription(CCRElement codedDataObject){
		String value = getCDTValue(codedDataObject, "Description");
		return(value);
	}
	
	protected static boolean matchDateTime(CCRElement from, CCRElement to) throws ParseException{
		boolean matches = false;
		
		String fromDate = getDateTime(from);
		String toDate = getDateTime(to);
		
		if (isMarked(from) || (isMarked(to))){
			
			matches = ElementUtils.fuzzyDateMatch(fromDate, toDate);
			//log.debug("Fuzzy date match + " + fromDate + " " + toDate + " " +matches);
		}
		else if ((fromDate != null)){
		    if (fromDate.equals(toDate)){
		        matches = true;
		    }
		}
		return(matches);
	}
	/**
	 * Returns true if the date strings are identical or if the "yyyy-MM-dd"
	 * parsing of the date string matches (e.g., the time and time zone are dropped from
	 * the comparison).
	 * 
	 * Also note that if two approximate date time strings (like "Yesterday") are identical
	 * that the fuzzy match returns true.
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 * @throws ParseException
	 */
	protected static boolean fuzzyDateMatch(String d1, String d2) throws ParseException{
		try{
		if ((d1 == null) || (d2==null)) return false;
		else if (d1.equals(d2)) return true;
		else{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date1 = formatter.parse(d1);
			Date date2 = formatter.parse(d2);
			return(date1.getTime() == date2.getTime());	
		}
		}
		catch(ParseException e){
			return(false);
		}
	}
	/**
	 * Tests an element to see if it has a marked attribute.
	 * In the future we may need to test for more than one marked attribute
	 * value?
	 * @param element
	 * @return
	 */
	protected static boolean isMarked(CCRElement element){
		return(element.getAttribute("marked") != null);
		
	}
	
	/**
	 * Note - returns true if both null.
	 * @param one
	 * @param two
	 * @return
	 */
	protected static boolean isEqual(String one, String two){
		if (one == two) return true;
		else if (one == null) return false;
		else return(one.equals(two));
	}
	protected static boolean isEqualNotNull(String one, String two){
		if (two == null) return false;
		else if (one == null) return false;
		else return(one.equals(two));
	}
}
