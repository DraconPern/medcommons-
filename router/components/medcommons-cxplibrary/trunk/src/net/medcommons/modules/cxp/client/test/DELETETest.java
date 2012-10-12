package net.medcommons.modules.cxp.client.test;

import net.medcommons.modules.cxp.client.CXPClient;

import org.apache.log4j.Logger;
import org.cxp2.DeleteRequest;
import org.cxp2.DeleteResponse;
import org.cxp2.Document;



public class DELETETest extends CXPBase{

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("DELETETest");
	
	public void setUp() throws Exception{
		super.setUp();
		
	}
	
	public void testDeleteSingleDocument() throws java.lang.Exception {
		
		
		String guid = properties.getProperty(DocumentJpeg + suffixGUID);
		String storageId = properties.getProperty(AccountID1);
		DeleteRequest request = new DeleteRequest();
		
		
		
		
		request.setStorageId(storageId);
		
		Document docinfo = new Document();
		
		
		docinfo.setGuid(guid);
		//docinfo.setContentType("JUNK");
		//docinfo.setDescription("Blah blah blah");

		
		request.getDocinfo().add(docinfo);
		
		log.info("Requesting deletion of storageid " + storageId +", guid=" + guid);
		CXPClient client = new CXPClient(endpoint);
		
		DeleteResponse resp = client.getService().delete(request);
		
		processDeleteResponse(resp);

	}
public void testDeleteCompoundDocument() throws java.lang.Exception {
		
		
		String guid = properties.getProperty(DICOMSeries1 + suffixGUID);
		String storageId = properties.getProperty(AccountID1);
		DeleteRequest request = new DeleteRequest();
		
		
		
		
		request.setStorageId(storageId);
		
		Document docinfo = new Document();
		
		
		docinfo.setGuid(guid);
		//docinfo.setContentType("JUNK");
		//docinfo.setDescription("Blah blah blah");

		
		request.getDocinfo().add(docinfo);
		
		log.info("Requesting deletion of storageid " + storageId +", guid=" + guid);
		
		CXPClient client = new CXPClient(endpoint);
		
		DeleteResponse resp = client.getService().delete(request);
		
		processDeleteResponse(resp);

	}
	


	private void processDeleteResponse(DeleteResponse resp){
		
		log.info("Response: " + resp.getStatus() + ", "
				+ resp.getReason());
		
		boolean success = statusOK(resp.getStatus());
		assertTrue("Return status not successful " +
				resp.getStatus() + ", "  + resp.getReason(), 
				success);
	
	}

	

}
