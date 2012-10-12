package net.medcommons.application.dicomclient.utils;

import static net.medcommons.application.dicomclient.utils.Params.where;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.transactions.*;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.utils.FileUtils;
import net.sourceforge.pbeans.Store;

/**
 * Utility methods for clearing out database and data from DDL
 * 
 * @author ssadedin
 */
public class ClearUtils {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ClearUtils.class);

    /**
     * Clear all significant persistent state.  Note: we do not clear
     * the ContextState because it is used to track known state hosts
     * across sessions for security reasons.
     */
    public static void clearAll() {
        clearAll(DicomTransaction.class);
        clearAll(DicomOutputTransaction.class);
        clearAll(CxpTransaction.class);
        clearAll(DicomMetadata.class);
        clearAll(CCRRef.class);
        clearAll(DownloadQueue.class);
        clearCache();
        PatientMatch.flushCache();
    }
    
    public static void clearCache() {
        
        File[] files = new File[] { ContextManager.get().getUploadCache(),  ContextManager.get().getDownloadCache() };
        for(File f : files) {
            try {
                if(f.exists()) {
                    if(!FileUtils.deleteDir(f)) {
                        log.warn("Unable to delete cache directory " + f);
                    }
                }
                if(!f.mkdirs()) {
                    log.warn("Unable to recreate cache directory " + f);
                }
            }
            catch(Exception e) {
                log.warn("Unable to delete cache files",e);
            }
        }
    }

    public static void clearAll(Class clazz){
        Store db = DB.get();
        List<Object> objs = db.select(clazz).all();
        log.info("About to delete " + objs.size() + " entries for class " + clazz.getCanonicalName());
        Iterator<Object> iter = objs.iterator();
        while(iter.hasNext()){
            Object obj = iter.next();
            db.delete(obj);
        }
    }
    
    public static void clearCompleted(Class clazz){
        Store db = DB.get();
        List<Object> objs = db.all(clazz, where("status", DicomTransaction.STATUS_COMPLETE));
        log.info("About to delete " + objs.size() + " entries for class " + clazz.getCanonicalName());
        Iterator<Object> iter = objs.iterator();
        while(iter.hasNext()){
            Object obj = iter.next();
        }
    }
}
