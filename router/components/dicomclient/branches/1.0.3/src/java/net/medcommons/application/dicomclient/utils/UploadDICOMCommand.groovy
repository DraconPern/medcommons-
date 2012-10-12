/**
 * $Id$
 */
package net.medcommons.application.dicomclient.utils

import org.json.JSONObjectimport org.dcm4che2.data.DicomObjectimport org.dcm4che2.data.Tagimport java.lang.IllegalArgumentException
import java.net.URLEncoderimport net.medcommons.application.utils.JSONSimpleGETimport org.apache.log4j.Loggerimport java.util.concurrent.Future/**
 * An upload which sends up specific DICOM study / series from
 * a known location.
 * 
 * @author ssadedin
 */
public class UploadDICOMCommand extends QuickStartUpload {

	private static Logger log = Logger.getLogger(UploadDICOMCommand.class);
	
    public Future<JSONObject> execute(CommandBlock params) {
        
        def props = params.properties
        
        if(!props.sourceLocation)
            throw new IllegalArgumentException("Parameter sourceLocation is required")
        
        def dir = new File(props.sourceLocation)
        if(!dir.exists()) 
            throw new IllegalArgumentException("Content selected for upload could not be found or was not accessible")
        
        if(!dir.isDirectory())
            throw new IllegalArgumentException("The selected item was of the incorrect type:  a directory or folder was expected")
            
        init(params)
        
        DICOMImportStatus status = new DICOMImportStatus(transferKey: transferKey)
        
        DicomFileChooser.add(status)
        
        // Figure out the series to upload
	    def seriesUIDs = props.series?props.series.split(","):null
	    
	    new Thread({
	        
	        try {
    	        // Upload the directory, filtering on the series that 
    	        // were specified in the command params (if any)
    	        uploadDir(dir, status) { DicomObject dcm -> 
    		        if(!seriesUIDs)
    		            return true
    		        String seriesUID = dcm.getString(Tag.SeriesInstanceUID)
    		        if(!(seriesUID in seriesUIDs)) {
    		            log.info "DICOM object rejected because seriesInstanceUID $seriesUID not in filter " + seriesUIDs
    		        }
    	            seriesUIDs.any{it==seriesUID} 
    	        }
    	        
    	        // If an order reference was supplied, update that reference
    	        // to indicate that it is complete
    	        if(props.callers_order_reference) {
    	            
    	            def orderStatus = null;
    	            def statusCode = ""
    	            if(status.status == "Error") {
    	                orderStatus = 'DDL_ORDER_ERROR'
    	                statusCode = status.message
    	            }
    	            else
    	            if(status.status == "Cancelled") {
    	                orderStatus = 'DDL_ORDER_CANCELLED'
    	            }
    	            else 
    	            if(status.status == "Finished") {
    	                orderStatus = 'DDL_ORDER_UPLOAD_COMPLETE'
    	            }
    	            else {
    	                log.error "Unexpected transfer state ${status.status} encountered"
    	                throw new IllegalStateException("Unexpected transfer state ${status.status} encountered")
    	            }
    	            
    	            log.info "Setting status ${orderStatus} for order " + props.callers_order_reference
    	            def json = new JSONSimpleGET()
    	            def userName = System.getProperty('user.name')?:'Unknown User'
    	            log.info "User = " + userName
    	            def result = json.get(ctx.applianceRoot + "/acct/update_order_status.php?callers_order_reference=" +
    	                    URLEncoder.encode(props.callers_order_reference) +
    	                    "&errorCode="+ URLEncoder.encode(statusCode) +
    				        "&desc="+URLEncoder.encode("DDL Upload")+"&user="+URLEncoder.encode(userName) +
    	                    "&status=${orderStatus}&auth=${ctx.auth}")
    	                    
    	            if(result.getString("status") != "ok")
    	                throw new RuntimeException("Unable to set order status to complete: " + result.getString("error"))
    	        }
	        }
	        catch(Exception ex) {
	            log.error("Failed to upload directory " + dir, ex)
	            status.status = "Error"
	            status.message = ex.message ?: ex.toString()
	        }
	    }).start()
    }
}
