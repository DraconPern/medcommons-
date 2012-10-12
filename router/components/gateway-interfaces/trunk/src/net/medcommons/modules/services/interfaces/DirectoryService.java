/*
 * $Id$
 * Created on 31/05/2006
 */
package net.medcommons.modules.services.interfaces;

import java.util.List;

public interface DirectoryService {
    
    /**
     * Querys for contact information in the given context filtered by the given
     * parameters which may be passed as null.
     */
    List<DirectoryEntry> query(String context, String externalId, String alias, String accid) throws ServiceException;
    
}
