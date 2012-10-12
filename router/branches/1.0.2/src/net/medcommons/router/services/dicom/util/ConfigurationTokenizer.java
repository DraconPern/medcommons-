/*
 * 
 * Created on Apr 23, 2004
 *
 * Copyright MedCommons 2004
 * sean
 */
package net.medcommons.router.services.dicom.util;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;

/**
 * Utility methods for tokenized items in configuration. This is a temporary
 * class - a far better mechanism is to use XML to model this directly instead
 * of introducing compound values in attributes or elements. 
 * 
 * Problems with this code:
 * <ul>
 * <li> Tokenization of elements/attributes is evil.
 * <li> The primitive macro substitution mechanism should be 
 * 		replaced with something more standard. The semantics of
 * 		config.xml should be broadened so this isn't necessary.
 * </ul>
 * It certainly is the case that the value of a configuration may depend
 * on other configurations - the issue is how to organize it.
 * See Mantis task 96.
 * 
 * @author sean
 *
 */
public class ConfigurationTokenizer {

	public static List tokenize(String s, List result) throws ConfigurationException, Exception{
		StringTokenizer stk = new StringTokenizer(s, ", ");
		while (stk.hasMoreTokens()) {
			String tk = stk.nextToken();
			if (tk.startsWith("$")) {
				String newValue = 
					(String) Configuration.getInstance().getConfiguredValue("net.medcommons.services.dicom."
						+ tk.substring(1));

				tokenize(newValue, result);
			} else {
				result.add(tk);
			}
		}
		return result;
	}


	public static String[] tokenize(String s) throws ConfigurationException, Exception{
		if (s == null)
			return null;

		List l = tokenize(s, new LinkedList());
		return (String[]) l.toArray(new String[l.size()]);
	}
}
