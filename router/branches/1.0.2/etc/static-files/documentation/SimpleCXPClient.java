/**
 * Public Domain CXP SOAP Client
 * 
 * @version 0.9.3
 * November 2, 2005
 */
package net.cxp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

import org.apache.axis.message.MessageElement;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * A trivial CXP client written in Java.
 * 
 * @author sean
 *
 */
public class SimpleCXPClient {

	/**
	 * Takes two arguments: the URL of the SOAP endpoint and the 
	 * filename of the CCR XML file.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		// A real program would test the validity of the input arguments.

		if (args.length!=2){
			usage();
			return;
		}
			
		URL serviceURL = new URL(args[0]);
		CCRServiceSoap ccrService = new CCRServiceLocator()
				.getCCRServiceSoap(serviceURL);


		Ccr ccr = new Ccr();

		File ccrFile = new File(args[1]);
		if (!ccrFile.exists())
			throw new FileNotFoundException("CCR file does not exist: "
					+ ccrFile.getAbsolutePath());
		FileInputStream ccrIs = new FileInputStream(ccrFile);
		InputSource is = new InputSource(ccrIs);

		DOMParser parser = new DOMParser();
		parser.parse(is);
		Document doc = parser.getDocument();

		// Generate the message
		MessageElement me = new MessageElement(doc.getDocumentElement());
		MessageElement[] messages = new MessageElement[1];
		messages[0] = me;

		ccr.set_any(messages);

		// Send message
		CCRResponse response = ccrService.submitCCR(ccr);

		// Print the response
		System.out.println("Response:");
		System.out.println(" ErrorCode=" + response.getErrorCode());
		System.out.println(" ErrorDescription=" + response.getErrorDescription());
		System.out.println(" UUID  =" + response.getUUID());

	}
	public static void usage(){
		System.out.println("Usage:");
		System.out.println("java -cp <path to jar files plus this source> net.cxp.SimpleCXPClient <CXP url> <CCR file>");
		
		System.out.println("\nExample:");
		
		StringBuffer buff = new StringBuffer();
		buff.append("java ");
		buff.append(" -cp \"");
		buff.append("build/classes/;lib/axis/;lib/axis/axis.jar;");
		buff.append("lib/axis/commons-logging-1.0.4.jar;lib/axis/log4j-1.2.8.jar;lib/axis/saaj.jar;");
		buff.append("lib/axis/axis-schema.jar;lib/axis/commons-discovery-0.2.jar;lib/axis/jaxrpc.jar;");
		buff.append("lib/axis/wsdl4j-1.5.1.jar;lib/xerces-2_7_1/xercesImpl.jar;lib/xerces-2_7_1/xml-apis.jar;");
		buff.append("lib/xerces-2_7_1/resolver.jar\"");
		buff.append(" net.cxp.SimpleCXPClient");
		buff.append(" http://gateway001.test.medcommons.net:9080/router/services/CCRServiceSoap");
		buff.append("  etc/data/ccr1.xml ");
		
		System.out.println(buff.toString());



		
	}

}
