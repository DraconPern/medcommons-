package net.medcommons.application.utils;

import net.medcommons.application.dicomclient.Job;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.upload.State;
import net.medcommons.application.upload.StatusUpdate;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.transfer.DownloadFileAgent;
import net.medcommons.modules.transfer.TransferBase;

import org.apache.log4j.Logger;
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
    
    boolean exit = false;
    private static boolean shutdown = false;
    
    private static Logger log = Logger.getLogger(MonitorTransfer.class.getName());
    private String transferDirection;
    private boolean success = false;
    private int version = 0;
    private Job job;
    
    public MonitorTransfer(Job job, TransferBase client, StatusUpdate statusCallback) {
        this(job, client, statusCallback,null);
        log.info("New monitor transfer " + this);
    }
    
    public MonitorTransfer(Job job, TransferBase client, StatusUpdate statusCallback, ContextState contextState) {
        this.client = client;
        this.statusCallback = statusCallback; 
        this.contextState = contextState;
        this.baseURL = DashboardMessageGenerator.createBaseStatusURL(contextState);
        SHA1 sha1 = new SHA1();
        sha1.initializeHashStreamCalculation();
         transferKey = sha1.calculateStringHash(Long.toString(System.currentTimeMillis()));
         
        // this.transferKey = transferKey;
        if (client instanceof DownloadFileAgent)
            transferDirection = "Downloading";
        else
            transferDirection = "Uploading";
        
        this.job = job;
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
        long startTime = System.currentTimeMillis();
        try {
            while(!exit & !shutdown) {
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
         
        }
        finally {
            if(success) {
                sendCompleteStatus();
            }
        }
        
    }

    /**
     * Send a status to the server indicating that the transfer (upload or download) is complete
     */
    private void sendCompleteStatus() {
        double progress = 1.0d;
        String url = DashboardMessageGenerator.makeStatusURL(baseURL, transferKey, "Complete", progress, version);
        try {
            JSONSimpleGET get = new JSONSimpleGET(contextState);
            JSONObject response = get.executeMethod(url);
        }
        catch(Exception e){
            log.error("Error sending complete status using URL" + url,e);
        }
    }

    private void sendUpdatedProgress(long byteCount, long totalBytes) {
        double progress = (byteCount * 1.0d)/(totalBytes * 1.0d);
        progress = Math.min(progress, 1.0d);
        progress = Math.max(progress, 0.0d);
        
        String url = DashboardMessageGenerator.makeStatusURL(baseURL, transferKey,transferDirection, progress, version);
        try{
            JSONSimpleGET get = new JSONSimpleGET(contextState);
            JSONObject response = get.executeMethod(url);
            Object responseStatus = response.get("status");
            log.info("Returned status is " + responseStatus + " for url " + url);
            if("ok".equals(responseStatus)) {
                JSONObject state = response.getJSONObject("transferState");
                version = state.getInt("version");
            }
            else
            if ("failed".equals(responseStatus)){
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
                }
                else
                    log.error("Failed response to url " + url + "\nError" + response.get("error"));
            }
        }
        catch(Exception e){
            log.error("Error with URL" + url, e);
        }
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
     
    public void exit(){
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
        try{
            JSONSimpleGET get = new JSONSimpleGET(cxpContextState);
            String getStatusURL = DashboardMessageGenerator.makeGetStatusURL(baseURL, key, cxpContextState.getStorageId());
            JSONObject getResponse = get.executeMethod(getStatusURL);
            if(!"ok".equals(getResponse.get("status"))) 
                throw new RuntimeException("Status not 'ok' " + getResponse);
            
            int version = getResponse.getJSONObject("transferState").getInt("version");
                
            statusURL = DashboardMessageGenerator.makeStatusURL(baseURL, key, status, progress, version);
            JSONObject response = get.executeMethod(statusURL);
            Object responseStatus = (String) response.get("status");
            if(response==null){
                throw new NullPointerException("Null return from url " + statusURL);
            }
            else if (!"ok".equals(responseStatus)){
                throw new RuntimeException("Status not 'ok' " + response);
            }
            else{
                ; // It's OK
            }
            
        }
        catch(Exception e){
            log.error("Error with URL" + statusURL);
        }
        
    }
}