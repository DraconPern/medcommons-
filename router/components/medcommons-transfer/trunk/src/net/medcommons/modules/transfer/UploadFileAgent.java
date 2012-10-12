package net.medcommons.modules.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.cxp.client.CXPClient;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.FilenameFileFilter;
import net.medcommons.modules.utils.PerformanceMeasurement;
import net.medcommons.modules.utils.SpecialFileFilter;

import org.apache.log4j.Logger;
import org.cxp2.Document;
import org.cxp2.Parameter;
import org.cxp2.PutRequest;
import org.cxp2.PutResponse;
import org.cxp2.RegistryParameters;

/**
 * This is a very simple shell for what will be a background application for transferring files between
 * applications (Osirix first, but others will be added in the future) and the MedCommons Gateway.
 * <P>
 * While the ambitions for this program are grandiose- this particular implementation is purposely
 * trivial. We might want:
 * <ul>
 * <li> Deployment via JavaWebStart</li>
 * <li> Logging, error handling to make remote support easier.</li>
 * <li> Ability to detect updates, run updates</li>
 * <li> Correct use of daemon threads for easy shutdown </li>
 * <li> Automatic restart in case of a crash </li>
 * <li> Run in a system tray app on Windows, run in the Dashboard (?) on MacOSX.</li>
 * </ul>
 * ... and many, many more things. But we'll figure this out as we go.
 *
 * <P>
 * File formats
 * <ul>
 * <li> A file version of -1 indicates the old file format; no Reciept.txt file is in the directory being uploaded. Details of the format:
 * 		<ul>
 *        <li> Each directory contains DICOM files. There are no directories that do not contain DICOM.
 *        <li> All other files are in the main directory. *.xml files are assumed to be CCRs, *.pdf files are PDFs, and
 *             *.JPG files are JPEGs.
 *        <li> Files with other extensions are ignored.
 * 		</ul>
 * <li> Otherwise version is the router's svn checkin number. The new file format is:
 * 		<ul>
 * 			<li> All PDFs are in the PDF/ subfolder.
 * 			<li> All CCRs are in the CCR/ subfolder
 * 			<li> All JPEGs are in the JPG/ subfolder.
 * 			<li> All DICOM series are in the DICOM/ subfolder. Each series is in a subfolder of the DICOM/ folder.
 * 			<li> The file Receipt.txt is in the top level folder.
 * 	        <li> Other files/directories are ignored.
 * 		</ul>
 * </ul>
 * 
 * The following parameters can be passed through UploadFileAgent to the CXP server:
 * <ul>
 *  <li> MergeCCR - instructions for how uploaded CCRs are to be merged. The caller determines the context.
 *  <li> PaymentBypassToken - a token which (if valid) excludes the message from any payments.
 * </ul>
 * @author mesozoic
 *
 */
public class UploadFileAgent extends TransferBase {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(UploadFileAgent.class);

	private File importFolder = null;
	private String storageId = null;
	private String endpoint = null;
	private String senderAccountId = null;
	private String authorizationToken = null;
	private File transformedCCRFolder = null;
	private String bypassPaymentToken = null;
	private CXPConstants.MergeCCRValues mergeCCR = null; 


	public UploadFileAgent(String endpoint, String authorizationToken, String storageId, File importFolder) throws Exception{
		this.endpoint = endpoint;
		this.importFolder = importFolder;
		this.storageId = storageId;
		if (!importFolder.exists()){
			throw new FileNotFoundException("Specified folder does not exist:" + importFolder.getAbsolutePath());
		}
		if (storageId == null)
			throw new NullPointerException("StorageId is null");
		if (!(storageId.length()==16) && (!"-1".equals(storageId)))
			throw new IllegalArgumentException("StorageId '" + storageId + "' must be 16 digits in length, not " + storageId.length());

		this.authorizationToken = authorizationToken;

	}
	public UploadFileAgent(String endpoint, String authorizationToken, String storageId, String senderAccountId, File importFolder) throws Exception{
		this(endpoint, authorizationToken, storageId, importFolder);
		this.senderAccountId = senderAccountId;
	}
	
	/**
	 * Overrides the default CCR folder location. Useful when the CCRs have to be modified 
	 * before being uploaded; we want to leave the original data intact.
	 * 
	 * @param ccrFolder
	 */
	public void setCCRFolder(File ccrFolder){
		this.transformedCCRFolder = ccrFolder;
	}
	
	public void setPaymentBypass(String bypassPaymentToken){
		this.bypassPaymentToken = bypassPaymentToken;
	}
	public void setMergeCCR(CXPConstants.MergeCCRValues mergeCCR){
	    this.mergeCCR = mergeCCR;
	}
	/**
	 * Uploads all files in the directory to the specified CXP endpoint.
	 * <P>
	 * Note that the CCR documents are uploaded last because they may have embedded references
	 * to other documents. This may throw an exception on the server if the referenced objects
	 * don't exist.
	 *
	 * @throws Exception
	 */
	public PutResponse upload() throws Exception{
		long startTime = System.currentTimeMillis();
		int version = getFileFormatVersion();
		;
		File dicomFolder;

		if (version == -1)
			dicomFolder = importFolder;
		else
			dicomFolder =  new File(importFolder, "DICOM");

		File cxpCache = new File(importFolder, "CXPCache");
		cxpCache.mkdir();

		client = new CXPClient(endpoint,cxpCache);
		setCXPClient(client);
		PutRequest request = new PutRequest();
		request.setStorageId(storageId);
		SpecialFileFilter filter = new SpecialFileFilter();
		filter.setFilterType(".");

		
		

		log.info("CXP PUT " + endpoint + " documents to storage account " + storageId + " from directory "
				+ importFolder.getAbsolutePath() + "(Revision " + version + ")");

		if (dicomFolder.exists()){

			File allFiles[] = dicomFolder.listFiles(filter);

			for (int i=0;i<allFiles.length;i++){
				File f = allFiles[i];
				if (f.isDirectory()){
					log.info("Series directory:" + f.getAbsolutePath());
					List<Document> documents = client.createCompoundDocument(allFiles[i], DocumentTypes.DICOM_MIME_TYPE, allFiles[i].getName(), false);
					for(int j=0;j<documents.size();j++){

						request.getDocinfo().add(documents.get(j));
						
					}
				}
			}
		}

		if (version == -1){
			// Old version
			  getPDFFiles_deprecated(client, request, importFolder);
			  getJPGFiles_deprecated(client, request, importFolder);
			  getCCRFiles_deprecated(client, request, importFolder);

		}
		else{
			
			//for CCR files - change to copy ccrs over to the cxpCache folder and (perhaps) modify them on the fly.
			//Also - might be good to take a first pass and see if the CCRs are invalid.
			
			File pdfFolder = new File(importFolder, "PDF");
			File jpgFolder = new File(importFolder, "JPG");
			File ccrFolder;
			if (transformedCCRFolder == null){
				ccrFolder = defaultCCRFolder();
			}
			else{
				ccrFolder = transformedCCRFolder;
			}
			getPDFFiles(client, request, pdfFolder);
			getJPGFiles(client, request, jpgFolder);
			getCCRFiles(client, request, ccrFolder);
		}
		log.info("storageId:" + storageId);
		log.info("senderAccountId:" + senderAccountId);
		log.info("authorizationToken:" + authorizationToken);
		
        setRequestParameters(request);
		
		if (senderAccountId != null) {
			if(storageId.equals("-1")){
				log.info("New account to be created in group of account " + senderAccountId);
			}
		}
		CXPClient.displayRegistryParameters(request.getRegistryParameters());
		setTotalBytes(client.getByteCount());
		PutResponse resp = client.getService().put(request);

		client.displayClientConfigs();
		long endTime = System.currentTimeMillis();
		log.info("client bytecount = " + client.getByteCount() + ", total bytes transferred = " + client.getOutputBytes());
		log.info(PerformanceMeasurement.throughputString("Upload to " + storageId, (endTime-startTime), client.getByteCount()));

		return(resp);


	}
	
	/**
	 * Prepare a request by setting sender id and other parameters.
	 * <p>
	 * Also allows code extending this class to customize parameters sent.
	 * 
	 * @param request
	 */
    protected void setRequestParameters(PutRequest request) {
        RegistryParameters parameters= CXPClient.generateSenderIdParameters(senderAccountId, authorizationToken);
        request.getRegistryParameters().add(parameters);
        if (bypassPaymentToken != null){
        	Parameter param = new Parameter();
 		    param.setName(CXPConstants.PaymentBypassToken);
 		    param.setValue(bypassPaymentToken);
 		    parameters.getParameters().add(param);
        	
        }
        if (mergeCCR != null){
            Parameter param = new Parameter();
            param.setName(CXPConstants.MergeCCR);
            param.setValue(mergeCCR.name());
            parameters.getParameters().add(param);
        }
    }

	private void getPDFFiles_deprecated(CXPClient client, PutRequest request, File folder) throws Exception{
		if (!folder.exists()) return;
		FilenameFileFilter filter = new FilenameFileFilter();
		filter.setFilenameFilter(".pdf");

		File files[] = folder.listFiles(filter);
		for (int i=0;i<files.length;i++){
			Document doc = client.createSimpleDocument(files[i], DocumentTypes.PDF_MIME_TYPE, files[i].getName());
			request.getDocinfo().add(doc);
		}
	}
	private void getJPGFiles_deprecated(CXPClient client, PutRequest request, File folder) throws Exception{
		if (!folder.exists()) return;
		FilenameFileFilter filter = new FilenameFileFilter();
		filter.setFilenameFilter(".jpg");
		File files[] = folder.listFiles(filter);
		for (int i=0;i<files.length;i++){
			Document doc = client.createSimpleDocument(files[i], DocumentTypes.JPG_MIME_TYPE, files[i].getName());
			request.getDocinfo().add(doc);
		}
	}
	private void getCCRFiles_deprecated(CXPClient client, PutRequest request, File folder) throws Exception{
		if (!folder.exists()) return;
		FilenameFileFilter filter = new FilenameFileFilter();
		filter.setFilenameFilter(".xml");
		File files[] = folder.listFiles(filter);
		for (int i=0;i<files.length;i++){
			Document doc = client.createSimpleDocument(files[i], DocumentTypes.CCR_MIME_TYPE, files[i].getName());
			request.getDocinfo().add(doc);
		}
	}

	private void getPDFFiles(CXPClient client, PutRequest request, File folder) throws Exception{
		if (!folder.exists()) return;
		FilenameFileFilter filter = new FilenameFileFilter();
		filter.setFilenameFilter(".pdf");

		File files[] = folder.listFiles(filter);
		for (int i=0;i<files.length;i++){
			Document doc = client.createSimpleDocument(files[i], DocumentTypes.PDF_MIME_TYPE, files[i].getName());
			request.getDocinfo().add(doc);
		}
	}
	private void getJPGFiles(CXPClient client, PutRequest request, File folder) throws Exception{
		if (!folder.exists()) return;
		FilenameFileFilter filter = new FilenameFileFilter();
		filter.setFilenameFilter(".jpg");
		File files[] = folder.listFiles(filter);
		for (int i=0;i<files.length;i++){
			Document doc = client.createSimpleDocument(files[i], DocumentTypes.JPG_MIME_TYPE, files[i].getName());
			request.getDocinfo().add(doc);
		}
	}
	
	private void getCCRFiles(CXPClient client, PutRequest request, File folder) throws Exception{
		if (!folder.exists()) return;
		FilenameFileFilter filter = new FilenameFileFilter();
		filter.setFilenameFilter(".xml");
		File files[] = folder.listFiles(filter);
		for (int i=0;i<files.length;i++){
			Document doc = client.createSimpleDocument(files[i], DocumentTypes.CCR_MIME_TYPE, files[i].getName());
			request.getDocinfo().add(doc);
		}
	}
	public Properties getFileProperties(){
		File versionFile = new File(importFolder, "Receipt.txt");
		Properties props = null;
		if (versionFile.exists()){
			try{
				props = new Properties();

				FileInputStream in = new FileInputStream(versionFile);
				props.load(in);
				return(props);
			}
			catch(Exception e){
				throw new RuntimeException("Error reading version file " + versionFile.getAbsolutePath(), e);
			}
		}
		else{
			return(null);
		}
	}
	/**
	 * Returns the version of the file to be uploaded. The Reciept.txt
	 * file contains this information- it is written out by the
	 * PersonalBackup utility. If the file is missing then -1 is returned
	 * and the files are assumed to be in the flat file format described above.
	 *
	 * @return
	 */
	public int getFileFormatVersion(){
		int version  = -1;
		Properties props = getFileProperties();
		if (props!=null){
			String s = props.getProperty("Revision");
			if (s != null)
				s = s.substring(0, s.length()-1);
			log.info("Revision is " + s);
			int i = s.indexOf(":");
			if (i != -1){
				// Version string of the format "2859:2860" - two version
				// numbers from svn. Take the more recent one
				s = s.substring(i+1);
				log.info("New revision number " + s);
			}
			version = Integer.parseInt(s);
		}
		return(version);
	}
	public  File defaultCCRFolder(){
		File ccrFolder = new File(importFolder, "CCR");
		return(ccrFolder);
	}

	public static void main(String[] args){

		try{
			if (args.length<2){
				usage();
				throw new IllegalArgumentException("Incorrect arguments provided to TransferAgent.main()");
			}
			String endpoint = args[0];
			String folderName = args[1];
			File importFolder = new File(folderName);
			if (!importFolder.exists()){
				usage();
				throw new FileNotFoundException("Import folder " + importFolder.getAbsolutePath());
			}
			
			String authorizationToken = null;
			if(args.length >= 3) {
			    authorizationToken = args[2];
			}

			// Want to filter out files like .DS_Store - any file that starts with '.'.
			SpecialFileFilter filter = new SpecialFileFilter();
			filter.setFilterType(".");

			File accountFolders[] = importFolder.listFiles(filter);
			if (accountFolders.length == 0){
				log.info("No account folders found for CXP upload");
			}
			else {
    		    for (int i=0;i<accountFolders.length; i++){
    		        File uploadFolder = accountFolders[i];
    		        String storageId = uploadFolder.getName();
    		        UploadFileAgent uploadFileAgent = new UploadFileAgent(endpoint, authorizationToken, storageId, uploadFolder);
    		        uploadFileAgent.setPaymentBypass(authorizationToken);
    		        PutResponse resp = uploadFileAgent.upload();
    		        CXPClient.displayResponseInfo(resp);
    		    }
			}
    		
		}
		catch(Exception e){
			log.error("Exception running TransferAgent:" + e.toString(), e);
		}

	}
	public static void usage(){
		System.out.println("TransferAgent <cxp endpoint> <folder containing document>");
	}
	public long getBytesTransferred(){
		if (client == null)
			return(0);
		else
			return(client.getOutputBytes());
	}

}
