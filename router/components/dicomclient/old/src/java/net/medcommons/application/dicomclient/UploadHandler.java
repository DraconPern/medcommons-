package net.medcommons.application.dicomclient;

import static net.medcommons.application.utils.Str.nvl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import net.medcommons.application.dicomclient.http.utils.Voucher;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.PatientMatch;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DDLTypes;
import net.medcommons.application.dicomclient.utils.DicomNameParser;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.DirectoryUtils;
import net.medcommons.application.dicomclient.utils.LocalHibernateUtil;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.utils.Str;
import net.medcommons.application.utils.DashboardMessageGenerator.MessageType;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.utils.FileUtils;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.Expression;
import org.mortbay.jetty.servlet.Context;

/**
 * This is the top-level thread for uploading content to a gateway.
 * 
 * TODO: detect if the series are already on the server; only send the data
 * necessary
 *
 * @author mesozoic
 *
 */
public class UploadHandler implements DDLTypes, StudyCompletionListener {

    private static final String DEFAULT_DICOM_VOUCHER_SERVICE = "DICOM+Upload";

    private File cacheFolder;

    protected PixDataExtractor pixDataExtractor = new PixDataExtractor();

    //private String gatewayRoot;

    private static Logger log = Logger.getLogger(UploadHandler.class.getName());

    public UploadHandler() {
        
        this.cacheFolder = ContextManager.getContextManager().getUploadCache();
        if (this.cacheFolder == null){
        	throw new RuntimeException("Unable to initialize UploadHandler with null upload cache folder");
        }
        
        StudyCompletionMonitor detectStudy = new StudyCompletionMonitor(this);
        Thread detectStudyThread = new Thread(detectStudy);
        detectStudyThread.setName("DetectStudy");
        detectStudyThread.start();

        UploadCompletedStudy uploadStudy = new UploadCompletedStudy();
        Thread uploadStudyThread = new Thread(uploadStudy);
        uploadStudyThread.setName("UploadStudy");
        uploadStudyThread.start();
    }

    /**
     * Polls the database to check for {@link CxpTransaction}s in 
     * STATUS_QUEUED state, and uploads a CXP job one at a time from the queue.
     *
     * @author mesozoic,ssadedin
     */
    private class UploadCompletedStudy implements Runnable {
         long timeOut = 2 * 1000;

         boolean running = true;

         @SuppressWarnings("unchecked")
        public void run() {
             CxpTransaction trans = null;
             JobHandler jobHandler = JobHandler.JobHandlerFactory();
             
             while(running){
                 try{
                     Thread.sleep(timeOut);
                     Session session = LocalHibernateUtil.currentSession();
                     
                     // Select a queued job.
                     Criteria queued = session.createCriteria(CxpTransaction.class);
                     queued.add(Expression.eq("status", CxpTransaction.STATUS_QUEUED));
                     queued.add(Expression.eq("transactionType", "PUT"));
                     queued.setMaxResults(1);
                     
                     // Should sort by time? Priority?
                     List<CxpTransaction> queuedJobs = queued.list();
                     
                     if(queuedJobs.isEmpty()) // Nothing to do 
                         continue;
                         
                     trans = queuedJobs.get(0);
                     boolean useREST = ContextManager.getContextManager().GetUseREST();
                     if (useREST){
                         log.info("Starting REST job");
                         UploadRestJob uploadRestJob = new UploadRestJob(cacheFolder, trans);
                         jobHandler.addCxpJob(uploadRestJob);
                         try{
                             uploadRestJob.run();
                         }
                         finally{
                             jobHandler.deleteCxpJob(trans.getId());
                             // Deletion rules for contextState?
                         }
                     }
                     else{
                         log.info("CXP style upload");
                         UploadCxpJob uploadCxpJob = new UploadCxpJob(cacheFolder, trans);
                         jobHandler.addCxpJob(uploadCxpJob);
                         try{
                             uploadCxpJob.run();
                         }
                         finally{
                             jobHandler.deleteCxpJob(trans.getId());
                             // Deletion rules for contextState?
                         }
                     }
                 }
                 catch(Exception e){
                     log.error("Error uploading", e);
                     StatusDisplayManager.getStatusDisplayManager().setErrorMessage("Error uploading", e.toString());
                 }
             }
         }
    }
    
    public void studyComplete(ContextState contextState, List<DicomTransaction> seriesToUpload) {
        try {
            String studyInstanceUid = seriesToUpload.get(0).getStudyInstanceUid();
            if (false) {
                for (int i = 0; i < seriesToUpload.size(); i++) {
                    DicomTransaction trans = seriesToUpload.get(i);
                    log.info("About to upload  " + trans.toString());

                }
            }
            uploadStudy(contextState, studyInstanceUid, seriesToUpload);
        } catch (Exception e) {
            log.error("Error uploading series ", e);
            StatusDisplayManager.setErrorIcon();
            StatusDisplayManager.getStatusDisplayManager().setErrorMessage(
                    "Error uploading series", e.getLocalizedMessage());

        }
    }

    private void moveFileToCache(DicomMetadata metadata, File dicomDir)
            throws IOException {
        File seriesDir = new File(dicomDir, metadata.getSeriesInstanceUid());
        DirectoryUtils.makeDirectory(seriesDir);

        File newFile = new File(seriesDir, metadata.getSopInstanceUid());
        if (log.isDebugEnabled()){
	        log.debug("Moving file " + metadata.getFile().getAbsolutePath()
	                + " to " + newFile.getAbsolutePath());
        }
        File currentFile = metadata.getFile();
        if (currentFile == null) {
            throw new NullPointerException("Null File in:" + metadata);
        }
        if (!currentFile.exists()) {
            log.error("Failed to move file into dicomDir "
                    + dicomDir.getAbsolutePath());
            throw new FileNotFoundException(currentFile.getAbsolutePath());
        }

        if (newFile.exists()) {
            FileUtils.deleteDir(newFile);
        }
        boolean fileMoved = currentFile.renameTo(newFile);
        if (!fileMoved) {
            throw new IOException("Failed to move file:\n"
                    + currentFile.getAbsolutePath() + "\n to new location:\n"
                    + newFile.getAbsolutePath());
        }
        metadata.setFile(newFile.getAbsoluteFile());
        TransactionUtils.saveTransaction(metadata);
    }
    
    /**
     * Utility class to carry results of moving dicom files 
     * between method calls.
     */
    private static class MoveResults {
    	public long totalDicomBytes;
    	public DicomMetadata dicomMetaData;
    	public File transactionDirectory;
    }

    /**
     * Queues a study for upload by moving it's files to a
     * transaction directory, and then setting status of all series to 
     * {@link CxpTransaction#STATUS_QUEUED}.
     * <p>
     * If vouchers are enabled in configuration and there is
     * no existing mapping yielding a MedCommons ID for the data,
     * then creates a voucher account for the patient and sets that
     * patient as the target of the upload. 
     */
    @SuppressWarnings("unchecked")
	private void uploadStudy(ContextState contextState, String studyInstanceUid,
            List<DicomTransaction> seriesToUpload) throws Exception {
    	String storageId = UNKNOWN_MEDCOMMONS_ACCOUNT;
    	
    	 if (contextState != null)
             storageId = contextState.getStorageId();
    	 
        StatusDisplayManager.setActiveIcon();
        Configurations configurations = ContextManager.getContextManager().getConfigurations();
        
        Session session = LocalHibernateUtil.currentSession();
        List<DicomMetadata> metadata  =
        	session.createQuery("from DicomMetadata d where d.studyInstanceUid = :suid")
            	   .setString("suid", studyInstanceUid)
            	   .list();
        LocalHibernateUtil.closeSession();
        
        MoveResults moveResults = moveFilestoTransactionDir(metadata, seriesToUpload,storageId, studyInstanceUid);
        DicomMetadata dicomMetadata = moveResults.dicomMetaData;
        long totalDicomBytes = moveResults.totalDicomBytes;
        File transactionDirectory = moveResults.transactionDirectory;
        
        StatusDisplayManager sdm = StatusDisplayManager.getStatusDisplayManager();
        CxpTransaction cxpTransaction = null;
        try {
            // Create CxpTransaction object here;
            // Save in queue
            // Exit - pick up in another thread.
            cxpTransaction = new CxpTransaction();
            cxpTransaction.setPatientName(dicomMetadata.getPatientName());
            cxpTransaction.setDisplayName(dicomMetadata.getStudyDescription());
            cxpTransaction.setTimeStarted(System.currentTimeMillis());
            cxpTransaction.setTotalBytes(totalDicomBytes);
            cxpTransaction.setTransactionType(CxpTransaction.TRANSACTION_PUT);
            if (storageId.equals(UNKNOWN_MEDCOMMONS_ACCOUNT))
            	cxpTransaction.setStatus(CxpTransaction.STATUS_WAIT_PENDING_MATCH);
            else{ 
            	// TODO: test for pending. If match 
            	// of patient name in queue - the set status to queued.
            	//
            	cxpTransaction.setStatus(CxpTransaction.STATUS_QUEUED);
            }
            
             // TODO REPLACE LOGIC..
            //
           // if (gatewayRoot != null){
           // 	String cxpEndpoint = gatewayRoot + CxpTransaction.CXP_ENDPOINT;
           // 	cxpTransaction.setCxpEndpoint(cxpEndpoint);
           // }
           // else
           //	cxpTransaction.setCxpEndpoint(CxpTransaction.UNKNOWN);
			// TODO: Need to get the context in here.
            if (!UNKNOWN_MEDCOMMONS_ACCOUNT.equals(storageId)){
            	cxpTransaction.setContextStateId(contextState.getId());
            }
            
            cxpTransaction.setTransactionFolder(transactionDirectory.getAbsolutePath());
            cxpTransaction.setStudyInstanceUid(dicomMetadata.getStudyInstanceUid());
            cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);

            setSeriesStatusReadyForUpload(studyInstanceUid, cxpTransaction);
            
            setDicomJobId(cxpTransaction, metadata);
            
            writeReceiptFile(transactionDirectory);
            
            log.info("About to upload from " + transactionDirectory.getAbsolutePath());
            
            DicomNameParser parser = new DicomNameParser();
            String patientGivenName = parser.givenName(cxpTransaction.getPatientName());
    		String patientFamilyName = parser.familyName(cxpTransaction.getPatientName());
           
            boolean uploadToVoucher = configurations.getAutomaticUploadToVoucher();
            if(uploadToVoucher) {
            	if (contextState == null){
            		contextState = ContextManager.getContextManager().getCurrentContextState();
            	}
            	if(!Voucher.contextComplete(contextState)) {
            		cxpTransaction.setStatusMessage("User must log in for credentials before voucher upload can take place");
            		sdm.setMessage("User must authenticate before upload can begin",
	     	                       "DICOM study complete for " +patientGivenName + " " + patientFamilyName);
            	}
            	else {
            		createVoucher(cxpTransaction, contextState, patientGivenName, patientFamilyName);
            		
    	    		sdm.setMessage(
     	                    "Automatic upload to voucher account queued",
     	                    "DICOM study complete for " +patientGivenName + " " + patientFamilyName);
    	    		sdm.sendDashboardMessage(MessageType.INFO, null, "Uploading patient " + nvl(patientGivenName, "") + " " + nvl(patientFamilyName,""));
            	}
            }
            else {
	            sdm.setMessage(
	                    "DICOM study ready for upload "+ patientGivenName + " " + patientFamilyName,
	                    "DICOM study complete for " +patientGivenName + " " + patientFamilyName);
            }
        }
        catch(Exception e){
                log.error("Exception queuing data", e);
                sdm.setErrorMessage("Error queueing", e.getLocalizedMessage());
                if (cxpTransaction != null){
                    cxpTransaction.setStatus(CxpTransaction.STATUS_TEMPORARY_ERROR);
                    cxpTransaction.setStatusMessage(e.getLocalizedMessage());
                    cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
                }
        }
    }

    private void setDicomJobId(CxpTransaction cxpTransaction, List<DicomMetadata> metadata) {
        DicomMetadata dicomMetadata;
        Iterator<DicomMetadata> metaIter = metadata.iterator();
        while (metaIter.hasNext()) {
            dicomMetadata = metaIter.next();
            dicomMetadata.setCxpJob(cxpTransaction.getId());
            dicomMetadata = TransactionUtils.saveTransaction(dicomMetadata);
        }
    }

    private void setSeriesStatusReadyForUpload(String studyInstanceUid, CxpTransaction cxpTransaction) {
        Session session;
        session = LocalHibernateUtil.currentSession();
        List transactions =  
            session.createCriteria(DicomTransaction.class)
                   .add(Expression.eq("studyInstanceUid", studyInstanceUid))
                   .list();
        LocalHibernateUtil.closeSession();
        
        Iterator<DicomTransaction> transIter = transactions.iterator();
        while (transIter.hasNext()) {
            DicomTransaction dTrans = transIter.next();
            dTrans.setCxpJob(cxpTransaction.getId());
            dTrans.setStatus(DicomTransaction.STATUS_READY_FOR_UPLOAD);
            TransactionUtils.saveTransaction(dTrans);
        }
    }

    /**
     * Creates a voucher for the specified patient by calling the gateway.
     * <p>
     * If voucher creation is successful, creates a new context state representing
     * the voucher details and associates it to the transaction.
     * <p>
     * If voucher creation is successful, stores patient data in the pix database.
     */
    @SuppressWarnings("unchecked")
    private void createVoucher(CxpTransaction cxpTransaction, ContextState contextState,
            String patientGivenName, String patientFamilyName) throws ParseException, HttpException,
            GeneralSecurityException, IOException {
        
        ContextState uploadContextState = null;
        
        // First check if there was an existing context state saved
        // while data was importing
        Session session = LocalHibernateUtil.currentSession();
        List<DicomTransaction> dtxs = 
            session.createQuery("from DicomTransaction d where d.studyInstanceUid = :suid " +
			            		"and d.contextState != null")
			        .setString("suid", cxpTransaction.getStudyInstanceUid())
            		.list(); 
        
        if(!dtxs.isEmpty()) {
           uploadContextState = dtxs.get(0).getContextState();   
           assert uploadContextState != null : "Upload context should not be null by previous query!";
        }
        else
        if(cxpTransaction.getContextStateId() != null) {
            // This case should not really occur - but we defend against it anyway
            uploadContextState = TransactionUtils.getContextState(cxpTransaction.getContextStateId());
            log.info("Found existing context state " + uploadContextState.getId() + " associated with cxp transaction " + cxpTransaction.getId());
        }
        else {
	        dtxs =  session.createQuery("from DicomTransaction d where d.studyInstanceUid = :suid ")
								        .setString("suid", cxpTransaction.getStudyInstanceUid())
					            		.list(); 
                
	        for(DicomTransaction tx : dtxs) {
	            uploadContextState = tx.resolveContextState();
	            log.info("Found context state " + uploadContextState + " from dicom transaction " + tx);
	            break;
	        }
	        
	        if(uploadContextState == null) {
	            log.info("No voucher found for study " + cxpTransaction.getStudyInstanceUid());
	            Voucher voucher = new Voucher(contextState, patientGivenName, patientFamilyName, DEFAULT_DICOM_VOUCHER_SERVICE);
	            voucher.createVoucher();
	            log.info("Voucher returned new medcommons id " + voucher.getPatientMedCommonsId());
	            
	            uploadContextState = voucher.createDocumentUploadContextState(contextState);
	            
	            this.pixDataExtractor.createPixData(cxpTransaction, uploadContextState);
	        }
        }
        
        log.info("Document upload context state is " + uploadContextState);
        Long contextStateId = uploadContextState.getId();
        cxpTransaction.setContextStateId(contextStateId);
        cxpTransaction.setStatus(CxpTransaction.STATUS_QUEUED);
        cxpTransaction.setStatusMessage("Uploading to voucher-created account");
        TransactionUtils.saveTransaction(cxpTransaction);
    }

    /**
     * Writes a file to flag that files are complete and upload is about to occur
     * @param transactionDirectory
     * @throws IOException
     */
	private void writeReceiptFile(File transactionDirectory) throws IOException {
        Configurations configurations = ContextManager.getContextManager().getConfigurations();
		Properties p = new Properties();
		p.setProperty("Version", "1.0.0.5");//Version.getVersionString());
		p.setProperty("Revision", configurations.getVersion());
		File receiptFile = new File(transactionDirectory, "Receipt.txt");
		FileOutputStream out = new FileOutputStream(receiptFile);
		p.store(out, "DDL");
	}

    /**
     * Iterates each of the given metadata instances and locates and moves 
     * it's file to a directory in preparation for the upload transaction.
     * <p>
     * If an error occurs while moving, sets all the status of all 
     * associated series to 'STATUS_PERMANENT_ERROR'.
     * 
     * @param metadata
     * @param seriesToUpload
     * @param storageId
     * @param studyInstanceUid
     * @return
     * @throws IOException
     */
    private MoveResults moveFilestoTransactionDir(List<DicomMetadata> metadata, List<DicomTransaction> seriesToUpload, String storageId, String studyInstanceUid) throws IOException {
    	
    	MoveResults results = new MoveResults();
        File timeDir = new File(cacheFolder, System.currentTimeMillis() + "");
        
        DirectoryUtils.makeDirectory(timeDir);
        File transactionDirectory = new File(timeDir, storageId);
        DirectoryUtils.makeDirectory(transactionDirectory);

        log.info("Made new transaction directory for DICOM :" + transactionDirectory.getAbsolutePath());
        File dicomDir = new File(transactionDirectory, "DICOM");
        DirectoryUtils.makeDirectory(dicomDir);
        results.transactionDirectory = transactionDirectory;
    	
        DicomMetadata dicomMetadata = null;
        try {
            log.info("There are " + metadata.size()
                            + " DICOM objects in study; moving to directory " + dicomDir.getAbsolutePath());
            if (metadata.size() == 0){
            	throw new RuntimeException("There are zero DICOM documents; nothing to upload");
            }

            Iterator<DicomMetadata> iter = metadata.iterator();
            //session.beginTransaction();
            while (iter.hasNext()) {
                dicomMetadata = iter.next();
                results.totalDicomBytes += dicomMetadata.getLength();
                moveFileToCache(dicomMetadata, dicomDir);
                dicomMetadata.setTransactionStatus(DicomMetadata.STATUS_READY_TO_DELETE);
                dicomMetadata = TransactionUtils.saveTransaction(dicomMetadata);
            }
        }
        catch(FileNotFoundException e){
        	Iterator<DicomTransaction> iter = seriesToUpload.iterator();
        	while (iter.hasNext()){
        		DicomTransaction trans = iter.next();
        		trans.setStatus(DicomTransaction.STATUS_PERMANENT_ERROR);
        		trans.setStatusMessage(e.getLocalizedMessage());
        		TransactionUtils.saveTransaction(trans);
        	}
        }
        results.dicomMetaData = dicomMetadata;
        return results;
	}

	private void dumpDatabase(Session session) {
        Criteria allMetadata = session.createCriteria(DicomMetadata.class);
        Criteria allTransactions = session
                .createCriteria(DicomTransaction.class);
        List<DicomMetadata> allMetadataObjects = allMetadata.list();
        List<DicomTransaction> allTransactionObjects = allTransactions.list();
        log.info("CurrentDB: Transactions: " + allTransactionObjects.size()
                + ", Metadata objects:" + allMetadataObjects.size());
        Iterator<DicomTransaction> iter = allTransactionObjects.iterator();
        while (iter.hasNext()) {
            DicomTransaction transaction = iter.next();
            log.info("Transction:" + transaction.toString());
        }
    }

	/**
	 * Called by {@link StudyCompletionMonitor} when first DicomTransaction of a new study
	 * being imported is observed.  This lets us create the account much earlier - even while
	 * data is still streaming in.
	 */
    @Override
    public void newTransaction(DicomTransaction tx) {
        try {
            
            // Check if there are any other transactions for this voucher that may already
            // have created a patient
            Session session = LocalHibernateUtil.currentSession();
            long count = (Long) session.createQuery("select count(*) from DicomTransaction d " +
            		"where d.studyInstanceUid = :suid " +
            		"and d.contextState != null")
            		.setString("suid", tx.getStudyInstanceUid())
            		.uniqueResult();
            
            if(count > 0)
                return;
            
            Configurations configurations = ContextManager.getContextManager().getConfigurations();
            boolean uploadToVoucher = configurations.getAutomaticUploadToVoucher();
            
            if(uploadToVoucher) {
                log.info("Found new transaction active transaction without patient context: " + tx.getId());
                DicomNameParser parser = new DicomNameParser();
                String patientGivenName = parser.givenName(tx.getPatientName());
            	String patientFamilyName = parser.familyName(tx.getPatientName());
                ContextState ctx = ContextManager.getContextManager().getCurrentContextState();
                Voucher voucher = new Voucher(ctx, patientGivenName, patientFamilyName, DEFAULT_DICOM_VOUCHER_SERVICE);
                voucher.createVoucher();
                
                ContextState uploadCtx = voucher.createDocumentUploadContextState(ctx);
                
                this.pixDataExtractor.createPixData(tx, uploadCtx);
                
                tx.setContextState(uploadCtx);
                
                try {
	                log.info("Created new voucher for study " + tx.getStudyInstanceUid());
	                TransactionUtils.saveTransaction(tx); // closes hibernate session
                }
				catch(StaleObjectStateException exStale) {
				    log.info("Stale object " + exStale.getEntityName() + " " + exStale.getIdentifier() + " - will reprocess");
				}
                
            }
        } 
        catch(Exception e) {
            log.error("Failed to create voucher for incoming DICOM.  Will retry when queued for upload",e);
            StatusDisplayManager sdm = StatusDisplayManager.getStatusDisplayManager();
            sdm.setErrorMessage("Voucher Error", "Failed to create voucher for incoming DICOM.  Will retry when queued for upload");
        }
    }
}
