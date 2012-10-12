package net.medcommons.application.ccreditortest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import net.medcommons.application.ccreditortest.CCREditReference;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpParams;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import astmOrgCCR.ContinuityOfCareRecordDocument;

public class PostCCR {
	private CCREditReference ccrReference;
	
	public PostCCR(CCREditReference ccrReference){
		this.ccrReference = ccrReference;
	}
	
	public void post(ContinuityOfCareRecordDocument ccr) throws IOException,JDOMException{
		File temp = new File ("temp" + System.currentTimeMillis() + ".xml");
		FileWriter writer = new FileWriter(temp);
		ccr.save(writer);
		writer.flush();
		writer.close();
		//String url = ccrReference.mergeActionURL + "?token=" + ccrReference.token + "&contentType=" + ccrReference.contentType
		//+ "&auth=" + "abcdefghijklmnop";
		String url = ccrReference.sessionURI;
		PutMethod put = new PutMethod(url);
		System.err.println("About to put to " + url);
		 put.setRequestBody(new FileInputStream(temp));
		/*PostMethod filePost = new PostMethod(url);
		
		  Part[] parts = {
		      //new StringPart("token", ccrReference.token),
		     //new StringPart("contentType", ccrReference.contentType),
		     
		      new FilePart("document", temp)
		  };
		  filePost.setRequestEntity(
                  new MultipartRequestEntity(parts, filePost.getParams())
                  );
		  */
		  HttpClient client = new HttpClient();
          client.getHttpConnectionManager().
              getParams().setConnectionTimeout(5000);
          client.getParams().setParameter("http.useragent","HealthFrame");
          client.getParams().setParameter("User-Agent","HealthFrame");
                  
          long startTime = System.currentTimeMillis();
          int status = client.executeMethod(put);
          long endTime = System.currentTimeMillis();
          if (status == HttpStatus.SC_OK) {
              
              SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
              builder.setFeature(
            		  "http://apache.org/xml/features/validation/schema", true);
      			Document doc = builder.build(put.getResponseBodyAsStream());
      			XMLOutputter serializer = new XMLOutputter();
    			StringWriter sOut = new StringWriter();
    			serializer.output(doc,sOut);
    			String sDoc = sOut.getBuffer().toString();
    			
      			
      			System.err.println("OK response in " + (endTime - startTime) +" msec\n" + sDoc);
      			
      			
          } else {
        	  System.err.println(
                  "Upload failed, response=" + status + ": " + HttpStatus.getStatusText(status)
              );
          }
	}
}

