/*
 * $Id: XPathChange.java 1902 2007-08-02 04:43:45Z ssadedin $
 * Created on 05/09/2006
 */
package net.medcommons.router.services.ccrmerge;

import java.util.List;

import org.jdom.Element;

import net.medcommons.router.services.ccr.CCRChangeElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

public class XPathChange extends Object implements Change {
    
    private String path;
    
    private ChangeOperation operation;
    
    private String value;

    public ChangeOperation getOperation() {
        return operation;
    }

    public void setOperation(ChangeOperation operation) {
        this.operation = operation;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Creates a change for the given path, value and operation
     */
    public XPathChange(String path, ChangeOperation operation, String value) {
        super();
        this.path = path;
        this.operation = operation;
        this.value = value;
    }
    
    /**
     * Creates a change for the given path, and operation
     */
    public XPathChange(String path, ChangeOperation operation) {
        super();
        this.path = path;
        this.operation = operation;
    }
    
     public CCRDocument apply(CCRDocument ccr) throws MergeException {
        return null;
    }

    public void toString(List<String> changes) {
        changes.add(this.path+" "+this.operation);
    }

    public void toXml(String rootPath, Element parent) {
        CCRChangeElement changeElement = new CCRChangeElement("Change");
        changeElement.addContent(new Element("Location").setText(rootPath+"/"+this.path));
        changeElement.addContent(new Element("Operation").setText(this.operation.toString()));
        parent.addContent(changeElement);
    }
}
