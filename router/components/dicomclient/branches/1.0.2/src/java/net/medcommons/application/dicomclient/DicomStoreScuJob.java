package net.medcommons.application.dicomclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.medcommons.application.dicomclient.dicom.CstoreScu;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.Commands;
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.utils.DicomOutputTransaction;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.DirectoryUtils;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.modules.utils.FileUtils;
import net.sourceforge.pbeans.Store;

import org.apache.log4j.Logger;

/**
 * Exports DICOM linked to a {@link DicomOutputTransaction} to 
 * a target, either the file system or a DICOM SCU via CSTORE.
 * <p>
 * When the export is complete, sets the status of the {@link DicomOutputTransaction}
 * to STATUS_COMPLETE.
 * 
 * @author ssadedin
 */
public class DicomStoreScuJob  implements Runnable,Job {
     private static Logger log = Logger.getLogger(DicomStoreScuJob.class
                .getName());
    private Long id = null;
    private CstoreScu scu = null;
    private DicomOutputTransaction transaction = null;
    private Store db = DB.get();

    public DicomStoreScuJob(DicomOutputTransaction transaction){

        this.transaction = transaction;
        this.id = transaction.getId();
    }
    public Long getId(){
        return(id);
    }

    public void run(){
    	boolean success = false;
        try{
            List<File> dicomFiles = new ArrayList<File>();
            File transactionFolder = new File(transaction.getTransactionFolder());
            if (!transactionFolder.exists()){
                // Need to set, save transaction state here.
                throw new FileNotFoundException(transactionFolder.getAbsolutePath());
            }

            // To do: Populate dicomFiles here.
            dicomFiles = FileUtils.getFilesInDirectory(transactionFolder, ".dcm");
            transaction.setStatus(DicomTransaction.STATUS_ACTIVE);
            transaction.setTimeStarted(System.currentTimeMillis());
            db.save(transaction);
            
            log.info("Starting transaction " + transaction);
            String exportMethod = transaction.getExportMethod();
            if (exportMethod.equalsIgnoreCase(Commands.EXPORT_METHOD_CSTORE))
                exportViaCSTORE(dicomFiles);
            else 
            if(exportMethod.equalsIgnoreCase(Commands.EXPORT_METHOD_FILE))
                exportViaFILE(dicomFiles);
            else {
                throw new IllegalStateException("Unknown export method:" + exportMethod);
            }
            
            if(transaction.getStatus().equals(DicomTransaction.STATUS_ACTIVE))
                transaction.setStatus(DicomTransaction.STATUS_COMPLETE);
            
            FileUtils.deleteDir(transactionFolder);
            success = true;
        }
        catch(FileNotFoundException e){
        	log.error("Error", e);
        	transaction.setStatus(DicomOutputTransaction.STATUS_PERMANENT_ERROR);
        	transaction.setStatusMessage(e.getLocalizedMessage());
        }
        catch(IOException e){
        	log.error("I/O Error", e);
        	transaction.setStatus(DicomOutputTransaction.STATUS_PERMANENT_ERROR);
        	transaction.setStatusMessage(e.getLocalizedMessage());
        }
        catch(Exception e){
        	log.error("Error", e);
        	transaction.setStatus(DicomOutputTransaction.STATUS_PERMANENT_ERROR);
        	transaction.setStatusMessage(e.getLocalizedMessage());

        }
        finally {
        	db.save(transaction);
        	if (!success){
        		StatusDisplayManager.get().setErrorMessage("Error exporting data",
        				transaction.getStatusMessage(),
        				transaction.getDashboardStatusId());
        	}
        }

    }
    public void cancelJob(){
        if (scu != null){
            if (transaction.getStatus().equals(DicomOutputTransaction.STATUS_ACTIVE)){
                scu.cancelTransfer();

            }
        }
        else{
            ;// scu can be null because its just a file copy.
        }
    }
     private void exportViaFILE(List<File> dicomFiles) throws IOException{
             String exportFolder = transaction.getExportFolder();
             if (exportFolder == null){
            	 File exportDirectory = ContextManager.get().getConfigurations().getExportDirectory();
            	 if (exportDirectory == null){
            		 File baseDir = ContextManager.get().getConfigurations().getBaseDirectory();
            		 exportDirectory = new File(baseDir, "Export");
            		 if (!exportDirectory.exists()){
            			 exportDirectory.mkdir();
            			 exportFolder = exportDirectory.getAbsolutePath();
            		 }
            	 }
             }
             

            if ((exportFolder == null) || ("".equals(exportFolder))){
                throw new NullPointerException("ExportDirectory not defined: please enter configuration  on about page");
            }
            File directory = new File(exportFolder);
            if (!directory.exists()){
                log.info("Export directory " + directory.getAbsolutePath() + " does not exist, attempting to create.");
                DirectoryUtils.makeDirectory(directory);

            }
            File outputDir = null;
            String dirname = transaction.getPatientIdType();
            /*
            if((dirname != null) && (!"".equals(dirname))){
            	dirname = sanitize(dirname);

                outputDir = new File(directory, dirname);
                DirectoryUtils.makeDirectory(outputDir);

                dirname = transaction.getPatientId();
                if((dirname != null) && (!"".equals(dirname))) {
                    outputDir = new File(outputDir, dirname);
                    DirectoryUtils.makeDirectory(outputDir);
                }
            }
            */
            
            if (outputDir == null){
                dirname = sanitize(transaction.getPatientName());
                outputDir = new File(directory, dirname);
                int i=1;
                while(outputDir.exists()) {
                    outputDir = new File(directory, dirname+"_"+i);
                    ++i;
                }
                DirectoryUtils.makeDirectory(outputDir);
            }


            Iterator<File> iter = dicomFiles.iterator();
            while(iter.hasNext()){
                File input = iter.next();
                String fname = input.getName();
                File output = new File(outputDir, fname); // Series? Need to think about this.
                if (log.isDebugEnabled()){
                	log.debug("Copying file " + input.getAbsolutePath() + " => " + output.getAbsolutePath());
                }
                FileUtils.copyFile(input, output);
                transaction.setBytesTransferred(transaction.getBytesTransferred() + input.length());

            }
            StatusDisplayManager.get().setMessage(
                    "Image File Export Complete",
                    "Saved " + dicomFiles.size()
                            + " images  to \n" + outputDir.getAbsolutePath(),
                            null);
                            // transaction.getDashboardStatusId());
            db.save(transaction);
            deleteDicomFiles(dicomFiles);
        }
     
     private String sanitize(String dirname) {
         return dirname
             .trim()
             .replace("/", "_")
             .replace(" ", "_")
             .replace("^", "_")
             .replace("@", "_")
             .replace(",", "_");
     }

        private void exportViaCSTORE(List<File> dicomFiles) throws IOException{
            
            scu = new CstoreScu();
            scu.setDicomOutputTransaction(transaction);
            String remoteAeTitle = transaction.getDicomRemoteAeTitle();

            String remoteHost = transaction.getDicomRemoteHost();
            int remotePort = transaction.getDicomRemotePort();
            scu.setRemoteHost(remoteHost);
            scu.setRemotePort(remotePort);
            scu.setCalledAET(remoteAeTitle);
            scu.setCalling(transaction.getDicomLocalAeTitle());

            for (int i = 0; i < dicomFiles.size(); i++) {
                scu.addFile(dicomFiles.get(i));
            }
            scu.configureTransferCapability();

            try {
                scu.open();
            } catch (Exception e) {
                log.error("ERROR: Failed to establish association:"
                        + e);
                StatusDisplayManager.get().setErrorMessage(
                        "DICOM Communication Error - failed to establish DICOM association:",
                        e.getLocalizedMessage()
                                + "\nFiles not sent to DICOM CSTORE target\n" +
                                transaction.toString(),
                                transaction.getDashboardStatusId());
                transaction.setStatus(DicomTransaction.STATUS_TEMPORARY_ERROR);
                transaction.setStatusMessage(e.toString());
                db.save(transaction);
                return;
            }

            log.info("Connected to " + remoteAeTitle);

            transaction = scu.send();
            log.info("scu.send() status is " + transaction.getStatus());
            if (transaction.getStatus().equals(DicomOutputTransaction.STATUS_COMPLETE)){
            	String message ="Sent " + dicomFiles.size() +
                	" images  to " + remoteHost;
            	log.info(message);
	            StatusDisplayManager.get().setMessage("DICOM Transfer Complete", message, transaction.getDashboardStatusId());


	            deleteDicomFiles(dicomFiles);
            }
            else{

            	StatusDisplayManager.get()
                .setErrorMessage(
                        "DICOM Transfer Failed",
                        transaction.getStatusMessage(),
                        transaction.getDashboardStatusId());

            }



        }
        void deleteDicomFiles(List<File>dicomFiles) throws IOException{
        	if (dicomFiles.size() == 0){
        		log.error("No DICOM files to delete");
        		return;
        	}
            Iterator<File>iter = dicomFiles.iterator();
            File parentDirectory = null;
            while(iter.hasNext()){
                File f = iter.next();
                if (parentDirectory == null){
                    parentDirectory = f.getParentFile();
                }
                boolean success = f.delete();
                if (!success)
                    throw new IOException("Temporary file not deleted:" + f.getAbsolutePath());
            }

            String filesRemaining[] = parentDirectory.list();
            if ((filesRemaining.length == 0) || (filesRemaining.length == 1)){
                // Delete the directory with the CCR
                deleteDir(parentDirectory);
            }


        }
        private boolean deleteDir(File dir) {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }

            // The directory is now empty so delete it
            return dir.delete();
        }
       

}
