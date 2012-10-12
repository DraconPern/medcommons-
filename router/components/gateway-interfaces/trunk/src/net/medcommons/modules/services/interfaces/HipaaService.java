/*
 * $Id$
 * Created on 4/08/2005
 */
package net.medcommons.modules.services.interfaces;


/**
 * Responsible for handling all transactions involving the HIPAA log.
 * 
 * @author ssadedin
 */
public interface HipaaService {
    /**
     * Updates the status of the given tracking number to the given string.  It is
     * not determined yet whether this will update an existing line in the hipaa log
     * or create a new one, but a call to retrieve log lines for this track#
     * should return this entry as the most recent one as a post condition of this call.
     *
     * @param mcId - the medcommons id for which the tracking number is to be updated
     * @param trackingNumber - the tracking number to be updated
     * @param status - the new status for the entry
     */
     void updateHipaaStatus(String mcId, String trackingNumber, String status) throws ServiceException;
     
     
     /**
      * Retrieves lines from the HIPAA log for the given MedCommons Id and
      * tracking number.   The log lines are returned in an XML form.
      *
      * @param mcId - the medcommons id for which the tracking number is to be updated
      * @param trackingNumber - the tracking number to be updated
      */
     String query(String mcId, String trackingNumber, int maxLines) throws ServiceException;
     
}
