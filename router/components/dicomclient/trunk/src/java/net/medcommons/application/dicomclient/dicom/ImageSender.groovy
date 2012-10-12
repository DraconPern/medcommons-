/**
 *  $Id$
 */
package net.medcommons.application.dicomclient.dicom

import net.medcommons.application.dicomclient.anon.Anonymizer;
import net.medcommons.application.dicomclient.transactions.ContextStateimport net.medcommons.application.utils.JSONSimpleGETimport net.medcommons.application.utils.MonitorTransferimport net.medcommons.modules.cxp.protocol.MeteredSocketFactoryimport net.medcommons.modules.cxp.protocol.MeteredSocketFactoryimport net.medcommons.modules.transfer.TransferBaseimport net.medcommons.modules.cxp.client.MeteredSocketListenerimport org.apache.log4j.Loggerimport org.apache.log4j.BasicConfiguratorimport net.medcommons.modules.utils.FileUtilsimport net.medcommons.modules.utils.FormatUtils;

import org.apache.commons.codec.binary.Base64
import groovy.beans.Bindable;

import java.net.URLEncoderimport org.dcm4che2.data.Tag
import net.medcommons.application.dicomclient.utils.*;
import net.medcommons.modules.utils.CancelledStreamExceptionimport net.medcommons.application.dicomclient.ContextManagerimport org.mortbay.util.IOimport java.io.FileInputStreamimport java.util.zip.GZIPOutputStream
import javax.swing.SwingUtilities;


/**
 * A simple extension of the transfer monitor to let a closure
 * intercept the calls to get progress.
 */
public class MonitorTransferX extends MonitorTransfer {
    
    def progressReporter
    
    MonitorTransferX(String transferKey, int studyCount, Closure progressReporter) {
        super(transferKey,"Upload", studyCount)
        this.progressReporter = progressReporter
    }
    
    public float getProgress() {
        return progressReporter()
    }
}


/**
 * Sends images to gateway using POST interface
 * 
 * @author ssadedin
 */
public class ImageSender {
    
	private static Logger log = Logger.getLogger(ImageSender.class)
	
    def meteredIO = null;
    
    def totalLength = 0L;
    
    def totalPreviousImageBytes = 0L;
    
    def MAX_RETRIES = 5
    
    /**
     * The transfer monitor used for this image sender.
     */
    MonitorTransfer transfer = null
    
    /**
     * Import status object - used to send status info
     * up to UI
     */
    DICOMImportStatus importStatus = null;
    
    /**
     * The results of the file scan
     */
    def scan = null
    
    Anonymizer anonymizer
    
    @Bindable
    float lastProgress = 0.0f
    
    boolean useFileUploads = ContextManager.get().configurations.useRawFileUploads
    
    boolean gzip = true;
    
    File tmpDir = new File(ContextManager.get().configurations.baseDirectory,"tmp")
    
    /**
     * Create a new ImageSender
     */
    ImageSender(DICOMImportStatus importStatus, Anonymizer anonymizer=null) {
        transfer = new MonitorTransferX(importStatus.transferKey, 1, {
	        if(meteredIO && totalLength) {
	            lastProgress = Math.min(1.0f,(totalPreviousImageBytes + meteredIO.outputBytes) / totalLength)
	            // log.info "Computed progress = min(100.0f, (${totalPreviousImageBytes} + ${meteredIO.outputBytes})/${totalLength}) = ${lastProgress}"
	        }
	        return lastProgress
        })
        this.importStatus = importStatus
        this.anonymizer = anonymizer
        tmpDir.mkdirs()
    }
    
    /**
     * Sends all DICOM files in the given directory
     */ 
    def sendDir(ContextState ctx, File dir, Closure filter) {
        
        importStatus.status = "Scanning"
        
        // Scan the directory        
        scan = scanDir(dir, filter)
        
        // The total sent length is actually the byte length inflated by 1/3
        // because we are sending with base64 encoding
        totalLength = useFileUploads ? scan.totalBytes : scan.totalBytes * 1.33 
        
        log.info "Found ${scan.studies.size()} studies in directory ${dir}"
        transfer.queuedStudies = scan.studies.size()
        
        log.info "Estimated total bytes to send = ${totalLength}"
        
        importStatus.status = "Uploading"
        
        // Send the images
        scan.files.eachWithIndex { f, i ->
            // Don't try and send more if the transfer was cancelled
            if(importStatus.status == "Cancelled")
                return;
  
            log.info "Sending image ${i} of ${scan.files.size()}"
            
            sendWithRetry(ctx,f)

            if(ContextUtils.isJDK6Orlater()) {
                Swing.later { 
    	            StatusDisplayManager.get().toolTip = "Uploading " + FormatUtils.formatNumberTenths(100*transfer.progress) + " % complete"
                }
            }
            
        }
        
    }
    
    /**
     * Try and send the file, retrying up to MAX_RETRIES times 
     * if failures occur.
     */
    def sendWithRetry(ContextState ctx, File f) {
        int errorCount = 0
        boolean retry = true
        while(retry) {
            try {
                sendImage(ctx,f)
                retry = false
            }
            catch(CancelledStreamException e) { // Don't retry if the user cancelled!
                throw e
            }
            catch(Exception e) {
                if(errorCount > MAX_RETRIES)
                    throw e
                    
                // Set to null because otherwise the sent
                // bytes will get counted in the progress
                meteredIO = null
                ++errorCount
                log.warn "Failed to send image " + f + ", errored " + errorCount +  " times (${e.message})"
                Thread.sleep(Math.pow(1.7, errorCount).intValue() * 1000)
                
                retry = true
            }
        }
    }
     
    /**
     * Scans the specified directory recursively and returns a map
     * with attributes:
     * <ul>
     *   <li>'files'        - containing a list of all DICOM files found
     *   <li>'totalBytes'   - total size of all files found
     *   <li>'studies'      - set containing all study instance uids found
     * </ul>
     */
    def scanDir(File dir, Closure filter) {
        
        def allFiles = []
        def totalBytes = 0L
        def studies = [] as Set
        int count = 0;
            
        dir.eachFileRecurse { File f ->
            ++count
            importStatus.message = "Scanned ${count} files"
            if(f.absolutePath =~ /\.svn/)
                return
                
            if(f.isDirectory())
                return
            
            def dcm = CstoreScp.getDICOMObject(f)
            if(!dcm)
                return
                
            if(filter && !filter(dcm))
                return
            
            // Found a DICOM file - index it
            allFiles << f
            totalBytes += f.length()
            log.info "Found image from study " + dcm.getString(Tag.StudyInstanceUID)
            studies << dcm.getString(Tag.StudyInstanceUID)
        }
        
        log.info "Found ${allFiles.size()} files (${totalBytes} bytes)"
        return [files: allFiles, totalBytes: totalBytes, studies: studies]
    }
    
    /**
     * Upload the given image file to the specified patient
     */
    def sendImage(ContextState ctx, File dcm) {
        
        def fileToUpload = dcm
        def tempFiles = []
        
        if(this.anonymizer) {
            fileToUpload = this.anonymizer.anonymize(dcm)
            tempFiles << fileToUpload
        } 
        
        if(gzip) {
            def tmpFile = new File(tmpDir, dcm.name)
            def os = new GZIPOutputStream(new FileOutputStream(tmpFile))
            os.withStream {
                fileToUpload.withInputStream { is ->
                    os << is
                }
            }
            fileToUpload = tmpFile
            tempFiles << tmpFile
            
            // Adjust length to reflect gain from gzip
            totalLength -= (dcm.length() - fileToUpload.length())
        }
        
        if(useFileUploads) {
            sendUsingFileUpload(ctx,fileToUpload)
        }
        else {
            sendUsingPostData(ctx,fileToUpload)
        }
        tempFiles.each { if(!it.delete()) log.warn("Unable to delete temporary file " + it) }
    }
    
        
    def sendUsingPostData(ContextState ctx, File dcm) {
        
        def json = new JSONSimpleGET()
        def bytes = FileUtils.readBytes(dcm)
        
        log.info "Raw file length to send is " + bytes.size()
        
        def base64 = new String(Base64.encodeBase64(bytes), "ASCII")
        def postData = ("image=" + URLEncoder.encode(base64,"UTF-8") ).getBytes("UTF-8")
        
        // If total length is not set, assume that we are being called
        // stand alone, not by the sendDir method, so set total length
        // based on the post data we know we are going to send
        if(!totalLength)
	        totalLength = postData.length
        
	    def socketListener =  {
           if(meteredIO != null) {
               log.info "New metered IO connection detected: accumulating ${meteredIO.outputBytes} bytes to sent total"
               totalPreviousImageBytes += meteredIO.outputBytes
           }
           meteredIO = it 
        } as MeteredSocketListener  
        MeteredSocketFactory.register(ctx.cxpProtocol, socketListener)
        
        transfer.state = MonitorTransfer.TxState.UPLOADING
        transfer.client = [cancelStream:{ meteredIO.cancelStream()} ] as TransferBase
        
        log.info("Attaching parcel image to account ${ctx.storageId} at ${ctx.gatewayRoot} with ${postData.length} bytes of post data")
        
        def url = "${ctx.gatewayRoot}/router/put/${ctx.storageId}?auth=${ctx.auth}&gzip=${gzip}"
        if(importStatus.documentType) 
            url += "&documentType=${importStatus.documentType}"
        
        def response = json.post(url,postData) 
                
        if(response.getString("status") != "ok") 
            throw new RuntimeException("Failed to add image " + dcm.absoluteFile.name + " to account ${ctx.storageId}: " + response.optString("error"))
        
        log.info("Successfully attached image ${dcm.name} to storage id ${ctx.storageId}");
    }
    
    def sendUsingFileUpload(ContextState ctx, File dcm) {
        
        log.info "Sending image data using multipart file upload"
        
        def json = new JSONSimpleGET()
        
        // If total length is not set, assume that we are being called
        // stand alone, not by the sendDir method, so set total length
        // based on the post data we know we are going to send
        if(!totalLength)
            totalLength = dcm.length()
        
        def socketListener =  {
           if(meteredIO != null) {
               log.info "New metered IO connection detected: accumulating ${meteredIO.outputBytes} bytes to sent total"
               totalPreviousImageBytes += meteredIO.outputBytes
           }
           meteredIO = it 
        } as MeteredSocketListener  
        MeteredSocketFactory.register(ctx.cxpProtocol, socketListener)
        
        transfer.state = MonitorTransfer.TxState.UPLOADING
        transfer.client = [cancelStream:{ meteredIO.cancelStream()} ] as TransferBase
        
        log.info("Attaching parcel image to account ${ctx.storageId} at ${ctx.gatewayRoot} with ${dcm.length()} bytes of post data")
        
        def url = "${ctx.gatewayRoot}/router/put/${ctx.storageId}?auth=${ctx.auth}&gzip=${gzip}"
        if(importStatus.documentType) 
            url += "&documentType=${importStatus.documentType}"
                
        def response = json.post(url,dcm) 
        if(response.getString("status") != "ok") 
            throw new RuntimeException("Failed to add image " + dcm.absoluteFile.name + " to account ${ctx.storageId}: " + response.optString("error"))

        log.info("Successfully attached image ${dcm.name} to storage id ${ctx.storageId}");
    }
    
    public static void main(String [] args) {
        
        BasicConfigurator.configure();
        
        println "Testing ImageSender"
        println ""
        
        
        TransferBase t = [cancelStream:{ meteredIO.cancelStream()} ] as TransferBase        
        
        println "Made transfer base " + t
        
        /*
        ImageSender is = new ImageSender("test")
        
        def results = is.scanDir('e:\\' as File)
        
        println "Found " + results.files.size() + " files, total bytes = ${results.totalBytes}, studies = ${results.studies.size()}"
        */
    }
}
