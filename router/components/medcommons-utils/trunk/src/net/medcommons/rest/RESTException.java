/*
 * $Id: $
 * Created on Oct 10, 2004
 */
package net.medcommons.rest;

import org.jdom.Document;

/**
 * Thrown when a REST call fails
 * 
 * @author ssadedin
 */
public class RESTException extends Exception {

    /**
     * Response XML for the bad request, where available
     */
    private Document document;
    
    /**
     * Summary status from the call
     */
    private String status;

    public RESTException() {
        super();
    }

    /**
     * @param document
     */
    public RESTException(Document document) {
        super(document.getRootElement().getChildText("summary_status"));
        this.document = document;
        this.status = document.getRootElement().getChildText("summary_status");
    }
    
    /**
     * @param message
     */
    public RESTException(String message,Document document) {
        super(message);
        this.document = document;
        this.status = document.getRootElement().getChildText("summary_status");
    }
    
    /**
     * @param message
     */
    public RESTException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public RESTException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public RESTException(Throwable cause) {
        super(cause);
    }

    public Document getDocument() {
        return document;
    }

    public String getStatus() {
        return status;
    }
}
