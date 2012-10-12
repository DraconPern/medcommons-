/*
 * $Id$
 * Created on 04/07/2007
 */
package net.medcommons.router.services.ccr;

/**
 * The kind of storage associated with a document.  This is a logical
 * storage mode rather than physical - documents may actually 
 * be stored using a different kind of storage model to that of their
 * storage model.  However the storage model under which the document
 * was loaded allows customization of the UI and other behavior to reflect
 * that.  
 * 
 * @author ssadedin
 */
public enum StorageMode {
    /**
     * Document has not been stored at all
     */
    SCRATCH,
    /**
     * Document is fixed, cannot be updated.  Updates must be saved as new files.
     */
    FIXED,
    /**
     * Document is a logical document.  Updates to the document will
     * replace the existing document.
     */
    LOGICAL
}
