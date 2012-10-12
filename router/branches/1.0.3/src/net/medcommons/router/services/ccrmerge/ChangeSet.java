/*
 * $Id$
 * Created on 24/08/2006
 */
package net.medcommons.router.services.ccrmerge;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

/**
 * A Change that models a set of other changes, executed sequentially 
 * 
 * @author ssadedin
 */
public class ChangeSet extends ArrayList<Change> implements Change {
    
    private String path = null;
    
    public ChangeSet(String path) {
        this.path = path;
    }
    
    /*public ChangeSet(Change...changes) {
        if(changes != null) {
            for (Change change : changes) {
                this.add(change);
            }
        }            
    }*/
    
    public CCRDocument apply(CCRDocument ccr) throws MergeException {
        CCRDocument result = ccr;
        for (Change change : this) {
            result = change.apply(result);            
        }
        return result;
    }
    
    /**
     * Over ride allows callers to conveniently attempt to add null elements
     * without affecting the ChangeSet.  This alleviates caller code
     * from having to test for nulls.
     */
    @Override
    public boolean add(Change change) {
        if(change != null)
            super.add(change);
        return true;
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public String toString() {
        List<String> changes = new ArrayList<String>();
        this.toString(changes); 
        StringBuilder result = new StringBuilder();
        for (String change : changes) {
            result.append(change+"\n");
        }
        return result.toString();
    }
    
    public void toXml(String rootPath, Element parent) {
        for (Change change : this) {
            change.toXml(rootPath+"/"+this.getPath(), parent);
        }
    }

    public void toString(List<String> changes) {
        if(changes != null) {
            List<String> childChanges = new ArrayList<String>();
            for (Change change : this) {
                change.toString(childChanges);
            }
            
            // Prefix each child change with our path and add to ourselves
            for (String childChange : childChanges) {
                changes.add(this.getPath()+"/"+childChange);
            }
        }
    }

}
