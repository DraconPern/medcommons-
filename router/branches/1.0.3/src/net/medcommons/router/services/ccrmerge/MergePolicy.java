/*
 * $Id: MergePolicy.java 2721 2008-07-04 22:58:11Z ssadedin $
 * Created on 27/03/2007
 */
package net.medcommons.router.services.ccrmerge;

import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

public interface MergePolicy {
    
    /**
     * Returns true if a merge between the two documents is acceptable  
     * 
     * @param mergeFrom
     * @param mergeTo
     * @return
     * @throws MergeException 
     */
    PolicyResult canMerge(CCRDocument mergeFrom, CCRDocument mergeTo) throws MergeException;

}
