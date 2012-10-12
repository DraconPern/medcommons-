package net.medcommons.cxp.utils;

import org.apache.log4j.Logger;

import net.medcommons.cxp.CXP_10;
import net.medcommons.cxp.Parameter;

public class ParameterHandling {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(ParameterHandling.class);
	
	public static Parameter[] createParameterBlock(int nElements){
    	Parameter params[] = new Parameter[nElements];
    	for (int i=0;i<nElements;i++){
    		params[i] = new Parameter();
    	}
    	
    	return(params);
    }

/**
 * Returns the value of the specified parameter from the parameter array 
 * or the default value if there is no match.
 * 
 * @param name
 * @param parameters
 * @return
 */
public static String getParameterValue(String name, Parameter[] parameters, String defaultValue){
	String value = null;
	for (int i=0;i<parameters.length;i++){
		String pName = parameters[i].getName();
		if (pName.equalsIgnoreCase(name)){
			value = parameters[i].getValue();
			log.info("getParameterValue: name=" + name + ", value=" + value);
			return(value);
		}
	}
	
	if (value == null) 
		value = defaultValue;
	log.info("getParameterValue: name=" + name + ", value=" + value);
	return(value);
	
}
}
