/*
 * $Id$
 * Created on 25/08/2006
 */
package net.medcommons.router.services.ccrmerge;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

/**
 * Specialized merge algorithm for DateOfBirth elements.
 * 
 * Simply replaces the children of to with those of from.
 * 
 * @author ssadedin
 */
public class DateOfBirthMerger implements CCRMerger {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DateOfBirthMerger.class);
    
    public DateOfBirthMerger() {
        boolean foo = true;
    }

    public Change merge(CCRElement from, CCRDocument toDocument, CCRElement to) throws MergeException {
        
        to.removeContent();
        List<CCRElement> children = from.getChildren();
         
        for (CCRElement child : children) {
            to.addChild((CCRElement) child.clone());
        }
        return new XPathChange(to.getName(), ChangeOperation.UPDATE);
    }

    public boolean match(CCRElement from, CCRElement to) {
        return from.getName().equals(to.getName());
    }

    public Change importNode(CCRElement from, CCRDocument toDocument, CCRElement toParent) throws MergeException {
        return null;
    }

}
