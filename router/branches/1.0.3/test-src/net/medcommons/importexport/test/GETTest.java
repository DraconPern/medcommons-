package net.medcommons.importexport.test;

import java.io.File;

import net.medcommons.modules.cxp.client.CXPClient;
import net.medcommons.modules.cxp.CXPConstants;

import org.apache.log4j.Logger;
import org.cxp2.Document;
import org.cxp2.GetRequest;
import org.cxp2.GetResponse;


public class GETTest extends CXPBase {
	
	File GETTESTDir = null;
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("GETTest");
	
	public void setUp() throws Exception{
		super.setUp();
		
		GETTESTDir = new File(scratch, "JUNIT_GETTEST_OUTPUT");
		if (!GETTESTDir.exists()){
			boolean success = GETTESTDir.mkdir();
			if (!success){
				throw new RuntimeException("Can't create scratch directory :" + GETTESTDir.getAbsolutePath());
			}
		}
		
		
	}
	private File outputDirectory(String resultsDirectoryName){
		File resultsDirectory = new File(GETTESTDir, resultsDirectoryName);
		
		boolean success = resultsDirectory.exists();
		if (!success)
			success = resultsDirectory.mkdir();
		
		if (!success)
			throw new RuntimeException("Unable to create results directory:" + resultsDirectory.getAbsolutePath());
		return(resultsDirectory);
	}
	public void testSimpleGet() throws java.lang.Exception {
		simpleGet(DocumentJpeg,200,true);
	}
	
	public void testSimpleGetFailure() throws java.lang.Exception{
		simpleGet(DocumentMissing,404,true);
	}
	
	public void testSimpleGetNoData() throws java.lang.Exception {
		simpleGet(DocumentJpeg,200,false);
	}
	
	public void testSimpleGetFailureNoData() throws java.lang.Exception{
		simpleGet(DocumentMissing,404,false);
	}
	public void testCompoundGet() throws java.lang.Exception {
		boolean retrieveData = true;
		compoundGet(retrieveData);
	}
	public void testCompoundGetNoData() throws java.lang.Exception {
		boolean retrieveData = false;
		compoundGet(retrieveData);
	}
	public void testCompoundMultipleGet() throws java.lang.Exception{
		compoundGetMultiple(true); 
	}
	public void xtestCompoundMultipleGetTest() throws java.lang.Exception{
		compoundGetMultipleTemp(true); 
	}
	public void simpleGet(String docName, int expectedStatus, boolean retrieveData) throws java.lang.Exception {

		long startTime = System.currentTimeMillis();
		String guid = properties.getProperty(docName + suffixGUID);
		String accountId = properties.getProperty(AccountID1);

		GetRequest request = new GetRequest();
		request.setStorageId(accountId);

		Document docinfo = new Document();

		docinfo.setDescription("Unknown");
		docinfo.setGuid(guid);
		docinfo.setContentType("Unknown");
		

		request.getDocinfo().add(docinfo);

		if (retrieveData == false){
			assignMedCommonsParameter(request, CXPConstants.RETRIEVE_DATA,CXPConstants.FALSE);
		}
		log.info("About to make simple GET request with document name "
				+ guid);
		CXPClient client = new CXPClient(getEndpoint());
		GetResponse resp = client.getService().get(request);
	
		if (expectedStatus==200)
			assertTrue("Status OK", statusOK(resp.getStatus()));
		else if (expectedStatus==404)
			assertTrue("Status Missing", statusMissing(resp.getStatus()));
		else
			assertTrue("Status OK", statusOK(resp.getStatus()));
		File resultsDirectory = outputDirectory("simpleGet_" + Boolean.toString(retrieveData));
		client.processGetResponse(resp, startTime, resultsDirectory);
		//processGetResponse(resp, startTime, "simpleGet_" + docName + "_" + Boolean.toString(retrieveData));

	}
	
	
	public void compoundGet(boolean retrieveData) throws java.lang.Exception {

		
		long startTime = System.currentTimeMillis();
		String guid = properties.getProperty(DICOMSeries1 + suffixGUID);
		String accountId = properties.getProperty(AccountID1);

		GetRequest request = new GetRequest();
		request.setStorageId(accountId);

		Document docinfo = new Document();

		docinfo.setDescription("Unknown");
		docinfo.setGuid(guid);
		docinfo.setContentType("Unknown");
		

		request.getDocinfo().add(docinfo);

		log.info("About to make compound GET request with document name "
				+ guid);
		if (retrieveData == false){
			assignMedCommonsParameter(request, CXPConstants.RETRIEVE_DATA,CXPConstants.FALSE);
		}
		CXPClient client = new CXPClient(getEndpoint());
		GetResponse resp = client.getService().get(request);

		assertTrue("Status Not OK", statusOK(resp.getStatus()));

		File resultsDirectory = outputDirectory("compoundGet_" +  Boolean.toString(retrieveData));
		client.processGetResponse(resp, startTime, resultsDirectory);
		//processGetResponse(resp,startTime, "compoundGet_" + guid + "_" + Boolean.toString(retrieveData));

	}
	
	public void compoundGetMultipleTemp(boolean retrieveData) throws java.lang.Exception {

		long startTime = System.currentTimeMillis();

		String guid1 = "0da345a6b44c4c71dedbe24c94bb7325b6f8d4bb";
		
		String accountId = "1013062431111407";

		GetRequest request = new GetRequest();
		request.setStorageId(accountId);

		Document docinfo1 = new Document();
		docinfo1.setDescription("Unknown");
		docinfo1.setGuid(guid1);
		docinfo1.setContentType("Unknown");
		
		
		

		request.getDocinfo().add(docinfo1);
		

		log.info("About to make compound GET request with (specific) guids "
				+ guid1 );
		if (retrieveData == false){
			assignMedCommonsParameter(request, CXPConstants.RETRIEVE_DATA,CXPConstants.FALSE);
		}
		CXPClient client = new CXPClient(getEndpoint());
		GetResponse resp = client.getService().get(request);
		

		assertTrue("Status Not OK", statusOK(resp.getStatus()));
		File resultsDirectory = outputDirectory("compoundGetMultipleTemp_" + Boolean.toString(retrieveData));
		client.processGetResponse(resp, startTime, resultsDirectory);

		//processGetResponse(resp,startTime, "compoundGetMultiple_" + Boolean.toString(retrieveData));

	}
public void compoundGetMultiple(boolean retrieveData) throws java.lang.Exception {

		long startTime = System.currentTimeMillis();

		String guid1 = properties.getProperty(DICOMSeries1 + suffixGUID);
		String guid2 = properties.getProperty(DICOMSeries2 + suffixGUID);
		String guid3 = properties.getProperty(DocumentJpeg + suffixGUID);
		String accountId = properties.getProperty(AccountID1);

		GetRequest request = new GetRequest();
		request.setStorageId(accountId);

		Document docinfo1 = new Document();
		docinfo1.setDescription("Unknown");
		docinfo1.setGuid(guid1);
		docinfo1.setContentType("Unknown");
		
		Document docinfo2 = new Document();
		docinfo2.setDescription("Unknown");
		docinfo2.setGuid(guid2);
		docinfo2.setContentType("Unknown");
		
		Document docinfo3 = new Document();
		docinfo3.setDescription("Unknown");
		docinfo3.setGuid(guid3);
		docinfo3.setContentType("Unknown");
		

		request.getDocinfo().add(docinfo1);
		request.getDocinfo().add(docinfo2);
		request.getDocinfo().add(docinfo3);

		log.info("About to make compound GET request with guids "
				+ guid1 + "," + guid2 + ", " + guid3);
		if (retrieveData == false){
			assignMedCommonsParameter(request, CXPConstants.RETRIEVE_DATA,CXPConstants.FALSE);
		}
		CXPClient client = new CXPClient(getEndpoint());
		GetResponse resp = client.getService().get(request);
		

		assertTrue("Status Not OK", statusOK(resp.getStatus()));
		File resultsDirectory = outputDirectory("compoundGetMultiple_" + Boolean.toString(retrieveData));
		client.processGetResponse(resp, startTime, resultsDirectory);

		//processGetResponse(resp,startTime, "compoundGetMultiple_" + Boolean.toString(retrieveData));

	}

}
