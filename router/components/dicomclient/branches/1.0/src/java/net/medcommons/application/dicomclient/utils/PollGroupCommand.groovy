/**
 * $Id$
 */
package net.medcommons.application.dicomclient.utils

import org.json.JSONObject
import net.medcommons.application.utils.JSONSimpleGETimport net.medcommons.application.dicomclient.ContextManagerimport org.apache.log4j.Loggerimport net.sourceforge.pbeans.Storeimport net.medcommons.application.dicomclient.DownloadHandlerimport net.medcommons.application.dicomclient.transactions.ContextStateimport net.medcommons.application.dicomclient.transactions.CxpDownloadimport net.medcommons.application.dicomclient.transactions.DownloadQueueimport java.lang.IllegalStateException
import java.util.concurrent.Futureimport net.medcommons.application.dicomclient.Configurationsimport groovy.xml.MarkupBuilder
import static net.medcommons.application.dicomclient.utils.ManagedTransaction.*


/**
 * Polls a specified group for new patients and downloads their DICOM.
 * 
 * @author ssadedin
 */
public class PollGroupCommand implements Command {
     
    private static Logger log = Logger.getLogger(PollGroupCommand.class);
     
    JSONSimpleGET get = new JSONSimpleGET()
    
    ContextState ctx = null
    
    StatusDisplayManager sdm  = StatusDisplayManager.get()
    
    static PollGroupCommand running = null  
    
    boolean stop = false
    
    boolean pause = false
    
    /**
     * Error message if any
     */
    String error = null
    
    /**
     * A hash of orders looked up by order reference.
     * This needs to move inside DDL database!
     */
    def orders = [:]

    public Future<JSONObject> execute(CommandBlock params) {
         
        def props = params.properties
        if(props.cxphost)
	        ctx = params.toContextState() 
        else
        if(ctx == null)
            ctx = ContextManager.get().currentContextState
        
        stopExistingPoller()
        
        running = this

        sdm.setMessage("Starting Group Poller", 
                       "This DDL will now poll group ${ctx.groupName} on host ${ctx.cxpHost}")
                       
        sdm.addPollerMenu(ctx);
        
        save()
        
        new Thread({
            try {
                while(!stop) {
                    
                    if(pause) {
                       Thread.sleep(10000)
                       continue
                    }
                    
                    try {
                        def xml = get.rawGet(ctx.applianceRoot + "/acct/patient_list_atom.php?auth=" + ctx.auth )
                        def feed = new XmlSlurper().parseText(xml).declareNamespace(mc: 'http://medcommons.net/patientdata')
                        feed.entry.each { entry ->
                          def reference = entry?.'mc:order-reference'?.text()
                          if(reference && !orders[reference]) {
                              processOrder(entry)
                          }
                        }        
                    }
                    catch(Throwable t) {
                        try {
                            log.error("Failed to poll for patient list orders",t)
                        }
                        catch(Throwable t2) {
                            log.warn("Failed to log error " + t.toString() + ": " +t2.toString());
                        }
                    }
                    Thread.sleep(10000)
                }
            }
            finally {
                log.info "Poller for group ${ctx.groupName} now exiting"
                running = null
            }
        }).start()
        
    }
    
    /**
     * Check if the given ATOM entry contains an order in complete status that we
     * have not processed yet.  If so, initiate processing by creating a 
     * download job.
     */
    private void processOrder(entry) {
        
      def callersOrderReference = entry.'mc:order-reference'.text()

      // Is this a known order entry?
      if(![null, DicomOrder.ERROR].contains(orders.callersOrderReference?.status))
          return
          
      // We are only interested in complete orders
      if(entry.'mc:status'.text() != 'DDL_ORDER_UPLOAD_COMPLETE') {
          // log.info "Ignoring entry $callersOrderReference with status " + entry.'mc:status'.text()
          return
      }
        
      
      String patientId = entry.'mc:order-patient-id'.text()
      String protocolId = entry.'mc:order-protocol-id'.text()
      
      log.debug "Found Patient " + entry.title.text() + ", external Id = $patientId, status = " + entry.'mc:status'.text() 
          
      Store db = DB.get();
      DicomOrder order = db.selectSingle(DicomOrder.class, "callersOrderReference", callersOrderReference)
              
      if(order && order.status != DicomOrder.ERROR) { // Existing order
          log.info "Order " + callersOrderReference + " is an existing order"
          orders[callersOrderReference] = order
          return
      }
        
      // New order
      order = new DicomOrder( 
                    callersOrderReference: entry.'mc:order-reference'.text(),
                    status:    entry.'mc:status'.text(),
                    guid:      entry.'mc:guid'.text(),
                    storageId: entry.id.text()
                  )
      log.info "Found new remote order: ${order.callersOrderReference} with status ${order.status}"
          
      StatusDisplayManager.get().setMessage("New DICOM Order", 
          "Found new order in Patient List: " + order.callersOrderReference +  " with status " + order.status)
          
      db.save(order)
      
      error = null
      try {
	      downloadDICOM(order,patientId,protocolId)
	      
	      def status = waitComplete(order)
	      
	      switch(status) {
		      case ManagedTransaction.STATUS_COMPLETE :
		          order.status = DicomOrder.COMPLETE
		          break;
		      case ManagedTransaction.STATUS_CANCELLED :
		          order.status = DicomOrder.CANCELLED
		          break;
		      case ManagedTransaction.STATUS_PERMANENT_ERROR :
		          order.status = DicomOrder.ERROR
		          break;
		      case ManagedTransaction.STATUS_TEMPORARY_ERROR :
		          order.status = DicomOrder.ERROR
		          break;
		      default:
		          throw new IllegalStateException("Unknown state " + order.status + " after order completion")
	      }
      }
      catch(Exception e) {
          log.error "Failed to download or forward DICOM for order ${order.callersOrderReference} / mcid ${order.storageId}", e
          order.status = DicomOrder.ERROR
          error = e.message
      }
      
      log.info "DICOM order processed with status " + order.status
      db.save(order)
      
      orders[callersOrderReference] = order
      
      sendStatus(order,error)
          
      StatusDisplayManager.get().setMessage("Processed DICOM Order", 
          "DICOM Order " + order.callersOrderReference +  " was processed with status " + order.status)
    }
    
    /**
     * Update order with specified status on server
     */
    void sendStatus(def order, def error = null) {
        def result = get.get(ctx.applianceRoot + 
                "/acct/update_order_status.php?callers_order_reference=${order.callersOrderReference}&status="+ order.status +
                "&desc="+URLEncoder.encode("DDL Download") +
                "&user="+URLEncoder.encode(System.properties['user.name']) +
                "&errorCode="+(error?URLEncoder.encode(error):"") 
                )
                
        if(result.getString("status") != "ok")
              throw new Exception("Failed to update order status: " + result.getString("error"))
    }
    
    /**
     * Create a Cxp download job (and associated CxpTransaction) for the 
     * given order.
     */
    void downloadDICOM(DicomOrder order, String patientId, String protocolId) {
        
      Store db = DB.get()
      db.beginTransaction()
      try {
          ContextState downloadCtx = ctx.clone();
          
          downloadCtx.with {
              storageId = order.storageId
              guid = order.guid              
          }
          
          db.save(downloadCtx);
          
          def download = new DownloadQueue(
                  creationTime: new Date(),
                  contextStateId: downloadCtx.id,
                  attachments: DownloadQueue.ATTACHMENTS_ALL
              )
                  
          log.info "Running download job " + download
          
          String exportFolder = patientId
          if(protocolId) 
              exportFolder += " $protocolId"
          
          log.info "Exporting to folder $exportFolder"
          CxpDownload downloadJob = new CxpDownload(download,exportFolder);
          downloadJob.run();
              
          order.cxpTransactionId = downloadJob.cxpTransaction.id
          log.info "Download job yielded CxpTransaction " + order.cxpTransactionId
          
          db.save(order)
          db.endTransaction()
      }
      catch(Exception e) {
          db.endTransaction(true)
          throw e
      }
    }
    
    /**
     * The time before the order is changed to TIMEOUT_WARNING status
     */
    static long TIMEOUT_WARNING_MS = 10 * 60000L; 

    /**
     * The time before the order is changed to ERROR status
     */
    static long TIMEOUT_ERROR_MS = 30 * 60000L; 
    
    /**
     * Wait until the CxpTransaction for given DicomOrder reaches
     * a status that indicates it has completed (either with an error
     * or with a another final state).
     */
    String waitComplete(DicomOrder order) {
        log.info "Waiting for order " + order.id + " to complete downloading"
        Store db = DB.get()
        
        def ERRORS = [STATUS_CANCELLED,STATUS_TEMPORARY_ERROR,STATUS_PERMANENT_ERROR]
        
        long startTimeMs = System.currentTimeMillis()
        while(true) {
            CxpTransaction tx = db.selectSingle(CxpTransaction.class, "id", order.cxpTransactionId)
            if(tx == null)
                throw new IllegalStateException("Unable to locate CxpTransaction for order " + order.id)
            
            log.info "CxpTransaction " + tx.id + " has status ${tx.status}"
            if(tx.status in ERRORS) {
                error = tx.statusMessage
                return tx.status
            }
            
            if(tx.status == STATUS_COMPLETE && !order.callersOrderReference.startsWith("medcommons_timeout_test"))
                break;
            
            checkTimeout(order, startTimeMs)
            
            Thread.sleep(2000)
        }
        
        log.info "CXP download transaction for order "  + order.id + " completed successfully - now wait for export"
        
        order.status = DicomOrder.DOWNLOAD_COMPLETE
        sendStatus(order)
        
        // Now wait until the transaction has been forwarded on
        startTimeMs = System.currentTimeMillis()
        while(true) {
            DicomOutputTransaction outTx = db.selectSingle(DicomOutputTransaction.class, "cxpJob", order.cxpTransactionId)
            if(outTx.status in ERRORS) {
                error = outTx.statusMessage
                return outTx.status
            }
                
            if(outTx.status == STATUS_COMPLETE)
                return outTx.status
            
            checkTimeout(order, startTimeMs)
            
            Thread.sleep(2000)
        }
    }
    
    /**
     * Check that timeout thresholds have not been exceeded for the given order
     * relative to the specified start time.
     * <p>
     * If they have been exceeded, send a status message if the threshold exceeded was
     * a warning, or throw an exception if it is an error threshold.
     */
    void checkTimeout(DicomOrder order, def startTimeMs) {
        long ageMs = System.currentTimeMillis() - startTimeMs
        def stage = order.status == DicomOrder.DOWNLOAD_COMPLETE ? "export" : "download"
        if(order.status != DicomOrder.TIMEOUT_WARNING && ageMs > TIMEOUT_WARNING_MS) {
            order.status = DicomOrder.TIMEOUT_WARNING            sendStatus(order, "Transaction stage $stage exceeded timeout warning threshold after ${ageMs/1000} seconds")
        }
        else
        if(ageMs > TIMEOUT_ERROR_MS) {
            throw new Exception("Transaction stage $stage timed out after ${ageMs/1000} seconds")
        }
    }
    
    /**
     * Check if another Poller is already running.  If so, set the 
     * flag to tell it to stop and wait for it.  Throws an exception
     * after 20 seconds if other poller does not stop.
     */
    void stopExistingPoller() {
        if(running) {
            log.info "Found existing running DDL Poller ${running.ctx.cxpHost}, group ${running.ctx.groupName}"
            try {
	            sdm.setTooltip(null)                           
            }
            catch(Exception ex) {
                
            }
            running.stop = true
            
            def count = 0
            while(running) {
                Thread.sleep(2000)
                if(count++ > 10)
                    throw new Exception("Unable to shut down running Group Poller.  Please restart this DDL.")
            }
        }
        else
            log.info "No existing poller"
    }
    
    void save() {
        new File(ContextManager.get().configurations.configurationFile.parentFile,"poller.xml").withWriter { w ->
            def xml = new MarkupBuilder(w)
            xml.context() {
                cxpHost(ctx.cxpHost) 
                cxpProtocol(ctx.cxpProtocol)
                cxpPath(ctx.cxpPath)
                groupName(ctx.groupName)
                auth(ctx.auth)
                gatewayRoot(ctx.gatewayRoot)
                applianceRoot(ctx.applianceRoot)
                paused(pause)
            }
        }
    }
    
    static File getConfigFile() {
        def ddlCfg = ContextManager.get().configurations.configurationFile

        if(!ddlCfg)
            return null;
            
        def cfg = new File(ContextManager.get().configurations.configurationFile.parentFile,"poller.xml")
    }
    
    static String resolveGatewayRoot() {
        def cfg = getConfigFile()
        def xml = new XmlSlurper().parse(cfg)
        return xml.gatewayRoot.text()
    }
    
    static void load() {
        
        def cfg = getConfigFile()
        if(!cfg || !cfg.exists())
            return
            
        def xml = new XmlSlurper().parse(cfg)
        
        PollGroupCommand pgc = new PollGroupCommand(ctx: new ContextState([
               cxpHost: xml.cxpHost.text(),
               cxpProtocol: xml.cxpProtocol.text(),
               cxpPath: xml.cxpPath.text(),
               groupName: xml.groupName.text(),
               auth: xml.auth.text(),
               gatewayRoot: xml.gatewayRoot.text(),
               applianceRoot: xml.applianceRoot.text()
          ]))
        
        pgc.pause = xml.paused.text() == "true"
        
        log.info "Loaded context state ${pgc.ctx} to run PollGroupCommand"  
        pgc.execute(new CommandBlock("pollgroup"))
    }
}

