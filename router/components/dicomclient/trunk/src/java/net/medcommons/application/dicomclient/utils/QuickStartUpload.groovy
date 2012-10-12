/**
 * 
 */
package net.medcommons.application.dicomclient.utils

import net.medcommons.application.dicomclient.anon.Anonymizer;
import net.medcommons.application.dicomclient.dicom.CstoreScpimport org.dcm4che2.data.DicomObjectimport org.apache.log4j.BasicConfiguratorimport javax.swing.JOptionPaneimport org.apache.log4j.Loggerimport java.lang.Runnableimport javax.swing.SwingUtilities;
import net.medcommons.application.dicomclient.ContextManagerimport net.medcommons.application.utils.Strimport net.medcommons.application.utils.MonitorTransferimport net.medcommons.modules.services.interfaces.DicomMetadataimport net.medcommons.application.dicomclient.http.utils.Voucherimport net.medcommons.application.dicomclient.transactions.ContextStateimport net.medcommons.application.utils.JSONSimpleGETimport org.apache.commons.codec.binary.Base64import org.hsqldb.lib.InOutUtilimport net.medcommons.modules.utils.FileUtilsimport net.medcommons.modules.utils.FormatUtils;

import java.net.URLEncoderimport net.medcommons.application.dicomclient.UploadHandlerimport net.medcommons.modules.crypto.SHA1import net.medcommons.modules.cxp.protocol.MeteredSocketFactoryimport net.medcommons.modules.cxp.client.MeteredSocketListenerimport java.lang.Overrideimport net.medcommons.modules.transfer.TransferBaseimport net.medcommons.modules.utils.CancelledStreamExceptionimport net.medcommons.application.dicomclient.dicom.ImageSenderimport net.medcommons.modules.utils.dicom.DicomNameParserimport org.json.JSONObjectimport java.util.concurrent.Future
/**
 * An import that does as many actions upfront as quickly as possible
 * so as to be more responsive and reliable for web users.
 * 
 * @author ssadedin
 */
public class QuickStartUpload  implements Command {

	private static Logger log = Logger.getLogger(QuickStartUpload.class);
	
	/**
	 * The transfer key for this upload
	 */
    def transferKey 
    
    def ctx 

    boolean createVoucher = true

    String documentType = null

    def result 
    
    /**
     * Initializes default state based on context from 
     * given parameters.  Clears out all existing transactions
     * in preparation for new transaction.
     */
    public init(CommandBlock params) {
         
        this.ctx = params.toContextState()
        
        log.info "Initializing context state to " + ctx
        
        // The command could be simply triggering the upload or it could
        // be setting the whole context for the upload at the same time
        if(params.properties.cxphost) 
            ContextManager.get().setCurrentContextState(ctx);
        else
            this.ctx = ContextManager.get().currentContextState

        MonitorTransfer.clear();
        ClearUtils.clearAll();
        
	    def seed = String.valueOf(System.currentTimeMillis() + this.toString())
	    transferKey = new SHA1().initializeHashStreamCalculation().calculateStringHash(seed) 

        if(params.properties.voucher != null && params.properties.voucher == "false")  {
            log.info "No voucher for upload $transferKey"
            createVoucher = false
        }
	    
        this.documentType = params.properties.documentType
    }
	
	/**
	 * Execute the command
	 */
    public Future<JSONObject> execute(CommandBlock params) { 

        init(params);
        
        SwingUtilities.invokeLater {
            new DicomFileChooser(null,true, { File dir, DICOMImportStatus status ->
                status.documentType = documentType
                uploadDir(dir,status)
                result = new JSONObject().put("status","ok")
					                     .put("importStatus", status.toJSON())
					                     .put("transferKey", transferKey)
            })
        } as Runnable

        
        return [ isDone: { result != null },  get: { result } ] as Future<JSONObject>
    }
    
	/**
	 * Upload given directory with status reported to 
	 * given status object.
	 */
    public uploadDir(File dir, DICOMImportStatus status, Anonymizer anon = null, Closure filter = null) {
        def tempFiles = []
        try {
            status.status = "Initializing"
            status.transferKey = transferKey;
            
            // Look for dicom
            File dcm = ScanFolderCommand.findDicom(dir)
            if(!dcm) 
                throw new Exception("No DICOM Files found in selection")
                
            if(anon) {
                dcm = anon.anonymize(dcm);
                tempFiles << dcm
            }
    
            log.info("Found dicom file " + dcm.name)
            
            // Get the patient information out
            DicomMetadata meta = new ExtractFileMetadata(dcm).parse()
                
            // Create a voucher / patient
            log.info("Found patient " + meta.patientName + " in imported DICOM file " + dcm.absoluteFile.name)
            
            StatusDisplayManager.get().setMessage("Uploading $meta.patientName", 
                                                  "Patient $meta.patientName is being uploaded from location $dir");
            
            // Attach the first image to the patient
            def sender = new ImageSender(status, anon)
            
            def patientName = DicomNameParser.parse(meta.patientName)
            if(createVoucher) {
	            Voucher voucher = 
	                new Voucher(ctx, patientName.given, patientName.family, meta.patientSex, UploadHandler.DEFAULT_DICOM_VOUCHER_SERVICE)
	            voucher.purpose = meta.studyDescription ?: meta.seriesDescription ?: 'DICOM on Demand Upload'
	            voucher.createVoucher()
	            VoucherMenu voucherMenu = StatusDisplayManager.get().addPatientMenu(ctx, voucher)
                sender.propertyChange = { evt ->
                    Swing.later {
                        voucherMenu.percentComplete = sender.lastProgress
                    } 
                }
            }
            
            long startTimeMs = System.currentTimeMillis()
            
            sender.sendDir(ctx,dir,filter)
            
            // Send the 'finished' message to remove warning from the CCR
            def url = "${ctx.gatewayRoot}/router/put/${ctx.storageId}?auth=${ctx.auth}&status=COMPLETE"
            if(status.documentType)
                url += "&documentType=${documentType}"
                    
            def response = new JSONSimpleGET().get(url,3)
            if(response.getString("status") != "ok")
                throw new RuntimeException("Unable to set status to COMPLETE for upload: " + response.optString("error"))
            
            // It's possible that the user cancels at just the right moment and
            // doesn't generate an exception (because the particular send in progress
            // has finished transmitting, but the next one hasn't started).
            // For this case, don't clobber the status
            if(status.status != "Cancelled") {
                status.status = "Finished"
                StatusDisplayManager.get().setMessage("Upload Finished", "Uploaded ${sender.scan.files.size()} Images (" + 
                                                      FormatUtils.formatMB(sender.scan.totalBytes) + " MB, " +
                                                      FormatUtils.formatKbPerSecond(sender.scan.totalBytes, System.currentTimeMillis() - startTimeMs) + " KB/s)")
            }
            
            // Trim imports so that they do not grow too big
            DicomFileChooser.trimImports()
        }
        catch(CancelledStreamException e) {
            log.info "Upload ${transferKey} cancelled"
            status.status = "Cancelled"
        }
        catch(Exception e) {
            log.error("Failed to import files from dir " + dir.name,e)
            status.message = e.message?: "Error ${e.class.name}"
            status.status = "Error"
        }
        finally {
            tempFiles.each { it.delete() }
        }
    }
    
    public static void main(String [] args) {
        
        BasicConfigurator.configure();
        
        println "Testing QuickStartUpload"
        println ""

        def q = new QuickStartUpload()
        def dcm = q.findDicom("f:\\" as File)
        
        println "Found DICOM " + dcm
        
        // def nodcm = q.findDicom("c:/temp/share" as File)
        
        // println "No DICOM = " + nodcm
    }
    
}
