/*
 * $Id$
 * Created on 27/03/2007
 */
package net.medcommons.phr;

import java.util.List;

import org.jdom.Document;


/**
 * An abstract Document representing a Personal Health Record, or part thereof.
 * 
 * The primary interface consists of methods to set and retrieve named properties
 * of the health record, methods to validate the document, and optionally 
 * for documents that are convertible to XML, methods to return XML document interface.  
 * 
 * @author ssadedin
 */
public interface PHRDocument {
    /**
     * Return the value at the given path
     * @throws PHRException 
     */
    String getValue(String path) throws PHRException;
    
    /**
     * Update the attribute at the given path with the given value.
     * If the attribute does not exist, create it. The path must
     * evalueate to a single valued property.
     * 
     * @param phr
     * @param attribute
     * @param value
     */
    PHRDocument setValue(String attribute, String value) throws PHRException;
    
    /**
     * Validate the document and throw a validation exception if not valid
     */
    ValidationResult validate() throws PHRException;

    /**
     * Delete the attribute at the given location, if it exists
     */
    void remove(String attribute) throws PHRException;    
    
    /**
     * Return this document in JDOM Compatible form
     */
    Document getDocument();
    
    /**
     * Return the root node of this document
     */
    PHRElement getRoot();
    
    /**
     * Query the document for the given property and return it.
     */
    PHRElement queryProperty(String path, String[]... params) throws PHRException;
    
    /**
     * Query all values of the given property
     */
    List<String> queryTextValues(String path, String[]... params) throws PHRException;
}
