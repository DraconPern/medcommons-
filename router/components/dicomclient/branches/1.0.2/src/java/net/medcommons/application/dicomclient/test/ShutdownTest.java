package net.medcommons.application.dicomclient.test;


import org.apache.log4j.Logger;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * This JUnit test Shuts down the DDL
 * Don't test for output -  just assume that it works.
 *
 * @author mesozoic
 *
 */
public class ShutdownTest extends BaseTest{
	private static Logger log = Logger.getLogger("ShutdownTest");
	public void setUp() throws Exception{
		super.setUp();
	}
	public void testShutdown() throws Exception{
		String contextUrl =  "http://localhost:16092/shutdown";



			try{
				WebConversation wc = new WebConversation();
			    WebRequest     req = new GetMethodWebRequest(contextUrl);
			    WebResponse   resp = wc.getResponse( req );


			}

			catch(Error e){
				log.error("Unexpected error", e);
			;
			}
		}
	}

