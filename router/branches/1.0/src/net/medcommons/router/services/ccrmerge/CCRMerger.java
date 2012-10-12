/*
 * $Id$
 * Created on 23/08/2006
 */
package net.medcommons.router.services.ccrmerge;

import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

/**
 * A component that supports merging one CCR into another.  Elements in the 
 * "from" document will be merged into the "to" document.
 * 
 * @author ssadedin
 */
public interface CCRMerger {
    
    /**
     * Test if the given two elements share 'identity', meaning that they 
     * should be treated as the same node for merging purposes.  When nodes
     * share identity their contents are merged, as opposed to both nodes
     * being included separately in the merge output. 
     * 
     * @param from
     * @param to
     * @return
     * @throws MergeException
     */
    boolean match(CCRElement from, CCRElement to) throws MergeException;
    
    /**
     * Merge contents of element 'from' into element 'to'.
     * 
     * @param from - element to merge 'from'
     * @param toDocument TODO
     * @param to - element to merge into
     * @return
     * @throws MergeException
     */
    Change merge(CCRElement from, CCRDocument toDocument, CCRElement to) throws MergeException;
    
    /**
     * Import contents of from into toParent as a child node
     * 
     * @param from
     * @param toDocument TODO
     * @param to
     * @throws MergeException
     */
    Change importNode(CCRElement from, CCRDocument toDocument, CCRElement toParent) throws MergeException;

}
