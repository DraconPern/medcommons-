package net.medcommons.modules.backup.test;

import junit.framework.TestCase;
import net.medcommons.modules.backup.BackupQueue;
import net.medcommons.modules.backup.BackupQueueEntry;

import org.apache.log4j.Logger;

public class BackupQueueTest extends TestCase {
	private static Logger log = Logger.getLogger(BackupQueueTest.class
			.getName());

	private static int TEST_N_TIMES = 1024; // 
	
	public void testInsertOneTBActive() {
		insertOneTB(true);
	}

	public void testInsertOneTBInactive() {
		insertOneTB(false);
	}

	public void testInsertBigSizeActive() {
		insertBigSize(true);
	}

	public void testInsertBigSizeInactive() {
		insertBigSize(false);
	}

	/**
	 * Test to see if there are any resource leaks (e.g, dangling sessions) which
	 * would cause crashes after a few thousand transactions.
	 *
	 */
	public void testInsertNTimes(){
		log.info("About to test inserts " + TEST_N_TIMES + " times");
		for (int i=0;i<TEST_N_TIMES;i++){
			insertOneTB(true);
		}
	}
	public void insertOneTB(boolean backupActive) {
	    
		if(!backupActive) {
		    return;
		}
		
		BackupQueueEntry newEntry = new BackupQueueEntry();

		newEntry.setGuid("I AM NOT A GUID");

		Long accountId = new Long(1234567890123456L);
		newEntry.setAccountId(accountId);
		newEntry.setStatus(BackupQueue.QUEUED);
		long bigSize = 1024L * 1024L * 1024L * 1024L; // 1TB
		newEntry.setSize(new Long(bigSize));

		newEntry.setStarttime(null);
		BackupQueue backupQueue = new BackupQueue(null);
		BackupQueueEntry insertedEntry = backupQueue.insert(newEntry);
		
		assertNotNull(insertedEntry);
		log.info("Inserted entry: id=" + insertedEntry.getId()
		        + ", accountId =" + insertedEntry.getAccountId()
		        + ", guid=" + insertedEntry.getGuid() + ", queued time="
		        + insertedEntry.getQueuetime() + ", size= "
		        + insertedEntry.getSize());
		// Delete entry so that it doesn't clog the queue table.
		backupQueue.delete(insertedEntry);
		log.info("Deleted entry: id=" + insertedEntry.getId());

	}

	public void insertBigSize(boolean backupActive) {
		if(!backupActive) {
		    return;
		}
		
		BackupQueueEntry newEntry = new BackupQueueEntry();
		newEntry.setGuid("I AM NOT A GUID2");

		Long accountId = new Long(1234567890123456L);
		newEntry.setAccountId(accountId);
		newEntry.setStatus(BackupQueue.QUEUED);

		newEntry.setSize(new Long(999999999999999L));
		newEntry.setStarttime(null);
		BackupQueue backupQueue = new BackupQueue(null);
		BackupQueueEntry insertedEntry = backupQueue.insert(newEntry);
		assertNotNull(insertedEntry);
		log.info("Inserted entry: id=" + insertedEntry.getId()
		        + ", accountId =" + insertedEntry.getAccountId()
		        + ", guid=" + insertedEntry.getGuid() + ", queued time="
		        + insertedEntry.getQueuetime() + ", size= "
		        + insertedEntry.getSize());
		// Delete entry so that it doesn't clog the queue table.
		backupQueue.delete(insertedEntry);
		
		log.info("Deleted entry: id=" + insertedEntry.getId());
		
	}
}
