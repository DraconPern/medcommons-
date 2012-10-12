/*
 *xdsSoapSubmission.java
 *
 * Created on September 4, 2004, 4:09 PM
 */
/**
 *
 * @author  gunn
 */
package makejar;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.Iterator;
import java.net.ConnectException;
import java.security.PrivilegedActionException;
import java.io.IOException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.Text;
import javax.xml.soap.AttachmentPart;
import java.net.URL;
import javax.mail.internet.MimeMultipart;
import javax.activation.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;

public class xdsSoapRequest {
    
     /** Creates a new instance of xdsSoapRequest*/
    public void SoapRequest(File metadatafile, String[]attachfileArray, String[]mimeTypeArray, String[]uuidArray, int NumOfAttachFiles, String url)
      
      {    
        DataContentHandlerFactory dchf = null;
        String headervalue = " ";
        PrintStream orig = System.out;
      
  
        try { 
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage message = messageFactory.createMessage();
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            SOAPHeader soapHeader = envelope.getHeader();
            
            //adds an empty string to the SOAPheader
            soapHeader.addTextNode(headervalue);
            SOAPBody soapBody = envelope.getBody();
            SOAPElement  bodyElement = soapBody.addDocument(buildDoc(metadatafile));

	    msgtofile("request.msg");
            printMessage(message, "REQUEST");


            //process the attached files
              if (NumOfAttachFiles > 0 ) { 
       		for (int i = 0; i < NumOfAttachFiles; i++) {
                      
		    // Set the mime type and uuid for the  attachment file. The uuid is used as the file name of 
                    // stored in the repository
		    String ExtrinsicObjMimeType = mimeTypeArray[i];
                    String ExtrinsicObjUUID =  uuidArray[i];

		    // Gets the attached file.
                    DataSource Attachfile = new FileDataSource(attachfileArray[i]);
		    DataHandler Attachmentdh = new DataHandler(Attachfile);
                                   
		    AttachmentPart Attachmentpart = message.createAttachmentPart(Attachmentdh);
		    Attachmentpart.setMimeHeader("Content-Type", ExtrinsicObjMimeType);
		    Attachmentpart.setContentId(ExtrinsicObjUUID);
                
		    message.addAttachmentPart(Attachmentpart);
		}
	    }
                 
            SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection conn = connFactory.createConnection();
            SOAPMessage response = conn.call(message, url);
            conn.close();
                
            //prints out the response back from the registry
            SOAPMessage responsemessage = onMessage(response);

            System.setOut(orig);
            System.out.println("See response.msg\n\n");
	    msgtofile("response.msg");
            printMessage(responsemessage, "RESPONSE");
	    	 
            // send exceptions to the error.log file
            } catch (ConnectException ce) {
               System.setOut(orig);
               System.out.println("\n\nERROR OCCURRED: See error.log\n");
               redirectOut();              
               ce.printStackTrace();

            } catch (SOAPException ex) {
               System.setOut(orig);
               System.out.println("\n\nERROR OCCURRED: See error.log\n");
	       redirectOut();              
               ex.printStackTrace();
   
            } catch (Exception e) {
               System.setOut(orig);
               System.out.println("\n\nERROR OCCURRED: See error.log\n");
               redirectOut();              
               e.printStackTrace();
            }
                
	    
       }

     public void redirectOut() {
	 //directs System.out to the logfile
	try {
	  File log = new File("error.log");
          PrintStream printStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(log)) , true);
          System.setErr(printStream);
          System.setOut(printStream);
	     } catch (IOException ioe) {
		  System.out.println("Problem with the logfile");
             }
         }
    
     public void msgtofile(String msgfilename) {
	 //directs SOAP messages to a file
	try {
	  File msgfile = new File(msgfilename);
          PrintStream printStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(msgfile)) , true);
          System.setErr(printStream);
          System.setOut(printStream);
	     } catch (IOException ioe) {
		  System.out.println("Problem with the message file");
             }
         }
    
    
     public Document buildDoc(File documentfile){
        
        Document document = null;
        PrintStream orig = System.out;

        //uses the JAXP API to build a DOM Document. This parses the SubmitObjectsRequest containing
        //registry metadata for addition to the soap body

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dbFactory.setNamespaceAware(true);
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            document = builder.parse(documentfile);
        } catch (ParserConfigurationException pce) {
            System.setOut(orig);
            System.out.println("\n\nERROR OCCURRED: See error.log\n");
            redirectOut();              
            pce.printStackTrace();
        } catch (org.xml.sax.SAXException se) {
            System.setOut(orig);
            System.out.println("\n\nERROR OCCURRED: See error.log\n");
            redirectOut();              
            se.printStackTrace();
        } catch (IOException ex) {
            System.setOut(orig);
            System.out.println("\n\nERROR OCCURRED: See error.log\n");
            redirectOut();                  
            ex.printStackTrace();
        }
        
        return document;
    }

 
    public SOAPMessage onMessage(SOAPMessage msg){
        //handles the response back from the registry
        PrintStream orig = System.out;      
  
        try {
            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
            Name response = env.createName("response");
            env.getBody().getChildElements(response);
        
        } catch (SOAPException ex) {
            System.setOut(orig);
            System.out.println("\n\nERROR OCCURRED: See error.log\n");
            redirectOut();                       
            ex.printStackTrace();
        }
              
        return msg;
        
    }
    
    private static void printMessage(SOAPMessage message, String headerType) throws IOException, SOAPException {
        if (message != null) {
            //get the mime headers and print them
            System.out.println("\n\nHeader: " + headerType);
            if (message.saveRequired()) {
                message.saveChanges();
            }
            MimeHeaders headers = message.getMimeHeaders();
            printHeaders(headers);
            
            //print the message itself
            System.out.println("\n\nMessage: " + headerType);
            message.writeTo(System.out);
            System.out.println();
        }
    }
    
    private static void printHeaders(MimeHeaders headers) {
	// used to print all http headers 
        printHeaders(headers.getAllHeaders());
    }
    
    private static void printHeaders(Iterator iter){
        while (iter.hasNext()) {
            MimeHeader header = (MimeHeader)iter.next();
            System.out.println("\t" + header.getName() + " : " + header.getValue());
        }
    }

    public static void clearFile(String filename) {
          
 	File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
 
    }
    
    public static void main(String[] args) {
        
    	String[] attachfileArray;
        String[] mimeTypeArray;
        String[] uuidArray;
        int NumOfAttachFiles = 0;
        String xdsPropertiesFile = "test.properties";
        
   
 	clearFile("error.log");
 	clearFile("request.msg");
        clearFile("response.msg");

    	Properties props = new Properties();
        try {
	    props.load(new FileInputStream(xdsPropertiesFile));
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

        if (args.length != 1) {
            System.err.println("\nUsage: java xdsTestClient");
        }
        
        String SubmitMetadataFile = props.getProperty("metadatafile");
        File Metadata = new File(SubmitMetadataFile);
	String url = props.getProperty("url");

        NumOfAttachFiles = Integer.valueOf(props.getProperty("NumOfDoc")).intValue();

        // if the number of attached files is greater than 0 retrieve the file and
        // associated metadata
        attachfileArray = new String[2];
        mimeTypeArray = new String[2];
        uuidArray = new String[2];

        if ((NumOfAttachFiles >= 0) && (NumOfAttachFiles <= 2)) {
                if (NumOfAttachFiles == 1) {
                    attachfileArray[0] = props.getProperty("doc1");
                    mimeTypeArray[0] = props.getProperty("doc1mimeType");
                    uuidArray[0] = props.getProperty("doc1UUID");
                }  
		if (NumOfAttachFiles == 2) {
                    attachfileArray[0] = props.getProperty("doc1");  
                    mimeTypeArray[0] = props.getProperty("doc1mimeType");
                    uuidArray[0] = props.getProperty("doc1UUID");

                    attachfileArray[1] = props.getProperty("doc2");
                    mimeTypeArray[1] = props.getProperty("doc2mimeType");
                    uuidArray[1] = props.getProperty("doc2UUID");
                }   
        } else {
	    System.out.println("\nThe number of attached files must be 0, 1 or 2\n");
            System.exit(0);
        }          
        
  	xdsSoapRequest SoapClient = new xdsSoapRequest();
	SoapClient.SoapRequest(Metadata, attachfileArray, mimeTypeArray, uuidArray, NumOfAttachFiles, url);
        
    }
}

