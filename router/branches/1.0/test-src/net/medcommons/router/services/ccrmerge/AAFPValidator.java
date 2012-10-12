package net.medcommons.router.services.ccrmerge;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
/**
 * Client interface to AAFP validator; throws IllegalArgumentException if any fatal 
 * errors are returned from the specified CCR.
 * 
 * Form appears on http://chit.dyndns.org/CCRValidation/:
 *	&lt;FORM METHOD=POST ENCTYPE="multipart/form-data" ACTION="./validator"&gt;
 *		File to upload: &lt;INPUT NAME="upfile" TYPE=FILE size="50"/&gt;
 *		&lt;INPUT TYPE=SUBMIT VALUE="Validate CCR"/&gt;
 *	&lt;/FORM&gt;
 * @author sdoyle
 *
 */
public class AAFPValidator {
	private final static String VALIDATOR_URL = "http://chit.dyndns.org/CCRValidation/validator";

	/**
	 * Uploads the specified CCR; throws IllegalArgumentException if there are any fatal 
	 * errors in the CCR.
	 * 
	 * @param ccrFile
	 * @throws IOException
	 * @throws HttpException
	 * @throws JDOMException
	 */
	public void validateCCR(java.io.File ccrFile) throws IOException,
			HttpException, JDOMException {
		
		PostMethod post = new PostMethod(VALIDATOR_URL);

		Part[] parts = {
				new FilePart("upfile", ccrFile) 
		};
		post.setRequestEntity(new MultipartRequestEntity(parts, post
				.getParams()));
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(5000);
		int status = client.executeMethod(post);
		if (status != HttpStatus.SC_OK) {
			System.err.println("Upload failed, response="
					+ HttpStatus.getStatusText(status));
			return;
		}
		InputStream responseStream = post.getResponseBodyAsStream();
		SAXBuilder saxBuilder=new SAXBuilder("org.apache.xerces.parsers.SAXParser");

         org.jdom.Document  jdomDocument=saxBuilder.build(responseStream);
        List<Element> nodeList  =XPath.selectNodes(jdomDocument, "//Error[@serverity='fatal']");
        Iterator<Element> iter=nodeList.iterator();


        StringBuffer buff = new StringBuffer();
        boolean fatalErrors = false;
       while(iter.hasNext()){
    	   fatalErrors = true;
           Element element=(org.jdom.Element)iter.next();
           Element message = element.getChild("Message");
           if (message == null){
        	   throw new NullPointerException("Null message in element:" + element.toString());
           }
           String s = message.getTextNormalize();
           buff.append(s);
           buff.append("\n");
       }
       if (fatalErrors){
    	   throw new IllegalArgumentException("Fatal CCR errors:" + buff.toString());
       }
       else
    	   return;


         


	}
}
