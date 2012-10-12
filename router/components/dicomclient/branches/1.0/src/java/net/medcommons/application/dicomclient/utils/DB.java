package net.medcommons.application.dicomclient.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import net.sourceforge.pbeans.Store;
import net.sourceforge.pbeans.StoreException;
import net.sourceforge.pbeans.data.GenericDataSource;

/**
 * Manages access to {@link Store} objects.
 * <p>
 * {@link Store}s are maintained as thread locals - one Store per thread.  This
 * is necessary due to properties of how pBeans handles caching in the stores.
 * Therefore no two threads ever see the same Store object.   It also means that 
 * distinct Java objects are seen by each thread (the same query will return 
 * *different* Java objects if done by different threads, the *same* Java object
 * if done by the *same* thread).
 * <p>
 * Because the stores are thread-local and automatically use the connection pool there
 * is no need to manage connections or "close" the store after you finish using it.
 * 
 * @author ssadedin
 */
public class DB {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DB.class);
    
    private static ThreadLocal<Store> store = new ThreadLocal<Store>();
    
    private static String strConnectionUrl = null;
    
    private static DataSource dataSource = null;
    
    public static void testMode() {
        
        if(store != null) {
            try {
                shutdown();
                store = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        strConnectionUrl = "jdbc:hsqldb:mem:testdb"+System.currentTimeMillis();
        GenericDataSource ds = new GenericDataSource(); 
        ds.setDriverClassName("org.hsqldb.jdbcDriver"); 
        ds.setUrl(strConnectionUrl);
        dataSource = ds;
    }

    public static void shutdown() throws SQLException {
        dataSource.getConnection().createStatement().execute("SHUTDOWN");
    }
    
    
    public static final Class [] allPersistentClasses = {
        net.medcommons.application.dicomclient.transactions.CCRRef.class,
        net.medcommons.application.dicomclient.transactions.ContextState.class,
        net.medcommons.application.dicomclient.transactions.DownloadQueue.class,
        net.medcommons.application.dicomclient.utils.CxpTransaction.class,
        net.medcommons.application.dicomclient.utils.DicomOrder.class,
        net.medcommons.application.dicomclient.utils.DicomOutputTransaction.class,
        net.medcommons.application.dicomclient.utils.DicomTransaction.class,
        net.medcommons.application.dicomclient.utils.PixDemographicData.class,
        net.medcommons.application.dicomclient.utils.PixIdentifierData.class        
    };
    
    public static void init(File databaseDir) {
		File f = new File(databaseDir, "database");
        strConnectionUrl = "jdbc:hsqldb:file:" + f.getAbsolutePath();
        
        GenericDataSource ds = new GenericDataSource(); 
        ds.setDriverClassName("org.hsqldb.jdbcDriver"); 
        ds.setUrl(strConnectionUrl);
        
        dataSource = ds;
        
        // We have problems with race conditions when different store instances try to 
        // access / initialize tables in parallel.   To avoid it, we initialize them all
        // right here up front
        for(int i=0;; ++i) {
            try {
                Store db = get();
                for(Class c : allPersistentClasses) {
                    db.getStoreInfo(c);
                }
                
                // We succeded, so return now, don't keep looping
                break;
            }
            catch (StoreException e1) {
                
                // Don't do too many retries
                if(i>4)
                    throw e1;
                
                log.error("Failed to initialize database:  waiting 1 second and trying again");
                try { Thread.sleep(1000); } catch (InterruptedException e) {  e.printStackTrace(); }
            }
        }
    }
    
    public static Store get() throws StoreException {
        if(store.get() == null) {
            store.set(new Store(dataSource));
        }
        return store.get();
    }
    
    /**
     * For unit tests only!
     */
    public static void set(Store s) {
        store.set(s);
    }
}
