/**
 * 
 */
package net.medcommons.application.dicomclient.utils

import org.apache.log4j.Loggerimport net.medcommons.application.dicomclient.dicom.CstoreScpimport org.json.JSONObjectimport javax.swing.SwingUtilitiesimport org.json.JSONArrayimport net.medcommons.modules.services.interfaces.DicomMetadataimport org.dcm4che2.data.Tagimport net.medcommons.application.dicomclient.http.CommandServletimport java.util.concurrent.Future
/**
 * @author ssadedin
 */
public class SelectFolderCommand implements Command {

	private static Logger log = Logger.getLogger(ScanFolderCommand.class);
	
    public Future<JSONObject> execute(CommandBlock params) { 
        
		JSONObject result = null
        File selectedLocation = null
        
        SwingUtilities.invokeLater {
	        new DicomFileChooser(null,true,{ File dir, DICOMImportStatus status ->
	            selectedLocation = dir
	            try {
		            def json = new JSONObject()
			            .put("selectedLocation", selectedLocation.absolutePath)
			            .put("status","ok")
		            result = json
	            }
	            catch(Exception ex) {
	                log.error("Failed to scan folder: " + dir, ex)
	                result = new JSONObject().put("status","failed").put("error",ex.message)
	            }
	        },true,false);
        }
        
        return [ isDone: { result != null },  get: { result } ] as Future<JSONObject>
    }
}
