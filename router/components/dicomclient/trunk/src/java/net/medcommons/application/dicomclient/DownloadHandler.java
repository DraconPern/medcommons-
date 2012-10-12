package net.medcommons.application.dicomclient;

import static net.medcommons.application.dicomclient.utils.Params.where;
import static net.medcommons.modules.utils.Str.blank;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.medcommons.application.dicomclient.dicom.CstoreScu;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.CxpDownload;
import net.medcommons.application.dicomclient.transactions.DownloadQueue;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.utils.DicomOutputTransaction;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.Params;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.utils.DashboardMessageGenerator.MessageType;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.utils.Str;
import net.sourceforge.pbeans.Store;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.cxp2.soap.CXPService;

import astmOrgCCR.ReferenceType;

public class DownloadHandler {

    private static Logger log = Logger.getLogger(DownloadHandler.class.getName());

    protected CXPService service = null;

    protected SHA1 sha1 = null;


    private  File cacheFolder; // Where parent folder where cxp data is written.

    List<CxpDownload> currentDownloads;


    private String currentStorageId = "-1";

    private static DownloadHandler downloadHandler = null;
    private DownloadCCR downloadCCR;
    private ExportDicom  exportDicom;
    private ProcessDownloadQueue processDownloadQueue;

    public static DownloadHandler InitFactory() {
        if (downloadHandler == null) {
        	File f = ContextManager.get().getDownloadCache();

            new DownloadHandler(f);
        }

        return (downloadHandler);
    }

    public static DownloadHandler Factory() {
        if (downloadHandler != null) {
            return (downloadHandler);
        } else
            throw new NullPointerException(
                    "Download Handler not yet initialized");
    }

    private DownloadHandler(File cacheFolder) {
        currentDownloads = new ArrayList<CxpDownload>();
        this.cacheFolder = cacheFolder;
        if (this.cacheFolder == null){
        	throw new RuntimeException("Unable to initialize DownloadHandler with null upload cache folder");
        }
        downloadCCR = new DownloadCCR();
        exportDicom = new ExportDicom();
        processDownloadQueue = new ProcessDownloadQueue();

        Thread downloadCCRThread = new Thread(downloadCCR);
        downloadCCRThread.setName("DownloadCCRThread");
        downloadCCRThread.start();

        Thread exportDicomThread = new Thread(exportDicom);
        exportDicomThread.setName("ExportDicomThread");
        exportDicomThread.start();


        Thread processDownloadThread = new Thread(processDownloadQueue);
        processDownloadThread.setName("Process Download Event Queue");
        processDownloadThread.start();
        downloadHandler = this;


    }
    
    /**
     * Creates and saves a download transcation for the content specified in the
     * given contextState.  The ContextState must be a persisted instance including
     * all the required parameters - storageId, auth, guid, host, etc.
     * 
     * @param downloadReferences
     * @param contextState
     */
    public void queueDownload(boolean downloadReferences, ContextState contextState) throws MalformedURLException {
        
        if(contextState.getId() == null) {
            throw new IllegalArgumentException("Context state must be a persistent instance");
        }
        if (blank(contextState.getStorageId())) {
            throw new IllegalArgumentException("storageid must not be blank or null");
        }
        if (blank(contextState.getAuth())) {
            throw new IllegalArgumentException("auth must not be blank or null");
        }
        if (blank(contextState.getGuid())) {
            throw new IllegalArgumentException("guid must not be blank or null");
        }
        if (blank(contextState.getCxpHost())) {
            throw new IllegalArgumentException("cxphost must not be blank or null");
        }
        if (blank(contextState.getAccountId())) {
            throw new IllegalArgumentException("accountid must not be blank or null");
        }

        Store db = DB.get();
        DownloadQueue downloadQueue = new DownloadQueue();
        downloadQueue.setCreationTime(new Date());
        downloadQueue.setContextStateId(contextState.getId());
      
        if (downloadReferences){

            // Download CCR and its references
             StatusDisplayManager.get().setMessage(
                     "Download DICOM",
                     "Request received to download DICOM");
             StatusDisplayManager.get().sendDashboardMessage(MessageType.INFO, null, "Downloading Patient");
             downloadQueue.setAttachments(DownloadQueue.ATTACHMENTS_ALL);

        }
        else{
            // Add DICOM case.
        	 downloadQueue.setAttachments(DownloadQueue.ATTACHMENTS_NONE);
        }
        db.save(downloadQueue);
        log.info("Saved DownloadQueue object for "
        		+ downloadQueue.getId() + ":" + contextState.getStorageId() + ","
        		+ contextState.getGuid() + ", " + downloadQueue.getAttachments());
    }
    
    /*
     *   CXPDownload download = new CXPDownload(contextManager,
                    downloadReferences);

            currentDownloads.add(download);
            Thread t = new Thread(download);
            t.start();
     */
   
    public void cancelCurrentJob(CxpDownload transaction){

    }
    public void addCurrentJob(CxpDownload downloadJob){

    }
    public void removeCurrentJob(CxpDownload downloadJob){
    	currentDownloads.remove(downloadJob);
    }

    public void setCurrentStorageId(String currentStorageId){
        this.currentStorageId = currentStorageId;
        log.info("Setting currentStorageId to " + currentStorageId);
    }

    public String getCurrentStorageId(){
        return(this.currentStorageId);
    }

    /**
     * Spawns new CxpDownload thread from entries in the DownloadQueue
     * table.
     * @author mesozoic
     *
     */
    private class ProcessDownloadQueue implements Runnable{

    	boolean running = true;
    	long timeOut = 2 * 1000;
    	public void run(){
    		DownloadQueue queuedJob = null;
    		while(running){
    			  try{
                      Thread.sleep(timeOut);
                      
                      Store s = DB.get();

                      // Should sort by time? Priority?
                      List<DownloadQueue> queuedJobs = s.select(DownloadQueue.class).all();

                      if (queuedJobs.size()>0){
                    	  log.info("Number of DownloadQueue objects in queue:" + queuedJobs.size());
                    	  queuedJob = queuedJobs.get(0);
                    	  final Long contextStateId = queuedJob.getContextStateId();
                    	  ContextState contextState = s.selectSingle(ContextState.class, "id", contextStateId);
                    	  if(contextState == null) 
                    	      throw new NullPointerException("Context state missing from DownloadQueue:" + queuedJob);
                    	  
                    	  log.info("Retrieved DownloadQueue object: "
                          		+ queuedJob.getId() + ":" + contextState.getStorageId() + ","
                          		+ contextState.getGuid() + ", " + queuedJob.getAttachments());
                    	  
                    	  CxpDownload downloadJob = new CxpDownload(queuedJob);
                    	  downloadJob.run();
                    	  
                    	  s.delete(queuedJob);
                       }

                 }
                 catch(Exception e) {
                    log.error("Error downloading", e);
                     StatusDisplayManager.get().setErrorMessage("Error downloading", e.toString());
                 }
             }
         }
    }
    
    /**
     * Queries for {@link DicomOutputTransaction} objects in STATUS_QUEUED state.
     * When it finds one, creates a {@link DicomStoreScuJob} to send it to a local
     * DICOM SCU.
     * 
     * @author ssadedin
     */
    private class ExportDicom implements Runnable {
        long timeOut = 2 * 1000;

        boolean running = true;

        public void run() {
            DicomOutputTransaction trans = null;
            JobHandler jobHandler = JobHandler.JobHandlerFactory();
            Store s = DB.get();

            while (running) {
                try {
                    Thread.sleep(timeOut);

                    // Should sort by time? Priority?
                    List<DicomOutputTransaction> queuedJobs = 
                        s.select(DicomOutputTransaction.class, where("status",DicomOutputTransaction.STATUS_QUEUED))
                         .all();    
                    
                    for (int i=0;i<queuedJobs.size(); i++){
                        log.info(" dicom " + queuedJobs.get(i).toString());
                    }
                    
                    if(queuedJobs.size() > 0) {
                        trans = queuedJobs.get(0);
                        DicomStoreScuJob dicomJob = new DicomStoreScuJob(trans);
                        Thread dicomThread = new Thread(dicomJob);
                        jobHandler.addDicomJob(dicomJob);
                        try{
                            dicomThread.run();
                        }
                        finally{
                            jobHandler.deleteDicomJob(dicomJob.getId());
                        }
                    }
                } 
                catch (Exception e) {
                    String message = "Error exporting DICOM ";
                    log.error(message, e);
                    synchronized(trans) {
                        s.requestLock(trans);
                        try {
		                    trans.setStatus(CxpTransaction.STATUS_PERMANENT_ERROR);
		                    trans.setStatusMessage(message);
		                    s.save(trans);
                        }
                        finally {
                            s.relinquishLock();
                        }
                    }
                    StatusDisplayManager.get()
                            .setErrorMessage("Error exporting files", message, trans.getDashboardStatusId());
                    return;

                }
            }
        }
    }

    /*
     * // This should be a separate thread seeing what is queued for transfer.
     *
     */


    /**
     * Performs a DICOM echo on the configured remote
     * DICOM device. Useful for testing configurations.
     */
    public String echo(boolean showStatus) {

        String response = null;

        String hostAddress = "Unknown";
        String remoteHost = "Unknown";
        String remoteAeTitle = "Unknown";
        String localAeTitle = "Unknown";
        int remotePort = Integer.MIN_VALUE;
        boolean success = false;
        try {
            Configurations configurations = ContextManager.get().getConfigurations();
            DicomOutputTransaction outputTransaction = createOutputTransaction();
            remoteAeTitle = outputTransaction.getDicomRemoteAeTitle();
            remoteHost = outputTransaction.getDicomRemoteHost();
            remotePort = outputTransaction.getDicomRemotePort();
            localAeTitle = outputTransaction.getDicomLocalAeTitle();
            InetAddress addr = InetAddress.getLocalHost();
            hostAddress = addr.getHostAddress();

            CstoreScu scu = new CstoreScu();

            scu.setRemoteHost(remoteHost);
            scu.setRemotePort(remotePort);
            scu.setCalledAET(remoteAeTitle);
            scu.setCalling(outputTransaction.getDicomLocalAeTitle());

            scu.configureTransferCapability();

            try {
                scu.open();
                success = true;
            }
            catch (Exception e) {

                response = "Failed attempting DICOM association to device at host:"
                    + remoteHost + ", port:" + remotePort + ", AE title:"
                    + remoteAeTitle + "\nfrom\n host:" + hostAddress
                    + ", AETitle: " + configurations.getDicomLocalAeTitle();
                log.error(response + ":" + e.getLocalizedMessage());
                if (showStatus)
                    StatusDisplayManager.get().setErrorMessage(
                            "DICOM Echo Failed", response);
            }
            if (success){
                log.info("Connected to " + remoteAeTitle);

                scu.echo();
                response = "Successfully sent DICOM echo to target device at\n host: "
                        + remoteHost
                        + ", port:"
                        + remotePort
                        + ", AETitle:"
                        + remoteAeTitle
                        + "\nfrom\n host: "
                        + hostAddress
                        + ", AETitle: " + configurations.getDicomLocalAeTitle();
                if (showStatus)
                    StatusDisplayManager.get().setMessage(
                            "DICOM Echo Success", response);
            }
        } catch (Exception e) {
            response = "Failed attempting DICOM echo to device at host:"
                    + remoteHost + ", port:" + remotePort + ", AE title:"
                    + remoteAeTitle + "\nfrom\n host:" + hostAddress
                    + ", AETitle: " + localAeTitle;
            if (showStatus)
                StatusDisplayManager.get().setErrorMessage(
                        "DICOM Echo Failed", response);
            log.error(response + ":" + e.getLocalizedMessage());
        }
        return (response);

    }


    //x:Locations/x:Location/x:Description/x:ObjectAttribute[x:Attribute='URL']/x:AttributeValue/x:Value
    /*
     * String queryExpression =
     "declare namespace xq='http://xmlbeans.apache.org/samples/xquery/employees';" +
     "$this/xq:employees/xq:employee/xq:phone[contains(., '(206)')]";
     */
    String getMcidBroken(ReferenceType reference) {
        String queryExpression = "declare namespace x='urn:astm-org:CCR';"
                + "$this/x:Locations/x:Location/x:Description/x:ObjectAttribute[x:Attribute='URL']/x:AttributeValue/x:Value";
        XmlObject[] mcids = reference.selectPath(queryExpression);
        for (int i = 0; i < mcids.length; i++) {
            log.info(" mcid should be in here.." + mcids[i].toString());
        }
        String val = "blah";
        return (val);
    }



    public static DicomOutputTransaction createOutputTransaction(){
        Configurations configurations = ContextManager.get().getConfigurations();
        DicomOutputTransaction outputTransaction = new DicomOutputTransaction();
        outputTransaction.setDicomRemoteAeTitle(configurations.getDicomRemoteAeTitle()) ;
        outputTransaction.setDicomRemoteHost(configurations.getDicomRemoteHost());
        outputTransaction.setDicomRemotePort(configurations.getDicomRemotePort());
        outputTransaction.setDicomLocalAeTitle(configurations.getDicomLocalAeTitle());
        outputTransaction.setExportMethod(configurations.getExportMethod());
        outputTransaction.setStatus(DicomOutputTransaction.STATUS_WAITING_FOR_DOWNLOAD);
        outputTransaction.setCxpJob(DicomTransaction.UNITIALIZED_CXPJOB);
        return(outputTransaction);
    }

}
