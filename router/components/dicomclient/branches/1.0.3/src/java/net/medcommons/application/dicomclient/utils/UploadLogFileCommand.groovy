/**
 * 
 */
package net.medcommons.application.dicomclient.utils

import net.medcommons.application.utils.JSONSimpleGETimport java.net.URLEncoder
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.DICOMClientimport java.io.ByteArrayOutputStreamimport java.util.zip.GZIPOutputStreamimport org.apache.commons.codec.binary.Base64import org.apache.commons.codec.net.URLCodecimport org.json.JSONObjectimport java.util.concurrent.Future
import org.apache.log4j.Logger

/**
 * An import that does as many actions upfront as quickly as possible
 * so as to be more responsive and reliable for web users.
 * 
 * @author ssadedin
 */
public class UploadLogFileCommand implements Command {

	private static Logger log = Logger.getLogger(UploadLogFileCommand.class);
     
    public Future<JSONObject> execute(CommandBlock params) {
        
        String to = params.getProperty("to")
        if(!to)
            throw new IllegalArgumentException("Parameter 'to' not supplied")
        
        String problemId = params.getProperty("problemId")
        if(!problemId)
            throw new IllegalArgumentException("Parameter 'problemId' not supplied")
        
        // Send the log file 
        def logContents = DICOMClient.LOG_FILE.text
        
        // GZip log file
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        GZIPOutputStream gzip = new GZIPOutputStream(os) 
        gzip.write(logContents.getBytes("UTF-8"))
        gzip.flush()
        gzip.close()
        
        def gzipped = os.toByteArray()
        log.info "Gzipped log file is ${gzipped.size()} bytes"
        def base64 = Base64.encodeBase64(gzipped)
        log.info "After base64 encoding data is " + base64.size() + " bytes"
        
        ByteArrayOutputStream encodedBytes = new ByteArrayOutputStream(base64.size())
        encodedBytes.write "logfile&problemId=${problemId}&encoding=gzip-b64&description=".getBytes("UTF-8")
        encodedBytes.write new URLCodec().encode(base64)
        encodedBytes.close();
        
        JSONSimpleGET json = new JSONSimpleGET()
        json.post(to, encodedBytes.toByteArray())
        
        log.info "Successfully sent log file for problem ${problemId} to ${to}"
        return null
    }
    
    public static void doStandaloneUpload() {
        def params = new CommandBlock("uploadlogs")
        
        def root = ContextManager.get().currentContextState.gatewayRoot 
        root = root ?: System.getProperty("command_applianceRoot")
        
        params.addProperty "to", root + "/router/ProblemReport.action"
        params.addProperty "problemId", (new Date()).time + "" + Math.round(Math.random() * 10000)
        
        def cmd = new UploadLogFileCommand()
        cmd.execute(params)
    }
}
