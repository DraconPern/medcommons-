package net.medcommons.application.utils;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import net.medcommons.application.dicomclient.Job;
import net.medcommons.application.dicomclient.utils.DDLTypes;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.upload.State;
import net.medcommons.application.upload.StatusUpdate;
import net.medcommons.application.upload.UploadContext;
import net.medcommons.client.utils.CCRDocumentUtils;
import net.medcommons.modules.cxp.client.CXPClient;
import net.medcommons.modules.transfer.UploadFileAgent;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.modules.utils.FilenameFileFilter;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.cxp2.PutResponse;
import org.cxp2.RegistryParameters;

import astmOrgCCR.ActorType;
import astmOrgCCR.CodedDescriptionType;
import astmOrgCCR.ContinuityOfCareRecordDocument;
import astmOrgCCR.IDType;
import astmOrgCCR.ReferenceType;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.References;

/*
 * UploadFileChooser.java uses these files:
 *  

 */
public class UploadFileChooser extends JPanel implements ActionListener {


	private static Logger log = Logger.getLogger(UploadFileChooser.class
			.getName());

	//JButton openButton;

	//JLabel description = new JLabel("Select folder containing DICOM files\nor individual DICOM files");
	private static File lastDirectory = null;
	private UploadContext uploadContext = null;

	JFileChooser fc;
	
	 

	public UploadFileChooser(UploadContext uploadContext ,boolean cdOnly,StatusUpdate statusCallback) {
		super(new BorderLayout());
		

		this.uploadContext = uploadContext;
		
		//Create a file chooser
		if (!cdOnly){
			if (lastDirectory != null)
				fc = new JFileChooser(lastDirectory);
			else
				fc = new JFileChooser();
		}
		else{
			File [] roots = File.listRoots();
			File cdDirectory = null;
			String osName = System.getProperty("os.name");
			log.info("OS name is:" + osName);
			/**
			 * Basic rule:
			 * In Mac OS X - just show the /Volumes
			 * In Windows - grab D:, E:, or F:.. if they are available.
			 * In Linux - just grab the first root folder.
			 * 
			 */
			if ("Mac OS X".equals(osName)){
				cdDirectory = new File("/Volumes");
			}
			else {
				
				for (int i=0;i<roots.length;i++){
					String fname = roots[i].getAbsolutePath();
					log.info("Root file system " + i + " " + fname);
					if (fname.indexOf("C:") == -1){
						
						if (roots[i].exists()){
							cdDirectory = roots[i];
							log.info("Selected root file system " + cdDirectory);
							break;
						}
					}
				}
				if (cdDirectory == null){
					if (roots.length>0){
						cdDirectory = roots[0];
					}
				}
			}
			if (cdDirectory == null)
				fc = new JFileChooser();
			else
				fc = new JFileChooser(cdDirectory);
			
		}
		// Permit the selection of a directory containing DICOM files
		// or an individual file.
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.show();
		int returnVal = fc.showOpenDialog(UploadFileChooser.this);

		log.info("File chooser returnVal is " + returnVal);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			lastDirectory = file.getParentFile();
			log.info("File chosen: " + file.getAbsolutePath());
			
			statusCallback.updateMessage("Opening: " + file.getAbsolutePath());
			
			uploadContext.setFolder(file.getAbsolutePath());
			UploadFiles uploadFiles = new UploadFiles(this, uploadContext, statusCallback);
			statusCallback.updateState(State.UPLOADING);
			new Thread(uploadFiles).start();


		} else {
			log.info("Open command cancelled by user.");
			statusCallback.updateMessage("Open command cancelled by user");
			statusCallback.updateState(State.CANCELLED);
		}

	}

	public void actionPerformed(ActionEvent e) {
/*
		//Handle open button action.
		if (e.getSource() == openButton) {
			int returnVal = fc.showOpenDialog(DicomFileChooser.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				lastDirectory = file.getParentFile();
				log.info("Opening: " + file.getAbsolutePath());
				UploadFiles uploadFiles = new UploadFiles(this, file);
				new Thread(uploadFiles).start();
				//uploadDicomDirectory(file);

				top.setVisible(false);
				top.dispose();


			} else {
				log.info("Open command cancelled by user.");
			}
		}
		else{
			log.info("Other event: " + e + ", from:" + e.getSource() + ", command:" + e.getActionCommand());
		}
		*/
	}

	private File preProcessCCRs(File ccrFolder) throws IOException, XmlException{
		File ccrTempDir = null;
		File tmpDir = null;
		String userHome = System.getProperty("user.home");
		boolean success = false;
		
		if (userHome != null){
			
			tmpDir = new File(userHome);
			boolean canWrite = tmpDir.canWrite();
			if (canWrite){
				 ccrTempDir = new File(tmpDir, "CCR_Upload_ScratchDirectory" + System.currentTimeMillis());
				 success = ccrTempDir.mkdir();
			}
		}
		
		if (success && (ccrTempDir!= null) && (ccrTempDir.exists()) && (ccrTempDir.canWrite())){
			
			log.info("Created temporary directory for storage of CCRs:" + ccrTempDir.getAbsolutePath());
		}
		else{
			File parentDir = ccrFolder.getParentFile();
			ccrTempDir = new File(parentDir, "CCR" + System.currentTimeMillis());
			success = ccrTempDir.mkdir();
			if (success && (ccrTempDir!= null) && (ccrTempDir.exists()) && (ccrTempDir.canWrite())){
				
				log.info("Created temporary directory for storage of CCRs:" + ccrTempDir.getAbsolutePath());
			}
			else{
				throw new IOException("Unable to create temporary directory for storing CCRs:" + ccrTempDir.getAbsolutePath());
			}
		}
			
		
	
		FilenameFileFilter filter = new FilenameFileFilter();
		filter.setFilenameFilter(".xml");
		File files[] = ccrFolder.listFiles(filter);
		
		for (int i=0;i<files.length;i++){
			boolean changed = false;
			File newCCR = new File(ccrTempDir, files[i].getName());
			FileUtils.copyFile(files[i],newCCR);
			ContinuityOfCareRecordDocument ccrDoc = CCRDocumentUtils.parseAndCheckSchemaValidation(newCCR);
			CCRDocumentUtils docUtils = new CCRDocumentUtils();
			List<IDType> ids = docUtils.getPatientIds(ccrDoc);
			boolean mcidPresent = false;
			for (int j=0;j<ids.size();j++){
				
				IDType id = ids.get(j);
				String type = docUtils.getIdType(id);
				if (DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN.equals(type)){
					String idValue = docUtils.getIdValue(id);
					if (!uploadContext.getStorageId().equals(idValue)){
						id.setID(uploadContext.getStorageId());
						changed = true;
						mcidPresent = true;
						log.info("Changed MedCommons ID to " + id.getID());
					}
					
				}
				
			}
			if (!mcidPresent){
				log.info("No MedCommons ID in CCR; adding " + uploadContext.getStorageId());
				String patientActorId = docUtils.getPatientActorId(ccrDoc);
				ActorType patient = docUtils.getActorObject(ccrDoc, patientActorId);
				IDType id = patient.addNewIDs();
				id.setID(uploadContext.getStorageId());
				CodedDescriptionType type = id.addNewType();
				type.setText(DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN);
				
				
			}
			References refs = ccrDoc.getContinuityOfCareRecord().getReferences();
			
			if (refs != null){
				List<ReferenceType> deleteList = new ArrayList<ReferenceType>();
				List<ReferenceType> refList = refs.getReferenceList();
				deleteList = new ArrayList<ReferenceType>();
				for (ReferenceType ref : refList){
					String refType = docUtils.getReferenceType(ref);
					if (DocumentTypes.CCR_CHANGE_HISTORY_MIME_TYPE.equals(refType)){
						deleteList.add(ref);
					}
				}
				if (deleteList.size()>0){
					for (ReferenceType deleteRef: deleteList){
						refList.remove(deleteRef);
						changed = true;
						log.info("Removed CCR History Reference ");
					}
				}
				
			}
			if (changed){
				CCRDocumentUtils.saveCCR(ccrDoc, newCCR);
				log.info("Saved changed CCR:" + newCCR.getAbsolutePath());
				ccrDoc = CCRDocumentUtils.parseAndCheckSchemaValidation(newCCR);
				// Test to make sure that it still passes schema validation after the changes.
				
			}
			log.info("CCR passes schema validation " + newCCR.getAbsolutePath());
			
			
		}
		return(ccrTempDir);
		
	}

	
	
	private class UploadFiles implements Runnable{
		UploadFileChooser uploadFileChooser;
		File uploadFileOrDirectory;
		UploadContext uploadContext;
		UploadFileAgent uploadFileAgent;
		StatusUpdate statusCallback;
		MonitorTransfer monitorTransfer;
		Thread monitorThread = null;
		UploadFiles(UploadFileChooser uploadFileChooser, UploadContext uploadContext, StatusUpdate statusCallback){
			this.uploadFileChooser = uploadFileChooser;
			this.uploadContext = uploadContext;
			this.statusCallback = statusCallback;
			
		}
		public void run(){
			StatusDisplayManager sdm = StatusDisplayManager
				.getStatusDisplayManager();
			File processedCCRs = null;
			int cxpStatus = -1;
			
			
			try{
				File uploadFolder = new File(uploadContext.getFolder());
				log.info("About to upload to " + uploadContext.toString());
				uploadFileAgent = new UploadFileAgent(uploadContext.getCxpEndpoint(),uploadContext.getAuthToken(),uploadContext.getStorageId(),uploadContext.getSenderId(), uploadFolder);
				if (uploadContext.getPaymentBypassToken() != null){
					uploadFileAgent.setPaymentBypass(uploadContext.getPaymentBypassToken());
				}
				if (uploadContext.getMergeCCR() != null){
				    uploadFileAgent.setMergeCCR(uploadContext.getMergeCCR());
				}
				File defaultCCRs = uploadFileAgent.defaultCCRFolder();
				monitorTransfer = new MonitorTransfer(new Job() {
					                    public void cancelJob() { uploadFileAgent.cancelStream(); }
					                    public Long getId() { return -1L; }
								  },uploadFileAgent, statusCallback);
									 
				processedCCRs = preProcessCCRs(defaultCCRs);
				uploadFileAgent.setCCRFolder(processedCCRs);
				uploadFileAgent.startTransactionTimer();
				new Thread(monitorTransfer).start();
				statusCallback.updateMessage("Connecting to server");
				PutResponse response = uploadFileAgent.upload();
				List<RegistryParameters> registryParameters = response
						.getRegistryParameters();
				CXPClient.displayResponseInfo(response);
				cxpStatus = response.getStatus();
				response.getDocinfo();	
				if (cxpStatus==200){
				    statusCallback.updateState(State.FINISHED);
					statusCallback.updateMessage("Uploaded " + response.getDocinfo().size() + " documents");
				}
				else{
				    statusCallback.updateState(State.FAILED);
					statusCallback.updateMessage("Status = " + cxpStatus + ", reason = " + response.getReason());
				}
			}
			catch(Exception e){
				UploadFileChooser.log.error("Error uploading files from " + uploadContext.getFolder(), e);
				log.error("Error uploading files:" + e.getLocalizedMessage());
				if (sdm!=null)
					sdm.setErrorMessage("Error uploading account files", "Upload of files from " + uploadContext.getFolder()  +
							" failed:\n " + e.getLocalizedMessage());
				if (statusCallback != null){
					statusCallback.updateState(State.FAILED);
					statusCallback.updateMessage(e.getLocalizedMessage());
				}
				throw new RuntimeException(e);
			}
			finally{
				if ((processedCCRs !=null) && (processedCCRs.exists())){
					FileUtils.deleteDir(processedCCRs);
				}
				if (monitorTransfer != null){
				    monitorTransfer.exit();
				}
			}
		}
	}
	
}
