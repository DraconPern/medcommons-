package net.medcommons.emcbridge;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.ExtractFileMetadata;
import net.medcommons.client.utils.CCRDocumentUtils;
import net.medcommons.client.utils.CCRGenerator;
import net.medcommons.emcbridge.data.DicomWrapper;
import net.medcommons.emcbridge.data.DocumentWrapper;
import net.medcommons.emcbridge.data.SeriesObject;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.FileUtils;

import org.apache.log4j.Logger;

import astmOrgCCR.ContinuityOfCareRecordDocument;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.emc.solution.common.data.BasicData;
import com.emc.solution.common.data.IBasicData;
import com.emc.solution.common.data.ISessionData;
import com.emc.solution.common.data.ObjectData;
import com.emc.solution.common.util.DfcCommon;
import com.emc.solution.common.util.IResourceManager;
import com.emc.solution.common.util.ResourceManager;
import com.emc.solution.osa.client.ClientManager;
import com.emc.solution.osa.client.dao.MasterClientData;
import com.emc.solution.osa.common.PatientDataHelper;
import com.emc.solution.osa.common.ReportHelper;
import com.emc.solution.osa.constants.OsaConstants;

public class DfcClient {
	 public static final String DICOM_DATE_FORMAT =  "yyyyMMdd";
	 // Note that dates in DICOM can be in (at least) two different formats. Welcome to DICOM  :-)
	 public static final String DICOM_TIME_FORMAT = "kkmmss.SSSSSS";
	 public static final String DICOM_TIME_FORMAT_SIMPLE = "kkmmss";
	 
	IBasicData basicData = new BasicData();
	ISessionData sessionData = null;
	IDfSession sess = null;
	DfcCommon common = null;
	IResourceManager resource = null;
	protected ClientManager clientManager = null;
	PatientDataHelper osaCommons = null;
	
	 File transactionDirectory1 = null;
	 File transactionDirectory2 = null;
	 File dataDirectory = null;
	 DicomMetadata dicomMetadata = null; 
	 Map<String, SeriesObject> series = new HashMap<String, SeriesObject>();
	 MasterClientData masterClientData = null;
	 DocumentWrapper faxes []= null;
	 DocumentWrapper reports[] = null;
	 List<DicomMetadata> metadata =null;
	 
	 private String docBase;
	 private String userName;
	 private String passWord;
	 private String rootDocumentId;
	 
	 String requestedDocumentId;
	    
	 
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(DfcClient.class.getName());

	public DfcClient(String docBase, String userName, String passWord, String rootDocumentId)
			throws IOException {

		basicData.setDocbaseName(docBase);
		basicData.setUserName(userName);
		basicData.setLoginTicket(passWord);

		resource = new ResourceManager();
		sessionData = null;
		osaCommons = new PatientDataHelper();
		this.docBase = docBase;
		this.userName = userName;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
		this.passWord = passWord;
		this.rootDocumentId = rootDocumentId; 
		initFiles();
	}
	
	/**
	 * Creates the scratch directories used to store files from 
	 * Documentum until they are pushed to MedCommons.
	 * 
	 * Transaction directory 1 is for DICOM, PDF, and other external documents.
	 * Transaction directory 2 is for CCR uploads (which typically isn't generated
	 * until the transaction 1 is complete).
	 * 
	 * @throws IOException
	 */
	private void initFiles() throws IOException{
		dataDirectory = new File("data");
        if (!dataDirectory.exists())
        	dataDirectory.mkdir(); // Need to care about security here.
        String transactionRoot = "Trans" + System.currentTimeMillis();
        transactionDirectory1 = new File(dataDirectory,transactionRoot+"_1");
        transactionDirectory1.mkdir();
        writeReceipt(transactionDirectory1);
        
        transactionDirectory2 = new File(dataDirectory,transactionRoot+"_2");
		transactionDirectory2.mkdir();
	    writeReceipt(transactionDirectory2);
	}

	public void startSession() throws DfException {

		sessionData = resource.getSessionData(basicData);
		if (sessionData == null){
			throw new NullPointerException("Null resource.getSessionData()");
		}
		sess = sessionData.getSession();
		if (sess == null) {
			throw new NullPointerException(
					"Null session - probably bad username or password");
		}
		common = new DfcCommon();
		clientManager = new ClientManager();

	}
	public MasterClientData getMetadata(String documentId) throws DfException{
        requestedDocumentId = documentId;
        //startSession();
    //  MasterClientData masterClient = null;
        try{
            masterClientData = clientManager.getMasterClientData(sess, documentId);
        }
         finally{
           // endSession();          
         }
        
        return(masterClientData);
    }
	
	public ByteArrayInputStream  getDocumentStream(String docId) throws DfException, IOException{
	        IDfSysObject doc = common.getExistingObjectByID(docId, sess);
	        
	        if (doc== null)
	            return(null);
	       
	
	        ByteArrayInputStream in= doc.getContent();
	        
	        return(in);
	    }


	public DocumentWrapper  getDocument(String docTitle, String docType, String docId) throws DfException, IOException{
		IDfSysObject doc = common.getExistingObjectByID(docId, sess);
		
		if (doc== null){
			log.info("Document Document returns null for " + docType + " with document id " + docId);
			return(null);
		}
		ByteArrayInputStream in= doc.getContent();
		File docDir = new File(transactionDirectory1, docType);
		docDir.mkdir();
		log.info("Created directory " + docDir.getAbsolutePath());
		File f = new File(docDir,docId + ".pdf");
		FileOutputStream fout = new FileOutputStream(f);
		
		byte b[] = new byte[4096];
		int i;
		while ((i = in.read(b)) != -1) {
		    fout.write(b);
		}
		fout.close();
		//TODO - sha1?
		DocumentWrapper dWrapper = new DocumentWrapper(doc, docTitle, f, docId); 
		return(dWrapper);
	}
	
	long msecReadingDICOM = 0;
	long msecParsingDICOM = 0;
	long totalBytes = 0;
	long imageCount = 0;
	public void resetDICOMCounters(){
		msecReadingDICOM = 0;
		msecParsingDICOM = 0;
		totalBytes = 0;
		imageCount = 0;
		
	}
	public void reportDICOMCounters(){
		log.info("[timing] DICOM Images processed = " + imageCount + ", total bytes = " + totalBytes );
		
		log.info("[timing] Elapsed time reading from Documentum server = " + msecReadingDICOM + "msec");
		log.info("[timing] Elapsed time parsing DICOM = " + msecParsingDICOM + "msec");
		log.info("[timing] Reading MB/second:" + (totalBytes/(1024.0 * 1024.0)) /  (msecReadingDICOM / 1000.0));
		log.info("[timing] Parsing MB/second:" + (totalBytes/(1024.0 * 1024.0)) /  (msecParsingDICOM / 1000.0));
		
	}
	/**
	 * Retrieves DICOM file from Documentum; Parses and extracts metadata from the DICOM file metadata.
	 * 
	 * First writes the data to a scratch file, then moves it to a series folder. The Documentum series
	 * metadata is avoided here - we follow the SeriesInstanceUID parsed from the DICOM header.
	 * 
	 * @param documentId
	 * @return
	 * @throws DfException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public DicomWrapper createMetadataFromImage(String documentId) throws DfException, IOException, NoSuchAlgorithmException{
		
		DicomWrapper dicom = null;
		if (documentId == null){
			throw new NullPointerException("Null documentId");
		}
		if (common == null){
			throw new NullPointerException("Null common - possible login credential problem");
		}
		IDfSysObject documentumData = common.getExistingObjectByID(documentId, sess);
		
		int attrCount = documentumData.getAttrCount();
		log.info("Attribute count is " + attrCount);
		for (int i=0;i<attrCount; i++){
			IDfAttr attr =  documentumData.getAttr(i);
			log.info("Attribute:" + attr.getName() + ", value=" + documentumData.getString(attr.getName()));
		}
		
		long objectSize = documentumData.getContentSize();
		// should test for doctype.
		ReportHelper x;
		//x.getStudyFolderFromReport(strOrderNumber, session, strModality);
		//getImageListFromSeries().
		
		if (documentumData == null){
			throw new IOException("Null document returned in session for document id " + documentId);
		}
		long startRead = System.currentTimeMillis();
		ByteArrayInputStream in= documentumData.getContent();
		File dicomDir = new File(transactionDirectory1, "DICOM");
		dicomDir.mkdir();
		
		File scratchDir = new File(dicomDir, "Scratch" );
		scratchDir.mkdir();
		
		File f = new File(scratchDir,documentId);
		FileOutputStream fout = new FileOutputStream(f);
		
		byte b[] = new byte[4096];
		int i;
		while ((i = in.read(b)) != -1) {
		    fout.write(b,0,i);
		}
		fout.close();
		long stopRead = System.currentTimeMillis();
		long readTime = (stopRead - startRead);
		msecReadingDICOM += readTime;
		
		totalBytes += f.length();
		
		long startParse = System.currentTimeMillis();
		ExtractFileMetadata extract = new ExtractFileMetadata(f);
		DicomMetadata dicomMetadata = extract.parse();
		SHA1 sha1 = new SHA1();
		sha1.initializeHashStreamCalculation();
		String guid = sha1.calculateFileHash(f);
		dicomMetadata.setSha1(guid);
		//log.info("===calculated dicom guid = " + guid);
		dicom = new DicomWrapper(dicomMetadata, documentumData, documentId);
		
		File seriesDir = new File(dicomDir, dicomMetadata.getSeriesInstanceUid() );
		if (!seriesDir.exists())
			seriesDir.mkdir();
		
		File imageFile = new File(seriesDir, documentId);
		FileUtils.copyFile(f, imageFile);
		FileUtils.deleteDir(scratchDir);
		long stopParse = System.currentTimeMillis();
		long parseTime = stopParse - startParse;
		msecParsingDICOM+=parseTime; // Note - this includes calculating SHA1, copying file, and DICOM parsing.
		log.info("Dicom image " + documentId + " has been placed into series folder " + seriesDir.getAbsolutePath());
		
		imageCount++;
		return(dicom);
	}//TODO: Add more data in
	private DicomMetadata extractMetadata(IDfSysObject documentObj) throws DfException{
		DicomMetadata dicom = new DicomMetadata();
		dicom.setSopInstanceUid(documentObj.getString(OsaConstants.OSA_IMAGE_UID));
		dicom.setFrames(1);
		dicom.setInstanceNumber(Integer.parseInt(documentObj.getString(OsaConstants.OSA_IMAGE_NUMBER)));
		dicom.setSeriesNumber(Integer.parseInt(documentObj.getString(OsaConstants.OSA_SERIES_NUMBER)));
		
		dicom.setStudyDescription(documentObj.getString(OsaConstants.OSA_STUDY_DESCRIPTION));
		dicom.setStudyInstanceUid(documentObj.getString(OsaConstants.OSA_STUDY_INSTANCE_UID));
		dicom.setSeriesInstanceUid(documentObj.getString(OsaConstants.OSA_SERIES_INSTANCE_UID));


		dicom.setSeriesDescription(documentObj.getString(OsaConstants.OSA_SERIES_DESCRIPTION));
		
		dicom.setInstitutionName(documentObj.getString(OsaConstants.OSA_INSTITUTION_NAME));
		dicom.setInstitutionAddress("UNKNOWN");
		dicom.setReferringPhysicianName("UNKNOWN");
		dicom.setReferringPhysicianTelephoneNumber("UNKNOWN");
		dicom.setReferringPhysicianAddress("UNKNOWN");
		dicom.setPerformingPhysicianName("UNKNOWN");
		dicom.setPhysicianOfRecord("UNKNOWN");
		dicom.setAccessionNumber(documentObj.getString(OsaConstants.OSA_ACCESSION_NUMBER));
		dicom.setImageType("UNKNOWN");
		dicom.setSopClassUid("UNKNOWN");
		dicom.setStationName("UNKNOWN");
		dicom.setManufacturer("UNKNOWN");
		dicom.setManufacturerModelName("UNKNOWN");
		//dicom.setPatientAddress(documentObj.getString(OsaConstants.OSA_PATIENT_ADDRESS));
		dicom.setPatientAge("");
		//dicom.setPatientTelephoneNumber(documentObj.getString(OsaConstants.OSA_PATIENT_PHONE));
		dicom.setWindowCenter(documentObj.getString("osa_window_center"));
		dicom.setWindowWidth(documentObj.getString("osa_window_width"));
		dicom.setWindowCenterWidthExplanation(documentObj.getString("osa_window_center_width_exp"));
		dicom.setSha1(documentObj.getString("osa_sha1_hash"));
		dicom.setModalities(documentObj.getString(OsaConstants.OSA_MODALITIES_IN_STUDY));
		dicom.setModality(documentObj.getString(OsaConstants.OSA_MODALITY));
		dicom.setPatientId(documentObj.getString(OsaConstants.OSA_PATIENT_ID));
		dicom.setPatientSex(documentObj.getString(OsaConstants.OSA_PATIENT_SEX));
		dicom.setPatientName(documentObj.getString(OsaConstants.OSA_PATIENT_NAME));
		dicom.setPatientDateOfBirth(documentObj.getString(OsaConstants.OSA_PATIENT_DOB));
		dicom.setLength(documentObj.getContentSize());
		
		String studyDate = documentObj.getString(OsaConstants.OSA_STUDY_DATE);
		
		Date d = dicomToJavaDateTime(studyDate, null);
		if (d==null){
			d=new Date(System.currentTimeMillis());
		}
		log.info("==>Study date set to " + d.toString());
		dicom.setStudyDate(d);
		dicom.setSeriesDate(d);
		
	
	/*
	
	 */
		return(dicom);
	}
public DicomWrapper createMetadataFromMetadata(String documentId) throws DfException, IOException, NoSuchAlgorithmException{
		
		long startParse = System.currentTimeMillis();
		DicomWrapper dicom = null;
		if (documentId == null){
			throw new NullPointerException("Null documentId");
		}
		if (common == null){
			throw new NullPointerException("Null common - possible login credential problem");
		}
		IDfSysObject documentumData = common.getExistingObjectByID(documentId, sess);
		
		int attrCount = documentumData.getAttrCount();
		log.info("Attribute count is " + attrCount);
		for (int i=0;i<attrCount; i++){
			IDfAttr attr =  documentumData.getAttr(i);
			if (log.isDebugEnabled())
				log.debug("Attribute:" + attr.getName() + ", value=" + documentumData.getString(attr.getName()));
		}
		
		
		DicomMetadata dicomMetadata = extractMetadata(documentumData);
		
		dicom = new DicomWrapper(dicomMetadata, documentumData, documentId);
		
	
		long stopParse = System.currentTimeMillis();
		long parseTime = stopParse - startParse;
		msecParsingDICOM+=parseTime; // Note - this includes calculating SHA1, copying file, and DICOM parsing.
		log.info("Dicom image " + documentId + " has been parsed from metadata ");
		
		imageCount++;
		return(dicom);
	}
	public String calculateSeriesSha1(List<DicomWrapper> images) throws NoSuchAlgorithmException, IOException{
		
		List<String> sha1List = new ArrayList<String>();
		for (int i=0;i<images.size();i++){
			sha1List.add(images.get(i).getDicomMetadata().getSha1());
			
		}
		Collections.sort(sha1List);
		String guids[] = sha1List.toArray(new String[sha1List.size()]);
		 SHA1 sha1 = new SHA1();
         sha1.initializeHashStreamCalculation();
        
         String seriesSha1 = sha1.calculateStringNameHash(guids);
         return(seriesSha1);
	}
	public List<DicomTransaction> generateTransactions() throws NoSuchAlgorithmException, IOException{
		List<DicomTransaction> transactions = new ArrayList<DicomTransaction>();
		 metadata = new ArrayList<DicomMetadata>();
		Set<String> seriesKeys = series.keySet();
		Iterator<String> keys = seriesKeys.iterator();
		while(keys.hasNext()){
			SeriesObject seriesObj = series.get(keys.next());
			List<DicomWrapper> images = seriesObj.getImages();
			if (images.size()==0){
				throw new RuntimeException("Series " + seriesObj.getIdentifier() + " has zero images");
			}
			DicomMetadata anImage = images.get(0).getDicomMetadata();
			if (dicomMetadata == null){
				dicomMetadata = anImage; // Kludge. Just take an image to get the study info correct.
			}
			for (int i=0;i<images.size();i++){
				metadata.add(images.get(i).getDicomMetadata());
			}
			DicomTransaction trans = new DicomTransaction();
			 trans.setSeriesDescription(anImage.getSeriesDescription());
	         trans.setSeriesInstanceUid(anImage.getSeriesInstanceUid());
	         trans.setPatientName(anImage.getPatientName());
	         trans.setStudyDescription(anImage.getStudyDescription());
	         trans.setStudyInstanceUid(anImage.getStudyInstanceUid());
	         trans.setSeriesSha1(calculateSeriesSha1(images));
	         trans.setStorageId("-1"); // For new account
	         transactions.add(trans);
			
		}
		return(transactions);
	}
	public File generateCCRUpload(String storageId) throws IOException, NoSuchAlgorithmException{
		
		  CCRGenerator ccrGenerator = new CCRGenerator();
		  ContinuityOfCareRecordDocument ccr = null;
		  List<DicomTransaction> transactions = generateTransactions();
		 
		 
		  
		 
	      log.info("(before CCR creation):DicomMetadata is " + dicomMetadata);
	      ccrGenerator.createDemographicsFromDICOM(storageId, metadata, transactions, null);
       
        for (int i=0;i<transactions.size();i++){
			DicomTransaction transaction = transactions.get(i);
			DicomMetadata seriesMetadata = null;
			for (int j=0;j<metadata.size();j++){
				DicomMetadata candidate = metadata.get(j);
				if (candidate.getSeriesInstanceUid().equals(transaction.getSeriesInstanceUid())){
					seriesMetadata=candidate;
					break;
				}

			}
			if (seriesMetadata == null){
				throw new NullPointerException("No series with SeriesInstanceUID " +
						transaction.getSeriesInstanceUid());
			}
			SeriesObject seriesObject = series.get(seriesMetadata.getSeriesInstanceUid());
			Map<String, String> docMap = generateMapfromDocumentId(seriesObject.getIdentifier());
			ccr = ccrGenerator.addDicomReference(transaction, seriesMetadata, docMap);
					
		}
        if (faxes != null){
        	
        	File f = faxes[0].getDocumentFile();
        	Map<String, String> docMap = generateMapfromDocumentId(faxes[0].getIdentifier());
        	long documentBytes = f.length();
        	SHA1 sha1 = new SHA1();
        	sha1.initializeHashStreamCalculation();
        	String faxSha1 = sha1.calculateFileHash(f);
        	ccr= ccrGenerator.addSimpleDocumentReference( 
        			
        			faxes[0].getTitle(),
        			faxSha1, DocumentTypes.PDF_MIME_TYPE, documentBytes, docMap);
        	
        }
        if (reports != null){
        	for (int i=0;i<reports.length;i++){
        		File f = reports[i].getDocumentFile();
        		Map<String, String> docMap = generateMapfromDocumentId(reports[i].getIdentifier());
        		long documentBytes = f.length();
        		SHA1 sha1 = new SHA1();
            	sha1.initializeHashStreamCalculation();
            	String docSha1 = sha1.calculateFileHash(f);
            	ccr= ccrGenerator.addSimpleDocumentReference( 
            			reports[i].getTitle(),docSha1, DocumentTypes.PDF_MIME_TYPE, documentBytes,docMap);
        	}
        }
        File ccrDir = new File(transactionDirectory2, "CCR");
        ccrDir.mkdir();
        ObjectData patientData= masterClientData.getPatientData();
        String patientId = (String) patientData.getStringAttributeMap().get(OsaConstants.OSA_PATIENT_ID);
        File ccrFile = new File(ccrDir,"Patient" + patientId + ".xml");
        
        
       
        
        CCRDocumentUtils.saveCCR(ccr, ccrFile);
        log.info("created CCR file " + ccrFile.getCanonicalPath() + ", length=" + ccrFile.length());
        return(transactionDirectory2);
	}
	public File[] retrieveFilesFromDocumentum(MasterClientData masterClient) throws DfException, IOException, NoSuchAlgorithmException{
        Map<String, List<String>> imageMap = masterClient.getImageMap();
        //dumpMap("imageMap", imageMap);
        //getDICOMFiles(imageMap);
        //masterClient.getFaxOrderId();
        downloadDocuments(sessionData, masterClient);
        
        File[] files = null; // actually - want format to be what makes sense for CXP upload.
        return(files);
    }
	public Map<String, SeriesObject> getSeriesMetadata(MasterClientData masterClient) throws DfException, IOException, NoSuchAlgorithmException{
	    Map<String, String> physicianMap        = masterClient.getPhysicianMap();
        Map<String, String> studyModalityMap    = masterClient.getStudyModalityMap();
        Map<String, String> modalityMap         = masterClient.getModalityMap();
        Map<String, List<String>> seriesMap     = masterClient.getSeriesMap();
        Map<String, List<String>> imageMap      = masterClient.getImageMap();
	    ObjectData patientData = masterClient.getPatientData();
        
        
        Set<String> modalityFolderIdSet = studyModalityMap.keySet();
        Iterator<String> modalityFolderIds = modalityFolderIdSet.iterator();
        while (modalityFolderIds.hasNext()){
            String modalityFolderId = modalityFolderIds.next();
            String studyFolderId = studyModalityMap.get(modalityFolderId);
            System.out.println("ModalityFolderId:" + modalityFolderId + "=> modality " + modalityMap.get(modalityFolderId) );
            List<String> seriesIds =seriesMap.get(studyFolderId);
            Iterator<String> seriesIterator = seriesIds.iterator();
            while(seriesIterator.hasNext()){
                String seriesFolderId = seriesIterator.next();
                List<String> imageList = imageMap.get(seriesFolderId);
                Iterator<String> imageIterator = imageList.iterator();
                while (imageIterator.hasNext()){
                    String imageId = imageIterator.next();
                    IDfSession session= sessionData.getSession();
                    IDfSysObject imageData = common.getExistingObjectByID(imageId, session);
                    log.info("imageData:" + imageData.toString() + " UID = " + imageData.getString(OsaConstants.OSA_IMAGE_UID));
                    //DicomWrapper imageObj = createMetadataFromImage(imageId);
                    DicomWrapper imageObj = createMetadataFromMetadata(imageId);
                    DicomWrapper.useDocumentumMetadata(imageObj, masterClient, imageData);
                    imageObj.getDicomMetadata().setInstitutionAddress("176 South Street. Hopkinton, MA 01748");
                    String seriesInstanceUID = imageObj.getDicomMetadata().getSeriesInstanceUid();
                    SeriesObject seriesObject = series.get(seriesInstanceUID);
                    if (seriesObject == null){
                        seriesObject = new SeriesObject(
                                imageObj.getDicomMetadata().getStudyInstanceUid(), 
                                seriesInstanceUID,
                                seriesFolderId);
                        series.put(seriesInstanceUID, seriesObject);
                    }
                    seriesObject.addImage(imageObj);
                    
                }
            }
            
            log.info("Created series object with " + series.size() + " series ");
            Set<String> seriesKeys = series.keySet();
            Iterator<String> iter = seriesKeys.iterator();
            while (iter.hasNext()){
                String seriesKey = iter.next();
               SeriesObject seriesObj = series.get(seriesKey);
              log.info("Series with key " + seriesKey + " has " + seriesObj.getImages().size() + " images ");
            }

            
        }
	    return series;
	}
    
	   public void downloadDocuments(ISessionData sessionData, MasterClientData masterClient) throws DfException, IOException, NoSuchAlgorithmException{
	        resetDICOMCounters();
	    
	        Map<String, String> reportMap           = masterClient.getStudyReportMap();
	    
	        long startTime = System.currentTimeMillis();
	        
	        String faxOrderId = masterClient.getFaxOrderId();
	        faxes = new DocumentWrapper[1];
	        faxes[0] = getDocument("Fax Order", "PDF", faxOrderId);
	        
	        String orderFolderId = masterClient.getOrderFolderId();
	        
	        log.info("Retrieved fax - " + faxes[0].getDocumentFile().getAbsolutePath());
	        //masterClient.getOrderFolderId();
	        
	        Set <String> reportSet = reportMap.keySet();
	        int reportCounter = 0;
	        if (reportSet.size() > 0){
	            reports = new DocumentWrapper[reportSet.size()];
	        }
	        Iterator<String> reportKeys = reportSet.iterator();
	        while(reportKeys.hasNext()){
	            String reportKey = reportKeys.next();
	            String reportId = reportMap.get(reportKey);
	            DocumentWrapper aReport = getDocument("Report", "PDF", reportId);
	            reports[reportCounter] = aReport;
	            reportCounter++;
	        }
	        long endTime = System.currentTimeMillis();
	        getSeriesMetadata(masterClient);
	        reportDICOMCounters();
	        
	    }
	/*
	 *  Write a property file for use by the CXP upload routine.
	 */
	protected void writeReceipt(File directory) throws IOException{
		Properties p = new Properties();
        p.setProperty("Version", "1.0.0.5");//Version.getVersionString());
        p.setProperty("Revision", "100");
        
        File receiptFile = new File(directory, "Receipt.txt");
        FileOutputStream out = new FileOutputStream(receiptFile);
        p.store(out, "EMCBRIDGE");
	}
	private Map<String, String> generateReferenceMap(){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("StorageHandler", "Documentum");
		map.put("UserName", this.userName);
		map.put("DocBase", this.docBase);
		map.put("PassWord", this.passWord);
		map.put("QueryDocumentIdentifier", this.rootDocumentId);
		return(map);
	}
	protected Map<String, String> genrateMapfromDocumentWrapper(DocumentWrapper document){
		Map<String, String> map = generateReferenceMap();
		
		map.put("DocumentIdentifier", document.getIdentifier());
		return(map);
	}
	
	protected Map<String, String> generateMapfromDocumentId(String documentId){
		Map<String, String> map = generateReferenceMap();
		
		map.put("DocumentIdentifier", documentId);
		return(map);
	}

	public void endSession() throws DfException {
		
		resource.releaseResources(sessionData);
	}
	
	public File getTransactionDirectory1(){
		return(this.transactionDirectory1);
	}
	
	/**
	 * Converts from DICOM format dates to Java Date Object
	 * @param sDate
	 * @param sTime
	 * @return
	 */
	 private Date dicomToJavaDateTime(String sDate, String sTime){
	    	Date newDate = null;
	    	if(sDate==null)
	    		return(null);

	       	 try{

		        	 if (sTime == null){
		        		 SimpleDateFormat dicomDate = new SimpleDateFormat(DICOM_DATE_FORMAT);
		        		 newDate = dicomDate.parse(sDate);
		        	 }
		        	 else if (sTime.length() < 7){
		        		 String dateTime = sDate + sTime;
		        		 SimpleDateFormat dicomDate = new SimpleDateFormat(DICOM_DATE_FORMAT + DICOM_TIME_FORMAT_SIMPLE);
		        		 newDate = dicomDate.parse(dateTime);
		        	 }
		        	 else{
		        		 String dateTime = sDate + sTime;
		        		 SimpleDateFormat dicomDate = new SimpleDateFormat(DICOM_DATE_FORMAT + DICOM_TIME_FORMAT);
		        		 newDate = dicomDate.parse(dateTime);
		        	 }
		        	 log.debug("Series date = " + sDate +
		        			 ", time=" + sTime + ", parsed =" +
		        			 newDate.toLocaleString());
	       	 }
	       	 catch(ParseException e){
	       		 log.error("Error parsing date " + sDate + ", time " + sTime);
	       		 newDate = null;
	       	 }
	       	 return(newDate);
	        }
	    
}
