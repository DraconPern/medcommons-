package net.medcommons.application.dicomclient.utils;

import java.util.StringTokenizer;

/**
 * Utilities for parsing DICOM names into names usable by the CCR.
 * <P/>
 * Names are in format:
 * <BR/>
 * FAMILY^GIVEN^MIDDLE^TITLE
 * <BR/>
 * or
 * <BR/>
 * FAMILY GIVEN MIDDLE TITLE
 * <BR/>
 * or 
 * FAMILY,GIVEN,MIDDLE,TITLE
 * Example "HERNANDEZ^JANE^ELLEN".
 * Example "DOE JOHN"
 * Example "DOYLE,SEAN"
 *
 * ?? What about "DOE    JOHN"? "DOE \t JOHN"?
 *
 * Note that if "^" occurs anywhere in the name that it is assumed that "^" is the delimiter
 * for the entire string - the delimiters are never mixed. This is so we can handle the case
 * <BR/>
 * VAN WINKLE^RIP
 * <BR/>
 * where the space is part of the last name.
 * But also note that 
 * <BR/>
 * VAN WINKLE RIP
 * <BR/>
 * is ambiguous - RIP will be interpreted as a middle name.
 * 
 * If the name string ends with a delimiter - the last token will be null.
 * 
 * @author mesozoic
 *
 */
public class DicomNameParser {
	private final String delimiter1 = "^";
	private final String delimiter2 = ",";
	private final String delimiter3 = " ";
	
	
	/**
	 * Selects the delimiter used in the name.
	 * The delimiter is the same for all segments
	 * in a filename.
	 * 
	 * 
	 * @param dicomName
	 * @return
	 */
	private String selectDelmiter(String dicomName){
		String theDelimiter = delimiter1;
		int i = dicomName.indexOf(theDelimiter);
		if (i == -1){
			theDelimiter = delimiter2;
			i = dicomName.indexOf(theDelimiter);
		}
		if (i == -1){
			theDelimiter = delimiter3;
			i = dicomName.indexOf(theDelimiter);
		}
		if (i == -1){
			theDelimiter = null;
		}
		return(theDelimiter);
	}
	private String getNthToken(StringTokenizer tokenizer, int count){
		String val = null;
		if (tokenizer.hasMoreTokens())
			val = tokenizer.nextToken();
		
		for (int i=0;i<count;i++){
			if (tokenizer.hasMoreTokens()){
				val = tokenizer.nextToken();
			}
			else{
				val=null; 
				break;
			}
		}
		return(val);
	}
	
	public  String familyName(String dicomName){
		String theDelimiter = selectDelmiter(dicomName);
		String name=dicomName;
		
		if (theDelimiter != null){
			StringTokenizer tokenizer = new StringTokenizer(dicomName,theDelimiter);
			name = getNthToken(tokenizer, 0);
		}
		
		return(name);
			
	}


	/**
	 * Returns the middle name of the person.
	 * 
	 * Note that there are three cases:
	 * <ol>
	 * <li> The middle name or initial is returned.
	 * <li> an empty string is returned if the item in the list is a delimiter.
	 * <li> a null is returned if there are only family  or family/given names.
	 * </ol>
	 * @param dicomName
	 * @return
	 */
	public  String middleName(String dicomName){
		String theDelimiter = selectDelmiter(dicomName);
		if (theDelimiter == null){
			return(null);
		}
		String name;
		
		StringTokenizer tokenizer = new StringTokenizer(dicomName,theDelimiter);
		name = getNthToken(tokenizer, 2);
		return(name);
		
	}
	/*
	* Note that there are three cases:
	 * <ol>
	 * <li> The given name  is returned.
	 * <li> an empty string is returned if the item in the list is a delimiter.
	 * <li> a null is returned if there is only a family name and no delimiters.
	 * </ol>
	*/ 
	public   String givenName(String dicomName){
		String theDelimiter = selectDelmiter(dicomName);
		if (theDelimiter == null){
			return(null);
		}
		StringTokenizer tokenizer = new StringTokenizer(dicomName,theDelimiter);
		String name = getNthToken(tokenizer, 1);
		return(name);
	}
}
