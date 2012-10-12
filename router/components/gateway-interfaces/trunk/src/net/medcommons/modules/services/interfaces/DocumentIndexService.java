/*
 * $Id: DocumentIndexService.java 3105 2008-12-02 10:09:07Z ssadedin $
 * Created on 03/11/2008
 */
package net.medcommons.modules.services.interfaces;

import java.util.Date;
import java.util.List;

/**
 * Service for managing indexes of user documents.
 * 
 * @author ssadedin
 */
public interface DocumentIndexService {
    
    /**
     * Index the given document for the given user
     */
    public void index(DocumentDescriptor desc) throws ServiceException;
    
    /**
     * Query for documents for the given user
     * @return
     */
    public List<DocumentDescriptor> getDocuments(String storageId, Date from, Date to, DocumentDescriptor matching) throws ServiceException;
    
    /**
     * Look up a specific document and return its descriptor
     * 
     * @param storageId
     * @param guid
     * @return the descriptor for the specified document or null if the document is not found
     */
    public DocumentDescriptor getDocument(String storageId, String guid);

    /**
     * Clear the index for the specified storage id
     * <p>
     * This is a function for tests and test users only!
     * <b>DO NOT call this function for real users.</b>
     * 
     * @param storageId
     */
    public void clear(String storageId);
}
