package net.medcommons.application.dicomclient.utils

import org.apache.log4j.Logger
import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.dicom.CstoreScpimport org.json.JSONObjectimport javax.swing.SwingUtilitiesimport org.json.JSONArray
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.services.interfaces.DicomMetadataimport net.medcommons.modules.utils.dicom.DicomNameParser;

import org.dcm4che2.data.Tagimport net.medcommons.application.dicomclient.http.CommandServletimport net.medcommons.application.utils.JSONSimpleGET;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Futureimport java.net.URLEncoder; 

/**
 * Scans a specified folder on the user's computer and posts results to the gateway.
 * A key to the results is returned which can be used to query the gateway for 
 * the results.
 * 
 * @author ssadedin
 */
public class ScanFolderCommand implements Command {

	private static Logger log = Logger.getLogger(ScanFolderCommand.class);
    
    /**
     * Used to post results to a gateway
     */
    JSONSimpleGET post = new JSONSimpleGET();
	
    public Future<JSONObject> execute(CommandBlock params) { 
        
		JSONObject result = null
        File selectedLocation = 
          params.properties.selectedLocation?new File(params.properties.selectedLocation):null
        
        if(!selectedLocation) {
            SwingUtilities.invokeLater {
    	        new DicomFileChooser(null,true,{ File dir, DICOMImportStatus status ->
                    
                    try {
    	                if(status.status == "Cancelled") 
    		                throw new RuntimeException("File selection was cancelled");
                        
    		            if(dir.isFile()) {
    		                log.info "Path ${dir.absolutePath} selected by user is a file, not a directory.  Will attempt to resolve parent directory"
    		                dir = dir.parentFile
    		            }
    	                
    		            selectedLocation = dir
                        if(!dir.exists()) 
                            throw new FileNotFoundException("The file " + dir.absolutePath + " could not be found.")
                        
    		            CommandServlet.signal("startScanning");
    		            // Thread.sleep(30000)
                        
    		            result = postScanResult(selectedLocation)
    	            }
    	            catch(Exception ex) {
    	                log.error("Failed to scan folder: " + dir, ex)
    	                result = new JSONObject().put("status","failed").put("error",ex.message)
    	            }
    	            finally {
    	                CommandServlet.signal("stopScanning");
    	            }
    	        },true,false);
            }
        }
        else {
            new Thread({
                try {
                    CommandServlet.signal("startScanning");
                    result = postScanResult(selectedLocation)
                }
                catch(Exception ex) {
                    log.error("Failed to scan folder: " + selectedLocation, ex)
                    result = new JSONObject().put("status","failed").put("error",ex.message)
                }
                finally {
                    CommandServlet.signal("stopScanning");
                }

            }).start()
        }
        
        return [ isDone: { result != null },  get: { result } ] as Future<JSONObject>
    }
    
    def postScanResult(File dir) {
        def json = new JSONObject()
        json = scanDir(dir)
        json.put("selectedLocation", dir.absolutePath)
            .put("status","ok")
            
        // Post the result to the gateway
        def ctx = ContextManager.get().currentContextState
        Configurations cfg = ContextManager.get().getConfigurations()
        
        String resultData = json.toString()
        JSONObject postResult = post.post(ctx.applianceRoot +
            "/router/ddl?result&ddlid=${cfg.DDLIdentity}&cmd=scanfolder",
            ("data="+URLEncoder.encode(resultData, "UTF-8")).getBytes("UTF-8"))
            
        String sha1 = SHA1.sha1(resultData)
        if(postResult.status != "ok")
            throw new RuntimeException("Unable to post result " + sha1 + " to gateway: "+postResult.error)
            
        json.put("key", sha1)
        
        log.info ("Posted result $sha1 to gatway")
        
        return json
    }
    
    /**
     * Scan the given directory recursively looking for DICOM files
     * and return a map hierarchy with details of the studies and series 
     * found.
     */
    def scanDir(File dir) {
        // Key is studyUID
        def studies = [:]
        
        int count = 0
        def dicomName = null
        dir.eachFileRecurse { f ->
            ++count
            
            // importStatus.message = "Scanned ${count} files"
            if(f.absolutePath =~ /\.svn/)
                return
                
            if(f.isDirectory())
                return
            
            def dcm = CstoreScp.getDICOMObject(f,-1)
            if(!dcm)
                return
                
            def studyUID = dcm.getString(Tag.StudyInstanceUID)?:''
            def study = studies[studyUID]
            if(!study) {
                log.info "Found DICOM file " + f + " with study UID = " + studyUID 
                DicomMetadata metaData = new ExtractFileMetadata(dcm).parse()
                study = [
	                description: metaData.studyDescription,
	                patient: [ name: metaData.patientName, id: metaData.patientId, dateOfBirth: metaData.patientDateOfBirth ],
	                modality: metaData.modality,
	                date: metaData.studyDate,
                    series: [:],
                    patientNames:  new HashSet()
                ]
                studies[studyUID] = study
            }
            
            def name = ExtractFileMetadata.extractPatientName(dcm)
            if(!dicomName)
                dicomName = name
            study.patientNames.add(name)
            
            def seriesUID = dcm.getString(Tag.SeriesInstanceUID)
            if(!study.series[seriesUID]) {
                log.info "Found series with series UID = " + seriesUID
                def series = [
                  description : dcm.getString(Tag.SeriesDescription),
                  seriesUID : seriesUID,
                  date : ExtractFileMetadata.extractSeriesDate(dcm)
              ]
              study.series[seriesUID] = series  
            }
        }
        
        def json = toJSON(studies)
        log.info "Found studies " + studies.toString()

        def patientsJSON = toJSON(studies.collect{it.value.patientNames}.sum())

        log.info "JSON for patients:  " + patientsJSON
        
        
        def result = new JSONObject().put("studies", json)
					             .put("patientNames", patientsJSON)
        
        if(dicomName) {
            def parsed = DicomNameParser.parse(dicomName)
            result.put("parsedName", 
                new JSONObject().put("givenName", parsed.given)
                                .put("familyName", parsed.family))        }
        
        // println "studies JSON = " + json.toString()
        return result                               
    }
    
    def toJSON(def obj) {
        if(obj instanceof List || obj instanceof Set) {
            return obj.inject(new JSONArray()) { arr, val -> arr.put(toJSON(val)); return arr; }
        }
        else
        if(obj instanceof Map) {
            return obj.inject(new JSONObject()) { o, entry -> 
                if(entry.key == null) {
                    log.warn "Ignoring entry with null key " + entry
                    return o;
                }
                o.put(entry.key, toJSON(entry.value)); return o; 
            }
        }
        else
        if(obj instanceof Date) {
            return obj.time
        }
        else
            return obj
    }
   
    /**
     * Find the first DICOM file in the specified folder and return
     * the File object for it.
     */
    static def findDicom(File dir) {
         
        for(f in dir.listFiles()) {
            def dcm = null;
                
            if(f.name == ".svn")
                continue
                
            if(f.isDirectory() )
                f = dcm = findDicom(f)
            else {
	            println "Testing file: " + f
	            dcm = CstoreScp.getDICOMObject(f)
            }
            
            if(dcm)
	            return f;
        }
    }
 
}
