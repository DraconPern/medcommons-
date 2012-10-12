package net.medcommons.application.dicomclient.utils;

import static net.medcommons.application.utils.Str.blank;
import groovy.lang.Closure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.dicom.CstoreScp;
import net.medcommons.application.dicomclient.dicom.ImportFiles;
import net.medcommons.application.upload.StatusUpdate;
import net.medcommons.modules.crypto.SHA1;

import org.apache.log4j.Logger;
import org.json.JSONArray;

/*
 * DicomFileChooser.java uses these files:
 *   images/Open16.gif

 */
public class DicomFileChooser extends JPanel {

    private static final long serialVersionUID = 1L;
    
    /**
     * Maximum number of imports that will be cached in memory
     */
    private static final int MAX_CACHED_IMPORTS = 3;

    private static Logger log = Logger.getLogger(DicomFileChooser.class.getName());

    private static File lastDirectory = null;
    
    private static List<DICOMImportStatus> imports = new ArrayList<DICOMImportStatus>();

    private DICOMImportStatus status = new DICOMImportStatus();
    
    private boolean threaded = true;
    
    private boolean withStatus = true;
    
    JFileChooser fc;
    JPanel top = null;
    
    public DicomFileChooser() {
        ;// Used for JUnit testing only.
    }

    public DicomFileChooser(JPanel top, boolean cdOnly) {
        show(top,cdOnly, new Closure(this) {
            public void doCall(File f) {
                uploadFiles(f);
            }
        });
    }
    
    public DicomFileChooser(JPanel top, boolean cdOnly, final Closure successHandler) {
        this(top,cdOnly,successHandler, true, true);
    }
    
    public DicomFileChooser(JPanel top, boolean cdOnly, final Closure successHandler, boolean threaded, boolean withStatus) {
        super(new BorderLayout());
        this.threaded = threaded;
        this.withStatus = withStatus;
        show(top, cdOnly, successHandler);
    }

    private void show(JPanel top, boolean cdOnly, final Closure successHandler) {
        this.top = top;

        if(withStatus) {
            synchronized (imports) {
                imports.add(status);
            }
        }
        
        fc = new JFileChooser();
                
        File dir = null;
            
        // Create a file chooser
        File cdDirectory = null;
        if (!cdOnly) {
            if (lastDirectory != null)
                dir = lastDirectory;
        }
        else {
            cdDirectory = guessCDFileRoot();
            if (cdDirectory != null)
                dir = cdDirectory;
        }
        log.info("About to show file chooser");
        
        final JFrame frame = new JFrame("DICOM Uploader - Please choose a CD or Folder to Upload");
        frame.setMinimumSize(new Dimension(600,300));
        frame.setBounds(new Rectangle(300,300,600,400));
        frame.setAlwaysOnTop(true);
        frame.setIconImage(StatusDisplayManager.activeImage);
        
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        frame.add(fc);
        
        fc.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent action) {
                    if(action.getActionCommand().equals("CancelSelection")) {
                        log.info("Open command cancelled by user: returnVal");
                        frame.setVisible(false);
                        frame.dispose();
                        status.setStatus("Cancelled");
                        successHandler.setDelegate(this);
                        successHandler.call(new Object[] { null, status });
                    }
                    else
                    if(action.getActionCommand().equals("ApproveSelection")) {
                        final File file = fc.getSelectedFile();
                        lastDirectory = file.getParentFile();
                        log.info("Opening: " + file.getAbsolutePath());
                        if(threaded) {
	                        new Thread("DICOMFileChooserAction") {
	                            public void run() {
	                                successHandler.setDelegate(this);
			                        successHandler.call(new Object[] { file, status });
	                            }
	                        }.start();
                        }
                        else {
                            successHandler.setDelegate(this);
                            successHandler.call(new Object[] { file, status });
                        }
                        frame.setVisible(false);
                        frame.dispose();
                    }
                }
        });
        
        status.setStatus("Choosing");
        
        frame.setVisible(true);
        
        if(dir!=null) {
            fc.setCurrentDirectory(dir);
        }
        
        frame.setAlwaysOnTop(false);
    }
    
    

    /**
     * Spawn a thread to upload files in the given directory
     * 
     * @param uploadFileOrDirectory
     */
    private void uploadFiles(final File uploadFileOrDirectory) {
        
        status.setStatus("Importing");
        
        try {
            importDicomDirectory(uploadFileOrDirectory);
        }
        catch(Exception e) {
            log.error("Error uploading DICOM from " + uploadFileOrDirectory.getAbsolutePath(), e);
            StatusDisplayManager sdm = StatusDisplayManager.get();
            if (sdm!=null)
                sdm.setErrorMessage("Error uploading DICOM", 
                        "Upload of DICOM from " + uploadFileOrDirectory.getAbsolutePath()  +
                        " failed:\n " + e.getLocalizedMessage());
        }
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
     * Validates directory and passes it to CStoreSCP instance
     * to import files.
     * 
     * @param uploadFileOrDirectory
     */
    public void importDicomDirectory(File uploadFileOrDirectory) {

        ContextManager cm = ContextManager.get();
        StatusUpdate statusUpdate = cm.getStatusUpdate();
        
        StatusDisplayManager sdm = StatusDisplayManager.get();
        
        if(!uploadFileOrDirectory.exists()) {
            String message =   "File or directory:\n" + uploadFileOrDirectory.getAbsolutePath()
                             + "\ndoes not exist";
            if(sdm != null) {
                sdm.setErrorMessage("DICOM upload failed", message);
                log.error("DICOM upload failed " + message);
                status.setStatus("Error");
            }
            return;
        }

        CstoreScp cstoreScp = cm.getDcmServer();
        ImportFiles importFiles = new ImportFiles();
        
        status.setStatus("Importing");
        if(statusUpdate != null) {
            statusUpdate.updateMessage("Importing DICOM");
        }
        
        try {
            
            SHA1 sha1 = new SHA1();
            sha1.initializeHashStreamCalculation();
            String transferKey = sha1.calculateStringHash(System.currentTimeMillis() + uploadFileOrDirectory.getAbsolutePath());
            log.info("Allocated transfer key " + transferKey + " for import operation.");
            status.setTransferKey(transferKey);
 
            cstoreScp.importDicomFilesInDirectory(status, importFiles, uploadFileOrDirectory);
            
            if("Cancelled".equals(status.getStatus())) {
                log.info("Import of folder " + uploadFileOrDirectory + " Cancelled");
                sdm.setMessage("Transfer Cancelled", "Import of files from " + uploadFileOrDirectory.getAbsolutePath() + " was cancelled.");
                return;
            }
            
            int numberOfDicomFiles = importFiles.getFilesImported();
            int numberOfFilesSkipped = importFiles.getFilesSkipped();
            if(numberOfDicomFiles < 1) {
                sdm.setErrorMessage("No files uploaded", "No files were found in selection to upload. " +
                        numberOfFilesSkipped + " files were skipped.");
                return;
            }
            
            status.setStatus("Uploading");
            
            String message = "Uploading " + numberOfDicomFiles + " DICOM objects from file \n" +
	            uploadFileOrDirectory.getAbsolutePath();
            if (sdm != null)
                sdm.setMessage("Uploading DICOM", message);
            
            log.info(message + "\nNon-DICOM files skipped:" + numberOfFilesSkipped);
            
            
            if (statusUpdate != null){
                statusUpdate.updateMessage("DICOM Import complete");
            }
        }
        catch(IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
            
    }
    
    public static List<DICOMImportStatus> getImports() {
        List<DICOMImportStatus> results = new ArrayList<DICOMImportStatus>();
        synchronized(imports) {
	        for(DICOMImportStatus s : imports) {
	            results.add(s.clone());
	        }
        }
        return results;
    }

    public static JSONArray getImportsJSON() {
        JSONArray results = new JSONArray();
        synchronized(imports) {
            int n = 0;
            for(DICOMImportStatus s : imports) {
                results.put(n++,s.toJSON());
            }
        }
        return results;
    }
    
    /**
     * Remove all except the most recent imports so that 
     * they do not grow without bound.  Note that care needs
     * to be taken about when this is invoked, as the web page
     * uses the count of imports as a signal to know that there 
     * is a new import.  Hence this should happen at the end of
     * an upload, not near the beginning.
     */
    public static void trimImports() {
        synchronized(imports) {
            while(imports.size()>MAX_CACHED_IMPORTS) {
                imports.remove(0);
            }
        }
    }
    
    public static void cancel(String transferKey) {
        synchronized(imports) {
            for(DICOMImportStatus s : imports) {
                if(transferKey.equals(s.getTransferKey())) {
                    s.setStatus("Cancelled");
                    break;
                }
            }
        }
    }
    
    public static void add(DICOMImportStatus status) {
        synchronized(imports) {
            for(DICOMImportStatus s : imports) {
                if(status.getTransferKey().equals(s.getTransferKey())) {
                    throw new IllegalArgumentException("Transfer key already registered");
                }
            }
            
            // Not found
            imports.add(status);
        }
    }

    public static boolean isCancelled(String transferKey) {
        if(blank(transferKey))
            return false;
        
        synchronized(imports) {
            for(DICOMImportStatus s : imports) {
                if(transferKey.equals(s.getTransferKey())) {
                    return "Cancelled".equals(s.getStatus());
                }
            }
        }
        return false;
    }

    public DICOMImportStatus getStatus() {
        return status;
    }
}
