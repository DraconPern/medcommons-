/*
 * $Id: TransferStatusService.java 3166 2009-01-06 07:39:07Z ssadedin $
 * Created on 12/12/2008
 */
package net.medcommons.modules.services.interfaces;

import java.util.List;

/**
 * Service for notifying / managing state of DICOM uploads
 * 
 * @author ssadedin
 */
public interface TransferStatusService {
    
    TransferState put(TransferState status) throws ServiceException, OutOfDateException;
    
    TransferState get(String accountId, String key) throws ServiceException;
    
    List<TransferState> list(String accountId) throws ServiceException;
    
    /**
     * Log a message 
     */
    void addMessage(TransferMessage message) throws ServiceException;

}
