/*
 * $Id: SmallFileBackupService.java 3529 2009-10-30 07:51:18Z ssadedin $
 * Created on 21/10/2008
 */
package net.medcommons.modules.backup;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.*;
import java.security.Key;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.spec.SecretKeySpec;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.*;
import net.medcommons.modules.services.interfaces.BackupException;
import net.medcommons.modules.services.interfaces.BackupService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.modules.utils.metrics.TimeSampledMetric;
import net.medcommons.s3.S3Client;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * A backup service for small files.
 * <p>
 * The files are gzipped, encrypted and sent to the backup service in a single chunk.  
 * This service is not intended for backuping up large files such as images.
 * <p>
 * This service wraps the underlying {@link BackupService} used by the appliance
 * to provide a service usable directly by code for backups of small files that
 * need encryption layered on top and which should be more responsive than
 * the backup queue designed for large files.
 * 
 * @see S3BackupService
 * @author ssadedin
 */
public class SmallFileBackupService implements BackupService {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SmallFileBackupService.class);
    
    /**
     * The total number of backups performed.  This is incremented for each
     * backup and used to name marker files so that they do not over write each
     * other if a single file is backed up multiple times.
     */
    public static AtomicInteger backupCounter = new AtomicInteger(0);
    
    /**
     * Thread pool for executing backups
     */
    private static ScheduledExecutorService backupExecutor =
        Executors.newScheduledThreadPool(Configuration.getProperty("ProfileServiceBackupThreads", 3));
    
    static {
        Metric.register("SmallFileBackupService.AverageBackupDelayMs", 
                        new TimeSampledMetric(Metric.getInstance("SmallFileBackupService.BackupDelayMs"), 2000, 20));
        Metric.register("SmallFileBackupService.AverageBackupJobTimeMs", 
                        new TimeSampledMetric(Metric.getInstance("SmallFileBackupService.BackupJobTimeMs"), 2000, 20));
    }
    
    static HashSet<String> activeBackups = new HashSet<String>();
    
    private BackupService backupService;
    
    /**
     * Key for encrypting data sent to S3
     */
    private Key key;

    /**
     * Create an instance of this service.
     * 
     * @throws ConfigurationException
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public SmallFileBackupService(BackupService backupService) throws BackupException {
        init();
        this.backupService = backupService;
    }

    /**
     * Initialize the service by acquiring or creating an encryption key
     * @throws BackupException 
     */
    protected void init() throws BackupException {
        try {
            boolean backupEnabled = Configuration.getProperty("Backup_Documents", false);
            if(backupEnabled) {
                
                // Read or generate encryption key
                File keyFile = new File(".aes-key");
                if(!keyFile.exists()) 
                    throw new BackupException("Backup enabled but unable to read key file "+keyFile.getAbsolutePath()+" to perform encryption.");
                    
                SHA1 sha1 = new SHA1();
                sha1.initializeHashStreamCalculation();
                byte[] hashKeyBytes = sha1.calculateHash(keyFile);
                log.info("AES Key  = " + Utils.hexEncodeBytes(hashKeyBytes, 0, 16));
                SecretKeySpec key = new SecretKeySpec(hashKeyBytes,0,16,"AES");
                setKey(key);
            }
        }
        catch (FileNotFoundException e) {
            throw new BackupException("Unable to initialize SmallFileBackupService", e);
        }
        catch (IOException e) {
            throw new BackupException("Unable to initialize SmallFileBackupService", e);
        }
    }
    
    /**
     * Backs up the specified file by sending it to S3
     * <p>
     * The backup is scheduled as a job passed to a backup thread pool, {@link #backupExecutor}, 
     * and thus executes asynchronously to the calling thread.
     * <p>
     * Prior to scheduling the backup, however, a <i>marker file</i> is written that
     * records the name of the file to be backed up to a directory, data/backup_queue/. This
     * is intended to allow backups to be restarted if a gateway is shut down or otherwise
     * fails in midstream while backing up content.
     * <p>Failures in the asynchronous backup job cause the backup to be retried a number 
     * of times with exponential backoff.  Should the retry count exceed the <i>MaxS3BackupAttempts</i>
     * configuration value (default = 6), a fatal error is logged and the ProfileBackupFailures
     * counter is increased.
     * 
     * @param f             file to backup
     * @param accountId     account for which file should be backed up
     * @throws BackupException  if there is a problem scheduling the backup.  No exception is
     *                           thrown in the main thread if the asynchronous part fails.
     */
    @Override
    public void backup(final String storageId, final String name, final File file) throws BackupException {
        
        log.info("Scheduling backup for file " + file);
        
        int count = backupCounter.incrementAndGet();
        
        final String accountId = storageId;
        final String fileId = storageId +"."+file.getName();
        final File marker = new File("data/backup_queue/"+fileId+"."+count);
        try {
            // Create a backup marker file
            if(!marker.getParentFile().exists()) 
                marker.getParentFile().mkdirs();
            
            FileWriter w = new FileWriter(marker);
            w.write(file.getAbsolutePath());
            w.flush();
            w.close();
            log.debug("Wrote marker file " + marker);
        }
        catch (IOException e) {
            throw new BackupException("Unable to write backup marker file: " + marker.getAbsolutePath(),e);
        }
        
        final long backupScheduleTimeMs = System.currentTimeMillis();
        
        backupExecutor.execute(new Runnable() {
            int attempts = 0;
            public void run() {
                try {
                    
                    Metric.addSample("SmallFileBackupService.BackupDelayMs", System.currentTimeMillis() - backupScheduleTimeMs);
                    
                    // Don't let the same file get backed up concurrently
                    synchronized(activeBackups) {
                        if(activeBackups.contains(fileId)) {
                            log.info("Concurrent backup of same file " + file.getName() + " in progress: rescheduling backup.");
                            backupExecutor.schedule(this, 1, TimeUnit.SECONDS);
                            return;
                        }
                        else
                            activeBackups.add(fileId);
                    }
                    
                    
                    long timeMs = System.currentTimeMillis();
                    AES aes = new AES();
                    File encryptedFile = new File(file.getAbsolutePath()+"__sfbs.tmp");
                    FileOutputStream byteStream = new FileOutputStream(encryptedFile);
                    GZIPOutputStream gzipStream = new GZIPOutputStream(aes.createOutputStream(byteStream, getKey()));
                    InputStream is = null;
                    try {
                        is = new FileInputStream(file);
                        copy(is, gzipStream);
	                    gzipStream.flush();
                    }
                    finally {
	                    closeQuietly(gzipStream);
	                    closeQuietly(is);
                    }
                    
                    backupService.backup(accountId, getBackupName(storageId, name), encryptedFile);
                    
                    // Delete the marker file
                    marker.delete();
                    encryptedFile.delete();
                    
                    log.info("Backup of file " + file + " succeeded");
                    Metric.addSample("SmallFileBackupService.BackupsCompleted");
                    Metric.addSample("SmallFileBackupService.BackupJobTimeMs", System.currentTimeMillis() - timeMs);
                }
                catch (Throwable t) {
                    ++attempts;
                    log.error("Failed to back up user profiles for " + accountId + " attempt " + attempts,t);
                    
                    // A very simplistic exponential backoff - first 10 seconds, then 100, then 1000 ...
                    final long delay = (long) (1 + Math.pow(10,attempts));
                    
                    // Record the failure
                    if(attempts < Configuration.getProperty("MaxS3BackupAttempts", 6)) {
                        Metric.addSample("SmallFileBackupService.BackupRetries");
                        log.info("Retry backup in " + delay + " seconds");
                        backupExecutor.schedule(this, delay, TimeUnit.SECONDS);
                    }
                    else {
                        Metric.addSample("SmallFileBackupService.Failures");
                        log.fatal("Failed to backup profiles for user " + accountId + " after " + attempts + " attempts");
                    }
                }
                finally {
                    synchronized (activeBackups) {
                      activeBackups.remove(fileId);    
                    }
                }
            }
        });        
    }
    
    /**
     * Restores a file by retrieving it from the backup service.
     * <p>
     * This operation executes synchronously - ie. it blocks until the backup
     * returns and the file is completely retrieved.
     */
    public void restore(String storageId, String name, File f) throws BackupException {
        try {
            log.info("Restoring file " + f + " for user " + storageId);
            InputStream in = openStream(storageId, name);
            if(in == null) {
                log.info("Requested file " + name + " not found in backup for storage id " + storageId);
                return;
            }
            
            FileOutputStream out = null;
            File tmpFile = new File(f.getAbsolutePath() + ".tmp");
            try {
                out = new FileOutputStream(tmpFile);
                copy(in, out);
                out.flush();
                
                if(f.exists())
                    if(!f.delete())
                        throw new BackupException("Unable to delete existing file " + f.getAbsolutePath());
                
                // Must close here or can't rename below
	            out.close();
                
                if(!tmpFile.renameTo(f)) {
                    tmpFile.delete();
                    throw new BackupException("Unable to rename backup file " + tmpFile + " to " + f.getAbsolutePath());
                }
                    
            }
            finally {
	            closeQuietly(in);
	            closeQuietly(out);
            }
            Metric.addSample("SmallFileBackupService.FilesRestored");
        }
        catch (BackupException e) {
            Metric.addSample("SmallFileBackupService.RestoreFailures");
            throw e;
        }
        catch (FileNotFoundException e) {
            Metric.addSample("SmallFileBackupService.RestoreFailures");
            throw new BackupException("Unable to restore backup file from S3: " + f, e);
        }
        catch (IOException e) {
            Metric.addSample("SmallFileBackupService.RestoreFailures");
            throw new BackupException("Unable to restore backup file from S3: " + f, e);
        }
    }
    
    @Override
    public InputStream openStream(String accountId, String name) throws BackupException {
        InputStream stream = null;
        InputStream aesStream = null;
        try {
            AES aes = new AES();
            stream = backupService.openStream(accountId, getBackupName(accountId,name));
            if(stream == null) {
                log.info("Unable to open stream to resource " + name + " for account " + accountId + ": not found in backup");
                return null;
            }
            aesStream = aes.createInputStream(stream, this.getKey());
            return new GZIPInputStream(aesStream); 
        }
        catch(Exception e) {
            closeQuietly(stream);
            closeQuietly(aesStream);
            throw new BackupException("Unable to open stream to document " + name + " for account " + accountId, e);
        }
    }

    @Override
    public void delete(String accountId, String name) throws BackupException {
        
        log.info("Deleting backup of small file: storageId=" + accountId + " file=" + name);
        backupService.delete(accountId, getBackupName(accountId, name));
    }
    
    /**
     * Calculates a file name for storing the given file in backup storage.  The file name
     * needs to be unique wrt to all other files that might be stored for the 
     * specified storage id by this appliance.
     */
    private String getBackupName(String storageId, String name) {
        return "__" + name + ".gz";
    }

    private void setKey(Key key) {
        this.key = key;
    }

    protected Key getKey() {
        return key;
    }
}
