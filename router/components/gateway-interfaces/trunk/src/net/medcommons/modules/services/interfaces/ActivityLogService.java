/*
 * $Id$
 * Created on 10/01/2007
 */
package net.medcommons.modules.services.interfaces;

import java.io.IOException;
import java.util.Collection;

/**
 * An interface for logging account activity events 
 * 
 * @author ssadedin
 */
public interface ActivityLogService {
    /**
     * Causes all activity events for the account to be read
     */
    final int READ_ALL = -1;
    
    /**
     * Saves the given activity event 
     * 
     * @param event - the event to save
     * @throws IOException 
     */
    void log(ActivityEvent event) throws IOException;
    
    /**
     * Returns the requested rows from the user's activity log
     * 
     * @param accountId
     * @return
     * @throws IOException 
     */
    Collection<ActivityEvent> load(String accountId, int begin, int limit) throws IOException; 
    
    /**
     * Clears all entries from the activity log for the given account
     * 
     * @throws ServiceException
     */
    public void clear(String accountId) throws ServiceException;
}
