/**
 * 
 */
package net.medcommons.application.dicomclient.utils

import org.apache.log4j.Logger
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