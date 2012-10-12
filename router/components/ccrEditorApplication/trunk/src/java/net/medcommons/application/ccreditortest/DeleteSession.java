package net.medcommons.application.ccreditortest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;

import org.apache.xmlbeans.XmlException;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
public class DeleteSession {
	private CCREditReference reference;
	private HttpClient client;
	public DeleteSession(CCREditReference reference) {
		this.reference = reference;
		client = new HttpClient(new MultiThreadedHttpConnectionManager());
		
	}
	public void delete()
	{
		try{
			DeleteMethod deleteMethod = new DeleteMethod(reference.sessionURI);
			int status = client.executeMethod(deleteMethod);
			 if (status == HttpStatus.SC_OK) {
	              
	              SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
	              builder.setFeature(
	            		  "http://apache.org/xml/features/validation/schema", true);
	      			Document doc = builder.build(deleteMethod.getResponseBodyAsStream());
	      			XMLOutputter serializer = new XMLOutputter();
	    			StringWriter sOut = new StringWriter();
	    			serializer.output(doc,sOut);
	    			String sDoc = sOut.getBuffer().toString();
	      			
	      			System.err.println("OK response:\n" + sDoc);
	      			
	      			
	          } else {
	        	  System.err.println(
	                  "Upload failed, response=" + HttpStatus.getStatusText(status)
	              );
	          }
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
}


