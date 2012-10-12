package net.medcommons.application.dicomclient.test;

import java.io.File;
import java.io.FileNotFoundException;

import net.medcommons.application.dicomclient.utils.DicomFileChooser;

import org.apache.log4j.Logger;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class UploadDICOMTest extends BaseTest {
	private static Logger log = Logger.getLogger("UploadDICOMTest");
	public void setUp() throws Exception{
		super.setUp();
		//DICOMClient.initializeFiles(true);
	}

	public void testUploadPET1() throws Exception{
		uploadDICOMData("images.PET");

		String contextUrl = "http://localhost:16092/localDDL/Status.action?getDicomSCP";
			WebConversation wc = new WebConversation();
	    WebRequest     req = new GetMethodWebRequest(contextUrl);
	    WebResponse   resp = wc.getResponse( req );
	    log.info(resp.getText());
	    log.info(resp.getScriptableObject());

	}
	/**
	 * Tests
	 * Upload folder w no dicom
	 * Upload folder with duplicates
	 * Upload folder with bad dicom
	 */

	private void uploadDICOMData(String propertyName) throws FileNotFoundException{
		DicomFileChooser d = new DicomFileChooser();
		String fileOrDir = getProperties().getProperty(propertyName);
		File f = new File(fileOrDir);
		if (!f.exists()){
			throw new FileNotFoundException(f.getAbsolutePath());
		}
		// Upload the data
		d.importDicomDirectory(f, false);
	
	}

}
