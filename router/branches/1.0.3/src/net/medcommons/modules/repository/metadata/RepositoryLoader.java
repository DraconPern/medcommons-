package net.medcommons.modules.repository.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;



import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;


public class RepositoryLoader {


	 public static Document loadTemplate(String filename) throws JDOMException, IOException {
			File templateFile = new File(filename);
		 	
		 	if (!templateFile.exists())
		 		throw new FileNotFoundException(templateFile.getAbsolutePath());
	        // Open the file as a stream and read it
		 
	        FileInputStream inputStream = new FileInputStream(templateFile);
	        StringBuffer xml = new StringBuffer();
	        byte[] buffer = new byte[4096];
	        int read = -1; 
	        while ((read = inputStream.read(buffer)) >= 0) {
	            xml.append(new String(buffer,0,read));
	        }
	        inputStream.close();                 
	        Document jdomDocument = new RepositoryBuilder().build(new StringReader(xml.toString()));
	        return jdomDocument;        
	    }
	 
	 /**
	  * Loads repository XML document, guaranteeing that all elements will be created
	  * as RepositoryElement child classes of the JDOM {@link Element}.
	  * 
	  * @param input       input stream to load, guaranteed to be closed
	  */
	 public static Document loadDocument(InputStream input) throws JDOMException, IOException{
	     try {
			 Document jdomDocument = new RepositoryBuilder().build(input);
			 return(jdomDocument);
	     }
	     finally {
			 input.close();
	     }
	 }
}