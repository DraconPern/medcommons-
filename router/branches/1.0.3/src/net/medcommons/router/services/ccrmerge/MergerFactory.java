/*
 * $Id$
 * Created on 24/08/2006
 */
package net.medcommons.router.services.ccrmerge;

import java.io.IOException;

import org.jdom.JDOMException;

import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

/**
 * Creates Mergers for updating CCRs
 * 
 * @author ssadedin
 */
public abstract class MergerFactory {
    
    private static MergerFactory instance;
   
    /**
     * Creates a merger for a particular element inside a document 
     */
    public abstract CCRMerger create(CCRElement element)  throws MergeException;
    
    /**
     * Creates a merger for the root level document in to and calls it with
     * the root node of the to and from CCRs to merge them.
     */
    public static Change merge(CCRDocument from, CCRDocument to) throws MergeException {
        try {
            CCRElement fromRoot = from.getRoot();
            return MergerFactory.getInstance().create(fromRoot).merge(fromRoot, to, (CCRElement)to.getJDOMDocument().getRootElement());
        }
      
        catch (PHRException e) {
            throw new MergeException(e);
        }
    }
   
    
    public static MergerFactory getInstance() {
       if(instance == null)
           instance = new DefaultMergerFactory(); 
       return instance;
    }
    
}
