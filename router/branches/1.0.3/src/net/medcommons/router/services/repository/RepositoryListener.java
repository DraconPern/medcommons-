package net.medcommons.router.services.repository;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A listener that can receive events and insert
 * behavior into the repository.
 * 
 * @author ssadedin@medcommons.net
 */
public interface RepositoryListener {
    
    /**
     * Called at the beginning of a storage operation
     * @param evt
     * @throws RepositoryException 
     */
    void onBeginStoreDocument(RepositoryEvent evt) throws RepositoryException;
    
    /**
     * Called before input is read.
     */
    void onInput(RepositoryEvent evt) throws RepositoryException;
    
    /**
     * Called beore data is written
     */
    void onOutput(RepositoryEvent evt) throws RepositoryException;
    
    /**
     * Called at the end of a storage operation
     * @param evt
     */
    void onEndStoreDocument(RepositoryEvent evt) throws RepositoryException;
    
    /**
     * Called if the specified file could not be resolved
     */
    void onFileUnavailable(RepositoryEvent evt, File f) throws RepositoryException;
    
    /**
     * Called when a document is deleted
     */
    void onDelete(RepositoryEvent evt) throws RepositoryException;
}
