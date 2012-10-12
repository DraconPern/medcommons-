package net.medcommons.importexport.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.crypto.io.FileGuid;
import net.medcommons.modules.cxp.client.CXPClient;
import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.modules.utils.PerformanceMeasurement;
import net.medcommons.modules.utils.SpecialFileFilter;
import net.medcommons.modules.utils.TestDataConstants;

import org.apache.log4j.Logger;
import org.cxp2.Document;
import org.cxp2.Parameter;
import org.cxp2.PutRequest;
import org.cxp2.PutResponse;
import org.cxp2.RegistryParameters;
/**
 * Test of CXP PUT
 * 
 * Currently only supports MTOM  transfers.
 * 
 * @author mesozoic
 *
 */
public class PUTTest extends CXPBase {

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("PUTTest");
	
	private static String auth = "9e9596cc8117d36327370a219e5bb7692ca23137";
	
	public void setUp() throws Exception{
		super.setUp();
		//CXPClient.setHttpProxy("localhost", "16093");
	}
	long totalBytes = 0;
	
	private void resetByteCount(){
		totalBytes = 0;
	}
	private void incrementBytecount(long byteCount){
		totalBytes += byteCount;
	}
	
	
	public void testPutSimpleDocumentMTOM_CCR() throws java.lang.Exception
	{
		
		resetByteCount();
		long startTime = System.currentTimeMillis();
		
		
		executePutSimpleDocument(DocumentCCR, CCR_MIME_TYPE, "Sample CCR", null);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT: testPutSimpleDocumentMTOM_CCR", (endTime - startTime), totalBytes));	
		
	}
	public void testPutSimpleDocumentMTOM_CCR_Pin() throws java.lang.Exception
	{
		
		resetByteCount();
		long startTime = System.currentTimeMillis();
	
		
		executePutSimpleDocument(DocumentCCR, CCR_MIME_TYPE, "Sample CCR", "54321");
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT: testPutSimpleDocumentMTOM_CCR_Pin", (endTime - startTime), totalBytes));	
		
	}
	public void testPutSimpleDocumentMTOM_JPEG() throws java.lang.Exception
	{
		
		resetByteCount();
		long startTime = System.currentTimeMillis();
		executePutSimpleDocument(DocumentJpeg, JPG_MIME_TYPE, "Sample JPEG", null);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:testPutSimpleDocumentMTOM_JPEG" , (endTime - startTime), totalBytes));	
		
	}
	public void testPut2SimpleDocuments() throws Exception{
		log.info("Putting two documents");
		resetByteCount();
		long startTime = System.currentTimeMillis();
	
		
		String storageId = properties.getProperty(AccountID1);
		PutRequest request = createComplexPut(storageId);
		String pin ="66666";
		addParameters(request,pin);
		Document doc1 = createSimpleDocument(DocumentCCR, CCR_MIME_TYPE, "Sample CCR");
		Document doc2 = createSimpleDocument(DocumentPDF, PDF_MIME_TYPE, "Sample PDF");
		request.getDocinfo().add(doc1);
		request.getDocinfo().add(doc2);
		CXPClient client = new CXPClient(endpoint);
		PutResponse resp = client.getService().put(request);
		displayResponseInfo(resp);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:testPut2SimpleDocuments" , (endTime - startTime), totalBytes));	
		
		
	}
	public void testPut3SimpleDocuments() throws Exception{
		log.info("Putting three documents");
		
		resetByteCount();
		long startTime = System.currentTimeMillis();
		
		String storageId = properties.getProperty(AccountID1);
		PutRequest request = createComplexPut(storageId);
		String pin = "12345";
		addParameters(request,pin);
		Document doc1 = createSimpleDocument(DocumentCCRWithPDFAndJPEG, CCR_MIME_TYPE, "Sample CCR");
		Document doc2 = createSimpleDocument(DocumentPDF, PDF_MIME_TYPE, "Sample PDF");
		Document doc3 = createSimpleDocument(DocumentJpeg, JPG_MIME_TYPE, "Sample JPEG");
		request.getDocinfo().add(doc1);
		request.getDocinfo().add(doc2);
		request.getDocinfo().add(doc3);
		
		CXPClient client = new CXPClient(endpoint);
		PutResponse resp = client.getService().put(request);
		displayResponseInfo(resp);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:testPut3SimpleDocuments" , (endTime - startTime), totalBytes));	
		
	}
	public void testPut2SimpleDocuments1CompoundDocument() throws Exception{
		log.info("Putting three simple documents, one compound document");
		
		resetByteCount();
		long startTime = System.currentTimeMillis();
		
		String storageId = properties.getProperty(AccountID1);
		PutRequest request = createComplexPut(storageId);
		String pin = "43432";
		addParameters(request,pin);
		Document doc1 = createSimpleDocument(DocumentCCR, CCR_MIME_TYPE, "Sample CCR");
		Document doc2 = createSimpleDocument(DocumentPDF, PDF_MIME_TYPE, "Sample PDF");
		List<Document> documents1 = createCompoundDocument(DICOMSeries1, DICOM_MIME_TYPE, "DICOM Series", false);
		Document doc3 = createSimpleDocument(DocumentJpeg, JPG_MIME_TYPE, "Sample JPEG");
		request.getDocinfo().add(doc1);
		request.getDocinfo().add(doc2);
		
		request.getRegistryParameters().add(CXPClient.generateSenderIdParameters(TestDataConstants.DOCTOR_ID, TestDataConstants.DOCTOR_AUTH));
		for(int i=0;i<documents1.size();i++){
			request.getDocinfo().add(documents1.get(i));
		}
		request.getDocinfo().add(doc3);
		
		CXPClient client = new CXPClient(endpoint);
		PutResponse resp = client.getService().put(request);
		displayResponseInfo(resp);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:testPut2SimpleDocuments1CompoundDocument", (endTime - startTime), totalBytes));	
		
	}
	public void testPut3SimpleDocuments1CompoundDocument2() throws Exception{
		log.info("Putting three simple documents, two compound document. CCR document has references to other four documents");
		
		resetByteCount();
		long startTime = System.currentTimeMillis();
		
		String storageId = properties.getProperty(AccountID1);
		PutRequest request = createComplexPut(storageId);
		String pin = "54321";
		addParameters(request,pin);
		Document doc1 = createSimpleDocument(DocumentCCRWithPDFAndJPEGAnd2DICOM, CCR_MIME_TYPE, "Sample CCR");
		Document doc2 = createSimpleDocument(DocumentPDF, PDF_MIME_TYPE, "Sample PDF");
		List<Document> documents3 = createCompoundDocument(DICOMSeries1, DICOM_MIME_TYPE, "DICOM Series 1", false);
		List<Document> documents4 = createCompoundDocument(DICOMSeries2, DICOM_MIME_TYPE, "DICOM Series 2", false);
		Document doc5 = createSimpleDocument(DocumentJpeg, JPG_MIME_TYPE, "Sample JPEG");
		request.getDocinfo().add(doc1);
		request.getDocinfo().add(doc2);
		for(int i=0;i<documents3.size();i++){
			request.getDocinfo().add(documents3.get(i));
		}
		for(int i=0;i<documents4.size();i++){
			request.getDocinfo().add(documents4.get(i));
		}
		request.getDocinfo().add(doc5);
		
		CXPClient client = new CXPClient(endpoint);
		PutResponse resp = client.getService().put(request);
		displayResponseInfo(resp);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:testPut3SimpleDocuments1CompoundDocument2" , (endTime - startTime), totalBytes));	
		
	}
	public void testPut2SimpleDocuments2CompoundDocumentWithPIN() throws Exception{
		log.info("Putting three simple documents, one compound document");
		
		resetByteCount();
		long startTime = System.currentTimeMillis();
		String pin = "12345";
		
		String storageId = properties.getProperty(AccountID1);
		PutRequest request = createComplexPut(storageId);
		addParameters(request,pin);
		
		
		Document doc1 = createSimpleDocument(DocumentCCRWithDICOM, CCR_MIME_TYPE, "Sample CCR");
		Document doc2 = createSimpleDocument(DocumentPDF, PDF_MIME_TYPE, "Sample PDF");
		List<Document> documents1 = createCompoundDocument(DICOMSeries1, DICOM_MIME_TYPE, "DICOM Series 1", false);
		List<Document> documents2 = createCompoundDocument(DICOMSeries2, DICOM_MIME_TYPE, "DICOM Series 2", false);
		Document doc3 = createSimpleDocument(DocumentJpeg, JPG_MIME_TYPE, "Sample JPEG");
		request.getDocinfo().add(doc1);
		request.getDocinfo().add(doc2);
		for(int i=0;i<documents1.size();i++){
			request.getDocinfo().add(documents1.get(i));
		}
		for(int i=0;i<documents2.size();i++){
			request.getDocinfo().add(documents2.get(i));
		}
		request.getDocinfo().add(doc3);
		
		CXPClient client = new CXPClient(endpoint);
		PutResponse resp = client.getService().put(request);
		displayResponseInfo(resp);
		testReturnedPin(resp, pin);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:testPut2SimpleDocuments2CompoundDocumentWithPIN" , (endTime - startTime), totalBytes));	
		
	}
	
	public void xtestPutCompoundDocumentSOAP() throws java.lang.Exception{
		
		resetByteCount();
		long startTime = System.currentTimeMillis();
	
		executePutCompoundDocument(true);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:xtestPutCompoundDocumentSOAP", (endTime - startTime), totalBytes));	
	}
	public void testPutCompoundDocumentMTOM() throws java.lang.Exception{
	
		resetByteCount();
		long startTime = System.currentTimeMillis();
	
		executePutCompoundDocument(true);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:testPutCompoundDocumentMTOM", (endTime - startTime), totalBytes));	
	}
	public void testPutCompoundDocumentMTOMNoFilenames() throws java.lang.Exception{
		
		resetByteCount();
		
		long startTime = System.currentTimeMillis();
		executePutCompoundDocument(false);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:testPutCompoundDocumentMTOMNoFilenames" , (endTime - startTime), totalBytes));	
	}
	public void testPutCompoundDocumentMTOMNoFilenamesPIN() throws java.lang.Exception{
	
		resetByteCount();
		
		long startTime = System.currentTimeMillis();
		executePutCompoundDocument(false);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:testPutCompoundDocumentMTOMNoFilenamesPIN" , (endTime - startTime), totalBytes));	
	}
	
	public void xtestPut2SimpleDocumentsCreateStorageId() throws Exception{
		log.info("Putting two documents into storage id generated by the server");
		resetByteCount();
		long startTime = System.currentTimeMillis();
		
		String storageId = "-1";
		PutRequest request = createComplexPut(storageId);
		String senderAccountId=properties.getProperty(SenderAccountId);
		
		RegistryParameters sponsoredAccount =  CXPClient.generateSenderIdParameters(senderAccountId);
		request.getRegistryParameters().add(sponsoredAccount);
		Document doc1 = createSimpleDocument(DocumentCCR, CCR_MIME_TYPE, "Sample CCR");
		Document doc2 = createSimpleDocument(DocumentPDF, PDF_MIME_TYPE, "Sample PDF");
		request.getDocinfo().add(doc1);
		request.getDocinfo().add(doc2);
		
		CXPClient client = new CXPClient(endpoint);
		PutResponse resp = client.getService().put(request);
		displayResponseInfo(resp);
		List<RegistryParameters> registryParameters = resp.getRegistryParameters();
		String returnedStorageId = getMedCommonsParameter(registryParameters, CXPConstants.STORAGE_ID);
		log.info("Returned storage id is " + returnedStorageId);
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("PUT:testPut2SimpleDocumentsCreateStorageId" , (endTime - startTime), totalBytes));	
		
		
	}

public void executePutSimpleDocument(String documentReference, String mimeType, String name, String pin) throws java.lang.Exception {
		
		
		String imageName = properties.getProperty(documentReference);
		

		File imageFile = initFile(imageName);
		if (!imageFile.exists())
			throw new FileNotFoundException(imageFile.getAbsolutePath());
		
		incrementBytecount(imageFile.length());
		//long fileLength = imageFile.length();

		// Calculate the SHA-1 hash of the contents of imageFile.
		// This value should match the returned value from the response.
		String guid = FileGuid.calculateFileGuid(imageFile);
		String accountId = properties.getProperty(AccountID1);

		PutRequest request = new PutRequest();

		request.setStorageId(accountId);
		Document docinfo = new Document();
		docinfo.setContentType(mimeType);
		docinfo.setDescription(name);
		//docinfo.setGuid(guid);
		//docinfo.setDocumentName(imageName);

		FileDataSource dataSource = new FileDataSource(imageFile);

		DataHandler dh = new DataHandler(dataSource);

		docinfo.setData(dh);

		/*
		 * XMLStreamReader reader = docinfo.getPullParser(null); OMElement omElt =
		 * new StAXOMBuilder(reader).getDocumentElement();
		 * 
		 * OMElement parameters[] = new OMElement[1]; parameters[0] = omElt;
		 */

		request.getDocinfo().add(docinfo);
		addParameters(request,pin);
		
		
		CXPClient client = new CXPClient(endpoint);
		PutResponse resp = client.getService().put(request);
		System.out.println("Response: " + resp.getStatus() + ", "
				+ resp.getReason());
		List<Document> responseDocs = resp.getDocinfo();
		assertNotNull("Empty list of documents returned from PUT", responseDocs);
		Iterator<Document> iter = responseDocs.iterator();
		while(iter.hasNext()){
			Document responseDoc = iter.next();
			assertEquals("GUID Mismatch", guid, responseDoc.getGuid());
			log.info(responseToString(responseDoc));
		}
		
		assertTrue("CXP Response is not OK:" + resp.getStatus(), statusOK(resp
				.getStatus()));
		List <RegistryParameters>returnedParameters= resp.getRegistryParameters();
		displayRegistryParameters(returnedParameters);
		testReturnedPin(resp, pin);
		if (pin != null){
			String returnedRegistryParameter = getMedCommonsParameter(returnedParameters, CXPConstants.REGISTRY_SECRET);
			assertEquals("Registry returned parameter", pin, returnedRegistryParameter);
		}

	}
	private String responseToString(Document responseDoc){
		StringBuffer buff = new StringBuffer("Response Document[");
		buff.append("guid=");
		buff.append(responseDoc.getGuid());
		buff.append(", documentName=");
		buff.append(responseDoc.getDocumentName());
		buff.append(", contentType=");
		buff.append(responseDoc.getContentType());
		buff.append(", sha1=");
		buff.append(responseDoc.getSha1());
		buff.append(", parentName=");
		buff.append(responseDoc.getParentName());
		buff.append("]");
		return(buff.toString());
	}

	private void addParameters(PutRequest request, String pin){
	    
        List<Parameter> params = new ArrayList<Parameter>();
        Parameter p = new Parameter();
        p.setName(CXPConstants.REGISTRY_SECRET);
        p.setValue(pin);
        params.add(p);
        p = new Parameter();
        p.setName(CXPConstants.AUTHORIZATION_TOKEN);
        p.setValue(auth);
        params.add(p);
        RegistryParameters registryParameters = createParameterBlock(params);
        
        request.getRegistryParameters().add(registryParameters);
	}
	
	public void executePutCompoundDocument(boolean useDocumentNames) throws java.lang.Exception {

		String seriesName = properties.getProperty(DICOMSeries1);
		String storageId = properties.getProperty(AccountID1);
		String pin = "12345";
		
		// In the future we should calculate the GUID from the files.
		// Here we just get a good 'known' value and test that the server 
		// returns the right answer below.
		// To calculate this we need to:
		// a) Calculate the SHA-1 hashes of all of files in compound document.
		// b) Sort these values by the server filename of the objects.
		// c) Calculate the SHA-1 hashes of this SHA-1 hash.
		String knownGuid = properties.getProperty(DICOMSeries1 + suffixGUID);

		File seriesDir = FileUtils.getTestResourceFile(seriesName);
		// Want to filter out files like .DS_Store - any file that starts with '.'.
		SpecialFileFilter filter = new SpecialFileFilter();
		filter.setFilterType(".");

		File files[] = seriesDir.listFiles(filter);
		DocumentInfo allDocs[] = new DocumentInfo[files.length];
		

		//DocumentDescriptor transactionDescr = repository.initializeCompoundDocument(descr);
		PutRequest request = new PutRequest();
		request.setStorageId(storageId);
		String contentType = DICOM_MIME_TYPE;
		addParameters(request,pin);
		

		List<String> sha1List = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			
			DocumentInfo aDoc = new DocumentInfo();
			allDocs[i] = aDoc;
			aDoc.f = files[i];
			if (!aDoc.f.exists())
				throw new FileNotFoundException(aDoc.f.getAbsolutePath());
			aDoc.sha1 = FileGuid.calculateFileGuid(aDoc.f);
			sha1List.add(aDoc.sha1);
			aDoc.size = aDoc.f.length();
			incrementBytecount(aDoc.size);
			aDoc.contentType = contentType;
			
			
			
			Document docinfo = new Document();
			
			FileDataSource dataSource = new FileDataSource(aDoc.f);

			DataHandler dh = new DataHandler(dataSource);
			docinfo.setParentName("aSeries");
			docinfo.setData(dh);
			docinfo.setContentType(contentType);
			if (useDocumentNames)
				docinfo.setDocumentName(aDoc.f.getName());
			//log.info("Added document for " + aDoc.f.getAbsolutePath());
			request.getDocinfo().add(docinfo);
			//if (i>2) break;

		}
		Collections.sort(sha1List);
		String sortedSha1s[] = sha1List.toArray(new String[sha1List.size()]);
		SHA1 sha1 = new SHA1();
		sha1.initializeHashStreamCalculation();
		String clientCalculatedGuid = sha1.calculateStringNameHash(sortedSha1s);
		CXPClient client = new CXPClient(endpoint);
		
		PutResponse resp = client.getService().put(request);
		
		List<Document> responseDocs = resp.getDocinfo();
		assertNotNull("Empty list of documents returned from PUT", responseDocs);
		Iterator<Document> iter = responseDocs.iterator();
		
		int j=0; 
		while(iter.hasNext()){
			Document responseDoc = iter.next();
			String serverGuid = responseDoc.getGuid();
			String clientGuid = clientCalculatedGuid;// allDocs[i].sha1;
			log.debug(responseToString(responseDoc));
			log.info("Need to put back assert");
			//assertEquals("Mismatch between client generated GUID and server guid", serverGuid, clientGuid);
			
			j++;
		}
		assertTrue("CXP Response is not OK:" + resp.getStatus(), statusOK(resp
				.getStatus()));
		List <RegistryParameters>returnedParameters= resp.getRegistryParameters();
		displayRegistryParameters(returnedParameters);
		

	}
	

	public void testReturnedPin(PutResponse response, String pin){
		if (pin != null){
			List <RegistryParameters>registryParameters= response.getRegistryParameters();
			String returnedRegistryParameter = getMedCommonsParameter(registryParameters, CXPConstants.REGISTRY_SECRET);
			assertEquals("Registry parameter", pin, returnedRegistryParameter);
		}
	}
	public PutRequest createComplexPut(String storageId){
		PutRequest request = new PutRequest();
		request.setStorageId(storageId);
		return(request);
		
	}
	public Document createSimpleDocument(String documentReference, String mimeType, String name) 
			throws IOException, NoSuchAlgorithmException{
		Document document = new Document();
		String filename = properties.getProperty(documentReference);
		

		File file = initFile(filename);
		if (!file.exists())
			throw new FileNotFoundException(file.getAbsolutePath());
	
		// This value should match the returned value from the response.
		String guid = FileGuid.calculateFileGuid(file);
		

	
		
		document.setContentType(mimeType);
		document.setDescription(name);
		document.setGuid(guid); // Not necessary - but useful for housekeeping
		

		FileDataSource dataSource = new FileDataSource(file);

		DataHandler dh = new DataHandler(dataSource);

		document.setData(dh);
		return(document);

	}
	public List<Document> createCompoundDocument(String documentReference, String mimeType, String name, boolean useDocumentNames) 
	throws IOException, NoSuchAlgorithmException{
		List<Document> documents = new ArrayList<Document>();
		String documentLocation = properties.getProperty(documentReference);
		//Document document = new Document();
		//String filename = properties.getProperty(documentReference);
		File seriesDir = FileUtils.getTestResourceFile(documentLocation);
		
		// Want to filter out files like .DS_Store - any file that starts with '.'.
		SpecialFileFilter filter = new SpecialFileFilter();
		filter.setFilterType(".");

		File files[] = seriesDir.listFiles(filter);
		DocumentInfo allDocs[] = new DocumentInfo[files.length];
		

		String contentType = mimeType;

		List<String> sha1List = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			
			DocumentInfo aDoc = new DocumentInfo();
			allDocs[i] = aDoc;
			aDoc.f = files[i];
			if (!aDoc.f.exists())
				throw new FileNotFoundException(aDoc.f.getAbsolutePath());
			aDoc.sha1 = FileGuid.calculateFileGuid(aDoc.f);
			sha1List.add(aDoc.sha1);
			aDoc.size = aDoc.f.length();
			aDoc.contentType = contentType;
			
			
			
			Document docinfo = new Document();
			
			FileDataSource dataSource = new FileDataSource(aDoc.f);

			DataHandler dh = new DataHandler(dataSource);
			docinfo.setParentName(documentReference);
			docinfo.setData(dh);
			docinfo.setContentType(contentType);
			if (useDocumentNames)
				docinfo.setDocumentName(aDoc.f.getName());
			//log.info("Added document for " + aDoc.f.getAbsolutePath());
			documents.add(docinfo);
			

		}


		return(documents);

}


	private void displayResponseInfo(PutResponse resp){
		List<Document> responseDocs = resp.getDocinfo();
		assertNotNull("Empty list of documents returned from PUT", responseDocs);
		Iterator<Document> iter = responseDocs.iterator();
		log.info("Number of documents successfully stored:" + responseDocs.size());
		while(iter.hasNext()){
			Document responseDoc = iter.next();
			
			log.info(responseToString(responseDoc));
			
			String parentName = responseDoc.getParentName();
			String guid = responseDoc.getGuid();
			String knownGuid = properties.getProperty(parentName+ suffixGUID);
			if (parentName != null){
				//assertEquals("Guid mismatch " + parentName + " :" + guid + " does not match known value " + knownGuid,
				//		guid, knownGuid);
				log.error("Guid mismatch " + parentName + " :" + guid + " does not match known value " + knownGuid + 
                        guid + " " + knownGuid);
				
			}
		}
		List registryParameters= resp.getRegistryParameters();
		for (int i=0;i<registryParameters.size(); i++){
			RegistryParameters r = (RegistryParameters) registryParameters.get(i);
			log.info("Registry Parameters:" + r.getRegistryId() + "," + r.getRegistryName());
			List<Parameter> params = r.getParameters();
			if(params.size() >0){
				log.info(" Parameters:");
				for (int k=0;k<params.size();k++){
					Parameter p = params.get(k);
					log.info("  Parameter name=" + p.getName() + ", value=" + p.getValue());
				}
			}
			else{
				log.info("   Parameter list empty");
			}
			
		}
		assertTrue("CXP Response is not OK:" + resp.getStatus(), statusOK(resp
				.getStatus()));
		
		log.info("Response: " + resp.getStatus() + ", "
				+ resp.getReason());
	}
	

	/*

	This class is used as a placeholder.
	The SHA1 hash can be put in here on input.
	This can be tested against the output in the response.
	 */
	private class DocumentInfo {
		File f = null;

	

		String sha1 = null;

		long size = -1;

		String contentType = null;
	}

}
