/*
 * $Id: SmallFileBackupServiceTest.java 3501 2009-10-08 21:39:26Z ssadedin $
 * Created on 22/10/2008
 */
package net.medcommons.modules.backup.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.HashMap;

import net.medcommons.modules.backup.S3Factory;
import net.medcommons.modules.backup.SmallFileBackupService;
import net.medcommons.modules.crypto.AES;
import net.medcommons.modules.services.interfaces.BackupException;
import net.medcommons.modules.services.interfaces.BackupService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.modules.utils.TestDataConstants;
import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.router.util.BaseTestCase;
import net.medcommons.s3.S3Client;

import org.junit.Before;
import org.junit.Test;

public class SmallFileBackupServiceTest extends BaseTestCase {

    private File file = new File("data/Repository/"+TestDataConstants.USER1_ID+"/test.txt");
    

    HashMap flags = new HashMap();
    
    class DummyBackup implements BackupService {
        @Override
        public void backup(String accountId, String name, File f) throws BackupException {
        }

        @Override
        public void delete(String accountId, String name) throws BackupException {
        }

        @Override
        public InputStream openStream(String accountId, String name) throws BackupException {
            return null;
        }

        @Override
        public void restore(String accountId, String name, File f) throws BackupException {
        }
    };
    
    BackupService backup = new DummyBackup() {
        @Override
        public void backup(String accountId, String name, File f) throws BackupException {
           flags.put("obj",name);
           try { flags.put("count",FileUtils.readBytes(f)); } catch (IOException e) {  }
        }
    };
    
    byte [] bytes = new byte[1024];
        
    Key key = new AES().generateKey();
    
    int attempts = 0;
        
    public SmallFileBackupServiceTest() throws Exception {
        super();
    }

    @Before
    public void setUp() throws Exception {
        FileUtils.writeFile(file, "Unit Test!");
        attempts = 0;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testScheduleBackup() throws Exception {
        // Create backup service, override the methods we aren't testing
        new SmallFileBackupService(backup) {
            protected void init() {}
            protected Key getKey() { return key ; }
        }.backup(TestDataConstants.USER1_ID, file.getName(), file);
        
        Thread.sleep(3000);
        
        // Assume the backup should have executed by now!
        String object = (String) flags.get("obj");
        final int count = (Integer) flags.get("count");
        assertNotNull(object);
        assertTrue(count > 0);
        
        // bytes will be encrypted, so can't easily test them directly, but we can decrypt them
        
        file.delete();
        assertTrue(!file.exists());
        
        backup = new DummyBackup() {
            @Override
            public InputStream openStream(String accountId, String name) throws BackupException {
                return new ByteArrayInputStream(bytes,0,count);
            }
        };
        
        // Create backup service, override the methods we aren't testing
        new SmallFileBackupService(backup) {
            protected void init() {}
            protected Key getKey() { return key ; }
        }.restore(TestDataConstants.USER1_ID, file.getName(), file);
        
        assertTrue(file.exists());
        assertEquals(FileUtils.readFile(file),"Unit Test!");
    }
    
    /**
     * Test that if S3 returns an error during the backup then
     * a retry is attempted, and a marker file is present during the failure.
     */
    @Test
    public void testBackupFailure() throws Exception {
        
        backup = new DummyBackup() {
            @Override
            public void backup(String accountId, String name, File f) throws BackupException {
                attempts++;
                if(attempts == 1)
                    throw new BackupException("Dummy S3 Error");
            }
        };
        
        // Create backup service, override the methods we aren't testing
        new SmallFileBackupService(backup) {
            protected void init() {}
            protected Key getKey() { return key ; }
        }.backup(TestDataConstants.USER1_ID, file.getName(), file);
        
        Thread.sleep(1000);
        
        assertTrue(attempts == 1);
        
        File markerFile = new File("data/backup_queue/"+TestDataConstants.USER1_ID+"."+ file.getName()+"."+SmallFileBackupService.backupCounter.intValue());
        
        assertEquals(1L,Metric.getInstance("SmallFileBackupService.BackupRetries").getValue());
        
        // There should be a marker file
        assertTrue("marker file  " + markerFile + " should exist while backup in failure state", markerFile.exists());
        
        // The algorithm should back off for a bit and then retry a few seconds later
        Thread.sleep(12000);
        
        assertTrue(attempts == 2);
        
        // There should *not* be a marker file
        assertFalse(markerFile.exists());
    }
    
    BackupService testBackup = null;
    
    int fileCount = 0;
 
    byte[] s3bytes = new byte[512];
        
    /**
     * Test that the same file can be backed up with multiple entries in the queue
     */
    @Test
    public void testConcurrentSameFileBackup() throws Exception {
        
        backup = testBackup;
        
        // Create backup service, override the methods we aren't testing
        SmallFileBackupService svc = new SmallFileBackupService(backup) {
            protected void init() {}
            protected Key getKey() { return key ; }
        };
        
        long beforeCount = Metric.getInstance("SmallFileBackupService.BackupsCompleted").getValue().longValue();
        
        final BackupService [] backups = new BackupService[10];
        
        for(fileCount=0; fileCount<10; ++fileCount) {
            backups[fileCount] = testBackup = backup = new DummyBackup() {
                int len = 0;
                @Override
                public void backup(String accountId, String name, File f) throws BackupException {
                    // How do we assume S3 behaves?  Two concurrent PUTs for same object
                    // Assume the last to start writing wins?
                    synchronized(s3bytes) {
	                    try {
                            s3bytes = FileUtils.readBytes(f);
		                    len = s3bytes.length;
                        }
                        catch (IOException e) {
                            throw new BackupException(e);
                        }
                    }
                }
                @Override
                public InputStream openStream(String accountId, String name) throws BackupException {
                    return new ByteArrayInputStream(s3bytes,0,len);
                }
            };            
            
            // Write a new version of the file
            FileUtils.writeFile(file, "Unit Test " + fileCount);
            
            // Schedule a backup of it
            svc.backup(TestDataConstants.USER1_ID, file.getName(), file);            
            System.out.println("==========================> " + fileCount);
        }
        
        Thread.sleep(5000);        
        
        // Should have gotten 10 backups
        assertEquals(beforeCount+10, Metric.getInstance("SmallFileBackupService.BackupsCompleted").getValue().longValue());
        
        // Last one should be able to decrypt the file
        file.delete();
        assertTrue(!file.exists());

        svc.restore(TestDataConstants.USER1_ID, file.getName(), file);
        
        assertTrue(file.exists());
        assertEquals("Unit Test 9",FileUtils.readFile(file));        
        
        Thread.sleep(10000);
        
        // Should all be finished now
        
    }
}
