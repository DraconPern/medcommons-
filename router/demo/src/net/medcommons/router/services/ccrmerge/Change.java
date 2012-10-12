/*
 * $Id$
 * Created on 24/08/2006
 */
package net.medcommons.router.services.ccrmerge;

import java.util.List;

import org.jdom.Element;

import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

public interface Change {
    
    /**
     * Apply this change to the given CCR
     */
    public abstract CCRDocument apply(CCRDocument ccr) throws MergeException;
    
    /**
     * Returns user readable representation of changes embodied by this change 
     * 
     * @param changes
     */
    public void toString(List<String> changes);
    
    /**
     * Serialize change into JDOM XML form
     * 
     * @param parent
     */
    public void toXml(String rootPath, Element parent);
}
