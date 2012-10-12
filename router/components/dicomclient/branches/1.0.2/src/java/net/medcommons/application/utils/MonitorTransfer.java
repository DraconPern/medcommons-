package net.medcommons.application.utils;

import static net.medcommons.application.utils.Str.blank;

import java.util.ArrayList;
import java.util.List;

import net.medcommons.application.dicomclient.Job;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.*;
import net.medcommons.application.dicomclient.utils.Shutdown;
import net.medcommons.application.upload.State;
import net.medcommons.application.upload.StatusUpdate;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.transfer.DownloadFileAgent;
import net.medcommons.modules.transfer.TransferBase;
import net.sourceforge.pbeans.Store;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Update thread for document upload/downloads.
 * Optionally updates status on server via the DICOMStatus.action service.
 * 
 * Perhaps key should be passed down to this class? How can it be derived?
 * Note that there may be more than one monitor thread running so that one 
 * doesn't block another.
 * 
 * 
 *     
 * @author mesozoic
 *
 */
public class MonitorTransfer implements Runnable {
    
    TransferBase client = null;
    StatusUpdate statusCallback = null;
    ContextState contextState = null;
    String baseURL = null;
    String transferKey = null;
    
    float processingBytesPerSecond = 10 * 1024 * 1024; // 2.6 MB/s approx
    
    boolean exit = false;
    private static boolean shutdown = false;
    
    private static Logger log = Logger.getLogger(MonitorTransfer.class.getName());
    private String transferDirection;
    private boolean success = false;
    private int version = 0;
    
    private float lastProgress = -1.0f;
    
    private long startTimeMs;
    private long transferCompletionTimeMS = 0;
    
    private int queuedStudies = 1;
    
    public static enum TxState {
        NEW,
        SCANNING,
        UPLOADING,
        ERROR
    }
    
    private TxState state = TxState.NEW;
    
    long unqueuedBytes = 0L;
    
    /**
     * Current job
     */
    private Job job = null;
    
    private static List<MonitorTransfer> allTransfers = new ArrayList<MonitorTransfer>();
    
    
    /**
     * This constructor creates a passive transfer monitor - it does not
     * have a job or transfer client so it cannot monitor progress itself. 
     * It relies on an external agent to update it's progress.
     * 
     * @param transferKey
     */
    public MonitorTransfer(String transferKey, String direction, int numberOfStudies) {
        this.transferKey = transferKey;
        this.queuedStudies = numberOfStudies;
        this.transferDirection = direction;
        synchronized (allTransfers) {
	        allTransfers.add(this);
        }
     }
    
    public MonitorTransfer(Job job, TransferBase client, StatusUpdate statusCallback) {
        this(job, client, statusCallback,null, null);
        synchronized (allTransfers) {
	        allTransfers.add(this);
        }
        log.info("New monitor transfer " + this);
    }
    
    public MonitorTransfer(Job job, TransferBase client, StatusUpdate statusCallback, ContextState contextState) {
        this(job, client,statusCallback,contextState,null);
    }
    
    public MonitorTransfer(Job job, TransferBase client, StatusUpdate statusCallback, ContextState contextState, String transferKey) {
        this.client = client;
        this.statusCallback = statusCallback; 
        this.contextState = contextState;
        this.job = job;
        
        if(contextState != null && !blank(contextState.getCxpProtocol()))
	        this.baseURL = DashboardMessageGenerator.createBaseStatusURL(contextState);
        
        if(!blank(transferKey)) {
            this.transferKey = transferKey;
        }
        else {
	        SHA1 sha1 = new SHA1();
	        sha1.initializeHashStreamCalculation();
	        this.transferKey = sha1.calculateStringHash(Long.toString(System.currentTimeMillis()));
        }
         
        // this.transferKey = transferKey;
        if (client instanceof DownloadFileAgent)
            transferDirection = "Downloading";
        else
            transferDirection = "Uploading";
        
        // TODO:  this might become part of the context state if it lives at all for any length of time
        // more likely we'll just forget it once we move to REST style uploads. 
        if(!blank(System.getProperty("gw_processing_bytes_per_second"))) {
            this.processingBytesPerSecond = Float.parseFloat(System.getProperty("gw_processing_bytes_per_second"));
        }
        
        synchronized (allTransfers) {
	        allTransfers.add(this);
        }
    }
    
    /**
     * Global shutdown for all monitor transfers.
     */
    public static void setShutdown(){
        shutdown = true;
    }
    
    public void setSuccess(boolean success){
        this.success = success;
    }
    
    public void run(){
        startTimeMs = System.currentTimeMillis();
        try {
            while(!exit & !shutdown && lastProgress < 1.0) {
                try{Thread.sleep(1000);} catch(InterruptedException e){;}
                long byteCount = client.getBytesTransferred();
                if (byteCount > 0){
                    statusCallback.updateState(State.UPLOADING);
                    statusCallback.updateMessage(transferDirection);
                }
                
                long elapsedTime = client.getElapsedTime();
                long totalBytes = client.getTotalBytes();
                if (log.isDebugEnabled())
                    log.debug("bytecount=" + byteCount + ", elapsedTime =" + elapsedTime + ", totalBytes = " + totalBytes);
                
                statusCallback.updateRate(byteCount, elapsedTime);
                
                if (totalBytes > 0) {
                    statusCallback.updateProgress(byteCount, totalBytes);
                }
                
                // Only update server if there is a ContextState
                if(contextState != null) {
                    sendUpdatedProgress(byteCount, totalBytes);
                }
            }
        }
        catch(Exception e) {
            log.error("Error monitoring upload thread", e);
            state = TxState.ERROR;
        }
        finally {
            if(success || lastProgress >= 1.0) {
                sendCompleteStatus();
            }
        }
        
        synchronized (allTransfers) {
            // allTransfers.remove(this);
        }
    }

    /**
     * Send a status to the server indicating that the transfer (upload or download) is complete
     */
    private void sendCompleteStatus() {
        double progress = 1.0d;
        String url = DashboardMessageGenerator.makeStatusURL(baseURL, transferKey, "Complete", progress, version);
        try {
            JSONSimpleGET get = new JSONSimpleGET();
            JSONObject response = get.get(url);
        }
        catch(Exception e){
            log.error("Error sending complete status using URL" + url,e);
        }
    }

    private void sendUpdatedProgress(long byteCount, long totalBytes) {
        
        double progress = computeProgress(byteCount, totalBytes, getElapsedSecs());
        
        String url = DashboardMessageGenerator.makeStatusURL(baseURL, transferKey, transferDirection, progress, version);
        try{
            JSONSimpleGET get = new JSONSimpleGET();
            JSONObject response = get.get(url);
            Object responseStatus = response.get("status");
            log.info("Returned status is " + responseStatus + " for url " + url);
            if("ok".equals(responseStatus)) {
                JSONObject state = response.getJSONObject("transferState");
                if("Cancelled".equals(state.get("status")))
                    exit();
                
                if("Shutdown".equals(state.get("status")))  {
                    new Shutdown().run();
                    System.exit(0);
                }
                
                version = state.getInt("version");
            }
            else
            if("failed".equals(responseStatus)) {
                if("invalid version".equals(response.get("error"))) {
                    JSONObject state = response.getJSONObject("transferState");
                    version = state.getInt("version");
                    log.info("Remote transfer state modified - local version = " + version + " remote version =  " + state.get("version"));
                    if("Cancelling".equals(state.get("status")) && !"Cancelled".equals(transferDirection)) {
                        
                        log.info("Cancelling  stream using client directly");
                        client.cancelStream();
                        
                        TransactionUtils.cancelTransaction(transferKey);
                        transferDirection = "Cancelled";
                        
                        // Notify server immediately that we received the cancelled message
                        sendUpdatedProgress(byteCount, totalBytes);
                        
                        exit();
                    }
                    else
                    if("Shutdown".equals(state.get("status")))  {
                        StatusDisplayManager.get().setMessage("Remote Shutdown Request Received", "This DDL has been asked to shut down by the server.  You can restart it manually by visiting the web page.");
                        log.info("Shutting down due to remote shutdown request");
                        new Shutdown().run();
                        System.exit(0);
                    }
                }
                else
                    log.error("Failed response to url " + url + "\nError" + response.get("error"));
            }
        }
        catch(Exception e){
            log.error("Error with URL" + url, e);
        }
    }

    public double computeProgress(long byteCount, long totalBytes, double elapsedSecs) {
        
        if(elapsedSecs < 0.01d) 
            return 0.0d;
        
        // Total passed in may be -1 while the job is initializing
        // A bit of a hack, but we just wait for it to get started
        // and return the previous progress until it's ready
        if(totalBytes == -1)
            return Math.max(lastProgress,0.0d);
        
        double progress = 0.0;
        
        Store db = DB.get();
        
        // get total transferred bytes across all jobs
        List<Object> totals = 
            db.selectValues("select sum(bytesTransferred), sum(elapsedtime), sum(totalbytes), count(*) "+
                            "from cxp_transaction where dashboardstatusid = ? and id <> ?", 
                new Object[]{transferKey, job.getId()});
        
        log.info("Got totals " + totals + " for other jobs with same transfer key");
        
        if(totals.get(0) != null) {
            byteCount += (Double)totals.get(0);
            totalBytes += (Double)totals.get(2);
            queuedStudies = (Integer)totals.get(3) + 1;
        }
        
        // Count all transferred bytes
        double transferRate = (byteCount*1.0d) / elapsedSecs;
        List<Double> unqueuedTotals = 
            db.selectValues("select sum(totalbytes) "+
                            "from dicom_transaction where transferkey = ? and cxpjob = ?", 
                new Object[]{transferKey, DicomTransaction.UNITIALIZED_CXPJOB});
                            
  
        long newUnqueuedBytes = 0L;
        if(unqueuedTotals.get(0) != null) {
            newUnqueuedBytes = unqueuedTotals.get(0).longValue();
            log.info("Found " + newUnqueuedBytes + " of data not yet queued but waiting for transfer");
        }
        
        if(newUnqueuedBytes != unqueuedBytes && newUnqueuedBytes > 0)  {
            lastProgress = 0.0f;
            if(state == TxState.NEW) {
                log.info("Found unqueued bytes ... transitioning to state SCANNING");
	            state = TxState.SCANNING;
            }
            return 0.0d;
        }
        else 
        if(newUnqueuedBytes == unqueuedBytes)
            state = TxState.UPLOADING;
        
        unqueuedBytes = newUnqueuedBytes;
            
        totalBytes += unqueuedBytes;
        
        if(totalBytes > 0 && byteCount >= totalBytes && transferCompletionTimeMS == 0) {
            log.info("Transfer " + this.transferKey + " completed: byteCount("+totalBytes+") > totalBytes("+totalBytes+")");
            transferCompletionTimeMS = System.currentTimeMillis();
        }
        
        double estimatedTransferTimeSecs;
        if(transferCompletionTimeMS == 0) {
            estimatedTransferTimeSecs = totalBytes / transferRate;
            log.info("estimatedTransferTimeSecs("+estimatedTransferTimeSecs+") = totalBytes("+totalBytes+") / transferRate("+transferRate+")");
        }
        else 
            estimatedTransferTimeSecs = (transferCompletionTimeMS*1.0d - startTimeMs*1.0d) / 1000;
        
        double estimatedProcessingTime = processingBytesPerSecond > 0 ? (totalBytes*1.0d) / processingBytesPerSecond : 0;
        
        progress = elapsedSecs / (estimatedTransferTimeSecs + estimatedProcessingTime);
        log.info("progress( " + progress + " ) = elapsedSecs("+elapsedSecs+") / ( " + estimatedTransferTimeSecs + " + " + estimatedProcessingTime  + ")");
        
        progress = Math.min(progress, 1.0d);
        progress = Math.max(progress, 0.0d);
        lastProgress = (float)progress;
        
        return progress;
    }

    private double getElapsedSecs() {
        return (System.currentTimeMillis() - startTimeMs) / 1000.0d;
    }
    
    public String getTransferKey(){
        return(this.transferKey);
    }
   
    /**
     * Creates a base URL for sending transfer state information to the gateway.
     * 
     * @return  URL in the form:
     * 
     *   (gw)/TransferState.action?put&status.key=5b91073a00fbb9dc8992b48b14722d1e979e9b43&status.accountId=1162164444007929
     */
    private static String createBaseURL(ContextState cxpContextState) {
        StringBuffer buff = new StringBuffer();
        if (cxpContextState.getCxpProtocol().equals("https")) {
            buff.append(JSONSimpleGET.HTTPS);
        } else {
            buff.append(cxpContextState.getCxpProtocol());
        }
        buff.append("://");
        buff.append(cxpContextState.getCxpHost());
        if (cxpContextState.getCxpPort() != null) {
            buff.append(":").append(cxpContextState.getCxpPort());
        }
        buff.append("/router/TransferState.action?");
        buff.append("status.accountId=");
        buff.append(cxpContextState.getStorageId());
        return (buff.toString());
    }
 
    /**
     * Returns the ContextState associated with a transaction.
     * @param transaction
     * @return
     */
     private static ContextState getContextState(CxpTransaction transaction){
        Long contextStateId = transaction.getContextStateId();
        ContextState cxpContextState = TransactionUtils.getContextState(contextStateId);
        return(cxpContextState);
    }
     
    public void exit() {
        if(lastProgress < 1.0)
            log.warn("Exiting monitoring while progress = " + this.lastProgress + " (less than 100%)");
            
        this.exit = true;
    }
    
    public static void sendStatusMessage(CxpTransaction transaction, String status, double progress){
        ContextState  cxpContextState = getContextState(transaction);
        if (cxpContextState == null){
            throw new NullPointerException("Null context state for " + transaction);
        }
        String baseURL = createBaseURL(cxpContextState);
        String key = transaction.getDashboardStatusId();
        String statusURL = null;
        try {
            JSONSimpleGET get = new JSONSimpleGET();
            String getStatusURL = DashboardMessageGenerator.makeGetStatusURL(baseURL, key, cxpContextState.getStorageId());
            JSONObject getResponse = get.get(getStatusURL);
            if(!"ok".equals(getResponse.get("status"))) 
                throw new RuntimeException("Status not 'ok' " + getResponse);
            
            int version = getResponse.getJSONObject("transferState").getInt("version");
                
            statusURL = DashboardMessageGenerator.makeStatusURL(baseURL, key, status, progress, version);
            JSONObject response = get.get(statusURL);
            Object responseStatus = (String) response.get("status");
            if(response==null) {
                throw new NullPointerException("Null return from url " + statusURL);
            }
            else 
            if(!"ok".equals(responseStatus)) {
                throw new RuntimeException("Status not 'ok' " + response);
            }
            else {
                ; // It's OK
            }
            
        }
        catch(Exception e) {
            log.error("Error with URL" + statusURL);
        }
        
    }
    
    public static JSONArray getCurrentTransfersJSON() {
        synchronized (allTransfers) {
            JSONArray transfers = new JSONArray();
            int i = 0;
            for(MonitorTransfer t : allTransfers) {
                JSONObject tx = new JSONObject();
                tx.put("direction", t.transferDirection);
                tx.put("key", t.transferKey);
                tx.put("progress", t.getProgress());
                tx.put("state", t.state.name());
                tx.put("queuedStudies", t.queuedStudies);
	            transfers.put(i++, tx);
            }
            return transfers;
        }
    }
    
    public static void clear() {
        synchronized (allTransfers) {
            allTransfers.clear();
        }
    }
    
    public static MonitorTransfer getTransferMonitor(Job job, TransferBase client, StatusUpdate statusCallback, ContextState contextState, String transferKey) {
        synchronized (allTransfers) {
            if(!blank(transferKey)) {
                for(MonitorTransfer t : allTransfers) {
                    if(t.getTransferKey().equals(transferKey)) {
                        log.info("Assigning existing transfer monitor " + t + " to job " + job.getId() + " old job = " + t.job.getId());
                        t.job = job;
                        t.client = client;
                        return t;
                    }
                }
            }
            return null;
        }
    }
    
    public static void cancel(String transferKey) {
        synchronized (allTransfers) {
            if(!blank(transferKey)) {
                for(MonitorTransfer t : allTransfers) {
                    if(transferKey.equals(t.transferKey)) {
                        t.client.cancelStream();
                        t.transferDirection = "Cancelled";
                    }
                }
            }
        }
    }

    public TxState getState() {
        return state;
    }

    public void setState(TxState state) {
        this.state = state;
    }

    public float getProgress() {
        return lastProgress;
    }

    public void setProgress(float lastProgress) {
        this.lastProgress = lastProgress;
    }

    public TransferBase getClient() {
        return client;
    }

    public void setClient(TransferBase client) {
        this.client = client;
    }

    public int getQueuedStudies() {
        return queuedStudies;
    }

    public void setQueuedStudies(int queuedStudies) {
        this.queuedStudies = queuedStudies;
    }
}