package net.medcommons.modules.transfer.test;

import java.io.File;

import net.medcommons.modules.cxp.client.test.CXPBase;
import net.medcommons.modules.transfer.DownloadFileAgent;

import org.apache.log4j.Logger;
import org.cxp2.GetResponse;

/**
 * Download test - extends cxp module's root junit. Uses
 * same JUNIT properties file for arguments to calls.
 *
 * @author mesozoic
 *
 */
public class DownloadTest extends CXPBase {


	protected File resources;

	private static Logger log = Logger.getLogger("DownloadTest");
	File scratch;
	public void setUp() throws Exception{
		super.setUp();

	}
	public void testDownloadDICOM() throws Exception{
		log.info("Downloading DICOM");
		String storageId = properties.getProperty(AccountID1);
		String senderAccountId = null;
		String guid1 = properties.getProperty(DICOMSeries1 + suffixGUID);
		String guid2 = properties.getProperty(DICOMSeries2 + suffixGUID);
		String guid3 = properties.getProperty(DocumentJpeg + suffixGUID);
		String guids[] = new String[3];
		guids[0] = guid1;
		guids[1] = guid2;
		guids[2] = guid3;
		scratch = new File(resources, storageId + "_DICOM");
		scratch.mkdir();

		DownloadFileAgent downloadFileAgent = new DownloadFileAgent(getEndpoint(), storageId,senderAccountId,guids,scratch);
		GetResponse resp = downloadFileAgent.download();
		assertTrue("Status Not OK", statusOK(resp.getStatus()));

	}
	public void testDownloadJPEG() throws Exception{
		log.info("Downloading JPEG");
		String senderAccountId = null;
		String storageId = properties.getProperty(AccountID1);
		String guid1 = properties.getProperty(DocumentJpeg + suffixGUID);
		scratch = new File(resources, storageId + "_JPEG");
		scratch.mkdir();

		String guids[] = new String[1];
		guids[0] = guid1;

		DownloadFileAgent downloadFileAgent = new DownloadFileAgent(getEndpoint(), storageId, senderAccountId, guids,scratch);
		GetResponse resp = downloadFileAgent.download();
		assertTrue("Status Not OK", statusOK(resp.getStatus()));

	}
}
