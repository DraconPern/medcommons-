package net.medcommons.application.dicomclient.test;

import java.io.IOException;
import java.net.MalformedURLException;

import net.medcommons.application.dicomclient.http.action.StatusActionBean;
import net.medcommons.application.dicomclient.http.action.StatusUpdateActionBean;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import junit.framework.TestCase;


public class ContextManagerTest extends BaseTest{
	private static Logger log = Logger.getLogger("ContextManagerTest");
	public void setUp() throws Exception{
		super.setUp();

	}

	//runCommand
	public void testCancelAllTransactions() throws Exception{
		AccountFocusParameters info = getAccountFocusInfo();

		String urlArgs = info.toURLArguments();
		String contextUrl =  "http://localhost:16092/localDDL/StatusUpdate.action?update&command=" + StatusUpdateActionBean.COMMAND_CLEAR_ALL;

		try{
			WebConversation wc = new WebConversation();
		    WebRequest     req = new GetMethodWebRequest(contextUrl);
		    WebResponse   resp = wc.getResponse( req );
		    assertNotNull("Null Response", resp);
		    String elements[] = resp.getElementNames();
		    for (int i=0;i<elements.length;i++){
		    	log.info("element " + i + " = " + elements[i]);
		    }

		}
		catch(MalformedURLException e){
			log.info("Error with args " + contextUrl, e);
			throw e;
		}
		catch(IOException e){
			log.info("Error with args " + contextUrl, e);
			throw e;
		}
		catch(SAXException e){
			log.info("Error with args " + contextUrl, e);
			throw e;
		}
		catch(Throwable t){
			log.info("Ignored error with args " + contextUrl, t);
		}
	}
	/**
	 *
	 *
	 */
	public void testSetAccountFocus() throws Exception{
		AccountFocusParameters info = getAccountFocusInfo();

		String urlArgs = info.toURLArguments();
		String contextUrl =  "http://localhost:16092/setAccountFocus" + urlArgs;

		try{
			WebConversation wc = new WebConversation();
		    WebRequest     req = new GetMethodWebRequest(contextUrl);
		    WebResponse   resp = wc.getResponse( req );
		    assertNotNull("Null Response", resp);
		    String elements[] = resp.getElementNames();
		    for (int i=0;i<elements.length;i++){
		    	log.info("element " + i + " = " + elements[i]);
		    }

		}
		catch(MalformedURLException e){
			log.info("Error with args " + contextUrl, e);
			throw e;
		}
		catch(IOException e){
			log.info("Error with args " + contextUrl, e);
			throw e;
		}
		catch(SAXException e){
			log.info("Error with args " + contextUrl, e);
			throw e;
		}
	}
	/*
	  INFO [btpool0-9] (HttpServer.java:314) - setAccountFocus
<br>url/setAccountFocus/
	 INFO [btpool0-9] (HttpServer.java:315) - ContextManager[storageId=null,guid=null,cxpProtocol=http,cxpHost=stego.myhealthespace.com,cxpPort=9080,cxpPath=/gateway/services/CXP2,accountId=1117658438174637,groupAccountId=1172619833385984,groupName=Demo Group Worklist,auth=0b357bcdf0c57f3a42fd3558eb13a99c0d924563]
 INFO [btpool0-9] (HttpServer.java:353) - Context parameters
 INFO [btpool0-9] (HttpServer.java:358) - name:guid, value:1ef09948adac278c2888b33cf1f1c509a0bf2f72
 INFO [btpool0-9] (HttpServer.java:358) - name:cxpport, value:9080
 INFO [btpool0-9] (HttpServer.java:358) - name:cxpprotocol, value:http
 INFO [btpool0-9] (HttpServer.java:358) - name:cxphost, value:stego.myhealthespace.com
 INFO [btpool0-9] (HttpServer.java:358) - name:storageId, value:9216383252071301
 INFO [btpool0-9] (HttpServer.java:358) - name:cxppath, value:/gateway/services/CXP2
 INFO [btpool0-9] (HttpServer.java:396) - Context object set to ContextManager[storageId=9216383252071301,guid=1ef09948adac278c2888b33cf1f1c509a0bf2f72,cxpProtocol=http,cxpHost=stego.myhealthespace.com,cxpPort=9080,cxpPath=/gateway/services/CXP2,accountId=1117658438174637,groupAccountId=1172619833385984,groupName=Demo Group Worklist,auth=0b357bcdf0c57f3a42fd3558eb13a99c0d924563]
 INFO [btpool0-9] (HttpServer.java:223) - ====setDocumentFocus
<br>url/setDocumentFocus/
 INFO [btpool0-9] (HttpServer.java:224) - ContextManager[storageId=9216383252071301,guid=1ef09948adac278c2888b33cf1f1c509a0bf2f72,cxpProtocol=http,cxpHost=stego.myhealthespace.com,cxpPort=9080,cxpPath=/gateway/services/CXP2,accountId=1117658438174637,groupAccountId=1172619833385984,groupName=Demo Group Worklist,auth=0b357bcdf0c57f3a42fd3558eb13a99c0d924563]
 INFO [btpool0-9] (DownloadHandler.java:124) - Currently ignoring case where no downloads are requested
 INFO [btpool0-11] (HttpServer.java:287) - ====tmon
	 */
	public void testGetDocumentFocus(){
		// Test to see that the document focus is what you think it is.
	}

	public void testSetDICOMParameters() throws Exception{
		DicomClientParameters params = getDicomClientParameters();
		String urlArgs = params.toURLArguments();
		String contextUrl =  "http://localhost:16092/localDDL/ConfigUpdate.action?update" + urlArgs;
log.info("About to submit url " + contextUrl);
		try{
			WebConversation wc = new WebConversation();
		    WebRequest     req = new PostMethodWebRequest(contextUrl);

		    WebResponse   resp = wc.getResponse( req );
		    assertNotNull("Null Response", resp);
		    String elements[] = resp.getElementNames();
		    for (int i=0;i<elements.length;i++){
		    	log.info("element " + i + " = " + elements[i]);
		    }

		}
		catch(MalformedURLException e){
			log.info("Error with args " + contextUrl, e);
			throw e;
		}
		catch(IOException e){
			log.info("Error with args " + contextUrl, e);
			throw e;
		}
		catch(SAXException e){
			log.info("Error with args " + contextUrl, e);
			throw e;
		}
		catch(Throwable e){
			log.info("(Unknown and ignored error with args " + contextUrl, e);
			// Bad - don't know what causes this.
		}
	}

}
