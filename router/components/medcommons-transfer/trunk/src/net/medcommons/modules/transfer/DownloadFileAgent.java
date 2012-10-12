package net.medcommons.modules.transfer;




import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.medcommons.modules.cxp.client.CXPClient;
import net.medcommons.modules.utils.PerformanceMeasurement;

import org.apache.log4j.Logger;
import org.cxp2.Document;
import org.cxp2.GetRequest;
import org.cxp2.GetResponse;
import org.cxp2.RegistryParameters;

/**
 * This is a very simple shell for what will be a background application for transferring files between
 * applications (Osirix first, but others will be added in the future) and the MedCommons Gateway.
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
 * @author mesozoic
 *
 */
public class DownloadFileAgent extends TransferBase{
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(DownloadFileAgent.class);

	private File exportFolder = null;
	private String storageId = null;
	private String endpoint = null;
	private String[] guids = null;
	private String senderAccountId = null;
	
	/**
	 * Controls whether downloaded files have file extensions added
	 * according to their content types.
	 */
	private boolean addFileExtensions = true;

	public DownloadFileAgent(String endpoint, String storageId, String senderAccountId, String[] guids, File exportFolder) throws Exception{
		this.endpoint = endpoint;
		this.exportFolder = exportFolder;
		this.storageId = storageId;
		this.guids = guids;
		this.senderAccountId = senderAccountId;
		if (endpoint == null)
			throw new NullPointerException("Endpoint must not be null");
		if (storageId == null)
			throw new NullPointerException("StorageId must not be null");
		if (guids == null)
			throw new NullPointerException("Guid array must not be null");
		for (int i=0;i<guids.length; i++){
			if (guids[i] == null)
				throw new NullPointerException("Element " + i + " of guid array is null");
		}
		if (exportFolder == null)
			throw new NullPointerException("Export folder must not be null");

		if (!exportFolder.exists()){
			throw new FileNotFoundException("Specified folder does not exist:" + exportFolder.getAbsolutePath());
		}

		if (!(storageId.length()==16))
			throw new IllegalArgumentException("StorageId '" + storageId + "' must be 16 digits in length, not " + storageId.length());
	}
	/**
	 * Downloads the files specified in the constructor.
	 * <p>
	 * Files are downloaded to a child folder inside the destination with the
	 * name equal to the storageId specified in the constructor.
	 */
	public GetResponse download() throws Exception {
		long startTime = System.currentTimeMillis();

		File cache = new File(exportFolder, "CXPCache");
		if (!cache.exists()){
			boolean success = cache.mkdirs();
			if (!success)
				throw new IOException("Unable to make export cache directory " + cache.getAbsolutePath());
		}
		
		client = new CXPClient(endpoint,cache);
		
		client.setAddFileExtensions(addFileExtensions);

		GetRequest request = new GetRequest();
		request.setStorageId(storageId);
		log.info("About to get " + guids.length + " documents");
		for (int i=0;i<guids.length;i++){
			Document docinfo = new Document();

			docinfo.setDescription("Unknown");
			docinfo.setGuid(guids[i]);
			docinfo.setContentType("Unknown");
			log.info("guid=" + guids[i]);

			request.getDocinfo().add(docinfo);
		}
		if (senderAccountId != null){
			RegistryParameters parameters= CXPClient.generateSenderIdParameters(senderAccountId);

			request.getRegistryParameters().add(parameters);

			if(storageId.equals("-1")){
				log.info("New account to be created in group of account " + senderAccountId);
			}
			CXPClient.displayRegistryParameters(request.getRegistryParameters());
		}


		GetResponse resp = client.getService().get(request);

		File downloadFolder = getDownloadFolder();
		client.processGetResponse(resp,  startTime, downloadFolder);
		client.displayClientConfigs();
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("Download to " + downloadFolder.getAbsolutePath(), (endTime-startTime), client.getByteCount()));

		return(resp);
	}
	
    public File getDownloadFolder() {
        File downloadFolder = new File(exportFolder, storageId);
        return downloadFolder;
    }
    
	public long getBytesTransferred(){
		if (client == null)
			return(0);
		else
			return(client.getInputBytes());
	}


	public static void main(String[] args){

		try{
			if (args.length<3){
				usage();
				throw new IllegalArgumentException("Incorrect arguments provided to DownloadFileAgent.main()");
			}
			String endPoint = args[0];

			String storageId = args[1];
			String guid= args[2];
			String folderName = args[3];

			String guids[] = new String[1];
			guids[0] = guid;

			String senderAccountId = null;
			if (args.length== 5){
				senderAccountId = args[4];
			}

			File exportFolder = new File(folderName);
			if (!exportFolder.exists()){
				usage();
				throw new FileNotFoundException("Export folder " + exportFolder.getAbsolutePath());
			}

			DownloadFileAgent downloadFileAgent = new DownloadFileAgent(endPoint, storageId, senderAccountId, guids,exportFolder);
			GetResponse resp = downloadFileAgent.download();



		}
		catch(Exception e){
			log.error("Exception running DownloadFileAgent:" + e.toString(), e);
		}

	}

	public static void usage(){
		System.out.println("DownlodTransferAgent <cxp endpoint> <storageid> <guid> <folder containing document>");
	}
    public boolean getAddFileExtensions() {
        return addFileExtensions;
    }
    public void setAddFileExtensions(boolean addFileExtensions) {
        this.addFileExtensions = addFileExtensions;
    }


}
