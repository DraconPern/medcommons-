package net.medcommons.application.dicomclient.test;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * This JUnit test returns success once the DDL is running.
 * This is a bit of a hack - it's basically in a wait loop
 * until DDL has loaded and the status.html file can be
 * read. At this point the DDL is ready to be tested.
 * @author mesozoic
 *
 */
public class WaitStartupTest extends BaseTest{
	private static Logger log = Logger.getLogger("WaitStartupTest");
	public void setUp() throws Exception{
		super.setUp();
	}
	public void testGetStatus() throws Exception{
		String contextUrl =  "http://localhost:16092/localDDL/status.html";
		boolean success = false;
		try{Thread.sleep(2000);} catch(Exception e){} // Sleep a little before starting test
		long startTime = System.currentTimeMillis();
		long maxEndTime = startTime + (1000 * 15); // Must start in 15 seconds.
		while(!success){
			if (System.currentTimeMillis() > maxEndTime){
				throw new RuntimeException("Timeout - DDL should have started; not responding after " +
						(maxEndTime - startTime) + " msec");
			}
			try{Thread.sleep(1000);} catch(Exception e){}
			try{
				WebConversation wc = new WebConversation();
			    WebRequest     req = new GetMethodWebRequest(contextUrl);
			    WebResponse   resp = wc.getResponse( req );
			    assertNotNull("Null Response", resp);
			    String elements[] = resp.getElementNames();
			    for (int i=0;i<elements.length;i++){
			    	log.info("element " + i + " = " + elements[i]);
			    }
			    success = true;
			    log.info("Connected to DDL");


			}
			catch(MalformedURLException e){
				log.info("Error with args " + contextUrl, e);
				throw e;
			}
			catch(IOException e){
				log.info("Error with args " + contextUrl, e);
				//throw e;
			}
			catch(SAXException e){
				log.info("Error with args " + contextUrl, e);
				throw e;
			}
			catch(Throwable e){
				//log.error("Unexpected error", e);
				log.error("Unexpected error:" + e.getLocalizedMessage(), e);
				success = true;
			}
		}
	}
}
