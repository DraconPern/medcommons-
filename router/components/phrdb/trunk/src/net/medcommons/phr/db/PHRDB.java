/*
 * $Id$
 * Created on 27/03/2007
 */
package net.medcommons.phr.db;

import net.medcommons.phr.DocumentNotFoundException;
import net.medcommons.phr.PHRDocument;
import net.medcommons.phr.PHRException;

/**
 * Defines a database capable of loading or storing a PHR.   
 * 
 * Where possible, implementations should use only methods from the
 * abstract PHRDocument interface, thereby allowing any compliant document
 * implementing that interface to be stored using the any PHRDB provider.
 * 
 * @author ssadedin
 */
public interface PHRDB {
    
    /**
     * Connect to a particular database
     */
    void connect(String connectString) throws PHRException;
    
    /**
     * Store the whole PHR 
     * @param id - name or id of document
     * @throws PHRException 
     */
    void save(String id, PHRDocument phr) throws PHRException;
    
    /**
     * Load the PHR for the given logical id
     */
    PHRDocument open(String id) throws PHRException;
    
    
    /**
     * Deletes the document completely.
     * 
     * @throws DocumentNotFoundException - if no document with given name exists
     */
    void delete(String id) throws PHRException;
    
    /**
     * Release any resources associated with database, if connected
     */
    void close() throws PHRException;
} 