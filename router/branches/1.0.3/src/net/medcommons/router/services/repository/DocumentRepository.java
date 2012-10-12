/*
 *  Copyright 2005 MedCommons Inc.   All Rights Reserved.
 * 
 * Created on Jun 27, 2005
 *
 * 
 */
package net.medcommons.router.services.repository;

import java.io.File;
import java.util.Properties;

import net.medcommons.modules.xml.RegistryDocument;

/**
 * @author sean
 *
 * This API needs to change.
 * Very CCR-centric. Doesn't permit access to compound documents.
 */
public interface DocumentRepository {
	
	
    
    /**
     * Loads a specific document via its guid
     * 
     * @param medcommonsId
     * @return
     * @throws RepositoryException
     */
	public abstract RegistryDocument queryDocument(String storageId, String guid) throws RepositoryException;
    
    /**
     * Deletes the given document from the repository.
     * 
     * <i>Warning: deleting content can have serious consequences for the integrity
     * of references in the system.  <b>Do not use this method without full
     * consideration of how you are handling dependencies.</b></i>
     * 
     * @param guid
     * @throws RepositoryException
     */
	public abstract void deleteDocument(String storageId, String guid) throws RepositoryException;
    

    /**
     * Physically deletes the given content, including all references.
     * 
     * <i>Warning: deleting content can have serious consequences for the integrity
     * of references in the system.  <b>Do not use this method without full
     * consideration of how you are handling dependencies.</b></i>
     * 
     * @param queryString
     * @throws RepositoryException
     */
    public abstract void deleteContent(String storageId, String queryString) throws RepositoryException;
    
    /**
     * Return the stored repository content type for the given document guid
     * @throws RepositoryException 
     */
    public abstract String getContentType(String storageId, String guid) throws RepositoryException;
    
    /**
     * Return the stored repository content length for the given document guid
     * @throws RepositoryException 
     */
    public abstract long getContentLength(String storageId, String guid) throws RepositoryException;
    
    /**
     * Return an arbitrary property of a document, if available
     * @throws RepositoryException 
     */
    public abstract Properties getProperties(String storageId, String guid) throws RepositoryException;
    
    /**
     * Returns true if document is in repository; false otherwise
     * @throws RepositoryException 
     */
    public abstract boolean inRepository(String storageId, String guid) throws RepositoryException;
    
    /**
     * Returns a scratch directory that can be used for writing temporary files. This directory may or
     * may not be in the repository file hierarchy.
     * @return
     */
    public abstract File getScratchDirectory();
    
    /**
     * Returns the path to the account directory for the given account.  This is only useful
     * if the repository is on the same computer as the caller. 
     */
    public abstract File getAccountDirectory(String accountId);
    
}