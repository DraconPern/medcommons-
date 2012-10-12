package net.medcommons.application.dicomclient;

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
import net.medcommons.application.dicomclient.utils.DicomOutputTransaction;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.LocalHibernateUtil;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.utils.DashboardMessageGenerator.MessageType;
import net.medcommons.modules.crypto.SHA1;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.cxp2.soap.CXPService;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import astmOrgCCR.ReferenceType;

public class DownloadHandler {
    //private final static String CCR_NAMESPACE = "urn:astm-org:CCR";

    private static Logger log = Logger.getLogger(DownloadHandler.class
            .getName());

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
        	File f = ContextManager.getContextManager().getDownloadCache();

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
 * Call context manager
    public static File getCacheFolder(){
    	return(cacheFolder);
    }
**/
    public void queueDownload(boolean downloadReferences, ContextState contextState) throws MalformedURLException {
        //ContextManager contextManager = ContextManager.getContextManager();

        DownloadQueue downloadQueue = new DownloadQueue();
        
        downloadQueue.setCreationTime(new Date());
        Configurations configurations = ContextManager.getContextManager().getConfigurations();
        downloadQueue.setContextStateId(contextState.getId());
        String gatewayRoot = contextState.getGatewayRoot();
       
      
        if (downloadReferences){

            // Download CCR and its references
             StatusDisplayManager.getStatusDisplayManager().setMessage(
                     "Download DICOM",
                     "Request received to download DICOM");
             StatusDisplayManager.getStatusDisplayManager().sendDashboardMessage(MessageType.INFO, null, "Downloading Patient");
             downloadQueue.setAttachments(DownloadQueue.ATTACHMENTS_ALL);

        }
        else{
            // Add DICOM case.
        	 downloadQueue.setAttachments(DownloadQueue.ATTACHMENTS_NONE);
        }
        downloadQueue = TransactionUtils.saveTransaction(downloadQueue);
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
                      Session session = LocalHibernateUtil.currentSession();

                      // Select a queued job.
                      Criteria queued = session.createCriteria(DownloadQueue.class);


                      // Should sort by time? Priority?
                      List<DownloadQueue> queuedJobs = queued.list();

                      if (queuedJobs.size()>0){
                    	  log.info("Number of DownloadQueue objects in queue:" + queuedJobs.size());
                    	  queuedJob = queuedJobs.get(0);
                    	  Long contextStateId = queuedJob.getContextStateId();
                    	  ContextState contextState = TransactionUtils.getContextState(contextStateId);
                    	  if (contextState == null){
                    	      throw new NullPointerException("Context state missing from DownloadQueue:" + queuedJob);
                    	  }
                    	  log.info("Retrieved DownloadQueue object: "
                          		+ queuedJob.getId() + ":" + contextState.getStorageId() + ","
                          		+ contextState.getGuid() + ", " + queuedJob.getAttachments());
                    	  CxpDownload downloadJob = new CxpDownload(queuedJob);
                    	  downloadJob.run();
                    	  TransactionUtils.delete(queuedJob);
                       }

                 }
                 catch(Exception e){
                    log.error("Error downloading", e);
                     StatusDisplayManager.getStatusDisplayManager().setErrorMessage("Error downloading", e.toString());
                 }
             }
         }
    }
    /**
     * This study downloads a CXP job one at a time from the queue.
     *
     * @author mesozoic
     *
     */
    private class DownloadCCR implements Runnable{
         long timeOut = 2 * 1000;

         boolean running = true;

         public void run(){
             CxpTransaction trans = null;
             JobHandler jobHandler = JobHandler.JobHandlerFactory();

             while(running){
                 try{
                      Thread.sleep(timeOut);
                      Session session = LocalHibernateUtil.currentSession();

                      // Select a queued job.
                      Criteria queued = session
                              .createCriteria(CxpTransaction.class);

                      queued.add(Expression.eq("status", CxpTransaction.STATUS_QUEUED));
                      queued.add(Expression.eq("transactionType", "GET"));
                      // Should sort by time? Priority?
                      List<CxpTransaction> queuedJobs = queued.list();
                      //log.info("Number of CXP transactions to start:" + queuedJobs.size());
                      if (queuedJobs.size()>0){
                          trans = queuedJobs.get(0);


                          Criteria matchingDicomQuery = session.createCriteria(DicomOutputTransaction.class);
                          matchingDicomQuery.add(Expression.eq("cxpJob", trans.getId()));
                          List<DicomOutputTransaction> dicomTransactions = matchingDicomQuery.list();
                          if ((dicomTransactions!= null) && (dicomTransactions.size() == 1)){
                              DicomOutputTransaction out = dicomTransactions.get(0);
                              DownloadCxpJob downloadCxpJob = new DownloadCxpJob(trans,out);
                              jobHandler.addCxpJob(downloadCxpJob);
                              try{
                                  downloadCxpJob.run();

                              }
                              finally{
                                  jobHandler.deleteCxpJob(trans.getId());
                              }
                          }
                          else{

                              throw new IllegalStateException("No matching output transaction for job id " + trans.getId());
                          }

                      }
                 }
                 catch(Exception e){
                    log.error("Error uploading", e);
                    trans.setStatus(CxpTransaction.STATUS_PERMANENT_ERROR);
                         trans.setStatusMessage("No matching output transaction for job id " + trans.getId());
                         trans = TransactionUtils.saveTransaction(trans);
                     StatusDisplayManager.getStatusDisplayManager().setErrorMessage("Error downloading", e.toString(),trans.getDashboardStatusId());
                 }

             }
         }
    }

    private class ExportDicom implements Runnable {
        long timeOut = 2 * 1000;

        boolean running = true;

        public void run() {
            DicomOutputTransaction trans = null;
            JobHandler jobHandler = JobHandler.JobHandlerFactory();

            while (running) {
                try {
                    Thread.sleep(timeOut);
                    Session session = LocalHibernateUtil.currentSession();

                    // Select a queued job.
                    Criteria queued = session
                            .createCriteria(DicomOutputTransaction.class);

                    queued.add(Expression.eq("status",
                            DicomOutputTransaction.STATUS_QUEUED));

                    // Should sort by time? Priority?
                    List<DicomOutputTransaction> queuedJobs = queued.list();
                    //log.info("There are " + queuedJobs.size() + " queued DICOM output jobs");
                    for (int i=0;i<queuedJobs.size(); i++){
                        log.info(" dicom " + queuedJobs.get(i).toString());
                    }
                    if (queuedJobs.size() > 0) {
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

                } catch (Exception e) {
                    String message = "Error exporting DICOM ";
                    log.error(message, e);
                    trans.setStatus(CxpTransaction.STATUS_PERMANENT_ERROR);
                    trans.setStatusMessage(message);
                    trans = TransactionUtils.saveTransaction(trans);
                    StatusDisplayManager.getStatusDisplayManager()
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
            Configurations configurations = ContextManager.getContextManager().getConfigurations();
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
                    StatusDisplayManager.getStatusDisplayManager().setErrorMessage(
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
                    StatusDisplayManager.getStatusDisplayManager().setMessage(
                            "DICOM Echo Success", response);
            }
        } catch (Exception e) {
            response = "Failed attempting DICOM echo to device at host:"
                    + remoteHost + ", port:" + remotePort + ", AE title:"
                    + remoteAeTitle + "\nfrom\n host:" + hostAddress
                    + ", AETitle: " + localAeTitle;
            if (showStatus)
                StatusDisplayManager.getStatusDisplayManager().setErrorMessage(
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
        Configurations configurations = ContextManager.getContextManager().getConfigurations();
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
