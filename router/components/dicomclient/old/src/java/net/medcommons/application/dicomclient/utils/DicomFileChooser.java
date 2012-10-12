package net.medcommons.application.dicomclient.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.dicom.CstoreScp;
import net.medcommons.application.dicomclient.dicom.ImportFiles;
import net.medcommons.application.upload.StatusUpdate;

import org.apache.log4j.Logger;

/*
 * DicomFileChooser.java uses these files:
 *   images/Open16.gif

 */
public class DicomFileChooser extends JPanel {

    private static final long serialVersionUID = 1L;

    private static Logger log = Logger.getLogger(DicomFileChooser.class.getName());

	private static File lastDirectory = null;

	JFileChooser fc;
	JPanel top = null;
	
	public DicomFileChooser() {
		;// Used for JUnit testing only.
	}

	public DicomFileChooser(JPanel top, boolean cdOnly) {
		super(new BorderLayout());
		
		this.top = top;

		// Create a file chooser
		File cdDirectory = null;
		if (!cdOnly) {
			if (lastDirectory != null)
				fc = new JFileChooser(lastDirectory);
			else
				fc = new JFileChooser();
		}
		else {
			cdDirectory = guessCDFileRoot();
			
			if (cdDirectory == null)
				fc = new JFileChooser();
			else
				fc = new JFileChooser(cdDirectory);
			
		}
		log.info("About to show file chooser");
		
        final JFrame frame = new JFrame("DICOM Uploader - Please choose a CD or Folder to Upload");
        frame.setMinimumSize(new Dimension(600,300));
        frame.setBounds(new Rectangle(300,300,600,400));
        frame.setAlwaysOnTop(true);
        frame.setIconImage(StatusDisplayManager.activeImage);
        
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        frame.add(fc);
        
        final DicomFileChooser dfc = this;
        fc.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent action) {
                    if(action.getActionCommand().equals("CancelSelection")) {
                        log.info("Open command cancelled by user: returnVal");
                        frame.setVisible(false);
                        frame.dispose();
                    }
                    else
                    if(action.getActionCommand().equals("ApproveSelection")) {
                        File file = fc.getSelectedFile();
                        lastDirectory = file.getParentFile();
                        log.info("Opening: " + file.getAbsolutePath());
                        UploadFiles uploadFiles = new UploadFiles(dfc, file);
                        new Thread(uploadFiles).start();
                        frame.setVisible(false);
                        frame.dispose();
                    }
                }                
        });
        
        frame.setVisible(true);
        frame.setAlwaysOnTop(false);
	}

    /**
     * Basic rule:
     * 
     * In Mac OS X - just show the /Volumes
     * In Windows - grab D:, E:, or F:.. if they are available.
     * In Linux - just grab the first root folder.
     */
    private File guessCDFileRoot() {
        File cdDirectory;
        cdDirectory = null;
        String osName = System.getProperty("os.name");
        log.info("OS name is:" + osName);
        
        if ("Mac OS X".equals(osName)) {
        	cdDirectory = new File("/Volumes");
        }
        else {
        	
            File [] roots = File.listRoots();
            for(File root : roots) {
                String fname = root.getAbsolutePath();
                log.info("Found root file system: " + fname);
                if(fname.indexOf("C:") == -1 && fname.indexOf("A:") == -1){
                    if(root.exists()) {
                        cdDirectory = root;
                        log.info("Selected root file system " + cdDirectory);
                        break;
                    }
                }
            }
            
        	if(cdDirectory == null && roots.length>0) {
        	    cdDirectory = roots[0];
        	}
        	
            FileSystemView fsv = FileSystemView.getFileSystemView();
            roots = fsv.getRoots();
            File[] files = roots[0].listFiles();
            File start = files[0];
            cdDirectory = start;
        }
        return cdDirectory;
    }

	/**
	 * This is public so that JUnit tests can run this method.
	 * @param uploadFileOrDirectory
	 * @param putInDB
	 */
	public void importDicomDirectory(File uploadFileOrDirectory, boolean putInDB) {

		ContextManager cm = ContextManager.getContextManager();
		StatusUpdate statusUpdate = cm.getStatusUpdate();
		
		StatusDisplayManager sdm = StatusDisplayManager
				.getStatusDisplayManager();

		if (!uploadFileOrDirectory.exists()) {
			String message = "File or directory:\n"
			+ uploadFileOrDirectory.getAbsolutePath()
			+ "\ndoes not exist";
			if (sdm != null){
				sdm.setErrorMessage("DICOM upload failed", message);
			}
			else{
				log.error("DICOM upload failed " + message);
			}
		}

		else {
			
			CstoreScp cstoreScp = cm.getDcmServer();
			ImportFiles importFiles = new ImportFiles();
			
			
			if (statusUpdate != null){
				statusUpdate.updateMessage("Importing DICOM");
			}
			
			try {
				cstoreScp.importDicomFilesInDirectory(importFiles, uploadFileOrDirectory);

				
				int numberOfDicomFiles = importFiles.getFilesImported();
				
				int numberOfFilesSkipped = importFiles.getFilesSkipped();
				if (numberOfDicomFiles < 1){
					sdm.setErrorMessage("No files uploaded", "No DICOM files were found in selection to upload. " +
							"Number of non-DICOM files in directory skipped:" + numberOfFilesSkipped);
					return;
				}
				String message = "Uploading " + numberOfDicomFiles + " DICOM objects from file \n" +
					uploadFileOrDirectory.getAbsolutePath();
				if (sdm != null)
					sdm.setMessage("Uploading DICOM", message);

					log.info(message + "\nNon-DICOM files skipped:" + numberOfFilesSkipped);
				
				
				if (statusUpdate != null){
					statusUpdate.updateMessage("DICOM Import complete");
				}
			} catch (IOException e) {
				log.error(e.getLocalizedMessage(), e);

			} finally {
				;

			}

		}
	}
	private class UploadFiles implements Runnable{
		DicomFileChooser d =null;
		File uploadFileOrDirectory;
		UploadFiles(DicomFileChooser d, File uploadFileOrDirectory){
			this.d = d;
			this.uploadFileOrDirectory = uploadFileOrDirectory;

		}
		public void run(){
			StatusDisplayManager sdm = StatusDisplayManager
				.getStatusDisplayManager();
			try{
				d.importDicomDirectory(uploadFileOrDirectory,true);
			}
			catch(Exception e){
				log.error("Error uploading DICOM from " + uploadFileOrDirectory.getAbsolutePath(), e);
				if (sdm!=null)
					sdm.setErrorMessage("Error uploading DICOM", "Upload of DICOM from " + uploadFileOrDirectory.getAbsolutePath()  +
							" failed:\n " + e.getLocalizedMessage());
			}
		}
	}

}
