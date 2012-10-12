/*
 * $Id$
 * Created on 05/09/2006
 */
package net.medcommons.router.services.ccrmerge;

import java.util.List;

import org.jdom.Element;

import net.medcommons.router.services.ccr.CCRChangeElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

public class CommentChange implements Change {
    public static enum CommentType {
        SUBJECTIVE,
        OBJECTIVE,
        ASSESSMENT,
        PLAN
    }
    
    /**
     * The type of comment that was changed (one of the SOAP types)
     */
    private String type;
    
    /**
     * The operation that was performed
     */
    private ChangeOperation operation;

    /**
     * Creates a comment change of the given type
     */
    public CommentChange(String type, ChangeOperation operation) {
        super();
        this.type = type;
        this.operation = operation;
    }

    public CCRDocument apply(CCRDocument ccr) throws MergeException {
        return null;
    }

    public void toString(List<String> changes) {
        changes.add(type + " " + operation.toString() );
    }

    public void toXml(String rootPath, Element parent) {
        CCRChangeElement changeElement = new CCRChangeElement("Change");
        changeElement.addContent(new Element("Location").setText(rootPath))
              .addContent(new Element("Operation").setText("Added Comment"));        
        parent.addContent(changeElement);
    }

}
