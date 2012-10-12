package net.medcommons.modules.backup;

import static org.apache.commons.io.FileUtils.copyFile;

import java.io.*;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.BackupException;
import net.medcommons.modules.services.interfaces.BackupService;

import org.apache.log4j.Logger;

/**
 * A very simple backup service that backs files up to a file system location.
 * This is only really for test purposes, but it may have a practical application
 * at some time in the future.
 * 
 * @author ssadedin
 */
public class FileSystemBackupService implements BackupService {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(FileSystemBackupService.class);
    
    File backupLocation = new  File(Configuration.getProperty("FileSystemBackupLocation", "data/backup"));

    public FileSystemBackupService() {
        if(!backupLocation.exists()) 
            backupLocation.mkdirs();
        
        if(!backupLocation.exists()) 
            throw new RuntimeException("Unable to create backup location " + backupLocation.getAbsolutePath());
    }

    @Override
    public void backup(String accountId, String name, File f) throws BackupException {
        log.info("Backing up file " + f);
        File target = getTargetFile(accountId, name);
        try {
            copyFile(f, target);
        }
        catch(IOException e) {
            throw new BackupException("Failed to copy file " + f.getAbsolutePath() + " to backup location " + target.getAbsolutePath());
        }
    }

    private File getTargetFile(String accountId, String name) {
        return new File(backupLocation, accountId + "/" + name);
    }

    @Override
    public InputStream openStream(String accountId, String name) throws BackupException {
        File target = getTargetFile(accountId, name);
        log.info("Opening stream to backup resource " + target.getAbsolutePath());
        
        if(!target.exists())
            return null;
        
        try {
            return new FileInputStream(target);
        }
        catch (FileNotFoundException e) {
            throw new BackupException("Failed to open backed up file " + target.getAbsolutePath() + " to return stream", e);
        }
    }

    @Override
    public void restore(String accountId, String name, File f) throws BackupException {
        File target = getTargetFile(accountId, name);
        try {
            copyFile(target, f);
        }
        catch(IOException e) {
            throw new BackupException("Failed to copy file " + f.getAbsolutePath() + " from backup location " + target.getAbsolutePath());
        }
    }

    @Override
    public void delete(String accountId, String name) throws BackupException {
        File target = getTargetFile(accountId, name);
        if(target.exists()) {
            if(!target.delete())
                throw new BackupException("Unable to delete backup file " 
                        + target.getAbsolutePath() + " for resource " + accountId + " / " + name);
        }
    }
}
