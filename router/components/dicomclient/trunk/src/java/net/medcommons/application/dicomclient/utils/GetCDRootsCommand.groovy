package net.medcommons.application.dicomclient.utils

import org.json.JSONArray;
import org.json.JSONObjectimport org.dcm4che2.data.DicomObjectimport org.dcm4che2.data.Tag
import java.io.File;
import java.lang.IllegalArgumentException
import java.net.URLEncoderimport org.apache.log4j.Logger;

import net.medcommons.application.dicomclient.anon.Anonymizer;
import net.medcommons.application.dicomclient.anon.AptusAnonymizer;
import net.medcommons.application.dicomclient.command.CommandDaemon;
import net.medcommons.application.utils.JSONSimpleGETimport org.apache.log4j.Loggerimport java.util.concurrent.Future
import javax.swing.filechooser.FileSystemView;
/**
 * An upload which sends up specific DICOM study / series from
 * a known location.
 * 
 * @author ssadedin
 */
public class GetCDRootsCommand implements Command {

	private static Logger log = Logger.getLogger(GetCDRootsCommand.class);
	
    public Future<JSONObject> execute(CommandBlock params) {
        JSONObject result = null
	    new Thread({
            try {
                JSONArray cdroots = new JSONArray()
                FileSystemView view = FileSystemView.getFileSystemView();
                String osName = System.getProperty("os.name");
                log.info("OS name is:" + osName);
                
                if ("Mac OS X".equals(osName)) {
                    new File("/Volumes").listFiles().each {
                        def drv = new JSONObject().put("drive", it.absolutePath).put("label",view.getSystemDisplayName(it))
                        cdroots.put(drv)
                    }
                }
                else {
                    File [] roots = File.listRoots();
                    for(File root : roots) {
                        String fname = root.getAbsolutePath();
                        def drv = new JSONObject().put("drive", fname).put("label",view.getSystemDisplayName(root))
                        cdroots.put(drv)
                    }
                }
                result = new JSONObject().put("status", "ok")
                                         .put("roots",cdroots)
            }
	        catch(Exception ex) {
	            log.error("Failed to scan for cds ", ex)
                result = new JSONObject().put("status", "failed")
                result.put("error", ex.getMessage())
            }
	    }).start()
        return [ isDone: { result != null },  get: { result } ] as Future<JSONObject>
    }
}
