/*
 * $Id: BackupService.java 3501 2009-10-08 21:39:26Z ssadedin $
 * Created on 21/10/2008
 */
package net.medcommons.modules.services.interfaces;

import java.io.File;
import java.io.InputStream;

/**
 * Supplies services for backing up files for a MedCommons Repository.
 * 
 * @author ssadedin
 */
public interface BackupService {
    
    /**
     * Cause a file f to be backed up for the given account, such that
     * it can be restored by calling {@link #restore(String, String, File)},
     * or a stream opened to it by calling {@link #openStream(String, String)}.
     * <p>
     * Note: depending on the implementation, the backup may be executed asynchronously.
     * 
     * @param accountId     account id of owner of file ("storage id")
     * @param name          name under which to store backed up contents
     * @param f             File to be backed up
     */
    void backup(String accountId, String name,  File f) throws BackupException;

    /**
     * Restores a file that was previously backed up using 
     * {@link #scheduleBackup(String, File)}.
     * 
     * @param accountId     account id of owner of file ("storage id")
     * @param name          name of entity to be restored
     * @param f             File to be restored to
     * @throws BackupException
     */
    void restore(String accountId, String name, File f) throws BackupException;
    
    /**
     * Locate the specified backed up resource and return an open 
     * stream to enable reading from it.  The stream must be closed by
     * the calling code.
     * <p>
     * If the specified resource is not available then an exception is
     * not thrown, rather <code>null</code> is returned.
     * 
     * @param accountId
     * @param name
     * @return
     * @throws BackupException
     */
    InputStream openStream(String accountId, String name) throws BackupException;

    /**
     * Deletes the backup of the specified resource, if it exists.  The 
     * resource may not have been backed up and an exception should
     * not be thrown if called for such a resource.
     * 
     * @throws BackupException
     */
    void delete(String accountId, String name) throws BackupException;
}
