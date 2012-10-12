package net.medcommons.modules.services.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
/**
 * Service for retrieving documents from third party systems (such as 
 * EMC's Documentum content server).
 * 
 * It is assumed that the calling interface has obtained the 
 * reference information out of band (typically from the CCR References). 
 * Note that the reference information may not contain all the data
 * needed to obtain a document; an additional metadata is required because
 * not all the metadata information fits into the CCR (e.g., the CCR
 * identifies the series but not the individual images).
 * 
 * @author sean
 *
 */
public interface DocumentRetrievalService {
    
    
    public void startTransaction(Map<String, String> reference);
    
    public void closeTransaction();
    
    public Object retrieveMetadata() throws Exception;
    
    /**
     * Returns a document given the reference information and an object id (perhaps 
     * calculated, perhaps derived from the metadata).
     * @param reference
     * @param objectId
     * @return
     * @throws IOException
     */
    public InputStream retrieveDocument(Map<String, String> reference, String objectId) throws IOException;
    
}
