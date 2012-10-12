/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr;

import java.util.List;


/**
 * A discrete piece of information in a PHR.  PHRElement is an interface 
 * provided by providers of PHR implementations that represents a single 
 * data value in a record. 
 * 
 * @author ssadedin
 */
public interface PHRElement {
    
    /**
     * Execute the given query local to the context of this node  
     * @throws PHRException 
     */
    PHRElement queryProperty(String path) throws PHRException;
    
    /**
     * Query for the given property and return the value as a string,
     * or null if it does not exist.
     */
    String queryTextProperty(String path) throws PHRException;
    
    /**
     * Returns the name of this element
     */
    String getName();
    
    /**
     * Get the text value of this element
     */
    String getElementValue();
    
    /**
     * Set the text value of this element
     */
    void setElementValue(String value);
    
    /**
     * Create the given path relative to this element, specified in pseudo
     * xpath form foo/bar/fubar.  Sets value as text content of created node.
     */
    PHRElement createPath(String xpath, String value);
    
    /**
     * Creates the given named attribute, relative to this element
     * @throws PHRException 
     */
    PHRElement createProperty(String attribute) throws PHRException;
    
    /**
     * Adds given child to this element.  Element is inserted in correct element 
     * order if the PHR type has strict ordering for elements of this type, 
     * otherwise it is simply appended to the end.
     */
    PHRElement addChild(PHRElement child);
    
    /**
     * Removes the given child from the parent, returning the child only if it is removed.
     */
    PHRElement removeChild(PHRElement child);
    
    /**
     * Replace the old child with the new one at the same position.
     */
    void replaceChild(PHRElement oldChild, PHRElement newChild);
    
    /**
     * Return the parent node of this one (if any)
     * @return
     */
    PHRElement getParentElement();
    
    /**
     * Returns the child of the given name, creating it if it does not exist.
     */
    PHRElement getOrCreate(String name);
    
    /**
     * Return the first child of given name
     */
    PHRElement getChild(String name);
    
    /**
     * Return all children of this element
     */
    List<PHRElement> getChildren();
    
    /**
     * Remove all children and return them in the given list
     */
    List<PHRElement> removeContent();

}
