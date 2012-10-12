/*
 * $Id: Backups.java 3501 2009-10-08 21:39:26Z ssadedin $
 * Created on 28/10/2008
 */
package net.medcommons.router.selftests;

import static net.medcommons.modules.utils.Str.eq;
import static net.medcommons.modules.utils.TestDataConstants.USER1_ID;

import java.io.File;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.BackupService;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestResult;

/**
 * Tests that files can be written to and restored from S3 via the small file
 * backup service.
 * <p>
 * Note: this does not test the actual backup and restore of image and CCR content,
 * but it does ensure S3 credentials are correct.
 * 
 * @author ssadedin
 */
public class Backups implements SelfTest {

    public SelfTestResult execute(ServicesFactory services) throws Exception {
        
        if(Configuration.getProperty("Backup_Documents", false)) {
            BackupService backupService = Configuration.getBean("smallFileBackupService");
            
            if(backupService == null)
                throw new Exception("smallFileBackupService returned as null from spring factory:  check conf/medcommons-config.xml");
            
            // Let's write a file out and see if it can be restored
            File f = new File("data/Repository/"+USER1_ID + "/backup_test.txt");
            String fileContents = "Backup Test File " + System.currentTimeMillis();
            FileUtils.writeFile(f, fileContents);
            backupService.backup(USER1_ID, f.getName(), f);
            
            Thread.sleep(Configuration.getProperty("BackupsSelfTestWaitTimeMs", 6000));
            
            // Delete the file
            f.delete();
            
            if(f.exists())
                throw new Exception("Unable to delete test file " + f);
            
            // Restore the file
            backupService.restore(USER1_ID, f.getName(), f);
            
            if(!f.exists()) 
                throw new Exception("Test file " + f + " does not exist after being restored");
            
            String contents = FileUtils.readFile(f);
            if(!eq(contents, fileContents))
                throw new Exception("Test file " + f + " does not exist after being restored");
                
            // Delete file
            f.delete();
            if(f.exists())
                throw new Exception("Unable to delete test file " + f + " after restoring it");
            
        }
        
        return null;
    }

}
