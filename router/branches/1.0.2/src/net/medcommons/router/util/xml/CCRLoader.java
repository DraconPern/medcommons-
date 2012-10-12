package net.medcommons.router.util.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import net.medcommons.phr.ccr.CCRBuilder;

import org.jdom.Document;
import org.jdom.JDOMException;

/**
 * Creates a JDOM document from the XPathCache. 
 * @author mesozoic
 *
 */
public class CCRLoader {

	
	 public static Document loadTemplate(String templatePath) throws JDOMException, IOException {
		 	File templateFile = new File(templatePath);
		 	if (!templateFile.exists())
		 		throw new FileNotFoundException(templateFile.getAbsolutePath());
	        // Open the file as a stream and read it
	        FileInputStream inputStream = new FileInputStream(templatePath);
	        StringBuffer xml = new StringBuffer();
	        byte[] buffer = new byte[4096];
	        int read = -1; 
	        while ((read = inputStream.read(buffer)) >= 0) {
	            xml.append(new String(buffer,0,read));
	        }
	        inputStream.close();                 
	        Document jdomDocument = new CCRBuilder().build(new StringReader(xml.toString()));
	        return jdomDocument;        
	    }
}
