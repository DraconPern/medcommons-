package net.medcommons.modules.backup;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.*;

import net.medcommons.modules.services.interfaces.BackupException;
import net.medcommons.modules.services.interfaces.BackupService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.s3.S3Client;

import org.apache.log4j.Logger;

/**
 * An implementation of a backup provider for S3.
 * 
 * @author ssadedin
 */
public class S3BackupService implements BackupService {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(S3BackupService.class);
    
    S3Factory s3factory;
    
    public S3BackupService(S3Factory s3factory) {
        super();
        this.s3factory = s3factory;
    }

    @Override
    public void backup(String accountId, String name, File f) throws BackupException {
        log.debug("Sending file " + f.getAbsolutePath() + " to S3 under name " + name + " for account " + accountId);
        try {
            String backupName = getBackupName(accountId, name);
            S3Client s3client = s3factory.createClient(accountId);
            s3client.put(s3factory.getDefaultBucket(),  backupName,  null, f);
        }
        catch (ServiceException e) {
            throw new BackupException("Unable to send file " + f.getAbsolutePath() + " to S3",e);
        }
        catch (IOException e) {
            throw new BackupException("Unable to send file " + f.getAbsolutePath() + " to S3",e);
        }
    }

    private String getBackupName(String accountId, String name) {
        String backupName = accountId+"__"+name;
        return backupName;
    }
    @Override
    public InputStream openStream(String accountId, String name) throws BackupException {
        log.info("Opening stream to resource " + name + " for account " + accountId);
        try {
            S3Client s3client = s3factory.createClient(accountId);
            return s3client.getInputStream(s3factory.getDefaultBucket(), getBackupName(accountId,name));
        }
        catch (ServiceException e) {
            throw new BackupException("Unable to open stream for backed up entity " + name,e);
        }
        catch (IOException e) {
            throw new BackupException("Unable to open stream for backed up entity " + name,e);
        }
    }

    @Override
    public void restore(String accountId, String name, File f) throws BackupException {
        log.info("Restoring " + name + " for account " + accountId + " in location " + f.getAbsoluteFile());
        OutputStream out = null;
        try {
            S3Client s3client = s3factory.createClient(accountId);
            
            // Want to avoid partial downloads getting treated as the real file so 
            // we download to a temp file and only rename it when we are sure that we got 
            // the whole thing.
            File tmp = new File(f.getAbsolutePath() + "__s3__tmp"+System.currentTimeMillis());
            out = new FileOutputStream(tmp);
            int status = s3client.get(s3factory.getDefaultBucket(), getBackupName(accountId,name), out);
            if(status == 404) {
                tmp.delete();
                return;
            }
            
            if(status >= 400) 
                throw new BackupException("Unexpected HTTP return code " + status + " when restoring document " + name + " for account " + accountId + " from S3");
                
            if(f.exists())
                if(!f.delete())
                    throw new BackupException("Unable to delete existing file " + f.getAbsolutePath() + " for replacement  with backup copy");
            
            out.close();
            
            if(!tmp.renameTo(f))
                throw new BackupException("Unable to rename backup file " + tmp.getAbsolutePath() + " to restore target " + f.getAbsolutePath());
        }
        catch(Exception e) {
            throw new BackupException("Unable to restore file " + name + " to location " + f,e);
        }
        finally {
            closeQuietly(out);
        }
    }

    @Override
    public void delete(String accountId, String name) throws BackupException {
        try {
            S3Client s3client = s3factory.createClient(accountId);
            int status = s3client.delete(s3factory.getDefaultBucket(), getBackupName(accountId,name));
            if(status == 404) {
                log.info("Attempt to delete non-existent resource " + name + " for account " + accountId);
                return;
            }
            
            if(status >= 400)
                throw new BackupException("Failed to delete backup for resource " 
                        + accountId + " / " +  name + ".  Status " + status + " returned from S3");
        }
        catch(BackupException e) {
            throw e;
        }
        catch(Exception e) {
            throw new BackupException("Failed to delete backup for resource " + accountId + " / " +  name, e);
        }
    }
}
