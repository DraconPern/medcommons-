package net.medcommons.application.dicomclient;

import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.FormatUtils;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.modules.transfer.TransferBase;

import org.apache.log4j.Logger;
import org.hibernate.StaleStateException;


/**
 * Monitors a CXP transaction via TransferBase and performs two distinct operations with the information:
 * <ol>
 * <li>Updates the tooltip with current information for the CXP transaction.</li>
 * <li>Updates the database row for the specified CxpTransferMonitor instance. These database values are
 * (for example) picked up by the status page display.</li>
 * </ol>
 * @author mesozoic
 *
 */
public class CxpTransferMonitor implements Runnable{
    private boolean stop = false;
    private CxpTransaction cxpTransaction;
    private String caption;
    private static Logger log = Logger.getLogger(CxpTransferMonitor.class);
    TransferBase client = null;
    long delay = 1 * 1000; // 1 second
    long startTime = -1;
    long endTime = -1;
    Long id;

    public CxpTransferMonitor(TransferBase client, String caption, CxpTransaction cxpTransaction){
        this.client = client;
        this.cxpTransaction = cxpTransaction;
        id = cxpTransaction.getId();
        if (id == null){ 
        	throw new NullPointerException("Null id in cxpTransaction");
        }
        this.caption =caption;
        log.debug("Created CxpTransferMonitor for " + client);
    }

    public void stopMonitor(boolean displayMessage){
        if (displayMessage){


            StringBuffer buff = new StringBuffer("Transfer of data for ");
            buff.append(caption);
            buff.append(" completed");

            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            long byteCount = client.getBytesTransferred();
            if (byteCount != 0){
            	String kbPerSecond = FormatUtils.formatKbPerSecond(byteCount,elapsedTime) + " KB/second";
                buff.append("\n");
                buff.append(kbPerSecond);
                cxpTransaction.setKbPerSecond(kbPerSecond);
            }
            buff.append("\n Total transfer time:");
            buff.append(FormatUtils.formatElapsedTime(elapsedTime));

            log.info(buff.toString());
            cxpTransaction.setBytesTransferred(byteCount);
            cxpTransaction.setElapsedTime(elapsedTime);
            
            
            if (byteCount != 0){

                buff.append("\n");
                buff.append(FormatUtils.formatKbPerSecond(byteCount, elapsedTime));
                buff.append(" KB/second");

            }
            
           
            String status = cxpTransaction.getStatus();
            if (status.equals(CxpTransaction.STATUS_ACTIVE))
                // Don't change ERROR or PERMANENT error status.
                cxpTransaction.setStatus(CxpTransaction.STATUS_COMPLETE);
            cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
            StatusDisplayManager.getStatusDisplayManager().setToolTip(buff.toString());
        }
        this.stop = true;
    }
    public void run(){
        long oldByteCount = 0;
        startTime = System.currentTimeMillis();
        cxpTransaction = TransactionUtils.getCxpTransactionWithId(id);
        if (cxpTransaction == null)
        {
        	throw new NullPointerException("Null cxpTransaction for id " + id);
        }
        cxpTransaction.setTimeStarted(System.currentTimeMillis()); // Reset time to be the time that the job started.
        TransactionUtils.saveTransaction(cxpTransaction);
        while(!stop){
            try{
                Thread.sleep(delay);

                long byteCount = client.getBytesTransferred();
                int objectCount = client.getObjectsTransferred();
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                String kbPerSecond = FormatUtils.formatKbPerSecond(byteCount,elapsedTime) + " KB/second";
                
                // Get the latest version from database. May have been updated by another thread
                cxpTransaction = TransactionUtils.getCxpTransactionWithId(id);
                if (cxpTransaction == null){
                	throw new NullPointerException("No CXP transaction in database with id = " + id);
                }
                cxpTransaction.setBytesTransferred(byteCount);
                cxpTransaction.setKbPerSecond(kbPerSecond);
                String status = cxpTransaction.getStatus();
                if (status.equals(CxpTransaction.STATUS_PERMANENT_ERROR) ||
                    status.equals(CxpTransaction.STATUS_TEMPORARY_ERROR) ||
                    status.equals(CxpTransaction.STATUS_COMPLETE) ||
                    status.equals(CxpTransaction.STATUS_CANCELLED)
                    )
                {
                    stop=true;
                }
                else{
                    ;//cxpTransaction.setStatus(CxpTransaction.STATUS_ACTIVE);
                }

                try{
                    cxpTransaction = TransactionUtils.saveTransaction(cxpTransaction);
                }
                catch(StaleStateException e){
                    // Don't think there is any cleanup needed here.
                    log.warn("Database entry updated (probably deleted) while transaction update was in progress "+
                            "\nExiting transfer monitor for this object", e);
                    String toolTip = "Transfer job for " + caption + " has been cancelled";
                    StatusDisplayManager.getStatusDisplayManager().setToolTip(toolTip);
                    return;

                }


                StringBuffer buff = new StringBuffer(caption);
                buff.append(": \nTransferred ");
                buff.append(FormatUtils.formatMB(byteCount));
                buff.append("MB");
                if (objectCount != 0){
                    buff.append(", Objects=");
                    buff.append(objectCount);
                }
                if (cxpTransaction.getTotalBytes() != 0){
                    buff.append("\nPercent complete ");


                    buff.append(FormatUtils.formatPercentComplete(byteCount, cxpTransaction.getTotalBytes()));
                    buff.append("%");
                }

                if (oldByteCount != byteCount){
                    //log.info(toolTip);
                    //log.info("input bytes" + client.getInputBytes());
                    //log.info("outputBytes:" + client.getOutputBytes());
                    oldByteCount = byteCount;
                }
               
                if (byteCount != 0){

                    buff.append("\n");
                    buff.append(FormatUtils.formatKbPerSecond(byteCount, elapsedTime));
                    buff.append(" KB/second");

                }
                String toolTip = buff.toString();

                StatusDisplayManager.getStatusDisplayManager().setToolTip(toolTip);

            }
            catch(InterruptedException e){
                ;
            }
        }
    }
}
