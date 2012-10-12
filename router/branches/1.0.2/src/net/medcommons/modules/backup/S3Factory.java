package net.medcommons.modules.backup;

import static net.medcommons.modules.utils.Str.blank;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.s3.S3Client;

import org.apache.log4j.Logger;

/**
 * Creates and configures S3Client instances with 
 * credentials and other settings appropriate for backing 
 * up data to S3.
 * 
 * @author ssadedin
 */
public class S3Factory {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(S3Factory.class);
    
    /**
     * Secret for signing S3 API calls
     */
    private String s3secret;

    /**
     * Amazon S3 ID
     */
    private String s3key_id;
    
    /**
     * Bucket to which profiles should be backed up
     */
    private String bucket;
    
    /**
     * System services factory for locating other services
     */
    private ServicesFactory services;
    
    /**
     * Creat a factory for producing S3 client instances
     * 
     * @param services
     * @throws BackupException
     */
    public S3Factory(ServicesFactory services) throws BackupException {
        super();
        this.services = services;
        init();
    }

    /**
     * Initialize the service by acquiring or creating an encryption key
     * and S3 credentials.
     * @throws BackupException 
     */
    protected void init() throws BackupException {
        try {
            boolean backupEnabled = Configuration.getProperty("Backup_Documents", false);
            if(backupEnabled) {
                s3secret = Configuration.getProperty("S3_Restore_Secret");
                s3key_id = Configuration.getProperty("S3_Restore_Key_ID");
                if(blank(s3secret) || blank(s3key_id)) 
                    throw new BackupException("Backup enabled but S3_Restore_Secret or S3_Restore_Key_ID is not configured");
                    
                bucket = Configuration.getProperty("S3_Restore_Bucket");
                if(blank(bucket)) 
                    throw new BackupException("Backup enabled but S3_Restore_Bucket is not configured");
                
                bucket = Configuration.getProperty("S3_Restore_Bucket");
            }
        }
        catch (ConfigurationException e) {
            throw new BackupException("Unable to create SmallFileBackupService", e);
        }
    }
    
    /**
     * Create and return an S3 client for the given storage id.  This requires
     * determining the appropriate devpay credentials for the user.
     * 
     * @return an S3 client to use for the given user
     */
    public S3Client createClient(String storageId) throws ServiceException {
        S3Client s3client = new S3Client(s3key_id, s3secret);
        AccountSettings settings = services.getAccountService().queryAccountSettings(storageId);
        if(!blank(settings.getAmazonUserToken())) {
            log.debug("Found user token " + settings.getAmazonUserToken() + " for profile backup");
            s3client.setProductToken(settings.getAmazonProductToken());
            s3client.setUserToken(settings.getAmazonUserToken());
        }
        return s3client;
    }

    /**
     * Create a generic client.  This should be used only when the storage should
     * not be billed to a specific account - the owner of the appliance will
     * pay for storage.  In general, calls should *not* be using this method.
     * 
     * @return a generic S3 client that will bill storage costs to the appliance owner
     */
    public S3Client createClient() {
        return new S3Client(s3key_id, s3secret);
    }

    public String getDefaultBucket() {
        return bucket;
    }
}
